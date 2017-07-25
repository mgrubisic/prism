/*******************************************************************************
 * Name: Java class SmProduct.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Aug. 2017
 ******************************************************************************/
package SmUtilities;

import static SmConstants.VFileConstants.MAX_LINE_LENGTH;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates the format of the record id before its use in building directory names
 * and recording in the apktable
 */
public class RecordIDValidator {
    private final boolean valid;
    private final String[] sections;
    
    public RecordIDValidator(String id) {
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        String pat = sb.append("^")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("[\\w-]+")
                       .append("$")
                       .toString();
        Pattern officialname = Pattern.compile(pat);
        Matcher m = officialname.matcher(id);
        valid = m.matches();
        sections = (valid) ? id.split("\\.") : null;            
    }
    public boolean isValidRcrdID() {
        return valid;
    }
    public String getEventID() {
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        String event = "";
        if (valid) {
            event = sb.append(sections[0]).append(".").append(sections[1]).toString();
        }
        return event; 
    }
    public String getStationID() {
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        String station = "";
        if (valid) {
            station = sb.append(sections[2]).append(".").append(sections[3]).toString();
        }
        return station;         
    }
}
