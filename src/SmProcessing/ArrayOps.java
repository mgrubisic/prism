/*******************************************************************************
 * Name: Java class ArrayOps.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmProcessing;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * The ArrayOps class contains static methods to perform various operations on
 * an input array.  For trend removal operations, the input array is
 * modified in place.  For integration and differentiation, a new array is
 * created with the calculated values.  A method to make an array of time
 * values for a given time step is also included.
 * 
 * The constructor for this class is private, and the "static factory" methods
 * are called directly with the class name without object creation, such as 
 * ArrayOps.removeValue(array, mean).
 * 
 * The linear and polynomial trend methods utilize the apache commons math 
 * package.  http://commons.apache.org/proper/commons-math/
 * 
 * @author jmjones
 */
public class ArrayOps {
    private static final double OPS_EPSILON = 0.00001;
    /**
     * Default private constructor for the static methods
     */
    private ArrayOps() {
    }
    /**
     * Removes the given value from each entry in the array.
     * This method allows for the removal of a mean that was calculated on
     * a subset of another array, or removal of the median.
     * 
     * @param array input array to have value removed from
     * @param val pre-calculated value to remove from the array
     * @return true if calculation performed, false if input parameters are invalid
     */
    public static boolean removeValue( double[] array, double val ) {
        if ((array == null) || (array.length == 0)) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] - val;
        }
        return true;
    }
    /**
     * Removes a linear trend (mx + b) from the input array with the given 
     * time step.  The linear trend is calculated internally using apache commons
     * math simple regression class.
     * 
     * @param array input array to have linear trend removed from
     * @param timestep the sample interval of the input array
     * @return true if calculation performed, false if input parameters are invalid
     */
    public static boolean removeLinearTrend( double[] array, double timestep ) {
        if ((array == null) || (array.length == 0) ||
                                    (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return false;
        }
        int len = array.length;
        double[] time = makeTimeArray( timestep, len);
        SimpleRegression regression = new SimpleRegression();
        for(int i = 0; i < len; i++) {
            regression.addData(time[i], array[i]);
        }
        //Remove the trend from the array
        for (int i = 0; i < len; i++) {
            array [i] = array[i] - regression.predict(time[i]);
        }
        return true;
    }
    /**
     * Fit a linear trend (mx + b) to the input array with the given 
     * time step.  The linear trend is calculated internally using apache commons
     * math simple regression class.
     * 
     * @param array input array to have linear trend fitted from
     * @param timestep the sample interval of the input array
     * @return array with linear function, or array of length 0 if input
     * parameters are invalid.
     */
    public static double[] findLinearTrend( double[] array, double timestep ) {
        if ((array == null) || (array.length == 0) ||
                                    (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return new double[0];
        }
        int len = array.length;
        double[] time = makeTimeArray( timestep, len);
        SimpleRegression regression = new SimpleRegression();
        for(int i = 0; i < len; i++) {
            regression.addData(time[i], array[i]);
        }
        //Get the baseline function
        double[] baseline = new double[len];
        for (int i = 0; i < len; i++) {
            baseline[i] = regression.predict(time[i]);
        }
        return baseline;
    }
    /**
     * Removes a linear trend from an array, where the linear trend is calculated
     * from another array.  This can be used where a portion of an array (the
     * subset array) is used to determine a linear trend, and then the trend
     * is removed from the entire array.  It is assumed that both arrays use
     * the same time step.
     * @param array input array to have the linear trend removed from
     * @param subarray input array to use to calculate the linear trend. This
     * array is not modified in the process.
     * @param timestep the sample interval of the input array
     * @return true if calculation performed, false if input parameters are invalid
     */
    public static boolean removeLinearTrendFromSubArray( double[] array, 
                                        double[] subarray, double timestep ) {
        if ((array == null) || (array.length == 0) || (subarray == null) ||
            (subarray.length == 0) ||(Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return false;
        }
        int lenfull = array.length;
        int lensub = subarray.length;
        double[] time = makeTimeArray( timestep, lensub);
        SimpleRegression regression = new SimpleRegression();
        for(int i = 0; i < lensub; i++) {
            regression.addData(time[i], subarray[i]);
        }
        //Remove the trend from the first array
        double[] fulltime = makeTimeArray( timestep, lenfull);
        for (int i = 0; i < lenfull; i++) {
            array [i] = array[i] - regression.predict(fulltime[i]);
        }
        return true;
    }
    /**
     * Make an array of time values, starting at 0 and incrementing by time step
     * to make an array of length arraylen.
     * 
     * @param timestep sample interval
     * @param arraylen length of desired array
     * @return an array of times, starting at 0, or an array of 0 length if
     * input parameters are invalid
     */
    public static double[] makeTimeArray( double timestep, int arraylen) {
        if ((arraylen == 0) || (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return new double[0];
        }
        double[] time = new double[arraylen];
        for (int i = 0; i < arraylen; i++) {
            time[i] = i * timestep;
        }
        return time;
    }
    /**
     * Finds the mean of values in a subset of the input array.  
     * 
     * @param array array to get the sub-array from
     * @param start starting index of the subset array in the input array
     * @param end ending index of the subset array in the input array
     * @return mean of the values in the sub-array, or Double.MIN_VALUE if
     * input parameters are invalid
     */
    public static double findSubsetMean( double[] array, int start, int end) {
        if ((array == null) || (array.length == 0) || (start < 0) || 
                                        (end > array.length) || (start >= end)) {
            return Double.MIN_VALUE;
        }
        double[] subset = Arrays.copyOfRange( array, start, end );
        ArrayStats accsub = new ArrayStats( subset );
        return accsub.getMean();
    }
    /**
     * Calculates the approximate integral of the input array using the trapezoidal 
     * method.  The spacing between each point is dt.  The value in the init
     * input variable is used as the first value in the integration calculations.
     * 
     * @param array array to be integrated
     * @param dt the time step in seconds
     * @param init initial value to use in the integration
     * @return new array containing the approximate integral of the input points,
     * or an array of 0 length if input parameters are invalid
     */
    public static double[] Integrate( double[] array, double dt, double init ) {
        if ((array == null) || (array.length == 0) || (Math.abs(dt - 0.0) < OPS_EPSILON)) {
            return new double[0];
        }
        int len = array.length;
        double[] calc = new double[len];
        double dt2 = dt / 2.0;
        calc[0] = init;
        for (int i = 1; i < len; i++) {
            calc[i] = calc[i-1] + (array[i-1] + array[i])*dt2;
        }
        return calc;
    }
    /**
     * Calculates the approximate derivative of the input array.
     * 
     * @param array array to be differentiated
     * @param dt the time step in seconds
     * @return new array containing the approximate derivative of the input points,
     * or an array of 0 length if input parameters are invalid
     */
    public static double[] Differentiate( double[] array, double dt) {
        if ((array == null) || (array.length == 0) || (Math.abs(dt - 0.0) < OPS_EPSILON)) {
            return new double[0];
        }
        int len = array.length;
        double[] calc = new double[ len ];
        calc[0] = (array[1] - array[0]) *2.0;
        for (int i = 1; i < len-1; i++) {
            calc[i] = array[i+1] - array[i-1];
        }
        calc[len-1] = (array[len-1] - array[len-2]) * 2.0;
        
        for (int i = 0; i < len; i++) {
            calc[i] = calc[i] / (dt + dt);
        }
        return calc;
    }
    /**
     * Finds a polynomial trend of specified degree from the input array.  The
     * polynomial trend is calculated with the apache commons math 
     * PolynomialCurveFitter class, and the coefficients are returned.
     * 
     * @param array input array containing data with a polynomial trend.
     * @param degree polynomial degree to calculate, such as 2 or 3.
     * @param timestep sample interval
     * @return array of coefficients or an array of 0 length if input parameters
     * are invalid
     */
    public static double[] findPolynomialTrend(double[] array, int degree, 
                                                            double timestep) {
        if ((array == null) || (array.length == 0) || (degree < 1) ||
                                    (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return new double[0];
        }
        int len = array.length;
        double[] time = makeTimeArray( timestep, len);
        ArrayList<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = 0; i < len; i++ ){
            points.add(new WeightedObservedPoint( 1.0, time[i], array[i]));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        double[] coefs = fitter.fit(points);
        return coefs;
    }
    /**
     * Finds a polynomial trend from the input array with an initial guess for
     * the coefficients.
     * The polynomial trend is calculated with the apache commons math 
     * PolynomialCurveFitter class, and the coefficients are returned.
     * 
     * @param array input array containing data with a polynomial trend.
     * @param guess array containing initial coefficient guesses, and the order
     * of this array defines the degree of the polynomial
     * @param timestep sample interval
     * @return array of coefficients or an array of 0 length if input parameters
     * are invalid
     */
    public static double[] findPolynomialTrendWithGuess(double[] array, double[] guess, 
                                                            double timestep) {
        if ((array == null) || (array.length == 0) || (guess.length == 0) ||
                                    (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return new double[0];
        }
        int len = array.length;
        double[] time = makeTimeArray( timestep, len);
        ArrayList<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = 0; i < len; i++ ){
            points.add(new WeightedObservedPoint( 1.0, time[i], array[i]));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(guess.length);
        fitter.withStartPoint(guess);
        double[] coefs = fitter.fit(points);
        return coefs;
    }
    /**
     * Removes a polynomial trend from the input array, with the trend defined
     * by the array of coefficients.
     * @param array input array to remove the polynomial trend from
     * @param coefs array of coefficients defining the polynomial trend.  These 
     * coefficients are returned from the findPolynomialTrend method.
     * @param timestep sample interval
     * @return true if calculation performed, false if input parameters are invalid
     */
    public static boolean removePolynomialTrend(double[] array, double[] coefs, 
                                                            double timestep) {
        if ((array == null) || (array.length == 0) || (coefs == null) ||
               (coefs.length == 0) || (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return false;
        }
        int len = array.length;
        double value;
        double[] time = makeTimeArray( timestep, len);
        PolynomialFunction poly = new PolynomialFunction( coefs );
        for (int i = 0; i < len; i++) {
            array[i] = array[i] - poly.value(time[i]);
        }
        return true;
    }
    /**
     * Calculates the root mean square (rms) value for the input array
     * @param orig original array to calculate rms for
     * @param est estimated array to compare with the original
     * @return the rms value or Double.MIN_VALUE if input arrays are invalid
     */
    public static double rootMeanSquare( double[] orig, double[] est ) {
        if ((orig == null) || (orig.length == 0) || (est == null) || 
                             (est.length == 0) || (orig.length != est.length)) {
            return Double.MIN_VALUE;
        }
        int len = orig.length;
        double rms = 0.0;
        for (int i = 0; i < len; i++) {
            rms += Math.pow((orig[i]-est[i]), 2);
        }
        return Math.sqrt(rms / len);
    }
    /**
     * Tests the input array to see if a first or second order polynomial makes
     * a better fit for the data, and then removes the trend with the best fit.
     * Best fit is determined by calculating the root mean square error between
     * the input data and the baseline trend.
     * @param inarr array to have best fit trend removed, this array is modified
     * @param timestep sample interval
     * @return order of best fit polynomial, or -1 if input parameters are invalid
     */
    public static int removeTrendWithBestFit( double[] inarr, double timestep) {
        if ((inarr == null) || (inarr.length == 0) || 
                                    (Math.abs(timestep - 0.0) < OPS_EPSILON)) {
            return -1;
        }
        //find linear trend for input array and rms compare with original
        int len = inarr.length;
        double[] lcoefs;
        double[] pcoefs;
        double[] time = makeTimeArray( timestep, len);
        PolynomialFunction poly;
        int numOrder;

        //find 1st order polynomial trend for input array and commpare with original
        lcoefs = findPolynomialTrend(inarr, 1, timestep);
//        System.out.println("a: " + lcoefs[0] + "  b: " + lcoefs[1]);
        poly = new PolynomialFunction( lcoefs );
        double[] linbase = new double[len];
        for (int i = 0; i < len; i++) {
            linbase[i] = poly.value(time[i]);
        }
        double linrms = rootMeanSquare( inarr, linbase );
        
        //find 2nd order polynomial trend for input array and rms compare with original
        pcoefs = findPolynomialTrend(inarr, 2, timestep);
//        System.out.println("a: " + pcoefs[0] + "  b: " + pcoefs[1] + " c: " + pcoefs[2]);
        poly = new PolynomialFunction( pcoefs );
        double[] polbase = new double[len];
        for (int i = 0; i < len; i++) {
            polbase[i] = poly.value(time[i]);
        }
        double polrms = rootMeanSquare( inarr, polbase);
        
        //compare the rms values and remove the trend with the smallest rms
        if ((linrms < polrms)|| (Math.abs(linrms - polrms) < 5*Math.ulp(polrms))) {
//            System.out.println("linear trend removed");
            numOrder = 1;
            removePolynomialTrend(inarr, lcoefs, timestep);
        } else {
//            System.out.println("polynomial trend removed");
            numOrder = 2;
            removePolynomialTrend(inarr, pcoefs, timestep);
        }
        return numOrder;
    }
    /**
     * Converts raw trace counts to physical values by multiplying the integer
     * counts in the input array by the count conversion factor.  The result is
     * a floating point array.
     * @param inArray input integer array to be converted
     * @param countConv conversion factor to multiply by
     * @return floating (double) point array of physical values or array of 0 
     * length if input parameters are invalid
     */
    public static double[] countsToPhysicalValues(final int[] inArray, final double countConv) {
        if ((inArray == null) || (inArray.length == 0)) {
            return new double[0];
        }
        int length = inArray.length;
        double[] result = new double[length];
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] * countConv;
        }
        return result;
    }
    /**
     * Converts a floating (double) point array of one data unit type to another
     * based on the conversion factor.
     * @param inArray input array to be converted
     * @param conversion conversion factor for multiplication
     * @return a new array of converted values or array of zero length if input
     * parameters are invalid
     */
    public static double[] convertArrayUnits(final double[] inArray, final double conversion) {
        if ((inArray == null) || (inArray.length == 0)) {
            return new double[0];
        }        
        int length = inArray.length;
        double[] result = new double[length];
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] * conversion;
        }
        return result;
    }
    /**
     * Performs a 3-point smoothing on the input array, using the algorithm:
     * new val = 0.5 * current val + 0.25 * (previous val + next val)
     * @param inArray array to be smoothed, this array is not modified
     * @return new array that has been smoothed or array of zero length if input
     * parameters are invalid
     */
    public static double[] perform3PtSmoothing(final double[] inArray) {
        if ((inArray == null) || (inArray.length == 0)) {
            return new double[0];
        }
        int length = inArray.length;
        double[] result = new double[length];
        result[0] = inArray[0];
        result[1] = inArray[1];
        result[length-1] = inArray[length-1];
        for (int i = 2; i < length-1; i++) {
            result[i] = 0.5 * inArray[i] + 0.25 * (inArray[i-1] + inArray[i+1]);
        }
        return result;
    }
    /**
     * Finds the first zero crossing in the input array, working either from the
     * front or the back of the array.  The zero crossing is determined by 
     * multiplying the current value with the previous value and checking if the
     * sign is negative.  This indicates where in the array the values shift
     * from positive to negative or negative to positive.  This method stops when
     * it finds the first crossing within the specified window and returns the
     * index of the first value in the pair being tested.  If the start value is
     * smaller than the stop value, the search will begin at start and move forward
     * to the stop index.  If start is greater than stop, search will begin at
     * start and move backwards through the array to the stop index.
     * @param inArray the input array to find the zero crossing
     * @param start index in the array to start the search
     * @param stop index in the array to stop the search
     * @return the index of the zero crossing, or -1 if no crossing found, -2 if 
     * input parameters are invalid
     */
    public static int findZeroCrossing(final double[] inArray, int start, int stop) {
        int cross = -1;
        if ((inArray == null) || (inArray.length == 0) || (start < 0) || 
            (start > inArray.length) || (stop < 0) || (stop > inArray.length)
                                                            || (start == stop)) {
            return -2;
        }
        if (start < stop) {  //work from start of subsection to the end
            for (int k = start+1; k <= stop; k++) {
                if ((inArray[k] * inArray[k-1]) < 0.0) {
                    cross = k-1;
                    break;
                }
                cross = -1;
            }
        } else { //work from the end of the subsection to the start
            for (int k = start; k > stop; k--) {
                if ((inArray[k] * inArray[k-1]) < 0.0) {
                    cross = k-1;
                    break;
                }
                cross = -1;
           }
        }
        return cross;
    }
}
