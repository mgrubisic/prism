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
public class VIntValueFormat extends COSMOSarrayFormat {
    private int[] intVals;
    private String vType = "";
    
    public VIntValueFormat(){
        super();
    }
    public VIntValueFormat(String dataType){
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
}
