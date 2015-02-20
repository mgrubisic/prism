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

import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.V2Status;
import SmException.SmException;
import SmUtilities.ABCSortPairs;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
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
    private final int NUM_BREAKS = 10;
    
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
    private ArrayList<double[]> params;
    private int[] breaks;
    private double[] rms;
    private int[] ranking;
    private int solution;
    private int counter;
    private int taplength_calculated;
    
    public AdaptiveBaselineCorrection(double delttime, double[] invel, 
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
        
        for (int order1 = degreeP1lo; order1 <= degreeP1hi; order1++) {
            for (int order3 = degreeP3lo; order3 <= degreeP3hi; order3++) {
                for (int spl_order = degreeSlo; spl_order <= degreeShi; spl_order++) {
                    for (int t2 = startval; t2 <= endval; t2 += MOVING_WINDOW) {
                        if ((t2-estart) >= ((int)1.0/lowcut)) {
                            //remove the spline fit from velocity
                            velocity = makeCorrection(velstart,estart,t2,spl_break,spl_order,
                                                        order1,order3);
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
                            onerun[6] = order1;
                            onerun[7] = order3;
                            onerun[8] = counter;
                            onerun[9] = rms[0];
                            onerun[10] = rms[1];
                            onerun[11] = rms[2];
                            onerun[12] = spl_break;
                            onerun[13]= spl_order;
                            //Penalty for initial acceleration step
                            ArrayStats accstat = new ArrayStats(accel);
                            if (Math.abs(Math.abs(accel[0]) - Math.abs(accstat.getPeakVal())) < 5*Math.ulp(accel[0])) {
                                onerun[0] = 1000;
                            }
                            params.add(onerun);
                            counter++;
                        }
                    }
                }
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
                velocity = makeCorrection(velstart,(int)eachrun[4],(int)eachrun[5],
                                        (int)eachrun[12],(int)eachrun[13],
                                        (int)eachrun[6],(int)eachrun[7]);

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
            velocity = makeCorrection(velstart,(int)eachrun[4],(int)eachrun[5],
                                    (int)eachrun[12],(int)eachrun[13],
                                    (int)eachrun[6],(int)eachrun[7]);
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
    public double[] makeCorrection( double[] array, int break1, int break2, int numknots,
                        int degreeS, int degreeP1, int degreeP2 ) {
        double[] h1;
        double[] h2;
        double[] h3;
        double[] r1;
        double[] r2;
        double[] r3;
        double[] bnt;
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
//        breaks = makeBreaks( break1, break2, spl_order);
        
        h1 = new double[break1];
        h2 = new double[break2-break1+1];
        h3 = new double[array.length-break2];
        System.arraycopy(array, 0, h1, 0, break1);
        System.arraycopy(array, break1, h2, 0, break2-break1+1);
        System.arraycopy(array, break2, h3, 0, array.length-break2);
        double[] result = new double[ array.length ];
        
        //Get the polynomials that were fitted to the input array
        //Construct the baseline function from each section
        PolynomialSplineFunction spfunction = getSplines(h2, break1, break2, numknots, degreeS);
        double[] coefs1 = ArrayOps.findPolynomialTrend(h1, degreeP1, dtime);
        double[] coefs3 = ArrayOps.findPolynomialTrend(h3, degreeP2, dtime);
        
        PolynomialFunction b1poly = new PolynomialFunction( coefs1 );
        PolynomialFunction b3poly = new PolynomialFunction( coefs3 );

        bnt = new double[time.length];
        bnn = new double[time.length];
        for (int i = 0; i < bnn.length; i++) {
            if ( i < break1) {
                bnt[i] = b1poly.value(time[i]);
            } else if ( i >= break2) {
                bnt[i] = b3poly.value(time[i] - (break2*dtime));
            } else {
                bnt[i] = spfunction.value(time[i] - (break1*dtime));
            }
        }
        bnn = ArrayOps.perform3PtSmoothing(bnt);
        //If smoothing selected, then smooth out discontinuities in the baseline
        //function before removing it from the input array.
        r1 = new double[h1.length];
        r2 = new double[h2.length];
        r3 = new double[h3.length];
        //Remove the baseline function from the input array
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
            if ( i < break1) {                
                r1[i] = result[i];
            } else if ( i >= break2) {
                r3[i-break2] = result[i];
            } else {
                r2[i-break1] = result[i];
            }
        }
        rms[0] = ArrayOps.rootMeanSquare(h1,r1);
        rms[1] = ArrayOps.rootMeanSquare(h2,r2);
        rms[2] = ArrayOps.rootMeanSquare(h3,r3);
        
        return result;
    }
    public PolynomialSplineFunction getSplines( double[] vals, int start, int end,  
                                                        int numknots, int degree ) {
        int len;
        double[] loctime;
        double[] subset;
        
        PolynomialSplineFunction spfunction;
        SplineInterpolator spline = new SplineInterpolator();
        int[] inbreaks = makeBreaks(start, end, numknots);
        double[] knots = new double[inbreaks.length];
        double[] knotvals = new double[inbreaks.length];
        for (int i = 0; i < inbreaks.length; i++) {
            knots[i] = inbreaks[i] * dtime;
            knotvals[i] = vals[inbreaks[i]];
        }
        spfunction = spline.interpolate(knots, knotvals);
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
