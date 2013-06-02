/*
 * tachometer_defs.h
 *
 *  Created on: Apr 3, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_DEFS_H_
#define TACHOMETER_DEFS_H_

#include <stdbool.h>
#include "fftw3.h"
#include "tachometer_history.h"

#define TACHO_SAMPLING_FREQ					16000	// 16 kHz sampling frequency
#define TACHO_HALF_SAMPLING_FREQ			(TACHO_SAMPLING_FREQ >> 1)
#define TACHO_WORD16_MIN       				-32768
#define TACHO_WORD16_MAX       				32767
#define TACHO_FRAME_LENGTH					1280		// 1280 samples per frame (80 ms)
#define TACHO_AUTO_CORRELATION_LENGTH		((TACHO_FRAME_LENGTH << 1) - 1)
#define TACHO_EXPECTED_LENGTH				(1 << 12)		// 2 ^ 13 values
#define TACHO_FFT_IN_LENGTH					TACHO_EXPECTED_LENGTH
#define TACHO_FFT_OUT_LENGTH				((TACHO_EXPECTED_LENGTH >> 1) + 1)
#define TACHO_ZERO_PADDING_LENGTH			(TACHO_EXPECTED_LENGTH - TACHO_AUTO_CORRELATION_LENGTH)
#define TACHO_ESTIMATION_HALF_RANGE			(TACHO_EXPECTED_LENGTH >> 6)	// About ~ 125 Hz
#define TACHO_ESTIMATION_RANGE				(TACHO_EXPECTED_LENGTH >> 5)	// The range to find the best Frequency, about ~250Hz precision
// The audio buffer size is in int16_t size
#define TACHO_AUDIO_BUFFER_SIZE				(TACHO_FRAME_LENGTH * 50)		// This means that the audio buffer can store 50 audio frames

#define TACHO_INDEX_TO_FREQ_COEF			(((float)(TACHO_SAMPLING_FREQ >> 1)) / ((float) TACHO_FFT_OUT_LENGTH))
#define TACHO_FREQ_TO_INDEX_COEF			(1 / TACHO_INDEX_TO_FREQ_COEF)
typedef struct {
	int16_t* audioFrame;
	int16_t* buffer;
	int32_t begin;
	int32_t end;
	int32_t capacity;
	int32_t size;

#ifdef BUFFER_DEBUG
	FILE* audioFile;
#endif
} Tacho_Buffer_t;

typedef struct {
	Tacho_History_t* tacho_history_inst;
	float* fft_in;
	fftwf_complex* fft_out;
	float* fft_out_magnitude;
	fftwf_plan plan_forward;
	int32_t beginIndex; // The begin index to find the best frequency

	// The audio buffer for processing
	Tacho_Buffer_t* audioBuffer;

	// These are for FFT_Output interpolation
	float* x; // The x value of the fft_out_magnitude
	float* newX;
	int32_t currSize;
	int32_t currBeginFreq;
	int32_t currEndFreq;
	bool newXInitialized;
	void* wavelets_inst;
} Tacho_t;

#endif /* TACHOMETER_DEFS_H_ */
