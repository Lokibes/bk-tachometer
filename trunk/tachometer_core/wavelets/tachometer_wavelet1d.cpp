/*
 * tachometer_wavelet1d.cpp
 *
 *  Created on: May 25, 2013
 *      Author: oneadmin
 */

#include <vector>
#include <cstddef>
#include <cstdlib>
#ifdef TACHO_DEBUG
#include <iostream>
#endif
#include <map>
#include <list>
#include <cmath>
#include <string>
#include "fftw3.h"
#include "wavelet2s.h"
#include "tachometer_wavelet1d.h"
#include "tachometer_wavelet1d_defs.h"
#include "tachometer_defs.h"
using namespace std;

#define IS_ACCEPTABLE_FREQ(testFreq, averageFreq) \
	((testFreq < (averageFreq + 10.0f)) && (testFreq > (averageFreq - 10.0f)))

// Asume that freq1 < freq2
#define IS_HARMONIC_PAIR(freq1, freq2) \
	(((freq2 < freq1 * 2.05f) && (freq2 > freq1 * 1.95f)) || ((freq2 < freq1 * 3.075f) && (freq2 > freq1 * 2.925f)) || ((freq2 < freq1 * 4.1f) && (freq2 > freq1 * 3.9f)))

/**
 * Some helper function names
 */
static void Wavelets_Zero_Crossings(wavelets_t *wavelets_inst);
static float Wavelets_Peaks_Finding(wavelets_t *wavelets_inst, float* fftArray);
static float Wavelets_History_Analyzing(history_t* waveletsHistory,
		map<float, pair<float, float> > &peakMap);
static float Wavelets_Variance_Calculating(history_unit_t* historyUnit);
/**
 * Main function implementations
 */
int32_t Tachometer_Wavelet_Create(void** wavelets) {
	wavelets_t* wavelets_inst = new wavelets_t(); // Allocate on stack. So that no need to free.
	*wavelets = wavelets_inst;
	wavelets_inst->waveletsHistory = new history_t();

	return 0;
}

int32_t Tachometer_Wavelet_Init(void* wavelets) {
	wavelets_t* wavelets_inst = (wavelets_t*) wavelets;
	if (wavelets_inst == NULL) {
		return -1;
	}
	wavelets_inst->waveletFamily = string("bior1.1");
	wavelets_inst->inWavelets.reserve(TACHO_ESTIMATION_RANGE);
	wavelets_inst->outWavelets.reserve(
			TACHO_ESTIMATION_RANGE * (TACHO_WAVELET_TRANSFORM_LEVEL_NUM + 1));
	wavelets_inst->t.reserve(TACHO_ESTIMATION_RANGE);
	wavelets_inst->zeroCrossingsDetail1.reserve(TACHO_ESTIMATION_RANGE);
	wavelets_inst->zeroCrossingsDetail1Indexes.reserve(TACHO_ESTIMATION_RANGE);
	wavelets_inst->zeroCrossingsDetail2.reserve(TACHO_ESTIMATION_RANGE);
	wavelets_inst->zeroCrossingsDetail3.reserve(TACHO_ESTIMATION_RANGE);
	wavelets_inst->zeroCrossingsDetail4.reserve(TACHO_ESTIMATION_RANGE);

	wavelets_inst->sizeFFT = -1;
	wavelets_inst->isInitialized = false;

	return 0;
}

int32_t Tachometer_Wavelet_Prepare(void* wavelets, Tacho_t* tacho_inst) {
	wavelets_t* wavelets_inst = (wavelets_t*) wavelets;
	if (wavelets_inst == NULL) {
		return -1;
	}

	// t = linspace(frequencies(waveletRangeBeginIndex), frequencies(waveletRangeEndIndex), length(array));
	// Initialize the t array
	wavelets_inst->t.clear();
	float beginFreq = ((float) tacho_inst->beginIndex)
			* TACHO_INDEX_TO_FREQ_COEF;
	float endFreq = ((float) (tacho_inst->beginIndex + TACHO_ESTIMATION_RANGE
			- 1)) * TACHO_INDEX_TO_FREQ_COEF;
	float deltaFreq = (endFreq - beginFreq) / (TACHO_ESTIMATION_RANGE - 1);
	for (int i = 0; i < TACHO_ESTIMATION_RANGE; i++) {
		wavelets_inst->t.push_back(beginFreq + i * deltaFreq);
	}

	// Clear the history
	for (list<history_unit_t*>::iterator it =
			wavelets_inst->waveletsHistory->historyList.begin();
			it != wavelets_inst->waveletsHistory->historyList.end(); it++) {
		delete (*it);
	}
	wavelets_inst->waveletsHistory->historyList.clear();
	wavelets_inst->waveletsHistory->currentFreq = 0.0f;

	return 0;
}

int32_t Tachometer_Wavelet_Free(void* wavelets) {
	wavelets_t* wavelets_inst = (wavelets_t*) wavelets;
	if (wavelets_inst == NULL) {
		return -1;
	}

	fftwf_free(wavelets_inst->inp_data);
	fftwf_free(wavelets_inst->filt_data);
	fftwf_free(wavelets_inst->inp_fft);
	fftwf_free(wavelets_inst->filt_fft);
	fftwf_free(wavelets_inst->temp_data);
	fftwf_free(wavelets_inst->temp_ifft);
	fftwf_destroy_plan(wavelets_inst->plan_forward_inp);
	fftwf_destroy_plan(wavelets_inst->plan_forward_filt);
	fftwf_destroy_plan(wavelets_inst->plan_backward);

	delete (wavelets_inst->waveletsHistory);
	delete (wavelets_inst);
	return 0;
}

float Tachometer_Wavelet_Transform(void* wavelets, float* fftArray,
		int32_t size) {
	wavelets_t* wavelets_inst = (wavelets_t*) wavelets;
	if (wavelets_inst == NULL) {
		return -1.0f;
	}

	// Copy the content of the fftArray to the outWavelets vector
	wavelets_inst->inWavelets.clear();
	wavelets_inst->outWavelets.clear();
	wavelets_inst->inWavelets.insert(wavelets_inst->inWavelets.begin(),
			fftArray, fftArray + size);

	// Doing the SWT
	swt(wavelets_inst, wavelets_inst->inWavelets,
			TACHO_WAVELET_TRANSFORM_LEVEL_NUM, wavelets_inst->waveletFamily,
			wavelets_inst->outWavelets, wavelets_inst->waveletLength);

	// Finding the zero crossing points
	Wavelets_Zero_Crossings(wavelets_inst);

	// Analyzing the peaks
	float resultFreq = Wavelets_Peaks_Finding(wavelets_inst, fftArray);

	return resultFreq;
}

/**
 * Helper function implementations
 */
static void Wavelets_Zero_Crossings(wavelets_t *wavelets_inst) {
	// t = linspace(frequencies(waveletRangeBeginIndex), frequencies(waveletRangeEndIndex), length(array));

	// The wavelets_inst->waveletLength should be TACHO_ESTIMATION_RANGE
	// This comment is when the TACHO_WAVELET_TRANSFORM_LEVEL_NUM is 4
	// In the wavelets_inst->outWavelets array,:
	//	Index (0 * TACHO_ESTIMATION_RANGE)	-> 		(1 * TACHO_ESTIMATION_RANGE - 1) is detail level 1
	//  Index (1 * TACHO_ESTIMATION_RANGE)	->		(2 * TACHO_ESTIMATION_RANGE - 1) is approximation level 1
	//	Index (2 * TACHO_ESTIMATION_RANGE)	-> 		(3 * TACHO_ESTIMATION_RANGE - 1) is detail level 2
	//  Index (3 * TACHO_ESTIMATION_RANGE)	->		(4 * TACHO_ESTIMATION_RANGE - 1) is approximation level 2
	//	Index (4 * TACHO_ESTIMATION_RANGE)	-> 		(5 * TACHO_ESTIMATION_RANGE - 1) is detail level 3
	//  Index (5 * TACHO_ESTIMATION_RANGE)	->		(6 * TACHO_ESTIMATION_RANGE - 1) is approximation level 3
	//	Index (6 * TACHO_ESTIMATION_RANGE)	-> 		(7 * TACHO_ESTIMATION_RANGE - 1) is detail level 4
	//  Index (7 * TACHO_ESTIMATION_RANGE)	->		(8 * TACHO_ESTIMATION_RANGE - 1) is approximation level 4

	// Clear the zero crossing arrays
	wavelets_inst->zeroCrossingsDetail1.clear();
	wavelets_inst->zeroCrossingsDetail1Indexes.clear();
	wavelets_inst->zeroCrossingsDetail2.clear();
	wavelets_inst->zeroCrossingsDetail3.clear();
	wavelets_inst->zeroCrossingsDetail4.clear();

	float multipleResult;
	float deltaX = wavelets_inst->t[1] - wavelets_inst->t[0];
	float deltaY;
	int32_t begin, end;

	// Process the Detail level 1
	begin = 0 * TACHO_ESTIMATION_RANGE;
	end = 1 * TACHO_ESTIMATION_RANGE - 1;
	for (int32_t i = begin; i <= end; i++) {
		multipleResult = wavelets_inst->outWavelets[i]
				* wavelets_inst->outWavelets[i + 1];

		if (multipleResult <= 0.0f) { // This seems to be the zero crossing point
			deltaY = wavelets_inst->outWavelets[i + 1]
					- wavelets_inst->outWavelets[i];
			if (deltaY > 0.0f) { // For finding local maximal, only choose deltaY > 0
				wavelets_inst->zeroCrossingsDetail1.push_back(
						wavelets_inst->t[i]
								- wavelets_inst->outWavelets[i - begin] * deltaX
										/ deltaY);
				wavelets_inst->zeroCrossingsDetail1Indexes.push_back(i);
			}
		}
	} // End for

	// Process the Detail level 2
	begin = 2 * TACHO_ESTIMATION_RANGE;
	end = 3 * TACHO_ESTIMATION_RANGE - 1;
	for (int32_t i = begin; i <= end; i++) {
		multipleResult = wavelets_inst->outWavelets[i]
				* wavelets_inst->outWavelets[i + 1];

		if (multipleResult <= 0.0f) { // This seems to be the zero crossing point
			deltaY = wavelets_inst->outWavelets[i + 1]
					- wavelets_inst->outWavelets[i];
			if (deltaY > 0.0f) { // For finding local maxima, only choose deltaY > 0
				wavelets_inst->zeroCrossingsDetail2.push_back(
						wavelets_inst->t[i - begin]
								- wavelets_inst->outWavelets[i] * deltaX
										/ deltaY);
			}
		}
	} // End for

	// Process the Detail level 3
	begin = 4 * TACHO_ESTIMATION_RANGE;
	end = 5 * TACHO_ESTIMATION_RANGE - 1;
	for (int32_t i = begin; i <= end; i++) {
		multipleResult = wavelets_inst->outWavelets[i]
				* wavelets_inst->outWavelets[i + 1];
		deltaY = wavelets_inst->outWavelets[i + 1]
				- wavelets_inst->outWavelets[i];
		if (multipleResult <= 0.0f) { // This seems to be the zero crossing point
			deltaY = wavelets_inst->outWavelets[i + 1]
					- wavelets_inst->outWavelets[i];
			if (deltaY > 0.0f) { // For finding local maxima, only choose deltaY > 0
				wavelets_inst->zeroCrossingsDetail3.push_back(
						wavelets_inst->t[i - begin]
								- wavelets_inst->outWavelets[i] * deltaX
										/ deltaY);
			}
		}
	} // End for

	// Process the Detail level 4
	begin = 6 * TACHO_ESTIMATION_RANGE;
	end = 7 * TACHO_ESTIMATION_RANGE - 1;
	for (int32_t i = begin; i <= end; i++) {
		multipleResult = wavelets_inst->outWavelets[i]
				* wavelets_inst->outWavelets[i + 1];
		deltaY = wavelets_inst->outWavelets[i + 1]
				- wavelets_inst->outWavelets[i];
		if (multipleResult <= 0.0f) { // This seems to be the zero crossing point
			deltaY = wavelets_inst->outWavelets[i + 1]
					- wavelets_inst->outWavelets[i];
			if (deltaY > 0.0f) { // For finding local maxima, only choose deltaY > 0
				wavelets_inst->zeroCrossingsDetail4.push_back(
						wavelets_inst->t[i - begin]
								- wavelets_inst->outWavelets[i] * deltaX
										/ deltaY);
			}
		}
	} // End for

	// No need to filter the zero crossing points in the tachometer case
}

static float Wavelets_Peaks_Finding(wavelets_t *wavelets_inst,
		float* fftArray) {
	/*
	 * Algorithm:
	 * Step 1: Loop in the detail 4 zero crossing array and find the nearest in the detail
	 * 3 zero crossing array. Continue again until can find the nearest in the detail 1 zero crossing array.
	 * Step 2: with the values founded, find TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS biggest values in the FFT array and store to
	 * the history.
	 * 3. If the history has enough data, analyze to find the best peak.
	 *
	 */
	int32_t detail4Index = 0;
	int32_t detail4Size = wavelets_inst->zeroCrossingsDetail4.size();

	int32_t detail3Index = 0;
	int32_t detail3Size = wavelets_inst->zeroCrossingsDetail3.size();

	int32_t detail2Index = 0;
	int32_t detail2Size = wavelets_inst->zeroCrossingsDetail2.size();

	int32_t detail1Index = 0;
	int32_t detail1Size = wavelets_inst->zeroCrossingsDetail1.size();

	wavelets_inst->peakMap.clear();
	float detail4Frequency, detail3Frequency, detail2Frequency,
			detail1Frequency;
	for (; detail4Index < detail4Size; detail4Index++) {
		detail4Frequency = wavelets_inst->zeroCrossingsDetail4[detail4Index];
		for (; detail3Index < detail3Size; detail3Index++) {
			detail3Frequency =
					wavelets_inst->zeroCrossingsDetail3[detail3Index];
			if (detail3Frequency - detail4Frequency >= 0) {
				for (; detail2Index < detail2Size; detail2Index++) {
					detail2Frequency =
							wavelets_inst->zeroCrossingsDetail2[detail2Index];
					if (detail2Frequency - detail3Frequency >= 0) { // This is the accepted detail2Frequency
						for (; detail1Index < detail1Size; detail1Index++) {
							detail1Frequency =
									wavelets_inst->zeroCrossingsDetail1[detail1Index];
							if (detail1Frequency - detail2Frequency >= 0) { // This is the accepted detail2Frequency
								int32_t freqIndex =
										wavelets_inst->zeroCrossingsDetail1Indexes[detail1Index];
								float spectrumValue = fftArray[freqIndex];
								pair<float, float> aPair = make_pair(
										spectrumValue, detail1Frequency);
								wavelets_inst->peakMap[spectrumValue] = aPair;
								break;
							} // End if
						} // End for
						break;
					} // End if
				} // End for
				break;
			} // End if
		} // End for
	} // End for

	float resultFreq = Wavelets_History_Analyzing(
			wavelets_inst->waveletsHistory, wavelets_inst->peakMap);

	return resultFreq;
}

static float Wavelets_History_Analyzing(history_t* waveletsHistory,
		map<float, pair<float, float> > &peakMap) {
#ifdef TACHO_DEBUG
	cout << "Then number of history units is "
	<< waveletsHistory->historyList.size() << endl;
#endif
	if (waveletsHistory->historyList.empty() == false) {
		// Clear the guessedFreqMap of every history unit
		for (list<history_unit_t*>::iterator it =
				waveletsHistory->historyList.begin();
				it != waveletsHistory->historyList.end(); it++) {
			(*it)->guessFreqMap.clear();
		}

		map<float, history_unit_t*> testMap;
		int32_t tmpCounter = 0;
		for (map<float, pair<float, float> >::reverse_iterator rit =
				peakMap.rbegin(); rit != peakMap.rend(); rit++) { // Loop for the last value pairs in the map
			// For each peak, find the best history unit. If not found, create another history unit
			float spectrumValue = (*rit).second.first;
			float freqValue = (*rit).second.second;
			bool hasSuitableHistoryUnit = false;
			// Loop to find the best history unit
			testMap.clear();
			for (list<history_unit_t*>::iterator it =
					waveletsHistory->historyList.begin();
					it != waveletsHistory->historyList.end(); it++) {
				float averageFreq = (*it)->averageFreq;
				if (IS_ACCEPTABLE_FREQ(freqValue, averageFreq) == false) {
					continue;
				} else { // IS_ACCEPTABLE_FREQ(freqValue, averageFreq) == true
					// This maybe a good history unit
					float deltaFreq = fabsf(averageFreq - freqValue);
					testMap[deltaFreq] = *it;
					hasSuitableHistoryUnit = true;
				} // End if (IS_ACCEPTABLE_FREQ(freqValue, averageFreq) == false)
			} // End for (list<history_unit_t*>::iterator it = ...
			if (hasSuitableHistoryUnit == true) {
				history_unit_t* historyUnit = (*(testMap.begin())).second;
				float deltaFreq = fabsf(historyUnit->averageFreq - freqValue);
				historyUnit->guessFreqMap[deltaFreq] = (*rit).second;
			} else { // hasSuitableHistoryUnit == false
				if (waveletsHistory->historyList.size()
						< TACHO_WAVELET_MAX_NUM_OF_HISTORY_UNITS) {
					// Create a new history unit
					history_unit_t* historyUnit = new history_unit_t();
					historyUnit->averageFreq = freqValue;
					historyUnit->averageSpectrum = spectrumValue;
					historyUnit->freqSum = freqValue;
					historyUnit->specSum = spectrumValue;
					historyUnit->num = 1;
					historyUnit->historyFreqList.clear();
					historyUnit->historyFreqList.push_front(freqValue);
					historyUnit->historySpectrumList.clear();
					historyUnit->historySpectrumList.push_back(spectrumValue);
					historyUnit->historyTime = 0; // For updating later
					historyUnit->guessFreqMap.clear();
					waveletsHistory->historyList.push_back(historyUnit);
				}
			} // End if (hasSuitableHistoryUnit)

			// Check if enough data
			tmpCounter++;
			if (tmpCounter >= TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS) {
				break;
			} // End if (tmpCounter >= TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS)
		} // End for (map<float, float>::reverse_iterator rit = peakMap.rbegin(); ...

		// Analyze each history unit:
		// 0. Update every history unit if it has a guessed frequency in the guessedFreqMap
		// 1. Update the historyTime
		// 2. Dismiss the history unit if: historyTime == TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS but num < TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS
		// 3. Return no result if no history unit has historyTime == TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS
		// 4. If there are some acceptable history units, choose the one whose FFT is highest
		list<history_unit_t*>::iterator it =
				waveletsHistory->historyList.begin();
		bool noAcceptableHistoryUnit = true;

		// Clear the result map
		for (map<int32_t, history_result_t*>::iterator it =
				waveletsHistory->resultMap.begin();
				it != waveletsHistory->resultMap.end(); it++) {
			delete ((*it).second);
		}
		waveletsHistory->resultMap.clear();
		while (it != waveletsHistory->historyList.end()) {
			history_unit_t* historyUnit = *it;
			historyUnit->historyTime++;
			// Update the history unit if there is a guessed frequency
			if (historyUnit->guessFreqMap.empty() == false) {
				// Get the entry in the map whose key (daltaFreq) is smallest.
				// This is the first element in the map
				float spectrumValue =
						(*historyUnit->guessFreqMap.begin()).second.first;
				float freqValue =
						(*historyUnit->guessFreqMap.begin()).second.second;
				historyUnit->num++;
				historyUnit->historyFreqList.push_back(freqValue);
				historyUnit->freqSum += freqValue;
				historyUnit->historySpectrumList.push_back(spectrumValue);
				historyUnit->specSum += spectrumValue;
				if (historyUnit->historyTime > TACHO_WAVELET_HISTORY_SIZE) {
					float firstFreq = historyUnit->historyFreqList.front();
					float firstSpec = historyUnit->historySpectrumList.front();
					historyUnit->historyFreqList.pop_front(); // Remove the first
					historyUnit->historySpectrumList.pop_front(); // Remove the first
					if (firstFreq != 0) {
						historyUnit->freqSum -= firstFreq;
						historyUnit->specSum -= firstSpec;
						historyUnit->num--;
					} // End if (firstFreq != 0)
				} // End if (historyUnit->historyTime > TACHO_WAVELET_HISTORY_SIZE)
				historyUnit->averageFreq = historyUnit->freqSum
						/ ((float) historyUnit->num);
				historyUnit->averageSpectrum = historyUnit->specSum
						/ ((float) historyUnit->num);
			} else { //historyUnit->guessFreqMap.empty() == true
				// There is no frequency to update
				// But still need to update the num when historyUnit->historyTime > TACHO_WAVELET_HISTORY_SIZE
				if (historyUnit->historyTime > TACHO_WAVELET_HISTORY_SIZE) {
					float firstFreq = historyUnit->historyFreqList.front();
					float firstSpec = historyUnit->historySpectrumList.front();
					historyUnit->historyFreqList.pop_front(); // Remove the first
					historyUnit->historySpectrumList.pop_front(); // Remove the first
					if (firstFreq != 0.0f) {
						historyUnit->freqSum -= firstFreq;
						historyUnit->specSum -= firstSpec;
						historyUnit->num--;
						historyUnit->averageFreq = historyUnit->freqSum
								/ ((float) historyUnit->num);
						historyUnit->averageSpectrum = historyUnit->specSum
								/ ((float) historyUnit->num);
						historyUnit->historyFreqList.push_back(0.0f);
						historyUnit->historySpectrumList.push_back(0.0f);
					} else { // firstFreq == 0.0f
						historyUnit->historyFreqList.push_back(0.0f);
						historyUnit->historySpectrumList.push_back(0.0f);
					} // End if (firstFreq != 0.0f)
				} // End if (historyUnit->historyTime > TACHO_WAVELET_HISTORY_SIZE)
			} // End if (historyUnit->guessFreqMap.empty() == false)

			if ((historyUnit->historyTime - historyUnit->num)
					> TACHO_WAVELET_DELTA_HISTORY_THRESHOLD
					) {
				it = waveletsHistory->historyList.erase(it); // it will point to the next history unit
				delete (historyUnit);
				continue;
			}
			if (historyUnit->historyTime > TACHO_WAVELET_HISTORY_SIZE) {
				historyUnit->historyTime = TACHO_WAVELET_HISTORY_SIZE;

				if ((*it)->num < TACHO_WAVELET_ACCEPTABLE_HISTORY_NUM) {
					// Dismiss this history unit
					it = waveletsHistory->historyList.erase(it); // it will point to the next history unit
					delete (historyUnit);
					continue;
				} else { // (*it)->num >= TACHO_WAVELET_ACCEPTABLE_HISTORY_NUM
					// TODO: compare the magnitude. Now, only choose the freq value whose num is biggest
					// Check if resultMap contains historyUnit->num
					map<int32_t, history_result_t*>::iterator resultMapIterator =
							waveletsHistory->resultMap.find(historyUnit->num);
					if (resultMapIterator == waveletsHistory->resultMap.end()) { // the resultMap does not have the key historyUnit->num
						history_result_t* historyResult =
								new history_result_t();
						historyResult->resultFrequency =
								historyUnit->averageFreq;
						historyResult->resultSpectrum =
								historyUnit->averageSpectrum;
						historyResult->variance = Wavelets_Variance_Calculating(
								historyUnit);
						waveletsHistory->resultMap[historyUnit->num] =
								historyResult;
					} else { // resultMapIterator != resultMap.end() : the resultMap has the key historyUnit->num
						float currentVariance = Wavelets_Variance_Calculating(
								historyUnit);
						history_result_t* lastHistoryResult =
								(*resultMapIterator).second;
						if (currentVariance
								< lastHistoryResult->variance * 0.5f) {
							lastHistoryResult->resultFrequency =
									historyUnit->averageFreq;
							lastHistoryResult->resultSpectrum =
									historyUnit->averageSpectrum;
							lastHistoryResult->variance = currentVariance;
						} else if ((currentVariance
								>= lastHistoryResult->variance * 0.5f)
								&& (currentVariance
										<= lastHistoryResult->variance * 1.5f)) {
							// harmonic filter
							float smallFreq, bigFreq;
							if (historyUnit->averageFreq
									< lastHistoryResult->resultFrequency) {
								smallFreq = historyUnit->averageFreq;
								bigFreq = lastHistoryResult->resultFrequency;
							} else {
								smallFreq = lastHistoryResult->resultFrequency;
								bigFreq = historyUnit->averageFreq;
							}
							if (IS_HARMONIC_PAIR(smallFreq, bigFreq)) {
								// Choose the freq with bigger average spectrum
								if (historyUnit->averageSpectrum
										> lastHistoryResult->resultSpectrum) {
									lastHistoryResult->resultFrequency =
											historyUnit->averageFreq;
									lastHistoryResult->resultSpectrum =
											historyUnit->averageSpectrum;
									lastHistoryResult->variance =
											currentVariance;
								}
#ifdef TACHO_DEBUG
								cout << smallFreq << " and " << bigFreq << " are harmonic frequencies." << endl;
#endif
							} else {
								// Choose the smaller variance
								lastHistoryResult->resultFrequency =
										historyUnit->averageFreq;
								lastHistoryResult->resultSpectrum =
										historyUnit->averageSpectrum;
								lastHistoryResult->variance = currentVariance;
							}
						} // if (currentVariance < lastHistoryResult->variance * 0.5f)
					} // End if (resultMapIterator != resultMap.end())
					noAcceptableHistoryUnit = false;
				} // End if ((*it)->num < TACHO_WAVELET_ACCEPTABLE_HISTORY_NUM)
			} // if ((*it)->historyTime >= TACHO_WAVELET_HISTORY_SIZE)
			it++;
		} // End while (it != waveletsHistory->historyList.end())

#ifdef TACHO_DEBUG
		cout << "The resultMap size: " << waveletsHistory->resultMap.size()
		<< endl;
#endif

		if (noAcceptableHistoryUnit == false) {
			// There is a good frequency
			return (*(waveletsHistory->resultMap.rbegin())).second->resultFrequency;
		} else { // noAcceptableHistoryUnit == true
			return 0.0f;
		} // End if (noAcceptableHistoryUnit == false)

	} else { // waveletsHistory->historyList.empty() == true
		// Loop for every peak in the peakMap and create a history unit and add to the historyList
		int32_t tmpCounter = 0;
		for (map<float, pair<float, float> >::reverse_iterator rit =
				peakMap.rbegin(); rit != peakMap.rend(); rit++) {
			float freqValue = (*rit).second.second;
			float spectrumValue = (*rit).second.first;

			// Create a history unit
			history_unit_t* historyUnit = new history_unit_t();
			historyUnit->freqSum = freqValue;
			historyUnit->specSum = spectrumValue;
			historyUnit->averageFreq = freqValue;
			historyUnit->averageSpectrum = spectrumValue;
			historyUnit->num = 1;
			historyUnit->historyTime = 1;
			historyUnit->historyFreqList.clear();
			historyUnit->historySpectrumList.clear();
			historyUnit->historyFreqList.push_front(freqValue);
			historyUnit->historySpectrumList.push_front(spectrumValue);
			historyUnit->guessFreqMap.clear();
			waveletsHistory->historyList.push_back(historyUnit);

			// Check if enough data
			tmpCounter++;
			if (tmpCounter >= TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS) {
				break;
			} // End if (tmpCounter >= TACHO_WAVELET_NUM_OF_ESTIMATE_PEAKS)
		} // End for (map<float, float>::reverse_iterator rit = peakMap.rbegin(); ...
		return 0.0f;
	} // End if (waveletsHistory->historyList.empty() == false)
}

/**
 * Asume that this function is called only when the historyUnit->num is
 * bigger than or equal to TACHO_WAVELET_ACCEPTABLE_HISTORY_NUM
 */
static float Wavelets_Variance_Calculating(history_unit_t* historyUnit) {
	float variance = 0.0f;
	float tmp;
	for (list<float>::iterator it = historyUnit->historyFreqList.begin();
			it != historyUnit->historyFreqList.end(); it++) {
		tmp = (*it) - historyUnit->averageFreq;
		variance = variance + tmp * tmp;
	}
	variance = variance / historyUnit->num;

	// TODO: check this
	variance = sqrtf(variance) / historyUnit->averageFreq;

#ifdef TACHO_DEBUG
	cout << "The history unit " << historyUnit->averageFreq << ": num = "
	<< historyUnit->num << " and historyUnit->historyFreqList.size() = "
	<< historyUnit->historyFreqList.size() << " and variance = "
	<< variance << " and historyUnit->historyTime = " << historyUnit->historyTime << endl;
#endif

	return variance;
}
