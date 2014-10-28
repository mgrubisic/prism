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

import java.util.ArrayList;
import java.util.Arrays;
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
 * ArrayOps.removeMean(array, mean).
 * 
 * The linear and polynomial trend methods utilize the apache commons math 
 * package.  http://commons.apache.org/proper/commons-math/
 * 
 * @author jmjones
 */
public class ArrayOps {
    private static final double OPS_EPSILON = 0.00001;
    /**
     * Default constructor
     */
    private ArrayOps() {
    }
    /**
     * Removes the given value from each entry in the array.
     * This method allows for the removal of a mean that was calculated on
     * a subset of another array, or removal of the median.
     * 
     * @param array input array to have mean removed from
     * @param val pre-calculated value to remove from the array
     */
    public static void removeValue( double[] array, double val ) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] - val;
        }
    }
    /**
     * Removes a linear trend (mx + b) from the input array with the given 
     * time step.  The linear trend is calculated internally using apache commons
     * math simple regression class.
     * 
     * @param array input array to have linear trend removed from
     * @param timestep the sample interval of the input array
     */
    public static void removeLinearTrend( double[] array, double timestep ) {
        int len = array.length;
        double[] time = makeTimeArray( timestep, len);
        SimpleRegression regression = new SimpleRegression();
        for(int i = 0; i < len; i++) {
            regression.addData(time[i], array[i]);
        }
//        System.out.println("+++ slopeA: " + regression.getSlope());
//        System.out.println("+++ interceptA: " + regression.getIntercept());
//        System.out.println("+++ mean sq. errorA: " + regression.getMeanSquareError());
        //Remove the trend from the array
        for (int i = 0; i < len; i++) {
            array [i] = array[i] - regression.predict(time[i]);
        }
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
     */
    public static void removeLinearTrendFromSubArray( double[] array, 
                                        double[] subarray, double timestep ) {
        int lenfull = array.length;
        int lensub = subarray.length;
//        System.out.println("+++ array length: " + lenfull + " subarray length: " + lensub);
        double[] time = makeTimeArray( timestep, lensub);
        SimpleRegression regression = new SimpleRegression();
        for(int i = 0; i < lensub; i++) {
            regression.addData(time[i], subarray[i]);
        }
//        System.out.println("+++ slopeB: " + regression.getSlope());
//        System.out.println("+++ interceptB: " + regression.getIntercept());
//        System.out.println("+++ mean sq. errorB: " + regression.getMeanSquareError());
        //Remove the trend from the first array
        double[] fulltime = makeTimeArray( timestep, lenfull);
        for (int i = 0; i < lenfull; i++) {
            array [i] = array[i] - regression.predict(fulltime[i]);
        }
    }
    /**
     * Make an array of time values, starting at 0 and incrementing by time step
     * to make an array of length arraylen.
     * 
     * @param timestep sample interval
     * @param arraylen length of desired array
     * @return an array of times, starting at 0
     */
    public static double[] makeTimeArray( double timestep, int arraylen) {
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
     * @return mean of the values in the sub-array
     */
    public static double findSubsetMean( double[] array, int start, int end) {
        double[] subset = Arrays.copyOfRange( array, start, end );
        ArrayStats accsub = new ArrayStats( subset );
        return accsub.getMean();
    }
    /**
     * Calculates the approximate integral of the input array using the trapezoidal 
     * method.  The spacing between each point is dt
     * 
     * @param array array to be integrated
     * @param dt the time step in seconds
     * @return new array containing the approximate integral of the input points
     */
    public static double[] Integrate( double[] array, double dt ) {
        int len = array.length;
        double[] calc = new double[len];
        double dt2 = dt / 2.0;
        calc[0] = 0.0;
        for (int i = 1; i < len; i++) {
            calc[i] = calc[i-1] + (array[i-1] + array[i])*dt2;
        }
        return calc;
    }/**
     * Calculates the approximate derivative of the input array.
     * 
     * @param array array to be differentiated
     * @param dt the time step in seconds
     * @return new array containing the approximate derivative of the input points
     */
    public static double[] Differentiate( double[] array, double dt) {
        if (Math.abs(dt - 0.0) < OPS_EPSILON) {
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
     * @return coefficients
     */
    public static double[] findPolynomialTrend(double[] array, int degree, double timestep) {
        int len = array.length;
        double value;
        double[] time = makeTimeArray( timestep, len);
        ArrayList<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = 0; i < len; i++ ){
            points.add(new WeightedObservedPoint( 1.0, time[i], array[i]));
        }
        
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        double[] coefs = fitter.fit(points);
        return coefs;
    }
    
    public static void removePolynomialTrend(double[] array, double[] coefs, 
                                                            double timestep) {
        int len = array.length;
        double value;
        double[] time = makeTimeArray( timestep, len);
        for (int i = 0; i < len; i++) {
            value = 0.0;
            for (int k = 0; k < coefs.length; k++) {
                value = value + coefs[k]*Math.pow(time[i],k);
            }
            array[i] = array[i] - value;
        }
    }
    /**
     * Calculates the root mean square (rms) value for the input array
     * @param array array to calculate rms for
     * @return the rms value
     */
    public static double rootMeanSquare( double[] array ) {
        double rms = 0.0;
        for (double each : array) {
            rms += Math.pow(each, 2);
        }
        return Math.sqrt(rms / array.length);
    }
    /**
     * Converts raw trace counts to physical values by multiplying the integer
     * counts in the input array by the count conversion factor.  The result is
     * a floating point array.
     * @param inArray input integer array to be converted
     * @param countConv conversion factor to multiply by
     * @return floating (double) point array of physical values
     */
    public static double[] countsToPhysicalValues(final int[] inArray, final double countConv) {
        
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
     * @return a new array of converted values
     */
    public static double[] convertArrayUnits(final double[] inArray, final double conversion) {
        
        int length = inArray.length;
        double[] result = new double[length];
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] * conversion;
        }
        return result;
    }
    public static double[] perform3PtSmoothing(final double[] inArray) {
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
    public static int findZeroCrossing(final double[] inArray, int start, int flag) {
        int cross = -1;
        if (flag == 0) {  //work from start of subsection to the end
            for (int k = 1; k < start; k++) {
                if ((inArray[k] * inArray[k-1]) < 0.0) {
                    cross = k-1;
                    break;
                }
                cross = 0;
            }
        } else { //work from the end of the subsection forward
            for (int k = start-1; k > 0; k--) {
                if ((inArray[k] * inArray[k-1]) < 0.0) {
                    cross = k-1;
                    break;
                }
                cross = start;
           }
        }
        return cross;
    }
}
