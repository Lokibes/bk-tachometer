/*
 * tachometer_wavelet1d.h
 *
 *  Created on: May 25, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_WAVELET1D_H_
#define TACHOMETER_WAVELET1D_H_

#ifdef __cplusplus
extern "C" {
#endif

int32_t Tachometer_Wavelet_Create(void** wavelets);
int32_t Tachometer_Wavelet_Init(void* wavelets);
int32_t Tachometer_Wavelet_Prepare(void* wavelets, Tacho_t* tacho_inst);
int32_t Tachometer_Wavelet_Free(void* wavelets);

/**
 * Return:
 * 		-1.0f			Error
 * 		 0.0f			Cannot find a good frequency
 * 		 ? > 0.0f		The result frequency
 *
 */
float Tachometer_Wavelet_Transform(void* wavelets, float* fftArray,
		int32_t size);

#ifdef __cplusplus
}
#endif

#endif /* TACHOMETER_WAVELET1D_H_ */
