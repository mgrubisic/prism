/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import static COSMOSformat.VFileConstants.*;
import SmException.FormatException;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class VIntArray extends COSMOSarrayFormat {
    private int[] intVals;
    private String displayType = "";
    
    public VIntArray(){
        super();
        this.setFieldWidth(DEFAULT_INT_FIELDWIDTH);
        this.displayType = DEFAULT_INT_DISPLAYTYPE;
    }
    //Copy constructor - use this to create a copy of a VIntArray object
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
     *
     * @param startLine
     * @param infile
     * @return
     * @throws FormatException
     */
    @Override
    //Customize the numeric extraction for integer arrays
    public int parseValues( int startLine, String[] infile) 
                                throws FormatException, NumberFormatException {
        int current = startLine;
        int next = 0;
        ArrayList<String> holdNumbers;
        
        //Check for EOF before parsing format line
        if (infile.length <= current) {
            throw new FormatException("Unexpected EOF encountered at line " + current);
        }
        this.parseNumberFormatLine(infile[current]);
        holdNumbers =  this.extractNumericVals( current, infile);
        intVals = new int[holdNumbers.size()];
        for (String each: holdNumbers){
            intVals[next++] = Integer.parseInt(each);
        }
        //add 1 to account for the integer header format line
        return (current + calculateNumLines() + 1);
    }
    public int getIntValue( int index) {
        return intVals[index];
    }
    public void setIntValue( int index, int aValue ) {
        intVals[index] = aValue;
    }
    public int[] getIntArray() {
        int length = intVals.length;
        int[] array = new int[length];
        System.arraycopy(intVals, 0, array, 0,length);
        return array;
    }    
    public void setIntArray(int[] inArray ) throws FormatException {
        System.arraycopy(inArray, 0, this.intVals, 0, inArray.length);
        this.setNumVals(this.intVals.length);
        this.buildTextFormats();
    }    
    public void setIntArray(int[] inArray, int afieldWidth) throws FormatException {
        System.arraycopy(inArray, 0, this.intVals, 0, inArray.length);
        this.setFieldWidth(afieldWidth);
        this.setNumVals(this.intVals.length);
        this.buildTextFormats();
    }
    public ArrayList<String> arrayToText() {
        String formatting = "%" + String.valueOf(this.getFieldWidth()) + "d";
        ArrayList <String> textVals = new ArrayList<>();
        
        for (int each : intVals){
            textVals.add(String.format(formatting, each));
        }
        return textVals;
    }
    private void buildTextFormats() throws FormatException {
        if (this.getFieldWidth() > 0 ) {
            this.setValsPerLine(MAX_LINE_LENGTH / this.getFieldWidth());
        } else {
            throw new FormatException("Invalid field width of " + this.getFieldWidth());
        }
        this.setNumberFormat("(" + String.valueOf(this.getValsPerLine()) + 
                this.displayType + String.valueOf(this.getFieldWidth()) + ")");
        this.calculateNumLines();
    }

}
