/*******************************************************************************
 * Name: Java class Resampling.java
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

import static SmConstants.VFileConstants.SAMPLING_LIMIT;
import SmException.SmException;
import java.util.Arrays;
import org.apache.commons.math3.complex.Complex;

/**
 * The Resampling class re-samples records to a higher sampling rate as needed.
 * If performs the resampling in the frequency domain, using the fft of the
 * input array to increase the frequency of the samples.
 * @author jmjones
 */
public class Resampling {
    private int ylen;
    private int zlen;
    private int padlen;
    private int factor;
    private int newrate;
    private final FFourierTransform fft;
    /**
     * The resampling constructor simply initializes variables
     */
    public Resampling() {
        this.ylen = 0;
        this.zlen = 0;
        this.factor = 0;
        this.padlen = 0;
        this.fft = new FFourierTransform();
    }
    /**
     * Performs the actual re-sampling by taking the fft of the input array and
     * padding the complex arrray with zeros in the middle to increase the frequency.
     * The inverse fft is then calculated and the new values are retrieved from
     * the real components of the ifft.
     * @param yarray the input array to be re-sampled
     * @param sps the initial sampling rate
     * @return the re-sampled array
     * @throws SmException if the input sampling rate is invalid
     */
    public double[] resampleArray( double[] yarray, int sps ) throws SmException {
        ylen = yarray.length;
        int fftpadlen = fft.findPower2Length(ylen);
        
//        System.out.println("input y array");
//        for (Double each : yarray) {
//            System.out.println(each);
//        }
        
        //compute the location for the zeroes in the complex array
        zlen = (int)Math.ceil((double)( fftpadlen + 1 ) / 2.0 );
        calcNewSamplingRate( sps );
        factor = getFactor();
        if (factor < 0) {
            throw new SmException("Invalid sampling rate of " + sps);
        }
        //this is the new length of the resampled output array
        int newlen = ylen * factor;
        
//        System.out.println("ylen: " + ylen);
//        System.out.println("fftpadlen: " + fftpadlen);
//        System.out.println("factor: " + factor);
//        System.out.println("newlen: " + newlen);
        
        //calculate the number of padding zeroes and fill the zero array
        padlen = ylen * (factor-1);
        int complexlen = fft.findPower2Length( padlen + fftpadlen);
        int padlenpower2 = complexlen - fftpadlen;
        Complex[] zeroes = new Complex[padlenpower2];
        Arrays.fill( zeroes, Complex.ZERO );

        //compute fft
        Complex[] zarray = fft.calculateFFTComplex( yarray );
//        System.out.println("length of zarray: " + zarray.length);
//        System.out.println("zlen: " + zlen);
//        System.out.println("padlen: " + padlen);
//        System.out.println("complexlen: " + complexlen);
//        System.out.println("padlen_power2: " + padlenpower2);
//        System.out.println("z array");
//        for (Complex each : zarray) {
//            System.out.println(each.toString());
//        }
        
        //construct a new Fourier spectrum by centering zeroes
        Complex[] zp = new Complex[complexlen];
//        System.out.println("length of zp: " + zp.length);
        System.arraycopy(zarray, 0, zp, 0, zlen);
        System.arraycopy(zeroes, 0, zp, zlen, padlenpower2);
        System.arraycopy(zarray, zlen, zp, (zlen+padlenpower2), fftpadlen-zlen);
//        System.out.println("zp before Nyquist");
//        for (Complex each : zp) {
//            System.out.println(each.toString());
//        }
        
        //correct for Nyquist (number of data in input signal is always even)
        zp[zlen-1] = zp[zlen-1].divide(2.0);
        zp[zlen-1+padlen] = zp[zlen-1];
//        System.out.println("zp after Nyquist");
//        for (Complex each : zp) {
//            System.out.println(each.toString());
//        }
        
        //compute inverse FFT
        double[] yp = new double[newlen];
        double[] ypfft = fft.inverseFFTComplex(zp);
//        System.out.println("yp back from inverse fft");
//        for (Double each : ypfft) {
//            System.out.println(each);
//        }
        //amplitude correction
        int newstart = complexlen - newlen;
        for (int i = newstart; i < complexlen; i++) {
            yp[i-newstart] = ypfft[i] * factor;
        }
//        System.out.println("output yp");
//        for (Double each : yp) {
//            System.out.println(each);
//        }
        return yp;
    }
    /**
     * Tests the input sampling rate against a sampling limit.
     * @param sps the input sampling rate
     * @return true if the array's sampling rate is below the sampling limit,
     * false otherwise
     */
    public boolean needsResampling( int sps ) {
        boolean needssampling = false;
        if (sps < SAMPLING_LIMIT) {
            needssampling = true;
        }
        return needssampling;
    }
    /**
     * Calculates a new sampling rate based on the initial rate and the sampling
     * limit.  Also calculates the factor such that old_rate * factor = new_rate.
     * @param sps the input sampling rate
     * @return the new sampling rate
     */
    public int calcNewSamplingRate( int sps ) {
        newrate = -1;
        if ((sps > 0) && (needsResampling( sps ))) {
            factor = (int)Math.ceil( (double)SAMPLING_LIMIT / (double)sps );
            newrate = sps * factor;
        }
        return newrate;
    }
    /**
     * Getter for the factor increase from old rate to new rate
     * @return the factor, such that old_rate * factor = new_rate
     */
    public int getFactor() { return factor; }
    /**
     * Getter for the new sampling rate.
     * @return the new sampling rate
     */
    public int getNewSamplingRate() { return newrate; }
}
