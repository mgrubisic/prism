/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import SmException.FormatException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class extends the COSMOScontentFormat base class to define a V0 record.
 * It defines the method to parse the V0 data array and provides getters to 
 * access the array.
 * @author jmjones
 */
public class V0Component extends COSMOScontentFormat {
    private VIntArray V0Data;  //raw acceleration counts
    private String fileName;
    private File stationDir;
    private String rcrdId;
    private String SCNLauth;
    /**
     * Default constructor
     * @param procType identifies the data type of raw accel., uncorrected 
     * accel., etc. of this component object (see data product names in 
     * VFileConstants)
     */
    public V0Component( String procType){
        super( procType );
        this.fileName = "";
        this.rcrdId = "";
        this.SCNLauth = "";
        this.stationDir = null;
    }
    /**
     * This method defines the steps for parsing a V0 data record, which contains
     * an integer data array.
     * @param startLine line number for the start of the data section
     * @param infile contents of the input file, one string per line
     * @return updated line number now pointing to first line after data section
     * @throws FormatException if unable to extract format information or
     * to convert text values to numeric
     */
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        int current = startLine;
        
        V0Data = new VIntArray();
        current = V0Data.parseValues( current, infile);
        return current;
    }
    /**
     * Getter for the length of the data array
     * @return the number of values in the data array
     */
    public void checkForRcrdIdAndAuth() {
        String line = this.textHeader[7];
        String[] segments;
        
        //Get the record id if available
        String matchRegex = "(RcrdId:)";
        Pattern regField = Pattern.compile(matchRegex);
        Matcher m = regField.matcher( line );
        rcrdId = (m.find()) ? line.substring(m.end()) : "";
        
        //Look for the SCNL and Auth tags and save if found
        String authRegex = "(<AUTH>)";
        Pattern authfield = Pattern.compile(authRegex);
        for (String each : this.comments) {
            m = authfield.matcher(each);
            if (m.find()) {
                SCNLauth = each.substring(1);
                break;                
            }
        }
        //Get the channel code if no channel number defined
        String scnlRegex = "(<SCNL>)(\\S+)";
        Pattern scnlfield = Pattern.compile(scnlRegex);
        if ((!SCNLauth.isEmpty()) && (channel.isEmpty())) {
            m = scnlfield.matcher( SCNLauth );
            if (m.find()) {
                segments = m.group(2).split("\\.");
                this.setChannel(segments[1]);
            }
        }
    }
    @Override
    public String[] VrecToText() {
        //add up the length of the text portions of the component, which are
        //the text header, the comments, and the end-of-data line.
        int totalLength = 0;
        int currentLength = 0;
        int textLength = this.textHeader.length + this.comments.length + 1;
        
        //get the header and data arrays as text
        String[] intHeaderText = this.intHeader.numberSectionToText();
        String[] realHeaderText = this.realHeader.numberSectionToText();
        String[] V0DataText = this.V0Data.numberSectionToText();
        
        //add the array lengths to the text lengths to get the total and declare
        //an array of this length, then build it by combining all the component
        //pieces into a text version of the component.
        totalLength = textLength + intHeaderText.length + realHeaderText.length + 
                                                        V0DataText.length;
        String[] outText = new String[totalLength];
        System.arraycopy(this.textHeader, 0, outText, currentLength, 
                                                        this.textHeader.length);
        currentLength = currentLength + this.textHeader.length;
        System.arraycopy(intHeaderText, 0, outText, currentLength, 
                                                            intHeaderText.length);
        currentLength = currentLength + intHeaderText.length;
        System.arraycopy(realHeaderText, 0, outText, currentLength, 
                                                          realHeaderText.length);
        currentLength = currentLength + realHeaderText.length;
        System.arraycopy(this.comments, 0, outText, currentLength, this.comments.length);
        currentLength = currentLength + this.comments.length;
        System.arraycopy(V0DataText, 0, outText, currentLength, V0DataText.length);
        outText[totalLength-1] = this.endOfData;
        return outText;
    }
    public int getDataLength() {
        return V0Data.getNumVals();
    }
    /**
     * Getter for a copy of the data array reference.  Used to access the entire
     * array during data processing.
     * @return a copy of the array reference
     */
    public int[] getDataArray() {
        return V0Data.getIntArray();
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName( String inName ) {
        fileName = inName;
    }
    public File getStationDir() {
        return stationDir;
    }
    public void setStationDir( File inDir ) {
        stationDir = inDir;
    }
    public String getRcrdId() {
        return rcrdId;
    }
    public String getSCNLauth() {
        return SCNLauth;
    }
}
