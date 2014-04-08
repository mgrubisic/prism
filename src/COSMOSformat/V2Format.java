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
public class V2Format extends COSMOScontentFormat {
    private VRealValueFormat V2;  //raw acceleration counts
    private final V0Format parentV0;  //link back to the parent V0 record
    private final V1Format parentV1;  //link back to the parent V0 record

    public V2Format( String procType, V0Format pV0, V1Format pV1){
        super( procType );
        this.parentV0 = pV0;
        this.parentV1 = pV1;
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                        FormatException, NumberFormatException {
        int current = startLine;
        
        V2 = new VRealValueFormat();
        current = V2.parseValues( current, infile);
        System.out.println("last data value: " + V2.getRealValue(V2.getNumVals()-1));
        return current;
    }
    
}
