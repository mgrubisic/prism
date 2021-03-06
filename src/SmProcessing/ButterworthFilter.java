/*******************************************************************************
 * Name: Java class ButterworthFilter.java
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

import java.util.Arrays;

/**
*  <p>function bandpass (s, nd, f1, f2, delt, nroll, icaus)</p>
*
*  <p>function bandpass implements a butterworth bandpass filter</p>
*
*  <p>s[] = input time series array of doubles </p>
* 
*  nd = the number of points in the time series, which is less than the length of s (see below) 
*  f1 = the lower cutoff frequency 
*  f2 = the higher cutoff frequency
*  delt = the timestep
*  nroll = butterworth bandpass filter order is 2*nroll (max val nroll=8)
*  icaus = causal, acausal filter flag, icaus = 1 for causal filter
*  <p>
*  The dimension of the input array s must be at least as large
*  as the larger of the following (if acausal filtering):
*  (nd + (3 * nroll)/(f1 * delt))  or  (nd + (6 * nroll)/((f2 - f1) * delt))
*  </p><p>
*  All floating point operations are of type double
*  In any operation with integer and double, integer is converted to double
* </p><p>
*  From the Fortran BAND.FOR, TSPP--A Collection of FORTRAN Programs for Processing
*                               and Manipulating Time Series
*  Butterworth bandpass filter order 2*nroll (nroll.le.8) (see Kanasewich, 
*    Time Sequence Analysis in Geophysics, Third Edition, 
*    University of Alberta Press, 1981)</p>
*  written by W.B. Joyner 01/07/97
* Dates: xx/xx/xx - Written by Bill Joyner
*        09/12/00 - Changed "n" to "nroll" to eliminate confusion with
*                   Kanesewich, who uses "n" as the order (=2*nroll), and
*                   cleaned up appearance of code by using spaces, indents, etc.
*        09/12/00 - double precision statements added so that the output
*                   series has sufficient precision for double integration.
*        11/08/00 - Increased dimension of s from 50000 to 100000
*        02/14/01 - Modified/corrected real numbers function calls to 
*                   double precision - cds
*        02/22/01 - Removed dimension of s (it is up to the user to specify
*                   it properly)
* @author jmjones, translated from Fortran into Java, June 2014
**/
public class ButterworthFilter {
    private double f1;
    private double f2;
    private double dtime;
    private int nroll;
    private boolean icaus;
    private final double pi = Math.PI;
    private final int MAXROLL = 8;
    private final double epsilon = 0.001;
    private double[] fact;
    private double[] b1;
    private double[] b2;
    private int npad;
    
    private int tapercount;
    private int taperend;
    
    /**
     * Default constructor
     */
    public ButterworthFilter() {
        this.npad = 0;
        this.tapercount = 0;
        this.taperend = 0;
    }
    /**
     * This method calculates the filter coefficients based on the corner frequencies,
     * the sample time interval, and the roll off.  A flag is also input to select
     * the type of filtering, either causal or acausal.
     * @param lowCutOff the filter low cutoff frequency
     * @param highCutOff the filter high cutoff frequency
     * @param dtime the sample time interval for the record
     * @param rolloff the filter roll off, which is 1/2 the filter order
     * @param acausal flag indicating causal or acausal filtering
     * @return true or false flag indicating successful calculation of filter
     * coefficients (based on valid input parameters)
     */
    public boolean calculateCoefficients(double lowCutOff, double highCutOff, double dtime,
                                int rolloff, boolean acausal) {
        
        this.f1 = lowCutOff;
        this.f2 = highCutOff;
        this.dtime = dtime;
        this.nroll = rolloff;
        this.icaus = acausal;           //true if acausal filter
        
        fact = new double[2*MAXROLL];
        b1 = new double[2*MAXROLL];
        b2 = new double[2*MAXROLL];
        
        double pre; double pim; double argre; double argim; double rho; double theta;
        double sjre; double sjim; double bj; double cj; double con;
        int index;
        
        //Check input parameters for valid values
        if ((Math.abs(f1 - 0.0) < epsilon) || (Math.abs(f2 - f1) < epsilon) ||
                                           (rolloff < 1) || (rolloff > MAXROLL)){
            return false;
        }
        double nyquist = (1.0 / dtime) / 2.0;
        if ((Math.abs(f1 - nyquist) < epsilon) || (Math.abs(f2 - nyquist) < epsilon)) {
            return false;
        }
        
        //for w1 and w2 calc., the 2 in the num. and denom. can be deleted but its
        //left in for clarity
        double w1 = 2.0 * Math.tan(((2.0*pi*f1)*dtime)/2.0) / dtime;
        double w2 = 2.0 * Math.tan(((2.0*pi*f2)*dtime)/2.0) / dtime;
        
        //calculate the filter coefficients into arrays b1 and b2, and calculate
        //the gain into array fact
        for (int k = 1; k < nroll+1; k++) {
            pre = (-1.0) * Math.sin((pi*(2.0*k - 1)) / (4.0*nroll));
            pim = Math.cos((pi*(2.0*k - 1)) / (4.0*nroll));
            
            argre = (((Math.pow(pre,2.0) - Math.pow(pim,2.0)) * Math.pow((w2-w1),2.0)) / 4.0) - (w1 * w2);
            argim = (2.0 * pre * pim * Math.pow((w2-w1),2.0)) / 4.0;
            
            rho = Math.pow((Math.pow(argre,2.0) + Math.pow(argim, 2.0)), (1.0/4.0));
            theta = pi + (Math.atan2(argim, argre)) / 2.0;
            
            for (int i = 1; i < 3; i++) {
                sjre = (pre * (w2-w1)/2.0) + (Math.pow(-1,i) * rho * ((-1.0)*Math.sin(theta-(pi/2.0))));
                sjim = (pim * (w2-w1)/2.0) + (Math.pow(-1,i) * rho * (Math.cos(theta-(pi/2.0))));
                
                bj = (-2.0) * sjre;
                cj = Math.pow(sjre,2) + Math.pow(sjim,2);
                con = 1.0 / ((2.0/dtime) + bj + (cj*dtime/2.0));
                
                index = 2*k + i - 3;
                fact[index] = (w2 - w1) * con;
                b1[index] = ((cj*dtime) - (4.0/dtime)) * con;
                b2[index] = ((2.0/dtime) - bj + (cj*dtime/2.0)) * con;
            }
        }
        return true;
    }
    /**
     * This method does the actual filtering of the input array.  If the input
     * flag was set to acausal, the array is filtered forwards and backwards.  
     * If it was set to causal, the filtering is only forwards.  Before filtering,
     * if acausal, pads are added to the front and back of the array. The ends of
     * the input array are tapered by a half-cosine taper (only acausal) in order to prevent
     * ringing in the output waveform.  The taper length for the start of the array is calculated by finding
     * the first zero crossing that occurs before the event onset.  This time
     * becomes the taper length time, with exceptions.  The exceptions: if there's
     * no zero crossing found before the event onset, or the calculated time
     * is less than the value entered as taplengthtime.  For these exceptions,
     * the input taper length value is used instead.  The taper length for the end
     * of the array is the taper length input value from the configuration file.
     * @param arrayS the input array to filter, NOTE: this array is updated with
     * the filtered version upon return
     * @param taplengthtime the length of time in seconds to apply the taper.  This
     * value is used for the taper at the end of the array and only used for the
     * taper at the start of the array if the calculated taper length is invalid or too short.
     * @param eventOnsetIndex the event onset index is used to refine the
     * taper length
     * @return an array containing the filtered result with the pads still included
     */
    public double[] applyFilter( double[] arrayS, double taplengthtime, int eventOnsetIndex ) {
        
        int np2;
        double[] filteredS;
        double x1; double x2; double y1; double y2; double xp; double yp;

        //Calculate the length of the initial cosine taper.  Put a lower limit of the
        //taperlength time specified in the configuration file, and set the ending
        //taper length to the configuration file value.
        tapercount = ArrayOps.findZeroCrossing(arrayS, eventOnsetIndex, 0);
        if ((tapercount <= 0) || ((tapercount*dtime) < taplengthtime)) {
            tapercount = (int)((2.0*taplengthtime) / dtime);
        }
        taperend = (int)((2.0*taplengthtime) / dtime);
        
        //Copy the input array into a return array.  If the filter was configured
        //as acausal, then pad the length of the array by the value calculated below.
        //Before padding, apply a cosine taper to the front and back of the 
        //array.
        if (icaus) {
            if (tapercount > 0) {
                applyCosineTaper( arrayS, tapercount, taperend);
            }
            npad = (int)Math.floor(3.0 * (nroll / (f1 * dtime)));
            int check = (int)Math.floor(6.0 * (nroll / ((f2 - f1) * dtime)));
            if (npad < check) {
                npad = check;
            }
//            System.out.println("+++ npad: " + npad + " array: " + arrayS.length);
            np2 = arrayS.length + npad;
            filteredS = new double[np2];
            Arrays.fill(filteredS, 0.0);
            System.arraycopy(arrayS, 0, filteredS, (npad/2), arrayS.length);
            
        } else {  //causal filter, filtered array is same length as input array
            np2 = arrayS.length;
            filteredS = new double[np2];
            System.arraycopy(arrayS, 0, filteredS, 0, np2);
        }
//        SmDebugLogger elog = SmDebugLogger.INSTANCE;
//        elog.writeOutArray(filteredS, "after_taper_with_pads.txt");
        //filter the array
        for (int k = 0; k < 2*nroll; k++) {
            x1 = 0.0;
            x2 = 0.0;
            y1 = 0.0;
            y2 = 0.0;
            for (int j = 0; j < np2; j++) {
                xp = filteredS[j];
                yp = fact[k] * (xp - x2) - (b1[k] * y1) - (b2[k] * y2);
                filteredS[j] = yp;
                y2 = y1;
                y1 = yp;
                x2 = x1;
                x1 = xp;
            }
        }
        
        //if acausal, filter again from back to front
        if (icaus) {
            for (int k = 0; k < 2*nroll; k++) {
                x1 = 0.0;
                x2 = 0.0;
                y1 = 0.0;
                y2 = 0.0;
                for (int j = 0; j < np2; j++) {
                    xp = filteredS[np2 - j - 1];
                    yp = fact[k] * (xp - x2) - (b1[k] * y1) - (b2[k] * y2);
                    filteredS[np2 - j - 1] = yp;
                    y2 = y1;
                    y1 = yp;
                    x2 = x1;
                    x1 = xp;
                }
            }
            System.arraycopy(filteredS, (npad/2), arrayS, 0, arrayS.length);
        } else {
            System.arraycopy(filteredS, 0, arrayS, 0, np2);
        }
//        System.out.println("+++ after filter, arrayS[0] = " + arrayS[0]);
//        System.out.println("+++ after filter, arrayS[end] = " + arrayS[arrayS.length-1]);
        return filteredS;
    }
    /**
     * This method adds the half cosine taper to the front and back of the array
     * @param array input array to have the taper applied to
     * @param range the number of elements at the front and back of the array that the
     * taper should be applied to. The length of the half cosine taper
     * itself is 1/2 of the range.
     * @param endrange the number of elements at the back of the array that the taper 
     * should be applied to. The length of the half cosine taper
     * itself is 1/2 of the end range.
     */
    public void applyCosineTaper( double[] array, int range, int endrange ) {
        //The range is the number of elements at the front and back of the
        //array that the taper should be applied to.
        
        int len = array.length;
        //range is N, the number of samples over which to apply the taper,
        //and m is the length of the half cosine taper itself.
        int m = range / 2;
        
        double[] taper = new double[m];
        for (int i = 0; i < m; i++) {
            taper[i] = 0.5 * (1.0 - Math.cos(2 * pi * i / (range-1)));
        }
        
        //apply at the front
        for (int i = 0; i < m; i++) {
            array[i] = array[i] * taper[i];
        }
        //Apply the end-length taper to the end of the array
        int m2 = endrange / 2;
        
        double[] taper2 = new double[m2];
        for (int i = 0; i < m2; i++) {
            taper2[i] = 0.5 * (1.0 - Math.cos(2 * pi * i / (endrange-1)));
        }
        
        //apply at the end
        int k = m2 - 1;
        for (int i = len - m2; i < len; i++) {
            array[i] = array[i] * taper2[k];
            k--;
        }
    }
    /**
     * Getter for the array with the calculated gains
     * @return the gains
     */
    public double[] getFact() { return fact; }
    /**
     * Getter for the first coefficient array
     * @return the first coefficient array
     */
    public double[] getB1() { return b1; }
    /**
     * Getter for the 2nd coefficient array
     * @return the second coefficient array
     */
    public double[] getB2() { return b2; }
    /**
     * Getter for the lengths of pads applied to front and back of array for
     * acausal filtering
     * @return the pad length
     */
    public int getPadLength() { return (npad/2); }
    /**
     * Getter for the calculated taper length
     * @return the calculated taper length used in filtering
     */
    public double getTaperlength() { return (tapercount * dtime); }
    /**
     * Getter for the end taper length
     * @return the end taper length used in filtering
     */
    public double getEndTaperlength() { return (taperend * dtime); }
}
