/*
 * tachometer_audio_buffer.h
 *
 *  Created on: May 13, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_AUDIO_BUFFER_H_
#define TACHOMETER_AUDIO_BUFFER_H_

#include "tachometer_defs.h"

int32_t Tacho_Buffer_Create(Tacho_Buffer_t** buffer);
int32_t Tacho_Buffer_Init(Tacho_Buffer_t* buffer_inst);
int32_t Tacho_Buffer_Free(Tacho_Buffer_t* buffer_inst);

/**
 * Push an audio array with size size to the audio buffer. NOTE: the
 * size here is the number of audio samples in the array.
 *
 * Return:
 * 	    -1 			Error
 * 	     0			Successful
 */
int32_t Tacho_Buffer_Push(Tacho_Buffer_t* buffer_inst, int16_t* inAudio, int32_t size);

/**
 * Set *destAudioArray to the audio array to be proceeded.
 * The audio time is 20 milliseconds. The audio size in int16_t is TACHO_FRAME_LENGTH.
 *
 * Return:
 * 	   -1			Error
 * 	   -2			The size of the buffer is not available for pulling
 * 		0			Successfully
 */
int32_t Tacho_Buffer_Pull(Tacho_Buffer_t* buffer_inst, int16_t** destAudioArray);

#endif /* TACHOMETER_AUDIO_BUFFER_H_ */
