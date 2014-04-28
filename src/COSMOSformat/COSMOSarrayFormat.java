/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import static COSMOSformat.VFileConstants.MAX_LINE_LENGTH;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import SmException.FormatException;
import java.util.Arrays;

/**
 *
 * @author jmjones
 */
abstract class COSMOSarrayFormat {
    private String formatLine;  //save a copy of the line with formatting
    private int numLines;  //total number of lines for numeric block
    private int numVals;  //total number of values in numeric block
    private String numberFormat;  //text of format, i.e. (10I8)
    private int valsPerLine; //number of values packed per 80-char line
    private int fieldWidth;  //number of characters for each numeric value
    private int precision;  //number of places after decimal point (reals)
    
    public COSMOSarrayFormat(){
        this.numLines = 0;
        this.numVals = 0;
        this.numberFormat = "";
        this.valsPerLine = 0;
        this.fieldWidth = 0;
        this.precision = 0;
    }
    
    //Let each caller handle the conversion of the text data to the numeric vals.
    public abstract int parseValues( int startLine, String[] infile) 
                                throws FormatException, NumberFormatException;
    
    //Start with the format line before either the header or the data arrays.
    //Pull out the number of values and the format for each value.
    public void parseNumberFormatLine( String line) 
                                throws FormatException, NumberFormatException {
         
        //at start of line, skip over any whitespace and pick up all digits
        String getDigitsRegex = "^((\\s*)(\\d+))";
        //pick up (xxx) for the format
        String formatRegex = "\\((\\d+)([A-Za-z]+)(\\d+)(\\.*)(\\d*)\\)";
        //look for groups of digits in the number format
        String fieldRegex = "(\\d+)";
        
        ArrayList<String> formatter;
        
        //set the copy of the header format line
        this.formatLine = line;
        
        //get the number of values at the start of the line
        Pattern regDigits = Pattern.compile( getDigitsRegex );
        Matcher m = regDigits.matcher( line );
        if (m.find(0)){
            this.numVals = Integer.parseInt(m.group().trim());
        } else {
            throw new FormatException("Could not find number of values in " + line);
        }
        //get the format as text
        Pattern regFormat = Pattern.compile(formatRegex);
        m = regFormat.matcher(line);
        if (m.find(0)){
            this.numberFormat = m.group().trim();
        } else {
            throw new FormatException("Could not find number format in " + line);
        }
        //get field width and optional precision (if real vals)
        Pattern regField = Pattern.compile(fieldRegex);
        m = regField.matcher( this.numberFormat );
        formatter = new ArrayList<>();
        while (m.find()) {
            formatter.add(m.group());
        }
        if ((formatter.size() == 2) && 
                ((this.numberFormat.contains("I")) || (this.numberFormat.contains("i")))){
            this.valsPerLine = Integer.parseInt(formatter.get(0));
            this.fieldWidth = Integer.parseInt(formatter.get(1));
        } else if ((formatter.size() == 3) && 
                            ((this.numberFormat.contains("F")) || 
                                    (this.numberFormat.contains("E")) || 
                                        (this.numberFormat.contains("f")) || 
                                            (this.numberFormat.contains("e")))){
            this.valsPerLine = Integer.parseInt(formatter.get(0));
            this.fieldWidth = Integer.parseInt(formatter.get(1));
            this.precision = Integer.parseInt(formatter.get(2));
        } else {
            throw new FormatException("Could not extract format values in " + line);
        }
    }

    //Pull each string representation of a number out of the line and put into
    //an array.  Use the field width to separate the values in each line.
    //Continue until the total value count is reached.  Let the caller of this
    //method do the integer or real conversion of the numbers.
    public ArrayList<String> extractNumericVals(int startLine, String[] infile) 
                                                        throws FormatException {
        ArrayList<String> holdNumbers;
        int current = startLine;
        String line;
        int total = 0;
        
        //do some initial error checking
        if ((this.numVals <= 0) || (this.fieldWidth <= 0)) {
            throw new FormatException("Invalid number of values: " + this.numVals 
            + " or field width: " + this.fieldWidth);
        }
        holdNumbers = new ArrayList<>();
        while (total < this.numVals){
            current++;
            if (infile.length <= current) {
                throw new FormatException("Unexpected end-of-file at line " + current);
            }
            line = infile[current];
            if (line.length() < this.fieldWidth) {
                throw new FormatException("Could not extract number from line " + current);
            }
            for (int j = 0; j <= line.length()- this.fieldWidth; j = j + this.fieldWidth) {
                holdNumbers.add(line.substring(j, j + this.fieldWidth).trim());
                total++;
            }
        }           
        return holdNumbers;
    }
    
    //Use the number of data values and the number of values per line extracted
    //from the format line to calculate the number of lines of data in the file.
    //Using integer math, round up the count if there's an unfilled last line.
    public int calculateNumLines() throws FormatException {
        if (this.valsPerLine > 0) {
            this.numLines = (this.numVals / this.valsPerLine) + 
                              (((this.numVals % this.valsPerLine) > 0) ? 1 : 0);
        } else {
            throw new FormatException("Invalid number of values per input line");
        }      
        return this.numLines;
    }
    //Let the individual array types convert their data types into formatted strings
    public abstract ArrayList<String> arrayToText();
    
    public String[] numberSectionToText() {
        int valsToPack = 0;
        int current = 0;
        int totalLength = 1 + this.numLines;
        String[] newText = new String[totalLength];
        newText[0] = this.getFormatLine();
        ArrayList<String> textVals = this.arrayToText();
        
        //pack each text value into a line according to the values per line
        for (int i=0; i<this.numLines; i++) {
            StringBuilder line = new StringBuilder();
            //calculate the number of vals to pack into a single line
            //use valsPerLine unless the last line has fewer
            valsToPack = ((this.numVals-current) >= this.valsPerLine) ? 
                                this.valsPerLine : (this.numVals - current);
            for (int next=0; next < valsToPack; next++){
                line.append(textVals.get(current));
                current++;
            }
            newText[i+1] = line.toString();
        }
        return newText;
    }
    
    public String getFormatLine(){
        return this.formatLine;
    }    
    public void setFormatLine(String aformatLine){
        this.formatLine = aformatLine;
    }    
    public String getNumberFormat(){
        return this.numberFormat;
    }    
    public void setNumberFormat(String anumberFormat){
        this.numberFormat = anumberFormat;
    }    
    public int getNumVals(){
        return this.numVals;
    }    
    public void setNumVals( int anumVals){
        this.numVals = anumVals;
    }    
    public int getNumLines(){
        return this.numLines;
    }    
    public void setNumLines( int anumLines){
        this.numLines = anumLines;
    }    
    public int getValsPerLine() {
        return this.valsPerLine;
    }
    public void setValsPerLine( int avalsPerLine ) {
        this.valsPerLine = avalsPerLine;
    }
    public int getFieldWidth() {
        return this.fieldWidth;
    }
    public void setFieldWidth( int afieldWidth ) {
        this.fieldWidth = afieldWidth;
    }
    public int getPrecision() {
        return this.precision;
    }
    public void setPrecision( int aprecision ) {
        this.precision = aprecision;
    }
}
