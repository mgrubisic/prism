/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2004 Anthony Lomax <anthony@alomax.net www.alomax.net>
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



public class BaerKradolferPicker extends BasicPicker {
    
    // AJL need long term window to limit npts used to update stats
    private static final double WINDOW_MIN = Double.MIN_VALUE;
    private static final double WINDOW_MAX = Double.MAX_VALUE;
    public double longTermWindow = 100.0;

    public double threshold1 = 10.0;
    public double threshold2 = 20.0;
    public double tUpEvent = 0.5;
    public double tDownMax = 0.25;
    
    public String errorMessage;
    
    private static final double THRESHOLD_MIN = Double.MIN_VALUE;
    private static final double THRESHOLD_MAX = Double.MAX_VALUE;
    private static final double TIME_MIN = -Double.MAX_VALUE;
    private static final double TIME_MAX = Double.MAX_VALUE;
    
    
    /** constructor */
    
    public BaerKradolferPicker(String localeText, double longTermWindow, double threshold1, double threshold2, double tUpEvent, double tDownMax, int direction) {
        
        super(localeText, direction);
        
        this.longTermWindow = longTermWindow;
        this.threshold1 = threshold1;
        this.threshold2 = threshold2;
        this.tUpEvent = tUpEvent;
        this.tDownMax = tDownMax;
        
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
    
    
    
    
    /** Method to set tDownMaxValue */
    
    public void setTDownMax(double tDownMaxValue) throws TimeDomainException {
        if (tDownMaxValue < TIME_MIN || tDownMaxValue > TIME_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_tDownMax_value + ": " + tDownMaxValue);
        }
        
        tDownMax = tDownMaxValue;
    }
    
    
    /** Method to set tDownMaxValue */
    
    public void setTDownMax(String str) throws TimeDomainException {
        
        double tDownMaxValue;
        
        try {
            tDownMaxValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_tDownMax_value + ": " + str);
        }
        
        setTDownMax(tDownMaxValue);
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
        if (tDownMax < TIME_MIN || tDownMax > TIME_MAX) {
            errMessage += ": " + TimeDomainText.invalid_tDownMax_value;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }
        
    }
    
    
    
    /*** function to calculate picks  */
    
    public final float[] apply(double dt, float[] sample) {
        
        // alomax test stuff
        //double uncertaintyThreshold = threshold1 / 2.0;
        double uncertaintyThreshold = 1.0;
        int indexUncertainty = -1;
        double longDecayConst = (1.0 - dt / longTermWindow);
        double longFactor = 1.0 - longDecayConst;
        int nLongTermWindow = 1 + (int) (longTermWindow / dt);
        
        
        float sampleLast = 0.0f;
        double sum_Xval = 0.0;
        double sum_dXdt = 0.0;
        
        double sum_mean_E4 = 0.0;
        double sum_var_E4 = 0.0;
        double stdDev_E4 = 0.0;
        double mean_E4 = 0.0;
        double charFunct = 0.0;
        
        double Xval, dXdt, E2, E4, stdDev;
        
        boolean pickFlag = false;
        
        boolean backwards = (direction == -1);
        
        int indexUpEvent = -1;
        int indexUpEventEnd = -1;
        int indexDownEnd = -1;
        int nTUpEvent = 1 + (int) (tUpEvent / dt);
        int nTDownMax = 1 + (int) (tDownMax / dt);
        boolean acceptedPick = false;
        
        double ampCharFunctTrigger = 0.0;
        
        int nvar = 0;
        
        int i, ilast;
        if (resultType == TRIGGER || resultType == CHAR_FUNC)
            sample[0] = sample[sample.length - 1] = 0.0f;
        for (int n = 1; n < sample.length - 1; n++) {
            
            if (backwards) {
                i = sample.length - n - 1;
            } else {
                i = n;
            }
            
            Xval = sample[i];
            dXdt = sample[i] - sampleLast;
            sampleLast = sample[i];
            
            if (sum_dXdt > Float.MIN_VALUE)
                E2 = Xval * Xval + dXdt * dXdt * (sum_Xval / sum_dXdt);
            else
                E2 = Xval * Xval + dXdt * dXdt / (dt * dt);
            E4 = E2 * E2;
            //System.out.println("i E4 E2 Xval dXdt" + " " + i + " " + E4 + " " + E2 + " " + Xval + " " + dXdt)   ;
            if (Double.isInfinite(E4) || E4 > Math.sqrt(Double.MAX_VALUE))
                E4 = Math.sqrt(Double.MAX_VALUE);
            
            if (stdDev_E4 <= Float.MIN_VALUE) {
                charFunct = 0.0;
            } else {
                charFunct = (E4 - mean_E4) / stdDev_E4;
                //System.out.println("charFunct = (E4 - mean_E4) / stdDev_E4" + " " + charFunct + " " + E4 + " " + mean_E4 + " " + stdDev_E4)   ;
            }
            
            if (charFunct < threshold2) {  // under threshold2, update stats
                /*  original BK formulation - no long-term decay
                nvar++;
                sum_Xval += Xval * Xval;
                sum_dXdt += dXdt * dXdt;
                sum_mean_E4 += E4;
                mean_E4 = sum_mean_E4 / (double) nvar;
                stdDev = E4 - mean_E4;
                sum_var_E4 += stdDev * stdDev;
                //stdDev_E4 = sum_var_E4 / (double) nvar;
                stdDev_E4 = Math.sqrt(sum_var_E4 / (double) nvar);
                 **/
                // long-term decay formulation
                // XXX = XXX * longDecayConst + NEW_XXX * longFactor;
                // sum_Xval += Xval * Xval;
                sum_Xval = sum_Xval * longDecayConst + Xval * Xval * longFactor;
                // sum_dXdt += dXdt * dXdt;
                sum_dXdt = sum_dXdt * longDecayConst + dXdt * dXdt * longFactor;
                // sum_mean_E4 += E4;
                sum_mean_E4 = sum_mean_E4 * longDecayConst + E4 * longFactor;
                //
                mean_E4 = sum_mean_E4;
                stdDev = E4 - mean_E4;
                // sum_var_E4 += stdDev * stdDev;
                sum_var_E4 = sum_var_E4 * longDecayConst + stdDev * stdDev * longFactor;
                stdDev_E4 = Math.sqrt(sum_var_E4);
            }
            
            // check characteristic function value
            
            // uncertainty logic
            if (charFunct >= uncertaintyThreshold) {
                if (indexUncertainty < 0)
                    indexUncertainty = i;
            } else /*if (!pickFlag)*/ {
                indexUncertainty = -1;
            }
            
            // pick logic
            if (charFunct >= threshold1) {  // over threshold
                
                if (!pickFlag) {    // set pick flag
                    pickFlag = true;
                    indexUpEvent = i;
                    indexUpEventEnd = n + nTUpEvent;
                    ampCharFunctTrigger = charFunct;
                } else {      // check if valid pick
                    if (n >= indexUpEventEnd) {     // reached end of tUpEvent window
                        acceptedPick = true;
                        pickFlag = false;
                    }
                }
                indexDownEnd = -1;
                
            } else {    // under threshold
                
                if (pickFlag) { // in pick
                    
                    if (n < indexUpEventEnd) { // in tUpEvent window
                        if (indexDownEnd < 0) {    // first drop under threshold
                            indexDownEnd = n + nTDownMax;
                        } else if (n >= indexDownEnd) {     // under threshold too long
                            pickFlag = false;
                        }
                    } else {     // reached end of tUpEvent window
                        pickFlag = false;
                    }
                    
                }
            }
            
            
            
            // act on result
            
            if (resultType == TRIGGER) {	// show triggers
                if (acceptedPick)
                    sample[i] = 1.0f;
                else
                    sample[i] = 0.0f;
            } else if (resultType == CHAR_FUNC) {	    // show char function
                sample[i] = (float) charFunct;
                //sample[i] = (float) E2;
            } else {                // generate picks
                // trigger
                if (acceptedPick) {
                    PickData pickData = new PickData((double) (indexUncertainty == indexUpEvent ? indexUncertainty - 1 : indexUncertainty), 
                    (double) indexUpEvent, PickData.POLARITY_UNKNOWN, ampCharFunctTrigger, PickData.CHAR_FUNCT_AMP_UNITS);
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
            
            
        }
        
        return(sample);
        
    }
    
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
        super.updateFields(timeSeries);
        
    }
    
    
}	// End class


