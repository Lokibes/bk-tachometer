package vn.edu.hcmut.tachometer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class LogViewAdapter extends ArrayAdapter<Log>	{
	
	public boolean deleteMode;
	public ArrayList<Boolean> checkList;
	
	private int layoutResourceId;
	private List<Log> logList;
	
	Context context;
	
	public LogViewAdapter(Context context, int layoutResourceId, List<Log> data) {
		super(context, R.layout.log_view, data);
		
		this.layoutResourceId = layoutResourceId;
        this.logList = data;
        
        checkList = new ArrayList<Boolean>();
        for (int i = 0; i < logList.size(); i ++)	{
        	checkList.add(i, false);
        }
        
        this.context = context;
        deleteMode = false;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (null == logList.get(position))	{
			android.util.Log.e("getView", "Loglist null at " + position);
			return null;
		}
		
		LogHolder logholder = null;
		
        // Unidentified view			
		if(convertView == null)	{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);
			
            // Create a new holder
			logholder = new LogHolder();
			
			logholder.del_box = (CheckBox) convertView.findViewById(R.id.del_box);
			logholder.profile_view = (TextView) convertView.findViewById(R.id.profile_view);
			logholder.date_view = (TextView) convertView.findViewById(R.id.date_view);
			logholder.value_view = (TextView) convertView.findViewById(R.id.value_view);
			logholder.avatar_view = (ImageView) convertView.findViewById(R.id.avatar_view);
			
			//logholder.avatar_view = (ImageView) convertView.findViewById(R.id.imgv_avatar);
			
			convertView.setTag(logholder);
		}
        
		// Identified view
		else	{
			logholder = (LogHolder)convertView.getTag();
		}
		
		if (null != logList && !logList.isEmpty())	{
			if (null == checkList.get(position))	{
				android.util.Log.e("getView", "Checklist null at " + position);
			}
			
			//logholder.del_box = (CheckBox)convertView.findViewById(R.id.del_box);
			logholder.date_view.setText(logList.get(position).date);
			logholder.value_view.setText(logList.get(position).value);
			
			// DONE obtain the profile name, not the file path!
			if (!logList.get(position).profile.equals("Unknown profile"))	{
				try	{
					if (logList.get(position).profile.contains(File.separator))	{
						logholder.profile_view.setText(logList.get(position).profile.substring(logList.get(position).profile.lastIndexOf("/") + 1, logList.get(position).profile.length() - "dd_MM_yyyy - HH_mm_ss.prof".length()));
					}
					
					else	{
						logholder.profile_view.setText(logList.get(position).profile);
					}
				}	catch(StringIndexOutOfBoundsException e)	{
					android.util.Log.e("StringIndexOutOfBoundsException", "Profile = " + logList.get(position).profile);
					android.util.Log.e("StringIndexOutOfBoundsException", "Index start = " + logList.get(position).profile.lastIndexOf("/") + 1);
					android.util.Log.e("StringIndexOutOfBoundsException", "Index end = " + (logList.get(position).profile.length() - "dd_MM_yyyy - HH_mm_ss.prof".length()));
				}
				logholder.avatar_view.setImageBitmap(BitmapUtil.decodeSampledBitmapFromResource(logList.get(position).profile.replace("profiles", "avatars").replace(".prof", ".png"), 55, 55));
				android.util.Log.e("DECODING", logList.get(position).profile.replace("profiles", "avatars").replace(".prof", ".png"));
			}
			
			else	{
				logholder.profile_view.setText("Unknown profile");
				logholder.avatar_view.setImageBitmap(null);
			}
			
			if (deleteMode)	{
				logholder.del_box.setVisibility(View.VISIBLE);
				logholder.del_box.setChecked(checkList.get(position));
			}	else	{
				logholder.del_box.setVisibility(View.INVISIBLE);
			}
		}
		
		//android.util.Log.e("getView", " at " + position + ", value = " + logholder.value_view.getText().toString());
		return convertView;
	}
	
	static class LogHolder	{
		ImageView avatar_view;
		TextView profile_view;
		TextView date_view;
	    TextView value_view;
	    CheckBox del_box;
	}
}
