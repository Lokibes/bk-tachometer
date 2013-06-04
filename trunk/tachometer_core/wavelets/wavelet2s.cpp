#include <iostream>
#include <fstream>
#include <complex>
#include <vector>
#include <string>
#include <cmath>
#include <algorithm>
#include "fftw3.h"
#include "wavelet2s.h"
using namespace std;

/**
 * Helper functions list
 */
void upsamp(vector<float> &sig, int M, vector<float> &sig_u);
int filtcoef(string name, vector<float> &lp1, vector<float> &hp1,
		vector<float> &lp2, vector<float> &hp2);
void* per_ext(vector<float> &sig, int a);
float convfft(wavelets_t* wavelets_inst, vector<float> &a, vector<float> &b,
		vector<float> &c);

/**
 * Main function: Discrete Stationary Wavelet transformation
 */
void* swt(wavelets_t* wavelets_inst, vector<float> &signal1, int J,
		string waveletName, vector<float> &swt_output, int &length) {
	vector<float> lpd, hpd, lpr, hpr;
	vector<float> sig = signal1;

	int N = sig.size();
	length = N;

	filtcoef(waveletName, lpd, hpd, lpr, hpr);

	for (int iter = 0; iter < J; iter++) {
		vector<float> low_pass;
		vector<float> high_pass;
		if (iter > 0) {
			int M = (int) pow(2.0f, iter);
			upsamp(lpd, M, low_pass);
			upsamp(hpd, M, high_pass);
		} else {
			low_pass = lpd;
			high_pass = hpd;
		}

		unsigned int len_filt = low_pass.size();
		per_ext(sig, len_filt / 2);

		vector<float> cA;
		convfft(wavelets_inst, sig, low_pass, cA);
		vector<float> cD;
		convfft(wavelets_inst, sig, high_pass, cD);

		// Resize cA and cD
		cA.erase(cA.begin(), cA.begin() + len_filt);
		cA.erase(cA.begin() + N, cA.end());
		cD.erase(cD.begin(), cD.begin() + len_filt);
		cD.erase(cD.begin() + N, cD.end());
		// Reset signal value;

		sig = cA;

		swt_output.insert(swt_output.end(), cD.begin(), cD.end());
		swt_output.insert(swt_output.end(), cA.begin(), cA.end());
	}

	return 0;
}

/**
 * Helper functions implementation follows here
 */

void upsamp(vector<float> &sig, int M, vector<float> &sig_u) {
	int len_n = sig.size() * M;

	for (int i = 0; i < len_n; i++) {
		if (i % M == 0) {
			float temp = sig[i / M];
			sig_u.push_back(temp);
		} else {
			sig_u.push_back(0);
		}
	}
}

int filtcoef(string name, vector<float> &lp1, vector<float> &hp1,
		vector<float> &lp2, vector<float> &hp2) {
	if (name == "bior1.1") {
		float lp1_a[] = { 0.70710678118654757f, 0.70710678118654757f };
		lp1.assign(lp1_a, lp1_a + sizeof(lp1_a) / sizeof(float));

		float hp1_a[] = { -0.70710678118654757f, 0.70710678118654757f };
		hp1.assign(hp1_a, hp1_a + sizeof(hp1_a) / sizeof(float));

		float lp2_a[] = { 0.70710678118654757f, 0.70710678118654757f };
		lp2.assign(lp2_a, lp2_a + sizeof(lp2_a) / sizeof(float));

		float hp2_a[] = { 0.70710678118654757f, -0.70710678118654757f };
		hp2.assign(hp2_a, hp2_a + sizeof(hp2_a) / sizeof(float));
		return 0;
	} else if (name == "sym4") {
		float lp1_a[] = { -0.075765714789273325f, -0.02963552764599851f,
				0.49761866763201545f, 0.80373875180591614f,
				0.29785779560527736f, -0.099219543576847216f,
				-0.012603967262037833f, 0.032223100604042702f };
		lp1.assign(lp1_a, lp1_a + sizeof(lp1_a) / sizeof(float));

		float hp1_a[] = { -0.032223100604042702f, -0.012603967262037833f,
				0.099219543576847216f, 0.29785779560527736f,
				-0.80373875180591614f, 0.49761866763201545f,
				0.02963552764599851f, -0.075765714789273325f };
		hp1.assign(hp1_a, hp1_a + sizeof(hp1_a) / sizeof(float));

		float lp2_a[] = { 0.032223100604042702f, -0.012603967262037833f,
				-0.099219543576847216f, 0.29785779560527736f,
				0.80373875180591614f, 0.49761866763201545f,
				-0.02963552764599851f, -0.075765714789273325f };
		lp2.assign(lp2_a, lp2_a + sizeof(lp2_a) / sizeof(float));

		float hp2_a[] = { -0.075765714789273325f, 0.02963552764599851f,
				0.49761866763201545f, -0.80373875180591614f,
				0.29785779560527736f, 0.099219543576847216f,
				-0.012603967262037833f, -0.032223100604042702f };
		hp2.assign(hp2_a, hp2_a + sizeof(hp2_a) / sizeof(float));
		return 0;
	} else {
		cout << "Filter Not in Database" << endl;
		return -1;
	}

}

void* per_ext(vector<float> &sig, int a) {
	unsigned int len;
	len = sig.size();
	if ((len % 2) != 0) {
		float temp = sig[len - 1];
		sig.push_back(temp);
		len = sig.size();
	}

	for (int i = 0; i < a; i++) {
		float temp1 = sig[2 * i];
		float temp2 = sig[len - 1];
		sig.insert(sig.begin(), temp2);
		sig.insert(sig.end(), temp1);

	}

	return 0;
}

float convfft(wavelets_t* wavelets_inst, vector<float> &a, vector<float> &b,
		vector<float> &c) {
	int32_t sz = a.size() + b.size() - 1;
	if (sz != wavelets_inst->sizeFFT) {
		if (wavelets_inst->isInitialized == true) {
			// Remove the old memory
			fftwf_free(wavelets_inst->inp_data);
			fftwf_free(wavelets_inst->filt_data);
			fftwf_free(wavelets_inst->inp_fft);
			fftwf_free(wavelets_inst->filt_fft);
			fftwf_free(wavelets_inst->temp_data);
			fftwf_free(wavelets_inst->temp_ifft);
			fftwf_destroy_plan(wavelets_inst->plan_forward_inp);
			fftwf_destroy_plan(wavelets_inst->plan_forward_filt);
			fftwf_destroy_plan(wavelets_inst->plan_backward);
		}
		// Should initialize
		wavelets_inst->isInitialized = true;
		wavelets_inst->sizeFFT = sz;
		wavelets_inst->inp_data = (fftwf_complex*) fftwf_malloc(
				wavelets_inst->sizeFFT * sizeof(fftwf_complex));
		wavelets_inst->filt_data = (fftwf_complex*) fftwf_malloc(
				wavelets_inst->sizeFFT * sizeof(fftwf_complex));
		wavelets_inst->inp_fft = (fftwf_complex*) fftwf_malloc(
				wavelets_inst->sizeFFT * sizeof(fftwf_complex));
		wavelets_inst->filt_fft = (fftwf_complex*) fftwf_malloc(
				wavelets_inst->sizeFFT * sizeof(fftwf_complex));
		wavelets_inst->temp_data = (fftwf_complex*) fftwf_malloc(
				wavelets_inst->sizeFFT * sizeof(fftwf_complex));
		wavelets_inst->temp_ifft = (fftwf_complex*) fftwf_malloc(
				wavelets_inst->sizeFFT * sizeof(fftwf_complex));
		wavelets_inst->plan_forward_inp = fftwf_plan_dft_1d(
				wavelets_inst->sizeFFT, wavelets_inst->inp_data,
				wavelets_inst->inp_fft, FFTW_FORWARD, FFTW_ESTIMATE);
		wavelets_inst->plan_forward_filt = fftwf_plan_dft_1d(
				wavelets_inst->sizeFFT, wavelets_inst->filt_data,
				wavelets_inst->filt_fft, FFTW_FORWARD, FFTW_ESTIMATE);
		wavelets_inst->plan_backward = fftwf_plan_dft_1d(wavelets_inst->sizeFFT,
				wavelets_inst->temp_data, wavelets_inst->temp_ifft,
				FFTW_BACKWARD, FFTW_ESTIMATE);

	} // End if (sz != wavelets_inst->sizeFFT)

	fftwf_complex* inp_data = wavelets_inst->inp_data;
	fftwf_complex* inp_fft = wavelets_inst->inp_fft;
	fftwf_complex* filt_data = wavelets_inst->filt_data;
	fftwf_complex* filt_fft = wavelets_inst->filt_fft;
	fftwf_complex* temp_data = wavelets_inst->temp_data;
	fftwf_complex* temp_ifft = wavelets_inst->temp_ifft;

	for (unsigned int i = 0; i < sz; i++) {
		if (i < a.size()) {
			inp_data[i][0] = a[i];
		} else {
			inp_data[i][0] = 0.0;

		}
		inp_data[i][1] = 0.0;
		if (i < b.size()) {
			filt_data[i][0] = b[i];
		} else {
			filt_data[i][0] = 0.0;

		}
		filt_data[i][1] = 0.0;

	}

	fftwf_execute(wavelets_inst->plan_forward_inp);
	fftwf_execute(wavelets_inst->plan_forward_filt);

	for (unsigned int i = 0; i < sz; i++) {
		temp_data[i][0] = inp_fft[i][0] * filt_fft[i][0]
				- inp_fft[i][1] * filt_fft[i][1];

		temp_data[i][1] = inp_fft[i][0] * filt_fft[i][1]
				+ inp_fft[i][1] * filt_fft[i][0];

	}

	fftwf_execute(wavelets_inst->plan_backward);

	float temp1;
	float sz_float_invs = 1.0f / ((float) sz);
	for (unsigned int i = 0; i < sz; i++) {
		temp1 = temp_ifft[i][0] * sz_float_invs;
		c.push_back(temp1);
	}
	return 0;
}
