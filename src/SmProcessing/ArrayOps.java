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
     * Removes the given mean value from each entry in the array.
     * This method does not calculate the mean internally in order
     * to handle the case where the mean is pre-calculated over a subset of the
     * array (pre-event) and then removed from the whole array.
     * 
     * @param array input array to have mean removed from
     * @param mean pre-calculated mean to remove from the array
     */
    public static void removeMean( double[] array, double mean ) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] - mean;
        }
    }
    /**
     * Removes a linear trend (mx + b) from the input array with the given 
     * timestep.  The linear trend is calculated internally using apache commons
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
        //Remove the trend from the array
        for (int i = 0; i < len; i++) {
            array [i] = array[i] - regression.predict(time[i]);
        }
    }
    /**
     * Removes a linear trend from an array, where the linear trend is calculated
     * from another array.  This is used when a portion of the original array
     * is identified for determining the linear trend, and then once the trend
     * is calculated it is applied to the entire array.
     * 
     * @param array input array to have the linear trend removed from
     * @param subarray input array to use to calculate the linear trend. This
     * array is not modified in the process.
     * @param timestep the sample interval of the input array
     */
    public static void removeLinearTrendFromSubArray( double[] array, 
                                        double[] subarray, double timestep ) {
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
            array [i] = array[i] - regression.predict(time[i]);
        }
    }
    /**
     * Make an array of time values, starting at 0 and incrementing by timestep
     * to make an array of length timelen.
     * 
     * @param timestep sample interval
     * @param timelen length of desired array
     * @return an array of times, starting at 0
     */
    public static double[] makeTimeArray( double timestep, int timelen) {
        double[] time = new double[timelen];
        for (int i = 0; i < timelen; i++) {
            time[i] = i * timestep;
        }
        return time;
    }
    /**
     * Calculates the approximate of the input array using the trapezoidal 
     * method.  The spacing between each point is dt
     * 
     * @param array array to be integrated
     * @param dt the time step
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
     * @param dt the time step
     * @return new array containing the approximate derivative of the input points
     */
    public static double[] Differentiate( double[] array, double dt) {
        if ((dt - 0.0) < OPS_EPSILON) {
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
     * Removes a polynomial trend of specified degree from the input array.  The
     * polynomial trend is calculated with the apache commons math 
     * PolynomialCurveFitter class, and the input array is modified by subtracting
     * the calculated value of the polynomial at each time step.
     * 
     * @param array input array containing data with a polynomial trend.  This
     * array is modified during execution.
     * @param degree polynomial degree to calculate, such as 2 or 3.
     * @param timestep sample interval
     */
    public static void removePolynomialTrend(double[] array, int degree, double timestep) {
        int len = array.length;
        double value;
        double[] time = makeTimeArray( timestep, len);
        ArrayList<WeightedObservedPoint> points = new ArrayList<>();
        for (int i = 0; i < len; i++ ){
            points.add(new WeightedObservedPoint( 1.0, time[i], array[i]));
        }
        
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        double[] coefs = fitter.fit(points);
        
//        for (double each : coefs) {
//            System.out.println("poly coef: " + each);
//        }
        //Remove the calculated polynomial trend from the array
        for (int i = 0; i < len; i++) {
            value = 0.0;
            for (int k = 0; k < coefs.length; k++) {
                value = value + coefs[k]*Math.pow(i,k);
            }
            array[i] = array[i] - value;
        }
    }
}
