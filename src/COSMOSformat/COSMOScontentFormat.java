/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import static SmConstants.VFileConstants.*;

import SmException.FormatException;
import SmException.SmException;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the base class for the COSMOS Strong Motion Data Format.  It extracts
 * a channel record from the input file and parses it into its various pieces.
 * Each COSMOS file type (V0-3) differs only in the format of its data section.
 * This base class defines the fields and methods common to all COSMOS files and
 * lets the extending classes define their data sections.
 * @author jmjones
 */
public class COSMOScontentFormat {
    protected final String procType;  //data file type flag
    protected int channelNum; //the channel number for this record
    protected int noIntVal;  //NoData value for integer header array
    protected double noRealVal;  //NoData value for real header array
    protected String[] textHeader;  //Holds the text header lines
    protected VIntArray intHeader;  //Holds the integer header array
    protected VRealArray realHeader;  //Holds the real header array
    protected String[] comments;  //Holds the comment field
    protected String endOfData;  //Save the end-of-data line
    /**
     * Default constructor
     * @param procType defines the data type of raw accel., uncorrected accel., etc.
     */
    public COSMOScontentFormat( String procType){
        this.procType = procType;
        this.noIntVal = DEFAULT_NOINTVAL;  //default values
        this.noRealVal = DEFAULT_NOREALVAL;
    }
    /**
     * This method extracts the current component/channel from the input file. 
     * It is set up to handle multiple components in the same file by starting
     * at the given line number in the file and returning the line number at the
     * end of the current component.
     * @param start starting line in the file contents to parse the channel
     * @param infile array holding all the lines from the input file
     * @return the line number after the end of the current component
     * @throws FormatException if unable to extract expected values from text
     * @throws SmException if unable to convert text to numeric
     */
    public int loadComponent (int start, String[] infile) 
                                throws FormatException, SmException {
        int current = start;
        int channel;
        
        //Read in text header, look for number of lines and int, real NoData vals
        current = parseTextHeader(current, infile);
         
        //get integer header values
        intHeader = new VIntArray();    
        current = intHeader.parseValues( current, infile);
        channel = intHeader.getIntValue(STATION_CHANNEL_NUMBER);
        if (channel != noIntVal) {
            channelNum = channel;
        } else {
            throw new SmException("Int header #" + (STATION_CHANNEL_NUMBER+1) 
                                    + ", station channel number, is undefined");            
        }
        
        //get real header values
        realHeader = new VRealArray();     
        current = realHeader.parseValues( current, infile);
         
        //store commments
        current = parseComments ( current, infile);
        
        //get data values
        current = parseDataSection ( current, infile);
        
        //check for last line
        current = parseEndOfData( current, infile );
                
        return (current);
    }
    /**
     * This method must be overridden by each extended class.  Since each COSMOS
     * file differs only in the format of the data sections, each class extending
     * this for a specific V type must define the process for extracting the
     * data from the data arrays.
     * @param startLine line number where data section starts
     * @param infile array containing each line of the input file
     * @return the updated line number, after the data section
     * @throws FormatException if unable to extract parameters from format line
     */
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        System.err.println("method parseDataSection must be overridden");
        return startLine;
    }
    /**
     * This method extracts the text header to get the number of lines and the
     * NoData values.  It also saves the header for writing out other data products.
     * @param startLine line number where the text header starts
     * @param infile array containing each line of the input file
     * @return the updated line number, after the text header section
     * @throws FormatException if unable to extract expected parameters
     * @throws NumberFormatException if unable to convert text to numeric
     */
    private int parseTextHeader(int startLine, String[] infile) 
                                                        throws FormatException {
        int current = startLine;
        String line;
        String[] numbers;
        int numHeaderLines = 0;
        //look for num. of text lines
        String matchRegex = "(?i)(with \\d\\d text lines)";
        
        try {
            //get the first header line and extract the number of lines in the header
            line = infile[current];
            Pattern regField = Pattern.compile(matchRegex);
            Matcher m = regField.matcher( line );
            if (m.find()) {
                String[] num = m.group().split(" ");
                numHeaderLines = Integer.parseInt(num[1]);
            }
            else {
                throw new FormatException("Unable to find number of text header lines at line " + 
                                                                        (current+1));
            }
            //verify that the header lines are in the array, then extract NoData vals
            if ((numHeaderLines > 0) && (infile.length > (startLine + numHeaderLines))) {
                textHeader = new String[numHeaderLines];
                textHeader = Arrays.copyOfRange( infile, startLine, startLine+numHeaderLines);
                line = textHeader[NODATA_LINE].substring(textHeader[NODATA_LINE].lastIndexOf(":")+1);
                numbers = line.split(",");
                if (numbers.length == 2) {
                    noIntVal = Integer.parseInt(numbers[0].trim());
                    noRealVal = Double.parseDouble(numbers[1].trim());
                } else {
                    throw new FormatException("Unable to extract NoData values at line " + 
                                                        (current + NODATA_LINE + 1));
                }
            }
            else {
                throw new FormatException("Error in text header length of " + numHeaderLines);
            }
        } catch (NumberFormatException err) {
            throw new FormatException("Unable to convert text to numeric in text header");
        }
        return (startLine + textHeader.length);
    }
    /**
     * This method extracts and saves the comments for the channel.
     * @param startLine line number where the comments start
     * @param infile array containing each line of the input file
     * @return the updated line number, after the comment section
     * @throws FormatException if unable to locate expected parameters
     * @throws NumberFormatException if unable to convert text to numeric
     */
    private int parseComments(int startLine, String[] infile) 
                                                        throws FormatException {

        //at start of line, skip over any whitespace and pick up all digits
        String getDigitsRegex = "^((\\s*)(\\d+))";
        String commentRegex = "(?i).*comment.*";
        
        int current = startLine;
        int numComments = 0;
        String line = "";
        
        //get the first header line and extract the number of lines in the header
        if (infile.length > current) {
            line = infile[current];
        } else {
            throw new FormatException("EOF found before comments at line " 
                                                                    + (current+1));
        }
        try {
            //Make sure it's the comment section
            if (line.matches(commentRegex)) {
                //get the number of values at the start of the line
                Pattern regDigits = Pattern.compile( getDigitsRegex );
                Matcher m = regDigits.matcher( line );
                if (m.find(0)){
                    numComments = Integer.parseInt(m.group().trim());
                } else {
                    throw new FormatException("Could not find number of comment lines at " + 
                                                                        (current+1));
                }
            } else {
                throw new FormatException("Could not find comments at " + (current+1));
            }

            //verify that the comment lines are in the array
            if ((numComments > 0) && (infile.length > (current + numComments + 1))) {
                comments = new String[numComments+1];
                comments = Arrays.copyOfRange(infile,current,(current + numComments + 1));
            }
            else {
                throw new FormatException("Error in comment length of " + numComments);
            }
        } catch (NumberFormatException err) {
            throw new FormatException("Unable to convert text to numeric in comment line");
        }
        return (startLine + comments.length);
    }
    /**
     * This method extracts and saves the end-of-data line for the channel.
     * @param startLine line number where the EOD starts
     * @param infile array containing each line of the input file
     * @return the updated line number, after the end-of-data line
     * @throws FormatException if unable to locate expected parameters
     */
    private int parseEndOfData( int startLine, String[] infile) throws FormatException {
        String line;
        int current = startLine;
        //at start of line, skip over any whitespace and look for end-of-data,
        // case insensitive
        String endOfDataRegex = "^((\\s*)(?i)(End-of-data))";

        if (infile.length > current) {
            line = infile[current];        
            Pattern regDigits = Pattern.compile( endOfDataRegex );
            Matcher m = regDigits.matcher( line );
            if (m.find(0)){
                this.endOfData = line;
            } else {
                throw new FormatException("Could not find End-of-data at line " + 
                                                                    (current+1));
            }            
        } else {
            throw new FormatException("End-of-file found before end-of-data at line " + 
                                                                        (current+1));
        }
        return (current + 1);
    }

    /**
     * This  method must be overridden by each extending class.  It is called to
     * format the V component into its text file format for writing out to file.
     * @return the contents of the V record in COSMOS format
     */
    public String[] VrecToText () {
        String[] temp = new String[0];
        System.err.println("method VrecToText must be overridden");
        return temp;
    }

    /**
     * Getter for the channel number, which is needed for the output file name
     * @return channel number
     */
    public int getChannelNum(){
        return channelNum;
    }
    /**
     * Getter for the NoData value used in the integer header
     * @return integer NoData value
     */
    public int getNoIntVal(){
        return noIntVal;
    }
    /**
     * Getter for the NoData value used in the real header
     * @return real NoData value
     */
    public double getNoRealVal(){
        return noRealVal;
    }
    public String getSensorLocation() {
        String notFound = "";
        String location = textHeader[SENSOR_LOCATION_LINE];
        if (location.length() < SENSOR_LOCATION_START) {
            return notFound;
        }
        return location.substring(SENSOR_LOCATION_START);
    }
    /**
     * Setter for the channel number.  It sets the number to the channel value
     * in the integer header.
     * @throws SmException if value in header is set to NoData
     */
    public void setChannelNum() throws SmException {
        int num = intHeader.getIntValue(STATION_CHANNEL_NUMBER);
        if (num == noIntVal) {
            throw new SmException("Undefined station channel number in int. header, index " 
                                                        + (STATION_CHANNEL_NUMBER+1));
        }
        channelNum = num;
    }
    /**
     * Getter for individual values from the real header.  
     * @param index location in real header to pick up value
     * @return the value from the header
     * @throws SmException if index is outside of real header range
     */
    public double getRealHeaderValue( int index ) throws SmException {
        double val = 0.0;
        try {
            val = realHeader.getRealValue(index);
        } catch (IndexOutOfBoundsException err) {
            throw new SmException("Real header index " + (index+1) + " is out of range");
        }
        return val;
    }
    /**
     * Setter for individual values in the real header.
     * @param index location in real header to update
     * @param value value to use to update header
     * @throws SmException if index is outside of real header range
     */
    public void setRealHeaderValue( int index, double value ) throws SmException {
        try {
            realHeader.setRealValue( index, value );
        } catch (IndexOutOfBoundsException err) {
            throw new SmException("Real header index " + (index+1) + " is out of range");
        }
        
    }
    /**
     * Setter for the real header format line, calls getters to get the number
     * of values, the number of lines, and the number format and updates the
     * format line.
     */
    public void setRealHeaderFormatLine() {
        String numvals = String.valueOf(realHeader.getNumVals());
        String numlines = String.valueOf(realHeader.getNumLines());
        String numberformat = realHeader.getNumberFormat();
        String line = String.format("%1$4s Real-header values follow on %2$3s lines, Format= ", 
                                                                numvals, numlines);
        realHeader.setFormatLine(line + numberformat);
    }
    /**
     * Getter for individual values from the integer header.  
     * @param index location in integer header to pick up value
     * @return the value from the header
     * @throws SmException if index is outside of integer header range
     */
    public int getIntHeaderValue( int index ) throws SmException {
        int val = 0;
        try {
            val = intHeader.getIntValue(index);
        } catch (IndexOutOfBoundsException err) {
            throw new SmException("Integer header index " + (index+1) + " is out of range");
        }
        return val;
    }
    /**
     * Setter for individual values from the integer header.
     * @param index location in integer header to pick up value
     * @param value value to use to update header
     * @throws SmException if index is outside of integer header range
     */
    public void setIntHeaderValue( int index, int value ) throws SmException {
        try {
            intHeader.setIntValue(index, value);
        } catch (IndexOutOfBoundsException err) {
            throw new SmException("Integer header index " + (index+1) + " is out of range");
        }
    }
    /**
     * This method retrieves a copy of the text header for use in creating a
     * new V component.
     * @return a copy of this component's text header
     */
    public String[] getTextHeader() {
        String[] textCopy = new String[textHeader.length];
        System.arraycopy(textHeader, 0 , textCopy, 0, textHeader.length);
        return textCopy;
    }
    /**
     * This method retrieves a copy of the comments for use in creating a
     * new V component.
     * @return a copy of this component's comments
     */
    public String[] getComments() {
        String[] textCopy = new String[comments.length];
        System.arraycopy(comments, 0 , textCopy, 0, comments.length);
        return textCopy;        
    }
    /**
     * Getter for the End-of-data line
     * @return End-of-data line
     */
    public String getEndOfData() {
        return endOfData;
    }
    /**
     * Getter for the process type of the data, i.e. Raw acceleration
     * @return process type
     */
    public String getProcType() {
        return procType;
    }
}
