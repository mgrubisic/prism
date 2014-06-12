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



public class PeakExtractor extends TimeDomainProcess {
    
    public int maxNumberPeaks = 5;
    public double minAmplitude = 0.25;
    public double stdDevWidth = 1.0;
    
    public String errorMessage;
    
    // picks
    protected Vector peakPicks = new Vector();
    
    private static final int NUMBER_PEAKS_MIN = 1;
    private static final int NUMBER_PEAKS_MAX = Integer.MAX_VALUE;
    private static final double MIN_AMP_MIN = 0.0;
    private static final double MIN_AMP_MAX = 1.0;
    private static final double NUM_DEV_MIN = Double.MIN_VALUE;
    private static final double NUM_DEV_MAX = Double.MAX_VALUE;
    
    
    /** constructor */
    
    public PeakExtractor(String localeText, int maxNumberPeaks, double minAmplitude, double stdDevWidth) {
        this.maxNumberPeaks = maxNumberPeaks;
        this.minAmplitude = minAmplitude;
        this.stdDevWidth = stdDevWidth;
        this.errorMessage = " ";
        
        TimeDomainText.setLocale(localeText);
    }
    
    
    /** Method to set maxNumberPeaksValue */
    
    public void setMaxNumberPeaks(int maxNumberPeaksValue) throws TimeDomainException {
        
        if (maxNumberPeaksValue < NUMBER_PEAKS_MIN || maxNumberPeaksValue > NUMBER_PEAKS_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_peakext_max_num_peaks_value + ": " + maxNumberPeaksValue);
        }
        
        maxNumberPeaks = maxNumberPeaksValue;
    }
    
    
    /** Method to set maxNumberPeaksValue */
    
    public void setMaxNumberPeaks(String str) throws TimeDomainException {
        
        int maxNumberPeaksValue;
        
        try {
            maxNumberPeaksValue = Integer.valueOf(str).intValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_peakext_max_num_peaks_value + ": " + str);
        }
        
        setMaxNumberPeaks(maxNumberPeaksValue);
    }
    
    
    /** Method to set minAmplitudeValue */
    
    public void setMinAmplitude(double minAmplitudeValue) throws TimeDomainException {
        
        if (minAmplitudeValue < MIN_AMP_MIN || minAmplitudeValue > MIN_AMP_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_min_amplitude_value + ": " + minAmplitudeValue);
        }
        
        minAmplitude = minAmplitudeValue;
    }
    
    
    /** Method to set minAmplitudeValue */
    
    public void setMinAmplitude(String str) throws TimeDomainException {
        
        double minAmplitudeValue;
        
        try {
            minAmplitudeValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_min_amplitude_value + ": " + str);
        }
        
        setMinAmplitude(minAmplitudeValue);
    }
    
    
    
    
    /** Method to set stdDevWidthValue */
    
    public void setDevWidth(double stdDevWidthValue) throws TimeDomainException {
        
        if (stdDevWidthValue < NUM_DEV_MIN || stdDevWidthValue > NUM_DEV_MAX) {
            throw new TimeDomainException(TimeDomainText.invalid_deviation_value + ": " + stdDevWidthValue);
        }
        
        stdDevWidth = stdDevWidthValue;
    }
    
    
    /** Method to set stdDevWidthValue */
    
    public void setDevWidth(String str) throws TimeDomainException {
        
        double stdDevWidthValue;
        
        try {
            stdDevWidthValue = Double.valueOf(str).doubleValue();
        } catch (NumberFormatException e) {
            throw new TimeDomainException(TimeDomainText.invalid_deviation_value + ": " + str);
        }
        
        setDevWidth(stdDevWidthValue);
    }
    
    
    
    
    /** Method to get pick trigger indeces
     *
     * @returns triggerPickData Vector of int[2] where int[0] is trigger index
     *  and int[1] is last index where STA/LTA ratio equaled 1.0
     */
    
    public Vector getPickData() {
        
        return(peakPicks);
        
    }
    
    
    
    
    
    /** Method to check settings */
    
    public void checkSettings() throws TimeDomainException {
        
        String errMessage = "";
        int badSettings = 0;
        
        if (maxNumberPeaks < NUMBER_PEAKS_MIN || maxNumberPeaks > NUMBER_PEAKS_MAX) {
            errMessage += ": " + TimeDomainText.invalid_peakext_max_num_peaks_value;
            badSettings++;
        }
        
        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }
        
    }
    
    
    
    /*** function to find peaks  */
    
    
    public static final int MODE_CENTER_BETWEEN_LR_STD = 0;
    public static final int MODE_CENTER_ON_PEAK_STD_L = 1;
    public int mode = MODE_CENTER_ON_PEAK_STD_L;
    
    public final float[] apply(double dt, float[] sample) {
        
        float ampAtStdDevWidth = (float) Math.exp(-0.5 * stdDevWidth);
        float ampAtStdDevWidth_10 = (float) Math.exp(-0.5 * stdDevWidth / 10.0);
        
        
        peakPicks = new Vector();
        
        int[] startNdx = new int[this.maxNumberPeaks];
        int[] endNdx = new int[this.maxNumberPeaks];
        
        float valMaxFirst = 0.0f;
        
        for (int currentPeak = 0; currentPeak < maxNumberPeaks; currentPeak++) {
            
            
            // find a peak outside of zones of previously found peaks
            float valMax = -Float.MAX_VALUE;
            int nMax = -1;
            boolean increased = false;
            boolean foundPeak = false;
            for (int n = 0; n < sample.length; n++) {
                if (indexInvalid(n, startNdx, endNdx, currentPeak)) {
                    valMax = -Float.MAX_VALUE;
                    nMax = -1;
                    increased = false;
                    continue;
                }
                float val = sample[n];
                if (val - valMax > Float.MIN_VALUE) { // increasing amplitude
                    if (nMax > 0)
                        increased = true;
                    valMax = val;
                    nMax = n;
                } else if (increased && valMax - val > Float.MIN_VALUE) {  // passed peak
                    foundPeak = true;
                    break;
                }
            }
            if (!foundPeak || nMax <= 0 || nMax == sample.length - 1)
                break;  // no peak found, end search
            if (currentPeak > 0) {
                if (valMax < valMaxFirst * minAmplitude)
                    break;  // peak too small, end search
            } else {
                valMaxFirst = valMax;
            }
            
            // find +/- one std deviation from peak (which define uncertainty of peak)
            //     and left and right minima points (which define width/zone of peak)
            float ampAtStdWidth = valMax * ampAtStdDevWidth;
            float ampAtStdWidth_10 = valMax * ampAtStdDevWidth_10;
            // left
            float amp = 0.0f;
            float lastamp = Float.MAX_VALUE;
            int nLeft = -1;
            int nLeftSTD = -1;
            for (nLeft = nMax; nLeft > 0; nLeft--) {
                if (indexInvalid(nLeft, startNdx, endNdx, currentPeak)) // into previous peak zone
                    break;
                amp = sample[nLeft];
                //if (nLeftSTD < 0) {  // STD not yet found
                if (amp < ampAtStdWidth_10) {   // make sure not caught near very flat peak
                    if (ampAtStdWidth - amp > Float.MIN_VALUE) {
                        nLeftSTD = nLeft;
                        break;
                    } else if (amp - lastamp > Float.MIN_VALUE) {    // passed minima
                        nLeftSTD = nLeft;
                        break;
                    }
                }
                lastamp = amp;
            }
            // right
            lastamp = Float.MAX_VALUE;
            int nRight = -1;
            int nRightSTD = -1;
            for (nRight = nMax; nRight < sample.length; nRight++) {
                if (indexInvalid(nRight, startNdx, endNdx, currentPeak)) // into previous peak zone
                    break;
                amp = sample[nRight];
                //if (nRightSTD < 0) {  // STD not yet found
                if (amp < ampAtStdWidth_10) {   // make sure not caught near very flat peak
                    if (ampAtStdWidth - amp > Float.MIN_VALUE) {
                        nRightSTD = nRight;
                        break;
                    } else if (amp - lastamp > Float.MIN_VALUE) {    // passed minima
                        nRightSTD = nRight;
                        break;
                    }
                }
                lastamp = amp;
            }
            
            if (nLeftSTD < 0 && nRightSTD < 0)
                break;  // incomplete peak, end search
            
            // if only one STD found, estimate second
            if (nLeftSTD < 0)
                nLeftSTD = nMax - (nRightSTD - nMax);
            else if (nRightSTD < 0)
                nRightSTD = nMax + (nMax - nLeftSTD);
            
            // create pick indices
            double[] pickIndices = new double[2];
            if (mode == MODE_CENTER_BETWEEN_LR_STD) {
                pickIndices[0] = nLeftSTD;
                pickIndices[1] = nRightSTD;
            } else if (mode == MODE_CENTER_ON_PEAK_STD_L) {
                int std = nMax - nLeftSTD;
                pickIndices[0] = nMax - std;
                pickIndices[1] = nMax + std;
            }
            PickData pickData = new PickData(pickIndices[0], pickIndices[1],
            PickData.POLARITY_UNKNOWN, valMax, PickData.DATA_AMP_UNITS);
            peakPicks.add(pickData);
            
            startNdx[currentPeak] = nLeft;
            endNdx[currentPeak] = nRight;
            
        }
        
        
        return(sample);
        
    }
    
    
    protected final boolean indexInvalid(int ndx, int[] startNdx, int[] endNdx, int currentPeak) {
        
        for (int npeak = 0; npeak < currentPeak; npeak++) {
            if (ndx >= startNdx[npeak] && ndx <= endNdx[npeak])
                return(true);
        }
        
        return(false);
        
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
    
    
}	// End class GaussianFilter


