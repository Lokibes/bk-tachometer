/*
 * max_absolute.c
 *
 *  Created on: Apr 3, 2013
 *      Author: oneadmin
 */

#include "tachometer_defs.h"
#include "tachometer_library.h"
#include <stdlib.h>

int Tachometer_MaxAbsolute16C(int16_t* vector) {
	int32_t i = 0, absolute = 0, maximum = 0;

	if (vector == NULL) {
		return -1;
	}

	for (i = 0; i < TACHO_FRAME_LENGTH; i++) {
		absolute = abs((int) vector[i]);

		if (absolute > maximum) {
			maximum = absolute;
		}
	}

	// Guard the case for abs(-32768).
	if (maximum > TACHO_WORD16_MAX) {
		maximum = TACHO_WORD16_MAX;
	}

	return (int16_t) maximum;
}
