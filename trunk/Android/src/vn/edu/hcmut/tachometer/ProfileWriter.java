package vn.edu.hcmut.tachometer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Environment;
import android.util.Xml;

public class ProfileWriter {
	public void writeInternal(Context cw, String filename, String name, String avatar, int minRPM, int maxRPM, int numBlade)	{
		File path = new File(cw.getFilesDir() + File.separator + "50802566/profiles");
		if (!path.exists())	{	path.mkdirs();	}
		
		XmlSerializer xmls = Xml.newSerializer();
		FileOutputStream outputStream;
		
		try {
			outputStream = cw.openFileOutput(
					path.getAbsolutePath() + File.separator + filename,
					DemoUIActivity.MODE_PRIVATE);
			xmls.setOutput(outputStream, null);
			
			// start DOCUMENT
			xmls.startDocument(null, true);
			
			xmls.startTag("", "profile");
				xmls.startTag("", "name");
					xmls.text(name);
				xmls.endTag("", "name");
				
				xmls.startTag("", "avatar");
					xmls.text(avatar);
				xmls.endTag("", "avatar");
				
				xmls.startTag("", "minRPM");
					xmls.text(Integer.toString(minRPM));
				xmls.endTag("", "minRPM");
				
				xmls.startTag("", "maxRPM");
					xmls.text(Integer.toString(maxRPM));
				xmls.endTag("", "maxRPM");
				
				xmls.startTag("", "numBlade");
					xmls.text(Integer.toString(numBlade));
				xmls.endTag("", "numBlade");
				
			xmls.endTag("", "profile");

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
		}
	}
	
	public void writeExternal(Context cw, String filename, String name, String avatar, int minRPM, int maxRPM, int numBlade) 	{
		File folder = new File(Environment.getExternalStorageDirectory(), "50802566/profiles");
		if (!folder.exists())	{	folder.mkdirs();	}
		File toWrite = new File(folder, filename);
		
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
			
			xmls.startTag("", "profile");
				xmls.startTag("", "name");
					xmls.text(name);
				xmls.endTag("", "name");
				
				xmls.startTag("", "avatar");
					xmls.text(avatar);
				xmls.endTag("", "avatar");
				
				xmls.startTag("", "minRPM");
					xmls.text(Integer.toString(minRPM));
				xmls.endTag("", "minRPM");
				
				xmls.startTag("", "maxRPM");
					xmls.text(Integer.toString(maxRPM));
				xmls.endTag("", "maxRPM");
				
				xmls.startTag("", "numBlade");
					xmls.text(Integer.toString(numBlade));
				xmls.endTag("", "numBlade");
				
			xmls.endTag("", "profile");

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
		}
		
		android.util.Log.e("PRINTLN", "Write done!");
	}
}
