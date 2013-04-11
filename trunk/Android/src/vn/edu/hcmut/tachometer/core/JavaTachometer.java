package vn.edu.hcmut.tachometer.core;

public class JavaTachometer {
	
	public JavaTachometer()	{
		android.util.Log.e("J-TACH", "Before running the tachometer_process.Tachometer_Create()");
		SWIGTYPE_p_void tacho_inst = tachometer_process.Tachometer_Create();
		android.util.Log.e("J-TACH", "Created the tachometer successfully");
				
		long ret = tachometer_process.Tachometer_Init(tacho_inst);
		if (ret == -1) {
			android.util.Log.e("J-TACH", "tachometer_process.Tachometer_Init fails");
			return;
		} else {
			android.util.Log.e("J-TACH", "tachometer_process.Tachometer_Init succeeds");
		}
	}
}
