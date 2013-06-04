/*
 * tachometer_wavelet_denoiser.h
 *
 *  Created on: Jun 4, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_WAVELET_DENOISER_H_
#define TACHOMETER_WAVELET_DENOISER_H_

#ifdef __cplusplus
extern "C" {
#endif

int32_t Tachometer_Denoise_Create(void** denoiser);
int32_t Tachometer_Denoise_Init(void* denoiser);
int32_t Tachometer_Denoise_Free(void* denoiser);
int32_t Tachometer_Denoise_Process(void* denoiser, int16_t* inAudio, float* fftIn);	// Size is TACHO_DENOISE_LENGTH

#ifdef __cplusplus
}
#endif

#endif /* TACHOMETER_WAVELET_DENOISER_H_ */
