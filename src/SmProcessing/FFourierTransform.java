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
    private double[] arrpad;
    private int powerlength;
    private int fftlen;
    /**
     * Constructor just initializes variables.
     */
    public FFourierTransform() {
        this.powerlength = 0;
        this.fftlen = 0;
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
        powerlength = findPower2Length( array.length);
        arrpad = new double[powerlength];
        Arrays.fill(arrpad, 0.0);
        System.arraycopy(array, 0, arrpad, 0, array.length);
         
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
