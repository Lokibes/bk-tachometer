package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DemoUIActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {
	private Toast notifier;		// a pop-up for testing purpose
	private SeekBar rpm;		// seek bar for estimating RPM
	private Button start;		// start/stop measuring
	private TextView rpmValue;	// notifier of the seek bar
	private TextView rpmCal;	// real-time calculated value of actual RPM
	
	private GLSurfaceView chartView;
	
	private Random randommer;	// randomly creates fake values for rpmCal. Testing purpose
	private Timer timer;
	private boolean isUpdateNeeded = true;
	
	private AudioRecord recorder = null;
	private FileWriter fw1, fw2;
	int bufferSize = 0;
	int read = AudioRecord.ERROR_INVALID_OPERATION;
	byte data[];
	private boolean isRecording = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relative);
        
        chartView = (ChartView)findViewById(R.id.chartView);
        chartView.setClickable(false);
        
        /** IMPORTANT! constructor of notifier. Don't miss! */
        notifier = Toast.makeText(this, "", 5);
        
        rpmValue = (TextView)findViewById(R.id.rpmValue);
        rpmCal = (TextView)findViewById(R.id.rpmCal);
        //rpmCal.setTextSize(rpmCal.getHeight());
        
        start = (Button)findViewById(R.id.buttonStart);
        start.setOnClickListener(this);
        
        rpm = (SeekBar)findViewById(R.id.seekBar);
        rpm.setOnSeekBarChangeListener(this);
        rpm.setMax(1000);
        
        randommer = new Random();
        isUpdateNeeded = false;
    }
    
    /** call when come back from the settings screen. Refresh parameters */
    protected void onStart ()	{
    	super.onStart();
    	
    	/** Adapt the seek bar and stuffs to the pre-defined settings */
    	// TODO change to the global shared pref, for the sake of the whole app
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int heli_type = Integer.valueOf(settings.getString("heli_type", "0"));
        
        switch(heli_type)	{
        	case 0:
        		rpm.setMax(1000);
        		Toast.makeText(this, "Standard", 5).show();
        		break;
        	case 1:
        		rpm.setMax(300);
        		Toast.makeText(this, "Microsized", 5).show();
        		break;
        	default:
        		Toast.makeText(this, "You couldn't see this cuz default = Standard Helicopter", 5).show();
        		break;
        }
    	
    	//notifier.setText("Just coming back...");
    	//notifier.show();
    }
    
    /** implements for the MENU soft-key. Android 2.3.x */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
            	Intent to_setting = new Intent(this, SettingActivity.class);
                startActivity(to_setting);
            	
            	//notifier.setText("Enter settings");
            	//notifier.show();
                return true;
            case R.id.log:
            	//Intent to_log = new Intent(this, LogActivity.class);
                //startActivity(to_log);
                
                Intent to_test_file = new Intent(this, LoggingActivity.class);
                startActivity(to_test_file);
                
            	//notifier.setText("Enter logs");
            	//notifier.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /** implements OnSeekBarChangeListener */
    public void onStartTrackingTouch(SeekBar sb) {
		
	}
	
	public void onStopTrackingTouch(SeekBar sb) {
		
	}
	
	public void onProgressChanged(SeekBar sb, int progress, boolean b) {
		//notifier.setText(Integer.toString(sb.getProgress()) + " rounds per minute");
		//notifier.show();
		rpmValue.setText(Integer.toString(progress) + " RPM");
	}
    
	/** implements OnClickListener */
	@Override
	public void onClick(View button) {
		if(!start.getText().toString().equals("STOP"))	{
			isUpdateNeeded = true;
			
			// Starting the Recorder
			bufferSize = AudioRecord.getMinBufferSize(
					44100,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			notifier.setText(String.valueOf(bufferSize));
			notifier.show();
			recorder = new AudioRecord(
					MediaRecorder.AudioSource.MIC,
					44100,
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					bufferSize);
			recorder.startRecording();
			
			isRecording = true;
			
			try {
				// Second parameter "true" for appending mode
				fw1 = new FileWriter(new File("/sdcard/50802566/java_in.txt"), true);
				fw2 = new FileWriter(new File("/sdcard/50802566/java_out.txt"), true);
				if (!new File("/sdcard/50802566").exists())	{
					new File("/sdcard/50802566").mkdirs();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Starting the Timer
			timer = new Timer();
			timer.schedule(new UpdateTimeTask(), 0, 20);
			
			// Some UI updating stuffs
			rpm.setEnabled(false);
			start.setText("STOP");
			start.setTextColor(Color.RED);
			
			// Testing stuffs
			//notifier.setText("Starting...");
			//notifier.show();
		}
		
		else	{
			isRecording = false;
			recorder.stop();
			// After releasing a "null"-set is a must
			recorder.release();
			recorder = null;
			data = null;
			
			isUpdateNeeded = false;
			// It cancel only the SCHEDULED tasks, not the RUNNING ones!
			// So we must use "isUpdateNeeded" flag to prevent its stupid behavior =.=
			timer.cancel();
			timer.purge();
			
			// Saving log if "auto_save_log" is set
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	        boolean auto_save = settings.getBoolean("auto_save_log", true);
			
			if (auto_save)	{
				// Filenames cannot contain "/"
				LogWriter lw = new LogWriter();
				String fDate = new SimpleDateFormat("dd_MM_yyyy - HH_mm_ss").format(new Date());
				lw.writeExternal(this, fDate, rpmCal.getText().toString());
				
				notifier.setText("Saved " + rpmCal.getText().toString());
				notifier.show();
			}
			
			else	{
				notifier.setText("Auto save disabled");
				notifier.show();
			}
			
			start.setText("MEASURE HEAD SPEED");
			start.setTextColor(Color.BLACK);
			//start.setBackgroundColor(Color.WHITE);
			rpm.setEnabled(true);
		}
	}
	
	/** Implements soft-key handler */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	notifier.cancel();
	    	
	    	// Exit this Activity
	    	Intent intent = new Intent(Intent.ACTION_MAIN);
	    	intent.addCategory(Intent.CATEGORY_HOME);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(intent);
	    	
	    	this.finish();
	    	
	    	return true;
	    }
	    
	    return false;
	}
	
	class UpdateTimeTask extends TimerTask {
		public void run() {
			/** Testing real-time run */
	        DemoUIActivity.this.runOnUiThread(new Runnable() {
	        	@Override
	        	public void run() {
	        		int currentValue = randommer.nextInt(20) - 10 + rpm.getProgress();
	        		
	        		if (isUpdateNeeded)	{
	        			rpmCal.setText(stringFromJNI() + currentValue + " RPM");
	        			
	        			/*if (null != data)	{
	        				rpmCal.setText(stringFromJNI() + String.valueOf(data[0]) + " RPM");
	        			}*/
	        		}
	        	}
	        });
	        
	        // TODO Check if it work well and not miss any audio-data.
	        if (isRecording)	{
	        	if(AudioRecord.ERROR_INVALID_OPERATION != read && null != data)	{
    				byte[] byte_reverse = processedByteArray(data, 1, data.length);
    				try {
    					char[] num = new char[data.length];
    					char[] num_reverse = new char[byte_reverse.length];
    					
    					for (int i = 0; i < data.length; i ++)	{
    						num[i] = (char)data[i];
    						num_reverse[i] = (char)byte_reverse[i];
    					}
    					
    					fw1.write(num);
    					fw2.write(num_reverse);
    					
    					fw1.close();
    	        	    fw2.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    				
    				Log.e("RECORD", "Flushed " + read + " bytes to C");
    			}
    			
    			else	{
    				Log.e("RECORD", "ERROR_INVALID_OPERATION");
    			}
	        	
	    		data = new byte[bufferSize];
	        	read = recorder.read(data, 0, bufferSize);
	        	
	        	Log.e("RECORD", "Read " + read + " bytes from the device recorder");
	        }
		}
	}
	
	
	/** JNI methods */
	// Return the maximum absolute 16 bit value in an array
	private native int Tachometer_MaxAbsolute16C(short[] vector);
	
	static {
		try	{
			System.loadLibrary("hello-jni");
		}
		
		catch (UnsatisfiedLinkError e)	{
			Log.e("ULE", e.getMessage());
		}
    }
}