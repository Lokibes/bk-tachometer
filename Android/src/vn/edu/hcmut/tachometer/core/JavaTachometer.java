package vn.edu.hcmut.tachometer.core;

public class JavaTachometer {
	private SWIGTYPE_p_void tacho_inst;

	public JavaTachometer() {
		// android.util.Log.e("J-TACH",
		// "Before running the tachometer_process.Tachometer_Create()");
		tacho_inst = tachometer_process.Tachometer_Create();

		long ret = tachometer_process.Tachometer_Init(tacho_inst);
		if (ret != -1) {
			android.util.Log.e("J-TACH",
					"Tachometer_Init succeeds with result: " + ret);
		} else {
			android.util.Log.e("J-TACH", "Tachometer_Init fails");
			return;
		}
	}

	public void jTachConfig(long estimatedFreq) {
		// TODO use the core and user 's inputs to get estimated frequency
		android.util.Log.e("J-TACH", "Before config");
		long ret = tachometer_process.Tachometer_Config(tacho_inst,
				estimatedFreq);
		android.util.Log.e("J-TACH", "Config done with result: " + ret);
	}

	public float jTachProcess(short[] inAudio) {
		// TODO use the core and user 's inputs to get estimated frequency
		android.util.Log.e("J-TACH", "Before process");
		float resultFreq = tachometer_process.Tachometer_Process(tacho_inst,
				inAudio);

		// Freq not found
		if (resultFreq == 0.0f) {
			android.util.Log.e("J-TACH", "Process done. Freq not found");
		}

		// Found Freq
		else if (resultFreq > 0.0f) {
			android.util.Log.e("J-TACH", "Process done, result Freq = "
					+ resultFreq);

		}

		// Error
		else {
			android.util.Log.e("J-TACH", "Process error!");
		}

		android.util.Log.e("J-TACH", "Processing done " + resultFreq);
		return resultFreq;
	}

	public void jTachFree() {
		android.util.Log.e("J-TACH", "Before Free");
		tachometer_process.Tachometer_Free(tacho_inst);
		android.util.Log.e("J-TACH", "After freed");
	}
}
