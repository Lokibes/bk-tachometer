/*
 * tachometer_linked_list.h
 *
 *  Created on: Apr 6, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_HISTORY_H_
#define TACHOMETER_HISTORY_H_

#define TACHO_HISTORY_CAPACITY					25

/*
 * This implementation uses an Array as the container
 */

typedef struct {
	int32_t size;
	float* data;
	int begin;
	int end;
	float sum;
	int accept_times;
} Tacho_History_t;

int32_t Tacho_History_Create(Tacho_History_t** tacho_history);
int32_t Tacho_History_Init(Tacho_History_t* tacho_history_inst);
int32_t Tacho_History_Free(Tacho_History_t* tacho_history_inst);

/**
 * Return:
 * 			-1		Error
 * 			0:		size < TACHO_HISTORY_CAPACITY
 * 			1:		size >= TACHO_HISTORY_CAPACITY
 */
int32_t Tacho_History_Add_Last(Tacho_History_t* tacho_history_inst, float number);

#endif /* TACHOMETER_HISTORY_H_ */
