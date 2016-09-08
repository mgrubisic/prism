/*******************************************************************************
 * Name: Java class FFourierTransform.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmProcessing;

import java.util.Arrays;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * This class calculates the FFT of an array using the Apache Commons Math 
 * package and returns the result as magnitudes (square root of the squares of
 * the real and imaginary parts).  It first pads the array to the next closest
 * power-of-2 length before calculating the FFT.
 * @author jmjones
 */
public class FFourierTransform {
    private int powerlength;
    private int fftlen;
    private int cpowerlength;
    /**
     * Constructor just initializes variables.
     */
    public FFourierTransform() {
        this.powerlength = 0;
        this.fftlen = 0;
        this.cpowerlength = 0;
    }
    /**
     * Performs the FFT calculations by padding the input array to the closest
     * power of 2 gt or eq to the current length, calling the FFT transform method,
     * extracting only the first half of the complex array returned and converting
     * these values to magnitudes.
     * @param array input array for calculating the transform
     * @return the magnitudes of the transformed array
     */
    public double[] calculateFFT( double[] array ) {
        double[] arrpad = padArray( array );
         
        FastFourierTransformer fft = new FastFourierTransformer( DftNormalization.STANDARD);
        Complex[] transfreq = fft.transform(arrpad, TransformType.FORWARD);

        fftlen = (powerlength / 2) + 1;
        double[] mags = new double[fftlen];
        for (int i = 0; i < fftlen; i++) {
            mags[i]= Math.sqrt(Math.pow(transfreq[i].getReal(),2) + 
                                    Math.pow(transfreq[i].getImaginary(),2));
        }
        return mags;
    }
    /**
     * Performs the FFT calculations by padding the input array AT THE START to the closest
     * power of 2 gt or eq to the current length, calling the FFT transform method,
     * and returning the complex array.
     * @param array input array for calculating the transform
     * @return the transformed array
     */
    public Complex[] calculateFFTComplex( double[] array ) {
        double[] arrpad = padArrayAtStart( array );
        FastFourierTransformer fft = new FastFourierTransformer( DftNormalization.STANDARD);
        Complex[] transfreq = fft.transform(arrpad, TransformType.FORWARD);
        return transfreq;
    }
    /**
     * Calculates the inverse FFT on an input complex array and returns an array
     * of doubles containing only the real component of the iFFT result.
     * @param transfreq array of complex frequency values
     * @return an array containing the real components of the iFFT result
     */
    public double[] inverseFFTComplex( Complex[] transfreq ) {
        Complex[] transpad = padArrayComplex( transfreq );
        double[] realvals;
        FastFourierTransformer fft = new FastFourierTransformer( DftNormalization.STANDARD);
        Complex[] invvals = fft.transform(transpad, TransformType.INVERSE);
        realvals = new double[invvals.length];
        for (int i = 0; i < invvals.length; i++) {
            realvals[i] = invvals[i].getReal();
        }
        return realvals;
    }
    /**
     * pads the incoming array with zeros to the nearest power of 2 length, pads
     * are added at the end
     * @param array the input array
     * @return the padded real array
     */
    private double[] padArray( double[] array ) {
        powerlength = findPower2Length( array.length);
        if (array.length != powerlength) {
            double[] arrpad = new double[powerlength];
            Arrays.fill(arrpad, 0.0);
            System.arraycopy(array, 0, arrpad, 0, array.length);
            return arrpad;
        } else {
            return array;
        }
    }
    /**
     * pads the incoming array with zeros to the nearest power of 2 length, pads
     * are added at the end
     * @param array the input array
     * @return the padded real array
     */
    private double[] padArrayAtStart( double[] array ) {
        powerlength = findPower2Length( array.length);
        int inlen = array.length;
        if (inlen != powerlength) {
            double[] arrpad = new double[powerlength];
            Arrays.fill(arrpad, 0.0);
            System.arraycopy(array, 0, arrpad, (powerlength-inlen), array.length);
            return arrpad;
        } else {
            return array;
        }
    }
    /**
     * pads the incoming array with zeros to the nearest power of 2 length, pads
     * are added at the end
     * @param array the input array
     * @return the padded real array
     */
    private Complex[] padArrayComplex( Complex[] carray ) {
        cpowerlength = findPower2Length( carray.length);
        Complex[] arrpad = new Complex[cpowerlength];
        Arrays.fill(arrpad, Complex.ZERO);
        System.arraycopy(carray, 0, arrpad, 0, carray.length);
        return arrpad;
    }
    /**
     * finds the nearest power of 2 that is greater than or equal to the input value
     * @param length the length of the input array
     * @return the nearest power of 2 length
     */
    public int findPower2Length( int length ) {
        int powlength = 2;
        int i = 2;
        if (length > 2) {
            while (powlength < length) {
                powlength = (int)Math.pow( 2,i);
                i++;
            }
        }
        return powlength;
    }
    /**
     * Getter for the calculated power of 2 length
     * @return the power of 2 length
     */
    public int getPowerLength() {
        return powerlength;
    }
}
