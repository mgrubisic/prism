/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 1999 Anthony Lomax <lomax@faille.unice.fr>
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
package net.alomax.freq;

import net.alomax.math.*;

public abstract class BandpassFilter implements FrequencyDomainProcess {

    public double highFreqCorner;
    public double lowFreqCorner;
    public int numPoles;
    public String errorMessage;
    public static final double FREQ_MIN = 1.0e-8;
    public static final double FREQ_MAX = 1.0e8;
    public static final double NUM_POLES_MIN = 2;
    public static final double NUM_POLES_MAX = 20;
    public static final double PI = Math.PI;
    public static final double TWOPI = 2.0 * Math.PI;

    /**
     * constructor
     */
    public BandpassFilter(double lowFreqCorner, double highFreqCorner, int numPoles) {
        this.highFreqCorner = highFreqCorner;
        this.lowFreqCorner = lowFreqCorner;
        this.numPoles = numPoles;
        this.errorMessage = " ";

    }

    /**
     * constructor
     */
    public BandpassFilter(String localeText, double lowFreqCorner, double highFreqCorner, int numPoles) {
        this.highFreqCorner = highFreqCorner;
        this.lowFreqCorner = lowFreqCorner;
        this.numPoles = numPoles;
        this.errorMessage = " ";

        FreqText.setLocale(localeText);
    }

    /**
     * Method to set high frequency corner
     */
    public void setHighFreqCorner(double freqValue)
            throws FilterException {
        if (freqValue < FREQ_MIN || freqValue > FREQ_MAX) {
            throw new FilterException(
                    FreqText.invalid_high_frequency_corner);
        }

        highFreqCorner = freqValue;
    }

    /**
     * Method to set high frequency corner
     */
    public void setHighFreqCorner(String str)
            throws FilterException {

        double freqValue;

        try {
            freqValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new FilterException(
                    FreqText.invalid_high_frequency_corner);
        }

        setHighFreqCorner(freqValue);
    }

    /**
     * Method to set low frequency corner
     */
    public void setLowFreqCorner(double freqValue)
            throws FilterException {
        if (freqValue < FREQ_MIN || freqValue > FREQ_MAX) {
            throw new FilterException(
                    FreqText.invalid_low_frequency_corner);
        }

        lowFreqCorner = freqValue;
    }

    /**
     * Method to set low frequency corner
     */
    public void setLowFreqCorner(String str)
            throws FilterException {

        double freqValue;

        try {
            freqValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new FilterException(
                    FreqText.invalid_low_frequency_corner);
        }

        setLowFreqCorner(freqValue);
    }

    /**
     * Method to set number of poles
     */
    public void setNumPoles(int nPoles)
            throws FilterException {

        if (nPoles < NUM_POLES_MIN || nPoles > NUM_POLES_MAX || nPoles % 2 != 0) {
            throw new FilterException(
                    FreqText.invalid_number_of_poles);
        }

        numPoles = nPoles;
    }

    /**
     * Method to set number of poles
     */
    public void setNumPoles(String str)
            throws FilterException {

        int nPoles;

        try {
            nPoles = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new FilterException(
                    FreqText.invalid_number_of_poles);
        }

        setNumPoles(nPoles);
    }

    /**
     * Method to check settings
     */
    public void checkSettings() throws FilterException {

        String errMessage = "";
        int badSettings = 0;

        if (highFreqCorner < FREQ_MIN || highFreqCorner > FREQ_MAX) {
            errMessage += ": " + FreqText.invalid_high_frequency_corner;
            badSettings++;
        }

        if (lowFreqCorner < FREQ_MIN || lowFreqCorner > FREQ_MAX) {
            errMessage += ": " + FreqText.invalid_low_frequency_corner;
            badSettings++;
        }

        if (lowFreqCorner >= highFreqCorner) {
            errMessage +=
                    ": " + FreqText.low_corner_greater_than_high_corner;
            badSettings++;
        }

        if (numPoles < NUM_POLES_MIN || numPoles > NUM_POLES_MAX || numPoles % 2 != 0) {
            errMessage += ": " + FreqText.invalid_number_of_poles;
            badSettings++;
        }

        if (badSettings > 0) {
            throw new FilterException(errMessage + ".");
        }

    }

    /**
     * method to apply filter in freq domain
     */
    public abstract Cmplx[] apply(double dt, Cmplx[] cx);

    /**
     * Does pre-processing on the time-domain data.
     */
    public float[] preProcess(double dt, float[] x) {
        return (x);	// do nothing
    }

    /**
     * Does post-processing on the time-domain data.
     */
    public float[] postProcess(double dt, float[] x) {
        return (x);	// do nothing
    }

    /**
     * Returns true if pre-processing on the time-domain data changes samples.
     *
     * @return true if post-processing on the time-domain data changes samples, false otherwise.
     */
    public boolean sampleChangedInPreProcess() {
        return (false);
    }

    /**
     * Returns true if post-processing on the time-domain data changes samples.
     *
     * @return true if post-processing on the time-domain data changes samples, false otherwise.
     */
    public boolean sampleChangedInPostProcess() {
        return (false);
    }

    /**
     * Update fields in TimeSeries object
     */
    public void updateFields(TimeSeries timeSeries) {
    }
}	// End class BandpassFilter

