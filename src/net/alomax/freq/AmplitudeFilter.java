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
package net.alomax.freq;

import net.alomax.math.*;

public class AmplitudeFilter extends BandpassFilter {

    /**
     * constructor
     */
    public AmplitudeFilter(double lowFreqCorner, double highFreqCorner, int numPoles) {

        super(lowFreqCorner, highFreqCorner, numPoles);
    }

    /**
     * constructor
     */
    public AmplitudeFilter(String localeText, double lowFreqCorner, double highFreqCorner, int numPoles) {

        super(localeText, lowFreqCorner, highFreqCorner, numPoles);
    }

    /**
     * * method to do amplitude band-pass filter in freq domain **
     */
    /*
     amplitude filter

     where -
     fl    - low frequency corner in Hz
     fh    - high frequency corner in Hz
     npole - number of poles in filter at each corner
     (not more than 20)
     npts  - number of complex fourier spectral coefficients
     dt    - sampling interval in seconds
     cx    - complex fourier spectral coefficients
     */
    public final Cmplx[] apply(double dt, Cmplx[] cx) {

        int npts = cx.length;
        double fl = lowFreqCorner;
        double fh = highFreqCorner;
        int npole = numPoles;

        Cmplx c0 = new Cmplx(0., 0.);
        Cmplx c1 = new Cmplx(1., 0.);

        double f, fnorm, ybp, testr, testi;
        double cr, ci;

        double df = 1. / ((double) npts * dt);
        double fn = 1. / (2. * dt);
        int j = 2;
        double amax = 10.0e30;
        int iexp = 2 * npole;
        double crash = Math.pow(10.0, (int) (35.0 / iexp + 0.5));


        cx[0] = c0;

        for (int i = 1; i < npts / 2 + 1; i++) {			//do 60 i=2,npts/2+1

            f = df * (double) i;
            fnorm = (f * f - fh * fl) / (f * (fh - fl));

            // CHECK FOR MATH OVERFLOW
            if (Math.abs(fnorm) > crash) {
                ybp = 10.0e-30;
            } else {
                ybp = 1. / (1. + Math.pow(fnorm, iexp));
            }
            cr = cx[i].doubleValue();
            ci = cx[i].imag();
            if (cr != 0.0) {
                testr = Math.abs(cr) * Math.abs(ybp);
                if (testr < 10.0e-35) {
                    cr = 0.0;
                }
            }
            if (ci != 0.0) {
                testi = Math.abs(ci) * Math.abs(ybp);
                if (testi < 10.0e-35) {
                    ci = 0.0;
                }
            }
            cx[i] = new Cmplx(cr, ci);
            //			cx[i] = Cmplx.mul(cx[i], ybp);
            cx[i].mul(ybp);
            cx[npts - i] = Cmplx.conjg(cx[i]);

        }


        return (cx);

    }

}	// End class AmplitudeFilter

