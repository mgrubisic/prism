/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 1999 Anthony Lomax <lomax@faille.unice.fr>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.alomax.math;

//import JSci.maths.LinearMath;
//import JSci.maths.polynomials.RealPolynomial;
import net.alomax.freq.*;
import net.alomax.timedom.*;
import net.alomax.util.PhysicalUnits;

/**
 * float data TimeSeries object
 */
/**
 * A class representing a float data time series.
 *
 * @author Anthony Lomax
 * @version %I%, %G%
 */
public class TimeSeriesFloat extends TimeSeries {

    public static final float RANGE_MIN = -Float.MAX_VALUE;
    public static final float RANGE_MAX = Float.MAX_VALUE;
    public static final float NO_DATA = RANGE_MIN;
    public Cmplx[] cdataLocal = new Cmplx[0];
    //public static TimeSeriesFloat cdataStaticOwner = null;
    public float outOfRangeMin = RANGE_MIN;
    public float outOfRangeMax = RANGE_MAX;
    public boolean sampleChanged = true;
    /**
     * float array to contain the time series samples
     */
    public float[] sample = new float[0];

    /**
     * Empty constructor
     */
    public TimeSeriesFloat() {

        super();

    }

    /**
     * Copy Constructor
     */
    public TimeSeriesFloat(TimeSeriesFloat timeSeries) {

        this(timeSeries.sample, timeSeries.sampleInt, timeSeries.lagTime, timeSeries.ampUnits, timeSeries.timeUnits);
        // copy samples
        float[] timeSeriesSampleCopy = new float[sample.length];
        System.arraycopy(sample, 0, timeSeriesSampleCopy, 0, sample.length);
        sample = timeSeriesSampleCopy;

    }

    /**
     * Complete constructor
     */
    public TimeSeriesFloat(float[] sampleFloat, double sampleInt, double lagTime, String ampUnits, String timeUnits) {

        super(sampleInt, lagTime, ampUnits, timeUnits);

        sample = sampleFloat;
        sampleChanged = true;

        // find min and max amplitude
        // 20060608 calculateAndSetAmplitudeRange(true);
        calculateAndSetAmplitudeRange(false);

        // calculate mean
        //calculateAndSetMean();

    }

    /**
     * Sets a float array as the samples in this time series.
     *
     */
    public final void setSampleFloat(float[] newSample) {

        sample = newSample;
        sampleChanged = true;

        // find min and max amplitude
        // 20060608 calculateAndSetAmplitudeRange(true);
        calculateAndSetAmplitudeRange(false);


    }

    /**
     * Appends a float array to the samples in this time series.
     *
     */
    public final int appendFloat(int discardToIndex, float[] appendArray, int startIndexBeforeDiscard) {

        // create and load new float array
        int newLength = startIndexBeforeDiscard + appendArray.length - discardToIndex;
        //System.out.println("newLength, startIndexBeforeDiscard, appendArray.length, discardToIndex " + newLength+" "+startIndexBeforeDiscard+" "+appendArray.length+" "+discardToIndex);
        float newArray[] = new float[newLength];
        System.arraycopy(sample, discardToIndex, newArray, 0, startIndexBeforeDiscard - discardToIndex);
        System.arraycopy(appendArray, 0, newArray, startIndexBeforeDiscard - discardToIndex, appendArray.length);
        sample = newArray;

        sampleChanged = true;

        //  lagTime ???

        // find min and max amplitude
        // 20060608 calculateAndSetAmplitudeRange(true);
        calculateAndSetAmplitudeRange(false);

        // calculate mean
        //calculateAndSetMean();

        return (appendArray.length);

    }

    /**
     * Returns a new float array that is a subset of the samples in this time series.
     */
    public final float[] getSampleSubsetAsFloat(int startIndex, int endIndex) {

        // create and load new float array
        float newArray[] = new float[endIndex - startIndex + 1];

        int sampleStart = startIndex;
        int sampleEnd = endIndex;
        int newArrayStart = 0;

        // outside of time series array
        if (endIndex < 0 || startIndex >= sample.length) {
            for (int i = 0; i < newArray.length; i++) {
                newArray[i] = 0.0f;
            }
            return (newArray);
        }

        // pad beginning with 0s
        if (startIndex < 0) {
            float fillArray[] = new float[-startIndex];
            for (int i = 0; i < fillArray.length; i++) {
                fillArray[i] = 0.0f;
            }
            System.arraycopy(fillArray, 0, newArray, 0, fillArray.length);
            sampleStart = 0;
            newArrayStart = fillArray.length;
        }

        // pad end with 0s
        if (endIndex >= sample.length) {
            float fillArray[] = new float[endIndex - sample.length + 1];
            sampleEnd = sample.length - 1;
            for (int i = 0; i < fillArray.length; i++) {
                fillArray[i] = 0.0f;
            }
            System.arraycopy(fillArray, 0, newArray,
                    newArrayStart + sampleEnd - sampleStart + 1, fillArray.length);
        }

        System.arraycopy(sample, sampleStart, newArray, newArrayStart,
                sampleEnd - sampleStart + 1);

        return (newArray);

    }

    /**
     * Calculates and returns the variance of the time series samples.
     */
    public final double calculateVariance() {

        double sumValue = 0.0;
        double sumValueSquare = 0.0;
        int nsum = 0;

        double value;
        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            if (sample[nsamp] > outOfRangeMin && sample[nsamp] < outOfRangeMax) {
                value = sample[nsamp];
                sumValue += value;
                sumValueSquare += value * value;
                nsum++;
            }
        }

        double variance = 0.0;
        if (nsum > 0) {
            double mean = sumValue / (double) nsum;
            variance = (sumValueSquare - sumValue * mean) / (double) nsum;
        }

        return (variance);

    }

    /**
     * Calculates and returns the mean of the time series samples.
     */
    public final double calculateMean() {

        double sumValue = 0.0;
        int nsum = 0;

        double value;
        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            if (sample[nsamp] > outOfRangeMin && sample[nsamp] < outOfRangeMax) {
                value = sample[nsamp];
                sumValue += value;
                nsum++;
            }
        }

        double mean = 0.0;
        if (nsum > 0) {
            mean = sumValue / (double) nsum;
        }

        return (mean);

    }

    /**
     * Calculates and returns the rms amplitude in the time series samples.
     *
     */
    public final double calculateRmsAmplitude() {

        return (calculateRmsAmplitude(0, sample.length));

    }

    /**
     * Calculates and returns the rms amplitude of a subset of the samples in this time series.
     */
    public final double calculateRmsAmplitude(int startIndex, int endIndex) {

        double rms = 0.0;
        double value = 0.0;
        int nsum = 0;
        for (int nsamp = startIndex; nsamp < endIndex; nsamp++) {
            if (sample[nsamp] > outOfRangeMin && sample[nsamp] < outOfRangeMax) {
                value = sample[nsamp];
                rms += value * value;
                nsum++;
            }
        }

        if (nsum > 0) {
            rms /= (double) nsum;
            rms = Math.sqrt(rms);
        }

        return (rms);

    }

    /**
     * Calculates and returns the minimum and maximum amplitude in a subset of the time series samples.
     *
     */
    public final RangeDouble calculateAmplitudeRange(int indexMin, int indexMax, boolean setOutOfRangeValuesToZero) {

        RangeFloat range = new RangeFloat(Float.MAX_VALUE, -Float.MAX_VALUE);
        for (int nsamp = indexMin; nsamp < indexMax; nsamp++) {
            if (sample[nsamp] > outOfRangeMin && sample[nsamp] < outOfRangeMax) {
                range.min = Math.min(sample[nsamp], range.min);
                range.max = Math.max(sample[nsamp], range.max);
            } else if (setOutOfRangeValuesToZero) {
                sample[nsamp] = 0.0f;
                sampleChanged = true;
            }
        }

        RangeDouble rangeDouble = new RangeDouble((double) range.min, (double) range.max);
        return (rangeDouble);

    }

    /**
     * Calculates and sets the minimum and maximum amplitude in the complete time series. Sets ampMin and ampMax.
     *
     */
    public final void calculateAndSetAmplitudeRange(boolean setOutOfRangeValuesToZero) {

        RangeDouble range = calculateAmplitudeRange(0, sample.length, setOutOfRangeValuesToZero);
        if (range.min < range.max) {
            ampMin = range.min;
            ampMax = range.max;
        } else {
            ampMin = range.min - 0.5;
            ampMax = range.max + 0.5;
        }

    }

    /**
     * Returns the samples as a float array.
     *
     */
    public final float[] getSampleAsFloat() {
        sampleChanged = true;
        return (sample);
    }

    /**
     * Returns the time series sample length
     *
     * @return the number of samples.
     */
    public final int sampleLength() {
        return (sample.length);
    }

    /**
     * Returns the amount of memory required for data array
     *
     * @return the memory size required for storing the data, measured in bytes.
     */
    public long getDataMemorySize() {

        if (sample == null) {
            return (0);
        }

        return (sample.length * 4);

    }

    /**
     * Returns a time series sample value.
     *
     * called often in loops, so final
     */
    public final double sampleAt(int index) {

        // index out of range
        if (index < 0 || index > sample.length) {
            return (INVALID_AMPLITUDE);
        }

        // sample value invalid
        if (sample[index] == NO_DATA) {
            return (INVALID_AMPLITUDE);
        }

        return ((double) sample[index]);
    }

    /**
     * Inverts the polarity of the time series samples.
     *
     */
    public final void invertPolarity() {

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            if (sample[nsamp] > outOfRangeMin && sample[nsamp] < outOfRangeMax) {
                sample[nsamp] *= -1.0f;
            }
        }

        sampleChanged = true;

        //sampleMean *= -1.0;
        lastSampleMean *= -1.0;

        double atemp = ampMin;
        ampMin = -1.0 * ampMax;
        ampMax = -1.0 * atemp;
    }

    //    /** Calculates and sets the mean of the time series samples.
    //     *   Sets sampleMean.
    //     *
    //     */
/*
     public final void calculateAndSetMean() {

     double mean = 0.0;

     for (int nsamp = 0; nsamp < sample.length; nsamp++)
     mean += (double) sample[nsamp];

     if (sample.length > 0) {
     sampleMean = mean / (double) sample.length;
     lastSampleMean = INVALID_AMPLITUDE;
     }
     }
     **/
    /**
     * A least squares curve fit of a straight line to the data is calculated. This straight line or trend is then "subtracted" from the data.
     *
     */
//    public final void removeTrend() {
//
//        // get least squares curve fit
//
//        /**
//         * Fits an nth degree polynomial to data using the method of least squares.
//         *
//         * @param n the degree of the polynomial (>= 0).
//         * @param data [0][] contains the x-series, [1][] contains the y-series.
//         */
//        int n = 1;
//        double[][] data = new double[2][sample.length];
//        double xvalue = 0.0;
//        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
//            float value = sample[nsamp];
//            if (value < outOfRangeMin || value > outOfRangeMax) {
//                value = 0.0f;
//            }
//            data[0][nsamp] = xvalue;
//            data[1][nsamp] = value;
//            xvalue += sampleInt;
//        }
//        RealPolynomial realPolynomial = LinearMath.leastSquaresFit(n, data);
//        // y = b + m*x
//        double b_coeff = realPolynomial.getCoefficientAsDouble(0);
//        double m_coeff = realPolynomial.getCoefficientAsDouble(1);
//
//
//        // subtract line from data
//
//        sampleChanged = true;
//
//        ampMin = Float.MAX_VALUE;
//        ampMax = -Float.MAX_VALUE;
//
//        xvalue = 0.0;
//        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
//            float value = sample[nsamp];
//            if (value > outOfRangeMin && value < outOfRangeMax) {
//                value = value - (float) (b_coeff + m_coeff * xvalue);
//                sample[nsamp] = value;
//                ampMin = Math.min(value, ampMin);
//                ampMax = Math.max(value, ampMax);
//            }
//            xvalue += sampleInt;
//        }
//
//        if (sample.length > 0) {
//            //sampleMean = (double) (mean / (float) sample.length);
//            lastSampleMean = INVALID_AMPLITUDE;
//        }
//
//    }

    /**
     * Removes the mean of the time series samples.
     *
     */
    public final void removeMean() {

        removeMean(sample.length);
        sampleChanged = true;

    }

    /**
     * Removes the mean of the time series samples. updates ampMin, ampMax.
     *
     * @param nPointsFromBegin the number of points from beginning of time series to use to calculate mean.
     *
     * @see	sampleMean
     */
    public void removeMean(int nPointsFromBegin) {

        removeMean(0, nPointsFromBegin - 1);
        sampleChanged = true;

    }

    /**
     * Removes the mean of the time series samples. updates ampMin, ampMax.
     *
     * @param nStart the start index of window to use to calculate mean.
     * @param nEnd the end index of window to use to calculate mean.
     *
     */
    public void removeMean(int nStart, int nEnd) {

        //System.out.println("removeMean " + nStart + "->" + nEnd);

        int indexStart = nStart;
        if (indexStart < 0) {
            indexStart = 0;
        }
        int indexEnd = nEnd + 1;
        if (indexEnd > sample.length) {
            indexEnd = sample.length;
        }

        double sampleMean = 0.0;
        int npointsMean = 0;
        for (int nsamp = indexStart; nsamp < indexEnd; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                sampleMean += (double) value;
                npointsMean++;
            }
        }

        if (npointsMean < 1) {
            return;
        }

        sampleMean = sampleMean / (double) npointsMean;
        lastSampleMean = INVALID_AMPLITUDE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                sample[nsamp] -= (float) sampleMean;
            }
        }

        sampleChanged = true;


        ampMin -= sampleMean;
        ampMax -= sampleMean;

        lastSampleMean = sampleMean;
        //sampleMean = 0.0;

    }

    /**
     * Undo the last remove mean.
     *
     */
    public final void unDoRemoveMean() {

        if (lastSampleMean == INVALID_AMPLITUDE) {
            return;
        }

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                sample[nsamp] += (float) lastSampleMean;
            }
        }

        sampleChanged = true;

        ampMin += lastSampleMean;
        ampMax += lastSampleMean;

        //sampleMean = lastSampleMean;
        lastSampleMean = INVALID_AMPLITUDE;

    }

    /**
     * Applies a cosine taper to each end of the time series samples.
     *
     * @param taperPercent the amount of taper as percent of time series length
     *
     */
    public final void cosineTaper(double taperPercent) {

        int taperLen = (int) ((taperPercent / 100.0) * (double) sample.length);
        if (taperLen < 2 || taperLen > sample.length / 2) {
            return;
        }

        double omega = Math.PI / (double) taperLen;

        double taper;
        int ibeg, iend;
        for (int i = 1; i < taperLen; i++) {
            ibeg = i - 1;
            iend = sample.length - i;
            taper = 0.5 * (1.0 - Math.cos(omega * i));
            sample[ibeg] *= taper;
            sample[iend] *= taper;
        }

        sampleChanged = true;

    }

    /**
     * Cuts time range of time series from time1 to time2.
     *
     */
    public final void cutInMemory(int startIndex, int endIndex) {

        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex > sample.length - 1) {
            endIndex = sample.length - 1;
        }

        float[] cutSample = getSampleSubsetAsFloat(startIndex, endIndex);

        sample = cutSample;

        sampleChanged = true;

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                ampMin = Math.min(value, ampMin);
                ampMax = Math.max(value, ampMax);
            }
        }

        if (sample.length > 0) {
            //sampleMean = (double) (mean / (float) sample.length);
            lastSampleMean = INVALID_AMPLITUDE;
        }

    }

    /**
     * Applies test processing to the time series samples.
     *
     */
    public final void test() {

        int nDegree = 21;

        int nAve = 5;
        double afact = (double) (nAve - 1) / (double) nAve;
        double bfact = 1.0 / (double) nAve;

        double[] coeff = new double[nDegree];
        coeff[0] = 1.0;
        for (int i = 1; i < nDegree; i++) {
            coeff[i] = coeff[i - 1] / (double) i;
        }

        double[] derivLast = new double[nDegree];
        for (int i = 0; i < nDegree; i++) {
            derivLast[i] = 0.0;
        }

        double[] derivNew = new double[nDegree];

        //double lastValue = sample[0];

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        double predNextValue = 0.0;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {

            float value = sample[nsamp];
            if (value <= outOfRangeMin || value >= outOfRangeMax) {
                value = 0.0f;
            }

            sample[nsamp] = value - (float) predNextValue;
            ampMin = Math.min(sample[nsamp], ampMin);
            ampMax = Math.max(sample[nsamp], ampMax);

            //derivNew[0] = value;
            derivNew[0] = afact * derivLast[0] + bfact * value;
            predNextValue = derivNew[0];
            for (int i = 1; i < nDegree; i++) {
                derivNew[i] = derivNew[i - 1] - derivLast[i - 1];
                //derivNew[i] = ((double) i / (double) (i + 1)) * derivLast[i] + (1.0 / (double) (i + 1)) * (derivNew[i - 1] - derivLast[i - 1]);
                predNextValue += coeff[i] * derivNew[i] * (double) nAve;
            }

            for (int i = 0; i < nDegree; i++) {
                derivLast[i] = derivNew[i];
            }

        }

        if (sample.length > 0) {
            //sampleMean = (double) (mean / (float) sample.length);
            lastSampleMean = INVALID_AMPLITUDE;
        }


        sampleChanged = true;


    }

    /**
     * Applies time-domain integration to the time series samples.
     *
     */
    public final void integrate(float startValue) {

        float sum = startValue;

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value <= outOfRangeMin || value >= outOfRangeMax) {
                value = 0.0f;
            }
            sum += value * (float) sampleInt;
            sample[nsamp] = sum;
            ampMin = Math.min(sum, ampMin);
            ampMax = Math.max(sum, ampMax);
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        ampUnits = PhysicalUnits.timeIntegral(ampUnits);


    }

    /**
     * Applies time-domain integration of maximum of positive and negatige samples to the time series samples.
     *
     * Integrates abs val of positive and negative values separately, loads case with maximum integral into samples
     *
     * @param peakOnly if true, then only returns integral of largest abs value of positive or negative peak
     *
     * @return the time window of integration for the maximum positive or negative value
     *
     */
    public final double integrateMaxOfPosNeg(boolean peakOnly) {

        //peakOnly
        boolean in_pos = false;
        int start_pos = 0;
        int first_index_pos = 0;
        int last_index_pos = 0;
        float pos_sum_max = -Float.MAX_VALUE;
        //
        boolean in_neg = false;
        int start_neg = 0;
        int first_index_neg = 0;
        int last_index_neg = 0;
        float neg_sum_max = -Float.MAX_VALUE;


        float pos_ampMin = Float.MAX_VALUE;
        float pos_ampMax = -Float.MAX_VALUE;
        float neg_ampMin = Float.MAX_VALUE;
        float neg_ampMax = -Float.MAX_VALUE;

        float pos_sum = 0.0f;
        float neg_sum = 0.0f;
        int nsamp = 0;
        for (; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value <= outOfRangeMin || value >= outOfRangeMax) {
                value = 0.0f;
            }
            if (value >= 0.0) {
                if (peakOnly) {
                    if (in_neg) {
                        in_neg = false;
                        if (neg_sum > neg_sum_max) {
                            neg_sum_max = neg_sum;
                            first_index_neg = start_neg;
                            last_index_neg = nsamp > 0 ? nsamp - 1 : nsamp;
                        }
                        neg_sum = 0.0f;
                    }
                    if (!in_pos) {
                        in_pos = true;
                        start_pos = nsamp;
                    }
                }
                pos_sum += value * (float) sampleInt;
                pos_ampMin = Math.min(pos_sum, pos_ampMin);
                pos_ampMax = Math.max(pos_sum, pos_ampMax);
            } else {
                if (peakOnly) {
                    if (in_pos) {
                        in_pos = false;
                        if (pos_sum > pos_sum_max) {
                            pos_sum_max = pos_sum;
                            first_index_pos = start_pos;
                            last_index_pos = nsamp > 0 ? nsamp - 1 : nsamp;
                        }
                        pos_sum = 0.0f;
                    }
                    if (!in_neg) {
                        in_neg = true;
                        start_neg = nsamp;
                    }
                }
                neg_sum += -value * (float) sampleInt;
                neg_ampMin = Math.min(neg_sum, neg_ampMin);
                neg_ampMax = Math.max(neg_sum, neg_ampMax);
            }
        }

        // check again for unterminated peakOnly max sum
        if (peakOnly) {
            if (in_neg) {
                if (neg_sum > neg_sum_max) {
                    neg_sum_max = neg_sum;
                    first_index_neg = start_neg;
                    last_index_neg = nsamp > 0 ? nsamp - 1 : nsamp;
                }
            } else if (in_pos) {
                if (pos_sum > pos_sum_max) {
                    pos_sum_max = pos_sum;
                    first_index_pos = start_pos;
                    last_index_pos = nsamp > 0 ? nsamp - 1 : nsamp;
                }
            }
        }

        int minIndex = 0;
        int maxIndex = sample.length;

        if (peakOnly) {
            pos_sum = pos_sum_max;
            neg_sum = neg_sum_max;
        }

        if (pos_sum > neg_sum) {
            if (peakOnly) {
                minIndex = first_index_pos;
                maxIndex = last_index_pos;
            }
            pos_sum = 0.0f;
            for (nsamp = 0; nsamp < maxIndex; nsamp++) {
                float value = sample[nsamp];
                if (value <= outOfRangeMin || value >= outOfRangeMax || peakOnly && nsamp < minIndex) {
                    value = 0.0f;
                }
                if (value > 0.0) {
                    pos_sum += value * (float) sampleInt;
                }
                sample[nsamp] = pos_sum;
            }
            ampMin = pos_ampMin;
            ampMax = pos_ampMax;
        } else {
            if (peakOnly) {
                minIndex = first_index_neg;
                maxIndex = last_index_neg;
            }
            neg_sum = 0.0f;
            for (nsamp = 0; nsamp < maxIndex; nsamp++) {
                float value = sample[nsamp];
                if (value <= outOfRangeMin || value >= outOfRangeMax || peakOnly && nsamp < minIndex) {
                    value = 0.0f;
                }
                if (value < 0.0) {
                    neg_sum += -value * (float) sampleInt;
                }
                sample[nsamp] = neg_sum;
            }
            ampMin = neg_ampMin;
            ampMax = neg_ampMax;
        }
        if (peakOnly && maxIndex < sample.length) {
            float value = 0.0f;
            if (maxIndex > 0) {
                value = sample[maxIndex - 1];
            }
            for (nsamp = maxIndex; nsamp < sample.length; nsamp++) {
                sample[nsamp] = value;
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            //sampleMean = (double) (mean / (float) sample.length);
            lastSampleMean = INVALID_AMPLITUDE;
        }

        ampUnits = PhysicalUnits.timeIntegral(ampUnits);

        return (sampleInt * (double) (maxIndex - minIndex + 1));

    }

    /**
     * Applies time-domain differentiation to the time series samples.
     *
     */
    public final void differentiate(float lastValue) {

        // if no valid lastValue set lastValue to value of first sample to help stabilize beginning of diff time series
        if (Float.isNaN(lastValue) && sample.length > 0) {
            lastValue = sample[0];
        }

        float diff;

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;
        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                diff = (value - lastValue) / (float) sampleInt;
                // the following println output prevents a crash on Netscape4.51 on Windows !!!
                //if (nsamp < 1)
                //    System.out.println("Differentiate");
/*System.out.println ("Differentiate: ampMin " + ampMin
                 + " ampMax " + ampMax + " diff " + diff + " sample[nsamp] "
                 + sample[nsamp] + " lastValue " + lastValue + " (float) sampleInt "
                 + (float) sampleInt);*/
                lastValue = value;
                sample[nsamp] = diff;
                ampMin = Math.min(diff, ampMin);
                ampMax = Math.max(diff, ampMax);
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        ampUnits = PhysicalUnits.timeDerivative(ampUnits);

    }

    /**
     * Multiply each of the time series samples by a constant value.
     *
     * @param value the multiplier value
     */
    public void multiply(double multiplier) {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                sample[nsamp] *= multiplier;
                ampMin = Math.min(sample[nsamp], ampMin);
                ampMax = Math.max(sample[nsamp], ampMax);
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Add a constant value to each of the time series samples.
     *
     * @param value the multiplier value
     */
    public void add(double addValue) {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                sample[nsamp] += addValue;
                ampMin = Math.min(sample[nsamp], ampMin);
                ampMax = Math.max(sample[nsamp], ampMax);
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Take the natural logarithm of each of the time series samples.
     *
     */
    public void log() {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                if (value > 0.0f) {
                    value = (float) Math.log(value);
                } else {
                    value = 0.0f;
                }
                ampMin = Math.min(value, ampMin);
                ampMax = Math.max(value, ampMax);
                sample[nsamp] = value;
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Take the base 10 logarithm of each of the time series samples.
     *
     */
    public void log10() {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                if (value > 0.0f) {
                    value = (float) Math.log10(value);
                } else {
                    value = 0.0f;
                }
                ampMin = Math.min(value, ampMin);
                ampMax = Math.max(value, ampMax);
                sample[nsamp] = value;
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Take square root of each of the time series samples.
     *
     */
    public void sqrt() {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                if (value > 0.0f) {
                    value = (float) Math.sqrt(value);
                } else {
                    value = 0.0f;
                }
                ampMin = Math.min(value, ampMin);
                ampMax = Math.max(value, ampMax);
                sample[nsamp] = value;
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Find square of each of the time series samples.
     *
     */
    public void square() {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                value = value * value;
                ampMin = Math.min(value, ampMin);
                ampMax = Math.max(value, ampMax);
                sample[nsamp] = value;
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Find absolute value of each of the time series samples.
     *
     */
    public void abs() {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                value = Math.abs(value);
                ampMin = Math.min(value, ampMin);
                ampMax = Math.max(value, ampMax);
                sample[nsamp] = value;
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Normalize each of the time series samples relative to its maximum abs.
     *
     */
    public void norm() {

        float peakValue = Math.abs(sample[indexOfAmpZeroToPeakMax()]);
        if (peakValue > 100.0f * Float.MIN_VALUE) {
            multiply(1.0 / peakValue);
        }


        sampleChanged = true;

    }

    /**
     * Method to chop each of the time series samples (set equal to zero if negative)
     *
     */
    public void chop() {

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            if (sample[nsamp] < 0.0f) {
                sample[nsamp] = 0.0f;
            }
        }

        sampleChanged = true;

    }

    /**
     * Multiply each of the time series samples by a constant value which varies linearly.
     *
     * @param value1 the multiplier value at the first sample
     * @param value2 the multiplier value at the last sample
     */
    public void multiplyLinear(double value1, double value2) {

        ampMin = Float.MAX_VALUE;
        ampMax = -Float.MAX_VALUE;

        double diff = value2 - value1;
        double dlength = (double) sample.length;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                sample[nsamp] *= value1 + diff * (double) nsamp / dlength;
                ampMin = Math.min(sample[nsamp], ampMin);
                ampMax = Math.max(sample[nsamp], ampMax);
            }
        }

        sampleChanged = true;

        if (sample.length > 0) {
            lastSampleMean = INVALID_AMPLITUDE;
        }

        //ampUnits = PhysicalUnits.timeIntegral(ampUnits);

    }

    /**
     * Returns the index of the maximum zero-to-peak amplitude in the seismogram.
     *
     * @return the index of the maximum zero-to-peak amplitude or -1 if not available
     */
    public final int indexOfAmpZeroToPeakMax() {

        float amax = -Float.MAX_VALUE;
        int ndxmax = -1;
        float atest;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                atest = Math.abs(value);
                if (atest > amax) {
                    amax = atest;
                    ndxmax = nsamp;
                }
            }
        }

        return (ndxmax);

    }

    /**
     * Returns the indices of the maximum peak-to-peak amplitude within a time window.
     *
     * @return the indices of the maximum amplitude or null if not available
     */
    public final RangeInteger indicesOfAmpPeakToPeakMax(double timeWindowWidth) {

        int iWindowWidth = (int) (timeWindowWidth / sampleInt);
        if (iWindowWidth < 1 || iWindowWidth >= sample.length) {
            iWindowWidth = sample.length - 1;
        }

        // extrema of time series
        float fvalMin = 0.0f;
        float fvalMax = 0.0f;
        int indexMin = -1;
        int indexMax = -1;

        // extrema in current window
        float fmin = Float.MAX_VALUE;
        float fmax = -Float.MIN_VALUE;
        int imin = -1;
        int imax = -1;

        // loop through time series
        for (int nsamp = 0; nsamp < sample.length - iWindowWidth; nsamp++) {

            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {

                // end of next window
                int nsampEnd = nsamp + iWindowWidth;

                // semi-degenerate case
                // check if both extremum are in window
                if (imin >= nsamp && imax >= nsamp) {
                    // then check if new sample at end of window replaces an existing extremum
                    if (sample[nsampEnd] < fmin) {
                        fmin = sample[nsampEnd];
                        imin = nsampEnd;
                    } else if (sample[nsampEnd] > fmax) {
                        fmax = sample[nsampEnd];
                        imax = nsampEnd;
                    } else {
                        continue;
                    }
                } else {
                    // find both extrema in current window
                    fmin = Float.MAX_VALUE;
                    fmax = -Float.MIN_VALUE;
                    imin = -1;
                    imax = -1;
                    for (int ns = nsamp; ns <= nsampEnd; ns++) {
                        if (sample[ns] < fmin) {
                            fmin = sample[ns];
                            imin = ns;
                        } else if (sample[ns] > fmax) {
                            fmax = sample[ns];
                            imax = ns;
                        }
                    }
                }

                // check if new extrema superseed previous extrema
                if (fmax - fmin > fvalMax - fvalMin) {
                    fvalMin = fmin;
                    fvalMax = fmax;
                    indexMin = imin;
                    indexMax = imax;
                }
            }
        }

        return (new RangeInteger(indexMin, indexMax));

    }

    /**
     * Returns the index of the first occurence of the target cumulative value as a proportion of the maxium zero to peak value on the TimeSeries.
     *
     * @param cumValue target cumulative value as a proportion of the value at the end of the TimeSeries
     * @return the index of the first occurence of the value or -1 if not available
     */
    public int indexOfCumulative(double cumValue) {

        float amax = sample[indexOfAmpZeroToPeakMax()];
        int ndxcum = -1;
        float atest;

        for (int nsamp = 0; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                atest = Math.abs(value);
                if (atest >= amax * cumValue) {
                    ndxcum = nsamp;
                    break;
                }
            }
        }

        return (ndxcum);
    }

    /**
     * Returns (1) the index of the first value on the TimeSeries where a line from the pivot point is further from the value than distanceLimit
     * proportion of its length, and (2) the index of the first value before (1) where the line from the pivot point is further from the value than
     * distanceLimit - errorWidth proportion of its length
     *
     *
     * @see	TimeSeries
     * @param pivotIndex index of the pivot point
     * @param linePivotDelay time delay from pivot point to start checking for distanceLimit
     * @param distanceLimit max distance of pivot line above values as a proportion of the height of the pivot line
     * @param errorWidth distance of pivot line from values for error determination as a proportion of the height of the pivot line
     * @return the index of the first occurence of the value or -1 if not available
     */
    public int[] indexOfPivotLimit(int pivotIndex, double linePivotDelay, double distanceLimit, double errorWidth) {

        // start at index = pivot index + errorWdith
        int startIndex = pivotIndex + (int) (linePivotDelay / this.sampleInt) + 1;

        if (startIndex >= sample.length) {
            return (null);
        }

        // initialize slope of line
        float currSlope = (sample[startIndex] - sample[pivotIndex]) / (float) (startIndex - pivotIndex);

        int indexLimit = -1;
        int indexMaxSlope = -1;
        for (int nsamp = startIndex + 1; nsamp < sample.length; nsamp++) {
            float value = sample[nsamp];
            if (value > outOfRangeMin && value < outOfRangeMax) {
                int npts = nsamp - pivotIndex;
                float currEstValue = currSlope * (float) npts;
                float currValue = sample[nsamp] - sample[pivotIndex];
                float atest = (currEstValue - currValue) / currEstValue;
                // difference is >= limit - error
                //if (indexMaxSlope == -1 && atest >= distanceLimit - errorWidth) {
                //     indexMaxSlope = nsamp;
                // } else if (atest < distanceLimit - errorWidth) {
                //     indexMaxSlope = -1;
                //}
                // difference is >= limit
                if (atest > distanceLimit) {
                    indexLimit = nsamp;
                    break;
                }
                // update slope of line
                float newSlope = (sample[nsamp] - sample[pivotIndex]) / (float) npts;
                if (newSlope > currSlope) {   // slope increases, reset max slope index
                    indexMaxSlope = -1;
                } else {   // slope decreased, save index for error determination
                    if (indexMaxSlope == -1) {
                        indexMaxSlope = nsamp;
                    }
                }
                newSlope = currSlope * ((float) (npts - 1) / (float) npts) + newSlope / (float) npts;
                currSlope = newSlope;
            }
        }

        if (indexLimit < 0) {
            return (null);
        }

        int[] indices = new int[2];
        indices[0] = (indexLimit + indexMaxSlope) / 2;
        indices[1] = indexMaxSlope;

        return (indices);

    }

    /**
     * Performs FFT on time series and returns Complex spectrum.
     *
     * @param taperPercent the amount of taper as percent of time series length
     * @param trailingZeroPad the number of points of trailing zero padding to apply
     */
    public Cmplx[] getFFT(double taperPercent, int trailingZeroPad) {

        float[] tempSample = new float[this.sample.length + trailingZeroPad];
        System.arraycopy(this.sample, 0, tempSample, 0, this.sample.length);
        for (int i = sample.length; i < tempSample.length; i++) {
            tempSample[i] = 0.0f;
        }

        TimeSeriesFloat tempTimeSeries = new TimeSeriesFloat(tempSample, this.sampleInt, this.lagTime,
                this.ampUnits, this.timeUnits);

        tempTimeSeries.cosineTaper(taperPercent);

        Cmplx[] cdata = Cmplx.fft(tempTimeSeries.sample);

        return (cdata);
    }
    /**
     * Applies a frequency domain process to the time series samples.
     *
     * @param freqProcess the frequency domain process to apply.
     * @param taperPercent the amount of taper as percent of time series length
     * @param params process dependent parameters
     * @param doFftInverse if true, do inverse fft, if false do not do inverse fft - implies next operation is freq process that can start from
     * complex fft array
     * @see	FrequencyDomainProcess
     */
    private static final boolean DO_NOT_SAVE_CDATA = false;

    public void applyFreqProcess(FrequencyDomainProcess freqProcess, double taperPercent, Object[] params, boolean doFftInverse) {

        //System.out.println("=== applyFreqProcess: " + freqProcess.getClass());

        // check if saved complex fft array can be used
        //System.out.println("" + DO_NOT_SAVE_CDATA + " " + sampleChanged + " " + (cdataLocal == null) + " "
        //+ (cdataStaticOwner != this) + " " + freqProcess.sampleChangedInPreProcess());
        if (DO_NOT_SAVE_CDATA || sampleChanged || cdataLocal.length < 1
                || freqProcess.sampleChangedInPreProcess()) {
            cosineTaper(taperPercent);
            sample = freqProcess.preProcess(sampleInt, sample);
            cdataLocal = Cmplx.fft(sample);
            //System.out.println("doing FFT: " + freqProcess.getClass());
        }
        cdataLocal = freqProcess.apply(sampleInt, cdataLocal);
        // check if inverse fft should be done
        if (DO_NOT_SAVE_CDATA || doFftInverse || freqProcess.sampleChangedInPostProcess()) {
            sample = Cmplx.fftInverse(cdataLocal, sample.length);
            sample = freqProcess.postProcess(sampleInt, sample);
            //System.out.println("doing inverse FFT: " + freqProcess.getClass());
            cdataLocal = new Cmplx[0];  // flag cdata as not to be used, and allow garbage collection
        }
        sampleChanged = freqProcess.sampleChangedInPostProcess();

        calculateAndSetAmplitudeRange(false);
        //calculateAndSetMean();

        freqProcess.updateFields(this);

    }

    /**
     * Applies a time domain process to the time series samples.
     *
     * @param timeProcess the time domain process to apply.
     * @see	TimeDomainProcess
     */
    public void applyTimeDomainProcess(TimeDomainProcess timeProcess) throws TimeDomainException {

        // check if process is something this time series handles itself
        if (timeProcess instanceof TimeDomainProcesses) {
            if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.INTEGRATE) {
                TimeDomainMemory tdm = timeProcess.getMemory();
                float startValue = 0.0f;
                if (tdm != null) {
                    startValue = tdm.output[0];
                }
                integrate(startValue);
                if (tdm != null) {
                    tdm.output[0] = this.sample[this.sampleLength() - 1];
                }
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.DIFFERENTIATE) {
                TimeDomainMemory tdm = timeProcess.getMemory();
                float lastValue = Float.NaN;
                if (tdm != null) {
                    lastValue = tdm.output[0];
                }
                if (tdm != null) {
                    tdm.output[0] = this.sample[this.sampleLength() - 1];
                }
                differentiate(lastValue);
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.MULTIPLY) {
                multiply(((TimeDomainProcesses) timeProcess).parameters[0]);
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.ADD) {
                add(((TimeDomainProcesses) timeProcess).parameters[0]);
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.SQRT) {
                sqrt();
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.SQUARE) {
                square();
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.ABS) {
                abs();
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.LOG) {
                log();
            } else if (((TimeDomainProcesses) timeProcess).processID == TimeDomainProcesses.LOG10) {
                log10();
            } else {
                sample = timeProcess.apply(sampleInt, sample);
                sampleChanged = true;
                calculateAndSetAmplitudeRange(false);
                //calculateAndSetMean();
                timeProcess.updateFields(this);
            }
        } else {
            sample = timeProcess.apply(sampleInt, sample);
            sampleChanged = true;
            calculateAndSetAmplitudeRange(false);
            //calculateAndSetMean();
            timeProcess.updateFields(this);
        }

    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    public String toString() {

        return (new String(
                "sample.length = " + sample.length + "\n"
                + super.toString()));

    }
}	// End class TimeSeriesFloat
