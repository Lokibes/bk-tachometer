package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import vn.edu.hcmut.tachometer.core.JavaTachometer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DemoUIActivity extends Activity implements
		OnSeekBarChangeListener, OnClickListener, OnTouchListener {

	/*
	 * Defines some necessary constants
	 */
	private static final int TIME_INTERVAL = 40; // The time in milliseconds
	private static final int UI_UPDATE_INTERVAL = 500; // The time in
														// milliseconds

	/*
	 * With 16kHz and 16 bit, mono
	 */
	private static final int AUDIO_SAMPLING_FREQUENCY = 16000;
	private static final int AUDIO_BUFFER_MAX_SIZE = 6400; // In bytes
	private static final int AUDIO_BUFFER_SIZE = AUDIO_SAMPLING_FREQUENCY
			/ 1000 * TIME_INTERVAL * 2; // In bytes

	// The timer counter to update UI
	private int uiCounter = 0;

	// The lock to make UI thread and audio recording thread working well
	private final Lock lock = new ReentrantLock();

	// The current RPM
	private int currentRPM = 0;

	// The floating point array for drawing the spectrum
	private float fftOutArray[] = null;

	/*
	 * The audio buffer for reading the audio recorded from the microphone
	 */
	private ByteBuffer mAudioFrame = null;

	private String baseDir;

	private Toast notifier; // a pop-up for testing purpose
	private SeekBar rpm; // seek bar for estimating RPM
	private Button start; // start/stop measuring
	private TextView rpmCal; // real-time calculated value of actual RPM

	private ChartView chartView; // For rendering the wave-form of temperature
									// results
	private SeekView seekView; // For rendering the peak of
								// highest-energy-level wave

	// Testing purpose
	private Timer timer;
	private boolean isUpdateNeeded = true;
	private boolean isMeasuring = false;
	private boolean isSeeking = true;

	private AudioRecord recorder = null;
	int read = AudioRecord.ERROR_INVALID_OPERATION;
	private boolean isRecording = false;

	private JavaTachometer jTach;

	// For debug purpose
	private byte[] audioDataInBytes;
	private int seekPos;
	private int audioDataLengthInBytes;
	float maxFFT = -1.0f;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.relative);

		baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

		jTach = new JavaTachometer();

		// Initialize the audio byte buffer for recording audio
		mAudioFrame = jTach.jTachCreateAudioBuffer();

		// Create the audio recorder
		final int minBufferSize = AudioRecord.getMinBufferSize(16000,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		final int bufferSize = Math.max(minBufferSize, AUDIO_BUFFER_MAX_SIZE) * 2;
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				AUDIO_SAMPLING_FREQUENCY, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		chartView = (ChartView) findViewById(R.id.chartView);
		chartView.setClickable(false);

		seekView = (SeekView) findViewById(R.id.seekView);
		seekView.setClickable(false);

		/** IMPORTANT! constructor of notifier. Don't miss! */
		notifier = Toast.makeText(this, "", 5);

		rpmCal = (TextView) findViewById(R.id.rpmCal);

		start = (Button) findViewById(R.id.buttonStart);
		start.setOnClickListener(this);

		rpm = (SeekBar) findViewById(R.id.seekBar);
		rpm.setOnSeekBarChangeListener(this);
		rpm.setOnTouchListener(this);
		//rpm.setMax(450);

		new Random();
		isUpdateNeeded = false;

		if (CONFIGURES_FOR_DEBUGGING_PURPOSE.debugMode) {
			String audioFilePath = new String(getString(R.string._mnt_sdcard_)
					+ "rotation_16kHz.raw");
			File audioFile = new File(audioFilePath);
			audioDataLengthInBytes = (int) audioFile.length();
			audioDataInBytes = new byte[audioDataLengthInBytes];
			FileInputStream fis;
			try {
				fis = new FileInputStream(audioFile);
				fis.read(audioDataInBytes);
			} catch (FileNotFoundException e) {
				android.util.Log.e("FILE-MISSED", e.toString());
			} catch (IOException e) {
				android.util.Log.e("FILE-MISSED", e.toString());
			}
			
			seekPos = 0;
		} // End if (CONFIGURES_FOR_DEBUGGING_PURPOSE.debugMode)
		
		// This observe the views and tell us whether the view is fully-displayed on screen or not,
		// for us to avoid calling UI-method with unavailable parameters.
		ViewTreeObserver vto = ((ViewGroup)this.getWindow().getDecorView()).getViewTreeObserver();
	    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {
	            ///getLocationOnScreen here
	        	if (seekView.getVisibility() == View.VISIBLE)	{
	        		rpm.setMax(seekView.getWidth());
	        		
	        		// Initialize the Tachometer
	    			jTach.jTachInit();
	    			jTach.jTachConfig(rpm.getProgress());

	    			recorder.startRecording();
	    			isRecording = true;
	    			isUpdateNeeded = true;
	    			
	    			// Reset the UI counter
	    			uiCounter = 0;

	    			// Starting the Timer
	    			timer = new Timer();
	    			timer.schedule(new UpdateTimeTask(), 0, TIME_INTERVAL);
	    			
	    			// Mission of this observer should end here
	    			android.util.Log.e("Observer", "Set done!");
	        	}

	            ViewTreeObserver obs = ((ViewGroup)DemoUIActivity.this.getWindow().getDecorView()).getViewTreeObserver();
	            obs.removeGlobalOnLayoutListener(this);
	        }
	    });
	}

	/** call when come back from the settings screen. Refresh parameters */
	protected void onStart() {
		super.onStart();

		SharedPreferences test_curname = getApplicationContext()
				.getSharedPreferences("my_pref", Context.MODE_PRIVATE);
		android.util.Log.e("CURRENT PROF",
				test_curname.getString("current_name", "not found"));

		/** Adapt the seek bar and stuffs to the pre-defined settings */
		// TODO change to the global shared pref, for the sake of the whole app
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		int heli_type = Integer.valueOf(settings.getString("heli_type", "0"));

		switch (heli_type) {
		case 0:
			//rpm.setMax(1000);
			Toast.makeText(this, "Standard", 5).show();
			break;
		case 1:
			//rpm.setMax(300);
			Toast.makeText(this, "Microsized", 5).show();
			break;
		default:
			Toast.makeText(this,
					"You couldn't see this cuz default = Standard Helicopter",
					5).show();
			break;
		}

		// notifier.setText("Just coming back...");
		// notifier.show();
	}

	/* implements for the MENU soft-key. Android 2.3.x */
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

			// notifier.setText("Enter settings");
			// notifier.show();
			return true;
		case R.id.log:
			// Intent to_log = new Intent(this, LogActivity.class);
			// startActivity(to_log);

			Intent to_test_file = new Intent(this, LoggingActivity.class);
			startActivity(to_test_file);

			// notifier.setText("Enter logs");
			// notifier.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/** implements OnTouchListener */
	@Override
	public boolean onTouch(View view, MotionEvent me) {
		// TODO Auto-generated method stub
		notifier.setText("Estimated frequency: "
				+ Float.toString(fftOutArray[((SeekBar)view).getProgress()]) + " Hz");
		
		notifier.show();
		return false;
	}
	
	/** implements OnSeekBarChangeListener */
	public void onStartTrackingTouch(SeekBar sb) {

	}

	public void onStopTrackingTouch(SeekBar sb) {
		isSeeking = false;
	}

	public void onProgressChanged(SeekBar sb, int progress, boolean b) {
		/*notifier.setText("Estimated head speed: "
				+ Integer.toString(sb.getProgress()) + " RPM");
		notifier.show();*/
	}

	/** implements OnClickListener */
	@Override
	public void onClick(View button) {
		if (!start.getText().toString().equals("STOP")) {
			isUpdateNeeded = true;

			// Starting the Recorder
			// TODO: check wether we should show the notifier
			//notifier.setText(String.valueOf(AUDIO_BUFFER_MAX_SIZE));
			//notifier.show();

			// Initialize the Tachometer
			jTach.jTachInit();
			jTach.jTachConfig(rpm.getProgress());

			recorder.startRecording();
			isRecording = true;

			// Reset the currentRPM
			currentRPM = 0;
			rpmCal.setText(currentRPM + " RPM");

			// Reset the UI counter
			uiCounter = 0;

			// Some UI updating stuffs
			rpm.setEnabled(false);
			start.setText("STOP");
			start.setTextColor(Color.RED);

			// Starting the Timer
			timer = new Timer();
			timer.schedule(new UpdateTimeTask(), 0, TIME_INTERVAL);
		} else {
			//isRecording = false;
			//recorder.stop();
			
			// It cancel only the SCHEDULED tasks, not the RUNNING ones!
			// So we must use "isUpdateNeeded" flag to prevent its stupid behavior =.=
			//isUpdateNeeded = false;
			
			timer.cancel();
			timer.purge();

			// Saving log if "auto_save_log" is set
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			boolean auto_save = settings.getBoolean("auto_save_log", true);

			if (auto_save) {
				// Filenames cannot contain "/"
				LogWriter lw = new LogWriter();
				String prof = "Microsized Helicopter";
				String fDate = new SimpleDateFormat("dd_MM_yyyy - HH_mm_ss")
						.format(new Date());

				SharedPreferences global = getApplicationContext()
						.getSharedPreferences("my_pref", Context.MODE_PRIVATE);
				File path = new File(baseDir + File.separator
						+ "50802566/profiles");
				if (!path.exists()) {
					path.mkdirs();
				}
				File[] list_file = path.listFiles();

				if (list_file.length != 0) {
					for (int i = 0; i < list_file.length; i++) {
						if (list_file[i].getName().startsWith(
								global.getString("current_name", "-not found-"))) {
							prof = list_file[i].getAbsolutePath();
							break;
						}

						else if (i == list_file.length) {
							prof = "Default "
									+ global.getString("current_name",
											"-not found-");
							android.util.Log.e(
									"SAVING LOG",
									list_file[i].getName()
											+ " is not started by "
											+ global.getString("current_name",
													"-not found-"));
							break;
						}
					}
				}

				lw.writeExternal(this, prof, fDate, rpmCal.getText().toString());

				notifier.setText("Saved " + rpmCal.getText().toString());
				notifier.show();
			}

			else {
				notifier.setText("Auto save disabled");
				notifier.show();
			}

			start.setText("MEASURE");
			start.setTextColor(Color.BLACK);
			
			rpm.setEnabled(true);
			isSeeking = true;
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

			isRecording = false;
			
			if (null != recorder) {
				recorder.release();
				recorder = null;
			}
			
			if (null != timer) {
				timer.cancel();
				timer.purge();
				timer = null;
			}

			jTach.jTachFree(mAudioFrame);
			this.finish();

			return true;
		}

		return false;
	}

	class UpdateTimeTask extends TimerTask {
		public void run() {
//			android.os.Process
//					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			
			/*if (!(DemoUIActivity.this.getWindow().getDecorView().findViewById(android.R.id.content).getVisibility() == View.VISIBLE) &&
				!(DemoUIActivity.this.getWindow().getDecorView().findViewById(android.R.id.content).getVisibility() == View.INVISIBLE))	{
				android.util.Log.e("RUN", "View is not shown yet");
				return;
			}
			
			else	{
				android.util.Log.e("RUN", "View is shown. Will now process...");
			}*/
			
			/** Testing real-time run */
			DemoUIActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isUpdateNeeded) {
						uiCounter += TIME_INTERVAL;
						if (uiCounter > UI_UPDATE_INTERVAL) {
							uiCounter = uiCounter % TIME_INTERVAL;
							
							lock.lock();
							
							try {
								rpmCal.setText(currentRPM + " RPM");
							} finally {
								lock.unlock();
							}
						}
					}
				}
			});

			if (isRecording) {
				if (isMeasuring)	{
					if (CONFIGURES_FOR_DEBUGGING_PURPOSE.debugMode == false) {
						int nRead = recorder.read(mAudioFrame, AUDIO_BUFFER_SIZE);
						jTach.jTachPush(mAudioFrame, nRead);
						int processResult = (int) jTach.jTachProcess();
						lock.lock();
						try {
							currentRPM = processResult;
						} finally {
							lock.unlock();
						}
						
					}
					
					// For debug mode
					else {
						mAudioFrame.position(0);
						mAudioFrame.put(audioDataInBytes, seekPos,
								AUDIO_BUFFER_SIZE);
						seekPos += AUDIO_BUFFER_SIZE;
						if (seekPos >= audioDataLengthInBytes - AUDIO_BUFFER_SIZE) {
							seekPos = 0;
						}
						
						jTach.jTachPush(mAudioFrame, AUDIO_BUFFER_SIZE);
						int processResult = (int) jTach.jTachProcess();
						
						lock.lock();
						
						try {
							currentRPM = processResult;
						} finally {
							lock.unlock();
						}
					} // End if (CONFIGURES_FOR_DEBUGGING_PURPOSE.debugMode == false)
				}

				else
				if (isSeeking)	{
					int nRead = recorder.read(mAudioFrame, AUDIO_BUFFER_SIZE);
					jTach.jTachPush(mAudioFrame, nRead);
					
					// TODO: Why must I process to get things that I need for the processing?
					jTach.jTachProcess();
					
					if (uiCounter % 200 == 0) {
						int width = seekView.getWidth();
						int height = seekView.getHeight();
	
						if (fftOutArray == null) {
							// Create new array
							fftOutArray = new float[width];
						} else if (fftOutArray.length != width) {
							// Initialize the array again
							fftOutArray = new float[width];
						}
						
						android.util.Log.e("PRE-FFT", "" + width + " " + height);
						maxFFT = jTach.jTachFFTOut(0, rpm.getMax() * 2, width, fftOutArray);
	
						if (maxFFT >= 0) { // No error
							// Draw the spectrum here
							for (int x = 0; x < width; x++) {
								seekView.drawLine(x, (fftOutArray[x] / maxFFT) * height);
								//android.util.Log.e("FFTOUT", "" + fftOutArray[x]);
							}
							
							seekView.requestRender();
						}
					}
				}
			} // End if (isRecording)
		}
	}
}