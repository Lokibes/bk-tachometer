package vn.edu.hcmut.tachometer.core;

public class JavaTachometer {
	
	public JavaTachometer()	{
		SWIGTYPE_p_p_void tacho_inst = new SWIGTYPE_p_p_void();
		android.util.Log.e("J-TACH", "New tacho_inst done!");
		
		tachometer_process.Tachometer_Create(tacho_inst);
		android.util.Log.e("J-TACH", "Created the tachometer successfully!!!");
	}
}
