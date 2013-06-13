package vn.edu.hcmut.tachometer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	
	private SharedPreferences settings;
	//private SharedPreferences defaults;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);
        
        //defaults = PreferenceManager.getDefaultSharedPreferences(this);
        //defaults.registerOnSharedPreferenceChangeListener(this);
        
        //ListPreference blade = (ListPreference) findPreference("blade");
		//ListPreference heli_type = (ListPreference) findPreference("heli_type");
        
    	Preference profiles = (Preference) findPreference("profiles");
    	profiles.setOnPreferenceClickListener(this);
    	settings = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE);
    	
        if (settings.getInt("current_pos", -1) == -1)	{
        	android.util.Log.e("onCreate", "Current pos = " + settings.getInt("current_pos", -1));
//        	// Using DEFAULT for sure
//        	
//        	int heli_index, blade_index;
//        	String[] rpm_range = getResources().getStringArray(R.array.rpm_range);
//    		
//        	if (!settings.getString("heli_type", "-not found-").equals("-not found-"))	{
//        		heli_index = Integer.parseInt(settings.getString("heli_type", "-not found-"));
//        	}	else	{
//        		heli_index = 0;
//        	}
//        	
//        	//heli_type.setSummary(heli_type.getEntries()[heli_index].toString() + "\t" + rpm_range[heli_index]);
//    		
//        	if (!settings.getString("blade", "-not found-").equals("-not found-"))	{
//        		blade_index = Integer.parseInt(settings.getString("blade", "-not found-"));
//        	}	else	{
//        		blade_index = 0;
//        	}
//        	
//	        //blade.setSummary(blade.getEntries()[blade_index].toString());
//    		
//	        profiles.setTitle("Using \"" + cHeliType + "\"");
//            profiles.setSummary(cRPMRange + " RPM, " + cBlades + " blades");
            
            // New: this case is first-run due to new design
        	SharedPreferences.Editor editor = settings.edit();
        	
        	editor.putInt("current_pos", 0);
            editor.putString("current_name", "Microsized Helicopter");
            editor.putInt("min_rpm", 0);
            editor.putInt("max_rpm", 1000);
            editor.putInt("blade_num", 3);
            editor.putString("helicopter_type", "Microsized Helicopter");
            
            editor.commit();
        }
        
        int cBlades = settings.getInt("blade_num", -1);
    	String cHeliType = settings.getString("helicopter_type", "-not found-");
    	String cRPMRange = settings.getInt("min_rpm", -1) + " ~ " + settings.getInt("max_rpm", -1) + " RPM";
    	
        profiles.setTitle("Using \"" + cHeliType + "\"");
        profiles.setSummary(cRPMRange + ", " + cBlades + " Blades");
        
        /*else	{
        	// Using certain profile in profile list
        	int cBlades = settings.getInt("blade_num", -1);
        	String cHeliType = settings.getString("helicopter_type", "-not found-");
        	String cRPMRange = settings.getInt("min_rpm", -1) + " ~ " + settings.getInt("max_rpm", -1) + " RPM";
        	
    		//heli_type.setSummary(cHeliType + " (" + cRPMRange + ")");
    		//blade.setSummary(cBlades + " Blades");
    		
            profiles.setTitle("Using \"" + cHeliType + "\"");
            profiles.setSummary(cRPMRange + " RPM, " + cBlades + " blades");
            
            heli_type.setDefaultValue(null);
            blade.setDefaultValue(null);
            
            for (int i = 0; i < blade.getEntries().length; i ++)	{
            	if (blade.getEntries()[i].toString().equals(blade.getSummary().toString()))	{
            		blade.setValueIndex(i);
            	}
            }
        }*/
    }
    
	@Override
	protected void onStart ()	{
    	super.onStart();
		
    	android.util.Log.e("onStart", "Settings started");
    	
    	//defaults = PreferenceManager.getDefaultSharedPreferences(this);
        //defaults.registerOnSharedPreferenceChangeListener(this);
        
        //ListPreference blade = (ListPreference) findPreference("blade");
		//ListPreference heli_type = (ListPreference) findPreference("heli_type");
        
    	Preference profiles = (Preference) findPreference("profiles");
    	profiles.setOnPreferenceClickListener(this);
    	settings = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE);
    	
        if (settings.getInt("current_pos", -1) == -1)	{
        	android.util.Log.e("onCreate", "Current pos = " + settings.getInt("current_pos", -1));
//        	// Using DEFAULT for sure
//        	
//        	int heli_index, blade_index;
//        	String[] rpm_range = getResources().getStringArray(R.array.rpm_range);
//    		
//        	if (!settings.getString("heli_type", "-not found-").equals("-not found-"))	{
//        		heli_index = Integer.parseInt(settings.getString("heli_type", "-not found-"));
//        	}	else	{
//        		heli_index = 0;
//        	}
//        	
//        	//heli_type.setSummary(heli_type.getEntries()[heli_index].toString() + "\t" + rpm_range[heli_index]);
//    		
//        	if (!settings.getString("blade", "-not found-").equals("-not found-"))	{
//        		blade_index = Integer.parseInt(settings.getString("blade", "-not found-"));
//        	}	else	{
//        		blade_index = 0;
//        	}
//        	
//	        //blade.setSummary(blade.getEntries()[blade_index].toString());
//    		
//	        profiles.setTitle("Using \"" + cHeliType + "\"");
//            profiles.setSummary(cRPMRange + " RPM, " + cBlades + " blades");
            
            // New: this case is first-run due to new design
        	SharedPreferences.Editor editor = settings.edit();
        	
        	editor.putInt("current_pos", 0);
            editor.putString("current_name", "Microsized Helicopter");
            editor.putInt("min_rpm", 0);
            editor.putInt("max_rpm", 1000);
            editor.putInt("blade_num", 3);
            editor.putString("helicopter_type", "Microsized Helicopter");
            
            editor.commit();
        }
        
        int cBlades = settings.getInt("blade_num", -1);
    	String cHeliType = settings.getString("helicopter_type", "-not found-");
    	String cRPMRange = settings.getInt("min_rpm", -1) + " ~ " + settings.getInt("max_rpm", -1) + " RPM";
    	
        profiles.setTitle("Using \"" + cHeliType + "\"");
        profiles.setSummary(cRPMRange + ", " + cBlades + " Blades");
        
        /*else	{
        	// Using certain profile in profile list
        	int cBlades = settings.getInt("blade_num", -1);
        	String cHeliType = settings.getString("helicopter_type", "-not found-");
        	String cRPMRange = settings.getInt("min_rpm", -1) + " ~ " + settings.getInt("max_rpm", -1) + " RPM";
        	
    		//heli_type.setSummary(cHeliType + " (" + cRPMRange + ")");
    		//blade.setSummary(cBlades + " Blades");
    		
            profiles.setTitle("Using \"" + cHeliType + "\"");
            profiles.setSummary(cRPMRange + " RPM, " + cBlades + " blades");
            
            heli_type.setDefaultValue(null);
            blade.setDefaultValue(null);
            
            for (int i = 0; i < blade.getEntries().length; i ++)	{
            	if (blade.getEntries()[i].toString().equals(blade.getSummary().toString()))	{
            		blade.setValueIndex(i);
            	}
            }
        }*/
        
        if (profiles.getSummary().toString().startsWith("Default"))	{
        	//blade.setEnabled(true);
        }	else	{
        	//blade.setEnabled(false);
        }
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
//		android.util.Log.e("onSharedPreferenceChanged", "Key = " + key);
//		// Change to default profiles for sure
//		
//		int heli_index, blade_index;
//		String[] rpm_range = getResources().getStringArray(R.array.rpm_range);
//		ListPreference blade = (ListPreference) findPreference("blade");
//		ListPreference heli_type = (ListPreference) findPreference("heli_type");
//		
//		if (!defaults.getString("heli_type", "-not found-").equals("-not found-"))	{
//    		heli_index = Integer.parseInt(defaults.getString("heli_type", "-not found-"));
//    	}	else	{
//    		heli_index = 0;
//    	}
//    	
//    	heli_type.setSummary(heli_type.getEntries()[heli_index].toString() + "\t" + rpm_range[heli_index]);
//		
//    	if (!defaults.getString("blade", "-not found-").equals("-not found-"))	{
//    		blade_index = Integer.parseInt(defaults.getString("blade", "-not found-"));
//    	}	else	{
//    		blade_index = 0;
//    	}
//    	
//        blade.setSummary(blade.getEntries()[blade_index].toString());
//		
//        Preference profiles = (Preference) findPreference("profiles");
//        profiles.setSummary("Default \"" + heli_type.getEntries()[heli_index].toString() + "\"");
//		
//		// Turn the global settings off
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("current_name", null);
//        
//        editor.commit();
//        
//        if (profiles.getSummary().toString().startsWith("Default"))	{
//        	blade.setEnabled(true);
//        }	else	{
//        	blade.setEnabled(false);
//        }
	}
	
	/** Unregister the unused listener when back to main screen */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	settings.unregisterOnSharedPreferenceChangeListener(this);
	    	//Toast.makeText(this, "Unregistered successfully", 10).show();
	    	
	    	this.finish();
	    	
	    	return true;
	    }
	    
	    return false;
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		//android.util.Log.e("CLICK", "AEWERCWETCWR");
    	Intent intent = new Intent(this, ProfileActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivity(intent);
    	
		return false;
	}
	
    // String
    /*public static String Read(Context context, final String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, "");
    }
    
    public static void Write(Context context, final String key, final String value) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
          SharedPreferences.Editor editor = settings.edit();
          editor.putString(key, value);
          editor.commit();        
    }
    
    // Boolean  
    public static boolean ReadBoolean(Context context, final String key, final boolean defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(key, defaultValue);
    }
 
    public static void WriteBoolean(Context context, final String key, final boolean value) {
          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
          SharedPreferences.Editor editor = settings.edit();
          editor.putBoolean(key, value);
          editor.commit();        
    }*/
}