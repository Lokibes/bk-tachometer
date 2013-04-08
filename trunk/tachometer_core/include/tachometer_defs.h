/*
 * tachometer_defs.h
 *
 *  Created on: Apr 3, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_DEFS_H_
#define TACHOMETER_DEFS_H_

#include "fftw3.h"
#include "tachometer_history.h"

#define TACHO_SAMPLING_FREQ					16000	// 16 kHz sampling frequency
#define TACHO_HALF_SAMPLING_FREQ			(TACHO_SAMPLING_FREQ >> 1)
#define TACHO_WORD16_MIN       				-32768
#define TACHO_WORD16_MAX       				32767
#define TACHO_FRAME_LENGTH					320		// 320 samples per frame (20 ms)
#define TACHO_AUTO_CORRELATION_LENGTH		((TACHO_FRAME_LENGTH << 1) - 1)
#define TACHO_EXPECTED_LENGTH				(4096 << 1)
#define TACHO_FFT_IN_LENGTH					TACHO_EXPECTED_LENGTH
#define TACHO_FFT_OUT_LENGTH				((TACHO_EXPECTED_LENGTH >> 1) + 1)
#define TACHO_ZERO_PADDING_LENGTH			(TACHO_EXPECTED_LENGTH - TACHO_AUTO_CORRELATION_LENGTH)
#define TACHO_ESTIMATION_HALF_RANGE			(TACHO_EXPECTED_LENGTH >> 4)
#define TACHO_ESTIMATION_RANGE				(TACHO_EXPECTED_LENGTH >> 3)	// The range to find the best Frequency
typedef struct {
	Tacho_History_t* tacho_history_inst;
	float* fft_in;
	fftwf_complex* fft_out;
	float* fft_out_magnitude;
	fftwf_plan plan_forward;
	int32_t beginIndex; // The begin index to find the best frequency
} Tacho_t;

#endif /* TACHOMETER_DEFS_H_ */
