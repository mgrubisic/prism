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



public class Convolve implements FrequencyDomainProcess {
    
    public static final int TYPE_CONV = 0;
    public static final int TYPE_DECONV = 1;
    private static final int NUM_TYPES = 2;
    public int type = TYPE_CONV;
    
    public static final String CONVOLUTION = "CONVOLUTION";
    public static final String DECONVOLUTION = "DECONVOLUTION";
    
    TimeSeries[] masterTimeSeries = null;
    protected int masterChannel = -1;
    
    public Cmplx[][] masterSpectra = null;
    
    public double waterLevel = -1.0;
    public double taperPercent = 0.0;
    
    public String errorMessage;
    
    
    
    /** constructor */
    
    public Convolve(String localeText, TimeSeries[] masterTimeSeries) {
        
        this.type = type;
        this.masterTimeSeries = masterTimeSeries;
        this.errorMessage = " ";
        
        FreqText.setLocale(localeText);
    }
    
    
    /** Method to set master TimeSeries */
    
    public void setMasterTimeSeries(TimeSeries[] masterTimeSeries) throws FilterException {
        
        if (masterTimeSeries == null || masterTimeSeries.length < 1) {
            throw new FilterException(FreqText.invalid_convolution_master_spectrum);
        }
        
        for (int i = 0; i < masterTimeSeries.length; i++) {
            if (masterTimeSeries[i] == null || masterTimeSeries[i].sampleLength() < 2) {
                throw new FilterException(FreqText.invalid_convolution_master_spectrum);
            }
        }
        
        this.masterTimeSeries = masterTimeSeries;
        
        this.masterSpectra = new Cmplx[masterTimeSeries.length][];
        for (int i = 0; i < masterTimeSeries.length; i++) {
            masterSpectra[i] = null;
        }
        
        
    }
    
     /** Method to get master TimeSeries */
    
    public TimeSeries[] getMasterTimeSeries()  {
        
        return (masterTimeSeries);
        
        
    }
    
    
    /** Method to set master channel index */
    
    public void setMasterChannel(int masterChannel) throws FilterException {
        
        if (masterChannel < 0 || masterChannel >= masterTimeSeries.length) {
            throw new FilterException(FreqText.invalid_convolution_master_channel);
        }
        
        this.masterChannel = masterChannel;
        
    }
    
    
    
    /** Method to set convolution waterLevel */
    
    public void setWaterLevel(double waterLevel) throws FilterException {
        
        //if (waterLevel < 0.0) {
        //    throw new FilterException(FreqText.invalid_water_level);
        //}
        
        this.waterLevel = waterLevel;
        
        
    }
    
    /** Method to get convolution waterLevel */
    
    public double getWaterLevel() {
        
        return(waterLevel);
        
    }
    
    
    
    
    /** Method to set taperPercent */
    
    public void setTaperPercent(double taperPercent) throws FilterException {
        
        if (taperPercent < 0.0) {
            throw new FilterException(FreqText.invalid_frequency_taper_values);
        }
        
        this.taperPercent = taperPercent;
        
        
    }
    
    /** Method to get taperPercent */
    
    public double getTaperPercent() {
        
        return(taperPercent);
        
    }
    
    
    
    
    /** Method to set convolution type */
    
    public void setType(String typeStr) throws FilterException {
        
        if (CONVOLUTION.startsWith(typeStr.toUpperCase()))
            this.type = TYPE_CONV;
        else if (DECONVOLUTION.startsWith(typeStr.toUpperCase()))
            this.type = TYPE_DECONV;
        else
            throw new FilterException(FreqText.invalid_convolution_type);
        
        
    }
    
    
    
    /** Method to set convolution type */
    
    public void setType(int type) throws FilterException {
        
        if (type >= 0 && type < NUM_TYPES)
            this.type = type;
        else
            throw new FilterException(FreqText.invalid_convolution_type);
        
        
    }
    
    
    
    /** Method to get convolution type */
    
    public String getType() {
        
        if (type == TYPE_CONV)
            return(CONVOLUTION);
        else if (type == TYPE_DECONV)
            return(DECONVOLUTION);
        
        return(null);
        
    }
    
    
    
    /** Method to check settings */
    
    public void checkSettings() throws FilterException {
        
        String errMessage = "";
        int badSettings = 0;
        
        if (masterSpectra == null || masterSpectra.length < 1) {
            errMessage += ": " + FreqText.invalid_convolution_master_spectrum;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new FilterException(errMessage + ".");
        }
        
    }
    
    
    
    /*** function to convolve in frequency domain */
    
    public final Cmplx[] apply(double dtime, Cmplx[] cz) {
        
        double masterDtime = masterTimeSeries[masterChannel].sampleInt;
        
        if (Math.abs(dtime - masterDtime) > dtime / (double) cz.length) {
            System.out.println(this.getClass().toString() + ": ERROR: target and master dt do not match: "
            + "dt target=" + dtime + ", dt master=" + masterDtime);
            return(cz);
        }
        
        Cmplx[] masterSpectrum = this.masterSpectra[masterChannel];
        if (masterSpectrum == null) {
            int trailingZeroPad = cz.length - masterTimeSeries[masterChannel].sampleLength();
            if (trailingZeroPad >= 0) {
                masterSpectrum =  masterTimeSeries[masterChannel].getFFT(taperPercent, trailingZeroPad);
            } else {
                System.out.println(this.getClass().toString() + ": ERROR: target length less than master length: "
                + "length target=" + cz.length);
                return(cz);
            }
        }
        
        if (masterSpectrum.length != cz.length) {
            System.out.println(this.getClass().toString() + ": ERROR: target and master length do not match: "
            + "length target=" + cz.length + ", length master=" + masterSpectrum.length);
            return(cz);
        }
        
        
        int ndxTarget1, ndxTarget2, ndxMaster1, ndxMaster2;
        int npTarget = cz.length;
        int npTarget_2 = npTarget / 2;
        int npMaster = masterSpectrum.length;
        int npMaster_2 = npMaster / 2;
        int npmin = npTarget < npMaster ? npTarget : npMaster;
        int npmax = npTarget > npMaster ? npTarget : npMaster;
        
        Cmplx czero = new Cmplx(0., 0.);
        
        if (type == this.TYPE_CONV) {   // convolution
            for (int i = 0; i < npmin / 2; i++) {
                
                ndxTarget1 = npTarget_2 + i;
                ndxTarget2 = npTarget_2 - 1 - i;
                ndxMaster1 = npMaster_2 + i;
                ndxMaster2 = npMaster_2 - 1 - i;
                cz[ndxTarget1].mul(masterSpectrum[ndxMaster1]);
                cz[ndxTarget2].mul(masterSpectrum[ndxMaster2]);
            }
        } else {    // deconvolution
            // check water level
            boolean checkWaterLevel = false;
            double maxAmp = -1.0;
            double maxRatio = -1.0;
            double ratio = -1.0;
            if (this.waterLevel >= 0.0) {
                checkWaterLevel = true;
                maxRatio = Math.pow(10.0, waterLevel / 20.0);
                double amp;
                for (int n = 1; n < masterSpectrum.length; n++) {   // ignore DC (?)
                    amp = masterSpectrum[n].mag();
                    maxAmp = maxAmp > amp ? maxAmp : amp;
                }
            }
            for (int i = 0; i < npmin / 2; i++) {
                ndxTarget1 = npTarget_2 + i;
                ndxTarget2 = npTarget_2 - 1 - i;
                ndxMaster1 = npMaster_2 + i;
                ndxMaster2 = npMaster_2 - 1 - i;
                if (checkWaterLevel) {
                    if ((ratio = maxAmp / masterSpectrum[ndxMaster1].mag()) >  maxRatio)
                        cz[ndxTarget1].div(Cmplx.mul(masterSpectrum[ndxMaster1], (ratio / maxRatio)));
                    else
                        cz[ndxTarget1].div(masterSpectrum[ndxMaster1]);
                    if ((ratio = maxAmp / masterSpectrum[ndxMaster2].mag()) >  maxRatio)
                        cz[ndxTarget2].div(Cmplx.mul(masterSpectrum[ndxMaster2], (ratio / maxRatio)));
                    else
                        cz[ndxTarget2].div(masterSpectrum[ndxMaster2]);
                } else {
                    cz[ndxTarget1].div(masterSpectrum[ndxMaster1]);
                    cz[ndxTarget2].div(masterSpectrum[ndxMaster2]);
                }
            }
        }
        
        /*
        if (npTarget == npmax) {
            for (int i = npmin / 2; i < npmax / 2; i++) {
                ndxTarget1 = npTarget_2 + i;
                ndxTarget2 = npTarget_2 - 1 - i;
                cz[ndxTarget1] = czero;
                cz[ndxTarget2] = czero;
            }
        }
         **/
        //cz[0] = czero;
        
        return(cz);
        
    }
    
    
    /**  Does pre-processing on the time-domain data.
     */
    
    public float[] preProcess(double dt, float[] x) {
        
        return(x);
        
    }
    
    
    /**  Does post-processing on the time-domain data.
     */
    
    public float[] postProcess(double dt, float[] x) {
        
        
        return(x);
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
             return(false);
    }
    
    
    
    
    
    /**  Update fields in TimeSeries object */
    
    public void updateFields(TimeSeries timeSeries) {
        
    }
    
    
}	// End class GaussianFilter


