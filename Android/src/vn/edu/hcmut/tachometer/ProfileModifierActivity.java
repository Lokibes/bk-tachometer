package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ProfileModifierActivity extends Activity implements OnClickListener {
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	
	private String fileToDelete = null;
	private String tempAvaToDelete = null;
	private Uri mImageCaptureUri;
	private String avaPath = null;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;
	   
	    switch (requestCode) {
		    case PICK_FROM_CAMERA:
		    	doCrop();
		    	
		    	break;
		    case PICK_FROM_FILE: 
		    	mImageCaptureUri = data.getData();
		    	doCrop();
	    
		    	break;	    	
		    case CROP_FROM_CAMERA:	    	
		        Bundle extras = data.getExtras();
	
		        if (extras != null) {	        	
		            Bitmap photo = extras.getParcelable("data");
		            ((ImageView) findViewById(R.id.profile_picture)).setImageBitmap(photo);
		            
		            // TODO save the croped image to profiles folder,
			        // update relative avatar link in xml file
		            
			        //1.Create a file from a URI path as:
			        //File from = new File(mImageCaptureUri.getPath());
			        
			        //2.Create another File where you want the file to save as:
		    		FileOutputStream out;
			        File to = new File(avaPath + File.separator + "tmp_avatar.png");
	    			
					try {
						out = new FileOutputStream(to);
						photo.compress(Bitmap.CompressFormat.PNG, 90, out);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    			
			        //3.Rename the file as:
			        //from.renameTo(to);
			        // With this the file from default path
			        // is automatically deleted and created at the new path.
			        
			        // Just to ensure
			        //System.gc();
			        
		            android.util.Log.e("TMP-AVA", to.getAbsolutePath());
		        }
		                
		        break;
	    }
	}
    
    private void doCrop() {
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        
        int size = list.size();
        
        if (size == 0) {	        
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
        	
            return;
        }
        
        else {
        	intent.setData(mImageCaptureUri);
            
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            
        	if (size == 1) {
        		Intent i = new Intent(intent);
	        	ResolveInfo res	= list.get(0);
	        	
	        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
	        	
	        	startActivityForResult(i, CROP_FROM_CAMERA);
        	}
        	
        	else {
		        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Choose Crop App");
		        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
		            public void onClick( DialogInterface dialog, int item ) {
		                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
		            }
		        });
	        
		        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
		            @Override
		            public void onCancel( DialogInterface dialog ) {
		               
		                if (mImageCaptureUri != null ) {
		                    getContentResolver().delete(mImageCaptureUri, null, null );
		                    mImageCaptureUri = null;
		                }
		            }
		        } );
		        
		        AlertDialog alert = builder.create();
		        
		        alert.show();*/
        	}
        }
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_form);
        
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		File path = new File(baseDir + File.separator + "50802566/avatars");
		if (!path.exists())	{	path.mkdirs();	}
		avaPath = path.getAbsolutePath();
        
        final String[] items = new String [] {"Take from camera", "Select from gallery"};				
		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Select Profile image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) {
				if (item == 0) { //pick from camera
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					
					mImageCaptureUri = Uri.fromFile(new File(
							Environment.getExternalStorageDirectory(),
								"tmp_avatar_" +
								String.valueOf(System.currentTimeMillis()) +
								".jpg"));

					tempAvaToDelete = mImageCaptureUri.getPath();
					
					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

					try {
						intent.putExtra("return-data", true);
						
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				}
				
				else { //pick from file
					Intent intent = new Intent();
					
	                intent.setType("image/*");
	                intent.setAction(Intent.ACTION_GET_CONTENT);
	                
	                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
				}
			}
		});
		
		final AlertDialog dialog = builder.create();
		
		((ImageView) findViewById(R.id.profile_picture)).setOnLongClickListener(new View.OnLongClickListener() {	
			@Override
			public boolean onLongClick(View arg0) {
				dialog.show();
				return true;
			}
		});
		
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
				((ImageView) findViewById(R.id.profile_picture)).setImageBitmap(BitmapUtil.decodeSampledBitmapFromResource(toEdit.avatar, 200, 200));
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
				
				String avatar = "";
				
				if (fileToDelete != null)	{ // Modifying
					File from = new File(avaPath + File.separatorChar + "tmp_avatar.png");
					if (!from.exists())	{
			        	from = new File(avaPath + File.separatorChar + new File(fileToDelete).getName().replace(".prof", ".png"));
			        }
					
					File to = new File(avaPath + File.separatorChar + name + " " + date + ".png");
					avatar = to.getAbsolutePath();
		        	from.renameTo(to);
		        	
		        	android.util.Log.e("onSave", "Changed " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
				}
				
				else	{
					
				}
				
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
				
				android.util.Log.e("onSave", name + " " + avatar + " " + minRPM + " " + maxRPM + " " + numBlade);
				
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
		
		if (null != tempAvaToDelete)	{
			File x = new File(tempAvaToDelete);
			if (x.delete())	{
				android.util.Log.e("onCreating", x.getAbsolutePath() + " was deleted");
			}
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
