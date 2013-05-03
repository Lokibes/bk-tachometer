/*
 * tachometer_process.c
 *
 *  Created on: Apr 4, 2013
 *      Author: Minh Luan
 */

#include "tachometer_defs.h"
#include "tachometer_library.h"
#include "tachometer_history.h"
#include "fftw3.h"
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

#define DEBUG_MODE

#define FREQ_TO_INDEX_COEF	(1 / (((float)(TACHO_SAMPLING_FREQ >> 1)) / ((float) TACHO_FFT_OUT_LENGTH)))

// Create
void* Tachometer_Create() {
	Tacho_t* tacho_inst = (Tacho_t*) malloc(sizeof(Tacho_t));

	Tacho_History_Create(&(tacho_inst->tacho_history_inst));
	tacho_inst->fft_in = fftwf_malloc(sizeof(float) * TACHO_FFT_IN_LENGTH);
	tacho_inst->fft_out = fftwf_malloc(
			sizeof(fftwf_complex) * TACHO_FFT_IN_LENGTH);
	tacho_inst->fft_out_magnitude = (float*) malloc(
			sizeof(float) * TACHO_FFT_IN_LENGTH);
	tacho_inst->x = (float*) malloc(TACHO_FFT_OUT_LENGTH * sizeof(float));

	return tacho_inst;
}

// Initialize
int32_t Tachometer_Init(void* tacho) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1;
	}

	Tacho_History_Init(tacho_inst->tacho_history_inst);

	float* fft_in = tacho_inst->fft_in;

	// Zero padding the tacho_inst->fft_in
	int i;
	for (i = TACHO_AUTO_CORRELATION_LENGTH; i < TACHO_EXPECTED_LENGTH; i++) {
		fft_in[i] = 0.0f;
	}

	// Create the FFT plan
	// Because the plan is executed several times, and the initialized time is not very important,
	// FFT_MEASURE is preferred to FFTW_ESTIMATE
	tacho_inst->plan_forward = fftwf_plan_dft_r2c_1d(TACHO_FFT_IN_LENGTH,
			tacho_inst->fft_in, tacho_inst->fft_out, FFTW_ESTIMATE);

	// Initialize the variables for FFT magnitude output
	tacho_inst->newX = NULL;
	tacho_inst->currSize = 0;
	tacho_inst->currBeginFreq = 0;
	tacho_inst->currEndFreq = 0;
	tacho_inst->newXInitialized = false;

	// Initialize the x array
	float* x = tacho_inst->x;
	x[0] = 0;
	x[TACHO_FFT_OUT_LENGTH - 1] = TACHO_HALF_SAMPLING_FREQ;
	float delta = ((float)TACHO_HALF_SAMPLING_FREQ) / (TACHO_FFT_OUT_LENGTH - 1);
	for (i = 1; i < TACHO_FFT_OUT_LENGTH - 1; i ++) {
		x[i] = i * delta;
	}

	return 0;
}

// Free
int32_t Tachometer_Free(void* tacho) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1;
	}
	Tacho_History_Free(tacho_inst->tacho_history_inst);
	fftwf_free(tacho_inst->fft_in);
	fftwf_free(tacho_inst->fft_out);
	fftwf_destroy_plan(tacho_inst->plan_forward);
	free(tacho_inst->fft_out_magnitude);
	free(tacho_inst);

	return 0;
}

// Configuration
int32_t Tachometer_Config(void* tacho, int32_t estimatedFreq) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	int32_t estimatedIndex = (int32_t) (((float) estimatedFreq)
			* FREQ_TO_INDEX_COEF);

	// Imply that vector is not NULL
	int32_t beginIndex = estimatedIndex - TACHO_ESTIMATION_HALF_RANGE + 1;
	int32_t endIndex = estimatedIndex + TACHO_ESTIMATION_HALF_RANGE;

	if (beginIndex < 0) {
		beginIndex = 0;
	} else if (endIndex >= TACHO_FFT_OUT_LENGTH) { // The two cases cannot happen the same time
		int32_t tmp_shift = TACHO_FFT_OUT_LENGTH - endIndex;
		beginIndex += tmp_shift;
	} // End if (beginIndex < 0)

	tacho_inst->beginIndex = beginIndex;
	return 0;
}

// Process:
float Tachometer_Process(void* tacho, int16_t* inAudio) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	float resultFreq = 0.0f;
	if (tacho_inst == NULL) {
		return -1.0f;
	}

	/*
	 * Step 1: Autocorrelation to suppress the noise
	 * Step 2: FFT
	 * Step 3: Peak finding
	 */

	/*
	 * Step 1: Autocorrelation to suppress the noise
	 * Input: the inAudio array with TACHO_FRAME_LENGTH samples, 16 bits per each sample
	 * Output: the tacho->timeSeries array, 32 bits per each sample.
	 */
	Tachometer_AutoCorrelation(inAudio, tacho_inst->fft_in);

	/*
	 * Step 2: FFT
	 *
	 */
	fftwf_execute(tacho_inst->plan_forward);

	int i;
	float* restrict fft_out_magnitude = tacho_inst->fft_out_magnitude;
	fftwf_complex* restrict fft_out = tacho_inst->fft_out;
	for (i = tacho_inst->beginIndex;
			i < TACHO_ESTIMATION_RANGE + tacho_inst->beginIndex; i++) {
		fft_out_magnitude[i] = fft_out[i][0] * fft_out[i][0]
				+ fft_out[i][1] * fft_out[i][1];
	}

	/*
	 *	Step 3: Peak finding
	 *	Find the maximum index of the
	 */
	Tacho_History_t* tacho_history_inst = tacho_inst->tacho_history_inst;
	float bestFrequency = Tachometer_MaxFrequency(fft_out_magnitude,
			tacho_inst->beginIndex);
	int32_t ret = Tacho_History_Add_Last(tacho_history_inst, bestFrequency);
	if (ret == 1) {
		/*
		 * Enough data in the history
		 */
		float average = tacho_history_inst->sum
				* (1.0f / TACHO_HISTORY_CAPACITY);
		if (bestFrequency < (average * 1.08f)
				&& bestFrequency > (average * 0.92f)) {
			/*
			 * The frequency may be stable. It might be the rotary speed.
			 */
			tacho_history_inst->accept_times++;
			if (tacho_history_inst->accept_times >= TACHO_HISTORY_CAPACITY) {
				// TACHO_HISTORY_CAPACITY == 25: the frequency is stable for 1 second long
				// This frequency is the rotary frequency
				resultFreq = average / 2.0f; // Divide by 2.0f to fix the autocorrelation frequency doubling problem
				return resultFreq;
			}
		} else {
			/*
			 * The frequency is not stable, it cannot be the rotary frequency
			 */
			tacho_history_inst->accept_times = 0;
		}
	} else if (ret == 0) {
		/*
		 * Not enough data in the history
		 */
	} else {
		// ERROR
		return -1.0f;
	}

	return 0.0f; // Means that has not found a rotary frequency
}

int32_t Tachometer_FFT_Out(void* tacho, int32_t beginFreq, int32_t endFreq,
		int32_t size, float* fft_out_magnitude) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1;
	}

	if (tacho_inst->newXInitialized == true) {
		if (tacho_inst->currBeginFreq == beginFreq
				&& tacho_inst->currEndFreq == endFreq
				&& tacho_inst->currSize == size) {
			// Should not reconstruct
			// Interpolate
			Tachometer_Interpolation(tacho_inst->x,
					tacho_inst->fft_out_magnitude, TACHO_FFT_OUT_LENGTH,
					tacho_inst->newX, fft_out_magnitude, size);

		} else { // Should reconstruct
			// Reconstruct the newX array
			if (tacho_inst->currSize < size) {
				// Should reallocate the array
				tacho_inst->newX = realloc(tacho_inst->newX,
						size * sizeof(float));
			}
			float* newX = tacho_inst->newX;
			newX[0] = (float) beginFreq;
			newX[size - 1] = (float) endFreq;
			float delta = (newX[size - 1] - newX[0]) / (size - 1);

			int i;
			for (i = 1; i < size - 1; i++) {
				// Construct the newX array
				newX[i] = newX[0] + i * delta;
			}

			// Interpolate
			Tachometer_Interpolation(tacho_inst->x,
					tacho_inst->fft_out_magnitude, TACHO_FFT_OUT_LENGTH,
					tacho_inst->newX, fft_out_magnitude, size);

			tacho_inst->currBeginFreq = beginFreq;
			tacho_inst->currEndFreq = endFreq;
			tacho_inst->currSize = size;

		} // End if (tacho_inst->currBeginFreq == beginFreq && ...)
	} else { // tacho_inst has not been initialized the first time
		tacho_inst->currBeginFreq = beginFreq;
		tacho_inst->currEndFreq = endFreq;
		tacho_inst->currSize = size;

		tacho_inst->newX = (float*) malloc(size * sizeof(float));
		float* newX = tacho_inst->newX;
		newX[0] = (float) beginFreq;
		newX[size - 1] = (float) endFreq;
		float delta = (newX[size - 1] - newX[0]) / (size - 1);
		int i;
		for (i = 1; i < size - 1; i++) {
			// Construct the newX array
			newX[i] = newX[0] + i * delta;
		}

		// Interpolate
		Tachometer_Interpolation(tacho_inst->x, tacho_inst->fft_out_magnitude,
				TACHO_FFT_OUT_LENGTH, tacho_inst->newX, fft_out_magnitude,
				size);

		tacho_inst->newXInitialized = true;
	} // End if (tacho_inst->newXInitialized == true)

	return 0;
}
