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
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author jmjones
 */
public class FFourierTransform {
    private double[] arrpad;
    private int powerlength;
    private int fftlen;
    
    public FFourierTransform() {
        this.powerlength = 0;
        this.fftlen = 0;
    }
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
    public int getPowerLength() {
        return powerlength;
    }
}
