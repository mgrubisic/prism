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

import java.util.Arrays;

/**
 * The ArrayStats class contains methods to calculate various statistics on an
 * array that is entered during object construction.  The constructor does the
 * computation of mean, peak value, and so on, and various getters provide
 * access to the calculated parameters.  The input array is not modified by
 * this class.  The peak value is the largest value found in the array,
 * regardless of the sign of the value.  Additional methods also calculate the
 * array histogram and determine the approximate most frequently occurring
 * value in the lower range of the array.
 * 
 * @author jmjones
 */
public class ArrayStats {
    private double[] statArray;
    private int length;
    private double total;
    private double maxhigh;
    private double maxlow;
    private double maxabs;
    private double mean;
    private int maxhighid;
    private int maxlowid;
    private int maxabsid;
    private double histstep;
    /**
     * The constructor for this class does all the computation on the array for
     * max, min, peak value, and so on.
     * 
     * @param array input array to calculate statistics on
     */
    public ArrayStats( double[] array ) {
        statArray = array;
        length = array.length;
        total = 0.0;
        maxhigh = Double.MIN_VALUE;
        maxlow = Double.MAX_VALUE;
        maxabs = Double.MIN_VALUE;
        mean = Double.MIN_VALUE;
        maxhighid = -1;
        maxlowid = -1;
        maxabsid = -1;
        histstep = 0.0;
        
        //Calculate the total value and the mean.
        //Record the highest and lowest values and their indexes
        for (int i = 0; i < length; i++) {
            total = total + statArray[i];
            if (statArray[i] > maxhigh) {
                maxhigh = statArray[i];
                maxhighid = i;
            }
            if (statArray[i] < maxlow) {
                maxlow = statArray[i];
                maxlowid = i;
            }
            mean = total / length;
        }
        //Find the peak value and its index.
        if (Math.abs(maxhigh) > Math.abs(maxlow)) {
            maxabs =  maxhigh;
            maxabsid = maxhighid;
        } else {
            maxabs =  maxlow;
            maxabsid = maxlowid;
        }        
    }
    /**
     * Getter for the calculated mean of the array
     * 
     * @return the mean value
     */
    public double getMean() {
        return mean;
    }
    /**
     * Getter for the calculated peak value, which is the largest + or - value
     * found in the array
     * 
     * @return the peak value
     */
    public double getPeakVal() {
        return maxabs;
    }
    /**
     * Getter for the index of the peak value
     * 
     * @return array index for the peak value
     */
    public int getPeakValIndex() {
        return maxabsid;
    }
    /**
     * Getter for the minimum array value
     * 
     * @return minimum array value
     */
    public double getMinVal() {
        return maxlow;
    }
    /**
     * Getter for the maximum array value
     * 
     * @return maximum array value
     */
    public double getMaxVal() {
        return maxhigh;
    }
    /**
     * Getter for the interval size calculated for the histogram, which is
     * (maxval - minval) / numIntervals
     * 
     * @return the calculated histogram interval
     */
    public double getHistogramInterval() {
        return histstep;
    }
    /**
     * Builds a histogram from the array that was given in the constructor call.
     * 
     * @param numIntervals the number of intervals to use in the histogram
     * @return array holding the counts of values that fell within each
     * interval of the histogram
     */
    public int[] makeHistogram(int numIntervals) {
        //THis can be made faster by smarter searching for the correct bin.
        //create the array for the histogram and fill with 0s
        double HIST_EPSILON = 0.0001;
        int[] hist = new int[numIntervals];
        Arrays.fill(hist, 0);
        //determine the range of values and the width of each bin
        double range = maxhigh - maxlow;
        histstep = range / numIntervals;
        //Go through the array, find which bin the current value belongs to, and
        //increment that bin.
        int index;
        double lowvalue;
        double highvalue;
        for (double val : statArray) {
            for (int i = 0; i < numIntervals; i++ ) {
                //Catch the highest value since the upper test for a value in
                //a bin is exclusive while the lower test is inclusive.
                if (Math.abs(maxhigh - val) < HIST_EPSILON) {
                    hist[numIntervals-1] += 1;
                    break;
                }
                lowvalue = maxlow + i * histstep;
                highvalue = maxlow + (i + 1) * histstep;
                if ((val > lowvalue) || (Math.abs(val - lowvalue) < HIST_EPSILON)) {
                    if (val < highvalue) {
                        hist[i] += 1;
                        break;
                    }
                }
            }
        }
//        System.out.println("+++ hist maxhigh: " + maxhigh + " maxlow: " + maxlow);
//        for (int i = 0; i < hist.length; i++) {
//            if (hist[i] > 0) {
//                System.out.println("+++ hist low: " + (maxlow + i*histstep) + " hist high: " + (maxlow + (i+1)*histstep));
//                System.out.println("+++ hist: " + hist[i] + " for index: " + i);
//            }
//        }
        return hist;
    }
    /**
     * Find the most frequently occurring value in the lower range of array values.
     * This is done by making a histogram of the array values and, looking only
     * at the lower half of the histogram, find the bin with the highest count.
     * This bin corresponds to a range of values in the array, and the lower end
     * value of the range is calculated and returned as the modal minimum.  The
     * number of bins used in the estimate is internally set at 100.
     * 
     * @return the approximate array value representing the most frequently
     * seen low range value.
     */
    public double getModalMinimum() {
        double modalMin = Double.MIN_VALUE;
        int NUM_BINS = 200;
        int[] hist;
        int mode = 0;
        int modeindex = -1;
        
        //Find the bin in the first half of the histogram with the highest
        //count.  This is the modal value for the minimum.
        hist = makeHistogram( NUM_BINS );
        
        int startbin = 0;
        int stopbin = NUM_BINS;
        for (int i = 0; i < NUM_BINS; i++) {
            if (hist[i] > 0) {
                startbin = i;
                break;
            }
        }
        for (int i = hist.length-1;  i <= 0; i-- ) {
            if (hist[i] > 0) {
                stopbin = i;
                break;
            }
        }
        for (int i = startbin; i < (stopbin-startbin)/2; i++) {
            if (hist[i] >= mode) {
                mode = hist[i];
                modeindex = i;
            }
        }
//        System.out.println("+++ found mode at index: " + modeindex);
//
        //returns the center point value of the range
        modalMin = maxlow + histstep * 0.5;
        
        return modalMin;
    }
}

