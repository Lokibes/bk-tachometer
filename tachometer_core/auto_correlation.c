/*
 * auto_correlation.c
 *
 *  Created on: Apr 3, 2013
 *      Author: Minh Luan
 */

#include "tachometer_defs.h"
#include "tachometer_library.h"

#define TACHO_W32_MUL(a, b) ((int32_t)(((int32_t)a)*((int32_t)b)))

void Tachometer_AutoCorrelation(int16_t* restrict in_vector,
		float* restrict result) {
	int32_t sum = 0;
	int32_t i = 0, j = 0;
	int16_t smax = 0;
	int32_t scaling = 0;

	// Find the maximum absolute value of the samples.
	smax = Tachometer_MaxAbsolute16C(in_vector);

	// In order to avoid overflow when computing the sum we should scale the
	// samples so that (in_vector_length * smax * smax) will not overflow.
	if (smax == 0) {
		scaling = 0;
	} else {
		// Number of bits in the sum loop.
		int32_t nbits = Tachometer_GetSizeInBits(TACHO_FRAME_LENGTH);
		// Number of bits to normalize smax.
		int32_t t = Tachometer_NormW32(TACHO_W32_MUL(smax, smax));

		if (t > nbits) {
			scaling = 0;
		} else {
			scaling = nbits - t;
		}
	}

	// Perform the actual correlation calculation.
	float tmp_sum_float = 0.0f;
	for (i = 0; i < TACHO_FRAME_LENGTH; i++) {
		sum = 0;
		for (j = 0; j < TACHO_FRAME_LENGTH - i; j++) {
			sum += ((in_vector[j] * in_vector[i + j]) >> scaling);
		}
		tmp_sum_float = (float) sum;
		result[TACHO_FRAME_LENGTH + i - 1] = tmp_sum_float;
		result[TACHO_FRAME_LENGTH - i - 1] = tmp_sum_float;
	}
}
