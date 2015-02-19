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

import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.V2Status;
import SmException.SmException;
import SmUtilities.ABCSortPairs;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;
import java.util.ArrayList;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 *
 * @author jmjones
 */
public class ABC2 {
    private final int NUM_SEGMENTS = 3;
    private final int RESULT_PARMS = 14;
    private final int NUM_BREAKS = 10;
    private final int CUBIC_ORD = 3;
    
    private final int MOVING_WINDOW = 200;
    private final double EPSILON = 0.00001;
    private double dtime;
    private double lowcut;
    private double highcut;
    private int numpoles;
    private int estart;
    private double taplength;
    private double[] velocity;
    private double[] velstart;
    private double[] displace;
    private double[] accel;
    private final int degreeSlo;
    private final int degreeShi;
    private final int degreeP1lo;
    private final int degreeP1hi;
    private final int degreeP3lo;
    private final int degreeP3hi;
    private double[] bnn;
    private double[] b1;
    private int bestfirstdegree;
    private int bestthirddegree;
    private double rms1;
    private ArrayList<double[]> params;
    private int[] breaks;
    private double[] rms;
    private int[] ranking;
    private int solution;
    private int counter;
    private int taplength_calculated;
    
    public ABC2(double delttime, double[] invel, 
                                      double lowcut,double highcut, int numpoles,
                                      int ppick, double taplengthtime) {
        this.dtime = delttime;
        this.estart = ppick;
        this.taplength = taplengthtime;
        this.velstart = invel;
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numpoles = numpoles;
        this.rms = new double[NUM_SEGMENTS];
        this.taplength_calculated = 0;
        this.solution = 0;
        this.counter = 1;
        this.bestfirstdegree = 0;
        this.bestthirddegree = 0;
        
        //Get the values out of the configuration file and screen for correctness.
        //Number of spline breaks
        //spline order
        this.degreeSlo = validateConfigParam(SPLINE_ORDER_LOWER, 
                                                DEFAULT_SPLINE_ORD_LOWER,
                                                DEFAULT_SPLINE_ORD_LOWER,
                                                DEFAULT_SPLINE_ORD_UPPER);
        this.degreeShi = validateConfigParam(SPLINE_ORDER_UPPER, 
                                                DEFAULT_SPLINE_ORD_UPPER,
                                                degreeSlo,
                                                DEFAULT_SPLINE_ORD_UPPER);
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
        this.degreeP3lo = validateConfigParam(THIRD_POLY_ORDER_LOWER, 
                                                DEFAULT_3RD_POLY_ORD_LOWER,
                                                DEFAULT_3RD_POLY_ORD_LOWER,
                                                DEFAULT_3RD_POLY_ORD_UPPER);
        this.degreeP3hi = validateConfigParam(THIRD_POLY_ORDER_UPPER, 
                                                DEFAULT_3RD_POLY_ORD_UPPER,
                                                degreeP3lo,
                                                DEFAULT_3RD_POLY_ORD_UPPER);
        
    }
    public final int validateConfigParam( String configparm, int defval, int lower,
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
    public VFileConstants.V2Status findFit() throws SmException {
        int vlen = velstart.length;
        int endval = (int)(0.8 * vlen);
        int spl_break = NUM_BREAKS;
        int startval = estart + MOVING_WINDOW;
        double[] v1poly = new double[estart];
        double[] paddedvelocity;
        double[] paddeddisplace;
        boolean success = false;
        ButterworthFilter filter;
        params = new ArrayList<>();
        double[] onerun;
        boolean pass = false;
        VFileConstants.V2Status status = V2Status.NOABC;
        QCcheck qcchecker = new QCcheck();
        qcchecker.validateQCvalues();
        qcchecker.findWindow(lowcut, (1.0/dtime), estart);
        filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcut,highcut,dtime,numpoles, true);
        if (!valid) {
            throw new SmException("ABC: Invalid bandpass filter input parameters");
        }
        //Fit done for input velocity from time 0 to event onset.  RMS value
        //returned for best fit, array b1 contains the baseline function, and
        //variable bestFirstDegree contains the degree of the fit.
        rms[0] = findFirstPolynomialFit();
        
        for (int t2 = startval; t2 <= endval; t2 += MOVING_WINDOW) {
            if ((t2-estart) >= ((int)1.0/lowcut)) {
                //remove the spline fit from velocity
                velocity = makeCorrection(velstart,t2,spl_break);
                //now filter velocity
                paddedvelocity = filter.applyFilter(velocity, taplength, estart);
                 //remove any mean value
                ArrayStats velmean = new ArrayStats( paddedvelocity );
                ArrayOps.removeValue(paddedvelocity, velmean.getMean());
                //integrate to get displacement, differentiate
                //for acceleration
                paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
                displace = new double[velocity.length];
                System.arraycopy(paddedvelocity, filter.getPadLength(), velocity, 0, velocity.length);
                System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
                accel = ArrayOps.Differentiate(velocity, dtime);
                qcchecker.qcVelocity(velocity);
                qcchecker.qcDisplacement(displace);
                //store the results in an array for comparison
                onerun = new double[RESULT_PARMS];
                onerun[0] = Math.sqrt(Math.pow(rms[0], 2) +
                        Math.pow(rms[1],2) + Math.pow(rms[2],2));
                onerun[1] = Math.abs(qcchecker.getResidualDisplacement());
                onerun[2] = Math.abs(qcchecker.getInitialVelocity());
                onerun[3] = Math.abs(qcchecker.getResidualVelocity());
                onerun[4] = estart;
                onerun[5] = t2;
                onerun[6] = bestfirstdegree;
                onerun[7] = bestthirddegree;
                onerun[8] = counter;
                onerun[9] = rms[0];
                onerun[10] = rms[1];
                onerun[11] = rms[2];
                onerun[12] = spl_break;
                onerun[13]= CUBIC_ORD;
                //Penalty for initial acceleration step
                ArrayStats accstat = new ArrayStats(accel);
                if (Math.abs(Math.abs(accel[0]) - Math.abs(accstat.getPeakVal())) < 5*Math.ulp(accel[0])) {
                    onerun[0] = 1000;
                }
                params.add(onerun);
                counter++;
            }
        }
        //exit with error status if no estimates performed
        if (params.isEmpty()) {
            status = V2Status.NOABC;
            return status;
        }
        //Sort the results based on cumulative rms
        System.out.println("length of params: " + params.size());
        int count = 0;
        ABCSortPairs sorter = new ABCSortPairs();
        double[] temp;
        for (int i = 0; i < params.size(); i++) {
            temp = params.get(i);
            sorter.addPair(temp[0], count++);
        }
        ranking = sorter.getSortedVals();
        double[] eachrun;
        
        //check each solution against the QA values and find the first that passes
        for (int idx : ranking) {
            eachrun = params.get(idx);
            success = (eachrun[2] <= qcchecker.getInitVelocityQCval()) && 
                          (eachrun[3] <= qcchecker.getResVelocityQCval()) && 
                                (eachrun[1] <= qcchecker.getResDisplaceQCval());
            if (success) {
                velocity = makeCorrection(velstart,(int)eachrun[5],(int)eachrun[12]);

                paddedvelocity = filter.applyFilter(velocity, taplength, estart);
                taplength_calculated = filter.getTaperlength();
                
                ArrayStats velmean = new ArrayStats( paddedvelocity );
                ArrayOps.removeValue(paddedvelocity, velmean.getMean());
                
                paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
                displace = new double[velocity.length];
                System.arraycopy(paddedvelocity, filter.getPadLength(), velocity, 0, velocity.length);
                System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
                accel = ArrayOps.Differentiate(velocity, dtime);

                status = V2Status.GOOD;
                solution = idx;
                System.out.println("ABC: found passing solution");
                System.out.println("ABC: start: " + eachrun[4] + "  stop: " + eachrun[5]);
                break;
            }
        }
        if (status != V2Status.GOOD) { //just pick the lowest rms run to return
            solution = 0;
            eachrun = params.get(solution);
            velocity = makeCorrection(velstart,(int)eachrun[5],(int)eachrun[12]);
            //now filter velocity
            paddedvelocity = filter.applyFilter(velocity, taplength, estart);
            taplength_calculated = filter.getTaperlength();
            ArrayStats velmean = new ArrayStats( paddedvelocity );
            ArrayOps.removeValue(paddedvelocity, velmean.getMean());

            paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
            displace = new double[velocity.length];
            System.arraycopy(paddedvelocity, filter.getPadLength(), velocity, 0, velocity.length);
            System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
            accel = ArrayOps.Differentiate(velocity, dtime);
            status = V2Status.FAILQC;
        }
        return status;
    }
    public double findFirstPolynomialFit() {
        double bestrms = Double.MAX_VALUE;
        int bestdegree = 0;
        int len = estart+1;
        int numiter = (degreeP1hi > degreeP1lo) ? (degreeP1hi - degreeP1lo) : 1;
        double[] bestcoefs = new double[0];
        double[] coefs;
        double[] h1 = new double[len];
        double[] r1 = new double[len];
        b1 = new double[len];
        double rms1;
        System.arraycopy(velstart,0,h1,0,h1.length);
        System.arraycopy(velstart,0,r1,0,r1.length);
        for (int order1 = degreeP1lo; order1 <= degreeP1hi; order1++) {
            //find best fit for 1st polynomial, since its length doesn't change
            coefs = ArrayOps.findPolynomialTrend(h1, order1, dtime);
            ArrayOps.removePolynomialTrend(r1, coefs, dtime);
            rms1 = ArrayOps.rootMeanSquare(h1, r1);
            if (rms1 < bestrms) {
                bestrms = rms1;
                bestdegree = order1;
                bestcoefs = coefs;
            }
        }
        double[] time = ArrayOps.makeTimeArray( dtime, h1.length);
        PolynomialFunction poly = new PolynomialFunction( bestcoefs );
        for (int i = 0; i < len; i++) {
            b1[i] = poly.value(time[i]);
        }
        bestfirstdegree = bestdegree;
        return bestrms;
    }
    public double[] makeCorrection( double[] array, int break2, int numknots) {
        double[] h2;
        double[] h3;
        double[] r1;
        double[] r2;
        double[] r3;
        int break1 = estart;
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
//        breaks = makeBreaks( break1, break2, spl_order);
        
//        h1 = new double[break1];
        h2 = new double[break2-break1+1]; //make these longer to overlap
        h3 = new double[array.length-break2+1];
//        System.arraycopy(array, 0, h1, 0, break1);
        System.arraycopy(array, break1, h2, 0, break2-break1+1);
        System.arraycopy(array, break2-1, h3, 0, array.length-break2);
        double[] result = new double[ array.length ];
        
        //Get the polynomials that were fitted to the input array
        //Construct the baseline function from each section
        PolynomialSplineFunction sp2 = getSplines(h2, break1, break2, numknots);
        double[] fit2 = new double[h2.length];
        for (int i = 0; i < h2.length; i++) {
            fit2[i] = sp2.value(time[i]);
        }
        h3[0] = fit2[fit2.length-1];
        double[] fit3 = find3rdPolyFit(h3);
        this.bestthirddegree = 1;
        System.out.println("best third degree: " + bestthirddegree);

        bnn = new double[time.length];
        for (int i = 0; i < bnn.length; i++) {
            if ( i < break1) {
                bnn[i] = b1[i];
            } else if ( i >= break2) {
                bnn[i] = fit3[i - break2+1];
            } else {
                bnn[i] = fit2[i - break1];
            }
        }
        //smooth out discontinuities in the baseline
        //function before removing it from the input array.
        ArrayOps.perform3PtSmoothing(bnn);
        
//        r1 = new double[h1.length];
        r2 = new double[h2.length];
        r3 = new double[h3.length];
        //Remove the baseline function from the input array
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
            if ( i >= break2) {
                r3[i-break2] = result[i];
            } else if (i >= break1) {
                r2[i-break1] = result[i];
            }
        }
        rms[1] = ArrayOps.rootMeanSquare(h2,r2);
        rms[2] = ArrayOps.rootMeanSquare(h3,r3);
        
        return result;
    }
    public PolynomialSplineFunction getSplines( double[] vals, int start, 
                                                        int end, int numknots) {
        int len;
        double[] loctime;
        double[] subset;
        
        PolynomialSplineFunction spfunction;
//        LoessInterpolator loess = new LoessInterpolator();
//        double[] time = ArrayOps.makeTimeArray(dtime, vals.length);
//        spfunction = loess.interpolate(time, vals);
        
        SplineInterpolator spline = new SplineInterpolator();
        int[] inbreaks = makeBreaks(start, end, numknots);
        double[] knots = new double[inbreaks.length];
        double[] knotvals = new double[inbreaks.length];
        for (int i = 0; i < inbreaks.length; i++) {
            knots[i] = inbreaks[i] * dtime;
            if (i == 0) {
                knotvals[i] = b1[b1.length-1];
            } else {
                knotvals[i] = vals[inbreaks[i]];
            }
        }
        spfunction = spline.interpolate(knots, knotvals);
        return spfunction;    
    }
    public double[] find3rdPolyFit(double[] array) {
        double[] result;
        LoessInterpolator loess = new LoessInterpolator();
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
        result = loess.smooth( time, array);
        return result;
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
    public double[] getBaselineFunction() {
        return bnn;
    }
    public int[] getRanking() {
        return ranking;
    }
    public int getSolution() {
        return solution;
    }
    public ArrayList<double[]> getParameters() {
        return params;
    }
    public double[] getSolutionParms(int sol) {
        return params.get(sol);
    }
    public int getNumRuns() {
        return params.size();
    }
    public int getMovingWindow() {
        return MOVING_WINDOW;
    }
    public int getCounter() {
        return counter;
    }
    public double[] getRMSvalues() {
        return rms;
    }
    public double[] getABCvelocity() {
        return velocity;
    }
    public double[] getABCdisplacement() {
        return displace;
    }
    public double[] getABCacceleration() {
        return accel;
    }
    public int getCalculatedTaperLength() {
        return this.taplength_calculated;
    }
    public void clearParamsArray() {
        params.clear();
    }
}
