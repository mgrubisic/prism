/*
 * Copyright (C) 2015 jmjones
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SmProcessing;

import static SmConstants.VFileConstants.ARIAS_INTENSITY_CONST;
import static SmConstants.VFileConstants.TO_G_CONVERSION;
import static SmProcessing.ArrayOps.Integrate;
import static SmProcessing.ArrayOps.SquareIntegrateAndSum;
import static SmProcessing.ArrayOps.convertArrayUnits;

/**
 *
 * @author jmjones
 */
public class ComputedParams {
    private double[] acc;
    private double[] gaccsq;
    private double[] gacc;
    private double t1;
    private double t2;
    private double dt;
    private int start;
    private int end;
    private int len;
    
    private double sumGaccsq;
    private double bracketedDuration;
    private double ariasIntensity;
    private double housnerIntensity;
    private double channelRMS;
    private double durationInterval;
    
    public ComputedParams(final double[] inArray, double dtime) {
        this.dt = dtime;
        this.acc = inArray;
        this.len = inArray.length;
        this.gacc = new double[len];
        this.gaccsq = new double[len];
        this.t1 = 0.0;
        this.t2 = 0.0;
        this.bracketedDuration = 0.0;
        this.ariasIntensity = 0.0;
        this.housnerIntensity = 0.0;
        this.channelRMS = 0.0;
        this.durationInterval = 0.0;
        this.start = 0;
        this.end = 0;
        
        //Get the acceleration squared and in g for calculations
        gacc = convertArrayUnits(acc, TO_G_CONVERSION);
        for (int i = 0; i < len; i++) {
            gaccsq[i] = Math.pow(gacc[i],2);
        }
        //Get sum/integration of gaccsq
        this.sumGaccsq = 0.5 * (gaccsq[0] + gaccsq[len-1]) * dt;
        for (int i = 1; i < len-1; i++) {
            sumGaccsq = sumGaccsq + gaccsq[i]*dt;
        }
        System.out.println("Ea: " + sumGaccsq);
    }
    public boolean calculateComputedParameters() {
        
        // Bracketed Duration (secs over 5% g)
        boolean strongMotion = calculateBracketedDuration();
        if (!strongMotion) {
            return false;
        }

        // Arias Intensity, units of g, damping = 0.05
        ariasIntensity = sumGaccsq * ARIAS_INTENSITY_CONST;

        // Housner Intensity, units of g*g
        calculateHousnerIntensity();
        
        // RMS of channel
        channelRMS = Math.sqrt(housnerIntensity);
        
        //Duration interval, (sec at 75% Arias I. - sec at 5% Arias I.)
        calculateDurationInterval();
        
        return true;
    }
    private boolean calculateBracketedDuration() {
        //Check if any value is greater than 0.05.  If not, no need to compute
        //parameters
        ArrayStats test = new ArrayStats(gacc);
        System.out.println("peak gacc: " + Math.abs(test.getPeakVal()));
        if (Math.abs(test.getPeakVal()) < 0.05) {
            return false;
        }
        //Find the index of the first value > 0.05
        for (int i = 0; i < len; i++) {
            if (Math.abs(gacc[i]) > 0.05) {
                start = i;
                break;
            }
        }
        t1 = start * dt;
        System.out.println("start: " + start);
        
        //Now find the index where the last value < 0.05
        for (int j = len-1; j > t1; j--) {
            if (Math.abs(gacc[j]) > 0.05) {
                end = j;
                break;
            }
        }
        t2 = end * dt;
        System.out.println("end: " + end);
        bracketedDuration = t2 - t1;
        return true;
    }
    private void calculateHousnerIntensity() {
        double hsum = 0.5 * (gaccsq[start] + gaccsq[end]) * dt;
        for (int i = start+1; i < end; i++) {
            hsum = hsum + gaccsq[i]*dt;
        }
        housnerIntensity = hsum / (t2-t1);
    }
    private void calculateDurationInterval() {
        double EPSILON = 0.01;
        double IA75 = 0.75 * sumGaccsq;
        double IA05 = 0.05 * sumGaccsq;
        boolean found05 = false;
        boolean found75 = false;
        double t05 = 0.0;
        double t75 = 0.0;
        double dsum = 0.5 * gaccsq[0] * dt + gaccsq[1] * dt;
        for (int i = 2; i < len; i++) {
            dsum = dsum + 0.5*gaccsq[i]*dt;
            if ((!found05) && (Math.abs(dsum - IA05) < EPSILON)) {
                found05 = true;
                t05 = i * dt;
            }
            if ((!found75) && (Math.abs(dsum - IA75) < EPSILON)) {
                found75 = true;
                t75 = i * dt;
            }
            if (found05 && found75) {
                break;
            } else {
                dsum = dsum + 0.5*gaccsq[i]*dt;      
            }
        }
        durationInterval = t75 - t05;
    }
    public double getBracketedDuration() {
        return this.bracketedDuration;
    }
    public double getAriasIntensity() {
        return this.ariasIntensity;
    }
    public double getHousnerIntensity() {
        return this.housnerIntensity;
    }
    public double getChannelRMS() {
        return this.channelRMS;
    }
    public double getDurationInterval() {
        return this.durationInterval;
    }
}
