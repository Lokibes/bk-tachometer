# include <stdlib.h>
# include <stdio.h>
# include <time.h>

#include <fftw3.h>
#include <math.h>
#include "tachometer_defs.h"
#include "tachometer_library.h"
#include "tachometer_wavelet1d.h"

#define LOOP_NUM		180
#define TEST_ROUND		10

int main(void)

{

	FILE* audioFile = fopen("./rotation_16kHz.raw", "rb");

	int32_t estimatedFreq = 800;
	int16_t* inAudio = malloc(TACHO_FRAME_LENGTH * LOOP_NUM * sizeof(int16_t));
	fread(inAudio, sizeof(int16_t), TACHO_FRAME_LENGTH * LOOP_NUM, audioFile);

	void* tacho_inst = Tachometer_Create();
	Tachometer_Init(tacho_inst);
	Tachometer_Config(tacho_inst, estimatedFreq);

	// Test the FFT Out
	float* fft_out_magnitude = (float*) malloc(1000 * sizeof(float));
	float max;

	printf("Processing ...\n");

	int32_t i, test_count;
	for (test_count = 0; test_count < TEST_ROUND; test_count++) {
		for (i = 0; i < LOOP_NUM; i++) {
			Tachometer_Push(tacho_inst, &(inAudio[i * TACHO_FRAME_LENGTH]),
					TACHO_FRAME_LENGTH);
			float ret = Tachometer_Process(tacho_inst);
			if (ret > 0.0f) {
				printf("Loop %d: Result frequency: %f\n", i + 1, ret);
			} else {
				printf("Loop %d: Not found a good rotary frequency\n", i + 1);
			}
		}
	}

	Tachometer_Free(tacho_inst);
	tacho_inst = NULL;
	free(inAudio);
	fclose(audioFile);

	printf("Finished processing.\n");

	return 0;
}
