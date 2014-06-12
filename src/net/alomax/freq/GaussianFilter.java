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

public class GaussianFilter implements FrequencyDomainProcess {

    public double centFreq;
    public double alpha;
    public String errorMessage;
    private static final double FREQ_MIN = Double.MIN_VALUE;
    private static final double FREQ_MAX = Double.MAX_VALUE;
    private static final double ALPHA_MIN = Double.MIN_VALUE;
    private static final double ALPHA_MAX = Double.MAX_VALUE;

    /**
     * constructor
     */
    public GaussianFilter(double centFreq, double alpha) {
        
        this.centFreq = centFreq;
        this.alpha = alpha;
        this.errorMessage = " ";

    }

    /**
     * constructor
     */
    public GaussianFilter(String localeText, double centFreq, double alpha) {
        
        this.centFreq = centFreq;
        this.alpha = alpha;
        this.errorMessage = " ";

        FreqText.setLocale(localeText);
    }

    /**
     * Method to set center frequency
     */
    public void setCentFreq(double freqValue) throws FilterException {
        if (freqValue < FREQ_MIN || freqValue > FREQ_MAX) {
            throw new FilterException(
                    FreqText.invalid_center_frequency);
        }

        centFreq = freqValue;
    }

    /**
     * Method to set center frequency
     */
    public void setCentFreq(String str)
            throws FilterException {

        double freqValue;

        try {
            freqValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new FilterException(
                    FreqText.invalid_center_frequency);
        }

        setCentFreq(freqValue);
    }

    /**
     * Method to set alpha
     */
    public void setAlpha(double alphaValue)
            throws FilterException {
        if (alphaValue < ALPHA_MIN || alphaValue > ALPHA_MAX) {
            throw new FilterException(
                    FreqText.invalid_alpha_value);
        }

        alpha = alphaValue;
    }

    /**
     * Method to set alpha
     */
    public void setAlpha(String str)
            throws FilterException {

        double alphaValue;

        try {
            alphaValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new FilterException(
                    FreqText.invalid_alpha_value);
        }

        setAlpha(alphaValue);
    }

    /**
     * Method to check settings
     */
    public void checkSettings() throws FilterException {

        String errMessage = "";
        int badSettings = 0;

        if (centFreq < FREQ_MIN || centFreq > FREQ_MAX) {
            errMessage += ": " + FreqText.invalid_center_frequency;
            badSettings++;
        }

        if (alpha < ALPHA_MIN || alpha > ALPHA_MAX) {
            errMessage += ": " + FreqText.invalid_alpha_value;
            badSettings++;
        }

        if (badSettings > 0) {
            throw new FilterException(errMessage + ".");
        }

    }

    /**
     * * function to do gaussian filter in frequency domain
     */
    public final Cmplx[] apply(double dtime, Cmplx[] cz) {

        //void gauss_filt(np, dtime, cz, fcent, alpha)
        //int np;
        //double dtime;
        //fcomplex cz[];
        //double fcent, alpha;


        double wcent = 2.0 * Math.PI * centFreq;
        //System.out.println("cz.length "+cz.length+"  ((double) cz.length)) "+((double) cz.length));
        double freq0 = 2.0 * Math.PI / (((double) (cz.length - 1) + 1.0) * dtime);
        // fix bug with (double) operator in JDK 1.1.8 win32 jit compiler !!!
        //double freq0 = 2.0 * Math.PI / ((double) cz.length * dtime);

        int i1, i2;
        int np = cz.length;
        int np2 = cz.length / 2;
        double w;
        double wdiff;
        Cmplx ctf;
        Cmplx chalf = new Cmplx(0.5, 0.);
        for (int i = 0; i < np2; i++) {
            w = freq0 * (double) (i + 1);
            wdiff = (w - wcent) / wcent;
            ctf = new Cmplx(Math.exp(-alpha * wdiff * wdiff), 0.0);
            i1 = i + 1;
            i2 = np - 1 - i;
            if (i != np2 - 1) {
                cz[i1].mul(ctf);
                cz[i2].mul(ctf);
            } else {
                cz[i1].mul(ctf.mul(chalf));
            }
        }
        cz[0] = new Cmplx(0., 0.);

        return (cz);

    }

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

    @Override
    public double[] preProcess(double dt, double[] x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double[] postProcess(double dt, double[] x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}	// End class GaussianFilter

