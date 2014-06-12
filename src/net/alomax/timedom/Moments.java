/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2009 Anthony Lomax <anthony@alomax.net www.alomax.net>
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

public class Moments extends TimeDomainProcess {

    public static final int TYPE_KURTOSIS = 0;
    private static final int NUM_TYPES = 1;
    public int type = TYPE_KURTOSIS;
    // types - !!! MUST MATCH TYPES AND ORDER ABOVE
    public static final String NAME_KURTOSIS = "KURTOSIS";
    public int windowWidth = 50;
    public String errorMessage = " ";
    private static final int WINDOW_MIN = 1;
    private static final int WINDOW_MAX = Integer.MAX_VALUE;

    /** constructor */
    public Moments(String localeText, int type, int windowWidth) {
        this.type = type;
        this.windowWidth = windowWidth;

        TimeDomainText.setLocale(localeText);
    }

    /** copy constructor */
    public Moments(Moments moments) {

        this.type = moments.type;
        this.windowWidth = moments.windowWidth;

        this.useMemory = moments.useMemory;
        if (moments.memory != null) {
            this.memory = new TimeDomainMemory(moments.memory);
        }

    }

    /** Method to set moments window width */
    public void setWindowWidth(int windowWidth) throws TimeDomainException {
        if (windowWidth < WINDOW_MIN || windowWidth > WINDOW_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_moments_width_value + ": " + windowWidth);
        }

        this.windowWidth = windowWidth;
    }

    /** Method to set moments window width */
    public void setWindowWidth(String str) throws TimeDomainException {

        int Width;

        try {
            Width = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_moments_width_value + ": " + str);
        }

        setWindowWidth(Width);
    }

    /** Method to set moments type */
    public void setType(String typeStr) throws TimeDomainException {

        if (NAME_KURTOSIS.startsWith(typeStr.toUpperCase())) {
            type = TYPE_KURTOSIS;
        } else {
            throw new TimeDomainException(TimeDomainText.invalid_moments_type + ": " + typeStr);
        }


    }

    /** Method to set moments type */
    public void setType(int type) throws TimeDomainException {

        if (type >= 0 && type < NUM_TYPES) {
            this.type = type;
        } else {
            throw new TimeDomainException(TimeDomainText.invalid_moments_type + ": " + type);
        }


    }

    /** Method to get moments type */
    public int getType() {

        return (type);

    }

    /** Returns true if this process supports memory usage
     *
     * @return    true if this process supports memory usage or does not require memory.
     */
    public boolean supportsMemory() {

        return (true);

    }

    /** Method to check settings */
    public void checkSettings() throws TimeDomainException {

        String errMessage = "";
        int badSettings = 0;

        setWindowWidth(windowWidth);
        setType(type);

        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }

    }

    /*** function to apply moments  */
    public final double[] apply(double dt, double[] sample) {

        double[] newSample = sample;

        // useMemory = true forces causal filter
        //if (type == TYPE_CAUSAL_BOXCAR || type == TYPE_CAUSAL_TRIANGLE)
        useMemory = true;   // must be true!

        if (useMemory) { // use stored memory
            if (memory == null) // no stored memory initialized
            {
                memory = new TimeDomainMemory(2 * windowWidth, 0.0f, 0, 0.0f);
            }
        }

        if (type == TYPE_KURTOSIS) {
            newSample = applyMoment(dt, sample);
        }

        // save memory if used
        if (useMemory) { // using stored memory
            memory.updateInput(sample);
        }

        return (newSample);

    }

    /** function to apply moments  */
    // http://www.itl.nist.gov/div898/handbook/eda/section3/eda35b.htm
    
    public final double[] applyMoment(double dt, double[] sample) {

        if (windowWidth < 2) {
            return (sample);
        }

        int order = 4;

        double[] newSample = new double[sample.length];

        int i1, i2;
        double sum;
        double sum2;
        int icount;

        // get moment

        sum = 0.0;
        sum2 = 0.0;
        icount = 0;

        // initalize mean and sd

        i1 = -windowWidth;
        i2 = 0;     // causal boxcar of width windowWidth

        double value = 0.0f;
        for (int n = i1; n < i2; n++) {
            // add new value
            if (n < 0) {
                value = memory.input[windowWidth + n];
            } else {
                value = sample[n];
            }
            sum += value;
            sum2 += value * value;
            icount++;
        }

        // accumulate values
        int m;
        for (int i = 0; i < sample.length; i++) {

            value = sample[i];
            sum += value;
            sum2 += value * value;
            // subtract old value
            m = i - windowWidth;
            if (m < 0) {
                value = memory.input[windowWidth + m];
            } else {
                value = sample[m];
            }
            sum -= value;
            sum2 -= value * value;

            if (icount > 1) {
                double mean = sum / (double) icount;
                double variance = (sum2 - sum * mean) / (double) (icount - 1 + 1);
                double variance_order = Math.pow(Math.sqrt(variance), order);
                double order_sum = 0.0;
                i1 = i - windowWidth;
                i2 = i;     // causal boxcar of width windowWidth
                for (int n = i1; n < i2; n++) {
                    if (n < 0) {
                        value = memory.input[windowWidth + n];
                    } else {
                        value = sample[n];
                    }
                    order_sum += Math.pow(value - mean, order);
                }
                newSample[i] = (float) (order_sum / ((double) (icount - 1 + 1) * variance_order));
            } else {
                newSample[i] = 0.0f;
            }

        }

        return (newSample);

    }

    /**  Update fields in TimeSeries object */
    public void updateFields(TimeSeries timeSeries) {

    //timeSeries.ampUnits = PhysicalUnits.NO_UNITS;

    }

    /** Returns true if this process modifies trace amplitude
     *
     * @return    true if this process modifies trace amplitude.
     */
    public boolean amplititudeModified() {

        return (true);

    }

}	// End class GaussianFilter


