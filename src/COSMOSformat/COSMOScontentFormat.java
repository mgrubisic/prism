/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import static COSMOSformat.VFileConstants.*;

import SmException.FormatException;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jmjones
 */
public class COSMOScontentFormat {
    private final String procType;  //data file type flag
    private int channelNum;
    protected int noIntVal;  //NoData value for integer header array
    protected double noRealVal;  //NoData value for real header array
    protected String[] textHeader;  //Holds the text header lines
    protected VIntArray intHeader;  //Holds the integer header array
    protected VRealArray realHeader;  //Holds the real header array
    protected String[] comments;  //Holds the comment field
    protected String endOfData;  //Save the end-of-data line
    
    public COSMOScontentFormat( String procType){
        this.procType = procType;
        this.noIntVal = -999;
        this.noRealVal = -999.0;
    }
    public int loadComponent (int start, String[] infile) 
                                throws FormatException, NumberFormatException {
        int current = start;
        
        //Read in text header, look for number of lines and int, real NoData vals
        current = parseTextHeader(current, infile);
         
        //get integer header values
        intHeader = new VIntArray();    
        current = intHeader.parseValues( current, infile);
        channelNum = intHeader.getIntValue(STATION_CHANNEL_NUMBER);
        
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
    
    //each COSMOS file differs only in the format of the data sections.  Let each
    //class extending this for a specific V type define the process for extracting
    //the data from the arrays.
    public int parseDataSection (int startLine, String[] infile) throws 
                                        FormatException, NumberFormatException {
        System.err.println("method parseDataSection must be overridden");
        return startLine;
    }
    
    //Extract the text header to get the number of lines and NoData values.  Also
    //save for writing out other data products.
    private int parseTextHeader(int startLine, String[] infile) 
                             throws FormatException, NumberFormatException {
        int current = startLine;
        String line;
        String[] numbers;
        int numHeaderLines = 0;
        
        //get the first header line and extract the number of lines in the header
        line = infile[current];
        if (line.length() >= HEADLINE_1_LENGTH){
            numHeaderLines = Integer.parseInt(
                            line.substring(NUM_HEAD_START,NUM_HEAD_END).trim());
        }
        else {
            throw new FormatException("Error parsing V0 line " + current + " " + 
                    " at line number " +line);
        }

        //verify that the header lines are in the array, then extract NoData vals
        if ((numHeaderLines > 0) && (infile.length > (startLine + numHeaderLines))) {
            textHeader = new String[numHeaderLines];
            textHeader = Arrays.copyOfRange( infile, startLine, startLine+numHeaderLines);
            line = textHeader[NODATA_LINE].substring(textHeader[NODATA_LINE].lastIndexOf(":")+1);
            numbers = line.split(",");
            noIntVal = Integer.parseInt(numbers[0].trim());
            noRealVal = Double.parseDouble(numbers[1].trim());
        }
        else {
            throw new FormatException("Error in text header length of " + numHeaderLines);
        }
        return (startLine + textHeader.length);
    }

    //Check for and save the comment section.
    private int parseComments(int startLine, String[] infile) 
                                throws FormatException, NumberFormatException {

        //at start of line, skip over any whitespace and pick up all digits
        String getDigitsRegex = "^((\\s*)(\\d+))";
        
        int current = startLine;
        int numComments = 0;
        String line = "";
        
        //get the first header line and extract the number of lines in the header
        if (infile.length > current) {
            line = infile[current];
        } else {
            throw new FormatException("End-of-file found before comments at line " 
                                                                    + current);
        }
        //get the number of values at the start of the line
        Pattern regDigits = Pattern.compile( getDigitsRegex );
        Matcher m = regDigits.matcher( line );
        if (m.find(0)){
            numComments = Integer.parseInt(m.group().trim());
        } else {
            throw new FormatException("Could not find number of comment lines in " + line);
        }
        
        //verify that the comment lines are in the array
        if ((numComments > 0) && (infile.length > (current + numComments + 1))) {
            comments = new String[numComments+1];
            comments = Arrays.copyOfRange(infile,current,(current + numComments + 1));
        }
        else {
            throw new FormatException("Error in comment length of " + numComments);
        }
        return (startLine + comments.length);
    }
    
    //Check for the end-of-data line to mark the end of the channel record.
    //Save the text line for writing out results.
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
                throw new FormatException("Could not find end-of-data at line " + current);
            }            
        } else {
            throw new FormatException("End-of-file found before end-of-data at line " + 
                                                                        current);
        }
        return (current + 1);
    }
    public void buildNewIntHeaderFormatLine() {
        String line = "";
        line = String.format("%1$4s Integer-header values follow on %2$3s lines, Format= ",
                    String.valueOf(intHeader.getNumVals()),
                        String.valueOf(intHeader.getNumLines()));
        intHeader.setFormatLine(line + intHeader.getNumberFormat());
    }
    public void buildNewRealHeaderFormatLine() {
        String line = "";
        line = String.format("%1$4s Real-header values follow on %2$3s lines, Format= ",
                    String.valueOf(realHeader.getNumVals()),
                        String.valueOf(realHeader.getNumLines()));
        realHeader.setFormatLine(line + realHeader.getNumberFormat());
    }
    public int getChannelNum(){
        return channelNum;
    }
    public void setChannelNum() {
        channelNum = intHeader.getIntValue(STATION_CHANNEL_NUMBER);
    }
    public double getRealHeaderValue( int index ) {
        return realHeader.getRealValue(index);
    }
    public void setRealHeaderValue( int aindex, double avalue ) {
        realHeader.setRealValue( aindex, avalue );
    }
    public int getIntHeaderValue( int index ) {
        return intHeader.getIntValue(index);
    }
    public void setIntHeaderValue( int index, int aValue ) {
        intHeader.setIntValue(index, aValue);
    }
    public double[] getRealHeaderArray() {
        return realHeader.getRealArray();
    }
    public int[] getIntHeaderArray() {
        return intHeader.getIntArray();
    }
    public void setRealHeaderArray( double[] aArray) throws FormatException {
        realHeader.setRealArray( aArray );
    }
    public void setIntHeaderArray( int[] aArray) throws FormatException {
        intHeader.setIntArray( aArray );
    }
    public String[] getTextHeader() {
        String[] textCopy = new String[textHeader.length];
        System.arraycopy(textHeader, 0 , textCopy, 0, textHeader.length);
        return textCopy;
    }
    public String[] getComments() {
        String[] textCopy = new String[comments.length];
        System.arraycopy(comments, 0 , textCopy, 0, comments.length);
        return textCopy;        
    }
}
