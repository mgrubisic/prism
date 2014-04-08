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
public class V0Format extends COSMOScontentFormat {
    private VIntValueFormat V0;  //raw acceleration counts

    public V0Format( String procType){
        super( procType );
    }

    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                        FormatException, NumberFormatException {
        int current = startLine;
        
        V0 = new VIntValueFormat();
        current = V0.parseValues( current, infile);
        System.out.println("last data value: " + V0.getIntValue(V0.getNumVals()-1));
        return current;
    }

    
}
