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

import SmUtilities.SmErrorLogger;
import java.util.Arrays;

/**
*  function bandpass (s, nd, f1, f2, delt, nroll, icaus)
*
*  function bandpass implements a butterworth bandpass filter
*
*  s[] = input time series array of doubles
*  nd = the number of points in the time series, which is less than the length of s (see below)
*  f1 = the lower cutoff frequency
*  f2 = the higher cutoff frequency
*  delt = the timestep
*  nroll = butterworth bandpass filter order is 2*nroll (max val nroll=8)
*  icaus = causal, acausal filter flag, icaus = 1 for causal filter
*  
*  The dimension of the input array s must be at least as large
*  as the larger of the following (if acausal filtering):
*  (nd + (3 * nroll)/(f1 * delt))  or  (nd + (6 * nroll)/((f2 - f1) * delt))
*  
*  All floating point operations are of type double
*  In any operation with integer and double, integer is converted to double
*
*  From the Fortran BAND.FOR, TSPP--A Collection of FORTRAN Programs for Processing
*                               and Manipulating Time Series
*  Butterworth bandpass filter order 2*nroll (nroll.le.8) (see Kanasewich, 
*    Time Sequence Analysis in Geophysics, Third Edition, 
*    University of Alberta Press, 1981)
*  written by W.B. Joyner 01/07/97
* Dates: xx/xx/xx - Written by Bill Joyner
*        09/12/00 - Changed "n" to "nroll" to eliminate confusion with
*                   Kanesewich, who uses "n" as the order (=2*nroll), and
*                   cleaned up appearance of code by using spaces, indents, etc.
*        09/12/00 - double precision statements added so that the output
*                   series has sufficient precision for double integration.
*        11/08/00 - Increased dimension of s from 50000 to 100000
*        02/14/01 - Modified/corrected real numbers function calls to 
*                   double precision - cds
*        02/22/01 - Removed dimension of s (it is up to the user to specify
*                   it properly)
* @author jmjones, translated from Fortran into Java, June 2014
**/
public class ButterworthFilter {
    private double f1;
    private double f2;
    private double dtime;
    private int nroll;
    private boolean icaus;
    private final double pi = Math.PI;
    private final int MAXPOLES = 8;
    private final double epsilon = 0.001;
    private double[] fact;
    private double[] b1;
    private double[] b2;
    private int npad;
    
    //Constructor
    public ButterworthFilter() {
    }
    
    public boolean calculateCoefficients(double lowCutOff, double highCutOff, double dtime,
                                int numberPoles, boolean acausal) {
        
        this.f1 = lowCutOff;
        this.f2 = highCutOff;
        this.dtime = dtime;
        this.nroll = numberPoles;
        this.icaus = acausal;           //true if acausal filter
        
        fact = new double[2*MAXPOLES];
        b1 = new double[2*MAXPOLES];
        b2 = new double[2*MAXPOLES];
        this.npad = 0;
        
        double pre; double pim; double argre; double argim; double rho; double theta;
        double sjre; double sjim; double bj; double cj; double con;
        int index;
        
        //Check input parameters for valid values
        if ((Math.abs(f1 - 0.0) < epsilon) || (Math.abs(f2 - f1) < epsilon)
                                                || (numberPoles > MAXPOLES)){
            return false;
        }
        double nyquist = (1.0 / dtime) / 2.0;
        if ((Math.abs(f1 - nyquist) < epsilon) || (Math.abs(f2 - nyquist) < epsilon)) {
            return false;
        }
        
        //for w1 and w2 calc., the 2 in the num. and denom. can be deleted !!!
        double w1 = 2.0 * Math.tan(((2.0*pi*f1)*dtime)/2.0) / dtime;
        double w2 = 2.0 * Math.tan(((2.0*pi*f2)*dtime)/2.0) / dtime;
        
        for (int k = 1; k < nroll+1; k++) {
            pre = (-1.0) * Math.sin((pi*(2.0*k - 1)) / (4.0*nroll));
            pim = Math.cos((pi*(2.0*k - 1)) / (4.0*nroll));
            
            argre = (((Math.pow(pre,2.0) - Math.pow(pim,2.0)) * Math.pow((w2-w1),2.0)) / 4.0) - (w1 * w2);
            argim = (2.0 * pre * pim * Math.pow((w2-w1),2.0)) / 4.0;
            
            rho = Math.pow((Math.pow(argre,2.0) + Math.pow(argim, 2.0)), (1.0/4.0));
            theta = pi + (Math.atan2(argim, argre)) / 2.0;
            
            for (int i = 1; i < 3; i++) {
                sjre = (pre * (w2-w1)/2.0) + (Math.pow(-1,i) * rho * ((-1.0)*Math.sin(theta-(pi/2.0))));
                sjim = (pim * (w2-w1)/2.0) + (Math.pow(-1,i) * rho * (Math.cos(theta-(pi/2.0))));
                
                bj = (-2.0) * sjre;
                cj = Math.pow(sjre,2) + Math.pow(sjim,2);
                con = 1.0 / ((2.0/dtime) + bj + (cj*dtime/2.0));
                
                index = 2*k + i - 3;
                fact[index] = (w2 - w1) * con;
                b1[index] = ((cj*dtime) - (4.0/dtime)) * con;
                b2[index] = ((2.0/dtime) - bj + (cj*dtime/2.0)) * con;
            }
        }
        return true;
    }
    
    public double[] applyFilter( double[] arrayS, double taplengthtime, int eventOnsetIndex ) {
        
        int np2;
        double[] filteredS;
        double x1; double x2; double y1; double y2; double xp; double yp;

//        int taperlength = (int)(2.0 / dtime); // 2 seconds worth of samples
//        int taperlength= (int)(((2.0/f1) + 1.0)/dtime);
        int taperlength = ArrayOps.findZeroCrossing(arrayS, eventOnsetIndex, 0);
        if ((taperlength <= 0) || ((taperlength*dtime) <= taplengthtime)) {
            taperlength = (int)(taplengthtime / dtime);
        }
//        System.out.println("+++ taperlength: " + taperlength);
        
        //Copy the input array into a return array.  If the filter was configured
        //as acausal, then pad the length of the array by the value calculated below.
        //Before padding, apply a cosine taper to the front and back of the 
        //array.
        if (icaus) {
            if (taperlength > 0) {
                applyCosineTaper( arrayS, taperlength);
            }
            npad = (int)Math.floor(3.0 * (nroll / (f1 * dtime)));
            int check = (int)Math.floor(6.0 * (nroll / ((f2 - f1) * dtime)));
            if (npad < check) {
                npad = check;
            }
//            System.out.println("+++ npad: " + npad + " array: " + arrayS.length);
            np2 = arrayS.length + 2 * npad;
            filteredS = new double[np2];
            Arrays.fill(filteredS, 0.0);
            System.arraycopy(arrayS, 0, filteredS, npad, arrayS.length);
            
        } else {  //causal filter, filtered array is same length as input array
            np2 = arrayS.length;
            filteredS = new double[np2];
            System.arraycopy(arrayS, 0, filteredS, 0, np2);
        }
//        SmErrorLogger elog = SmErrorLogger.INSTANCE;
//        elog.writeOutArray(filteredS, "after_taper_with_pads.txt");
        //filter the array
        for (int k = 0; k < 2*nroll; k++) {
            x1 = 0.0;
            x2 = 0.0;
            y1 = 0.0;
            y2 = 0.0;
            for (int j = 0; j < np2; j++) {
                xp = filteredS[j];
                yp = fact[k] * (xp - x2) - (b1[k] * y1) - (b2[k] * y2);
                filteredS[j] = yp;
                y2 = y1;
                y1 = yp;
                x2 = x1;
                x1 = xp;
            }
        }
        
        //if acausal, filter again from back to front
        if (icaus) {
            for (int k = 0; k < 2*nroll; k++) {
                x1 = 0.0;
                x2 = 0.0;
                y1 = 0.0;
                y2 = 0.0;
                for (int j = 0; j < np2; j++) {
                    xp = filteredS[np2 - j - 1];
                    yp = fact[k] * (xp - x2) - (b1[k] * y1) - (b2[k] * y2);
                    filteredS[np2 - j - 1] = yp;
                    y2 = y1;
                    y1 = yp;
                    x2 = x1;
                    x1 = xp;
                }
            }
            System.arraycopy(filteredS, npad, arrayS, 0, arrayS.length);
        } else {
            System.arraycopy(filteredS, 0, arrayS, 0, np2);
        }
//        System.out.println("+++ after filter, arrayS[0] = " + arrayS[0]);
//        System.out.println("+++ after filter, arrayS[end] = " + arrayS[arrayS.length-1]);
        return filteredS;
    }
    
    public void applyCosineTaper( double[] array, int range ) {
        //The range is the number of elements at the front and back of the
        //array that the taper should be applied to.
        
        int len = array.length;
        //range is N, the number of samples over which to apply the taper,
        //and m is the length of the half cosine taper itself.
        int m = range / 2;
        
        double[] taper = new double[m];
        for (int i = 0; i < m; i++) {
            taper[i] = 0.5 * (1.0 - Math.cos(2 * pi * i / (range-1)));
        }
        
        //apply at the front
        for (int i = 0; i < m; i++) {
            array[i] = array[i] * taper[i];
        }
        //apply at the end
        int k = m-1;
        for (int i = len-m; i < len; i++) {
            array[i] = array[i] * taper[k];
            k--;
        }
    }
    
    public double[] getFact() {
        return fact;
    }
    public double[] getB1() {
        return b1;
    }
    public double[] getB2() {
        return b2;
    }
    public int getPadLength() {
        return npad;
    }
}
