/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2008 Anthony Lomax <anthony@alomax.net www.alomax.net>
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

public class DurationAmplitude extends TimeDomainProcess {

    public static final double MWPD_CONST = 1.886E+019;  // from net.alomax.seisgram2k.calc.AmplitudeDurationMagnitudeCalculator
    public double mwpdConst = MWPD_CONST;
    private static final double MWPD_CONST_MIN = -Double.MAX_VALUE;
    private static final double MWPD_CONST_MAX = Double.MAX_VALUE;
    public static final String RESULT_MOMENT = "MOMENT";
    public static final String RESULT_MAGNITUDE = "MAGNITUDE";
    public static final String RESULT_ERROR = "ERROR";
    public static final int MOMENT = 0;
    public static final int MAGNITUDE = 1;
    public int resultType = MOMENT;
    public String errorMessage;

    /**
     * constructor
     */
    public DurationAmplitude(String localeText, double mwpdConst) {

        this.mwpdConst = mwpdConst;
        this.errorMessage = " ";

        TimeDomainText.setLocale(localeText);
    }

    /**
     * Method to set mwpdConst
     */
    public void setMwpdConst(double mwpdConstValue) throws TimeDomainException {
        if (mwpdConstValue < MWPD_CONST_MIN || mwpdConstValue > MWPD_CONST_MAX) {
            throw new TimeDomainException(
                    TimeDomainText.invalid_mwpdConst_value);
        }

        mwpdConst = mwpdConstValue;
    }

    /**
     * Method to set mwpdConst
     */
    public void setMwpdConst(String str) throws TimeDomainException {

        double mwpdConstValue;

        try {
            mwpdConstValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_mwpdConst_value);
        }

        setMwpdConst(mwpdConstValue);
    }

    /**
     * Method to set results mode
     */
    public void setResultsType(String str) throws TimeDomainException {

        if (RESULT_MOMENT.startsWith(str.toUpperCase())) {
            resultType = MOMENT;
        } else if (RESULT_MAGNITUDE.startsWith(str.toUpperCase())) {
            resultType = MAGNITUDE;
        } else {
            throw new TimeDomainException(TimeDomainText.invalid_result_value + ": " + str);
        }

    }

    /**
     * Method to get results mode as String
     */
    public String getResultsTypeString() {

        if (resultType == MOMENT) {
            return (RESULT_MOMENT);
        } else if (resultType == MAGNITUDE) {
            return (RESULT_MAGNITUDE);
        } else {
            return (RESULT_ERROR);
        }

    }

    /**
     * Method to check settings
     */
    public void checkSettings() throws TimeDomainException {

        String errMessage = "";
        int badSettings = 0;

        if (mwpdConst < MWPD_CONST_MIN || mwpdConst > MWPD_CONST_MAX) {
            errMessage += ": " + TimeDomainText.invalid_mwpdConst_value;
            badSettings++;
        }

        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }

    }
    /**
     * * function to calculate energy duration magnitude from trace of cumulative energy
     *
     * implements ... (Lomax et al., 2006)
     *
     */
    // moment magnitude parameters
    protected static double log10 = Math.log(10.0);
    protected static double multiplier = 2.0 / 3.0;
    protected static double constant = 9.1;

    public final float[] apply(double dt, float[] sample) {


        double value, magnitude;

        sample[0] = 0.0f;

        double duration = dt;
        for (int i = 1; i < sample.length; i++) {

            value = mwpdConst * sample[i];
            if (resultType == MOMENT) {
                sample[i] = (float) value;
            } else {
                magnitude = 0.0;
                if (value > Float.MIN_VALUE) {
                    magnitude = multiplier * (Math.log(value) / log10 - constant);
                }
                sample[i] = (float) magnitude;
            }
            duration += dt;

        }

        return (sample);

    }

    /**
     * Update fields in TimeSeries object
     */
    public void updateFields(TimeSeries timeSeries) {

        if (resultType == MOMENT) {
            timeSeries.ampUnits = "Nm";
        } else if (resultType == MAGNITUDE) {
            timeSeries.ampUnits = "Mwpd";
        } else {
            timeSeries.ampUnits = RESULT_ERROR;
        }


    }

    /**
     * Returns true if this process modifies trace amplitude
     *
     * @return true if this process modifies trace amplitude.
     */
    public boolean amplititudeModified() {

        return (true);

    }
}	// End class GaussianFilter

