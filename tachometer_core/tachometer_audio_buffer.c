/*
 * tachometer_audio_buffer.c
 *
 *  Created on: May 13, 2013
 *      Author: oneadmin
 */

#include "tachometer_defs.h"
#include "tachometer_audio_buffer.h"
#include <stdlib.h>

static void increase(int32_t* index, int32_t step);
static void copyAudio(Tacho_Buffer_t* buffer_inst, int16_t* inAudio,
		int32_t size, int32_t oldEnd);

int32_t Tacho_Buffer_Create(Tacho_Buffer_t** buffer) {
	*buffer = (Tacho_Buffer_t*) malloc(sizeof(Tacho_Buffer_t));
	Tacho_Buffer_t* buffer_inst = *buffer;

	// The size of the buffer is a special trick. The last: TACHO_FRAME_LENGTH - 1
	// samples is the space for storing the copy of the beginning audio samples
	buffer_inst->audioFrame = (int16_t*) malloc(
			TACHO_FRAME_LENGTH * sizeof(int16_t));
	buffer_inst->buffer = (int16_t*) malloc(
			(TACHO_AUDIO_BUFFER_SIZE + TACHO_FRAME_LENGTH - 1)
					* sizeof(int16_t));
	buffer_inst->capacity = TACHO_AUDIO_BUFFER_SIZE;

#ifdef BUFFER_DEBUG
	buffer_inst->audioFile = fopen("/mnt/sdcard/audioOutput.pcm", "wb");
#endif

	// Successfully
	return 0;
}

int32_t Tacho_Buffer_Init(Tacho_Buffer_t* buffer_inst) {
	if (buffer_inst == NULL) {
		return -1;
	}

	buffer_inst->begin = 0;
	buffer_inst->end = 0;
	buffer_inst->size = 0;

	// Successfully
	return 0;
}

int32_t Tacho_Buffer_Free(Tacho_Buffer_t* buffer_inst) {
	if (buffer_inst == NULL) {
		return -1;
	}

#ifdef BUFFER_DEBUG
	fclose(buffer_inst->audioFile);
#endif

	free(buffer_inst->audioFrame);
	free(buffer_inst->buffer);
	free(buffer_inst);

	// Successfully
	return 0;
}

int32_t Tacho_Buffer_Push(Tacho_Buffer_t* buffer_inst, int16_t* inAudio,
		int32_t size) {
	if (buffer_inst == NULL) {
		return -1;
	}

#ifdef BUFFER_DEBUG
	fwrite(inAudio, sizeof(int16_t), size, buffer_inst->audioFile);
#endif

	buffer_inst->size += size;
	int32_t oldEnd = buffer_inst->end;
	buffer_inst->end += size;

	if (buffer_inst->size <= TACHO_AUDIO_BUFFER_SIZE) { // No need to shift the begin index
		copyAudio(buffer_inst, inAudio, size, oldEnd);
	} else { // buffer_inst->size > TACHO_AUDIO_BUFFER_SIZE
		// Should shift the begin index
		int32_t shiftStep = buffer_inst->size - TACHO_AUDIO_BUFFER_SIZE;
		increase(&buffer_inst->begin, shiftStep);
		copyAudio(buffer_inst, inAudio, size, oldEnd);
		buffer_inst->size = TACHO_AUDIO_BUFFER_SIZE;
	} // End if (buffer_inst->size <= TACHO_AUDIO_BUFFER_SIZE)

	return 0;
}

int32_t Tacho_Buffer_Pull(Tacho_Buffer_t* buffer_inst, int16_t** destAudioArray) {
	if (buffer_inst == NULL) {
		return -1;
	}

	if (buffer_inst->size < TACHO_FRAME_LENGTH) {
		return -2;
	}

	int32_t oldBegin = buffer_inst->begin;
	increase(&buffer_inst->begin, TACHO_FRAME_LENGTH);
	*destAudioArray = &buffer_inst->buffer[oldBegin];
	buffer_inst->size -= TACHO_FRAME_LENGTH;

	return 0;
}

/**
 * Increase an index with some step. The step number should
 * be a positive integer.
 */
static void increase(int32_t* index, int32_t step) {
	*index = *index + step;
	if (*index >= TACHO_AUDIO_BUFFER_SIZE) {
		*index = *index - TACHO_AUDIO_BUFFER_SIZE;
	}
}

/**
 * Copy the inAudio to the buffer. This function also update the end index
 */
static void copyAudio(Tacho_Buffer_t* buffer_inst, int16_t* inAudio,
		int32_t size, int32_t oldEnd) {
	if (buffer_inst->end < TACHO_AUDIO_BUFFER_SIZE) {
		memcpy(&buffer_inst->buffer[oldEnd], inAudio, (size << 1));
	} else { // buffer_inst->end >= TACHO_AUDIO_BUFFER_SIZE
		int32_t deltaSize = buffer_inst->end - TACHO_AUDIO_BUFFER_SIZE + 1;
		buffer_inst->end = deltaSize - 1;
		memcpy(&buffer_inst->buffer[oldEnd], inAudio, (size << 1)); // This command also copy the audio to the tale of the physical array
		memcpy(buffer_inst->buffer, &inAudio[deltaSize], (deltaSize << 1));
	} // End if (buffer_inst->end < TACHO_AUDIO_BUFFER_SIZE)
}

