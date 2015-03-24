/*******************************************************************************
 * Name: Java class VFileConstants.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmProcessing;

import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.V2Status;
import SmException.SmException;
import SmUtilities.ABCSortPairs;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;
import java.util.ArrayList;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

/**
 * <p>This class performs the adaptive baseline correction to search for
 * a baseline function that corrects the velocity trace with the goal of passing
 * the quality checks and maximizing goodness of fit. It accomplishes this by 
 * breaking the input velocity trace into 3 segments.
 * </p><p>
 * The first segment extends from the array start to the 
 * event onset.  This segment is fitted with polynomials of different order
 * (defined in the configuration file) and the fit with the lowest rms is selected.
 * Then the next 2 segments are selected through an iterative walk through the 
 * part of the array from event onset to near the end.  
 * </p><p>
 * The last
 * portion is fitted with a polynomial while an interpolating spline function is
 * used to create the middle segment.  The rms is calculated for these 2 baseline
 * functions, the input velocity is corrected by the 3 baseline segments, and after
 * filtering and integration the QC checks are recorded for each iteration along
 * with a goodness of fit value. 
 * </p><p>
 * The goodness of fit values are ranked, and the lowest ranking iteration that
 * also passes the QC checks is chosen as the final baseline correction.</p>
 * @author jmjones
 */
public class ABC2 {
    private final int NUM_SEGMENTS = 3;
    private final int RESULT_PARMS = 14;
    
    private final int MOVING_WINDOW = 200;
    private final double dtime;
    private final double lowcut;
    private final double highcut;
    private final int numroll;
    private final int estart;
    private final double taplength;
    private double[] velocity;
    private final double[] velstart;
    private double[] displace;
    private double[] accel;
    private double []paddedaccel;
    ButterworthFilter filter;
    private final int degreeP1lo;
    private final int degreeP1hi;
    private final int degreeP3lo;
    private final int degreeP3hi;
    private double[] bnn;
    private double[] b1;
    private int bestfirstdegree;
    private int bestthirddegree;
    private ArrayList<double[]> params;
    private int[] breaks;
    private double[] rms;
    private int[] ranking;
    private int solution;
    private int counter;
    private int taplength_calculated;
    /**
     * The constructor for ABC validates the low and high ranges for the 1st and
     * 3rd polynomial orders that were defined in the configuration file.
     * @param delttime sampling interval, in seconds/sample
     * @param invel velocity array to find the baseline function for
     * @param lowcut lowcut filter value to use
     * @param highcut high cut filter value
     * @param numroll filter order / 2
     * @param ppick event onset index
     * @param taplengthtime minimum number of seconds for the filter taper length
     */
    public ABC2(double delttime, double[] invel, 
                                      double lowcut,double highcut, int numroll,
                                      int ppick, double taplengthtime) {
        this.dtime = delttime;
        this.estart = ppick;
        this.taplength = taplengthtime;
        this.velstart = invel;
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numroll = numroll;
        this.rms = new double[NUM_SEGMENTS];
        this.taplength_calculated = 0;
        this.solution = 0;
        this.counter = 1;
        this.bestfirstdegree = 0;
        this.bestthirddegree = 0;
        
        //Get the values out of the configuration file and screen for correctness.
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
    /**
     * Validates the input configuration parameter against the acceptable upper
     * and lower limits.  If input parm is out of range, it is set to the 
     * default value.
     * @param configparm configuration parameter to validate
     * @param defval the default value for this parameter
     * @param lower the acceptable lower limit
     * @param upper the acceptable upper limit
     * @return a valid value for the configuration parameter
     */
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
    /**
     * Finds the best fit for the input velocity array.  This method controls the
     * flow of adaptive baseline correction.  It runs through each iteration, 
     * recording the results, then ranks the results based on the rms values.
     * The iterations are checked in the ranking order and the first iteration
     * found that passes QC is used to generate the baseline-corrected velocity.
     * If no iteration passes QC, then a flag is set to indicate failure and the
     * iteration with the lowest ranked rms value is returned for inspection.
     * The status of NOABC would be returned if the length of the iteration 
     * segment never gets long enough to exceed the lower bound for the filter limit.
     * @return processing status of GOOD, FAILQC, or NOABC
     * @throws SmException if unable to calculate valid filter parameters
     */
    public VFileConstants.V2Status findFit() throws SmException {
        int vlen = velstart.length;
        int endval = (int)(0.8 * vlen);
        int startval = estart + MOVING_WINDOW;
        boolean success = false;
        params = new ArrayList<>();
        double[] onerun;
        VFileConstants.V2Status status = V2Status.NOABC;
        QCcheck qcchecker = new QCcheck();
        qcchecker.validateQCvalues();
        qcchecker.findWindow(lowcut, (1.0/dtime), estart);
        filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcut,highcut,dtime,numroll, true);
        if (!valid) {
            throw new SmException("ABC: Invalid bandpass filter input parameters");
        }
        //Fit done for input velocity from time 0 to event onset.  RMS value
        //returned for best fit, array b1 contains the baseline function, and
        //variable bestFirstDegree contains the degree of the fit.
        rms[0] = findFirstPolynomialFit();
        
        //Iterate to find the 2nd break point which results in the lowest rms
        //for the 3 segments.  For each 3rd polynomial order to try, walk through
        //the array section, increasing the window for the 2nd segment each time.
        //At each iteration, filter, integrate, and differentiate.  Store the
        //QC results for each iteration as well as the rms of the corrected vs. 
        //original segments.
        for (int order3 = degreeP3lo; order3 <= degreeP3hi; order3++) {
            for (int t2 = startval; t2 <= endval; t2 += MOVING_WINDOW) {
                if (((t2-estart)*dtime) >= ((int)1.0/lowcut)) {
                    processTheArrays( t2, order3);
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
                    onerun[7] = order3;
                    onerun[8] = counter;
                    onerun[9] = rms[0];
                    onerun[10] = rms[1];
                    onerun[11] = rms[2];
                    onerun[12] = 0;
                    onerun[13]= 0;
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
        //exit with error status if no estimates performed
        if (params.isEmpty()) {
            status = V2Status.NOABC;
            return status;
        }
        //Sort the results based on cumulative rms
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
                processTheArrays((int)eachrun[5],(int)eachrun[7]);
                status = V2Status.GOOD;
                solution = idx;
                break;
            }
        }
        if (status != V2Status.GOOD) { //just pick the lowest rms run to return
            solution = 0;
            eachrun = params.get(solution);
            processTheArrays((int)eachrun[5],(int)eachrun[7]);
            status = V2Status.FAILQC;
        }
        return status;
    }
    private void processTheArrays( int firstb, int secondb) {
        double[] paddedvelocity;
        double[] paddeddisplace;
        
        //fit a baseline function to each segment and make correction
        velocity = makeCorrection(velstart,firstb,secondb);
        //filter velocity
        paddedvelocity = filter.applyFilter(velocity, taplength, estart);
        taplength_calculated = filter.getTaperlength();
        //remove any mean value
        ArrayStats velmean = new ArrayStats( paddedvelocity );
        ArrayOps.removeValue(paddedvelocity, velmean.getMean());
        //integrate to get displacement, differentiate for acceleration
        paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
        displace = new double[velocity.length];
        System.arraycopy(paddedvelocity, filter.getPadLength(), velocity, 0, velocity.length);
        System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
        paddedaccel = ArrayOps.Differentiate(paddedvelocity, dtime);
        accel = new double[velocity.length];
        System.arraycopy(paddedaccel, filter.getPadLength(), accel, 0, accel.length);
    }
    /**
     * Finds the best fit for the first segment (from 0 to event onset) by
     * iterating over the different polynomial orders and choosing the order that
     * produces a fit with the lowest rms error compared to the original segment.
     * @return the rms value for the best fit
     */
    private double findFirstPolynomialFit() {
        double bestrms = Double.MAX_VALUE;
        int bestdegree = 0;
        int len = estart;
        double[] bestcoefs = new double[0];
        double[] coefs;
        double[] h1 = new double[len];
        b1 = new double[len];
        double rms1;
        PolynomialFunction poly;
        double[] time = ArrayOps.makeTimeArray( dtime, h1.length);
        System.arraycopy(velstart,0,h1,0,h1.length);
        for (int order1 = degreeP1lo; order1 <= degreeP1hi; order1++) {
            //find best fit for 1st polynomial, since its length doesn't change
            coefs = ArrayOps.findPolynomialTrend(h1, order1, dtime);
            poly = new PolynomialFunction( coefs );
            for (int i = 0; i < len; i++) {
                b1[i] = poly.value(time[i]);
            }
            rms1 = ArrayOps.rootMeanSquare(h1, b1);
            if (rms1 < bestrms) {
                bestrms = rms1;
                bestdegree = order1;
                bestcoefs = coefs;
            }
        }
        poly = new PolynomialFunction( bestcoefs );
        for (int i = 0; i < len; i++) {
            b1[i] = poly.value(time[i]);
        }
        bestfirstdegree = bestdegree;
        return bestrms;
    }
    /**
     * Makes the baseline correction on the input array. It first calculates the
     * baseline function for the 3rd segment based on the input order and beginning
     * at the input break index.  Then it calls the spline method to build the
     * interpolating spline between the baseline functions of the 1st and 3rd 
     * segments. It subtracts the baseline function from the input array and
     * calculates the rms of the 2nd and 3rd segments and adds these to the stored
     * rms values.
     * @param array the input array to correct
     * @param break2 the index to split the array at for the 3rd segment
     * @param order3 the order of the 3rd segment polynomial for correction
     * @return the baseline-corrected input array
     */
    private double[] makeCorrection( double[] array, int break2, int order3) {
        double[] h2;
        double[] h3;
        int order2 = 3;
        int break1 = estart;
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
        
        h2 = new double[break2-break1];
        double[] b2 = new double[break2-break1];
        h3 = new double[array.length-break2];
        System.arraycopy(array, break1, h2, 0, break2-break1);
        System.arraycopy(array, break2, h3, 0, array.length-break2);
        double[] result = new double[ array.length ];
        
        //Get the best fit baseline function for the 3rd segment
        double[] b3 = find3rdPolyFit(h3, order3);
        
        //Construct the baseline function from the first and 3rd sections
        bnn = new double[time.length];
        for (int i = 0; i < bnn.length; i++) {
            if ( i < break1) {
                bnn[i] = b1[i];
            } else if ( i >= break2) {
                bnn[i] = b3[i - break2];
            } else {
                bnn[i] = 0.0;
            }
        }
        //Connect the 1st and 3rd segments with the interpolating spline
        getSplineSmooth( bnn, break1, break2, dtime );
        System.arraycopy(bnn,break1,b2,0,break2-break1);
        
        //Remove the baseline function from segments 2 and 3
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
        }
        //Compute the rms of original and corrected segments
        rms[1] = ArrayOps.rootMeanSquare(h2,b2);
        rms[2] = ArrayOps.rootMeanSquare(h3,b3);
        return result;
    }
    /**
     * Finds the 3rd polynomial baseline fit based on the polynomial degree.
     * @param array the input array to fit
     * @param degree the degree of the polynomial to use to fit
     * @return the baseline correction function
     */
    private double[] find3rdPolyFit(double[] array, int degree) {
        double[] result = new double[array.length];
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
        double[] coefs = ArrayOps.findPolynomialTrend(array, degree, dtime);
        PolynomialFunction poly = new PolynomialFunction( coefs );
        for (int i = 0; i < array.length; i++) {
            result[i] = poly.value(time[i]);
        }
        return result;
    }
    /**
     * Connects the 1st and 3rd segments of the baseline correction fit with
     * "the lowest order (smoothest) polynomial baseline connection that
     * continuously connects the initial and final portions" (p.1) from:
     * <p>
     * Wang, Luo-Jia,
     * EERL 96-04, Earthquake Engineering Research Laboratory, 
     * California Institute of Technology Pasadena, 1996-09, 25 pages,
     * </p>
     * @param vals the input array of values
     * @param break1 starting index to connect
     * @param break2 ending index to connect
     * @param intime the time interval between samples
     */
    public void getSplineSmooth( double[] vals, int break1, int break2, double intime ) {
        double start;
        double end;
        double ssq;
        double esq;
        
        int len = vals.length;
        double[] loctime = ArrayOps.makeTimeArray( intime, len);
        double t1 = break1 * intime;
        double t2 = (break2-1) * intime;
        double time12 = intime * 12.0;   //dt12
        double intlen = t2 - t1;        //t21
        
        double a = vals[break1-1];
        double b = vals[break2];
        double c = (   3.0 * vals[break1-5] 
                    - 16.0 * vals[break1-4] 
                    + 36.0 * vals[break1-3] 
                    - 48.0 * vals[break1-2] 
                    + 25.0 * vals[break1-1]   )/ time12;
        
        double d = ( -25.0 * vals[break2] 
                    + 48.0 * vals[break2+1] 
                    - 36.0 * vals[break2+2] 
                    + 16.0 * vals[break2+3] 
                    -  3.0 * vals[break2+4]   )/ time12;

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
    /**
     * Getter for the baseline function
     * @return the baseline function
     */
    public double[] getBaselineFunction() {
        return bnn;
    }
    /**
     * Getter for the array of ranks
     * @return ranked index array of iteration numbers
     */
    public int[] getRanking() {
        return ranking;
    }
    /**
     * Getter for the index of the iteration identified as the solution
     * @return index to the parameter array holing the solution parameters
     */
    public int getSolution() {
        return solution;
    }
    /**
     * Getter for the entire array list of parameters for every iteration
     * @return array list of parameters
     */
    public ArrayList<double[]> getParameters() {
        return params;
    }
    /**
     * Getter for the individual solution array based on the input index
     * @param sol the index for the solution set of parameters
     * @return the array of solution parameters
     */
    public double[] getSolutionParms(int sol) {
        return params.get(sol);
    }
    /**
     * Getter for the number of iterations performed
     * @return the total number of iterations
     */
    public int getNumRuns() {
        return params.size();
    }
    /**
     * Getter for the length of the moving window, used as a step added to the
     * length of the 2nd segment for each iteration
     * @return the length of the moving window
     */
    public int getMovingWindow() {
        return MOVING_WINDOW;
    }
    /**
     * Getter for the number of iterations, should be same as getNumRuns
     * @return counter of the number of iterations
     */
    public int getCounter() {
        return counter;
    }
    /**
     * Getter for the array of RMS values for the winning or return solution
     * @return array of RMS values, one for each segment
     */
    public double[] getRMSvalues() {
        return rms;
    }
    /**
     * Getter for the corrected velocity array
     * @return the final velocity
     */
    public double[] getABCvelocity() {
        return velocity;
    }
    /**
     * Getter for the final displacement array
     * @return the final displacement
     */
    public double[] getABCdisplacement() {
        return displace;
    }
    /**
     * Getter for the corrected acceleration array
     * @return the final acceleration
     */
    public double[] getABCacceleration() {
        return accel;
    }
    /**
     * Getter for the corrected padded acceleration array
     * @return the padded acceleration
     */
    public double[] getABCpaddedacceleration() {
        return paddedaccel;
    }
    /**
     * Getter for the calculated taper length used during filtering
     * @return the calculated taper length
     */
    public int getCalculatedTaperLength() {
        return this.taplength_calculated;
    }
    /**
     * Clears the params array to release dynamic memory storage
     */
    public void clearParamsArray() {
        params.clear();
    }
}
