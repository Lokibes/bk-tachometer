/*
 * tachometer_history.c
 *
 *  Created on: Apr 6, 2013
 *      Author: Minh Luan
 */

#include <stdlib.h>
#include "tachometer_history.h"

int32_t Tacho_History_Create(Tacho_History_t** tacho_history) {
	*tacho_history = (Tacho_History_t*) malloc(sizeof(Tacho_History_t));
	Tacho_History_t* tacho_history_inst = *tacho_history;

	tacho_history_inst->data = calloc(TACHO_HISTORY_CAPACITY, sizeof(float));
	return 0;
}

int32_t Tacho_History_Init(Tacho_History_t* tacho_history_inst) {
	if (tacho_history_inst == NULL) {
		return -1;
	}

	// The history size should be zero now
	tacho_history_inst->size = 0;
	tacho_history_inst->begin = 0;
	tacho_history_inst->end = 0;
	tacho_history_inst->sum = 0.0f;
	tacho_history_inst->accept_times = 0;

	return 0;
}

int32_t Tacho_History_Free(Tacho_History_t* tacho_history_inst) {
	if (tacho_history_inst == NULL) {
		return -1;
	}

	free(tacho_history_inst->data);
	free(tacho_history_inst);
	return 0;
}

int32_t Tacho_History_Add_Last(Tacho_History_t* tacho_history_inst,
		float number) {
	if (tacho_history_inst == NULL) {
		return -1;
	}

	tacho_history_inst->size++;
	tacho_history_inst->data[tacho_history_inst->end] = number;

	if (tacho_history_inst->size > TACHO_HISTORY_CAPACITY) {
		tacho_history_inst->size = TACHO_HISTORY_CAPACITY;
		tacho_history_inst->sum = tacho_history_inst->sum
				- tacho_history_inst->data[tacho_history_inst->begin] + number;
		tacho_history_inst->end = tacho_history_inst->begin;
		tacho_history_inst->begin++;
		if (tacho_history_inst->begin >= TACHO_HISTORY_CAPACITY) {
			tacho_history_inst->begin = 0;
		}
		return 1;
	} else if (tacho_history_inst->size == TACHO_HISTORY_CAPACITY) {
		tacho_history_inst->end = 0;
		tacho_history_inst->begin = 1;
		tacho_history_inst->sum += number;
		return 0;
	} else { // tacho_history_inst->size < TACHO_HISTORY_CAPACITY
		tacho_history_inst->sum += number;
		tacho_history_inst->end++;
		return 0;
	} // End if (tacho_history_inst->size >= TACHO_HISTORY_CAPACITY)

}
