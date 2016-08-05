/*******************************************************************************
 * Name: Java class EventOnsetProcess.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 * Date: second release date Oct 2016
 ******************************************************************************/
package SmProcessing;

import SmConstants.VFileConstants;
import SmException.SmException;

/**
 *
 * @author jmjones
 */
public class EventOnsetProcess {
    private final int numroll;
    private final double taperlength;
    private final double lowcutoff;
    private final double highcutoff;
    private final double ebuffer;
    private int pickIndex;
    private int startIndex;
    private double taperused;
    
    public EventOnsetProcess( double lowcut, double highcut, double taper, 
                                int numrl, double ebuf) {
        this.lowcutoff = lowcut;
        this.highcutoff = highcut;
        this.taperlength = taper;
        this.numroll = numrl;
        this.ebuffer =  ebuf;
        this.pickIndex = 0;
        this.startIndex = 0;
        this.taperused = 0.0;
    }
    public void findEventOnset(double[] acc, double dtime, 
                        VFileConstants.EventOnsetType emethod) throws SmException {
        ArrayOps.removeLinearTrend( acc, dtime);
        
        //set up the filter coefficients and run
        ButterworthFilter filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcutoff, highcutoff, 
                                                        dtime, numroll, true);
        if (valid) {
            int calcSec = (int)(taperlength * dtime);
            filter.applyFilter(acc, taperlength, calcSec);  //filtered values are returned in acc
            taperused = filter.getTaperlength();
        } else {
            throw new SmException("Invalid bandpass filter input parameters");
        }
        // Find event onset
        if (emethod == VFileConstants.EventOnsetType.PWD) {
            EventOnsetDetection depick = new EventOnsetDetection( dtime );
            pickIndex = depick.findEventOnset(acc);
            startIndex = depick.applyBuffer(ebuffer);
        } else {
            AICEventDetect aicpick = new AICEventDetect();
            pickIndex = aicpick.calculateIndex(acc, "ToPeak");
            startIndex = aicpick.applyBuffer(ebuffer, dtime);
        }
    }
    public int getStartIndex() { return startIndex; }
    public int getPickIndex() { return pickIndex; }
    public double getTaperlengthAtEventOnset() { return taperused; }
}
