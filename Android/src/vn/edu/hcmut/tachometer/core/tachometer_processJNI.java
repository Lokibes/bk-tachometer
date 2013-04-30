/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package vn.edu.hcmut.tachometer.core;

public class tachometer_processJNI {
	public final static native long Tachometer_Create();

	public final static native long Tachometer_Init(long jarg1);

	public final static native long Tachometer_Free(long jarg1);

	public final static native long Tachometer_Config(long jarg1, long jarg2);

	public final static native long Tachometer_Process(long jarg1, short[] audioArray,
			long jarg3);

	static {
		try {
			System.loadLibrary("tachometer_core");
			//android.util.Log.e("tachometer_processJNI", "Loading the tachometer_core library successfully");
		} catch (UnsatisfiedLinkError e) {
			//android.util.Log.e("tachometer_processJNI", "Cannot load the tachometer_core library");
		}
	}
}
