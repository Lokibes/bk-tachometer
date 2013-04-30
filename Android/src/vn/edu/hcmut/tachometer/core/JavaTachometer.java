package vn.edu.hcmut.tachometer.core;

public class JavaTachometer {
	private SWIGTYPE_p_void tacho_inst;
	
	public JavaTachometer()	{
		//android.util.Log.e("J-TACH", "Before running the tachometer_process.Tachometer_Create()");
		tacho_inst = tachometer_process.Tachometer_Create();
		
		long ret = tachometer_process.Tachometer_Init(tacho_inst);
		if (ret != -1) {
			android.util.Log.e("J-TACH", "Tachometer_Init succeeds with result: " + ret);
		} else {
			android.util.Log.e("J-TACH", "Tachometer_Init fails");
			return;
		}
	}
	
	public void jTachConfig(long estimatedFreq)	{
		// TODO use the core and user 's inputs to get estimated frequency
		android.util.Log.e("J-TACH", "Before config");
		long ret = tachometer_process.Tachometer_Config(tacho_inst, estimatedFreq);
		android.util.Log.e("J-TACH", "Config done with result: " + ret);
	}
	
	public void jTachProcess()	{
		
	}
	
	public void jTachFree()	{
		
	}
}
