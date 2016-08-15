/*******************************************************************************
 * Name: Java class FilterAndIntegrateProcess.java
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
 ******************************************************************************/
package SmProcessing;

import static SmConstants.VFileConstants.DEFAULT_NUM_ROLL;
import SmException.SmException;

/**
 *
 * @author jmjones
 */
public class FilterAndIntegrateProcess {
    private final double lowcut;
    private final double highcut;
    private final int numroll;
    private double[] velocity;
    private double[] displace;
    private double[] paddedaccel;
    private final double taperlength;
    private double calculated_taper;
    private final int startIndex;
    private double initialVel;
    private double initialDis;
    
    public FilterAndIntegrateProcess( double lowcut, double highcut, int numroll,
                                double tapertime, int startInd) {
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numroll = numroll;
        this.taperlength = tapertime;
        this.startIndex = startInd;
    }
    public void filterAndIntegrate( double[] accel, double dtime) throws SmException {
        ButterworthFilter filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcut, highcut, 
                                                            dtime, numroll, true);
        if (valid) {
            paddedaccel = filter.applyFilter(accel, taperlength, startIndex);
        } else {
            throw new SmException("Invalid bandpass filter calculated parameters");
        }
        calculated_taper = filter.getTaperlength();
        //The velocity array was updated with the filtered values in the 
        //applyFilter call
        double[] paddedvelocity;
        double[] paddeddisplace;
        velocity = new double[accel.length];
        displace = new double[accel.length];
        
        // Integrate padded acceleration to velocity and displacement and unpad
        paddedvelocity = ArrayOps.Integrate( paddedaccel, dtime, 0.0);
        paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
        System.arraycopy(paddedvelocity, filter.getPadLength(), velocity, 0, velocity.length);
        System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
        initialVel = velocity[0];
        initialDis = displace[0];
    }
    public double[] getVelocity() { return velocity;}
    public double[] getDisplacement() { return displace; }
    public double[] getPaddedAccel() { return paddedaccel; }
    public double getInitialVel() { return initialVel; }
    public double getInitialDis() { return initialDis; }
    public double getCalculatedTaper() { return calculated_taper; }
}
