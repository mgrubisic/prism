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

import static SmConstants.VFileConstants.*;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;
import java.util.ArrayList;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jmjones
 */
public class AdaptiveBaselineCorrection {
    private final int NUM_SEGMENTS = 3;
    private final int RESULT_PARMS = 14;
//    private final int NUM_BREAKS = 10;
    
    private final double TOL_RES_DISP = 0.001;
    private final double TOL_INIT_VEL = 0.001;
    private final double TOL_RES_VEL = 0.001;
    private final double TOL_RMS_VEL = 0.001;
    
    private final int MOVING_WINDOW = 500;
    private final double EPSILON = 0.001;
    private double dtime;
    private double lowcut;
    private double highcut;
    private int numpoles;
    private double taperlength;
    private int estart;
    private double[] velocity;
    private double[] velfinal;
    private double[] displace;
    private double[] accel;
    private final int numbreakslo;
    private final int numbreakshi;
    private final int degreeSlo;
    private final int degreeShi;
    private final int degreeP1lo;
    private final int degreeP1hi;
    private final int degreeP2lo;
    private final int degreeP2hi;
    private double[] bnn;
    private double[] result;
    private ArrayList<double[]> params;
    private int[] breaks;
    private double[] rms;
    private int[] polyBreaks;

    
    public AdaptiveBaselineCorrection(double delttime, double[] invel, 
                                      double lowcut,double highcut, int numpoles,
                                      double taperlength, int ppick) {
        this.dtime = delttime;
        this.estart = ppick;
        this.velocity = invel;
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numpoles = numpoles;
        this.taperlength = taperlength;
        this.rms = new double[NUM_SEGMENTS];
        this.polyBreaks = new int[NUM_SEGMENTS-1];
        
        //Get the values out of the configuration file and screen for correctness.
        //Number of spline breaks
        this.numbreakslo = validateConfigParam(NUM_SPLINE_BREAKS_LOWER, 
                                                DEFAULT_NUM_BREAKS_LOWER,
                                                DEFAULT_NUM_BREAKS_LOWER,
                                                DEFAULT_NUM_BREAKS_UPPER);
        this.numbreakshi = validateConfigParam(NUM_SPLINE_BREAKS_UPPER, 
                                                DEFAULT_NUM_BREAKS_UPPER,
                                                numbreakslo,
                                                DEFAULT_NUM_BREAKS_UPPER);
        //spline order
        this.degreeSlo = validateConfigParam(SPLINE_ORDER_LOWER, 
                                                DEFAULT_SPLINE_ORDER_LOWER,
                                                DEFAULT_SPLINE_ORDER_LOWER,
                                                DEFAULT_SPLINE_ORDER_UPPER);
        this.degreeShi = validateConfigParam(SPLINE_ORDER_UPPER, 
                                                DEFAULT_SPLINE_ORDER_UPPER,
                                                degreeSlo,
                                                DEFAULT_SPLINE_ORDER_UPPER);
        //First polynomial order
        this.degreeP1lo = validateConfigParam(FIRST_POLY_ORDER_LOWER, 
                                                DEFAULT_1ST_POLY_ORD_LOWER,
                                                DEFAULT_1ST_POLY_ORD_LOWER,
                                                DEFAULT_1ST_POLY_ORD_UPPER);
        this.degreeP1hi = validateConfigParam(FIRST_POLY_ORDER_UPPER, 
                                                DEFAULT_1ST_POLY_ORD_UPPER,
                                                degreeP1lo,
                                                DEFAULT_1ST_POLY_ORD_UPPER);
        //second polynomial order
        this.degreeP2lo = validateConfigParam(SECOND_POLY_ORDER_LOWER, 
                                                DEFAULT_2ND_POLY_ORD_LOWER,
                                                DEFAULT_2ND_POLY_ORD_LOWER,
                                                DEFAULT_2ND_POLY_ORD_UPPER);
        this.degreeP2hi = validateConfigParam(SECOND_POLY_ORDER_UPPER, 
                                                DEFAULT_2ND_POLY_ORD_UPPER,
                                                degreeP2lo,
                                                DEFAULT_2ND_POLY_ORD_UPPER);
    }
    
    public int validateConfigParam( String configparm, int defval, int lower,
                                                                    int upper) {
        int outval = 0;
        ConfigReader config = ConfigReader.INSTANCE;
        String inval = config.getConfigValue(configparm);
        if (inval == null) {
            outval = defval;
        } else {
            try {
                outval = Integer.parseInt(inval);
                outval = ((outval < lower) || (outval > upper)) ? defval : outval;
            } catch (NumberFormatException e) {
                outval = defval;
            }
        }
        return outval;
    }
    
    public boolean startIterations() throws SmException {
        //The matlab code works with the time index into the array, while this
        //implementation just uses the index into the array, and only uses the
        //time step to create the time arrays as needed.
        int t11 = estart;
        int vlen = velocity.length;
        int t22 = (int)(0.8 * vlen);
        int dt1 = MOVING_WINDOW;
        int t21 = t11 + dt1;
        int t1;
        velfinal = new double[vlen];
        boolean smooth = false;
        ButterworthFilter filter;
        ArrayList<double[]> results = new ArrayList<>();
        double[] onerun;
        boolean pass = false;
        
        int counter = 1;
        for (int order1 = degreeP1lo; order1 <= degreeP1hi; order1++) {
            System.out.println("ABC: order1 = " + order1);
            for (int order2 = degreeP2lo; order2 <= degreeP2hi; order2++) {
                System.out.println("ABC: order2 = " + order2);
                for (int spl_break = numbreakslo; spl_break <= numbreakshi; spl_break++) {
                    System.out.println("ABC: spl_break = " + spl_break);
                    for (int spl_order = degreeSlo; spl_order <= degreeShi; spl_order++) {
                        System.out.println("ABC: spl_order = " + spl_order);
                        for (int t2 = t21; t2 <= t22; t2 += dt1) {
                            t1 = t11;
                            if ((t2-t1) >= ((int)1.0/lowcut)) {
                                System.out.println("ABC: count = " + counter);
                                System.arraycopy(velocity,0,velfinal,0,vlen);
                                //remove the spline fit from velocity
                                makeCorrection(velfinal,t1,t2,spl_break,spl_order,
                                                            order1,order2,smooth);
                                //now filter velocity
                                filter = new ButterworthFilter();
                                boolean valid = filter.calculateCoefficients(lowcut, 
                                                 highcut,dtime, numpoles, true);
                                if (valid) {
                                    filter.applyFilter(velfinal, taperlength);
                                } else {
                                    throw new SmException("ABC: Invalid bandpass "
                                                     + "filter input parameters");
                                }
                                //remove any mean value
                                ArrayStats velmean = new ArrayStats( velfinal );
                                ArrayOps.removeValue(velfinal, velmean.getMean());
                                //integrate to get displacement, differentiate
                                //for acceleration
                                displace = ArrayOps.Integrate( velocity, dtime);
                                accel = ArrayOps.Differentiate(velocity, dtime);
                                //store the results in an array for comparison
                                onerun = new double[RESULT_PARMS];
                                onerun[0] = Math.sqrt(Math.pow(rms[0], 2) +
                                        Math.pow(rms[1],2) + Math.pow(rms[2],2));
                                onerun[1] = Math.abs(displace[displace.length-1]);
                                onerun[2] = Math.abs(velfinal[0]);
                                onerun[3] = Math.abs(velfinal[velfinal.length-1]);
                                onerun[4] = t1;
                                onerun[5] = t2;
                                onerun[6] = order1;
                                onerun[7] = order2;
                                onerun[8] = counter;
                                onerun[9] = rms[0];
                                onerun[10] = rms[1];
                                onerun[11] = rms[2];
                                onerun[12] = spl_break;
                                onerun[13]= spl_order;
                                //Penalty for initial acceleration step
                                ArrayStats accstat = new ArrayStats(accel);
                                if ((Math.abs(accel[0]) - Math.abs(accstat.getPeakVal())) < EPSILON) {
                                    onerun[0] = 10000;
                                }
                                results.add(onerun);
                                counter++;
                            }
                        }
                    }
                }
            }
        }
        return pass;
    }
    public void makeCorrection( double[] array, int break1, int break2, int spl_order,
                        int degreeS, int degreeP1, int degreeP2, boolean smooth ) {
        polyBreaks[0] = break1;
        polyBreaks[1] = break2;
        double[] h1;
        double[] h2;
        double[] h3;
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
        breaks = makeBreaks( break1, break2, spl_order);
        
        h1 = new double[break1];
        h2 = new double[break2-break1];
        h3 = new double[array.length-break2];
        System.arraycopy(array, 0, h1, 0, break1);
        System.arraycopy(array, break1, h2, 0, break2-break1);
        System.arraycopy(array, break2, h3, 0, array.length-break2);
        result = new double[ array.length ];
        
        //Get the polynomials that were fitted to the input array
        //Construct the baseline function from each section
        PolynomialSplineFunction spfunction = getSplines(h2, breaks, degreeS);
        double[] coefs1 = ArrayOps.findPolynomialTrend(h1, degreeP1, dtime);
        double[] coefs3 = ArrayOps.findPolynomialTrend(h3, degreeP2, dtime);
        
        PolynomialFunction b1poly = new PolynomialFunction( coefs1 );
        PolynomialFunction b3poly = new PolynomialFunction( coefs3 );

        bnn = new double[time.length];
        for (int i = 0; i < bnn.length; i++) {
            if ( i < break1) {
                bnn[i] = b1poly.value(time[i]);
            } else if ( i >= break2) {
                bnn[i] = b3poly.value(time[i] - (break2*dtime));
            } else {
                bnn[i] = spfunction.value(time[i] - (break1*dtime));
            }
        }
        //If smoothing selected, then smooth out discontinuities in the baseline
        //function before removing it from the input array.
        if (smooth) {
            for (int k = 0; k < 1; k++) {
                bnn = smoothValues(bnn);
            }
        }
        //Remove the baseline function from the input array
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
            if ( i < break1) {                
                h1[i] = result[i];
            } else if ( i >= break2) {
                h3[i-break2] = result[i];
            } else {
                h2[i-break1] = result[i];
            }
        }
        rms[0] = ArrayOps.rootMeanSquare(h1);
        rms[1] = ArrayOps.rootMeanSquare(h2);
        rms[2] = ArrayOps.rootMeanSquare(h3);
    }
    public PolynomialSplineFunction getSplines( double[] vals, int[] inbreaks, 
                                                                    int degree ) {
        int len;
        double[] loctime;
        double[] subset;
        int numbreaks= inbreaks.length;
        
        PolynomialFunction poly;
        PolynomialFunction[] polyArrays;
        PolynomialSplineFunction spfunction;
        polyArrays = new PolynomialFunction[numbreaks-1];
        double[] knots = new double[numbreaks];
        
        //For each section of the array to fit (between each break), fit a
        //polynomial of the specified degree.  Save each polynomial in an 
        //array for the polynomial spline function.
        for (int i = 0; i < numbreaks-1; i++) {
            len = inbreaks[i+1] - inbreaks[i];
            subset = new double[len];
            System.arraycopy(vals,inbreaks[i],subset,0,len);
            loctime = ArrayOps.makeTimeArray( dtime, len);
            ArrayList<WeightedObservedPoint> points = new ArrayList<>();
            for (int j = 0; j < len; j++ ){
                points.add(new WeightedObservedPoint( 1.0, loctime[j], subset[j]));
            }
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
            double[] coefs = fitter.fit(points);
            poly = new PolynomialFunction (coefs);
            polyArrays[i] = poly;
        }
        //Calculate the knots for the spline function and create a new spline
        //function object.
        for (int k = 0; k < numbreaks; k++) {
            knots[k] = inbreaks[k] * dtime;
        }
        spfunction = new PolynomialSplineFunction( knots, polyArrays );
        return spfunction;    
    }
    public int[] makeBreaks(int start, int end, int numbreaks) {
        int[] breakers = new int[numbreaks+1];
        int interval = Math.round((end-start) / (numbreaks));
        for (int i = 0; i < numbreaks; i++) {
            breakers[i] = i * interval;
        }
        breakers[numbreaks] = end-start;
        return breakers;
    }
    public double[] smoothValues(double[] array) {
        //!!! This must be checked.  The current implementation introduces a
        // phase shift in the fitted line.
        final int WINDOW_SIZE = 21;
        double mean = 0.0;
        int window = WINDOW_SIZE;
        double weight = 1.0 / window;
        double[] avg = new double[array.length];
        DescriptiveStatistics desc = new DescriptiveStatistics( window);
        for (int i = 0; i < window; i++) {
            desc.addValue(array[i]);
            avg[i] = array[i];
        }
        for (int i = window; i < array.length; i++) {
            mean = desc.getMean();
            avg[i] = weight * array[i] + (1.0 - weight) * mean;
            desc.addValue(array[i]);
        }
        return avg;
    }
    public double[] getBaselineFunction() {
        return bnn;
    }
    public int[] getBreaks() {
        return breaks;
    }
    public double[] getBaselineCorrectedArray() {
        return result;
    }
    public double[] getRMSvalues() {
        return rms;
    }
    public int[] getPolyBreaks() {
        return polyBreaks;
    }
    public double[] getABCvelocity() {
        return velfinal;
    }
    public double[] getABCdisplacement() {
        return displace;
    }
    public double[] getABCacceleration() {
        return accel;
    }public int[] getConfigRanges() {
        int[] out = new int[8];
        out[0] = numbreakslo;
        out[1] = numbreakshi;
        out[2] = degreeSlo;
        out[3] = degreeShi;
        out[4] = degreeP1lo;
        out[5] = degreeP1hi;
        out[6] = degreeP2lo;
        out[7] = degreeP2hi;
        return out;
    }
}
