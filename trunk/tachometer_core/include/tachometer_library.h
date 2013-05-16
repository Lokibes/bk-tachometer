/*
 * auto_correlation.h
 *
 *  Created on: Apr 3, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_LIBRARY_
#define TACHOMETER_LIBRARY_

#include "tachometer_defs.h"

#define INDEX_TO_FREQ	(((float)TACHO_HALF_SAMPLING_FREQ) / ((float)(TACHO_FFT_OUT_LENGTH - 1)))

// To make the auto correlation
void Tachometer_AutoCorrelation(int16_t* restrict in_vector,
		float* restrict result);

// Return the maximum absolute 16 bit value in an array
int32_t Tachometer_MaxAbsolute16C(int16_t* vector);

// Create
//int32_t Tachometer_Create(void** tacho);
void* Tachometer_Create(); // Hack code to run on Java

// Initialize
int32_t Tachometer_Init(void* tacho);

// Free
int32_t Tachometer_Free(void* tacho);

// Configuration
int32_t Tachometer_Config(void* tacho, int32_t estimatedFreq);

/**
 * Get the audio frame location for Java to save the recorded audio
 */
int32_t Tachometer_Get_Audio_Frame_Location(void* tacho, int16_t** audioFrame);

/**
 * Push audio
 */
int32_t Tachometer_Push(void* tacho, int16_t* inAudio,
		int32_t size);

/**
 * Return:
 * 			-1.0f					Error
 * 			-2.0f					Not enough data to process in the audio buffer
 * 			0.0f					Not a rotary frequency
 * 			? > 0.0f				Is a rotary frequency
 */
float Tachometer_Process(void* tacho);

/**
 * Return the address of the FFT out magnitude array
 * Pre:
 * 	The fft_out_magnitude has been allocated
 * Input:
 * 		void*		tacho				The tachometer instance
 * 		int32_t		beginFreq			The beginning frequency
 * 		int32_t		endFreq				The ending frequency
 * 		int32_t 	size				The size of the output array
 * 	Output:
 * 		float*		fft_out_magnitude		The result array
 * 	Return:
 * 		-1			Error
 * 		0			Successful
 */
int32_t Tachometer_FFT_Out(void* tacho, int32_t beginFreq, int32_t endFreq, int32_t size, float* fft_out_magnitude);

static __inline int16_t Tachometer_GetSizeInBits(uint32_t n) {
	int32_t bits;

	if (0xFFFF0000 & n) {
		bits = 16;
	} else {
		bits = 0;
	}
	if (0x0000FF00 & (n >> bits))
		bits += 8;
	if (0x000000F0 & (n >> bits))
		bits += 4;
	if (0x0000000C & (n >> bits))
		bits += 2;
	if (0x00000002 & (n >> bits))
		bits += 1;
	if (0x00000001 & (n >> bits))
		bits += 1;

	return (int16_t) bits;
}

static __inline int Tachometer_NormW32(int32_t a) {
	int32_t zeros;

	if (a == 0) {
		return 0;
	} else if (a < 0) {
		a = ~a;
	}

	if (!(0xFFFF8000 & a)) {
		zeros = 16;
	} else {
		zeros = 0;
	}
	if (!(0xFF800000 & (a << zeros)))
		zeros += 8;
	if (!(0xF8000000 & (a << zeros)))
		zeros += 4;
	if (!(0xE0000000 & (a << zeros)))
		zeros += 2;
	if (!(0xC0000000 & (a << zeros)))
		zeros += 1;

	return zeros;
}

// Index of maximum value in a floating point vector.
// Imply that the length of vector is TACHO_EXPECTED_LENGTH
static __inline float Tachometer_MaxFrequency(float* vector, int32_t beginIndex) {
	int i = 0, index = 0;
	float maximum = 0.0f;
	for (i = 0; i < TACHO_ESTIMATION_RANGE; i++) {
		if (vector[beginIndex + i] > maximum) {
			maximum = vector[i];
			index = i;
		}
	}

	return ((index + beginIndex) * INDEX_TO_FREQ);
}

// Interpolation: Mimic Matlab interp1 function
// Return: float* newY
// Pre:
//		1. size is the size of x and y arrays. newSize is the size of newX.
//		2. newX should be contained in x
//		3. x is a uniform array --> Ex: 0, a, 2a, 3a, 4a, 5a, etc
static __inline void Tachometer_Interpolation(float* x, float* y, int size,
		float* newX, float* newY, int newSize) {
	int i, j;
	i = 0;
	j = 0;
	float tmpNewX, a, b;
	float deltaX = x[1] - x[0];
	float deltaXReverse = 1 / deltaX;
	if (newX[0] == x[0]) {
		newY[0] = y[0];
		i = 1;
		j = 1;
	}

	for (; j < newSize; j++) {
		// Get the current newX
		tmpNewX = newX[j];

		while (x[i] < tmpNewX) {
			i++;
		}

		a = x[i - 1];
		newY[j] = y[i - 1] + (y[i] - y[i - 1]) * (x[i] - a) * deltaXReverse;
	}
}

#endif /* TACHOMETER_LIBRARY_ */
