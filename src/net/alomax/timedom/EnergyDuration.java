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



public class EnergyDuration extends TimeDomainProcess {
    
    public static final double COEFF_MED = 1.55e10;
    public double coeff = COEFF_MED;
    private static final double COEFF_MIN = -Double.MAX_VALUE;
    private static final double COEFF_MAX = Double.MAX_VALUE;
    
    public static final double POWER_MED = 3.0 /2.0;
    public double power = POWER_MED;
    private static final double POWER_MIN = -Double.MAX_VALUE;
    private static final double POWER_MAX = Double.MAX_VALUE;
    
    public static final String RESULT_VALUE = "VALUE";
    public static final String RESULT_MAGNITUDE = "MAGNITUDE";
    public static final String RESULT_ERROR = "ERROR";
    public static final int VALUE = 0;
    public static final int MAGNITUDE = 1;
    public int resultType = VALUE;
    
    
    public String errorMessage;
    
    
    /** constructor */
    
    public EnergyDuration(String localeText, double coeff, double power) {
        
        this.coeff = coeff;
        this.power = power;
        this.errorMessage = " ";
        
        TimeDomainText.setLocale(localeText);
    }
    
    
    /** Method to set coeff */
    
    public void setCoeff(double coeffValue) throws TimeDomainException {
        if (coeffValue < COEFF_MIN || coeffValue > COEFF_MAX) {
            throw new TimeDomainException(
            TimeDomainText.invalid_coeff_value);
        }
        
        coeff = coeffValue;
    }
    
    
    /** Method to set coeff */
    
    public void setCoeff(String str) throws TimeDomainException {
        
        double coeffValue;
        
        try {
            coeffValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_coeff_value);
        }
        
        setCoeff(coeffValue);
    }
    
    
    
    /** Method to set power */
    
    public void setPower(double powerValue) throws TimeDomainException {
        if (powerValue < POWER_MIN || powerValue > POWER_MAX) {
            throw new TimeDomainException(
            TimeDomainText.invalid_power_value);
        }
        
        power = powerValue;
    }
    
    
    /** Method to set power */
    
    public void setPower(String str) throws TimeDomainException {
        
        double powerValue;
        
        try {
            powerValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_power_value);
        }
        
        setPower(powerValue);
    }
    
    
    /** Method to set results mode */
    
    public void setResultsType(String str) throws TimeDomainException {
        
        if (RESULT_VALUE.startsWith(str.toUpperCase()))
            resultType = VALUE;
        else if (RESULT_MAGNITUDE.startsWith(str.toUpperCase()))
            resultType = MAGNITUDE;
        else
            throw new TimeDomainException(TimeDomainText.invalid_result_value + ": " + str);
        
    }
    
    
    
    /** Method to get results mode as String */
    
    public String getResultsTypeString() {
        
        if (resultType == VALUE)
            return(RESULT_VALUE);
        else if (resultType == MAGNITUDE)
            return(RESULT_MAGNITUDE);
        else
            return(RESULT_ERROR);
        
    }
    
    
    
    /** Method to check settings */
    
    public void checkSettings() throws TimeDomainException {
        
        String errMessage = "";
        int badSettings = 0;
        
        if (coeff < COEFF_MIN || coeff > COEFF_MAX) {
            errMessage += ": " + TimeDomainText.invalid_coeff_value;
            badSettings++;
        }
        
        if (power < POWER_MIN || power > POWER_MAX) {
            errMessage += ": " + TimeDomainText.invalid_power_value;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }
        
    }
    
    
    
    /*** function to calculate energy duration magnitude from trace of cumulative energy
     *
     * implements ... (Lomax et al., 2006)
     *
     */
    
    public final float[] apply(double dt, float[] sample) {
        
        // moment magnitude parameters
        double log10 = Math.log(10.0);
        double multiplier = 2.0 / 3.0;
        double constant = 9.1;
        
        double value, magnitude;
        
        sample[0] = 0.0f;
        
        double duration = dt;
        for (int i = 1; i < sample.length; i++) {
            
            value = coeff * sample[i] * Math.pow(duration, power);
            if (resultType == VALUE) {
                sample[i] = (float) value;
            } else {
                magnitude = multiplier * (Math.log(value) / log10 - constant);
                sample[i] = (float) magnitude;
            }
            duration += dt;
            
        }
        
        return(sample);
        
    }
    
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
        timeSeries.ampUnits = timeSeries.timeUnits;
        
    }
    
    
    /** Returns true if this process modifies trace amplitude
     *
     * @return    true if this process modifies trace amplitude.
     */
    
    public boolean amplititudeModified() {
        
        return(true);
        
    }
    
    
    
}	// End class GaussianFilter


