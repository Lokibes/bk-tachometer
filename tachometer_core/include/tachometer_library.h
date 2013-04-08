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
int32_t Tachometer_Create(void** tacho);

// Initialize
int32_t Tachometer_Init(void* tacho);

// Free
int32_t Tachometer_Free(void* tacho);

// Configuration
int32_t Tachometer_Config(void* tacho, int32_t estimatedFreq);

// Process
/**
 * Output:
 * 			float* resultFreq		The result of frequency if found
 * Return:
 * 			-1		Error
 * 			0		Not a rotary frequency
 * 			1		Is a rotary frequency
 */
int32_t Tachometer_Process(void* tacho, int16_t* inAudio, float* resultFreq);

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

#endif /* TACHOMETER_LIBRARY_ */
