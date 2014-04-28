/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import SmException.FormatException;

/**
 *
 * @author jmjones
 */
public class V0Component extends COSMOScontentFormat {
    private VIntArray V0;  //raw acceleration counts

    public V0Component( String procType){
        super( procType );
    }

    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                        FormatException, NumberFormatException {
        int current = startLine;
        
        V0 = new VIntArray();
        current = V0.parseValues( current, infile);
        return current;
    }
    public int getDataLength() {
        return V0.getNumVals();
    }
    public int getDataValue( int index ) {
        return V0.getIntValue(index);
    }
    public int[] getDataArray() {
        return V0.getIntArray();
    }
    
}
