package vn.edu.hcmut.tachometer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Log	{
	public String profile;
    public String date;
    public String value;

    public Log(String profile, String date, String value) {
    	this.profile = profile;
        this.date = date;
        this.value = value;
    }
    
	public static Comparator<Log> COMPARE_BY_VALUE = new Comparator<Log>() {
        public int compare(Log one, Log other) {
        	int valueOne = Integer.valueOf(one.value.replace("JNI: ", "").replace(" RPM", ""));
        	int valueOther = Integer.valueOf(other.value.replace("JNI: ", "").replace(" RPM", ""));
        	
            return Integer.valueOf(valueOne).compareTo(Integer.valueOf(valueOther));
        }
    };
    
    public static Comparator<Log> COMPARE_BY_DATE = new Comparator<Log>() {
        public int compare(Log one, Log other) {
        	Date dateOne = null;
        	Date dateOther = null;
        	
        	try {
				dateOne = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").parse(one.date);
				dateOther = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").parse(other.date);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			return dateOther.compareTo(dateOne);
        }
    };
    
    public static Comparator<Log> COMPARE_BY_PROFILE = new Comparator<Log>() {
        public int compare(Log one, Log other) {
            return one.profile.compareTo(other.profile);
        }
    };
    
    /** Template of a log_<date>.xml file
     * 
     * <log>
     * 		<profile>Type of being used profile<profile>
     * 		<date>Date of saving the Log</date>
     * 		<value>Calculated value of RPM</value>
     * </log>
     * 
     */
}