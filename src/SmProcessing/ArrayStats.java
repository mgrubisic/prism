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
 *
 * @author jmjones
 */
public class ArrayStats {
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
    
    public ArrayStats( double[] array ) {
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
            total = total + array[i];
            if (array[i] > maxhigh) {
                maxhigh = array[i];
                maxhighid = i;
            }
            if (array[i] < maxlow) {
                maxlow = array[i];
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
    public double getMean() {
        return mean;
    }
    public double getPeakVal() {
        return maxabs;
    }
    public int getPeakValIndex() {
        return maxabsid;
    }
    public double getMinVal() {
        return maxlow;
    }
    public double getMaxVal() {
        return maxhigh;
    }
    public double getHistogramInterval() {
        return histstep;
    }
    public int[] makeHistogram(double[] array, int numIntervals) {
        //create the array for the histogram and fill with 0s
        int[] hist = new int[numIntervals];
        Arrays.fill(hist, 0);
        //determine the range of values and the width of each bin
        double range = maxhigh - maxlow;
        histstep = range / numIntervals;
        System.out.format("+++ maxhigh: %f  maxlow: %f%n", maxhigh, maxlow);
        System.out.format("+++ range: %f  step: %f%n", range, histstep);
        //Go through the array, find which bin the current value belongs to, and
        //increment that bin.
        int index;
        for (double val : array) {
            index = (int)Math.ceil(Math.abs(val-maxlow)/histstep);
            if (index >= numIntervals) {
                hist[index-1] += 1;
                System.out.format("+++ index: %d  val: %f%n", index, val);
            } else {
                hist[index] += 1;
            }
        }
//        for (int i = 0; i < hist.length; i++) {
//            if (hist[i] > 0) {
//                System.out.println("+++ hist: " + hist[i] + " for index: " + i);
//            }
//        }
        return hist;
    }
    //Find the most frequently occurring value in the lower range of array values.
    //This is done by making a histogram of the array values and, looking only
    //at the lower half of the histogram, find the bin with the highest count.
    //The minimum array value for this histogram bin is computed and returned.
    public double getModalMinimum(double[] array) {
        double modalMin = Double.MIN_VALUE;
        int NUM_BINS = 100;
        int[] hist;
        int mode = 0;
        int modeindex = -1;
        
        //Find the bin in the first half of the histogram with the highest
        //count.  This is the modal value for the minimum.
        hist = makeHistogram( array, NUM_BINS);
        for (int i = 0; i < NUM_BINS/2; i++) {
            if (hist[i] >= mode) {
                mode = hist[i];
                modeindex = i;
            }
        }
        System.out.println("+++ found mode at index: " + modeindex);
        //!!!check this to make sure that the low end of the interval is getting
        //picked up and not the high end
        modalMin = maxlow + histstep * modeindex;
        
        return modalMin;
    }
}
