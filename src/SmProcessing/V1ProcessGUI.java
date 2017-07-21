/*******************************************************************************
 * Name: Java class V1ProcessGUI.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Aug. 2017
 ******************************************************************************/

package SmProcessing;

/**
 * The V1ProcessGUI class extends the V1ProcessFormat with a simple constructor
 * to get the acceleration array, and then a process method to remove the mean
 * and record the parameters such as peak value.
 * @author jmjones
 */
public class V1ProcessGUI extends V1ProcessFormat {
    private double[] accel;
    
    public V1ProcessGUI( double[] inArray ) {
        super();
        this.accel = inArray;
    }
    /**
     * Reprocesses the newly-trimmed acceleration array by removing the mean
     * and recalculating the parms such as peak value.
     */
    @Override
    public void processV1Data(){
        //Remove the mean from the array and save for the Real Header
        meanToZero = ArrayOps.findAndRemoveMean(accel);
        
        //Find the new mean (should now be zero) and the location and mag. of peak value
        ArrayStats stat = new ArrayStats( accel );
        avgVal = stat.getMean();
        peakVal = stat.getPeakVal();
        peakIndex = stat.getPeakValIndex();
    }
}
