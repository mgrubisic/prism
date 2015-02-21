/*******************************************************************************
 * Name: Java class SmTimeFormatter.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmUtilities;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * This class provides the current date and time in GMT time, according to the
 * Gregorian calendar.  Date format is YYYY-MM-DD where January = 01, Feb = 02, etc.
 * and DD is the day of the month.  The time format is HH:MM:SS where the
 * hour is hour-of-day (24-hour clock).
 * @author jmjones
 */
public class SmTimeFormatter {
    String GMT_ZONE = "GMT";
    
/**
 * Default constructor
 */
    public SmTimeFormatter() {
    }
/**
 * This method returns the formatted date and time, with "GMT" appended
 * @return Text representation of date and time
 */
    public String getGMTdateTime() {
        String result;
        TimeZone zone = TimeZone.getTimeZone("GMT");
        Calendar cal = new GregorianCalendar(zone);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        result = String.format("%1$4d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d %7$3s",
                                year,month,day,hour,min,sec,GMT_ZONE);
        return result;
    }
}
