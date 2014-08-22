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

package PRISMtest.Package;

import SmProcessing.ArrayOps;
import static SmProcessing.ArrayOps.makeTimeArray;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialsUtils;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class MathSplineTest {
    static final String splinetest = "D:/PRISM/matlab_code/PRISM_V2/adaptiveBaseline/vel.txt";
    static String[] fileContents;
    
    static final int NUM_BREAKS = 10;
    
    double deltat = 0.005;
    double[] time;
    static double[] hnn;
    
    int break1 = 2023;
//    int break2 = 4636;
    int break2 = 7038;
    
    String outdir = "D:/PRISM/adaptive_test/junit";
    int degreeL = 1;
    int degreeP = 2;
    int degreeS = 3;
    
    public MathSplineTest() {
        
        System.out.println("constructor");

        time = new double[ hnn.length];
        for (int i = 0; i < hnn.length; i++) {
            time[i] = i* deltat;
        }
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        System.out.println("setUpClass");
        int next = 0;
        Path name = Paths.get( splinetest );
        TextFileReader infile = new TextFileReader( name.toFile() );
        fileContents = infile.readInTextFile();
        hnn = new double[fileContents.length];
        for (String num : fileContents) {
            hnn[next++] = Double.parseDouble(num);
        }
        System.out.println("first hnn: " + hnn[0]);
    }
    
    public PolynomialSplineFunction getSplines( double[] vals, int[] inbreaks, int degree ) {
        int len;
        double[] loctime;
        double[] subset;
        int numbreaks = inbreaks.length - 1;
        PolynomialFunction poly;
        PolynomialFunction[] polyArrays;
        PolynomialSplineFunction spfunction;
        polyArrays = new PolynomialFunction[numbreaks];
        double[] knots = new double[numbreaks+1];
        for (int i = 0; i < numbreaks; i++) {
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
        for (int k = 0; k < numbreaks+1; k++) {
            knots[k] = (inbreaks[k]) * deltat;
        }
        spfunction = new PolynomialSplineFunction( knots, polyArrays );
        return spfunction;    
    }
    public int[] makeBreaks(int start, int end, int numbreaks) {
        int[] breakers = new int[numbreaks+1];
        int interval = Math.round((end-start) / (numbreaks));
        System.out.println("break interval: " + interval);
        for (int i = 0; i < numbreaks; i++) {
            breakers[i] = i * interval + start;
        }
        breakers[numbreaks] = end;
        System.out.println("breaks: ");
        for (int each : breakers) {
            System.out.println(each);
        }
        return breakers;
    }
    
    @Test
    public void adaptiveBaselineTest() {
        double[] bnn;
        double[] h1;
        double[] h2;
        double[] h3;
        double[] result;
        int[] breaks = makeBreaks( break1, break2, NUM_BREAKS);
        int[] localbreaks = new int[ breaks.length ];
        for (int i = 0; i < breaks.length; i++) {
            localbreaks[i] = breaks[i] - break1;
        }
        System.out.println();
        
        h1 = new double[break1];
        h2 = new double[break2-break1];
        h3 = new double[hnn.length-break2];
        System.arraycopy(hnn, 0, h1, 0, break1);
        System.arraycopy(hnn, break1, h2, 0, break2-break1);
        System.arraycopy(hnn, break2, h3, 0, hnn.length-break2);
        result = new double[ hnn.length ];
        System.out.println("hnn length: " + hnn.length + " result: " + result.length + " time: " + time.length);

        System.out.println("test");
        double[] loctime;
        PolynomialSplineFunction spfunction = getSplines(h2, localbreaks, degreeS);
        double[] coefs1 = ArrayOps.removePolynomialTrend(h1, degreeP, deltat);
        double[] coefs3 = ArrayOps.removePolynomialTrend(h3, degreeP, deltat);
        double rms1 = ArrayOps.rootMeanSquare(h1);
        double rms3 = ArrayOps.rootMeanSquare(h3);
        loctime = ArrayOps.makeTimeArray( deltat, h2.length);
        for (int i = 0; i < h2.length; i++) {
            h2[i] = h2[i] - spfunction.value(loctime[i]);
        }
        double rms2 = ArrayOps.rootMeanSquare(h2);
        System.out.println("rms1: " + rms1 + "  rms2: " + rms2 + "  rms3: " + rms3);
        System.arraycopy(h1, 0, result, 0, break1);
        System.arraycopy(h3, 0, result, break2, hnn.length - break2);
        System.arraycopy(h2, 0, result, break1, break2-break1);
        System.out.println("h1[start]: " + h1[0] + "  h1[end]: " + h1[h1.length-1]);
        System.out.println("h2[start]: " + h2[0] + "  h2[end]: " + h2[h2.length-1]);
        System.out.println("h3[start]: " + h3[0] + "  h3[end]: " + h3[h3.length-1]);
        
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
        //!!!test
        breaks = makeBreaks( 0, bnn.length, NUM_BREAKS);
        double[] smoothbnn = new double[ bnn.length ];
        spfunction = getSplines(bnn, breaks, degreeS);
        for (int i = 0; i < time.length; i++) {
            smoothbnn[i] = spfunction.value(time[i]);
        }
        TextFileWriter textsm2 = new TextFileWriter( "D:/PRISM/adaptive_test/junit", "baseline_smooth.txt", smoothbnn);
        try {
            textsm2.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        breaks = makeBreaks( 0, bnn.length, NUM_BREAKS+2);
        smoothbnn = new double[ bnn.length ];
        spfunction = getSplines(bnn, breaks, degreeS);
        for (int i = 0; i < time.length; i++) {
            smoothbnn[i] = spfunction.value(time[i]);
        }
        TextFileWriter textsm = new TextFileWriter( "D:/PRISM/adaptive_test/junit", "baseline_smooth2.txt", smoothbnn);
        try {
            textsm.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        //!!!Test
        TextFileWriter textout = new TextFileWriter( "D:/PRISM/adaptive_test/junit", "vel_interpolate.txt", result);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        TextFileWriter text2 = new TextFileWriter( "D:/PRISM/adaptive_test/junit", "vel_baseline.txt", bnn);
        try {
            text2.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
    }
//    @Test
//    public void findBreakPoint() {
//        System.out.println("findBreakPoints");
//        int hnnlen = hnn.length;
//        int start;
//        int end;
//        double[] segment = new double[hnnlen-break1];
//        double[] coefs;
//        int[] signs = new int[NUM_BREAKS-1];
//        int[] transition = new int[NUM_BREAKS-1];
//        Arrays.fill(transition, 0);
//        System.arraycopy(hnn,break1,segment,0,segment.length);
//        int[] breaks = makeBreaks( 0, segment.length, NUM_BREAKS);
//        PolynomialSplineFunction  polyfuncs = getSplines( segment, breaks, degreeL );
//        PolynomialFunction[] parrays = polyfuncs.getPolynomials();
//        for (int i = 0; i < parrays.length; i++) {
//            coefs = parrays[i].getCoefficients();
//            signs[i] = (coefs[1] < 0) ? -1 : 1;
//            System.out.println("coefs length: " + coefs.length);
//            System.out.println("constant: " + coefs[0] + "  x term: " + coefs[1]);
////            System.out.println("constant: " + coefs[0] + "  x term: " + coefs[1] + "  x^2 term: " + coefs[2]);
//        }
//        start = breaks[0];
//        end = breaks[breaks.length-1];
//        for (int i = 0; i < signs.length-1; i++) {
//            if ((signs[i+1] * signs[i]) < 0) {
//                transition[i] = 1;
//                start = (i >= 2) ? breaks[i-2] : breaks[0];
//                end = (i <= breaks.length-3) ? breaks[i+2] : breaks[breaks.length-1];
//            }
//        }
//        System.arraycopy(hnn,break1,segment,0,segment.length);
//    }
}
