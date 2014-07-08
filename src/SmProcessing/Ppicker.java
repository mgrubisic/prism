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

/**
 *
 * @author jmjones
 */
public class Ppicker {
    private static final double XI = 0.5;  //damping ratio
    private static final double TN = 0.01; //vibration period
    private final double omegan;
    private final double const_C;
    private double deltaT;
    private double coef_a; private double coef_b; 
    private double coef_c; private double coef_d; 
    private double coef_e; private double coef_f;
    
    //set up for integration and differentiation, and for histogram
    ArrayStats stat;
    
    //constructor
    public Ppicker(double deltaT) {
        this.deltaT = deltaT;
        omegan= 2.0 * Math.PI / TN;
        const_C = 2.0 * XI * omegan;

        //Get the matrix coefficients for the specific sampling interval
        PpickerCoefs pickCoef = new PpickerCoefs();
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
        
        System.out.format("+++ a: %f  b: %f  c: %f  d:%f  e:%f  f:%f%n", coef_a,
                coef_b,coef_c,coef_d,coef_e,coef_f);        
    }
    //buffer has units of seconds and is used to increase the length of time
    //between the detected P-wave and the reported start of waveform.
    public int pickPwave( final double[] acc, double buffer) {
        int len = acc.length;
        stat = new ArrayStats(acc);
        int found = 0;
        
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
        
        //normalize the array by dividing all vals by the max
        double[] EIM = new double[Edoverm.length];
        for (int i = 0; i < Edoverm.length; i++) {
            EIM[i] = Edoverm[i] / Edoverm_max;
        }
        
        //Integrand of normalized damping energy (m^2/sec^3)
        double[] PIM = ArrayOps.Differentiate(EIM, deltaT);
        
        // find the most common value in the lower half of the range of PIM.
        // The value returned is the most frequently-occurring
        // value in the lower half of the array min-max range.
        stat = new ArrayStats(PIM);
        double lowerMode = stat.getModalMinimum(PIM);
        System.out.println("+++ modalMin in ppicker: " + lowerMode);
        //Now find the index of the first occurrence in the array of a value
        //that is greater than the most frequently-occurring value.
        int peak = 0;
        for (int i = 0; i < len; i++) {
            if (acc[i] > lowerMode) {
                peak = i;
                break;
            }
        }
        System.out.println("+++ acc index of peak: " + peak);
        //In the array subset acc[0:peak], start at the end and work back to front
        //to find the index of the first zero-crossing.  This is the start of
        //the P-Wave.  The zero-crossing is identified by 2 consecutive values
        //in the array with differing signs.
        double temp = 0.0;
        for (int k = peak; k > 0; k--) {
            if ((acc[k] * acc[k-1]) < 0.0) {
                found = k-1;
                break;
            }
        }
        //Return the index into the acceleration array that marks the start of
        //the P-wave, adjusted by the buffer
        found = found - (int)(buffer/deltaT);
        if (found < 0 ) {
            return 0;
        } else {
            return found;
        }
    }
}
