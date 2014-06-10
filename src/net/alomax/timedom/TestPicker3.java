/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2006 Anthony Lomax <anthony@alomax.net www.alomax.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */





package net.alomax.timedom;

import net.alomax.math.*;
import net.alomax.util.PhysicalUnits;

import java.util.Vector;



public class TestPicker3 extends BasicPicker {
    
    // AJL need long term window to limit npts used to update stats
    private static final double WINDOW_MIN = Double.MIN_VALUE;
    private static final double WINDOW_MAX = Double.MAX_VALUE;
    public double longTermWindow = 10.0;
    
    public double threshold1 = 10.0;    // threshold to intiatie trigger
    public double threshold2 = 10.0;       // threshold to maintain trigger
    public double tUpEvent = 1.0;
    public double filterWindow = 2.0;
    
    public String errorMessage;
    
    private static final double THRESHOLD_MIN = Double.MIN_VALUE;
    private static final double THRESHOLD_MAX = Double.MAX_VALUE;
    private static final double TIME_MIN = -Double.MAX_VALUE;
    private static final double TIME_MAX = Double.MAX_VALUE;
    
    private static final int INT_UNSET = -Integer.MAX_VALUE / 2;
    
    // instance variables needed for memory
    private TestPicker3_Memory mem = null;
    private double deltaTime;
    
    
    /** constructor */
    
    public TestPicker3(String localeText, double longTermWindow, double threshold1, double threshold2, double tUpEvent, double filterWindow, int direction) {
        
        super(localeText, direction);
        
        this.longTermWindow = longTermWindow;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
        this.tUpEvent = tUpEvent;
        this.filterWindow = filterWindow;
        
    }
    
    
    
    /** copy constructor */
    
    public TestPicker3(TestPicker3 tp) {
        
        super(tp.direction);
        
        this.resultType = tp.resultType;
        
        this.longTermWindow = tp.longTermWindow;
        this.threshold1 = tp.threshold1;
        this.threshold2 = tp.threshold2;
        this.tUpEvent = tp.tUpEvent;
        this.filterWindow = tp.filterWindow;
        
    }
    
    
    
    
    
    
    /** Method to set longTermWindowValue */
    
    public void setLongTermWindow(double longTermWindowValue) throws TimeDomainException {
        if (longTermWindowValue < WINDOW_MIN || longTermWindowValue > WINDOW_MAX) {
            throw new TimeDomainException(
            TimeDomainText.invalid_long_term_window_value + ": " + longTermWindowValue);
        }
        
        longTermWindow = longTermWindowValue;
    }
    
    
    /** Method to set longTermWindowValue */
    
    public void setLongTermWindow(String str) throws TimeDomainException {
        
        double longTermWindowValue;
        
        try {
            longTermWindowValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_long_term_window_value + ": " + str);
        }
        
        setLongTermWindow(longTermWindowValue);
    }
    
    
    
    
    /** Method to set threshold1Value */
    
    public void setThreshold1(double threshold1Value) throws TimeDomainException {
        if (threshold1Value < THRESHOLD_MIN || threshold1Value > THRESHOLD_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_threshold1_value + ": " + threshold1Value);
        }
        
        threshold1 = threshold1Value;
    }
    
    
    /** Method to set threshold1Value */
    
    public void setThreshold1(String str) throws TimeDomainException {
        
        double threshold1Value;
        
        try {
            threshold1Value = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_threshold1_value + ": " + str);
        }
        
        setThreshold1(threshold1Value);
    }
    
    
    
    /** Method to set threshold2Value */
    
    public void setThreshold2(double threshold2Value) throws TimeDomainException {
        if (threshold2Value < THRESHOLD_MIN || threshold2Value > THRESHOLD_MAX) {
            throw new TimeDomainException(
            TimeDomainText.invalid_threshold2_value + ": " + threshold2Value);
        }
        
        threshold2 = threshold2Value;
    }
    
    
    /** Method to set threshold2Value */
    
    public void setThreshold2(String str) throws TimeDomainException {
        
        double threshold2Value;
        
        try {
            threshold2Value = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_threshold2_value + ": " + str);
        }
        
        setThreshold2(threshold2Value);
    }
    
    
    
    /** Method to set tUpEventValue */
    
    public void setTUpEvent(double tUpEventValue) throws TimeDomainException {
        if (tUpEventValue < TIME_MIN || tUpEventValue > TIME_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_tUpEvent_value + ": " + tUpEventValue);
        }
        
        tUpEvent = tUpEventValue;
    }
    
    
    /** Method to set tUpEventValue */
    
    public void setTUpEvent(String str) throws TimeDomainException {
        
        double tUpEventValue;
        
        try {
            tUpEventValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_tUpEvent_value + ": " + str);
        }
        
        setTUpEvent(tUpEventValue);
    }
    
    
    
    
    /** Method to set meanWindowValue */
    
    public void setMeanWindow(double meanWindowValue) throws TimeDomainException {
        if (meanWindowValue < WINDOW_MIN || meanWindowValue > WINDOW_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_meanWindow_value + ": " + meanWindowValue);
        }
        
        filterWindow = meanWindowValue;
    }
    
    
    /** Method to set meanWindowValue */
    
    public void setMeanWindow(String str) throws TimeDomainException {
        
        double meanWindowValue;
        
        try {
            meanWindowValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_meanWindow_value + ": " + str);
        }
        
        setMeanWindow(meanWindowValue);
    }
    
    
    
    
    
    /** Method to check settings */
    
    public void checkSettings() throws TimeDomainException {
        
        super.checkSettings();
        
        String errMessage = "";
        int badSettings = 0;
        
        if (longTermWindow < WINDOW_MIN || longTermWindow > WINDOW_MAX) {
            errMessage += ": " + TimeDomainText.invalid_long_term_window_value;
            badSettings++;
        }
        
        
        if (threshold1 < THRESHOLD_MIN || threshold1 > THRESHOLD_MAX) {
            errMessage += ": " + TimeDomainText.invalid_threshold1_value;
            badSettings++;
        }
        if (threshold2 < THRESHOLD_MIN || threshold2 > THRESHOLD_MAX) {
            errMessage += ": " + TimeDomainText.invalid_threshold2_value;
            badSettings++;
        }
        if (tUpEvent < TIME_MIN || tUpEvent > TIME_MAX) {
            errMessage += ": " + TimeDomainText.invalid_tUpEvent_value;
            badSettings++;
        }
        if (filterWindow < TIME_MIN || filterWindow > TIME_MAX) {
            errMessage += ": " + TimeDomainText.invalid_meanWindow_value;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }
        
    }
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
        super.updateFields(timeSeries);
        
    }
    
    
    
    
    /*** function to calculate picks  */
    
    public final float[] apply(double dt, float[] sample) {
        
        // initialize instance variables needed for memory
        deltaTime = dt;
        // initialize memory object
        if (mem == null) {
            mem = new TestPicker3_Memory(sample);
        }
        
        // create array for time-series results
        float[] sampleNew = null;
        if (resultType == TRIGGER || resultType == CHAR_FUNC) {
            sampleNew = new float[sample.length];
            //sampleNew[0] = sample[sample.length - 1] = 0.0f;
        }
        
        
        
        //**AJL for (int n = 1; n < sample.length - 1; n++) {
        for (int n = 0; n < sample.length; n++) {
            
            // $DOC when not in active trigger event, use current long-term statistics for characteristic function
            // $DOC when in active trigger event, use long-term statistics from pre-trigger for characteristic function
            if (!mem.inTriggerEvent) {  // not in pick, update active stats
                for (int k = 0; k < mem.numRecursive; k++) {
                    mem.mean_xRec_ACTIVE[k] = mem.mean_xRec[k];
                    mem.mean_stdDev_xRec_ACTIVE[k] = mem.mean_stdDev_xRec[k];
                }
            }
            
            
            // $DOC characteristic function is  (E2 - mean_E2_ACTIVE) / mean_stdDev_E2_ACTIVE
            double charFunct = 0.0;
            double period = 0.0;
            // $DOC evaluate current signal values
            float currentSample = sample[n];
            mem.index = (mem.index + 1) % mem.numPrevious;
            mem.indexLast = (mem.indexLast + 1) % mem.numPrevious;
            // $DOC loop over numRecursive
            mem.filteredSample[mem.numRecursive - 1][mem.index] = mem.filteredSample[mem.numRecursive - 1][mem.indexLast]
            * mem.decayConst[mem.numRecursive - 1] + currentSample * mem.decayFactor[mem.numRecursive - 1];
            for (int k = mem.numRecursive - 2; k >= 0; k--) {
                mem.filteredSample[k][mem.index]
                = mem.filteredSample[k][mem.indexLast] * mem.decayConst[k] + currentSample * mem.decayFactor[k];
                // XXX
                //**AJL int iDelay = (mem.numPrevious + n - mem.indexDelay[k + 1]) % mem.numPrevious;
                int iDelay = (2 * mem.numPrevious + mem.index - mem.indexDelay[k + 1]) % mem.numPrevious;
                // XXX
                //System.out.println("k,mem.index,mem.indexLast,iDelay " + k+" "+mem.index+" "+mem.indexLast+" "+iDelay);
                mem.xRec[k] = mem.filteredSample[k][mem.index] - mem.filteredSample[k + 1][iDelay];
                /* TEST */ //mem.test[k] = mem.xRec[k];
                mem.xRec[k] *= mem.xRec[k];
                if (mem.inTriggerEvent || n > mem.indexUpEventEnd + mem.indexDelay[k + 1]) { // do not update charFunct if < delay beyond end of last accepted trigger event
                    if (mem.mean_stdDev_xRec_ACTIVE[k] > Float.MIN_VALUE) {
                        double charFunctTest = (mem.xRec[k] - mem.mean_xRec_ACTIVE[k]) / mem.mean_stdDev_xRec_ACTIVE[k];
                        // AJL 20080201 - test to boost sensitivity to longer period onsets
                        //charFunctTest *= Math.sqrt(k + 1);
                        //
                        if (charFunctTest > charFunct) {
                            charFunct = charFunctTest;
                            period = dt / mem.decayFactor[k];
                        }
                    }
                }
                //}
            }
            
            
            // check characteristic function value
            
            // uncertainty logic
            mem.charFunctUncertainty = (charFunct + mem.charFunctLast1 + mem.charFunctLast2) / 3.0;   // 3 point smoothing to avoid sample oscillation
            mem.charFunctLast2 = mem.charFunctLast1;
            mem.charFunctLast1 = charFunct;  // $$$ ADDED charFunctUncertainty
            if (mem.charFunctUncertainty > mem.uncertaintyThreshold) {
                // $DOC each time characteristic function rises past uncertaintyThreshold, if not in trigger event, store sample index and initiate polarity algoirithm
                if (mem.indexUncertainty == INT_UNSET) {
                    mem.indexUncertainty = n - 1;
                    //mem.amplitudeUncertainty = sample[mem.indexUncertainty];
                    mem.amplitudeUncertainty = mem.lastSample;
                    // $DOC initialize polarity algorithm, uses count of sign of derivative of signal
                    mem.amplitudePolarity = mem.amplitudeUncertainty;
                    // countPolarity = dXdt > 0.0 ? 1 : -1; // 200606222 AJL
                    mem.countPolarity = 0;
                }
            } else if (!mem.inTriggerEvent) {
                // $DOC if characteristic function is below uncertaintyThreshold, and if not in trigger event, unset uncertainty sample index
                mem.indexUncertainty = INT_UNSET;
            }
            // $DOC if characteristic function is above uncertaintyThreshold, and if not in trigger event, accumulate count of sign of derivative for polarity estimate
            if (mem.indexUncertainty != INT_UNSET && !mem.inTriggerEvent) {   // accumulate count of polarity between uncertainty point and trigger point
                mem.amplitudePolarity = Math.abs(currentSample - mem.amplitudeUncertainty)
                > Math.abs(mem.amplitudePolarity) ? currentSample - mem.amplitudeUncertainty : mem.amplitudePolarity;
                mem.countPolarity += currentSample - mem.lastSample > 0.0 ? 1 : -1;
            }
            
            // trigger and pick logic
            if (!mem.allowTriggerEvent)
                mem.nTotal++;
            // $DOC only apply trigger and pick logic if past long-term stabilisation time (longTermWindow)
           if (mem.allowTriggerEvent || mem.nTotal > mem.indexAllowTriggerEvent) {  // past long-term stabilisation time and numPrevious past last end event
                
                //if (!mem.allowTriggerEvent)
                //    System.out.println(">>>>>>>>> allowTriggerEvent");
                mem.allowTriggerEvent = true;
                
                // upEventCharFunctValue    // $$$ ADDED
                // $DOC update index of UpEvent length buffer of charFunct values, subtract oldest value, and save provisional current sample charFunct value
                mem.upEventIndex = (mem.upEventIndex + 1) % mem.nTUpEvent;
                mem.integralCharFunct -= mem.upEventCharFunctValue[mem.upEventIndex];
                mem.upEventCharFunctValue[mem.upEventIndex] = charFunct;
                
                if (mem.inTriggerEvent) {
                    //System.out.println(">>>>>>>>> inTriggerEvent");
                    // $DOC if in trigger event, accumlated sum of characteristic function values (integralCharFunct)
                    // $DOC to prevent triggering on spikes, limit characteristic function value to threshold1 for first 2 triggered samples
                    if (n > mem.indexUpEvent + 1) {    // past 2nd sample after trigger, to avoid spikes
                        mem.integralCharFunct += charFunct;
                    } else {   // to avoid spikes, do not include full charFunct value, may be very large
                        double value = charFunct < 5.0 * threshold1 ? charFunct : 5.0 * threshold1;
                        mem.integralCharFunct += value;
                        mem.upEventCharFunctValue[mem.upEventIndex] = value;
                    }
                    //System.out.println("" + (n - indexUpEvent) + " " + n + " " + charFunct + " " + integralCharFunct + " / " + criticalIntegralCharFunct);
                    // $DOC save accumlated sum of characteristic function values (ampCharFunctSum) as indicator of pick strenth
                    mem.ampCharFunctSum += charFunct;
                    if (n <= mem.indexUpEventEnd) {     // before end of tUpEvent window
                        // $DOC if in trigger event and before or at end of tUpEvent window, check if integralCharFunct >= criticalIntegralCharFunct, if so, declare pick
                        if (!mem.willAcceptPick && mem.integralCharFunct >= mem.criticalIntegralCharFunct) {
                            mem.willAcceptPick = true;
                            //System.out.println(">>>>>>>>> willAcceptPick");
                        }
                    } else {    // reached end of tUpEvent window
                        // $DOC accept pick
                        if (mem.willAcceptPick) {
                            mem.acceptedPick = true;
                            //System.out.println(">>>>>>>>> acceptedPick");
                            // set previous samples to their mean value to avoid post-picking an event inside last event window
                            /*double dmean = 0.0;
                            for (int k = 0; k < numPrevious; k++)
                                dmean += previousSamples[k];
                            dmean /= (double) numPrevious;
                            for (int k = 0; k < numPrevious; k++)
                                previousSamples[k] = dmean;
                             */
                            //indexAllowTriggerEvent = n + numPrevious;
                        } else {
                            mem.indexUpEventEnd = INT_UNSET;   // prevents disabling of charFunct update
                        }
                        mem.willAcceptPick = false;
                        // $DOC set flag to indicate not in trigger event
                        mem.inTriggerEvent = false;
                    }
                } else if (charFunct >= threshold1) {  // over threshold, start pick event - triggered
                   //System.out.println(">>>>>>>>> charFunct >= threshold1");
                    // $DOC if not in trigger event and characteristic function > threshold1, declare trigger event
                    mem.inTriggerEvent = true;
                    // $DOC initialize ampCharFunctSum (=charFunct)
                    mem.ampCharFunctSum = charFunct;
                    // $DOC set index for trigger event begin and end (= begin + nTUpEvent)
                    mem.indexUpEvent = n;
                    mem.indexUpEventEnd = n + mem.nTUpEvent;
                    //integralCharFunct = 0.0; // do not include trigger value, may be very large
                    // $DOC initialize integralCharFunct (=threshold1 to avoid large values and to prevent triggering on spikes)
                    double value = charFunct < 5.0 * threshold1 ? charFunct : 5.0 * threshold1;
                    mem.integralCharFunct += value; // do not include full charFunct value, may be very large
                    mem.upEventCharFunctValue[mem.upEventIndex] = threshold1;
                    // $DOC evaluate polarity based on accumulate count of sign of derivative (=POS if count > 1; = NEG if count < -1, UNK otherwise)
                    if (mem.countPolarity > 1)
                        mem.pickPolarity = PickData.POLARITY_POS;
                    else if (mem.countPolarity < -1)
                        mem.pickPolarity = PickData.POLARITY_NEG;
                    else
                        mem.pickPolarity = PickData.POLARITY_UNKNOWN;
                    mem.triggerPeriod = period;
                } else {  // under threshold, accumulate integral
                    mem.integralCharFunct += charFunct;
                }
            }
            
            
            // $DOC update "true", long-term statistic based on current signal values based on long-term window
            // long-term decay formulation
            // $DOC update long-term means of x, dxdt, E2, var(E2)
            for (int k = 0; k < mem.numRecursive; k++) {
                mem.mean_xRec[k] = mem.mean_xRec[k] * mem.longDecayConst + mem.xRec[k] * mem.longDecayFactor;
                double dev = mem.xRec[k] - mem.mean_xRec[k];
                mem.mean_var_xRec[k] = mem.mean_var_xRec[k] * mem.longDecayConst + dev * dev * mem.longDecayFactor;
                // $DOC mean_stdDev_E2 is sqrt(long-term mean var(E2))
                mem.mean_stdDev_xRec[k] = Math.sqrt(mem.mean_var_xRec[k]);
            }
            
            
            // act on result
            
            if (resultType == TRIGGER) {	// show triggers
                if (mem.acceptedPick)
                    sampleNew[n] = 1.0f;
                else
                    sampleNew[n] = 0.0f;
                // TEST...
                //sampleNew[n] = (float) mem.test[0];
                //sampleNew[n] = (float) test[numRecursive - 2];
            } else if (resultType == CHAR_FUNC) {	    // show char function
                sampleNew[n] = (float) charFunct;
                //sample[n] = (float) integralCharFunct;
            } else {                // generate picks
                // PICK
                if (mem.acceptedPick) {
                    // $DOC if pick declared, save pick time, uncertainty, strength (ampCharFunctSum) and polarity
                    // $DOC pick time is mean of time of last uncertainty threshold (characteristic function rose past uncertaintyThreshold) and trigger time (characteristic function >= threshold1)
                    // $DOC pick uncertainty is from time of last uncertainty threshold to trigger time
                    PickData pickData = new PickData((double) mem.indexUncertainty, (double) mem.indexUpEvent,
                    mem.pickPolarity, Math.log(mem.ampCharFunctSum / mem.criticalIntegralCharFunct),
                    PickData.CHAR_FUNCT_AMP_UNITS, mem.triggerPeriod);
                    triggerPickData.add(pickData);
                    //System.out.println("triggerPickData.add(pickData):  " + pickData);
                }
            }
            
            if (mem.acceptedPick)
                mem.indexUncertainty = INT_UNSET;
            
            mem.acceptedPick = false;
            
            mem.lastSample = currentSample;
            
        }
        
        
        if (useMemory) {
            // corect memory index values for sample length
            mem.indexUncertainty -= sample.length;
            mem.indexUpEvent -= sample.length;
            mem.indexUpEventEnd -= sample.length;
            //mem.indexAllowTriggerEvent -= sample.length;
            //mem.upEventIndex -= sample.length;
        } else {
            mem = null;
        }
        
        
        if (resultType == TRIGGER || resultType == CHAR_FUNC)
            sample = sampleNew;
        
        return(sample);
        
    }
    
    
    
    /** Returns true if this process supports memory usage
     *
     * @return    true if this process supports memory usage.
     */
    
    public boolean supportsMemory() {
        
        return(true);
        
    }
    
    
    
    /** custom memory class */
    
    public class TestPicker3_Memory extends TimeDomainMemory {
        
        
        // alomax test stuff
        //double uncertaintyThreshold = threshold1 / 2.0;
        //double uncertaintyThreshold = 0.5 * this.threshold2;
        double uncertaintyThreshold = 2.0;
        int indexUncertainty = INT_UNSET;
        double amplitudeUncertainty = 0.0;
        double amplitudePolarity = 0.0;
        int countPolarity = 0;
        
        //boolean backwards = (direction == -1);
        
        double longDecayFactor = deltaTime / longTermWindow;
        double longDecayConst = 1.0 - longDecayFactor;
        int nLongTermWindow = 1 + (int) (longTermWindow / deltaTime);
        //double longTermMean = 0.0;
        int indexAllowTriggerEvent = nLongTermWindow;
        boolean allowTriggerEvent = false;
        int nTotal = -1;
        
        
        int numPrevious = (int) (filterWindow * 2.0 / deltaTime);  // estimate of number of previous samples to bufer
        int numRecursive = 1;   // number of powers of 2 to process
        int nTemp = 1; {
            while (nTemp < numPrevious) {
                numRecursive++;
                nTemp *= 2;
            }
            numPrevious = nTemp;    // numPrevious is now a power of 2
            //System.out.println("numRecursive, numPrevious " + numRecursive + " " + numPrevious);
        }
        double[] xRec = new double[numRecursive];
        double[] test = new double[numRecursive];
        double[][] filteredSample = new double[numRecursive][numPrevious];
        double[] mean_xRec = new double[numRecursive];
        double[] mean_xRec_ACTIVE = new double[numRecursive];
        double[] mean_stdDev_xRec = new double[numRecursive];
        double[] mean_stdDev_xRec_ACTIVE = new double[numRecursive];
        double[] mean_var_xRec = new double[numRecursive];
        double[] decayFactor = new double[numRecursive];
        double[] decayConst = new double[numRecursive];
        int[] indexDelay = new int[numRecursive];
        
        int index = -1;
        int indexLast = numPrevious - 2;
        
        double window = deltaTime;
        int nDelay = 1;
        
        {
            for (int k = 0; k < numRecursive; k++) {
                mean_xRec[k] = 0.0;
                mean_stdDev_xRec[k] = 0.0;
                decayFactor[k] = deltaTime / window;
                decayConst[k] = 1.0 - decayFactor[k];
                indexDelay[k] = nDelay + 1;
                window *= 2.0;
                nDelay *= 2;
            }
        }
        
        
        double lastSample = 0.0;
        boolean dataGap = false;
        
        
        //double charFunct = 0.0;
        double charFunctTrue = 0.0;
        double charFunctUncertainty = 0.0;
        double charFunctLast1 = 0.0;
        double charFunctLast2 = 0.0;
        
        boolean inTriggerEvent = false;
        double ampCharFunctSum = 0.0;
        
        int indexUpEvent = INT_UNSET;
        int indexUpEventEnd = INT_UNSET;   // prevents disabling of charFunct update
        //int indexDownCount = INT_UNSET;
        int nTUpEvent = (int) (0.5 + tUpEvent / deltaTime) - 1; {
            if (nTUpEvent < 1)
                nTUpEvent = 1;
        }
        // $DOC criticalIntegralCharFunct is tUpEvent * threshold2
        double criticalIntegralCharFunct = (double) (nTUpEvent) * threshold2;   // one less than number of samples examined
        
        // $DOC integralCharFunct is integral of charFunct values for last nTUpEvent samples, charFunct values possibly limited if around trigger time
        double integralCharFunct = 0.0;
        
        double[] upEventCharFunctValue = new double[nTUpEvent];    // $$$ ADDED
        //for (int n = 0; n < nTUpEvent; n++)
        //    upEventCharFunctValue[n] = 0.0;
        int upEventIndex = -1;
        
        boolean acceptedPick = false;
        boolean willAcceptPick = false;
        int pickPolarity = PickData.POLARITY_UNKNOWN;
        //double period = 0.0;
        double triggerPeriod = 0.0;
        
        int nvar = 0;
        
        
        
        
        
        public TestPicker3_Memory(float[] sample) {
            
            // initialize previous samples to first sample value
            
            //longTermMean = sample[0];
            for (int k = 0; k < numRecursive; k++)
                for (int j = 0; j < numPrevious; j++)
                    filteredSample[k][j] = sample[0];
            
            
            System.out.println("TestPicker3_Memory initialized: numRecursive, numPrevious " + numRecursive + " " + numPrevious);
            
            
        }
        
    }
    
    
    
    
    
}	// End class


