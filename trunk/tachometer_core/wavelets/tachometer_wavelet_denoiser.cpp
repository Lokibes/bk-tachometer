/*
 * tachometer_wavelet_denoiser.cpp
 *
 *  Created on: Jun 4, 2013
 *      Author: oneadmin
 */

#include <vector>
#include <string>
#include <cstddef>
#include "tachometer_wavelet_denoiser.h"
#include "tachometer_wavelet1d_defs.h"
#include "tachometer_defs.h"
#include "wavelet2s.h"
#include "fftw3.h"
using namespace std;

#define WAVELET_DENOISE_LEVEL		3

int32_t Tachometer_Denoise_Create(void** denoiser) {
	wavelets_t* denoise_inst = new wavelets_t();
	*denoiser = denoise_inst;

	return 0;
}

int32_t Tachometer_Denoise_Init(void* denoiser) {
	wavelets_t* denoise_inst = (wavelets_t*) denoiser;
	if (denoise_inst == NULL) {
		return -1;
	}

	denoise_inst->waveletFamily = string("sym4");
	denoise_inst->sizeFFT = -1;
	denoise_inst->isInitialized = false;

	denoise_inst->inWavelets.reserve(TACHO_DENOISE_HALF_LENGTH);
	denoise_inst->outWavelets.reserve(
			WAVELET_DENOISE_LEVEL * 2 * TACHO_DENOISE_HALF_LENGTH);

	return 0;
}

int32_t Tachometer_Denoise_Free(void* denoiser) {
	wavelets_t* denoise_inst = (wavelets_t*) denoiser;
	if (denoise_inst == NULL) {
		return -1;
	}

	fftwf_free(denoise_inst->inp_data);
	fftwf_free(denoise_inst->filt_data);
	fftwf_free(denoise_inst->inp_fft);
	fftwf_free(denoise_inst->filt_fft);
	fftwf_free(denoise_inst->temp_data);
	fftwf_free(denoise_inst->temp_ifft);
	fftwf_destroy_plan(denoise_inst->plan_forward_inp);
	fftwf_destroy_plan(denoise_inst->plan_forward_filt);
	fftwf_destroy_plan(denoise_inst->plan_backward);

	delete (denoise_inst);
	return 0;
}

int32_t Tachometer_Denoise_Process(void* denoiser, int16_t* inAudio,
		float* fftIn) {
	wavelets_t* denoise_inst = (wavelets_t*) denoiser;
	if (denoise_inst == NULL) {
		return -1;
	}

	denoise_inst->inWavelets.clear();
	denoise_inst->outWavelets.clear();
	for (int i = 0; i < TACHO_DENOISE_HALF_LENGTH; i++) {
		denoise_inst->inWavelets.push_back((float) inAudio[i]);
	}
	swt(denoise_inst, denoise_inst->inWavelets, WAVELET_DENOISE_LEVEL,
			denoise_inst->waveletFamily, denoise_inst->outWavelets,
			denoise_inst->waveletLength);
	copy(
			denoise_inst->outWavelets.begin()
					+ ((2 * WAVELET_DENOISE_LEVEL - 1)
							* TACHO_DENOISE_HALF_LENGTH),
			denoise_inst->outWavelets.end(), fftIn);

	for (int i = TACHO_DENOISE_HALF_LENGTH; i < TACHO_DENOISE_LENGTH; i++) {
		denoise_inst->inWavelets.push_back((float) inAudio[i]);
	}
	swt(denoise_inst, denoise_inst->inWavelets, WAVELET_DENOISE_LEVEL,
			denoise_inst->waveletFamily, denoise_inst->outWavelets,
			denoise_inst->waveletLength);
	copy(
			denoise_inst->outWavelets.begin()
					+ ((2 * WAVELET_DENOISE_LEVEL - 1)
							* TACHO_DENOISE_HALF_LENGTH),
			denoise_inst->outWavelets.end(), &fftIn[TACHO_DENOISE_HALF_LENGTH]);
	return 0;
}

