/*******************************************************************************
 * Name: Java class VIntArray.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package COSMOSformat;

import static SmConstants.VFileConstants.*;
import SmException.FormatException;
import java.util.ArrayList;

/**
 * This class defines the fields and methods for integer arrays in COSMOS format.
 * It can be used for integer headers or integer data arrays.  It extracts the
 * array information from the text file, provides access to individual values or
 * the whole array, and converts the array values and header into text for
 * writing out to a file.  The access to the whole array was added to speed up
 * processing of strong motion records, eliminating the need to copy the data  
 * array before using it in processing.
 * @author jmjones
 */
public class VIntArray extends COSMOSarrayFormat {
    private int[] intVals; // the integer array 
    private String displayType = ""; // holds the display type, such as 'I' 
    /**
     * Default constructor
     */
    public VIntArray(){
        super();
        this.setFieldWidth(DEFAULT_INT_FIELDWIDTH);
        this.displayType = DEFAULT_INT_DISPLAYTYPE;
    }
    /**
     * Copy constructor - use this to create a copy of a VIntArray object from
     * another VIntArray object.  Useful during processing to create a COSMOS
     * component at the next processing step.
     * @param source a VIntArray object to be copied
     */
    public VIntArray( VIntArray source ){
        super();
        this.setFieldWidth(source.getFieldWidth());
        this.displayType = source.displayType;
        this.intVals = new int[source.intVals.length];
        System.arraycopy(source.intVals, 0, this.intVals, 0, source.intVals.length);
        this.setNumVals(source.getNumVals());
        this.setFormatLine(source.getFormatLine());
        this.setNumberFormat(source.getNumberFormat());
        this.setValsPerLine(source.getValsPerLine());
        this.setNumLines(source.getNumLines());  
    }
    /**
     * This method overrides the abstract class to handle the extraction of 
     * numeric values for integer arrays.  It takes a string array of text lines
     * from the input file and an index into the text where the array section
     * begins, and parses the format line, then extracts each number and stores
     * in an integer array
     * @param startLine beginning line in the text file for the array information
     * @param infile string array holding the contents of the COSMOS file
     * @return updated line number, now pointing to the line after the array info.
     * @throws FormatException if unable to find the expected format values or
     * unable to convert text to integer
     */
    @Override
    public int parseValues( int startLine, String[] infile) 
                                                        throws FormatException {
        int current = startLine;
        int next = 0;
        ArrayList<String> holdNumbers;
        
        //Check for EOF before parsing format line
        if (infile.length <= current) {
            throw new FormatException("Unexpected EOF encountered at line " + current);
        }
        try {
            this.parseNumberFormatLine(infile[current]);
            holdNumbers =  this.extractNumericVals( current, infile);
            intVals = new int[holdNumbers.size()];
            for (String each: holdNumbers){
                intVals[next++] = Integer.parseInt(each);
            }
        } catch (NumberFormatException err) {
            throw new FormatException("Unable to convert text to numeric at line " 
                                                                        + current);
        }
        //add 1 to account for the integer header format line
        return (current + calculateNumLines() + 1);
    }
    /**
     * This getter returns a value from the integer array at the given index
     * @param index index into the integer array
     * @return value from the integer array at the index
     * @throws IndexOutOfBoundsException if index not within array index range
     */
    public int getIntValue( int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > intVals.length)) {
            throw new IndexOutOfBoundsException("Integer array index: " + index);
        }
        return intVals[index];
    }
    /**
     * This setter sets a value in the integer array at the given index to the
     * new value
     * @param index index into the integer array
     * @param value value to write into the array at the given index
     * @throws IndexOutOfBoundsException if index not within array index range
     */
    public void setIntValue( int index, int value ) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > intVals.length)) {
            throw new IndexOutOfBoundsException("Integer array index: " + index);
        }
        intVals[index] = value;
    }
    /**
     * This method returns a reference to the integer array.  This is used when
     * processing V0 data values to V1 data.
     * @return reference to the integer array
     */
    public int[] getIntArray(){
        return this.intVals;
    }
    /**
     * This method calculates the values for the valsPerLine, numberFormat, and
     * numLines fields based on the current array size and other field values.
     * @param packtype single-column or packed for output of array
     * @throws FormatException if the fieldWidth field is less than or equal to 0
     */
    public void buildArrayParams(SmArrayStyle packtype) throws FormatException {
        if (this.getFieldWidth() > 0 ) {
            if (packtype == SmArrayStyle.PACKED) {
                this.setValsPerLine(MAX_LINE_LENGTH / this.getFieldWidth());
            } else {
                this.setValsPerLine(1);
            }
        } else {
            throw new FormatException("Invalid field width of " + this.getFieldWidth());
        }
        this.setNumberFormat("(" + String.valueOf(this.getValsPerLine()) + 
                this.displayType + String.valueOf(this.getFieldWidth()) + ")");
        this.calculateNumLines();
    }
    /**
     * This method takes each numeric value and converts it to its text
     * representation according to the formatting stored for the array.
     * Each text representation of a number is stored in an arrayList
     * @return arrayList of text-formatted numbers
     */
    @Override
    public ArrayList<String> arrayToText() {
        String formatting = "%" + String.valueOf(this.getFieldWidth()) + "d";
        ArrayList <String> textVals = new ArrayList<>();
        
        for (int each : intVals){
            textVals.add(String.format(formatting, each));
        }
        return textVals;
    }
    /**
     * Getter for the displayType field,i.e. "I", for type integer
     * @return the display type
     */
    public String getDisplayType() {
        return this.displayType;
    }
}
