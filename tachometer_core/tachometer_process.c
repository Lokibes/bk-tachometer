/*
 * tachometer_process.c
 *
 *  Created on: Apr 4, 2013
 *      Author: Minh Luan
 */

#include <stdlib.h>
#include <math.h>
#include <stdbool.h>
#include "tachometer_defs.h"
#include "tachometer_library.h"
#include "tachometer_history.h"
#include "tachometer_audio_buffer.h"
#include "fftw3.h"
#include "tachometer_wavelet1d.h"
#include "tachometer_wavelet_denoiser.h"
#include "tachometer_hanning.h"

// Create
void* Tachometer_Create() {
	Tacho_t* tacho_inst = (Tacho_t*) malloc(sizeof(Tacho_t));

	Tacho_History_Create(&(tacho_inst->tacho_history_inst));
	tacho_inst->fft_in = (float*) fftwf_malloc(
			sizeof(float) * TACHO_FFT_IN_LENGTH);
	tacho_inst->fft_out = fftwf_malloc(
			sizeof(fftwf_complex) * TACHO_FFT_IN_LENGTH);
	tacho_inst->fft_out_magnitude = (float*) malloc(
			sizeof(float) * TACHO_FFT_IN_LENGTH);
	tacho_inst->x = (float*) malloc(TACHO_FFT_OUT_LENGTH * sizeof(float));

	Tacho_Buffer_Create(&tacho_inst->audioBuffer);

	// Create the wavelet instance
	Tachometer_Wavelet_Create(&tacho_inst->wavelets_inst);

	// Create the denoiser
	Tachometer_Denoise_Create(&tacho_inst->denoise_inst);

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
	for (i = TACHO_DENOISE_LENGTH; i < TACHO_EXPECTED_LENGTH; i++) {
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
	float delta = ((float) TACHO_HALF_SAMPLING_FREQ)
			/ (TACHO_FFT_OUT_LENGTH - 1);
	for (i = 1; i < TACHO_FFT_OUT_LENGTH - 1; i++) {
		x[i] = i * delta;
	}

	// Initialize the audio buffer
	Tacho_Buffer_Init(tacho_inst->audioBuffer);

	// Initialize the wavelet 1d struct
	Tachometer_Wavelet_Init(tacho_inst->wavelets_inst);

	// Initialize the denoiser
	Tachometer_Denoise_Init(tacho_inst->denoise_inst);

	return 0;
}

// Free
int32_t Tachometer_Free(void* tacho) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1;
	}

	Tacho_History_Free(tacho_inst->tacho_history_inst);
	Tacho_Buffer_Free(tacho_inst->audioBuffer);
	Tachometer_Wavelet_Free(tacho_inst->wavelets_inst);
	Tachometer_Denoise_Free(tacho_inst->denoise_inst);
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
			* TACHO_FREQ_TO_INDEX_COEF);

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

	// Also prepare the wavelet instant before processing
	Tachometer_Wavelet_Prepare(tacho_inst->wavelets_inst, tacho_inst);

	return 0;
}

int32_t Tachometer_Get_Audio_Frame_Location(void* tacho, int16_t** audioFrame) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1;
	}

	*audioFrame = tacho_inst->audioBuffer->audioFrame;

	return 0;
}

int32_t Tachometer_Push(void* tacho, int16_t* inAudio, int32_t size) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1;
	}

	Tacho_Buffer_Push(tacho_inst->audioBuffer, inAudio, size);

	return 0;
}

// Process:
float Tachometer_Process(void* tacho) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1.0f;
	}
	int i;

	if (tacho_inst->audioBuffer->size < TACHO_FRAME_LENGTH) {
		return -2.0f; // There is not enough data to process
	}

	// Set the inAudio to point to the audio location
	int16_t* inAudio;
	Tacho_Buffer_Pull(tacho_inst->audioBuffer, &inAudio);

	// Denoise the audio
	float* fftIn = tacho_inst->fft_in;
	Tachometer_Denoise_Process(tacho_inst->denoise_inst, inAudio, fftIn);
//	for (i = 0; i < TACHO_DENOISE_LENGTH; i++) {
//		fftIn[i] = (float) inAudio[i];
//	}

	// Hanning window
	for (i = 0; i < TACHO_DENOISE_LENGTH; i++) {
		fftIn[i] = fftIn[i] * hanning_window_1024[i];
	}

	// Do the FFT

	fftwf_execute(tacho_inst->plan_forward);
	float* restrict fft_out_magnitude = tacho_inst->fft_out_magnitude;
	fftwf_complex* restrict fft_out = tacho_inst->fft_out;
	for (i = 0; i < TACHO_FFT_OUT_LENGTH; i++) {
		fft_out_magnitude[i] = sqrtf(
				fft_out[i][0] * fft_out[i][0] + fft_out[i][1] * fft_out[i][1]);
	}

	// Wavelet transformation
	float ret = Tachometer_Wavelet_Transform(tacho_inst->wavelets_inst,
			&fft_out_magnitude[tacho_inst->beginIndex], TACHO_ESTIMATION_RANGE);

	return ret;
}

float Tachometer_FFT_Out(void* tacho, int32_t beginFreq, int32_t endFreq,
		int32_t size, float* fft_out_magnitude) {
	Tacho_t* tacho_inst = (Tacho_t*) tacho;
	if (tacho_inst == NULL) {
		return -1.0f;
	}

	float maxNum = 0.0f;
	if (tacho_inst->newXInitialized == true) {
		if (tacho_inst->currBeginFreq == beginFreq
				&& tacho_inst->currEndFreq == endFreq
				&& tacho_inst->currSize == size) {
			// Should not reconstruct
			// Interpolate
			Tachometer_Interpolation(tacho_inst->x,
					tacho_inst->fft_out_magnitude, TACHO_FFT_OUT_LENGTH,
					tacho_inst->newX, fft_out_magnitude, size, &maxNum);

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
					tacho_inst->newX, fft_out_magnitude, size, &maxNum);

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
				TACHO_FFT_OUT_LENGTH, tacho_inst->newX, fft_out_magnitude, size,
				&maxNum);

		tacho_inst->newXInitialized = true;
	} // End if (tacho_inst->newXInitialized == true)

	return maxNum;
}
