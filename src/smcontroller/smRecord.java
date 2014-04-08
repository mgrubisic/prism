/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package smcontroller;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static COSMOSformat.VFileConstants.*;
import SmException.FormatException;
import COSMOSformat.VIntValueFormat;
import COSMOSformat.VRealValueFormat;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
//This class is the container for a channel record.  It parses the input text
//file to extract the information into arrays.  
public class smRecord {
    private String procType;  //data file type flag
    private int noIntVal;  //NoData value for integer header array
    private double noRealVal;  //NoData value for real header array
    private String[] textHeader;  //Holds the text header lines
    private VIntValueFormat intHeader;  //Holds the integer header array
    private VRealValueFormat realHeader;  //Holds the real header array
    private VIntValueFormat V0;  //raw acceleration counts
    private String[] comments;  //Holds the comment field
    private String endOfData;  //Save the end-of-data line
    
    smRecord( String procType){
        this.procType = procType;
        this.noIntVal = -999;
        this.noRealVal = -999.0;
    }
    /**
     * 
     * @param start
     * @param infile
     * @return
     * @throws FormatException
     * @throws NumberFormatException 
     */
    //Step through the parsing of a record, starting at the given line number and
    //continuing until the end-of-data marker.  Return the updated line number.
    public int loadComponent (int start, String[] infile) 
                                throws FormatException, NumberFormatException {
        int current = start;
        
        //Read in text header, look for number of lines and int, real NoData vals
        current = parseTextHeader(current, infile);
         
        //get integer header values
        intHeader = new VIntValueFormat();
        current = intHeader.parseValues( current, infile);
        System.out.println("50th integer val, channel #: " + intHeader.getIntValue(49));
        
        //get real header values
        realHeader = new VRealValueFormat();
        current = realHeader.parseValues( current, infile);
        System.out.format("63d real val, max val: %f%n", realHeader.getRealValue(63));
         
        //store commments
        current = parseComments ( current, infile);
        
        //get data values
        V0 = new VIntValueFormat();
        current = V0.parseValues( current, infile);
        System.out.println("last data value: " + V0.getIntValue(V0.getNumVals()-1));
        
        //check for last line
        current = parseEndOfData( current, infile );
                
        return (current);
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
            System.out.println("numHeaderLines: " + numHeaderLines);
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
            System.out.println(textHeader[8]);
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
            for (String each: comments){
                System.out.println(each);
            }
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
                System.out.println("last line: " + line);
            } else {
                throw new FormatException("Could not find end-of-data at line " + current);
            }            
        } else {
            throw new FormatException("End-of-file found before end-of-data at line " + 
                                                                        current);
        }
        return (current + 1);
    }
}
