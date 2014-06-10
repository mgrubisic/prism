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
import net.alomax.util.PhysicalUnits;



public class FrequencyProcesses implements FrequencyDomainProcess {
    
    protected static int ndx = -1;
    public static final int UNDEF = ndx++;
    public static final int DIFFERENTIATE = ndx++;
    public static final int INTEGRATE = ndx++;
    public static final int HILBERT = ndx++;
    public static final int ENVELOPE = ndx++;
    public static final int REMOVE_ATTENUATION = ndx++;
    
    public int processID = UNDEF;
    
    // envelope
    protected float[] origSample;
    
    // general parameters
    protected Object[] params = new Object[0];
    
    
    /** constructor */
    
    public FrequencyProcesses(int processID) {
        this.processID = processID;
    }
    
    
    /**  set general parameters.
     */
    
    public void setParams(Object[] params) {
        
        this.params = params;
        
    }
    
    
    
    /**  Does pre-processing on the time-domain data.
     */
    
    public float[] preProcess(double dt, float[] x) {
        
        if (processID == ENVELOPE) {
            // store original data
            origSample = new float[x.length];
            System.arraycopy(x, 0, origSample, 0, x.length);
            return(x);	// do nothing to original data
        } else {
            return(x);	// do nothing
        }
    }
    
    
    /**  Does post-processing on the time-domain data.
     */
    
    public float[] postProcess(double dt, float[] x) {
        
        if (processID == ENVELOPE) {
            for (int i = 0; i < x.length; i++)
                x[i] = (float) Math.sqrt(x[i] * x[i] + origSample[i] * origSample[i]);
            return(x);
        } else {
            return(x);	// do nothing
        }
        
    }
    
    
    /**  Returns true if pre-processing on the time-domain data changes samples.
     *
     * @return    true if post-processing on the time-domain data changes samples, false otherwise.
     */
    
    public boolean sampleChangedInPreProcess() {
        return(false);
    }
    
    
    /**  Returns true if post-processing on the time-domain data changes samples.
     *
     * @return    true if post-processing on the time-domain data changes samples, false otherwise.
     */
    
    public boolean sampleChangedInPostProcess() {
        if (processID == ENVELOPE) {
            return(true);
        } else {
            return(false);
        }
        
    }
    
    
    
    
    /**  Method to Differentiate, Integrate, Hilbert transform or calculate Envelope in the freq domain
     */
    
    static final Cmplx cHalf = new Cmplx(0.5, 0.0);
    static final Cmplx cOne = new Cmplx(1.0, 0.0);
    static final Cmplx cNegOne = new Cmplx(-1.0, 0.0);
    
    public Cmplx[] apply(double dt, Cmplx[] cz) {
        
        if (processID == REMOVE_ATTENUATION)
            return(applyRemoveAttenuation(dt, cz));
        
        return(applyBasic(dt, cz));
        
    }
    
    
    /**  Method to Differentiate, Integrate, Hilbert transform or calculate Envelope in the freq domain
     */
    
    public Cmplx[] applyBasic(double dt, Cmplx[] cz) {
        
        
        int np = cz.length;
        double w0 = 2.0 * Math.PI / ((double) np * dt);
        
        Cmplx ctf = new Cmplx(0.0, 0.0);
        //		if (processID == HILBERT)
        ctf = new Cmplx(0.0, -1.0);
        //		if (processID == ENVELOPE)
        //			ctf = new Cmplx(0.0, 0.0);
        
        int i1, i2;
        double w;
        int np2 = np / 2;
        for (int i = 0; i < np2; i++) {
            i1 = i + 1;
            w = w0 * (double) (i1);
            if (processID == INTEGRATE) {
                ctf = Cmplx.div(cNegOne, new Cmplx(0.0, w));
            } else if (processID == DIFFERENTIATE) {
                ctf = new Cmplx(0., -w);
            }
            i2 = np - 1 - i;
            if (i1 != np2) {
                //				if (processID != ENVELOPE)
                cz[i1].mul(ctf);
                cz[i2].mul(Cmplx.conjg(ctf));
            } else {
                cz[i1].mul(Cmplx.mul(Cmplx.conjg(ctf), cHalf));
            }
        }
        cz[0] = new Cmplx(0., 0.);
        
        return(cz);
        
    }
    
    
    /**  Method to Remove attenuation in the freq domain
     */
    
    public Cmplx[] applyRemoveAttenuation(double dt, Cmplx[] cz) {
        
        
        int np = cz.length;
        double w0 = 2.0 * Math.PI / ((double) np * dt);
        
        Cmplx ctf = new Cmplx(1.0, 0.0);
        
        double tStar = ((Double) params[0]).doubleValue();
        double wmax = ((Double) params[1]).doubleValue() * (2.0 * Math.PI);
        
        int i1, i2;
        double w;
        int np2 = np / 2;
        for (int i = 0; i < np2; i++) {
            i1 = i + 1;
            w = w0 * (double) (i1);
            if (w < wmax)
                ctf = new Cmplx(Math.exp(w * tStar / 2.0), 0.);     // increases amplitude to correct for attenuation
            else
                ctf = cOne;
            i2 = np - 1 - i;
            if (i1 != np2) {
                cz[i1].mul(ctf);
                cz[i2].mul(Cmplx.conjg(ctf));
            } else {
                cz[i1].mul(Cmplx.mul(Cmplx.conjg(ctf), cHalf));
            }
        }
        cz[0] = new Cmplx(0., 0.);
        
        return(cz);
        
    }
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
        if (processID == DIFFERENTIATE)
            timeSeries.ampUnits = PhysicalUnits.timeDerivative(timeSeries.ampUnits);
        else if (processID == INTEGRATE)
            timeSeries.ampUnits = PhysicalUnits.timeIntegral(timeSeries.ampUnits);
        
    }
    
    
    /** Method to check settings */
    
    public void checkSettings() throws FilterException {
        
        String errMessage = "";
        int badSettings = 0;
        
        if (processID == UNDEF) {
            errMessage += ": " + FreqText.invalid_frequency_process;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new FilterException(errMessage + ".");
        }
        
    }
    
    
    
    
}	// End class FrequencyProcesses


