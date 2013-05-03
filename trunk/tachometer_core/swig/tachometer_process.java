/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package vn.edu.hcmut.tachometer.core;

public class tachometer_process {
  public static SWIGTYPE_p_void Tachometer_Create() {
    long cPtr = tachometer_processJNI.Tachometer_Create();
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static SWIGTYPE_p_int32_t Tachometer_Init(SWIGTYPE_p_void tacho) {
    return new SWIGTYPE_p_int32_t(tachometer_processJNI.Tachometer_Init(SWIGTYPE_p_void.getCPtr(tacho)), true);
  }

  public static SWIGTYPE_p_int32_t Tachometer_Free(SWIGTYPE_p_void tacho) {
    return new SWIGTYPE_p_int32_t(tachometer_processJNI.Tachometer_Free(SWIGTYPE_p_void.getCPtr(tacho)), true);
  }

  public static SWIGTYPE_p_int32_t Tachometer_Config(SWIGTYPE_p_void tacho, SWIGTYPE_p_int32_t estimatedFreq) {
    return new SWIGTYPE_p_int32_t(tachometer_processJNI.Tachometer_Config(SWIGTYPE_p_void.getCPtr(tacho), SWIGTYPE_p_int32_t.getCPtr(estimatedFreq)), true);
  }

  public static float Tachometer_Process(SWIGTYPE_p_void tacho, SWIGTYPE_p_int16_t inAudio) {
    return tachometer_processJNI.Tachometer_Process(SWIGTYPE_p_void.getCPtr(tacho), SWIGTYPE_p_int16_t.getCPtr(inAudio));
  }

  public static SWIGTYPE_p_float Tachometer_FFT_Out(SWIGTYPE_p_void tacho) {
    long cPtr = tachometer_processJNI.Tachometer_FFT_Out(SWIGTYPE_p_void.getCPtr(tacho));
    return (cPtr == 0) ? null : new SWIGTYPE_p_float(cPtr, false);
  }

}
