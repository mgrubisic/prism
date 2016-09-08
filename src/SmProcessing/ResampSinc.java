/*******************************************************************************
 * Name: Java class ResampSinc.java
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

import static SmConstants.VFileConstants.SAMPLING_LIMIT;

/**
 *
 * @author jmjones
 */
public class ResampSinc {
    private int newrate;
    private final double EPSILON = 0.000000001;
    private int factor;
    private double[] timenew;
    
    public ResampSinc() {
        this.newrate = 0;
        this.factor = 0;
    }
    public double[] resample( double[] array, int sps ) {
        double[] outarr;
        double outval;
        double[] conv;
        double[] errarray = new double[0];
        int inlen = array.length;
        double[] sincout = new double[inlen];
        if (factor == 0) {
            calcNewSamplingRate( sps );
        }
        if (newrate == -1) {
            return errarray;
        }
        int newlen = inlen * factor;
        outarr = new double[newlen];
        
        double newstep = (double)inlen / (double)(newlen-1);
        timenew = ArrayOps.makeTimeArray(newstep, newlen);
        
        for (int i=0; i < newlen; i++) {
            outval = 0.0;
            for (int k=0; k < inlen; k++) {
                conv = ArrayOps.makeTimeArray(1.0, inlen);
                for (int j=0; j < inlen; j++ ) {
                    conv[j] = conv[j] - timenew[i];
                }
                sinc_function( conv, sincout );
                outval = outval + (array[k] * sincout[k]);
            }
            outarr[i] = outval;
        }
        return outarr;
    }
    private void sinc_function( double[] xarray, double[] yarray ) {
        int len = xarray.length;
        for (int i=0; i < len; i++ ) {
            if (( xarray[i] > -EPSILON) && ( xarray[i] < EPSILON)) {                
                yarray[i] = 1.0;
            } else {
                yarray[i] = Math.sin( Math.PI * xarray[i] ) / ( Math.PI * xarray[i] );
            }
        }
    }
    public boolean needsResampling( int sps ) {
        boolean needssampling = false;
        if (sps < SAMPLING_LIMIT) {
            needssampling = true;
            newrate = SAMPLING_LIMIT;
        }
        return needssampling;
    }
    public int calcNewSamplingRate( int sps ) {
        newrate = -1;
        if ((sps > 0) && (needsResampling( sps ))) {
            factor = (int)Math.ceil( (double)SAMPLING_LIMIT / (double)sps );
            newrate = sps * factor;
        }
        return newrate;
    }
    public int getFactor() { return factor; }
    public int getNewSamplingRate() { return newrate; }
    public double[] getNewStepArray() { return timenew; }
}
