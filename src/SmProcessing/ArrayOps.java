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

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author jmjones
 */
public class ArrayOps {
    private ArrayOps() {
    }
    public static void removeMean( double[] array, double mean ) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] - mean;
        }
    }
    public static void removeLinearTrend( double[] array, double timestep ) {
        int len = array.length;
        double[] time = makeTimeArray( timestep, 0, len);
        SimpleRegression regression = new SimpleRegression();
        for(int i = 0; i < len; i++) {
            regression.addData(time[i], array[i]);
        }
        System.out.println("trend slope: " + regression.getSlope());
        System.out.println("trend intercept: " + regression.getIntercept());
        
        //Remove the trend from the array
        for (int i = 0; i < len; i++) {
            array [i] = array[i] - regression.predict(time[i]);
        }
    }
    public static double[] makeTimeArray( double timestep, int start, int stop) {
        int timelen = stop - start;
        double[] time = new double[timelen];
        for (int i = 0; i < timelen; i++) {
            time[i] = i * timestep;
        }
        return time;
    }
    public static double[] Integrate( double[] array, double dt ) {
        int len = array.length;
        double[] calc = new double[len];
        double dt2 = dt / 2.0;
        calc[0] = 0.0;
        for (int i = 1; i < len; i++) {
            calc[i] = calc[i-1] + (array[i-1] + array[i])*dt2;
        }
        return calc;
    }
    public static double[] Differentiate( double[] array, double dt) {
        int len = array.length;
        double[] calc = new double[ len ];
        calc[0] = (array[1] - array[0]) *2.0;
        for (int i = 1; i < len-2; i++) {
            calc[i] = array[i+1] - array[i-1];
        }
        calc[len-1] = (array[len-1] - array[len-2]) * 2.0;
        
        for (int i = 0; i < len; i++) {
            calc[i] = calc[i] / (dt + dt);
        }
        return calc;
    }
}
