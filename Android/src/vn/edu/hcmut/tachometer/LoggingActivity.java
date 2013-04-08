package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class LoggingActivity extends Activity implements OnClickListener	{
	
	private int countCheck;
	private int sortStatus;
	private Button btn_del_log;
	private Button btn_cancel;
	
	private List<Log> logList;
	AlertDialog.Builder alertDialogBuilder;
	private LogViewAdapter lw_adapter;
	private ListView listView;
	
	private RelativeLayout.LayoutParams params;
	
	private int BUTTON_DEL_SPACE = 80;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_list);
		
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		countCheck = 0;
		sortStatus = 1;
		
		logList = loadLog();
		// Default sorting type = 1 - "Sort by Date created"
		Collections.sort(logList, Log.COMPARE_BY_DATE);
		listView = (ListView)findViewById(R.id.log_list);
		lw_adapter = new LogViewAdapter(this, R.layout.log_view, logList);
		
        listView.setAdapter(lw_adapter);
        
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setVisibility(View.GONE);
        btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Refresh the checklist
            	for (int i = 0; i < lw_adapter.checkList.size(); i ++)	{
            		lw_adapter.checkList.set(i, false);
            	}
            	
            	// Reset stuffs
            	lw_adapter.deleteMode = false;
            	countCheck = 0;

            	btn_del_log.setVisibility(View.GONE);
            	btn_cancel.setVisibility(View.GONE);
            	
            	params.setMargins(0, 0, 0, 0);
            	listView.setLayoutParams(params);
			}
        });
        
        btn_del_log = (Button) findViewById(R.id.btn_dellog);
        btn_del_log.setVisibility(View.GONE);
        btn_del_log.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
            	File folder = new File(Environment.getExternalStorageDirectory(), "50802566/logs");
				
				for (int i = logList.size(); i >= 0 ; i --) {
            		try	{
	            		if (null == logList.get(i))	{
	            			continue;
	            		}
            		}	catch (IndexOutOfBoundsException e)	{
            			continue;
            		}
            		
            		if (null == lw_adapter.checkList.get(i))	{
            			android.util.Log.e("DEL", "checklist["+ i + "] = null");
            			continue;
            		}
            		
                    if (true == lw_adapter.checkList.get(i)) {
                    	// Delete correlative xml file
                    	Date date = null;
                    	try {
							date = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").parse(logList.get(i).date);
							String filename = new SimpleDateFormat("dd_MM_yyyy - HH_mm_ss").format(date) + ".xml";
	                    	
							File toDelete = new File(folder, filename);
	                		if (!toDelete.delete())	{
	                			android.util.Log.e("DEL", "File " + filename + ": not deleted or not existed");
	                		}
						} catch (ParseException e) {
							e.printStackTrace();
						}
                    	
                		lw_adapter.checkList.remove(i);
                		lw_adapter.remove(lw_adapter.getItem(i));
                    	//android.util.Log.e("DEL", "Deleted " + i + ", size now = " + logList.size());
                    }
                }
				
            	// Refresh the checklist
            	for (int i = 0; i < lw_adapter.checkList.size(); i ++)	{
            		lw_adapter.checkList.set(i, false);
            	}
            	
            	// Reset stuffs
            	lw_adapter.deleteMode = false;
            	countCheck = 0;
            	
            	btn_del_log.setVisibility(View.GONE);
            	btn_cancel.setVisibility(View.GONE);
            	
            	params.setMargins(0, 0, 0, 0);
            	listView.setLayoutParams(params);
            	
            	lw_adapter.notifyDataSetChanged();
			}
		});
	}
	
	/** implements for the MENU soft-key. Android 2.3.x */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.log_menu, menu);
        
        alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
			.setTitle("Sort Logs by")
			.setCancelable(true)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener()	{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
           	
        return true;
    }
	
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (lw_adapter.deleteMode)	{
        	// Don't allow sorting while deleting
            menu.getItem(0).setEnabled(false);
        }
        
        else	{
        	menu.getItem(0).setEnabled(true);
        }
        
        return true;
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		// IMPORTANT: getChildAt(index) only return the VISIBLE views in our current screen
		if (null == listView)	{
			//android.util.Log.e("onOptionsItemSelected", "list view null");
			return true;
		}
		
		alertDialogBuilder.setSingleChoiceItems(R.array.sort_type, sortStatus, new DialogInterface.OnClickListener() {
       		@Override
       		public void onClick(DialogInterface dialog, int item) {
       			switch (item)	{
       				case 0:
       					//Collections.sort(logList, Log.COMPARE_BY_PROFILE);
       					break;
       					
       				case 1:
       					Collections.sort(logList, Log.COMPARE_BY_DATE);
       	            	break;
       					
       				case 2:
       					Collections.sort(logList, Log.COMPARE_BY_VALUE);
       					break;
       					
       				default:
       					break;
       			}
       			
       			sortStatus = item;
       			android.util.Log.e("After", "Now sortStatus = " + sortStatus);
       			lw_adapter.notifyDataSetChanged();
       			
       			dialog.dismiss();
       		}
       	});
		
		// Handle item selection
        switch (item.getItemId()) {
            case R.id.log_sort:
            	// Create and show the Dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
            	
            	
            	
                return true;
                
            case R.id.log_del:
            	if (logList.size() == 0)	{
            		return true;
            	}
            	
            	lw_adapter.deleteMode = true;
            	
            	for (int i = 0; i < listView.getChildCount(); i++) {
            		if (null == listView.getChildAt(i))	{
            			continue;
            		}
        			
        			CheckBox cb = (CheckBox) listView.getChildAt(i).findViewById(R.id.del_box);
        			if (null == cb) {
        				android.util.Log.e("CB", "CheckBox at i = " + i + " null");
        				continue;
        			}
        			
        			cb.setVisibility(View.VISIBLE);
        			cb.setOnClickListener(this);
        			cb.setEnabled(true);
        			
        			cb.setChecked(false);
        		}
            	
            	btn_del_log.setEnabled(false);
            	btn_del_log.setText("DELETE LOG");
    			btn_del_log.setVisibility(View.VISIBLE);
    			
    			btn_cancel.setEnabled(true);
    			btn_cancel.setVisibility(View.VISIBLE);
    			
    			params.setMargins(0, 0, 0, BUTTON_DEL_SPACE);
            	listView.setLayoutParams(params);
            	
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public void onClick(View cb) {
		int graphicalPos = 0;
		int top = ((View)cb.getParent()).getTop();
		int height = ((View)cb.getParent()).getHeight();
		
		// View is NOT partialy hidden
		if (top < 0)	{
			graphicalPos = 0;
		}
		
		else if (top == 0)	{
			graphicalPos = (int) (top / height);
		}
		
		else	{
			graphicalPos = (int) (top / height);
			if (listView.getChildAt(0).getTop() < 0)	{
				graphicalPos = graphicalPos + 1;
			}
			
			else	{
				//android.util.Log.e("NOT PLUS", "First top = " + listView.getChildAt(0).getTop());
			}
		}
		
		int dataPos = graphicalPos + listView.getFirstVisiblePosition();
		lw_adapter.checkList.set(dataPos, ((CheckBox) cb).isChecked());
		
		//android.util.Log.e("onClick", "Checked row " + dataPos + ", graphicalPos = " + graphicalPos + ", top = " + ((View)cb.getParent()).getTop() + ", firstVisible = " + listView.getFirstVisiblePosition());
		
		if (((CheckBox) cb).isChecked())	{
			countCheck += 1;
		}	else	{
			countCheck -= 1;
		}
		
		android.util.Log.e("onClick", "Count check = " + countCheck);
		
		if (0 < countCheck)	{
			if (2 > countCheck)	{
				btn_del_log.setText("DELETE 1 SELECTED ITEM");
			}
			
			else	{
				btn_del_log.setText("DELETE " + countCheck + " SELECTED ITEMS");
			}
			
			btn_del_log.setEnabled(true);
		}
		
		else	{
			btn_del_log.setText("DELETE LOG");
		}
	}
	
	private ArrayList<Log> loadLog()	{
		/** Access folder "my_logs" in external memory */
		File baseDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
								File.separator +
								"50802566/logs");
		if (!baseDir.exists())	{	baseDir.mkdirs();	}
	    
		File[] list_file = baseDir.listFiles();
		
	    /*for (int i = 0; i < list_file.length; i ++)	{
			android.util.Log.e("PRINTLN", list_file[i].getPath());
		}*/
		
		int index = list_file.length;
		ArrayList<Log> list = new ArrayList<Log>();
		
		while (index-- > 0) {
			File file = list_file[index];
			
			FileInputStream fin = null;
			LogReader logr = new LogReader();
			
			try {
				fin = new FileInputStream(file);
				list.add(list_file.length - index - 1, logr.read(fin));
				fin.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}
	
	/** Implements soft-key handler */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (lw_adapter.deleteMode)	{
	    		// Refresh the checklist
            	for (int i = 0; i < lw_adapter.checkList.size(); i ++)	{
            		lw_adapter.checkList.set(i, false);
            	}
            	
            	// Reset stuffs
            	lw_adapter.deleteMode = false;
            	countCheck = 0;

            	btn_del_log.setVisibility(View.GONE);
            	btn_cancel.setVisibility(View.GONE);
            	
            	params.setMargins(0, 0, 0, 0);
            	listView.setLayoutParams(params);
	    	}
	    	
	    	else	{
	    		this.finish();
	    	}
	    	
	    	return true;
	    }
	    
	    return false;
	}
}
