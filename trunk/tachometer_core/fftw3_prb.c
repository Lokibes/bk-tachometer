# include <stdlib.h>
# include <stdio.h>
# include <time.h>

# include <fftw3.h>
#include <math.h>
#include "tachometer_defs.h"
#include "tachometer_library.h"

#define LOOP_NUM		200

int main(void);
void test01(void);
void test02(void);
void test03(void);
void test04(void);
float frand(void);
void timestamp(void);

/******************************************************************************/

int main(void)

/******************************************************************************/
/*
 Purpose:

 FFTW3_PRB demonstrates the use of FFTW3.

 Modified:

 05 November 2007

 Author:

 John Burkardt
 */
{
//	timestamp();
//
//	printf("\n");
//	printf("FFTW3_PRB\n");
//	printf("  C version\n");
//	printf("  Test thed FFTW3 library.\n");

//	test01();
//	test02();
//	test03();
//	test04();
	/*
	 Terminate.
	 */
//	printf("\n");
//	printf("FFTW3_PRB\n");
//	printf("  Normal end of execution.\n");
//
//	printf("\n");
//	timestamp();
	/*
	 * Test the main algorithm here
	 */
	FILE* audioFile = fopen("./rotation_16kHz.raw", "rb");

	int32_t estimatedFreq = 452;
	int16_t* inAudio = malloc(TACHO_FRAME_LENGTH * LOOP_NUM * sizeof(int16_t));
	fread(inAudio, sizeof(int16_t), TACHO_FRAME_LENGTH * LOOP_NUM, audioFile);

	void* tacho_inst;
	Tachometer_Create(&tacho_inst);
	Tachometer_Init(tacho_inst);
	Tachometer_Config(tacho_inst, (estimatedFreq << 1));	// Fix the autocorrelation frequency doubling problem

	int32_t i;
	float resultFreq = 0.0f;
	for (i = 0; i < LOOP_NUM; i++) {
		int32_t ret = Tachometer_Process(tacho_inst, &(inAudio[i * TACHO_FRAME_LENGTH]), &resultFreq);
		if (ret == 1) {
			printf("Loop %d: Result frequency: %f\n", i + 1,  resultFreq);
		} else {
			printf("Loop %d: Not found a good rotary frequency\n", i + 1);
		}
	}

	Tachometer_Free(tacho_inst);
	tacho_inst = NULL;
	free(inAudio);
	fclose(audioFile);

	return 0;
}
/******************************************************************************/

void test01(void)

/******************************************************************************/
/*
 Purpose:

 TEST01: apply FFT to complex 1D data.

 Discussion:

 In this example, we generate N=100 random complex values stored as
 a vector of type FFTW_COMPLEX named "IN".

 We have FFTW3 compute the Fourier transform of this data named "OUT".

 We have FFTW3 compute the inverse Fourier transform of "OUT" to get
 "IN2", which should be the original input data, scaled by N.

 Modified:

 04 November 2007

 Author:

 John Burkardt
 */
{
	int i;
	fftwf_complex *in;
	fftwf_complex *in2;
	int n = 100;
	fftwf_complex *out;
	fftwf_plan plan_backward;
	fftwf_plan plan_forward;
	unsigned int seed = 123456789;

	printf("\n");
	printf("TEST01\n");
	printf("  Demonstrate FFTW3 on a single vector of complex data.\n");
	printf("\n");
	printf("  Transform data to FFT coefficients.\n");
	printf("  Backtransform FFT coefficients to recover data.\n");
	printf("  Compare recovered data to original data.\n");
	/*
	 Create the input array.
	 */
	in = fftwf_malloc(sizeof(fftwf_complex) * n);

	srand(seed);

	for (i = 0; i < n; i++) {
		in[i][0] = frand();
		in[i][1] = frand();
	}

	printf("\n");
	printf("  Input Data:\n");
	printf("\n");

	for (i = 0; i < n; i++) {
		printf("  %3d  %12f  %12f\n", i, in[i][0], in[i][1]);
	}
	/*
	 Create the output array.
	 */
	out = fftwf_malloc(sizeof(fftwf_complex) * n);

	plan_forward = fftwf_plan_dft_1d(n, in, out, FFTW_FORWARD, FFTW_ESTIMATE);

	fftwf_execute(plan_forward);

	printf("\n");
	printf("  Output FFT Coefficients:\n");
	printf("\n");

	for (i = 0; i < n; i++) {
		printf("  %3d  %12f  %12f\n", i, out[i][0], out[i][1]);
	}
	/*
	 Recreate the input array.
	 */
	in2 = fftwf_malloc(sizeof(fftwf_complex) * n);

	plan_backward = fftwf_plan_dft_1d(n, out, in2, FFTW_BACKWARD,
			FFTW_ESTIMATE);

	fftwf_execute(plan_backward);

	printf("\n");
	printf("  Recovered input data:\n");
	printf("\n");

	for (i = 0; i < n; i++) {
		printf("  %3d  %12f  %12f\n", i, in2[i][0], in2[i][1]);
	}

	printf("\n");
	printf("  Recovered input data divided by N:\n");
	printf("\n");

	for (i = 0; i < n; i++) {
		printf("  %3d  %12f  %12f\n", i, in2[i][0] / (float) (n),
				in2[i][1] / (float) (n));
	}
	/*
	 Free up the allocated memory.
	 */
	fftwf_destroy_plan(plan_forward);
	fftwf_destroy_plan(plan_backward);

	fftwf_free(in);
	fftwf_free(in2);
	fftwf_free(out);

	return;
}
/******************************************************************************/

void test02(void)

/******************************************************************************/
/*
 Purpose:

 TEST02: apply FFT to real 1D data.

 Modified:

 23 October 2005

 Author:

 John Burkardt
 */
{
	int i;
	float *in;
	float *in2;
	int n = 128;
	int nc;
	fftwf_complex *out;
	fftwf_plan plan_backward;
	fftwf_plan plan_forward;
	unsigned int seed = 123456789;

	printf("\n");
	printf("TEST02\n");
	printf("  Demonstrate FFTW3 on a single vector of real data.\n");
	printf("\n");
	printf("  Transform data to FFT coefficients.\n");
	printf("  Backtransform FFT coefficients to recover data.\n");
	printf("  Compare recovered data to original data.\n");
	/*
	 Set up an array to hold the data, and assign the data.
	 */
	in = fftwf_malloc(sizeof(float) * n);

	srand(seed);

	for (i = 0; i < n; i++) {
		in[i] = frand();
	}

	printf("\n");
	printf("  Input Data:\n");
	printf("\n");

	for (i = 0; i < n; i++) {
		printf("  %4d  %12f\n", i, in[i]);
	}
	/*
	 Set up an array to hold the transformed data,
	 get a "plan", and execute the plan to transform the IN data to
	 the OUT FFT coefficients.
	 */
	nc = (n / 2) + 1;

	out = fftwf_malloc(sizeof(fftwf_complex) * nc);

	plan_forward = fftwf_plan_dft_r2c_1d(n, in, out, FFTW_ESTIMATE);

	fftwf_execute(plan_forward);

	printf("\n");
	printf("  Output FFT Coefficients:\n");
	printf("\n");

	for (i = 0; i < nc; i++) {
		printf("  %4d  %12f  %12f\n", i, out[i][0], out[i][1]);
	}
	/*
	 Set up an arrray to hold the backtransformed data IN2,
	 get a "plan", and execute the plan to backtransform the OUT
	 FFT coefficients to IN2.
	 */
	in2 = fftwf_malloc(sizeof(float) * n);

	plan_backward = fftwf_plan_dft_c2r_1d(n, out, in2, FFTW_ESTIMATE);

	fftwf_execute(plan_backward);

	printf("\n");
	printf("  Recovered input data divided by N:\n");
	printf("\n");

	for (i = 0; i < n; i++) {
		printf("  %4d  %12f\n", i, in2[i] / (float) (n));
	}
	/*
	 Release the memory associated with the plans.
	 */
	fftwf_destroy_plan(plan_forward);
	fftwf_destroy_plan(plan_backward);

	fftwf_free(in);
	fftwf_free(in2);
	fftwf_free(out);

	return;
}
/******************************************************************************/

void test03(void)

/******************************************************************************/
/*
 Purpose:

 TEST03: apply FFT to complex 2D data.

 Discussion:

 In this example, we generate NX=8 by NY=10 random complex values
 stored as an NX by NY array of type FFTW_COMPLEX named "IN".

 We have FFTW3 compute the Fourier transform of this data named "OUT".

 We have FFTW3 compute the inverse Fourier transform of "OUT" to get
 "IN2", which should be the original input data, scaled by NX * NY.

 For a 2D complex NX by NY array used by FFTW, we need to access elements
 as follows:

 a[i*ny+j][0] is the real      part of A(I,J).
 a[i*ny+j][1] is the imaginary part of A(I,J)..

 Modified:

 05 November 2007

 Author:

 John Burkardt
 */
{
	int i;
	fftwf_complex *in;
	fftwf_complex *in2;
	int j;
	int nx = 8;
	int ny = 10;
	fftwf_complex *out;
	fftwf_plan plan_backward;
	fftwf_plan plan_forward;
	unsigned int seed = 123456789;

	printf("\n");
	printf("TEST03\n");
	printf("  Demonstrate FFTW3 on a %d by %d array of complex data.\n", nx,
			ny);
	printf("\n");
	printf("  Transform data to FFT coefficients.\n");
	printf("  Backtransform FFT coefficients to recover data.\n");
	printf("  Compare recovered data to original data.\n");
	/*
	 Create the input array.
	 */
	in = fftwf_malloc(sizeof(fftwf_complex) * nx * ny);

	srand(seed);

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			in[i * ny + j][0] = frand();
			in[i * ny + j][1] = frand();
		}
	}

	printf("\n");
	printf("  Input Data:\n");
	printf("\n");

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			printf("  %4d  %4d  %12f  %12f\n", i, j, in[i * ny + j][0],
					in[i * ny + j][1]);
		}
	}
	/*
	 Create the output array.
	 */
	out = fftwf_malloc(sizeof(fftwf_complex) * nx * ny);

	plan_forward = fftwf_plan_dft_2d(nx, ny, in, out, FFTW_FORWARD,
			FFTW_ESTIMATE);

	fftwf_execute(plan_forward);

	printf("\n");
	printf("  Output FFT Coefficients:\n");
	printf("\n");

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			printf("  %4d  %4d  %12f  %12f\n", i, j, out[i * ny + j][0],
					out[i * ny + j][1]);
		}
	}
	/*
	 Recreate the input array.
	 */
	in2 = fftwf_malloc(sizeof(fftwf_complex) * nx * ny);

	plan_backward = fftwf_plan_dft_2d(nx, ny, out, in2, FFTW_BACKWARD,
			FFTW_ESTIMATE);

	fftwf_execute(plan_backward);

	printf("\n");
	printf("  Recovered input data divided by NX * NY:\n");
	printf("\n");

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			printf("  %4d  %4d  %12f  %12f\n", i, j,
					in2[i * ny + j][0] / (float) (nx * ny),
					in2[i * ny + j][1] / (float) (nx * ny));
		}
	}
	/*
	 Free up the allocated memory.
	 */
	fftwf_destroy_plan(plan_forward);
	fftwf_destroy_plan(plan_backward);

	fftwf_free(in);
	fftwf_free(in2);
	fftwf_free(out);

	return;
}
/******************************************************************************/

void test04(void)

/******************************************************************************/
/*
 Purpose:

 TEST04: apply FFT to real 2D data.

 Discussion:

 In this example, we generate NX=8 by NY=10 random real values
 stored as an NX by NY array of type DOUBLE named "IN".

 We have FFTW3 compute the Fourier transform of this data named "OUT".

 We have FFTW3 compute the inverse Fourier transform of "OUT" to get
 "IN2", which should be the original input data, scaled by NX * NY.

 The Fourier coefficients are stored in an NX by NYH array where
 NYH = (NY/2) + 1.  We only compute about half the data because
 of real data implies symmetric FFT coefficients.

 a[i*nyh+j][0] is the real      part of A(I,J).
 a[i*nyh+j][1] is the imaginary part of A(I,J)..

 Modified:

 05 November 2007

 Author:

 John Burkardt
 */
{
	int i;
	float *in;
	float *in2;
	int j;
	int nx = 8;
	int ny = 10;
	int nyh;
	fftwf_complex *out;
	fftwf_plan plan_backward;
	fftwf_plan plan_forward;
	unsigned int seed = 123456789;

	printf("\n");
	printf("TEST04\n");
	printf("  Demonstrate FFTW3 on a %d by %d array of real data.\n", nx, ny);
	printf("\n");
	printf("  Transform data to FFT coefficients.\n");
	printf("  Backtransform FFT coefficients to recover data.\n");
	printf("  Compare recovered data to original data.\n");
	/*
	 Create the input array, an NX by NY array of floats.
	 */
	in = malloc(sizeof(float) * nx * ny);

	srand(seed);

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			in[i * ny + j] = frand();
		}
	}

	printf("\n");
	printf("  Input Data:\n");
	printf("\n");

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			printf("  %4d  %4d  %12f\n", i, j, in[i * ny + j]);
		}
	}
	/*
	 Create the output array OUT, which is of type FFTW_COMPLEX,
	 and of a size NX * NYH that is roughly half the dimension of the input data
	 (ignoring the fact that the input data is real, and the FFT
	 coefficients are complex).
	 */
	nyh = (ny / 2) + 1;

	out = fftwf_malloc(sizeof(fftwf_complex) * nx * nyh);

	plan_forward = fftwf_plan_dft_r2c_2d(nx, ny, in, out, FFTW_ESTIMATE);

	fftwf_execute(plan_forward);

	printf("\n");
	printf("  Output FFT Coefficients:\n");
	printf("\n");

	for (i = 0; i < nx; i++) {
		for (j = 0; j < nyh; j++) {
			printf("  %4d  %4d  %12f  %12f\n", i, j, out[i * nyh + j][0],
					out[i * nyh + j][1]);
		}
	}
	/*
	 Recreate the input array.
	 */
	in2 = malloc(sizeof(float) * nx * ny);

	plan_backward = fftwf_plan_dft_c2r_2d(nx, ny, out, in2, FFTW_ESTIMATE);

	fftwf_execute(plan_backward);

	printf("\n");
	printf("  Recovered input data divided by NX * NY:\n");
	printf("\n");

	for (i = 0; i < nx; i++) {
		for (j = 0; j < ny; j++) {
			printf("  %4d  %4d  %12f\n", i, j,
					in2[i * ny + j] / (float) (nx * ny));
		}
	}
	/*
	 Free up the allocated memory.
	 */
	fftwf_destroy_plan(plan_forward);
	fftwf_destroy_plan(plan_backward);

	free(in);
	free(in2);
	fftwf_free(out);

	return;
}
//*****************************************************************************/

float frand(void)

//*****************************************************************************/
/*
 Purpose:

 FRAND returns random values between 0 and 1.

 Discussion:

 The random seed can be set by a call to SRAND ( unsigned int ).

 Note that Kernighan and Ritchie suggest using

 ( ( float ) rand ( ) / ( RAND_MAX + 1 ) )

 but this seems to result in integer overflow for RAND_MAX + 1,
 resulting in negative values for the random numbers.

 Modified:

 23 October 2005

 Author:

 John Burkardt

 Reference:

 Brian Kernighan, Dennis Ritchie,
 The C Programming Language,
 Prentice Hall, 1988.

 Parameters:

 Output, float FRAND, a random value between 0 and 1.
 */
{
	float value;

	value = ((float) rand() / (RAND_MAX) );

	return value;
}
//*****************************************************************************/

void timestamp(void)

/******************************************************************************/
/*
 Purpose:

 TIMESTAMP prints the current YMDHMS date as a time stamp.

 Example:

 31 May 2001 09:45:54 AM

 Modified:

 24 September 2003

 Author:

 John Burkardt

 Parameters:

 None
 */
{
# define TIME_SIZE 40

	static char time_buffer[TIME_SIZE];
	const struct tm *tm;
	size_t len;
	time_t now;

	now = time(NULL);
	tm = localtime(&now);

	len = strftime(time_buffer, TIME_SIZE, "%d %B %Y %I:%M:%S %p", tm);

	printf("%s\n", time_buffer);

	return;
# undef TIME_SIZE
}
