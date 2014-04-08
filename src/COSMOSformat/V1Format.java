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
public class V1Format extends COSMOScontentFormat {
    private VRealValueFormat V1;  //raw acceleration counts
    private final V0Format parentV0;  //link back to the parent V0 record

    public V1Format( String procType, V0Format pV0){
        super( procType );
        this.parentV0 = pV0;
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                        FormatException, NumberFormatException {
        int current = startLine;
        
        V1 = new VRealValueFormat();
        current = V1.parseValues( current, infile);
        System.out.println("last data value: " + V1.getRealValue(V1.getNumVals()-1));
        return current;
    }
    
}
