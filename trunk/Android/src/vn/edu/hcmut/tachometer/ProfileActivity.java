package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ProfileActivity extends Activity implements OnClickListener	{
	
	private ListView listView;
	private ProfileViewAdapter pw_adapter;
	private List<Profile> profileList;
	
	AlertDialog.Builder alertDialogBuilder;
	private int attempPos;
	
	@Override
	/** This method is an Android bug ? >"< */
	public void onNewIntent(Intent intent) {
		android.util.Log.e("onNewIntent", "Nothing, just kidding :D");
	    super.onNewIntent(intent);
	    setIntent(intent);
	}
	
	protected void onStart ()	{
		android.util.Log.e("onStart", "ProfAct started");
    	super.onStart();
    	
    	/*pw_adapter.currentPos = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE).getInt("current_pos", -1);
		
        if (pw_adapter.currentPos != -1)	{
        	pw_adapter.notifyDataSetChanged();
        }*/

		Bundle extras = getIntent().getExtras();
		if(null != extras) {
			android.util.Log.e("onStart", extras.toString());
		    String name = extras.getString("name");
		    String avatar = extras.getString("avatar");
		    int minRPM = extras.getInt("minRPM");
		    int maxRPM = extras.getInt("maxRPM");
		    int numBlade = extras.getInt("numBlade");
		    
		    // TODO Check duplicate item before adding
		    Profile p = new Profile(name, avatar, minRPM, maxRPM, numBlade);
		}
		
		pw_adapter.currentPos = -1;
		String currentName = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE).getString("current_name", null);
		
		if (null == currentName)	{
			pw_adapter.currentPos = -1;
			pw_adapter.notifyDataSetChanged();
			return;
		}
		
	    for (int i = 0; i < profileList.size(); i ++)	{
	    	android.util.Log.e("onStart", currentName + " :VS: " + profileList.get(i).name + " at " + i);
	    	
	    	if (currentName.equals(profileList.get(i).name))	{
	    		pw_adapter.currentPos = i;
	    		android.util.Log.e("onStart", "Re-find the being-used: " + profileList.get(i).name + " at " + i); 
	    		break;
	    	}
	    }
		
	    pw_adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDestroy() {
	    android.util.Log.e("onDestroy", "onDestroy()");
	    super.onDestroy();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		android.util.Log.e("onCreate", "ProfAct created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_list);
        
        attempPos = -1;
		profileList = loadProfile();
		listView = (ListView) findViewById(R.id.profile_list);
		pw_adapter = new ProfileViewAdapter(this, R.layout.profile_view, profileList);
        
		listView.setAdapter(pw_adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				attempPos = position;
				
				alertDialogBuilder.create().show();
			}
		});
        
		alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
			.setTitle("Profile menu")
			.setCancelable(true)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener()	{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			})
			.setItems(R.array.profile_menu, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   String[] menu_items = getResources().getStringArray(R.array.profile_menu);
	            	   SharedPreferences settings = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE);
                       SharedPreferences.Editor editor = settings.edit();
	            	   
                       /** Access folder "my_profiles" in external memory */
                	   String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                	   File path = new File(baseDir + File.separator + "50802566/profiles");
                	   if (!path.exists())	{	path.mkdirs();	}
                	   File[] list_file = path.listFiles();
                       
	                   switch (which)	{
		                   case 0:
		                	   android.util.Log.e("P-MENU", menu_items[which] + ": " + attempPos);
		                	   pw_adapter.currentPos = attempPos;
		                	   pw_adapter.notifyDataSetChanged();
		                	   
		                       editor.putInt("current_pos", attempPos);
		                       editor.putString("current_name", profileList.get(pw_adapter.currentPos).name);
		                       editor.putInt("min_rpm", profileList.get(attempPos).minRPM);
		                       editor.putInt("max_rpm", profileList.get(attempPos).maxRPM);
		                       editor.putInt("blade_num", profileList.get(attempPos).numBlade);
		                       editor.putString("helicopter_type", profileList.get(attempPos).name);
		                       
		                	   break;
	                	   
		                   case 1:
		                	   android.util.Log.e("P-MENU", menu_items[which] + ": " + attempPos);
		                	   
		                	   Intent prof_view = new Intent(ProfileActivity.this, ProfileModifierActivity.class);
		                	   
		                	   for (int i = 0; i < list_file.length; i ++)	{
		                		   android.util.Log.e("onCreate", list_file[i].getName());
		                		   if (list_file[i].getName().startsWith(profileList.get(attempPos).name + " "))	{
		                			   prof_view.putExtra("profile_to_load", list_file[i].getAbsolutePath());
		                			   
		                			   /*if (-1 != pw_adapter.currentPos)	{
		                				   // Edit the unused row
		                				   if (attempPos != pw_adapter.currentPos)	{
		                					   editor.putString("current_name", profileList.get(pw_adapter.currentPos).name);
		                				   }
		                				   
		                				   // Edit the being-used row
		                				   else	{
		                					   android.util.Log.e("onCreate", "Edit the one using?");
		                					   editor.putString("current_name", null);
		                					   continue;
		                				   }
		                			   }*/
		                			   
		                			   break;
		                		   }
		                	   }
		                	   
		                	   prof_view.putExtra("profile_name", profileList.get(attempPos).name);
		                	   
		                	   startActivity(prof_view);
		                	   
		                	   ProfileActivity.this.finish();
		                	   
		                	   break;
	                	   
		                   case 2:
		                	   android.util.Log.e("P-MENU", menu_items[which] + ": " + attempPos);
		                	   
		                	   for (File f: list_file)	{
		                		   if (f.getName().startsWith(profileList.get(attempPos).name))	{
		                			   if (!f.delete())	{
		                				   android.util.Log.e("DEL", "File " + f.getAbsolutePath() + ": not deleted or not existed");
		                			   }
		                		   }
		                	   }
		                	   
		                	   pw_adapter.remove(profileList.get(attempPos));
		                	   pw_adapter.notifyDataSetChanged();
		                	   
		                	   break;
	                   }
	                   
                       editor.commit();
	                   
                       android.util.Log.e("DialogInterface.onClick", "Current pos = " + getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE).getInt("current_pos", -1));
                       
	                   dialog.dismiss();
	               }
       		});
		
        ((Button) findViewById(R.id.btn_addprofile)).setOnClickListener(this);
    }
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_addprofile)	{
			Intent prof_view = new Intent(this, ProfileModifierActivity.class);
			//prof_view.putExtra("profile_to_load", "Yeah I got ya message!");
			
	        startActivity(prof_view);
	        
	        this.finish();
		}
		
		/*In your LocationActivity class:

			Intent i = new Intent(this, FindAndroidActivity.class);
			i.putExtra("KEY",YourData);
			
		In FindAndroidActivity class

			Bundle extras = getIntent().getExtras();
			if(extras !=null) {
			    String value = extras.getString("KEY");
			}*/
	}
	
	/** Unregister the unused listener when back to main screen */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	android.util.Log.e("GOING_BACK", "Current pos = " + getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE).getInt("current_pos", -1));
	    	
	    	SharedPreferences settings = getApplicationContext().getSharedPreferences("my_pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
	    	
            if (pw_adapter.currentPos != -1)	{
	            editor.putString("current_name", profileList.get(pw_adapter.currentPos).name);
	            editor.putInt("min_rpm", profileList.get(pw_adapter.currentPos).minRPM);
	            editor.putInt("max_rpm", profileList.get(pw_adapter.currentPos).maxRPM);
	            editor.putInt("blade_num", profileList.get(pw_adapter.currentPos).numBlade);
	            editor.putString("helicopter_type", profileList.get(pw_adapter.currentPos).name);
	            
            }	else	{
            	editor.putInt("current_pos", -1);
            	editor.putString("current_name", null);
            }
            
            editor.commit();
            
	    	this.finish();
	    	
	    	return true;
	    }
	    
	    return false;
	}	
	
	private ArrayList<Profile> loadProfile()	{
		/** Access folder "my_profiles" in external memory */
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		File path = new File(baseDir + File.separator + "50802566/profiles");
		if (!path.exists())	{	path.mkdirs();	}
	    File[] list_file = path.listFiles();
	    
	    /** For internal memory */
	    //File[] list_file = this.getFilesDir().listFiles();
	    
	    if (list_file.length == 0)	{
	    	String files[] = {
	    			"micro_helicopter.prof",
	    			"standard_helicopter.prof"	};
	    	InputStream in = null;
	    	OutputStream out = null;
	    	
			try {
				for (String filename : files)	{
					in = getAssets().open(filename);
					out = new FileOutputStream(new File(path.getAbsolutePath() + File.separator + filename));
					
					// Copy the bits from in-stream to out-stream
					byte[] buf = new byte[1024];
			        int len;
			        while ((len = in.read(buf)) > 0) {
			            out.write(buf, 0, len);
			        }
				}
				
		        in.close();
		        out.close();
		        
			} catch (FileNotFoundException e) {
				android.util.Log.e("LOAD PROF", e.getMessage());
			} catch (IOException e)	{
				android.util.Log.e("LOAD PROF", e.getMessage());
			}
			
			// Reload the file list
			list_file = path.listFiles();
	    }
	    
		for (int i = 0; i < list_file.length; i ++)	{
			//android.util.Log.e("PRINTLN", list_file[i].getPath());
		}
		
		int index = list_file.length;
		
		ArrayList<Profile> list = new ArrayList<Profile>();
		
		while (index-- > 0) {
			//File file = list_file[l];
			FileInputStream fin = null;
			ProfileReader profr = new ProfileReader();
			
			try {
				fin = new FileInputStream(list_file[index]);
				list.add(list_file.length - index - 1, profr.read(fin));
				fin.close();
			} catch (FileNotFoundException e) {
				android.util.Log.e("LOAD PROF", e.getMessage());
			} catch (XmlPullParserException e) {
				android.util.Log.e("LOAD PROF", e.getMessage());
			} catch (IOException e) {
				android.util.Log.e("LOAD PROF", e.getMessage());
			}
		}
		
		return list;
	}
}