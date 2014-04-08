/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import SmException.FormatException;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class VRealValueFormat extends COSMOSarrayFormat {
    private double[] realVals;
    private String vType = "";
    
    public VRealValueFormat() {
        super();
    }
    public VRealValueFormat(String dataType){
        super();
        this.vType = dataType;
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
    public double getRealValue( int index){
        return realVals[index];
    }
    
}
