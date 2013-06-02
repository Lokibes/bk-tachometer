#ifndef WAVELET2S_H
#define WAVELET2S_H
#include <vector>
#include <complex>
#include "tachometer_wavelet1d_defs.h"
using namespace std;

// 1D Stationary Wavelet Transform
void* swt(wavelets_t*, vector<float> &, int, string, vector<float> &, int &);

#endif/* WAVELET2S_H */
