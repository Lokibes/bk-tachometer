package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

public class LogWriter {
	public void writeInternal(Context cw, String filename, String content)	{
		File file = cw.getFileStreamPath(filename);
		if (!file.exists())	{
			file = new File(cw.getFilesDir(), "50802566/logs" + filename);
		}
		
		XmlSerializer xmls = Xml.newSerializer();
		FileOutputStream outputStream;
		
		try {
			outputStream = cw.openFileOutput(filename, DemoUIActivity.MODE_PRIVATE);
			xmls.setOutput(outputStream, null);
			
			// start DOCUMENT
			xmls.startDocument(null, true);
			
			xmls.startTag("", "log");
				xmls.startTag("", "date");
					Date date = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").parse(filename.replace(".xml", ""));
					xmls.text(date.toString());
				xmls.endTag("", "date");
				
				xmls.startTag("", "value");
					xmls.text(content);
				xmls.endTag("", "value");
			xmls.endTag("", "log");

			// end DOCUMENT
			xmls.endDocument();
			
			xmls.flush();
			outputStream.flush();
			outputStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeExternal(Context cw, String filename, String content) 	{
		File folder = new File(Environment.getExternalStorageDirectory(), "50802566/logs");
		File toWrite = new File(folder, filename + ".xml");
		
		if (!toWrite.exists()) {
			try {
				if (!folder.mkdirs())	{
					//android.util.Log.e("Save", "Failed to create directories for save file!");
				}
				
				toWrite.createNewFile();
			} catch (IOException e) {
				//android.util.Log.e("Save", "Failed to create save file! " + e.getMessage());
			}
		}
		
		FileOutputStream outputStream = null;
		
		try {
			outputStream = new FileOutputStream(toWrite);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		XmlSerializer xmls = Xml.newSerializer();
		
		//android.util.Log.e("PRINTLN", folder.getAbsolutePath() + File.separator + filename);
		
		try {
			xmls.setOutput(outputStream, null);
			
			// start DOCUMENT
			xmls.startDocument(null, true);
			
			xmls.startTag("", "log");
				xmls.startTag("", "date");
					Date fromName = new SimpleDateFormat("dd_MM_yyyy - HH_mm_ss").parse(filename);
					String date = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(fromName);
					xmls.text(date.toString());
				xmls.endTag("", "date");
				
				xmls.startTag("", "value");
					xmls.text(content);
				xmls.endTag("", "value");
			xmls.endTag("", "log");

			// end DOCUMENT
			xmls.endDocument();
			
			xmls.flush();
			outputStream.flush();
			outputStream.close();
			
		} catch (FileNotFoundException e) {
			android.util.Log.e("writeExternal", e.toString());
		} catch (IllegalArgumentException e) {
			android.util.Log.e("writeExternal", e.toString());
		} catch (IllegalStateException e) {
			android.util.Log.e("writeExternal", e.toString());
		} catch (IOException e) {
			android.util.Log.e("writeExternal", e.toString());
		} catch (ParseException e) {
			android.util.Log.e("writeExternal", e.toString());
		}
		
		android.util.Log.e("PRINTLN", "Write done!");
	}
}
