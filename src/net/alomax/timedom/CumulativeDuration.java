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



public class CumulativeDuration extends TimeDomainProcess {
    
    public double thresholdLevel = 0.5;
    public double cutoffLevel = 0.1;
    public double durationFraction = -1.0;
    public double durationMin = 0.0;
    
    public String errorMessage;
    
    // picks
    protected Vector cumDurPicks = new Vector();
    
    private static final double THRESHOLD_MIN = 0.0;
    private static final double THRESHOLD_MAX = 1.0;
    private static final double CUTOFF_MIN = 0.0;
    private static final double CUTOFF_MAX = 1.0;
    private static final double DURATION_FRACTION_MIN = -1.1;
    private static final double DURATION_FRACTION_MAX = Double.MAX_VALUE;
    private static final double DURATION_MIN_MIN = 0.0;
    private static final double DURATION_MIN_MAX = Double.MAX_VALUE;
    
    
    /** constructor */
    
    public CumulativeDuration(String localeText, double thresholdLevel, double cutoffLevel, double durationFraction, double durationMin) {
        this.thresholdLevel = thresholdLevel;
        this.cutoffLevel = cutoffLevel;
        this.durationFraction = durationFraction;
        this.durationMin = durationMin;
        this.errorMessage = " ";
        
        TimeDomainText.setLocale(localeText);
    }
    
    
    /** Method to set thresholdLevel value */
    
    public void setThresholdLevel(double thresholdLevelValue) throws TimeDomainException {
        
        if (thresholdLevelValue < THRESHOLD_MIN || thresholdLevelValue > THRESHOLD_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_threshold_value + ": " + thresholdLevelValue);
        }
        
        thresholdLevel = thresholdLevelValue;
    }
    
    
    /** Method to set thresholdLevel value */
    
    public void setThresholdLevel(String str) throws TimeDomainException {
        
        double thresholdLevelValue;
        
        try {
            thresholdLevelValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_threshold_value + ": " + str);
        }
        
        setThresholdLevel(thresholdLevelValue);
    }
    
    
    /** Method to set cutoffLevel value */
    
    public void setCutoffLevel(double cutoffLevelValue) throws TimeDomainException {
        
        if (cutoffLevelValue < CUTOFF_MIN || cutoffLevelValue > CUTOFF_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_cutoff_value + ": " + cutoffLevelValue);
        }
        
        cutoffLevel = cutoffLevelValue;
    }
    
    
    /** Method to set cutoffLevel value */
    
    public void setCutoffLevel(String str) throws TimeDomainException {
        
        double cutoffLevelValue;
        
        try {
            cutoffLevelValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_cutoff_value + ": " + str);
        }
        
        setCutoffLevel(cutoffLevelValue);
    }
    
    
    /** Method to set durationFraction value */
    
    public void setDurationFraction(double durationFractionValue) throws TimeDomainException {
        
        if (durationFractionValue < DURATION_FRACTION_MIN || durationFractionValue > DURATION_FRACTION_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_duration_fraction_value + ": " + durationFractionValue);
        }
        
        durationFraction = durationFractionValue;
    }
    
    
    /** Method to set durationFraction value */
    
    public void setDurationFraction(String str) throws TimeDomainException {
        
        double durationFractionValue;
        
        try {
            durationFractionValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_duration_fraction_value + ": " + str);
        }
        
        setDurationFraction(durationFractionValue);
    }
    
    
    
    /** Method to set durationMin value */
    
    public void setDurationMin(double durationMinValue) throws TimeDomainException {
        
        if (durationMinValue < DURATION_MIN_MIN || durationMinValue > DURATION_MIN_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_duration_min_value + ": " + durationMinValue);
        }
        
        durationMin = durationMinValue;
    }
    
    
    /** Method to set durationMin value */
    
    public void setDurationMin(String str) throws TimeDomainException {
        
        double durationMinValue;
        
        try {
            durationMinValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_cumdur_duration_min_value + ": " + str);
        }
        
        setDurationMin(durationMinValue);
    }
    
    
    
    
    
    /** Method to get pick trigger indeces
     *
     * @returns triggerPickData Vector of int[2] where int[0] is trigger index
     *  and int[1] is last index where STA/LTA ratio equaled 1.0
     */
    
    public Vector getPickData() {
        
        return(cumDurPicks);
        
    }
    
    
    
    
    
    /** Method to check settings */
    
    public void checkSettings() throws TimeDomainException {
        
        String errMessage = "";
        int badSettings = 0;
        
        if (thresholdLevel < THRESHOLD_MIN || thresholdLevel > THRESHOLD_MAX) {
            errMessage += ": " + TimeDomainText.invalid_cumdur_threshold_value;
            badSettings++;
        }
        
        if (cutoffLevel < CUTOFF_MIN || cutoffLevel > CUTOFF_MAX) {
            errMessage += ": " + TimeDomainText.invalid_cumdur_cutoff_value;
            badSettings++;
        }
        
        if (durationFraction < DURATION_FRACTION_MIN || durationFraction > DURATION_FRACTION_MAX) {
            errMessage += ": " + TimeDomainText.invalid_cumdur_duration_fraction_value;
            badSettings++;
        }
        
        if (durationMin < DURATION_MIN_MIN || durationMin > DURATION_MIN_MAX) {
            errMessage += ": " + TimeDomainText.invalid_cumdur_duration_min_value;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }
        
    }
    
    
    
    /*** function to accumulate duration of signal where amplitude > threshold * max amplitude  */
    
    public final float[] apply(double dt, float[] sample) {
        
        if (durationFraction <= 0.0)
            return(applyImpl(dt, sample));
        else
            return(applyImpl(dt, sample, durationFraction, cutoffLevel));
        
    }
    
    
    
    
    /*** function to accumulate duration of signal where amplitude > threshold * max amplitude  */
    
    public final float[] applyImpl(double dt, float[] sample) {
        
        // find peak amplitude
        float valMax = -Float.MAX_VALUE;
        int indexOfMax = 0;
        for (int n = 0; n < sample.length; n++) {
            float val = sample[n];
            if (val > valMax ) {
                valMax = val;
                indexOfMax = n;
            }
        }
        
        float threshold = (float) thresholdLevel * valMax;
        double cumulDur = 0.0;
        double[] pickIndices = new double[2];
        // accumulate duration above threshold * amplitude
        for (int n = 0; n < sample.length; n++) {
            float val = sample[n];
            if (n <= indexOfMax || val > threshold ) {
                cumulDur += dt;
                pickIndices[0] = n;
                pickIndices[1] = n;
            }
        }
        
        
        PickData pickData = new PickData(pickIndices[0], pickIndices[1],
        PickData.POLARITY_UNKNOWN, cumulDur, PickData.INDEP_VAR_UNITS);
        cumDurPicks = new Vector();
        cumDurPicks.add(pickData);
        
        
        return(sample);
        
    }
    
    
    
    
    
    /*** function to accumulate duration of signal where amplitude > threshold * max amplitude
     *
     *  This version will only determine peak and accumlate duration in windows from start to
     *  (timeEvent + durationFraction * timeEvent), where timeEvent is the peak time or threshold first occurence time
     *
     *  This algoirthm is intended to help avoid including a second packet of energy in the calculation
     *
     */
    
    public final float[] applyImpl(double dt, float[] sample, double durationFraction, double cutoffLevel) {
        
        // find peak amplitude
        float valMax = -Float.MAX_VALUE;
        double time = 0.0;
        double stopTime = Double.MAX_VALUE;
        int indexOfMax = 0;
        for (int n = 0; n < sample.length; n++) {
            time += dt;
            float val = sample[n];
            if (val > valMax ) {
                valMax = val;
                indexOfMax = n;
                double duration = durationFraction * time + dt;
                // AJL 20080130 - BUG fix
                //duration = duration < durationMin ? durationMin: duration;
                stopTime = time + duration;
                // AJL 20080130 - BUG fix
                stopTime = stopTime < durationMin ? durationMin: stopTime;
            }
            if (time > stopTime)    // reached (timeEvent + durationFraction * timeEvent)
                break;
        }
        
        
        float threshold = (float) thresholdLevel * valMax;
        float cutoff = (float) cutoffLevel * valMax;
        time = 0.0;
        stopTime = Double.MAX_VALUE;
        boolean reachedCutoff = false;
        double cumulDur = 0.0;
        double[] pickIndices = new double[2];
        // accumulate duration above threshold * amplitude
        for (int n = 0; n < sample.length; n++) {
            time += dt;
            float val = sample[n];
            if (n <= indexOfMax || val > threshold ) {      // before max or value > threshold
                // AJL 20080513 - BUG fix, changed following line, now accumulates duration upto last drop below threshold
                //   before, accumulated only when above threshold.
                //cumulDur += dt;
                cumulDur = time;
                pickIndices[0] = n;
                pickIndices[1] = n;
                // AJL 20080513 - BUG fix, added following 2 lines, before could have pre-mature stop
                reachedCutoff = false;
                stopTime = Double.MAX_VALUE;
            } else if (val >= cutoff) {    // value above cutoff
                reachedCutoff = false;
                stopTime = Double.MAX_VALUE;
            } else if (!reachedCutoff && val < cutoff) {    // value falls below cutoff
                reachedCutoff = true;
                double duration = durationFraction * time + dt;
                // AJL 20080130 - BUG fix
                //duration = duration < durationMin ? durationMin: duration;
                stopTime = time + duration;
                // AJL 20080130 - BUG fix
                stopTime = stopTime < durationMin ? durationMin: stopTime;
            }
            if (time > stopTime)    // reached (timeEvent + durationFraction * timeEvent)
                break;
        }
        
        
        PickData pickData = new PickData(pickIndices[0], pickIndices[1],
        PickData.POLARITY_UNKNOWN, cumulDur, PickData.INDEP_VAR_UNITS);
        cumDurPicks = new Vector();
        cumDurPicks.add(pickData);
        
        
        return(sample);
        
    }
    
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
        timeSeries.ampUnits = PhysicalUnits.NO_UNITS;
        
    }
    
    
    
    /** Returns true if this process modifies trace amplitude
     *
     * @return    true if this process modifies trace amplitude.
     */
    
    public boolean amplititudeModified() {
        
        return(false);
        
    }

    @Override
    public double[] apply(double dt, double[] sample) throws TimeDomainException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}	// End class


