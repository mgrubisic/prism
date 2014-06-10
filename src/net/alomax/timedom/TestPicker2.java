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



public class TestPicker2 extends BasicPicker {
    
    // AJL need long term window to limit npts used to update stats
    private static final double WINDOW_MIN = Double.MIN_VALUE;
    private static final double WINDOW_MAX = Double.MAX_VALUE;
    public double longTermWindow = 10.0;
    
    public double threshold1 = 10.0;    // threshold to intiatie trigger
    public double threshold2 = 10.0;       // threshold to maintain trigger
    public double tUpEvent = 1.0;
    public double recursionWindow = 2.0;
    
    public String errorMessage;
    
    private static final double THRESHOLD_MIN = Double.MIN_VALUE;
    private static final double THRESHOLD_MAX = Double.MAX_VALUE;
    private static final double TIME_MIN = -Double.MAX_VALUE;
    private static final double TIME_MAX = Double.MAX_VALUE;
    
    
    /** constructor */
    
    public TestPicker2(String localeText, double longTermWindow, double threshold1, double threshold2, double tUpEvent, double recursionWindow, int direction) {
        
        super(localeText, direction);
        
        this.longTermWindow = longTermWindow;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
        this.tUpEvent = tUpEvent;
        this.recursionWindow = recursionWindow;
        
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
        
        recursionWindow = meanWindowValue;
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
        if (recursionWindow < TIME_MIN || recursionWindow > TIME_MAX) {
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
        
        // alomax test stuff
        //double uncertaintyThreshold = threshold1 / 2.0;
        double uncertaintyThreshold = 2.0;
        int indexUncertainty = -1;
        double amplitudeUncertainty = 0.0;
        double amplitudePolarity = 0.0;
        int countPolarity = 0;
        
        double longDecayFactor = dt / longTermWindow;
        double longDecayConst = 1.0 - longDecayFactor;
        int nLongTermWindow = 1 + (int) (longTermWindow / dt);
        int indexAllowTriggerEvent = nLongTermWindow;
        
        
        int numPrevious = (int) (recursionWindow / dt);  // estimate of number of previous samples to bufer
        int numRecursive = 1;   // number of powers of 2 to process
        int nTemp = 1;
        while (nTemp < numPrevious) {
            numRecursive++;
            nTemp *= 2;
        }
        numPrevious = nTemp;    // numPrevious is now a power of 2
        System.out.println("numRecursive, numPrevious " + numRecursive + " " + numPrevious);
        double[] xRec = new double[numRecursive];
        double[] previousSamples = new double[numPrevious];
        double[] mean_xRec = new double[numRecursive];
        double[] mean_xRec_ACTIVE = new double[numRecursive];
        double[] mean_stdDev_xRec = new double[numRecursive];
        double[] mean_stdDev_xRec_ACTIVE = new double[numRecursive];
        double[] mean_var_xRec = new double[numRecursive];
        
        for (int k = 0; k < numRecursive; k++) {
            mean_xRec[k] = 0.0;
            mean_stdDev_xRec[k] = 0.0;
        }
        
        
        
        double lastSample = 0.0;
        double sampleDemeanedLast = 0.0;
        boolean dataGap = false;
        
        
        double charFunct = 0.0;
        double charFunctTrue = 0.0;
        double charFunctUncertainty = 0.0;
        double charFunctLast1 = 0.0;
        double charFunctLast2 = 0.0;
        
        boolean inTriggerEvent = false;
        double ampCharFunctSum = 0.0;
        
        boolean backwards = (direction == -1);
        
        int indexUpEvent = -1;
        int indexUpEventEnd = -Integer.MAX_VALUE;   // prevents disabling of charFunct update
        //int indexDownCount = -1;
        int nTUpEvent = (int) (0.5 + tUpEvent / dt) - 1;
        if (nTUpEvent < 1)
            nTUpEvent = 1;
        // $DOC criticalIntegralCharFunct is tUpEvent * threshold2
        double criticalIntegralCharFunct = (double) (nTUpEvent) * threshold2;   // one less than number of samples examined
        
        // $DOC integralCharFunct is integral of charFunct values for last nTUpEvent samples, charFunct values possibly limited if around trigger time
        double integralCharFunct = 0.0;
        
        double[] upEventCharFunctValue = new double[nTUpEvent];    // $$$ ADDED
        //for (int i = 0; i < nTUpEvent; i++)
        //    upEventCharFunctValue[i] = 0.0;
        int upEventIndex = -1;
        
        boolean acceptedPick = false;
        boolean willAcceptPick = false;
        int pickPolarity = PickData.POLARITY_UNKNOWN;
        
        int nvar = 0;
        
        // initialize previous samples to first sample value
        if (backwards) {
            for (int k = 0; k < numPrevious; k++)
                previousSamples[k] = sample[sample.length - 1];
        } else {
            for (int k = 0; k < numPrevious; k++)
                previousSamples[k] = sample[0];
        }
        
        
        if (resultType == TRIGGER || resultType == CHAR_FUNC)
            sample[0] = sample[sample.length - 1] = 0.0f;
        
        int i;
        for (int n = 1; n < sample.length - 1; n++) {
            
            if (backwards) {
                i = sample.length - n - 1;
            } else {
                i = n;
            }
            
            // $DOC when not in active trigger event, use current long-term statistics for characteristic function
            // $DOC when in active trigger event, use long-term statistics from pre-trigger for characteristic function
            if (!inTriggerEvent) {  // not in pick, update active stats
                for (int k = 0; k < numRecursive; k++) {
                    mean_xRec_ACTIVE[k] = mean_xRec[k];
                    mean_stdDev_xRec_ACTIVE[k] = mean_stdDev_xRec[k];
                }
            }
            
            
            // $DOC characteristic function is  (E2 - mean_E2_ACTIVE) / mean_stdDev_E2_ACTIVE
            charFunct = 0.0;
            // $DOC evaluate current signal values
            float currentSample = sample[i];
            int index = i % numPrevious;
            int power = 1;
            for (int k = 0; k < numRecursive; k++) {
                xRec[k] = currentSample - previousSamples[index];
                xRec[k] *= xRec[k];
                if (inTriggerEvent || n > indexUpEventEnd + power) { // do not update charFunct if < delay beyond end of last accepted trigger event
                    if (mean_stdDev_xRec_ACTIVE[k] > Float.MIN_VALUE) {
                        // max
                        charFunct = Math.max(charFunct, (xRec[k] - mean_xRec_ACTIVE[k]) / mean_stdDev_xRec_ACTIVE[k]);
                        // rms
                        //double val = (xRec[k] - mean_xRec_ACTIVE[k]) / mean_stdDev_xRec_ACTIVE[k];
                        //charFunct += val * val;
                    }
                }
                index = (numPrevious + index - power) % numPrevious;
                power *= 2;
            }
            // save curent sample to preveious list
            previousSamples[(i + 1) % numPrevious] = currentSample;
            
            
            
            // update true stats
            // $DOC if in trigger event, need to calculate "true" B-K statistic (E2) based on current statistics
            /*
            if (inTriggerEvent) {  // in trigger event, set true sample stats
                for (int k = 0; k < numRecursive; k++) {
                    charFunctTrue += (xRec[k] - mean_xRec[k]) / mean_stdDev_xRec[k];
                }
            }
            charFunctTrue /= (double) numRecursive;
             **/
            
            // check characteristic function value
            
            // uncertainty logic
            charFunctUncertainty = (charFunct + charFunctLast1 + charFunctLast2) / 3.0;   // 3 point smoothing to avoid sample oscillation
            charFunctLast2 = charFunctLast1;
            charFunctLast1 = charFunct;  // $$$ ADDED charFunctUncertainty
            if (charFunctUncertainty > uncertaintyThreshold) {
                // $DOC each time characteristic function rises past uncertaintyThreshold, if not in trigger event, store sample index and initiate polarity algoirithm
                if (indexUncertainty < 0) {
                    if (backwards) {
                        indexUncertainty = i + 1;
                    } else {
                        indexUncertainty = i - 1;
                    }
                    // $DOC initialize polarity algorithm, uses count of sign of derivative of signal
                    amplitudeUncertainty = sample[indexUncertainty];
                    amplitudePolarity = amplitudeUncertainty;
                    // countPolarity = dXdt > 0.0 ? 1 : -1; // 200606222 AJL
                    countPolarity = 0;
                }
            } else if (!inTriggerEvent) {
                // $DOC if characteristic function is below uncertaintyThreshold, and if not in trigger event, unset uncertainty sample index
                indexUncertainty = -1;
            }
            // $DOC if characteristic function is above uncertaintyThreshold, and if not in trigger event, accumulate count of sign of derivative for polarity estimate
            if (indexUncertainty >= 0 && !inTriggerEvent) {   // accumulate count of polarity between uncertainty point and trigger point
                amplitudePolarity = Math.abs(currentSample - amplitudeUncertainty) > Math.abs(amplitudePolarity) ? currentSample - amplitudeUncertainty : amplitudePolarity;
                countPolarity += currentSample - lastSample > 0.0 ? 1 : -1;
            }
            
            // trigger and pick logic
            // $DOC only apply trigger and pick logic if past long-term stabilisation time (longTermWindow)
            if (n > indexAllowTriggerEvent) {  // past long-term stabilisation time and numPrevious past last end event
                
                // upEventCharFunctValue    // $$$ ADDED
                // $DOC update index of UpEvent length buffer of charFunct values, subtract oldest value, and save provisional current sample charFunct value
                upEventIndex = (upEventIndex + 1) % nTUpEvent;
                integralCharFunct -= upEventCharFunctValue[upEventIndex];
                upEventCharFunctValue[upEventIndex] = charFunct;
                
                if (inTriggerEvent) {
                    // $DOC if in trigger event, accumlated sum of characteristic function values (integralCharFunct)
                    // $DOC to prevent triggering on spikes, limit characteristic function value to threshold1 for first 2 triggered samples
                    if (i > indexUpEvent + 1) {    // past 2nd sample after trigger, to avoid spikes
                        integralCharFunct += charFunct;
                    } else {   // to avoid spikes, do not include full charFunct value, may be very large
                        double value = charFunct < threshold1 ? charFunct : threshold1;
                        integralCharFunct += value;
                        upEventCharFunctValue[upEventIndex] = value;
                    }
                    //System.out.println("" + (i - indexUpEvent) + " " + n + " " + charFunct + " " + integralCharFunct + " / " + criticalIntegralCharFunct);
                    // $DOC save accumlated sum of characteristic function values (ampCharFunctSum) as indicator of pick strenth
                    ampCharFunctSum += charFunct;
                    if (n <= indexUpEventEnd) {     // before end of tUpEvent window
                        // $DOC if in trigger event and before or at end of tUpEvent window, check if integralCharFunct >= criticalIntegralCharFunct, if so, declare pick
                        if (!willAcceptPick && integralCharFunct >= criticalIntegralCharFunct) {
                            willAcceptPick = true;
                            //System.out.println(">>>>>>>>> acceptedPick");
                        }
                    } else {    // reached end of tUpEvent window
                        // $DOC accept pick
                        if (willAcceptPick) {
                            acceptedPick = true;
                            // set previous samples to their mean value to avoid post-picking an event inside last event window
                            /*double dmean = 0.0;
                            for (int k = 0; k < numPrevious; k++)
                                dmean += previousSamples[k];
                            dmean /= (double) numPrevious;
                            for (int k = 0; k < numPrevious; k++)
                                previousSamples[k] = dmean;
                             */
                            indexAllowTriggerEvent = n + numPrevious;
                        } else {
                            indexUpEventEnd = -Integer.MAX_VALUE;   // prevents disabling of charFunct update
                        }
                        willAcceptPick = false;
                        // $DOC set flag to indicate not in trigger event
                        inTriggerEvent = false;
                    }
                } else if (charFunct >= threshold1) {  // over threshold, start pick event - triggered
                    // $DOC if not in trigger event and characteristic function > threshold1, declare trigger event
                    inTriggerEvent = true;
                    // $DOC initialize ampCharFunctSum (=charFunct)
                    ampCharFunctSum = charFunct;
                    // $DOC set index for trigger event begin and end (= begin + nTUpEvent)
                    indexUpEvent = i;
                    indexUpEventEnd = n + nTUpEvent;
                    //integralCharFunct = 0.0; // do not include trigger value, may be very large
                    // $DOC initialize integralCharFunct (=threshold1 to avoid large values and to prevent triggering on spikes)
                    integralCharFunct += threshold1; // do not include full charFunct value, may be very large
                    upEventCharFunctValue[upEventIndex] = threshold1;
                    // $DOC evaluate polarity based on accumulate count of sign of derivative (=POS if count > 1; = NEG if count < -1, UNK otherwise)
                    if (countPolarity > 1)
                        pickPolarity = PickData.POLARITY_POS;
                    else if (countPolarity < -1)
                        pickPolarity = PickData.POLARITY_NEG;
                    else
                        pickPolarity = PickData.POLARITY_UNKNOWN;
                } else {  // under threshold, accumulate integral
                    integralCharFunct += charFunct;
                }
            }
            
            
            // $DOC update "true", long-term statistic based on current signal values based on long-term window
            // long-term decay formulation
            // $DOC update long-term means of x, dxdt, E2, var(E2)
            for (int k = 0; k < numRecursive; k++) {
                mean_xRec[k] = mean_xRec[k] * longDecayConst + xRec[k] * longDecayFactor;
                double dev = xRec[k] - mean_xRec[k];
                mean_var_xRec[k] = mean_var_xRec[k] * longDecayConst + dev * dev * longDecayFactor;
                // $DOC mean_stdDev_E2 is sqrt(long-term mean var(E2))
                mean_stdDev_xRec[k] = Math.sqrt(mean_var_xRec[k]);
            }
            
            
            // act on result
            
            if (resultType == TRIGGER) {	// show triggers
                if (acceptedPick)
                    sample[i] = 1.0f;
                else
                    sample[i] = 0.0f;
            } else if (resultType == CHAR_FUNC) {	    // show char function
                sample[i] = (float) charFunct;
                //sample[i] = (float) integralCharFunct;
            } else {                // generate picks
                // PICK
                if (acceptedPick) {
                    // $DOC if pick declared, save pick time, uncertainty, strength (ampCharFunctSum) and polarity
                    // $DOC pick time is mean of time of last uncertainty threshold (characteristic function rose past uncertaintyThreshold) and trigger time (characteristic function >= threshold1)
                    // $DOC pick uncertainty is from time of last uncertainty threshold to trigger time
                    PickData pickData = new PickData((double) indexUncertainty, (double) indexUpEvent,
                    pickPolarity, Math.log(ampCharFunctSum / criticalIntegralCharFunct), PickData.CHAR_FUNCT_AMP_UNITS);
                    triggerPickData.add(pickData);
                    indexUncertainty = -1;
                }
                /*
                // save index if trigger tUpEvent went from < 1 to > 1
                if (lastUnitTUpEventIndex < 0 && n > nThreshold2 && tUpEvent > 1.0) {
                    lastUnitTUpEventIndex = i;
                } else if (n > nThreshold2 && tUpEvent < 1.0) {
                    lastUnitTUpEventIndex = -1;
                }
                 **/
            }
            
            acceptedPick = false;
            
            lastSample = currentSample;
            
        }
        
        return(sample);
        
    }
    
    
    
    
    
    
}	// End class


