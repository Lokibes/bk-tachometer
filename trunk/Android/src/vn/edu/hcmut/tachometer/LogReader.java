package vn.edu.hcmut.tachometer;

import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class LogReader {
    // We don't use namespaces
    private static final String ns = null;
    
    public Log read(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readLog(parser);
        } finally {
            in.close();
        }
    }
    
    private Log readLog(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "log");
        
        String profile = null;
        String date = null;
        String value = null;
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            
            String tag_name = parser.getName();
            // Starts by looking for the tags
            if (tag_name.equals("date")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		date = parser.getText();
	                parser.nextTag();
	            }
            }
            
            else if (tag_name.equals("value")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		value = parser.getText();
	                parser.nextTag();
	            }
            }
            
            else if (tag_name.equals("profile")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		profile = parser.getText();
	                parser.nextTag();
	            }
            }
            
            else {
                skip(parser);
            }
        }
        
		return new Log(profile, date, value);
    }
    
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
     }
}