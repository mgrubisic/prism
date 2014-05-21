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
public class V2Component extends COSMOScontentFormat {
    private VRealArray V2;  //raw acceleration counts
    private final V0Component parentV0;  //link back to the parent V0 record
    private final V1Component parentV1;  //link back to the parent V0 record

    public V2Component( String procType, V0Component pV0, V1Component pV1){
        super( procType );
        this.parentV0 = pV0;
        this.parentV1 = pV1;
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        int current = startLine;
        
        V2 = new VRealArray();
        current = V2.parseValues( current, infile);
        System.out.println("last data value: " + V2.getRealValue(V2.getNumVals()-1));
        return current;
    }
    
}
