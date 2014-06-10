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

import net.alomax.math.TimeSeries;

public class InstantPeriodWindowed extends TimeDomainProcess {

    public double windowWidth;
    public String errorMessage;
    private static final double WIDTH_MIN = Double.MIN_VALUE;
    private static final double WIDTH_MAX = Double.MAX_VALUE;
    protected TimeDomainMemoryDouble memory_double_dval = null;

    /**
     * constructor
     *
     * @param localeText
     * @param windowWidth
     */
    public InstantPeriodWindowed(double windowWidth) {
        this.windowWidth = windowWidth;
        this.errorMessage = " ";
    }

    /**
     * constructor
     *
     * @param localeText
     * @param windowWidth
     */
    public InstantPeriodWindowed(String localeText, double windowWidth) {
        this.windowWidth = windowWidth;
        this.errorMessage = " ";

        TimeDomainText.setLocale(localeText);
    }

    /**
     * copy constructor
     *
     * @param intantPeriodWindowd
     */
    public InstantPeriodWindowed(InstantPeriodWindowed intantPeriodWindowd) {

        this.windowWidth = intantPeriodWindowd.windowWidth;

        this.useMemory = intantPeriodWindowd.useMemory;
        if (intantPeriodWindowd.memory != null) {
            this.memory = new TimeDomainMemoryDouble((TimeDomainMemoryDouble) intantPeriodWindowd.memory);
        }
        if (intantPeriodWindowd.memory_double_dval != null) {
            this.memory_double_dval = new TimeDomainMemoryDouble(intantPeriodWindowd.memory_double_dval);
        }

    }

    /**
     * Method to set windowWidth
     *
     * @param windowWidthValue
     * @throws TimeDomainException
     */
    public void setWindowWidth(double windowWidthValue)
            throws TimeDomainException {
        if (windowWidthValue < WIDTH_MIN || windowWidthValue > WIDTH_MAX) {
            throw new TimeDomainException(
                    TimeDomainText.invalid_window_width_value);
        }

        windowWidth = windowWidthValue;
    }

    /**
     * Method to set windowWidth
     *
     * @param str
     * @throws TimeDomainException
     */
    public void setWindowWidth(String str)
            throws TimeDomainException {

        double windowWidthValue;

        try {
            windowWidthValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(
                    TimeDomainText.invalid_window_width_value);
        }

        setWindowWidth(windowWidthValue);
    }

    /**
     * Returns true if this process supports memory usage
     *
     * @return true if this process supports memory usage or does not require memory.
     */
    @Override
    public boolean supportsMemory() {

        return (true);

    }

    /**
     * Clears the Memory object for this process
     *
     */
    @Override
    public void clearMemory() {

        this.memory = null;
        this.memory_double_dval = null;

    }

    /**
     * Method to check settings
     *
     * @throws TimeDomainException
     */
    public void checkSettings() throws TimeDomainException {

        String errMessage = "";
        int badSettings = 0;

        if (windowWidth < WIDTH_MIN || windowWidth > WIDTH_MAX) {
            errMessage += ": " + TimeDomainText.invalid_window_width_value;
            badSettings++;
        }

        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }

    }
    /**
     * * function to calculate instantaneous period in a fixed window (Tau_c)
     *
     * implements eqs 1-3 in : Allen, R.M., and H. Kanamori, The Potential for Earthquake Early Warning in Southern California, Science, 300 (5620),
     * 786-789, 2003.
     *
     * except use a fixed width window instead of decay function
     */
    private static double MIN_FLOAT_VAL = 1.0e-20;

    public final float[] apply(double deltaTime, float[] sample) {

        TimeDomainMemoryDouble memory_double = (TimeDomainMemoryDouble) memory;

        int windowLength = (int) Math.round(windowWidth / deltaTime);
        if (windowLength < 1) {
            windowLength = 1;
        }

        float[] sampleNew = new float[sample.length];
        double[] deriv = new double[sample.length];

        useMemory = true;   // alway use memory (simplifies recursive code below)

        if (useMemory) { // use stored memory
            if (memory_double == null) // no stored memory initialized
            {
                memory_double = new TimeDomainMemoryDouble(windowLength, 0.0, 1, 0.0);
                memory = memory_double;
            }
            if (memory_double_dval == null) // no stored memory_double_dval initialized
            {
                memory_double_dval = new TimeDomainMemoryDouble(windowLength, 0.0, 1, 0.0);
            }
        }

        // use double precision where possible to avoid loss of precision in accumulating xval and dval
        double twopi = 2.0 * Math.PI;
        double sampleLast = 0.0;
        double sample_d;
        double deriv_d;
        double xval = 0.0;
        double dval = 0.0;
        if (useMemory) { // using stored memory
            sampleLast = memory_double.input[windowLength - 1];
            xval = memory_double.output[0];
            dval = memory_double_dval.output[0];
        }

        int i;
        for (i = 0; i < sample.length; i++) {

            sample_d = (double) sample[i];
            deriv_d = (sample_d - sampleLast) / deltaTime;
            int indexBegin = i - windowLength;
            if (indexBegin >= 0) {
                xval = xval - ((double) sample[indexBegin]) * ((double) sample[indexBegin]) + sample_d * sample_d;
                dval = dval - deriv[indexBegin] * deriv[indexBegin] + deriv_d * deriv_d;
            } else {
                int index = i;
                xval = xval - memory_double.input[index] * memory_double.input[index] + sample_d * sample_d;
                dval = dval - memory_double_dval.input[index] * memory_double_dval.input[index] + deriv_d * deriv_d;
            }
            deriv[i] = deriv_d;
            sampleLast = sample_d;
            //if (xval > MIN_FLOAT_VAL && dval > MIN_FLOAT_VAL) {
            if (dval > MIN_FLOAT_VAL) {
                sampleNew[i] = (float) (twopi * Math.sqrt(xval / dval));
            } else {
                sampleNew[i] = 0.0f;
            }

        }

        // save memory if used
        if (useMemory) { // using stored memory
            memory_double.output[0] = xval;
            memory_double.updateInput(sample);
            memory_double_dval.output[0] = dval;
            memory_double_dval.updateInput(deriv);
        }

        return (sampleNew);

    }

    /**
     * Update fields in TimeSeries object
     */
    public void updateFields(TimeSeries timeSeries) {

        timeSeries.ampUnits = timeSeries.timeUnits;

    }

    /**
     * Returns true if this process modifies trace amplitude
     *
     * @return true if this process modifies trace amplitude.
     */
    public boolean amplititudeModified() {

        return (true);

    }

    /**
     * Main method for stand-alone testing
     *
     */
    public static void main(String argv[]) {

        try {

            // create a synthetic trace
            FunctionGenerator funcGen = new FunctionGenerator();
            funcGen.setType(FunctionGenerator.TYPE_SINE);
            double params[] = new double[2];
            params[0] = 1.0;    // frequency (Hz)
            params[1] = 0.0;    // phase (deg)
            funcGen.setParams(params);
            double dt = 0.05;   // sec
            float[] sample = new float[80];   // 4 sec
            sample = funcGen.apply(dt, sample);

            // display sample
            System.out.println("Input trace samples (" + (1.0 / params[0]) + "sec sinusoid):");
            for (int i = 0; i < sample.length; i++) {
                System.out.print(" " + sample[i]);
            }
            System.out.println("");
            System.out.println("");

            // get the instantaneous period in a fixed window (Tau_c)
            double windowWidth = 2.0;   // sec
            InstantPeriodWindowed instantPeriodWindowed = new InstantPeriodWindowed(windowWidth);
            sample = instantPeriodWindowed.apply(dt, sample);

            // display instantaneous period
            System.out.println("Output instantaneous period (sec):");
            for (int i = 0; i < sample.length; i++) {
                System.out.print(" " + sample[i]);
            }
            System.out.println("");

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}	// End class

