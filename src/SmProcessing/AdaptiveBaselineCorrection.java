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
import SmUtilities.ABCSortPairs;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;
import SmUtilities.SmErrorLogger;
import java.util.ArrayList;
import java.util.Arrays;
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
    
//    private final double TOL_RES_DISP = 0.001;
//    private final double TOL_INIT_VEL = 0.001;
//    private final double TOL_RES_VEL = 0.001;
//    private final double TOL_RMS_VEL = 0.001;
    private final double TOL_RES_DISP = 0.001;
    private final double TOL_INIT_VEL = 0.002;
    private final double TOL_RES_VEL = 0.002;
    
    private int MOVING_WINDOW = 200;
    private final double EPSILON = 0.001;
    private double dtime;
    private double lowcut;
    private double highcut;
    private int numpoles;
    private int estart;
    private double[] velocity;
    private double[] velstart;
    private double[] displace;
    private double[] accel;
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
    private int[] ranking;
    private int solution;
    private int counter;
    private SmErrorLogger elog;
    private boolean writeArrays;
    
    public AdaptiveBaselineCorrection(double delttime, double[] invel, 
                                      double lowcut,double highcut,int numpoles,
                                      int ppick) {
        this.dtime = delttime;
        this.estart = ppick;
        this.velstart = invel;
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numpoles = numpoles;
        this.rms = new double[NUM_SEGMENTS];
        this.polyBreaks = new int[NUM_SEGMENTS-1];
        this.solution = 0;
        this.counter = 1;
        this.elog = SmErrorLogger.INSTANCE;
        writeArrays = true;
        
        //Get the values out of the configuration file and screen for correctness.
        //Number of spline breaks
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
    
    public V2Status startIterations() throws SmException {
        //The matlab code works with the time index into the array, while this
        //implementation just uses the index into the array, and only uses the
        //time step to create the time arrays as needed.  However, this requires
        //checking if ever values are compared against a time-based measurement
        //such as frequency cut-off!!!
        int t11 = estart;  
        int vlen = velstart.length;
        int t22 = (int)(0.8 * vlen);
        int dt1 = MOVING_WINDOW;
        int t21 = t11 + dt1;
        velocity = new double[vlen];
        boolean success;
        ButterworthFilter filter;
        params = new ArrayList<>();
        double[] onerun;
        V2Status status = V2Status.NOABC;
        
        for (int order1 = degreeP1lo; order1 <= degreeP1hi; order1++) {
            for (int order2 = degreeP2lo; order2 <= degreeP2hi; order2++) {
                for (int t2 = t21; t2 <= t22; t2 += dt1) {
                    if (((t2-t11)*dtime) >= (1.0/lowcut)) {
                        //remove the spline fit from velocity
                        velocity = makeCorrection(velstart,t11,t2,order1,order2);
                        //now filter velocity
                        filter = new ButterworthFilter();
                        boolean valid = filter.calculateCoefficients(lowcut, 
                                         highcut,dtime, numpoles, true);
                        if (valid) {
                            filter.applyFilter(velocity, estart);
                        } else {
                            throw new SmException("ABC: Invalid bandpass "
                                             + "filter input parameters");
                        }
                        //remove any mean value
                        ArrayStats velmean = new ArrayStats( velocity );
                        ArrayOps.removeValue(velocity, velmean.getMean());
                        //integrate to get displacement, differentiate
                        //for acceleration
                        displace = ArrayOps.Integrate( velocity, dtime);
                        accel = ArrayOps.Differentiate(velocity, dtime);
                        //store the results in an array for comparison
                        int vellen = velocity.length;
                        int dislen = displace.length;
                        int window = (int)(estart * 0.25);
                        int velwindowstart = ArrayOps.findZeroCrossing(velocity, window, 1);
                        int velwindowend = ArrayOps.findZeroCrossing(velocity, vellen-window, 0);
                        int diswindowend = ArrayOps.findZeroCrossing(displace, dislen-window, 0);
                        double velstart = ArrayOps.findSubsetMean(velocity, 0, velwindowstart);
                        double velend = ArrayOps.findSubsetMean(velocity, velwindowend,
                                                                        vellen);
                        double disend = ArrayOps.findSubsetMean(displace, diswindowend,
                                                                        dislen);
                        onerun = new double[RESULT_PARMS];
                        onerun[0] = Math.sqrt(Math.pow(rms[0], 2) +
                                Math.pow(rms[1],2) + Math.pow(rms[2],2));
                        onerun[1] = Math.abs(disend);
                        onerun[2] = Math.abs(velstart);
                        onerun[3] = Math.abs(velend);
                        onerun[4] = t11;
                        onerun[5] = t2;
                        onerun[6] = order1;
                        onerun[7] = order2;
                        onerun[8] = counter;
                        onerun[9] = rms[0];
                        onerun[10] = rms[1];
                        onerun[11] = rms[2];
                        onerun[12] = 0;
                        onerun[13]= 0;
                        //Penalty for initial acceleration step
                        ArrayStats accstat = new ArrayStats(accel);
                        if (Math.abs(Math.abs(accel[0]) - Math.abs(accstat.getPeakVal())) < EPSILON) {
                            onerun[0] = 1000;
                        }
                        params.add(onerun);
                        counter++;
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
//            System.out.println("total rms: " + temp[0]);
            sorter.addPair(temp[0], count++);
        }
        ranking = sorter.getSortedVals();
        double[] eachrun;
//        for (int each : ranking) {
//            System.out.println("rank: " + each);
//        }
        
        //check each solution against the QA values and find the first that passes
        for (int idx : ranking) {
            eachrun = params.get(idx);
            success = (eachrun[2] <= TOL_INIT_VEL) && 
                                (eachrun[3] <= TOL_RES_VEL) && 
                                            (eachrun[1] <= TOL_RES_DISP);
            if (success) {
                velocity = makeCorrection(velocity,(int)eachrun[4],(int)eachrun[5],
                                        (int)eachrun[6],(int)eachrun[7]);
                //now filter velocity
                filter = new ButterworthFilter();
                filter.calculateCoefficients(lowcut,highcut,dtime,numpoles,true);
                filter.applyFilter(velocity, estart);
                //remove any mean value
                ArrayStats velmean = new ArrayStats( velocity );
                ArrayOps.removeValue(velocity, velmean.getMean());
                //integrate to get displacement, differentiate
                //for acceleration
                displace = ArrayOps.Integrate( velocity, dtime);
                accel = ArrayOps.Differentiate(velocity, dtime);
                status = V2Status.GOOD;
                solution = idx;
                System.out.println("ABC: found passing solution");
                System.out.println("ABC: winning rank: " + solution);
                System.out.println("ABC: poly1 order: " + eachrun[6]);
                System.out.println("ABC: poly2 order: " + eachrun[7]);
                System.out.println("ABC: start: " + eachrun[4] + "  stop: " + eachrun[5]);
                
//                if (writeArrays) {
//                    elog.writeOutArray(bnn, "baseline.txt");
//                } 
                break;
            }
        }
        if (status != V2Status.GOOD) {
            solution = 0;
            eachrun = params.get(solution);
            velocity = makeCorrection(velocity,(int)eachrun[4],(int)eachrun[5],
                                    (int)eachrun[6],(int)eachrun[7]);
            //now filter velocity
            filter = new ButterworthFilter();
            filter.calculateCoefficients(lowcut,highcut,dtime,numpoles,true);
            filter.applyFilter(velocity, estart);
            //remove any mean value
            ArrayStats velmean = new ArrayStats( velocity );
            ArrayOps.removeValue(velocity, velmean.getMean());
            //integrate to get displacement, differentiate
            //for acceleration
            displace = ArrayOps.Integrate( velocity, dtime);
            accel = ArrayOps.Differentiate(velocity, dtime);
            status = V2Status.FAILQC;
        }
        return status;
    }
    public double[] makeCorrection( double[] array, int break1, int break2, 
                                int degreeP1, int degreeP2 ) {
        polyBreaks[0] = break1;
        polyBreaks[1] = break2;
        double[] h1;
        double[] h2;
        double[] h3;
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
        
        h1 = new double[break1];
        h2 = new double[break2-break1];
        h3 = new double[array.length-break2];
        System.arraycopy(array, 0, h1, 0, break1);
        System.arraycopy(array, break2, h3, 0, array.length-break2);
        result = new double[ array.length ];
        
        //Get the polynomials that were fitted to the input array
        //Construct the baseline function from each section
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
                bnn[i] = 0.0;
            }
        }
        getSpline( bnn, break1, break2);
        
        //Remove the baseline function from the input array
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
        }
        rms[0] = ArrayOps.rootMeanSquare(Arrays.copyOfRange(result,0,break1));
        rms[1] = ArrayOps.rootMeanSquare(Arrays.copyOfRange(result,break1,break2));
        rms[2] = ArrayOps.rootMeanSquare(Arrays.copyOfRange(result,break2,array.length));
        
        return result;
    }
    /**
     * The getSpline algorithm is from:
     * 
     * Processing of near-field earthquake accelerograms, by Luo-Jia Wang,
     * EERL 96-04, Earthquake Engineering Research Laboratory, 
     * California Institute of Technology Pasadena, 1996-09, 25 pages.
     * 
     * @param vals
     * @param break1
     * @param break2 
     */
    public void getSpline( double[] vals, int break1, int break2 ) {
        double start;
        double end;
        double ssq;
        double esq;
        
        int len = vals.length;
        double[] loctime = ArrayOps.makeTimeArray( dtime, len);
        double t1 = break1 * dtime;
        double t2 = (break2-1) * dtime;
        double time12 = dtime * 12.0;   //dt12
        double intlen = t2 - t1;        //t21
        
        double a = vals[break1-1];
        double b = vals[break2];
        double c = (   3.0 * vals[break1-5] 
                    - 16.0 * vals[break1-4] 
                    + 36.0 * vals[break1-3] 
                    - 48.0 * vals[break1-2] 
                    + 25.0 * vals[break1-1] )/ time12;
        
        double d = ( -25.0 * vals[break2] 
                    + 48.0 * vals[break2+1] 
                    - 36.0 * vals[break2+2] 
                    + 16.0 * vals[break2+3] 
                    -  3.0 * vals[break2+4] )/ time12;

        for (int i = break1; i < break2; i++) {
            start = loctime[i] - t1;
            end = loctime[i] - t2;
            ssq = Math.pow(start, 2);
            esq = Math.pow(end, 2);
            vals[i] = (1.0 + ((2.0 * start)/intlen)) * esq * a + 
                      (1.0 - ((2.0 * end)/intlen)) * ssq * b +
                      start * esq * c +
                      end * ssq * d;
            vals[i] = vals[i] / Math.pow(intlen, 2);
        }
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
    public int[] getBreaks() {
        return breaks;
    }
    public int getMovingWindow() {
        return MOVING_WINDOW;
    }
    public int getCounter() {
        return counter;
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
        return velocity;
    }
    public double[] getABCdisplacement() {
        return displace;
    }
    public double[] getABCacceleration() {
        return accel;
    }public int[] getConfigRanges() {
        int[] out = new int[4];
        out[0] = degreeP1lo;
        out[1] = degreeP1hi;
        out[2] = degreeP2lo;
        out[3] = degreeP2hi;
        return out;
    }
    public void clearParamsArray() {
        params.clear();
    }
}
