package vn.edu.hcmut.tachometer.core;

import java.nio.ByteBuffer;

public class JavaTachometer {
	private SWIGTYPE_p_void tacho_inst;
	private static int processCounter = 0;

	public JavaTachometer() {
		tacho_inst = tachometer_process.Tachometer_Create();
	}

	public void jTachInit() {
		long ret = tachometer_process.Tachometer_Init(tacho_inst);
		processCounter = 0;
		if (ret != -1) {
			android.util.Log.e("J-TACH",
					"Tachometer_Init succeeds with result: " + ret);
		} else {
			android.util.Log.e("J-TACH", "Tachometer_Init fails");
			return;
		}
	}

	public void jTachConfig(long estimatedFreq) {
		long ret = tachometer_process.Tachometer_Config(tacho_inst,
				estimatedFreq);
		android.util.Log.e("J-TACH", "Config done with result: " + ret);
	}

	public float jTachProcess() {
		float resultFreq = tachometer_process.Tachometer_Process(tacho_inst);

		// Error
		if (resultFreq < 0.0f) {
			android.util.Log.e("J-TACH", "Process error!");
			return 0.0f;
		}
		return resultFreq;
	}

	public void jTachFree(ByteBuffer byteBuffer) {
		// android.util.Log.e("J-TACH", "Before Free");
		tachometer_process.Tachometer_Free(tacho_inst, byteBuffer);
		android.util.Log.e("J-TACH", "After freed");
	}

	public ByteBuffer jTachCreateAudioBuffer() {
		ByteBuffer byteBuffer = tachometer_process
				.Tachometer_Get_Audio_Frame_Location(tacho_inst);
		if (byteBuffer != null) {
			android.util.Log.e("J-TACH", "jTachCreateAudioBuffer successfully");
		} else {
			android.util.Log.e("J-TACH", "jTachCreateAudioBuffer return null");
		}

		return byteBuffer;
	}

	public void jTachPush(ByteBuffer byteBuffer, int size) {
		long ret = -1;
		ret = tachometer_process.Tachometer_Push(tacho_inst, byteBuffer,
				(long) size);

		if (ret < 0) {
			android.util.Log.e("J-TACH", "Pushing audio to buffer error");
		}
	}
	
	
}
