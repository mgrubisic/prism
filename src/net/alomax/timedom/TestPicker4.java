/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2006-2008 Anthony Lomax <anthony@alomax.net www.alomax.net>
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


// _DOC_ =============================
// _DOC_ TestPicker4
// _DOC_ =============================

public class TestPicker4 extends BasicPicker {
    
    public double filterWindow = 4.0;
    // _DOC_ the filter window (filterWindow) in seconds determines how far back in time the previous samples are examined.  The filter window will be adjusted upwards to be an integer N power of 2 times the sample interval (deltaTime).  Then numRecursive = N + 1 "filter bands" are created.  For each filter band n = 0,N  the data samples are processed through a simple recursive filter backwards from the current sample, and picking statistics and characteristic function are generated.  Picks are generated based on the maximum of the characteristic funciton values over all filter bands relative to the threshold values threshold1 and threshold2.
    
    // AJL need long term window to limit npts used to update stats
    private static final double WINDOW_MIN = Double.MIN_VALUE;
    private static final double WINDOW_MAX = Double.MAX_VALUE;
    public double longTermWindow = 10.0;
    // _DOC_ the long term window (longTermWindow) determines: a) a stabilisation delay time after the beginning of data; before this delay time picks will not be generated. b) the decay constant of a simple recursive filter to accumlate/smooth all picking statistics and characteristic functions for all filter bands.
    
    public double threshold1 = 8.0;    // threshold to intiatie trigger
    // _DOC_ threshold1 sets the threshold to trigger a pick event (potential pick).  This threshold is reached when the (clipped) characteristic function for any filter band exceeds threshold1.
    public double threshold2 = 8.0;       // threshold to maintain trigger
    // _DOC_ threshold2 sets the threshold to declare a pick (pick will be accepted when tUpEvent reached).  This threshold is reached when the integral of the (clipped) characteristic function for any filter band over the window tUpEvent exceeds threshold2 * tUpEvent (i.e. the average (clipped) characteristic function over tUpEvent is greater than threshold2)..
    public double tUpEvent = 0.5;
    // _DOC_ tUpEvent determines the maximum time the integral of the (clipped) characteristic function is accumlated after threshold1 is reached (pick event triggered) to check for this integral exceeding threshold2 * tUpEvent (pick declared).
    
    public String errorMessage;
    
    private static final double THRESHOLD_MIN = Double.MIN_VALUE;
    private static final double THRESHOLD_MAX = Double.MAX_VALUE;
    private static final double TIME_MIN = -Double.MAX_VALUE;
    private static final double TIME_MAX = Double.MAX_VALUE;
    
    private static final int INT_UNSET = -Integer.MAX_VALUE / 2;
    
    // instance variables needed for memory
    private TestPicker4_Memory mem = null;
    // _DOC_ a memory structure/object is used so that this function can be called repetedly for packets of data in sequence from the same channel.
    
    private double deltaTime;
    
    
    
    
    /** constructor */
    
    public TestPicker4(String localeText, double longTermWindow, double threshold1, double threshold2,
    double tUpEvent, double filterWindow, int direction) {
        
        super(localeText, direction);
        
        this.longTermWindow = longTermWindow;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
        this.tUpEvent = tUpEvent;
        this.filterWindow = filterWindow;
        
    }
    
    
    
    /** copy constructor */
    
    public TestPicker4(TestPicker4 tp) {
        
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
    
    
    
    
    /** Method to set filterWindowValue */
    
    public void setMeanWindow(double filterWindowValue) throws TimeDomainException {
        if (filterWindowValue < WINDOW_MIN || filterWindowValue > WINDOW_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_filterWindow_value + ": " + filterWindowValue);
        }
        
        filterWindow = filterWindowValue;
    }
    
    
    /** Method to set filterWindowValue */
    
    public void setMeanWindow(String str) throws TimeDomainException {
        
        double filterWindowValue;
        
        try {
            filterWindowValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_filterWindow_value + ": " + str);
        }
        
        setMeanWindow(filterWindowValue);
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
            errMessage += ": " + TimeDomainText.invalid_filterWindow_value;
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
        
        // _DOC_ =============================
        // _DOC_ apply algoritm
        
        // initialize instance variables needed for memory
        deltaTime = dt;
        // initialize memory object
        if (mem == null) {
            mem = new TestPicker4_Memory(sample);
        }
        
        // create array for time-series results
        float[] sampleNew = null;
        if (resultType == TRIGGER || resultType == CHAR_FUNC) {
            sampleNew = new float[sample.length];
            //sampleNew[0] = sample[sample.length - 1] = 0.0f;
        }
        
        // _DOC_ set clipped limit of maximum char funct value to 5 * threshold1 to avoid long recovery time after strong events
        double maxCharFunctValue = 5.0 * threshold1;
        
        
        // _DOC_ loop over all samples
        for (int n = 0; n < sample.length; n++) {
            
            // _DOC_ characteristic function is  (E2 - mean_E2) / mean_stdDev_E2
            // _DOC_    where E2 = (filtered band value current - filtered band value previous)**2
            // _DOC_    where value previous is taken futher back for longer filter bands
            double charFunct = 0.0;
            double charFunctClipped = 0.0;
            int charFuntNumRecursiveIndex = -1;
            // _DOC_ evaluate current signal values
            float currentSample = sample[n];
            mem.numPreviousPtr = (mem.numPreviousPtr + 1) % mem.numPrevious;
            mem.numPreviousPtrLast = (mem.numPreviousPtrLast + 1) % mem.numPrevious;
            // _DOC_ loop over numRecursive filter bands
            for (int k = mem.numRecursive - 1; k >= 0; k--) {
                mem.filteredSample[k][mem.numPreviousPtr]
                = mem.filteredSample[k][mem.numPreviousPtrLast] * mem.decayConst[k] + currentSample * mem.decayFactor[k];
                int iDelay = (2 * mem.numPrevious + mem.numPreviousPtr - mem.indexDelay[k]) % mem.numPrevious;
                mem.xRec[k] = mem.filteredSample[k][mem.numPreviousPtr] - mem.filteredSample[k][iDelay];
                /* TEST */ //
                //mem.test[k] = mem.xRec[k];
                //mem.test[k] = mem.filteredSample[k][mem.numPreviousPtr];
                //mem.test[k] = mem.filteredSample[k][iDelay];
                //
                mem.xRec[k] *= mem.xRec[k];
                if (mem.mean_stdDev_xRec[k] <= Float.MIN_VALUE) {
                    if (mem.enableTriggering)
                        System.out.println("ERROR: mem.mean_stdDev_xRec[k] <= Float.MIN_VALUE (this should not happen!)");
                } else {
                    double charFunctTest = (mem.xRec[k] - mem.mean_xRec[k]) / mem.mean_stdDev_xRec[k];
                    double charFunctClippedTest = charFunctTest;
                    // _DOC_ limit maximum char funct value to avoid long recovery time after strong events
                    if (charFunctClippedTest > maxCharFunctValue) {
                        charFunctClippedTest = maxCharFunctValue;
                        // save corrected mem.xRec[k]
                        mem.xRec[k] = maxCharFunctValue * mem.mean_stdDev_xRec[k] + mem.mean_xRec[k];
                    }
                    // _DOC_ characteristic function is maximum over numRecursive filter bands
                    if (charFunctTest > charFunct) {
                        charFunct = charFunctTest;
                        charFunctClipped = charFunctClippedTest;
                        charFuntNumRecursiveIndex = k;
                    }
                }
            }
            
            // uncertainty and polarity logic
            mem.charFunctUncertainty = (charFunctClipped + mem.charFunctLast1) / 2.0;   // 2 point smoothing to avoid sample oscillation
            // _DOC_ uncertaintyThreshold is at minimum of 2-point smoothed char function or char funct increases past uncertaintyThreshold
            boolean doResetCharFunctUncertainty =
            ((mem.charFunctLast1Smooth < mem.charFunctLast2Smooth) && (mem.charFunctLast1Smooth < mem.charFunctUncertainty))
            || ((charFunctClipped >= mem.uncertaintyThreshold) && (mem.charFunctLast1 < mem.uncertaintyThreshold));
            if (doResetCharFunctUncertainty)
                mem.uncertaintyAtResetCharFunctUncertainty = mem.charFunctUncertainty;
            mem.charFunctLast2 = mem.charFunctLast1;
            mem.charFunctLast1 = charFunctClipped;  // $$$ ADDED charFunctUncertainty
            mem.charFunctLast2Smooth = mem.charFunctLast1Smooth;
            mem.charFunctLast1Smooth = mem.charFunctUncertainty;  // $$$ ADDED charFunctUncertainty
            if (doResetCharFunctUncertainty || mem.charFunctUncertainty > mem.uncertaintyAtResetCharFunctUncertainty) {
                // _DOC_ each time characteristic function rises past uncertaintyThreshold, if not in trigger event, store sample index and initiate polarity algoirithm
                if (doResetCharFunctUncertainty || mem.indexUncertainty == INT_UNSET) {
                    if (!mem.inTriggerEvent) {
                        mem.indexUncertainty = n - 1;
                        //mem.amplitudeUncertainty = sample[mem.indexUncertainty];
                        mem.amplitudeUncertainty = mem.lastSample;
                        // _DOC_ initialize polarity algorithm, uses count of sign of derivative of signal
                        mem.countPolarity = 0;
                    }
                }
            } else if (!mem.inTriggerEvent) {
                // _DOC_ if characteristic function is below uncertaintyThreshold, and if not in trigger event, unset uncertainty sample index
                mem.indexUncertainty = INT_UNSET;
            }
            // _DOC_ if characteristic function is above uncertaintyThreshold, and if not in trigger event, accumulate count of sign of derivative for polarity estimate
            int polarityIncrement = 0;
            if (mem.indexUncertainty != INT_UNSET && !mem.inTriggerEvent) {   // accumulate count of polarity between uncertainty point and trigger point
                polarityIncrement = currentSample - mem.lastSample > 0.0 ? 1 : -1;
                mem.countPolarity += polarityIncrement;
            }
            
            
            // check characteristic function charFunctClipped
            
            // trigger and pick logic
            // _DOC_ only apply trigger and pick logic if past stabilisation time (longTermWindow)
            if (mem.enableTriggering || mem.nTotal++ > mem.indexEnableTriggering) {  // past stabilisation time
                
                mem.enableTriggering = true;
                
                // _DOC_ update index of UpEvent length buffer of charFunctClipped values, subtract oldest value, and save provisional current sample charFunct value
                mem.upEventBufPtr = (mem.upEventBufPtr + 1) % mem.nTUpEvent;
                // _DOC_ if in trigger event, accumlated sum of characteristic function values (integralCharFunctClipped)
                // _DOC_ to avoid spikes, do not include full charFunct value, may be very large
                mem.integralCharFunctClipped -= mem.upEventCharFunctClippedValue[mem.upEventBufPtr];
                mem.integralCharFunctClipped += charFunctClipped;
                mem.upEventCharFunctClippedValue[mem.upEventBufPtr] = charFunctClipped;
                // _DOC_ save accumlated sum of characteristic function values (integralCharFunct) as indicator of pick strenth
                mem.integralCharFunct -= mem.upEventCharFunctValue[mem.upEventBufPtr];
                mem.integralCharFunct += charFunct;
                mem.upEventCharFunctValue[mem.upEventBufPtr] = charFunct;
                
                if (mem.inTriggerEvent) {
                    mem.integralCharFunctPick = Math.max(mem.integralCharFunctPick, mem.integralCharFunct);
                    if (n <= mem.indexUpEventEnd) {     // before end of tUpEvent window
                        // _DOC_ if in trigger event and before or at end of tUpEvent window, check if integralCharFunctClipped >= criticalIntegralCharFunct, if so, declare pick
                        if (!mem.willAcceptPick && mem.integralCharFunctClipped >= mem.criticalIntegralCharFunct) {
                            mem.willAcceptPick = true;
                        }
                    } else {    // reached end of tUpEvent window
                        // _DOC_ accept pick
                        if (mem.willAcceptPick) {
                            mem.acceptedPick = true;
                            //System.out.println("mem.criticalIntegralCharFunct " + mem.criticalIntegralCharFunct);
                            //System.out.println("mem.integralCharFunctPick " + mem.integralCharFunctPick);
                            // set flag to prevent next trigger until charFunc drops below threshold
                            mem.underThresholdSinceLastTrigger = false;
                        } else {
                            mem.indexUpEventEnd = INT_UNSET;   // prevents disabling of charFunct update
                        }
                        mem.willAcceptPick = false;
                        // _DOC_ set flag to indicate not in trigger event
                        mem.inTriggerEvent = false;
                    }
                } else if (mem.underThresholdSinceLastTrigger && charFunctClipped >= threshold1) {  // over threshold, start pick event - triggered
                    // _DOC_ if not in trigger event and characteristic function > threshold1, declare trigger event
                    mem.inTriggerEvent = true;
                    // AJL 20080716 - bug fix
                    if (mem.indexUncertainty == INT_UNSET) {
                        //System.out.println("XXX declare trigger event but mem.indexUncertainty == INT_UNSET, triggerPickData=" + triggerPickData.size());
                        mem.indexUncertainty = n - 1;
                    }
                    //
                    mem.triggerNumRecursiveIndex = charFuntNumRecursiveIndex;
                    mem.integralCharFunctPick = 0.0;
                    // _DOC_ set index for trigger event begin and end (= begin + nTUpEvent)
                    mem.indexUpEvent = n;
                    mem.indexUpEventEnd = n + mem.nTUpEvent;
                    // _DOC_ evaluate polarity based on accumulate count of sign of derivative (=POS if count > 1; = NEG if count < -1, UNK otherwise)
                    if (n > mem.indexUncertainty + 1)   // if uncertainty width > 1, remove last polarity increment (if width = 1, amp diff must be used for polarity; if width > 1, it is more robust to ignore last am diff)
                        mem.countPolarity -= polarityIncrement;
                    if (mem.countPolarity > 0)
                        mem.pickPolarity = PickData.POLARITY_POS;
                    else if (mem.countPolarity < 0)
                        mem.pickPolarity = PickData.POLARITY_NEG;
                    else
                        mem.pickPolarity = PickData.POLARITY_UNKNOWN;
                } else {  // no trigger, accumulate integral
                    if (mem.charFunctUncertainty < mem.uncertaintyThreshold)  // allow new triggers
                        mem.underThresholdSinceLastTrigger = true;
                }
            }
            
            
            // _DOC_ update "true", long-term statistic based on current signal values based on long-term window
            // long-term decay formulation
            // _DOC_ update long-term means of x, dxdt, E2, var(E2)
            for (int k = 0; k < mem.numRecursive; k++) {
                mem.mean_xRec[k] = mem.mean_xRec[k] * mem.longDecayConst + mem.xRec[k] * mem.longDecayFactor;
                double dev = mem.xRec[k] - mem.mean_xRec[k];
                // AJL 20080307
                mem.mean_var_xRec[k] = mem.mean_var_xRec[k] * mem.longDecayConst + dev * dev * mem.longDecayFactor;
                //mem.mean_var_xRec[k] = mem.mean_var_xRec[k] * mem.filterDecayConst + dev * dev * mem.filterDecayFactor;
                // _DOC_ mean_stdDev_E2 is sqrt(long-term mean var(E2))
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
                //sampleNew[n] = (float) mem.test[mem.numRecursive - 1];
                //
            } else if (resultType == CHAR_FUNC) {	    // show char function
                sampleNew[n] = (float) charFunct;
            } else {                // generate picks
                // PICK
                if (mem.acceptedPick) {
                    // _DOC_ if pick declared, save pick time, uncertainty, strength (integralCharFunct) and polarity
                    // _DOC_ pick time is mean of time of last uncertainty threshold (characteristic function rose past uncertaintyThreshold) and trigger time (characteristic function >= threshold1)
                    // _DOC_ pick uncertainty is from time of last uncertainty threshold to trigger time
                    double triggerPeriod = dt / mem.decayFactor[mem.triggerNumRecursiveIndex];
                    PickData pickData = new PickData((double) mem.indexUncertainty,
                    (double) mem.indexUpEvent,
                    mem.pickPolarity, mem.integralCharFunctPick / mem.criticalIntegralCharFunct,
                    PickData.CHAR_FUNCT_AMP_UNITS, triggerPeriod);
                    triggerPickData.add(pickData);
                }
            }
            
            mem.acceptedPick = false;
            
            mem.lastSample = currentSample;
            
        }
        
        
        if (useMemory) {
            // corect memory index values for sample length
            mem.indexUncertainty -= sample.length;
            mem.indexUpEvent -= sample.length;
            mem.indexUpEvent = mem.indexUpEvent < INT_UNSET ? INT_UNSET : mem.indexUpEvent;
            mem.indexUpEventEnd -= sample.length;
            mem.indexUpEventEnd = mem.indexUpEventEnd < INT_UNSET ? INT_UNSET : mem.indexUpEventEnd;
            
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
    // _DOC_ =============================
    // _DOC_ TestPicker4_Memory object/structure
    // _DOC_ =============================
    
    public class TestPicker4_Memory extends TimeDomainMemory {
        
        // _DOC_ =============================
        // _DOC_ picker memory for realtime processing of packets of data
        
        double longDecayFactor = deltaTime / longTermWindow;
        double longDecayConst = 1.0 - longDecayFactor;
        int nLongTermWindow = 1 + (int) (longTermWindow / deltaTime);
        
        int indexEnableTriggering = nLongTermWindow;
        boolean enableTriggering = false;
        int nTotal = -1;
        
        
        // _DOC_ set up buffers and memory arrays for previous samples and their statistics
        int numPrevious = (int) (filterWindow / deltaTime);  // estimate of number of previous samples to bufer
        int numRecursive = 1;   // number of powers of 2 to process
        int nTemp = 1; {
            while (nTemp < numPrevious) {
                numRecursive++;
                nTemp *= 2;
            }
            numPrevious = nTemp;    // numPrevious is now a power of 2
        }
        double[] xRec = new double[numRecursive];
        double[] test = new double[numRecursive];
        double[][] filteredSample = new double[numRecursive][numPrevious];
        double[] mean_xRec = new double[numRecursive];
        double[] mean_stdDev_xRec = new double[numRecursive];
        double[] mean_var_xRec = new double[numRecursive];
        double[] decayFactor = new double[numRecursive];
        double[] decayConst = new double[numRecursive];
        int[] indexDelay = new int[numRecursive];
        
        int numPreviousPtr = -1;
        int numPreviousPtrLast = numPrevious - 2;
        
        double window = deltaTime;
        int nDelay = 1;
        
        {
            for (int k = 0; k < numRecursive; k++) {
                mean_xRec[k] = 0.0;
                mean_stdDev_xRec[k] = 0.0;
                decayFactor[k] = deltaTime / window;
                decayConst[k] = 1.0 - decayFactor[k];
                window *= 2.0;
                indexDelay[k] = nDelay + 1;
                nDelay *= 2;
                //System.out.println(k, decayFactor[k], indexDelay[k] " + k + " " + decayFactor[k] + " " + indexDelay[k]);
            }
        }
        
        
        double lastSample = 0.0;
        
        double charFunctUncertainty;
        double charFunctLast1;
        double charFunctLast2;
        double charFunctLast1Smooth;
        double charFunctLast2Smooth;
        double uncertaintyAtResetCharFunctUncertainty = Double.MAX_VALUE;
        double uncertaintyThreshold = 2.0;
        double amplitudeUncertainty;
        int indexUncertainty = INT_UNSET;
        int countPolarity;
        
        boolean inTriggerEvent = false;
        double integralCharFunct = 0.0;
        double integralCharFunctPick = 0.0;
        
        int indexUpEvent = INT_UNSET;
        int indexUpEventEnd = INT_UNSET;   // prevents disabling of charFunct update
        int nTUpEvent = (int) (0.5 + tUpEvent / deltaTime) - 1; {
            if (nTUpEvent < 1)
                nTUpEvent = 1;
        }
        // _DOC_ criticalIntegralCharFunct is tUpEvent * threshold2
        double criticalIntegralCharFunct = (double) (nTUpEvent) * threshold2;   // one less than number of samples examined
        
        // _DOC_ integralCharFunctClipped is integral of charFunct values for last nTUpEvent samples, charFunct values possibly limited if around trigger time
        double integralCharFunctClipped = 0.0;
        
        // AJL 20080222 - added to supress triggering immediately after end of previous inTriggerEvent ends
        // flag to prevent next trigger until charFunc drops below threshold2
        boolean underThresholdSinceLastTrigger = false;
        
        double[] upEventCharFunctClippedValue = new double[nTUpEvent];    // $$$ ADDED
        double[] upEventCharFunctValue = new double[nTUpEvent];    // $$$ ADDED
        int upEventBufPtr = -1;
        
        boolean acceptedPick = false;
        boolean willAcceptPick = false;
        int pickPolarity = PickData.POLARITY_UNKNOWN;
        int triggerNumRecursiveIndex = -1;
        
        int nvar = 0;
        
        
        
        
        
        public TestPicker4_Memory(float[] sample) {
            
            // initialize previous samples to first sample value
            
            for (int k = 0; k < numRecursive; k++)
                for (int j = 0; j < numPrevious; j++)
                    filteredSample[k][j] = sample[0];
            
        }
        
    }
    
    
    
    
    
}	// End class


