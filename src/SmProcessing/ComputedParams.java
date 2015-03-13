/*******************************************************************************
 * Name: Java class ComputedParams.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmProcessing;

import static SmConstants.VFileConstants.FROM_G_CONVERSION;
import static SmConstants.VFileConstants.TO_G_CONVERSION;
import static SmProcessing.ArrayOps.convertArrayUnits;
import java.util.Arrays;

/**
 * The computed parameters class calculates the computed parameters for records
 * where the acceleration exceeds a threshold (default is 5% g) at any point.  
 * The values computed are bracketed duration, duration interval, Arias intensity, 
 * Housner intensity, channel RMS, and cumulative absolute velocity.
 * @author jmjones
 */
public class ComputedParams {
    private final double[] acc;
    private final double[] gaccsq;
    private double[] gacc;
    private final double dt;
    private int brackstart;
    private int brackend;
    private int durstart;
    private int durend;
    private final int len;
    private final double threshold;
    
    private double sumGaccsq;
    private double bracketedDuration;
    private double ariasIntensity;
    private double housnerIntensity;
    private double channelRMS;
    private double durationInterval;
    private double CAV;
    /**
     * Constructor for the Computed Parameters class - this method initializes
     * variables and calculates arrays for acceleration in g, squared acceleration
     * in g, and the sum/integration of squared acceleration in g.
     * @param inAcc acceleration in cm/sq.sec
     * @param dtime time interval between samples in seconds
     * @param inThreshold percentage of g to qualify as strong motion record
     */
    public ComputedParams(final double[] inAcc, double dtime, double inThreshold) {
        this.dt = dtime;
        this.acc = inAcc;
        this.len = inAcc.length;
        this.gacc = new double[len];
        this.gaccsq = new double[len];
        this.bracketedDuration = 0.0;
        this.ariasIntensity = 0.0;
        this.housnerIntensity = 0.0;
        this.channelRMS = 0.0;
        this.durationInterval = 0.0;
        this.CAV = 0.0;
        this.brackstart = 0;
        this.brackend = 0;
        this.durstart = 0;
        this.durend = 0;
        this.threshold = inThreshold / 100.0; //change from % to value
        
        //Get the acceleration in g for calculations
        gacc = convertArrayUnits(acc, TO_G_CONVERSION);
        
        //square acceleration in g
        for (int i = 0; i < len; i++) {
            gaccsq[i] = Math.pow(gacc[i],2);
        }
        //Get sum/integration of gaccsq, which is Ea
        this.sumGaccsq = 0.5 * (gaccsq[0] + gaccsq[len-1]) * dt;
        for (int i = 1; i < len-1; i++) {
            sumGaccsq = sumGaccsq + gaccsq[i]*dt;
        }
//        System.out.println("Ea: " + sumGaccsq);
    }
    /**
     * This method performs the calculations for the computed parameters.  It 
     * returns true if the bracketed duration calculation found at least one
     * value greater than the input threshold (5%g default), which indicates that 
     * the calculations were performed.  If
     * the return is false, all computed parameters are set at 0.
     * @return true if calculations performed, false if no strong motion detected
     */
    public boolean calculateComputedParameters() {
        
        // Bracketed Duration (secs over 5% g)
        boolean strongMotion = calculateBracketedDuration();
        if (!strongMotion) {
            return false;
        }       
        //Duration interval, (sec at 75% Arias I. - sec at 5% Arias I.)
        calculateDurationInterval();
        
        // Arias Intensity, units of m/sec, damping = 0.05
        ariasIntensity = (sumGaccsq * Math.PI / 2.0) * FROM_G_CONVERSION * 0.01;

        // Housner Intensity, units of g*g
        calculateHousnerIntensity();
        
        // RMS of channel, units of g
        channelRMS = Math.sqrt(housnerIntensity);
        
        //Cumulative absolute velocity, CAV (m/s)
        calculateCumulativeAbsVelocity();
        
        return true;
    }
    /**
     * Calculates the number of seconds that the acceleration is greater than the threshold.
     * Determines the difference in time between the first moment that 
     * acceleration is greater than threshold and the last moment that acc. is greater than threshold.  
     * If no values
     * in the array are greater than threshold, return is set to false and no computed 
     * parameters are calculated.
     * @return true if at least 1 value is greater than threshold, 
     * false if no values greater than threshold
     */
    private boolean calculateBracketedDuration() {
        //Check if any value is greater than strong motion threshold.  If not, no need to compute
        //parameters
        ArrayStats test = new ArrayStats(gacc);
        if (Math.abs(test.getPeakVal()) < threshold) {
            return false;
        }
        //Find the index of the first value > threshold
        for (int i = 0; i < len; i++) {
            if (Math.abs(gacc[i]) > threshold) {
                brackstart = i;
                break;
            }
        }
//        System.out.println("bracketed start: " + brackstart);
        
        //Now find the index where the last value < threshold
        for (int j = len-1; j >= brackstart; j--) {
            if (Math.abs(gacc[j]) > threshold) {
                brackend = j;
                break;
            }
        }
//        System.out.println("bracketed end: " + brackend);
        bracketedDuration = (brackend - brackstart) * dt;
        return true;
    }
    /**
     * Calculates the duration interval between 5-75% of Arias intensity.  Uses
     * the integral of the squared acceleration in g over the whole time period
     * for Arias intensity.  Finds the moment when the Arias intensity is 5% of
     * the total and the moment when the Arias intensity is 75% of the total.
     * The time intervals for duration start and end are saved for use in the
     * Housner intensity calculations.
     */
    private void calculateDurationInterval() {
        double IA75 = 0.75 * sumGaccsq;
        double IA05 = 0.05 * sumGaccsq;
        boolean found05 = false;
        boolean found75 = false;
        double t05 = 0.0;
        double t75 = 0.0;
        
        //Walk through the array calculating the running integral at each sample.
        //This is done by starting with 1/2 of the first value and adding in the
        //whole second value as the initial integral.  Now, for each remaining sample
        //in the array, first add 1/2 of the value (integral at that point) and 
        //check if the 5% or 75% value has been reached.  If not, add in another 
        //half of the current value and move to the next value.
        //When the integral reaches 5% of total and again when it reaches 
        //75% of the total set a flag and mark the value.
        double dsum = 0.5 * gaccsq[0] * dt + gaccsq[1] * dt;
        for (int i = 2; i < len; i++) {
            dsum = dsum + 0.5*gaccsq[i]*dt;
            if ((!found05) && (Math.abs(dsum - IA05) < (0.01*IA05))) {
                found05 = true;
                t05 = i * dt;
                durstart = i;
//                System.out.println("Ia1: " + i);
            } else if ((!found05) && (dsum > IA05)) {
                found05 = true;
                t05 = i * dt;
                durstart = i;
//                System.out.println("Ia1, condition 2: " + i);
            }
            if ((!found75) && (Math.abs(dsum - IA75) < (0.01*IA75))) {
                found75 = true;
                t75 = i * dt;
                durend = i;
//                System.out.println("Ia2: " + i);
            } else if ((!found75) && (dsum > IA75)) {
                found75 = true;
                t75 = i * dt;
                durend = i;
//                System.out.println("Ia2, condition 2: " + i);
            }
            if (found05 && found75) {
                break;
            } else {
                dsum = dsum + 0.5*gaccsq[i]*dt;      
            }
        }
        durationInterval = t75 - t05;
    }
    /**
     * Calculate Housner Intensity, using the start and end values determined
     * from the duration interval calculations.
     */
    private void calculateHousnerIntensity() {
        double hsum = 0.5 * (gaccsq[durstart] + gaccsq[durend]) * dt;
        for (int i = durstart+1; i < durend; i++) {
            hsum = hsum + gaccsq[i]*dt;
        }
        if (durend > durstart) {
            housnerIntensity = hsum / ((durend-durstart)*dt);
        } else {
            housnerIntensity = 0.0;
        }
    }
    /**
     * Calculate the cumulative absolute velocity, using only the 1-second intervals
     * where the abs value of acceleration exceeds 0.025 g at least once in the
     * interval.
     */
    private void calculateCumulativeAbsVelocity() {
        int step = (int)(1.0 / dt);
        int numsecs = (int)Math.ceil(dt*len);
        int upperlim;
        int ctr = 0;
        boolean[] intervals = new boolean[numsecs];
        Arrays.fill(intervals, 0, numsecs, false);
        
        //First build an array of boolean flags for each 1-sec interval in the
        //acceleration array.  For each, 1-sec. step, if any value exceeds
        //0.025g, set the flag for that interval to true.
        for (int k = 0; k < len; k = k + step) {
            upperlim = ((k+step) <= len) ? k+step : len;
            for (int i = k; i < upperlim; i++) {
                if (Math.abs(gacc[i]) > 0.025) {
                    intervals[ctr] = true;
                    break;
                }
            }
            ctr++;
        }
        //Now go through the array again and for each interval where the flag
        //is true, sum/integrate the abs. acceleration to get the velocity total
        //for that interval.  Add the velocity total to the running cumulative
        //total.
        ctr = 0;
        double sum = 0.0;
        for (int k = 0; k < len; k = k + step) {
            if (intervals[ctr]) {
                upperlim = ((k+step) <= len) ? k+step : len;
                sum = 0.5 * Math.abs(acc[k] + acc[upperlim-1]) * 0.01 * dt;
                for (int i = k+1; i < upperlim-1; i++) {
                    sum = sum + Math.abs(acc[i]) * 0.01 * dt;
                }
                CAV = CAV + sum;
                sum = 0;
            }
            ctr++;            
        }
    }
    /**
     * Getter for the bracketed duration
     * @return the bracketed duration
     */
    public double getBracketedDuration() {
        return this.bracketedDuration;
    }
    /**
     * Getter for the Arias intensity
     * @return the Arias intensity
     */
    public double getAriasIntensity() {
        return this.ariasIntensity;
    }
    /**
     * Getter for the Housner intensity
     * @return the Housner intensity
     */
    public double getHousnerIntensity() {
        return this.housnerIntensity;
    }
    /**
     * Getter for the channel RMS
     * @return the channel RMS
     */
    public double getChannelRMS() {
        return this.channelRMS;
    }
    /**
     * Getter for the duration interval
     * @return the duration interval
     */
    public double getDurationInterval() {
        return this.durationInterval;
    }
    /**
     * Getter for the cumulative absolute velocity
     * @return the cumulative absolute velocity
     */
    public double getCumulativeAbsVelocity() {
        return this.CAV;
    }
}