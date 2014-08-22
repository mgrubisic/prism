/*
 * Copyright (C) 2014 jmjones
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

import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author jmjones
 */
public class EventOnsetDetection {
    private static final int NUM_BINS = 200;
    private static final double XI = 0.6;  //damping ratio
    private static final double TN = 0.01; //vibration period
    private final double omegan;
    private final double const_C;
    private final double deltaT;
    private final double coef_a; 
    private final double coef_b; 
    private final double coef_c; 
    private final double coef_d; 
    private final double coef_e; 
    private final double coef_f;
    
    private int eventStart;
    private double bufferVal;
    private int bufferedStart;
    
    //constructor
    public EventOnsetDetection(double deltaT) {
        this.deltaT = deltaT;
        omegan= 2.0 * Math.PI / TN;
        const_C = 2.0 * XI * omegan;
        
        eventStart = 0;
        bufferVal = 0.0;
        bufferedStart = 0;

        //Get the matrix coefficients for the specific sampling interval
        EventOnsetCoefs pickCoef = new EventOnsetCoefs();
        double[] Ae;
        double[] AeB;
        Ae = pickCoef.getAeCoefs(deltaT);
        AeB = pickCoef.getAeBCoefs(deltaT);
        coef_a = Ae[0];
        coef_b = Ae[1];
        coef_c = Ae[2];
        coef_d = Ae[3];
        coef_e = AeB[0];
        coef_f = AeB[1];
    }
    public double[] showCoefficients() {  //for debug
        double[] coefs = new double[6];
        coefs[0] = coef_a;
        coefs[1] = coef_b;
        coefs[2] = coef_c;
        coefs[3] = coef_d;
        coefs[4] = coef_e;
        coefs[5] = coef_f;
        return coefs;
    }
    //buffer has units of seconds and is used to increase the length of time
    //between the detected P-wave and the reported start of waveform.
    public int findEventOnset( final double[] acc, double buffer) {
        int len = acc.length;
        int found = 0;
        bufferVal = buffer;
        
        System.out.println("deltaT: " + deltaT);
        System.out.println("const_C: " + const_C);
        System.out.println("coef_a: " + coef_a + " coef_b: " + coef_b);
        System.out.println("coef_c: " + coef_c + " coef_d: " + coef_d);
        System.out.println("coef_e: " + coef_e + " coef_f: " + coef_f);
        
        //Calculate the transient response of an oscillator with vibration period
        //TN and damping ratio XI subjected to support acceleration (array acc)
        //and sampled at a step deltaT.
        double[][] y = new double[2][len];
        y[0][0] = 0.0;
        y[1][0] = 0.0;
        
        for(int k = 1; k < len; k++) {
            y[0][k] = coef_a * y[0][k-1] + coef_b * y[1][k-1] + coef_e * acc[k];
            y[1][k] = coef_c * y[0][k-1] + coef_d * y[1][k-1] + coef_f * acc[k];
        }
        
        //Get the relative velocity (m/sec)
        double[] veloc = y[1];
        double[] Edi = new double[len];
        
        //Integrand of viscous damping energy (m^2/sec^3)
        for (int i = 0; i < len; i++) {
            Edi[i] = const_C * Math.pow(veloc[i], 2);
        }
        //Viscous damping energy over mass (m^2/sec^2)
        double[] Edoverm = ArrayOps.Integrate(Edi, deltaT);
        
        //Spectral viscous damping energy over mass (m^2/sec^2)
        //find largest absolute value in array
        double Edoverm_max = Double.MIN_VALUE;
        for (double each : Edoverm) {
            if (Math.abs(each) > Edoverm_max) {
                Edoverm_max = Math.abs(each);
            }
        }
        System.out.println("Edoverm_max: " + Edoverm_max);
        
        //normalize the array by dividing all vals by the max
        double[] EIM = new double[Edoverm.length];
        for (int i = 0; i < Edoverm.length; i++) {
            EIM[i] = Edoverm[i] / Edoverm_max;
        }
        
        //Integrand of normalized damping energy (m^2/sec^3)
        double[] PIM = ArrayOps.Differentiate(EIM, deltaT);
        
        //!!!Debug 
        TextFileWriter textout = new TextFileWriter( "D:/PRISM/filter_test/junit", 
                                                            "ppick_pim.txt", PIM);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out PIM in EventOnsetDetection");
        }
        //!!!Debug
        
        // find the most common value in the lower half of the range of PIM.
        // The value returned is the most frequently-occurring
        // value in the lower half of the array min-max range.
        ArrayStats statPIM = new ArrayStats(PIM);
        double lowerMode = statPIM.getModalMinimum(NUM_BINS);
        System.out.println("+++ modalMin in ppicker: " + lowerMode);
        //Now find the index of the first occurrence in the array of a value
        //that is greater than the most frequently-occurring value.
        int peak = 0;
        for (int i = 0; i < len; i++) {
            if (PIM[i] > lowerMode) {
                peak = i;
                break;
            }
        }
        System.out.println("+++ PIM index of peak: " + peak);
        //In the array subset acc[0:peak], start at the end and work back to front
        //to find the index of the first zero-crossing.  This is the start of
        //the P-wave.  The zero-crossing is identified by 2 consecutive values
        //in the array with differing signs.
        for (int k = peak; k > 0; k--) {
            if ((acc[k] * acc[k-1]) < 0.0) {
                found = k-1;
                break;
            }
        }
        System.out.println("+++ start of zero crossing: " + found);
        //Return the index into the acceleration array that marks the start of
        //the P-wave, adjusted by the buffer amount
        eventStart = found;
        bufferedStart = found - (int)Math.round(buffer/deltaT);
        return bufferedStart;
    }
    public int getEventStart() {
        return this.eventStart;
    }
    public double getBufferLength() {
        return this.bufferVal;
    }
    public int getBufferedStart() {
        return this.bufferedStart;
    }
}
