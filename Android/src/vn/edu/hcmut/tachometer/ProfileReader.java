package vn.edu.hcmut.tachometer;

import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class ProfileReader {
    // We don't use namespaces
    private static final String ns = null;
    
    public Profile read(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readProfile(parser);
        } finally {
            in.close();
        }
    }
    
    private Profile readProfile(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "profile");
        
        String name = "", avatar = "";
        int minRPM = 0, maxRPM = 0, numBlade = 0;
        
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            
            String tag_name = parser.getName();
            // Starts by looking for the tags
            if (tag_name.equals("name")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		name = parser.getText();
	                parser.nextTag();
	            }
            }
            
            else if (tag_name.equals("avatar")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		avatar = parser.getText();
	                parser.nextTag();
	            }
            }
            
            else if (tag_name.equals("minRPM")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		minRPM = Integer.valueOf(parser.getText());
	                parser.nextTag();
	            }
            }
            
            else if (tag_name.equals("maxRPM")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		maxRPM = Integer.valueOf(parser.getText());
	                parser.nextTag();
	            }
            }
            
            else if (tag_name.equals("numBlade")) {
            	if (parser.next() == XmlPullParser.TEXT) {
            		numBlade = Integer.valueOf(parser.getText());
	                parser.nextTag();
	            }
            }
            
            else {
                skip(parser);
            }
        }
        
		return new Profile(name, avatar, minRPM, maxRPM, numBlade);
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