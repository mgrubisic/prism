/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import SmException.FormatException;

/**
 * This class extends the COSMOScontentFormat base class to define a V0 record.
 * It defines the method to parse the V0 data array and provides getters to 
 * access the array.
 * @author jmjones
 */
public class V0Component extends COSMOScontentFormat {
    private VIntArray V0Data;  //raw acceleration counts
    /**
     * Default constructor
     * @param procType identifies the data type of raw accel., uncorrected 
     * accel., etc. of this component object (see data product names in 
     * VFileConstants)
     */
    public V0Component( String procType){
        super( procType );
    }
    /**
     * This method defines the steps for parsing a V0 data record, which contains
     * an integer data array.
     * @param startLine line number for the start of the data section
     * @param infile contents of the input file, one string per line
     * @return updated line number now pointing to first line after data section
     * @throws FormatException if unable to extract format information or
     * to convert text values to numeric
     */
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        int current = startLine;
        
        V0Data = new VIntArray();
        current = V0Data.parseValues( current, infile);
        return current;
    }
    /**
     * Getter for the length of the data array
     * @return the number of values in the data array
     */
    public int getDataLength() {
        return V0Data.getNumVals();
    }
    /**
     * Getter for a copy of the data array reference.  Used to access the entire
     * array during data processing.
     * @return a copy of the array reference
     */
    public int[] getDataArray() {
        return V0Data.getIntArray();
    }
}
