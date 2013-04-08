package vn.edu.hcmut.tachometer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
			
			logholder.del_box = (CheckBox)convertView.findViewById(R.id.del_box);
			logholder.date_view = (TextView)convertView.findViewById(R.id.date_view);
			logholder.value_view = (TextView)convertView.findViewById(R.id.value_view);
			
			convertView.setTag(logholder);
		}
        
		// Identified view
		else	{
			logholder = (LogHolder)convertView.getTag();
		}
		
		if (null != logList)	{
			if (null == checkList.get(position))	{
				android.util.Log.e("getView", "Checklist null at " + position);
			}
			
			//logholder.del_box = (CheckBox)convertView.findViewById(R.id.del_box);
			logholder.date_view.setText(logList.get(position).date);
			logholder.value_view.setText(logList.get(position).value);
			
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
		TextView date_view;
	    TextView value_view;
	    CheckBox del_box;
	}
}
