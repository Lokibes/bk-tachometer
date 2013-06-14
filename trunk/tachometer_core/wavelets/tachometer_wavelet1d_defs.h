/*
 * tachometer_wavelet1d_defs.h
 *
 *  Created on: May 25, 2013
 *      Author: oneadmin
 */

#ifndef TACHOMETER_WAVELET1D_DEFS_H_
#define TACHOMETER_WAVELET1D_DEFS_H_

#include <vector>
#include <string>
#include <map>
#include <list>
#include "tachometer_defs.h"
#include "fftw3.h"
using namespace std;

#define TACHO_WAVELET_TRANSFORM_LEVEL_NUM						4		// 4 levels of transformation
#define TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS						15		// The number of estimated peaks to find the correct peak
#define TACHO_WAVELET_HISTORY_SIZE								25
#define TACHO_WAVELET_ACCEPTABLE_HISTORY_NUM					15
#define TACHO_WAVELET_DELTA_HISTORY_THRESHOLD					(TACHO_WAVELET_HISTORY_SIZE - TACHO_WAVELET_ACCEPTABLE_HISTORY_NUM)
#define TACHO_WAVELET_MAX_NUM_OF_HISTORY_UNITS					(TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS * 2)
typedef struct {
	float resultFrequency;
	float resultSpectrum;
	float variance;
} history_result_t;

typedef struct {
	float freqSum;
	float specSum;
	float averageFreq;
	float averageSpectrum;
	int32_t num;
	int32_t historyTime;
	list<float> historyFreqList;
	list<float> historySpectrumList;
	map<float, pair<float, float> > guessFreqMap;
} history_unit_t;

typedef struct {
	list<history_unit_t*> historyList;
	map<int32_t, history_result_t*> resultMap;
	float currentFreq;
} history_t;

typedef struct {
	string waveletFamily;
	vector<float> t;
	vector<float> zeroCrossingsDetail1;
	vector<int32_t> zeroCrossingsDetail1Indexes;
	vector<float> zeroCrossingsDetail2;
	vector<float> zeroCrossingsDetail3;
	vector<float> zeroCrossingsDetail4;
	map<float, pair<float, float> > peakMap; // map< spectrum, pair < spectrum, frequency > >
	history_t* waveletsHistory;
	vector<float> inWavelets;
	vector<float> outWavelets;
	int32_t waveletLength;
	int32_t sizeFFT;
	bool isInitialized;
	fftwf_complex *inp_data;
	fftwf_complex *filt_data;
	fftwf_complex *inp_fft;
	fftwf_complex *filt_fft;
	fftwf_complex *temp_data;
	fftwf_complex *temp_ifft;
	fftwf_plan plan_forward_inp;
	fftwf_plan plan_forward_filt;
	fftwf_plan plan_backward;
} wavelets_t;

#endif /* TACHOMETER_WAVELET1D_DEFS_H_ */
