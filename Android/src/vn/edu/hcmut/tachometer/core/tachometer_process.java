/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package vn.edu.hcmut.tachometer.core;

import java.nio.ByteBuffer;

public class tachometer_process {
	public static SWIGTYPE_p_void Tachometer_Create() {
		long cPtr = tachometer_processJNI.Tachometer_Create();
		return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
	}

	public static long Tachometer_Init(SWIGTYPE_p_void tacho) {
		return tachometer_processJNI.Tachometer_Init(SWIGTYPE_p_void
				.getCPtr(tacho));
	}

	public static long Tachometer_Free(SWIGTYPE_p_void tacho,
			ByteBuffer byteBuffer) {
		return tachometer_processJNI.Tachometer_Free(
				SWIGTYPE_p_void.getCPtr(tacho), byteBuffer);
	}

	public static long Tachometer_Config(SWIGTYPE_p_void tacho,
			long estimatedFreq) {
		return tachometer_processJNI.Tachometer_Config(
				SWIGTYPE_p_void.getCPtr(tacho), estimatedFreq);
	}

	/**
	 * Note: This function should only be called right after the tacho is
	 * created.
	 * 
	 * @param tacho
	 * @return The direct ByteBuffer pointing to the audio array
	 */
	public static ByteBuffer Tachometer_Get_Audio_Frame_Location(
			SWIGTYPE_p_void tacho) {
		return (ByteBuffer) tachometer_processJNI
				.Tachometer_Get_Audio_Frame_Location(SWIGTYPE_p_void
						.getCPtr(tacho));
	}

	public static long Tachometer_Push(SWIGTYPE_p_void tacho,
			ByteBuffer byteBuffer, long size) {
		return tachometer_processJNI.Tachometer_Push(
				SWIGTYPE_p_void.getCPtr(tacho), byteBuffer, size);
	}

	public static float Tachometer_Process(SWIGTYPE_p_void tacho) {
		return tachometer_processJNI.Tachometer_Process(SWIGTYPE_p_void
				.getCPtr(tacho));
	}

	public static float Tachometer_FFT_Out(SWIGTYPE_p_void tacho, int beginFreq,
			int endFreq, int size, float[] fft_out_magnitude) {
		return tachometer_processJNI.Tachometer_FFT_Out(
				SWIGTYPE_p_void.getCPtr(tacho), beginFreq, endFreq, size,
				fft_out_magnitude);
	}
}
