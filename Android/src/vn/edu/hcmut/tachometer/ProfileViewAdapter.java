package vn.edu.hcmut.tachometer;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class ProfileViewAdapter extends ArrayAdapter<Profile>	{
	
	public int currentPos = -1;
	public String currentName = null;
	
	private int layoutResourceId;
	private List<Profile> profileList = null;
	
	Context context;
	
	public ProfileViewAdapter(Context context, int layoutResourceId, List<Profile> data) {
		super(context, R.layout.profile_view, data);
		
		this.layoutResourceId = layoutResourceId;
        this.profileList = data;
        this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == profileList.get(position))	{
			android.util.Log.e("getView", "Loglist null at " + position);
			return null;
		}
		
		ProfileHolder profileholder = null;
		
        // Unidentified view			
		if(convertView == null)	{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);
			
            // Create a new holder
			profileholder = new ProfileHolder();
			profileholder.name_view = (TextView) convertView.findViewById(R.id.name_view);
			profileholder.summary_view = (TextView) convertView.findViewById(R.id.summary_view);
			profileholder.img_view = (ImageView) convertView.findViewById(R.id.imgv_avatar);
			
			convertView.setTag(profileholder);
		}
        
		// Identified view
		else	{
			profileholder = (ProfileHolder)convertView.getTag();
		}
		
		profileholder.name_view.setText(profileList.get(position).name);
		String summary = 	profileList.get(position).numBlade + " blades, " +
							profileList.get(position).minRPM + " ~ " +
							profileList.get(position).maxRPM + " RPM";
		profileholder.summary_view.setText(summary);
		profileholder.img_view.setImageURI(Uri.fromFile(new File(profileList.get(position).avatar)));
		
		convertView.setBackgroundColor(Color.BLACK);
		
		if (position == currentPos)	{
			if (profileList.get(currentPos).name.equals(profileholder.name_view.getText().toString()))	{
				convertView.setBackgroundColor(Color.YELLOW);
			}
		}
		
		return convertView;
	}
	
	static class ProfileHolder	{
		ImageView img_view;
		TextView name_view;
		TextView summary_view;
	}
}
