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



public class ShortLongTermAve extends BasicPicker {
    
    public double shortTermWindow = 1.0;
    public double longTermWindow = 10.0;
    public double triggerRatio = 2.0;
    
    public String errorMessage;
    
    private static final double WINDOW_MIN = Double.MIN_VALUE;
    private static final double WINDOW_MAX = Double.MAX_VALUE;
    private static final double RATIO_MIN = -Double.MAX_VALUE;
    private static final double RATIO_MAX = Double.MAX_VALUE;
    
    
    /** constructor */
    
    public ShortLongTermAve(String localeText, double shortTermWindow, double longTermWindow, double triggerRatio, int direction) {
        
        super(localeText, direction);
        
        this.shortTermWindow = shortTermWindow;
        this.longTermWindow = longTermWindow;
        this.triggerRatio = triggerRatio;
        
    }
    
    
    /** Method to set shortTermWindowValue */
    
    public void setShortTermWindow(double shortTermWindowValue) throws TimeDomainException {
        if (shortTermWindowValue < WINDOW_MIN || shortTermWindowValue > WINDOW_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_short_term_window_value + ": " + shortTermWindowValue);
        }
        
        shortTermWindow = shortTermWindowValue;
    }
    
    
    /** Method to set shortTermWindowValue */
    
    public void setShortTermWindow(String str) throws TimeDomainException {
        
        double shortTermWindowValue;
        
        try {
            shortTermWindowValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_short_term_window_value + ": " + str);
        }
        
        setShortTermWindow(shortTermWindowValue);
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
    
    
    
    /** Method to set ratioValue */
    
    public void setRatio(double ratioValue) throws TimeDomainException {
        if (ratioValue < RATIO_MIN || ratioValue > RATIO_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_ratio_value + ": " + ratioValue);
        }
        
        triggerRatio = ratioValue;
    }
    
    
    /** Method to set ratioValue */
    
    public void setRatio(String str) throws TimeDomainException {
        
        double ratioValue;
        
        try {
            ratioValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_ratio_value + ": " + str);
        }
        
        setRatio(ratioValue);
    }
    
    
    
    
    
    /** Method to check settings */
    
    public void checkSettings() throws TimeDomainException {
        
        super.checkSettings();
        
        String errMessage = "";
        int badSettings = 0;
        
        if (shortTermWindow < WINDOW_MIN || shortTermWindow > WINDOW_MAX) {
            errMessage += ": " + TimeDomainText.invalid_short_term_window_value;
            badSettings++;
        }
        if (longTermWindow < WINDOW_MIN || longTermWindow > WINDOW_MAX) {
            errMessage += ": " + TimeDomainText.invalid_long_term_window_value;
            badSettings++;
        }
        if (triggerRatio < RATIO_MIN || triggerRatio > RATIO_MAX) {
            errMessage += ": " + TimeDomainText.invalid_ratio_value;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }
        
    }
    
    
    
    /*** function to calculate picks  */
    
    public final float[] apply(double dt, float[] sample) {
        
        
        float shortDecayConst = (float) (1.0 - dt / shortTermWindow);
        float shortFactor = 1.0f - shortDecayConst;
        float longDecayConst = (float) (1.0 - dt / longTermWindow);
        float longFactor = 1.0f - longDecayConst;
        
        float sval = 0.0f;
        float lval = 0.0f;
        float mval = sample[0];
        
        float ratio = 0.0f;
        
        int nLongTermWindow = 1 + (int) (longTermWindow / dt);
        
        boolean backwards = (direction == -1);
        
        // pick
        int lastCriticalRatioIndex = -1;
        double criticalRatio = 1.0;
        double ratioLast = sval;
        double lastPickIndex1 = 0.0;
        
        int i;
        for (int n = 0; n < sample.length; n++) {
            
            if (backwards)
                i = sample.length - n - 1;
            else
                i = n;
            
            //mval = mval * longDecayConst + sample[i] * longFactor;
            mval = 0.0f;
            sval = sval * shortDecayConst + Math.abs(sample[i] - mval) * shortFactor;
            
            if (n > nLongTermWindow) {
                lval = lval * longDecayConst + Math.abs(sample[i] - mval) * longFactor;
                ratio = sval / lval;
            } else {
                lval = sval;
            }
            
            boolean triggered = n > nLongTermWindow && ratio > triggerRatio;
            
            if (resultType == TRIGGER) {	// show triggers
                if (triggered)
                    sample[i] = 1.0f;
                else
                    sample[i] = 0.0f;
            } else if (resultType == CHAR_FUNC) {	    // show sta/lta
                sample[i] = ratio;
            } else {                // generate picks
                // trigger
                if (triggered) {
                    double[] pickIndices = new double[2];
                    //pickIndices[0] = lastCriticalRatioIndex >= 0 ? lastCriticalRatioIndex : i - 1;
                    pickIndices[0] = (double) i - (ratio - 1.0) / (ratio - ratioLast);
                    if (pickIndices[0] < lastPickIndex1)
                        pickIndices[0] = lastPickIndex1;
                    if (pickIndices[0] < lastCriticalRatioIndex)
                        pickIndices[0] = lastCriticalRatioIndex;
                    pickIndices[1] = (double) i;
                    PickData pickData = new PickData(pickIndices[0], pickIndices[1], PickData.POLARITY_UNKNOWN, ratio, PickData.CHAR_FUNCT_AMP_UNITS);
                    triggerPickData.add(pickData);
                    lastPickIndex1 = pickIndices[1];
                }
                // save index if trigger ratio went from < criticalRatio to > criticalRatio
                if (lastCriticalRatioIndex < 0 && n > nLongTermWindow && ratio > criticalRatio) {
                    lastCriticalRatioIndex = i;
                } else if (n > nLongTermWindow && ratio < criticalRatio) {
                    lastCriticalRatioIndex = -1;
                }
            }
            
            // if triggered, restart lval
            if (triggerRatio != 0.0 && ratio > Math.abs(triggerRatio))
                lval = sval;
            
            ratioLast = ratio;
            
        }
        
        return(sample);
        
    }
    
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
        super.updateFields(timeSeries);
        
    }

    @Override
    public double[] apply(double dt, double[] sample) throws TimeDomainException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}	// End class


