package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileModifierActivity extends Activity implements OnClickListener {
	
	private String fileToDelete = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_form);
        
        ((Button) findViewById(R.id.btn_saveprof)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_cancel)).setOnClickListener(this);
        
        Bundle extras = getIntent().getExtras();
		if(null != extras) {
		    CharSequence value = extras.getCharSequence("profile_to_load");
		    android.util.Log.e("PROF_MOD", value.toString());
		    
		    try {
		    	ProfileReader pr = new ProfileReader();
				Profile toEdit = pr.read(new FileInputStream(value.toString()));
				
				fileToDelete = new File(value.toString()).getAbsolutePath(); 
				
				//android.util.Log.e("Load success", toEdit.name + " " + toEdit.numBlade + " " + toEdit.minRPM + " " + toEdit.maxRPM + " " + toEdit.avatar);
				
				((EditText) findViewById(R.id.tv_profname)).setText(toEdit.name);
				((EditText) findViewById(R.id.tv_blade)).setText(String.valueOf(toEdit.numBlade));
				((EditText) findViewById(R.id.tv_minrpm)).setText(String.valueOf(toEdit.minRPM));
				((EditText) findViewById(R.id.tv_maxrpm)).setText(String.valueOf(toEdit.maxRPM));
				
				// According to the case, set relative label of the SAVE button. Default is in @string/
				((Button) findViewById(R.id.btn_saveprof)).setText("SAVE CHANGES");
			} catch (FileNotFoundException e) {
				android.util.Log.e("PROF_MOD", e.toString());
			} catch (XmlPullParserException e) {
				android.util.Log.e("PROF_MOD", e.toString());
			} catch (IOException e) {
				android.util.Log.e("PROF_MOD", e.toString());
			}
		}
		
		else	{
			// According to the case, set relative label of the SAVE button. Default is in @string/
			((Button) findViewById(R.id.btn_saveprof)).setText("SAVE THIS NEW PROFILE");
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId())	{
			case R.id.btn_saveprof:
				String date = new SimpleDateFormat("dd_MM_yyyy - HH_mm_ss").format(new Date());
				String model = ((EditText) findViewById(R.id.tv_profilemodel)).getText().toString().trim();
				String name = ((EditText) findViewById(R.id.tv_profname)).getText().toString().trim();
				
				if (null != model && !"".equals(model))	{
					name = name  + " - " + model;
				}
				
				String filename = name + " " + date + ".prof";
				
				String avatar = "default link to the empty profile avatar";
				int minRPM = Integer.parseInt(((EditText) findViewById(R.id.tv_minrpm)).getText().toString());
				int maxRPM = Integer.parseInt(((EditText) findViewById(R.id.tv_maxrpm)).getText().toString());
				int numBlade = Integer.parseInt(((EditText) findViewById(R.id.tv_blade)).getText().toString());
				
				// TODO check the logical problem of the inputs
				if (numBlade <= 1)	{
					Toast.makeText(this, "Invalidate blades number", 5).show();
					return;
				}
				
				if (minRPM < 0 || maxRPM < 0)	{
					Toast.makeText(this, "Invalidate RPM", 5).show();
					return;
				}
				
				if (maxRPM < minRPM)	{
					Toast.makeText(this, "Invlidate Maximum RPM", 5).show();
					return;
				}
				
				// Write the real file
				ProfileWriter pw = new ProfileWriter();
				pw.writeExternal(this, filename, name, avatar, minRPM, maxRPM, numBlade);
				
				// Pass temp Profile back to ProfileActivity
				Intent i = new Intent(this, ProfileActivity.class);
				
		    	i.putExtra("name", name);
		    	i.putExtra("avatar", avatar);
		    	i.putExtra("minRPM", minRPM);
		    	i.putExtra("maxRPM", maxRPM);
		    	i.putExtra("numBlade", numBlade);
		    	i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    	
				if (null != getIntent().getExtras())	{
					if (null != fileToDelete){
						SharedPreferences settings = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						// If editing the being-used file, update the name
						if (getIntent().getExtras().getCharSequence("profile_name").equals(settings.getString("current_name", "")))	{
							android.util.Log.e("btn_saveprof", "Updated. Current name = " + name);
							
							editor.putString("current_name", name);
							editor.commit();
						}
						
						File toDel = new File(fileToDelete);
						if (!toDel.delete())	{
	            			android.util.Log.e("DEL", "File " + fileToDelete + ": not deleted or not existed");
	            		}
						
						fileToDelete = null;
					}
				}
				
				android.util.Log.e("onClick", name + " " + avatar + " " + minRPM + " " + maxRPM + " " + numBlade);
				
		    	startActivity(i);
		    	
		    	//Set the transition -> method available from Android 2.0 and beyond  
		    	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		    	
		    	break;
			
			case R.id.btn_cancel:
				Intent j = new Intent(this, ProfileActivity.class);
		    	j.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    	
		    	startActivity(j);
		    	
		    	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);  

		    	break;
			default:
				break;
		}
		
		this.finish();
	}
	
	/** Implements soft-key handler */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	Intent i = new Intent(this, ProfileActivity.class);
	    	i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    	
	    	startActivity(i);
	    	
	    	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);  
	    	
	    	this.finish();
	    	
	    	return true;
	    }
	    
	    return false;
	}
}
