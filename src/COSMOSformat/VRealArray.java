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
public class VRealArray extends COSMOSarrayFormat {
    private double[] realVals;
    private String displayType;
    
    public VRealArray(){
        super();
        this.setFieldWidth(DEFAULT_REAL_FIELDWIDTH);
        this.setPrecision(DEFAULT_REAL_PRECISION);
        this.displayType = DEFAULT_REAL_DISPLAYTYPE;
    }
    //Copy constructor - use this to create a copy of a VRealArray object
    public VRealArray( VRealArray source ){
        super();
        this.setFieldWidth(source.getFieldWidth());
        this.setPrecision(source.getPrecision());
        this.displayType = source.displayType;
        this.realVals = new double[source.realVals.length];
        System.arraycopy(source.realVals, 0, this.realVals, 0, source.realVals.length);
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
    //Customize the numeric extraction for real arrays
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
        realVals = new double[holdNumbers.size()];
        for (String each: holdNumbers){
            realVals[next++] = Double.parseDouble(each);
        }
        //add 1 to account for the real header format line
        return (current + calculateNumLines() + 1);
    }

    @Override
    public ArrayList<String> arrayToText() {
        String outType = ("e".compareToIgnoreCase(this.displayType) == 0) ? "e" : "f";
        //!!update this line for floating pt. format
        String formatting = "%" + String.valueOf(this.getFieldWidth()) + "." +
                                String.valueOf(this.getPrecision())+ outType;
        ArrayList <String> textVals = new ArrayList<>();
        
        for (double each : realVals){
            textVals.add(String.format(formatting, each));
        }
        return textVals;
    }
    public double getRealValue( int aindex){
        return realVals[aindex];
    }
    public void setRealValue( int aindex, double avalue ){
        this.realVals[aindex] = avalue;
    }
    public void initRealArray( int aLength ) {
        this.realVals = new double[ aLength ];
    }
    public double[] getRealArray() {
        double[] array = new double[this.realVals.length];
        System.arraycopy(this.realVals, 0, array, 0, this.realVals.length);
        return array;
    }    
    //allow the array to be set with the default print option or to specify
    // a different fieldWidth, precision, and displayType of 'F' or 'E'
    public void setRealArray(double[] inArray ) throws FormatException {
        System.arraycopy(inArray, 0, this.realVals, 0, inArray.length);
        this.setNumVals(this.realVals.length);
        this.buildArrayParams();
    }    
    public void setRealArray(double[] inArray, int afieldWidth, int aprecision,
                                  String adisplayType) throws FormatException {
        System.arraycopy(inArray, 0, this.realVals, 0, inArray.length);
        this.setFieldWidth(afieldWidth);
        this.setPrecision(aprecision);
        this.displayType = adisplayType;
        this.setNumVals(this.realVals.length);
        this.buildArrayParams();
    }    
    private void buildArrayParams() throws FormatException {
        if (this.getFieldWidth() > 0 ) {
            this.setValsPerLine(MAX_LINE_LENGTH / this.getFieldWidth());
        } else {
            throw new FormatException("Invalid field width of " + this.getFieldWidth());
        }
        this.setNumberFormat("(" + String.valueOf(this.getValsPerLine()) + 
                this.displayType + String.valueOf(this.getFieldWidth()) + 
                "." + String.valueOf(this.getPrecision()) + ")");
        this.calculateNumLines();
    }
}
