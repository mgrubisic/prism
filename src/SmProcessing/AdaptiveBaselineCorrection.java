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
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 *
 * @author jmjones
 */
public class AdaptiveBaselineCorrection {
    private double deltat;
    private int numbreaks;
    private int degreeS;
    private int degreeP;
    
    public void AdaptiveBaselineCorrection(double deltat, int numbreaks, 
                                            int polyDegree, int splineDegree) {
        this.deltat = deltat;
        this.numbreaks = numbreaks;
        this.degreeP = polyDegree;
        this.degreeS = splineDegree;
    }
    
    public PolynomialSplineFunction getSplines( double[] vals, int[] inbreaks, 
                                                                    int degree ) {
        int len;
        double[] loctime;
        double[] subset;
        PolynomialFunction poly;
        PolynomialFunction[] polyArrays;
        PolynomialSplineFunction spfunction;
        polyArrays = new PolynomialFunction[numbreaks-1];
        double[] knots = new double[numbreaks];
        for (int i = 0; i < numbreaks-1; i++) {
            len = inbreaks[i+1] - inbreaks[i];
            subset = new double[len];
            System.arraycopy(vals,inbreaks[i],subset,0,len);
            loctime = ArrayOps.makeTimeArray( deltat, len);
            ArrayList<WeightedObservedPoint> points = new ArrayList<>();
            for (int j = 0; j < len; j++ ){
                points.add(new WeightedObservedPoint( 1.0, loctime[j], subset[j]));
            }
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
            double[] coefs = fitter.fit(points);
            poly = new PolynomialFunction (coefs);
            polyArrays[i] = poly;
        }
        for (int k = 0; k < numbreaks; k++) {
            knots[k] = inbreaks[k] * deltat;
        }
        spfunction = new PolynomialSplineFunction( knots, polyArrays );
        return spfunction;    
    }
    public int[] makeBreaks(int start, int end, int numbreaks) {
        int[] breakers = new int[numbreaks];
        int interval = Math.round((end-start) / (numbreaks-1));
//        System.out.println("break interval: " + interval);
        for (int i = 0; i < numbreaks-1; i++) {
            breakers[i] = i * interval;
        }
        breakers[numbreaks-1] = end-start;
//        System.out.println("breaks: ");
//        for (int each : breakers) {
//            System.out.println(each);
//        }
        return breakers;
    }
    public void makeCorrection( double[] array, int break1, int break2 ) {
        double[] bnn;
        double[] h1;
        double[] h2;
        double[] h3;
        double[] result;
        double[] time = ArrayOps.makeTimeArray(deltat, array.length);
        int[] breaks = makeBreaks( break1, break2, numbreaks);
//        System.out.println();
        
        h1 = new double[break1];
        h2 = new double[break2-break1];
        h3 = new double[array.length-break2];
        System.arraycopy(array, 0, h1, 0, break1);
        System.arraycopy(array, break1, h2, 0, break2-break1);
        System.arraycopy(array, break2, h3, 0, array.length-break2);
        result = new double[ array.length ];
//        System.out.println("hnn length: " + array.length + " result: " + result.length + " time: " + time.length);

//        System.out.println("test");
        double[] loctime;
        PolynomialSplineFunction spfunction = getSplines(h2, breaks, degreeS);
        double[] coefs1 = ArrayOps.removePolynomialTrend(h1, degreeP, deltat);
        double[] coefs3 = ArrayOps.removePolynomialTrend(h3, degreeP, deltat);
        double rms1 = ArrayOps.rootMeanSquare(h1);
        double rms3 = ArrayOps.rootMeanSquare(h3);
        loctime = ArrayOps.makeTimeArray( deltat, h2.length);
        for (int i = 0; i < h2.length; i++) {
            h2[i] = h2[i] - spfunction.value(loctime[i]);
        }
        double rms2 = ArrayOps.rootMeanSquare(h2);
//        System.out.println("rms1: " + rms1 + "  rms2: " + rms2 + "  rms3: " + rms3);
        System.arraycopy(h1, 0, result, 0, break1);
        System.arraycopy(h3, 0, result, break2, array.length - break2);
        System.arraycopy(h2, 0, result, break1, break2-break1);
        
        PolynomialFunction b1poly = new PolynomialFunction( coefs1 );
        PolynomialFunction b3poly = new PolynomialFunction( coefs3 );
        
        bnn = new double[time.length];
        for (int i = 0; i < bnn.length; i++) {
            if ( i < break1) {
                bnn[i] = b1poly.value(time[i]);
            } else if ( i >= break2) {
                bnn[i] = b3poly.value(time[i] - (break2*deltat));
            } else {
                bnn[i] = spfunction.value(time[i] - (break1*deltat));
            }
        }
        
    }
}
