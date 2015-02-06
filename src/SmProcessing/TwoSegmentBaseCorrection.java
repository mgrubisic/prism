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
import static SmConstants.VFileConstants.DEFAULT_1ST_POLY_ORD_LOWER;
import static SmConstants.VFileConstants.DEFAULT_1ST_POLY_ORD_UPPER;
import static SmConstants.VFileConstants.DEFAULT_2ND_POLY_ORD_LOWER;
import static SmConstants.VFileConstants.DEFAULT_2ND_POLY_ORD_UPPER;
import SmException.SmException;
import static SmProcessing.ArrayOps.makeTimeArray;
import SmUtilities.ABCSortPairs;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.FIRST_POLY_ORDER_LOWER;
import static SmUtilities.SmConfigConstants.FIRST_POLY_ORDER_UPPER;
import static SmUtilities.SmConfigConstants.SECOND_POLY_ORDER_LOWER;
import static SmUtilities.SmConfigConstants.SECOND_POLY_ORDER_UPPER;
import SmUtilities.SmDebugLogger;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 *
 * @author jmjones
 */
public class TwoSegmentBaseCorrection {
    private final int NUM_SEGMENTS = 2;
    private final int RESULT_PARMS = 14;
    
    private final double EPSILON = 0.001;
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
    private final int degreeP1lo;
    private final int degreeP1hi;
    private final int degreeP2lo;
    private final int degreeP2hi;
    private double[] bnn;
    private double[] result;
    private SmDebugLogger elog;
    private boolean writeArrays;
    private int taplength_calculated;
    private int order1;
    private int order2;
    private double[] onerun;
    
    public TwoSegmentBaseCorrection(double delttime, double[] invel, 
                                      double lowcut,double highcut,int numpoles,
                                      int ppick, double taplengthtime) {
        this.dtime = delttime;
        this.estart = ppick;
        this.taplength = taplengthtime;
        this.velstart = invel;
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numpoles = numpoles;
        this.order1 = 0;
        this.order2 = 0;
        this.elog = SmDebugLogger.INSTANCE;
        this.taplength_calculated = 0;
        onerun = new double[RESULT_PARMS];
        ConfigReader config = ConfigReader.INSTANCE;
        this.degreeP1lo = validateConfigParam(FIRST_POLY_ORDER_LOWER, 
                                                DEFAULT_1ST_POLY_ORD_LOWER,
                                                DEFAULT_1ST_POLY_ORD_LOWER,
                                                DEFAULT_1ST_POLY_ORD_UPPER);
        this.degreeP1hi = validateConfigParam(FIRST_POLY_ORDER_UPPER, 
                                                DEFAULT_1ST_POLY_ORD_UPPER,
                                                degreeP1lo,
                                                DEFAULT_1ST_POLY_ORD_UPPER);
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
    
    public VFileConstants.V2Status startIterations() throws SmException {
        int vlen = velstart.length;
        velocity = new double[vlen];
        boolean success;
        ButterworthFilter filter;
        double[] paddedvelocity;
        double[] paddeddisplace;
        VFileConstants.V2Status status = VFileConstants.V2Status.NOABC;
        QCcheck qcchecker = new QCcheck();
        if (!qcchecker.validateQCvalues()){
            throw new SmException("Error extracting QC values from configuration file");
        }
        qcchecker.findWindow(lowcut, (1.0/dtime), estart);
        filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcut, 
                                                highcut,dtime, numpoles, true);
        if (!valid) {
            throw new SmException("ABC: Invalid bandpass filter input parameters");
        }
        
        velocity = makeCorrection(velstart,estart,degreeP1hi,degreeP2hi);
        //now filter velocity
        paddedvelocity = filter.applyFilter(velocity, taplength, estart);
        taplength_calculated = filter.getTaperlength();
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
        onerun[0] = 0.0;
        onerun[1] = Math.abs(qcchecker.getResidualDisplacement());
        onerun[2] = Math.abs(qcchecker.getInitialVelocity());
        onerun[3] = Math.abs(qcchecker.getResidualVelocity());
        onerun[4] = estart;
        onerun[5] = 0;
        onerun[6] = order1;
        onerun[7] = order2;

        success = (onerun[2] <= qcchecker.getInitVelocityQCval()) && 
                    (onerun[3] <= qcchecker.getResVelocityQCval()) && 
                        (onerun[1] <= qcchecker.getResDisplaceQCval());
        onerun[8] = (success) ? 1 : 0;  //QC flag of pass or fail
        onerun[9] = 0.0;
        onerun[10] = 0.0;
        onerun[11] = 0.0;
        onerun[12] = velocity[0];
        onerun[13]= displace[0];

        status = (success) ? VFileConstants.V2Status.GOOD : VFileConstants.V2Status.FAILQC;
        return status;
    }
    
    public double[] makeCorrection(double[] array, int breakpt,int degreeP1, int degreeP2 ) {
        double[] h1;
        double[] h2;
        double[] time = ArrayOps.makeTimeArray(dtime, array.length);
        double[] coefs;
        
        h1 = new double[breakpt];
        h2 = new double[array.length-breakpt];
        System.arraycopy(array, 0, h1, 0, breakpt);
        System.arraycopy(array, breakpt, h2, 0, array.length-breakpt);
        result = new double[ array.length ];
                
        PolynomialFunction b1poly = findFirstPoly( h1, 2, dtime);
        coefs = b1poly.getCoefficients();
        order1 = coefs.length - 1;
        double[] test = new double[1];
        test[0] = h1[h1.length-1];
//        System.out.println("start pt: " + test[0]);
        PolynomialFunction b2poly = findSecondPoly( h2, 3, test, dtime);
        order2 = b2poly.getCoefficients().length - 1;

        bnn = new double[time.length];
        for (int i = 0; i < bnn.length; i++) {
            if ( i < breakpt) {
                bnn[i] = b1poly.value(time[i]);
            } else {
                bnn[i] = b2poly.value(time[i] - (breakpt*dtime));
            }
        }
        //Remove the baseline function from the input array
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
        }
        return result;
    }
    public PolynomialFunction findFirstPoly( double[] inarr, int maxOrder, double dtime) {
        
        ArrayList<PolynomialFunction> polys = new ArrayList<>();
        int len = inarr.length;
        double[] corrected = new double[ len ];
        double[] baseline = new double[ len ];
        int numsteps = maxOrder;
        ArrayList<Double> rmsvals = new ArrayList<>();
        PolynomialFunction bestfit;
        double[] time = ArrayOps.makeTimeArray(dtime, len);
        double[] coefs;
        int winrank;
        ABCSortPairs sorter = new ABCSortPairs();
        int[] ranking;
        
        for (int i = 1; i <= maxOrder; i++) {
            coefs = ArrayOps.findPolynomialTrend(inarr, i, dtime);
            PolynomialFunction basepoly = new PolynomialFunction( coefs );
            polys.add(basepoly);
            
            for (int j = 0; j < len; j++) {
                baseline[j] = basepoly.value(time[j]);
            }
            rmsvals.add(ArrayOps.rootMeanSquare( inarr, baseline));
        }
//        System.out.println("1st poly:");
//        for (int k = 0; k < rmsvals.size(); k++) {
//            System.out.println("order: " + (k+1) + "  rms: " + rmsvals.get(k));
//        }
        winrank = 0;
        if (numsteps > 1) {
            for (int i = 0; i < rmsvals.size(); i++) {
                sorter.addPair(rmsvals.get(i), i);
            }
            ranking = sorter.getSortedVals();
            winrank = ranking[0];
            for (int i = 0; i < ranking.length-1; i++) {
                if (Math.abs(rmsvals.get(ranking[i])-rmsvals.get(ranking[i+1])) < 0.001){
                    winrank = (ranking[i] > ranking[i+1]) ? ranking[i+1] : winrank;
                }
            }
        }
//        System.out.println("win rank: " + (winrank+1));
        bestfit = polys.get(winrank);
        return bestfit;
    }
    public PolynomialFunction findSecondPoly( double[] inarr, int maxOrder, double[]coefs1, double dtime) {
        
        ArrayList<PolynomialFunction> polys = new ArrayList<>();
        int len = inarr.length;
        double[] corrected = new double[ len ];
        double[] baseline = new double[ len ];
        int numsteps = maxOrder;
        ArrayList<Double> rmsvals = new ArrayList<>();
        PolynomialFunction bestfit;
        double[] time = ArrayOps.makeTimeArray(dtime, len);
        double[] coefs;
        int winrank;
        ABCSortPairs sorter = new ABCSortPairs();
        int[] ranking;
        double[] guess;
        
        for (int i = 1; i <= maxOrder; i++) {
            guess = new double[i];
            Arrays.fill(guess, 0.0);
            if (guess.length >= coefs1.length) {
                System.arraycopy(coefs1,0,guess,0,coefs1.length);
            } else {
                System.arraycopy(coefs1,0,guess,0,guess.length);
            }
            coefs = ArrayOps.findPolynomialTrendWithGuess(inarr, guess, dtime);
            PolynomialFunction basepoly = new PolynomialFunction( coefs );
            polys.add(basepoly);
            
            for (int j = 0; j < len; j++) {
                baseline[j] = basepoly.value(time[j]);
            }
            rmsvals.add(ArrayOps.rootMeanSquare( inarr, baseline));
        }
//        System.out.println("2nd poly:");
//        for (int k = 0; k < rmsvals.size(); k++) {
//            System.out.println("order: " + (k+1) + "  rms: " + rmsvals.get(k));
//        }
        winrank = 0;
        if (numsteps > 1) {
            for (int i = 0; i < rmsvals.size(); i++) {
                sorter.addPair(rmsvals.get(i), i);
            }
            ranking = sorter.getSortedVals();
            winrank = ranking[0];
            for (int i = 0; i < ranking.length-1; i++) {
                if (Math.abs(rmsvals.get(ranking[i])-rmsvals.get(ranking[i+1])) < 0.001){
                    winrank = (ranking[i] > ranking[i+1]) ? ranking[i+1] : winrank;
                }
            }
        }
//        System.out.println("win rank: " + (winrank+1));
        bestfit = polys.get(winrank);
        return bestfit;
    }
    public double[] getBaselineFunction() {
        return bnn;
    }
    public double[] getBaselineCorrectedArray() {
        return result;
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
    public double getInitialVelocity() {
        return onerun[12];
    }
    public double[] getParams() {
        return onerun;
    }
    public double getInitialDisplace() {
        return onerun[13];
    }
    public int getCalculatedTaperLength() {
        return this.taplength_calculated;
    }
}
