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

import static PRISMtest.Package.FFTtest.picktest;
import SmProcessing.ArrayOps;
import SmProcessing.ArrayStats;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class ArrayOpsTest {
    double[] posconstant;
    double[] zeroconstant;
    double[] negconstant;
    double[] linecentered;
    double[] linepos;
    double[] lineneg;
    double[] lineinte;
    double[] poly;
    double[] polypos;
    double[] polysin;
    double[] polyline;
    double[] poly3order;
    double[] time;
    int[] counter;
    int LENGTH = 100;
    double EPSILON = 0.1;
    double BIG_EPSILON = 1.0;
    double STEP = 1.0;
    double[] smooth;
    double[] accel;
    double[] vel;
    double[] disp;
    String[] filecontents;
    
    String dirname = "D:\\PRISM\\test\\arrayops";
    String accelfile = "D:\\PRISM\\test\\arrayops\\acceleration.txt";
    String velfile = "D:\\PRISM\\test\\arrayops\\velocity.txt";
    String dispfile = "D:\\PRISM\\test\\arrayops\\displacement.txt";
    
    ArrayStats centerstat;
    ArrayStats posstat;
    ArrayStats negstat;
    ArrayStats polystat;
    
    public ArrayOpsTest() {
        posconstant = new double[LENGTH];
        zeroconstant = new double[LENGTH];
        negconstant = new double[LENGTH];
        linecentered = new double[LENGTH];
        lineinte = new double[LENGTH];
        linepos = new double[LENGTH];
        lineneg = new double[LENGTH];
        polypos = new double[LENGTH];
        poly = new double[LENGTH];
        polysin = new double[LENGTH];
        polyline = new double[LENGTH];
        poly3order = new double[LENGTH];
        time = new double[LENGTH];
        counter = new int[LENGTH];
        smooth = new double[LENGTH];
        filecontents = new String[36199];
        
        Arrays.fill(posconstant, 2.0);
        Arrays.fill(zeroconstant, 0.0);
        Arrays.fill(negconstant, -3.8);
        for (int i = 0; i < LENGTH; i++) {
            counter[i] = 2 * i;
            time[i] = i;
            linecentered[i] = (0.1 * i) - 5.0;
            linepos[i] = (0.1 * i);
            lineneg[i] = (0.1 * i) - 10.0;
            lineinte[i] = 2.0 * i;
            poly[i] = Math.pow(i, 2);
            polysin[i] = Math.sin((double)i);
            polypos[i] = polysin[i] + 4.0;
            polyline[i] = polysin[i] + (0.1 * i);
            poly3order[i] = Math.pow(i,3) + 2.5*Math.pow(i,2) - 8.9*i - 4.2;
        }
        Arrays.fill(smooth,2.0);
        smooth[9] = 2.25;
        smooth[10] = 2.5;
        smooth[11] = 2.25;
    }
    
    @Before
    public void setUp() throws IOException {
        centerstat = new ArrayStats( linecentered );
        posstat = new ArrayStats( linepos );
        negstat = new ArrayStats( lineneg );
        polystat = new ArrayStats( polypos );

        int next = 0;
        Path name = Paths.get( accelfile );
        TextFileReader infile = new TextFileReader( name.toFile() );
        filecontents = infile.readInTextFile();
        accel = new double[filecontents.length];
        for (String num : filecontents) {
            accel[next++] = Double.parseDouble(num);
        }

        next = 0;
        name = Paths.get( velfile );
        infile = new TextFileReader( name.toFile() );
        filecontents = infile.readInTextFile();
        vel = new double[filecontents.length];
        for (String num : filecontents) {
            vel[next++] = Double.parseDouble(num);
        }

        next = 0;
        name = Paths.get( dispfile );
        infile = new TextFileReader( name.toFile() );
        filecontents = infile.readInTextFile();
        disp = new double[filecontents.length];
        for (String num : filecontents) {
            disp[next++] = Double.parseDouble(num);
        }
}
    
     @Test
     public void testRemoveValue() throws IOException {
        TextFileWriter writeout = new TextFileWriter(dirname, "time.txt",time);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "linecentered.txt",linecentered);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "linepos.txt",linepos);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "lineneg.txt",lineneg);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "lineinte.txt",lineinte);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "poly.txt",poly);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "polysin.txt",polysin);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "polypos.txt",polypos);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "polyline.txt",polyline);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "zeroconstant.txt",zeroconstant);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "posconstant.txt",posconstant);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "negconstant.txt",negconstant);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "poly3order.txt",poly3order);
        writeout.writeOutArray();

        double[] test = new double[LENGTH];
         System.arraycopy(zeroconstant, 0, test, 0, LENGTH);
         ArrayOps.removeValue(test, 0.0);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);

         System.arraycopy(posconstant, 0, test, 0, LENGTH);
         ArrayOps.removeValue(test, 2.0);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(negconstant, 0, test, 0, LENGTH);
         ArrayOps.removeValue(test, -3.8);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(linecentered, 0, test, 0, LENGTH);
         ArrayOps.removeValue(test, centerstat.getMean());
         org.junit.Assert.assertArrayEquals(linecentered, test, EPSILON);
         
         System.arraycopy(linepos, 0, test, 0, LENGTH);
         ArrayOps.removeValue(test, posstat.getMean());
         org.junit.Assert.assertArrayEquals(linecentered, test, EPSILON);
         
         System.arraycopy(lineneg, 0, test, 0, LENGTH);
         ArrayOps.removeValue(test, negstat.getMean());
         org.junit.Assert.assertArrayEquals(linecentered, test, EPSILON);
         
         boolean result;
         System.arraycopy(polypos, 0, test, 0, LENGTH);
         result = ArrayOps.removeValue(test, polystat.getMean());
         org.junit.Assert.assertArrayEquals(polysin, test, EPSILON);
         org.junit.Assert.assertEquals(true, result);
         
         double[] test2 = null;
         result = ArrayOps.removeValue(test2, 1.0);
         org.junit.Assert.assertEquals(false, result);
         test2 = new double[0];
         result = ArrayOps.removeValue(test2, 1.0);
         org.junit.Assert.assertEquals(false, result);
     }
     @Test
     public void testRemoveLinearTrend() {
         double[] test = new double[LENGTH];
         System.arraycopy(linecentered, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(linepos, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(lineneg, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(zeroconstant, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(posconstant, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(negconstant, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(polyline, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(polysin, test, EPSILON);

         double[] test2 = null;
         boolean result;
         result = ArrayOps.removeLinearTrend(test2, STEP);
         org.junit.Assert.assertEquals(false, result);
         test2 = new double[0];
         result = ArrayOps.removeLinearTrend(test2, STEP);
         org.junit.Assert.assertEquals(false, result);
         System.arraycopy(polyline, 0, test, 0, LENGTH);
         result = ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertEquals(true, result);
         System.arraycopy(polyline, 0, test, 0, LENGTH);
         result = ArrayOps.removeLinearTrend(test, 0.0);
         org.junit.Assert.assertEquals(false, result);
}
     @Test
     public void testIntegrateDifferentiate() {
         double[] test = new double[0];
         double[] test1 = new double[0];
         double[] test2 = null;
         org.junit.Assert.assertArrayEquals(lineinte, ArrayOps.Differentiate(poly, STEP), BIG_EPSILON);
         org.junit.Assert.assertArrayEquals(posconstant, ArrayOps.Differentiate(lineinte, STEP), EPSILON);
         org.junit.Assert.assertArrayEquals(test, ArrayOps.Differentiate(lineinte, 0.0), EPSILON);
         org.junit.Assert.assertArrayEquals(test1, ArrayOps.Differentiate(test, STEP), EPSILON);
         org.junit.Assert.assertArrayEquals(test1, ArrayOps.Differentiate(test2, STEP), EPSILON);
         org.junit.Assert.assertArrayEquals(poly, ArrayOps.Integrate(lineinte, STEP,0.0), EPSILON);
         org.junit.Assert.assertArrayEquals(lineinte, ArrayOps.Integrate(posconstant, STEP,0.0), EPSILON);
         org.junit.Assert.assertArrayEquals(test, ArrayOps.Integrate(lineinte, 0.0,0.0), EPSILON);
         org.junit.Assert.assertArrayEquals(test1, ArrayOps.Integrate(test, STEP,0.0), EPSILON);
         org.junit.Assert.assertArrayEquals(test1, ArrayOps.Integrate(test2, STEP,0.0), EPSILON);
     }
     @Test
     public void testMakeTimeArray() {
         org.junit.Assert.assertArrayEquals(time, ArrayOps.makeTimeArray(1.0, LENGTH), EPSILON);
         double[] test = new double[0];
         org.junit.Assert.assertArrayEquals(test, ArrayOps.makeTimeArray(1.0, 0), EPSILON);
         org.junit.Assert.assertArrayEquals(test, ArrayOps.makeTimeArray(0.0, LENGTH), EPSILON);
     }
     @Test
     public void testRemoveLinearTrendFromSubArray() {
         double[] test = new double[LENGTH];
         boolean result;
         System.arraycopy(polyline, 0, test, 0, LENGTH);
         result = ArrayOps.removeLinearTrendFromSubArray(test, linepos ,STEP);
         org.junit.Assert.assertArrayEquals(polysin, test, EPSILON);
         org.junit.Assert.assertEquals(true, result);
         double[] test2 = null;
         result = ArrayOps.removeLinearTrendFromSubArray(test2, linepos ,STEP);
         org.junit.Assert.assertEquals(false, result);
         result = ArrayOps.removeLinearTrendFromSubArray(test, test2 ,STEP);
         org.junit.Assert.assertEquals(false, result);
         test2 = new double[0];
         result = ArrayOps.removeLinearTrendFromSubArray(test2, linepos ,STEP);
         org.junit.Assert.assertEquals(false, result);
         result = ArrayOps.removeLinearTrendFromSubArray(test, test2 ,STEP);
         org.junit.Assert.assertEquals(false, result);
         result = ArrayOps.removeLinearTrendFromSubArray(test, linepos ,0.0);
         org.junit.Assert.assertEquals(false, result);
     }
     @Test
     public void testRemovePolynomialTrend() {
         double[] coefs;
         double[] test = new double[LENGTH];
         boolean result;
         System.arraycopy(poly, 0, test, 0, LENGTH);
         coefs = ArrayOps.findPolynomialTrend(test, 2, STEP);
         result = ArrayOps.removePolynomialTrend(test, coefs, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         org.junit.Assert.assertEquals(true, result);
         
         System.arraycopy(poly3order, 0, test, 0, LENGTH);
         coefs = ArrayOps.findPolynomialTrend(test, 3, STEP);
         result = ArrayOps.removePolynomialTrend(test, coefs, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         org.junit.Assert.assertEquals(true, result);
         
         result = ArrayOps.removePolynomialTrend(test, coefs, 0.0);
         org.junit.Assert.assertEquals(false, result);
         test = null;
         result = ArrayOps.removePolynomialTrend(test, coefs, STEP);
         org.junit.Assert.assertEquals(false, result);
         test = new double[0];
         result = ArrayOps.removePolynomialTrend(test, coefs, STEP);
         org.junit.Assert.assertEquals(false, result);
         test = new double[LENGTH];
         coefs = null;
         result = ArrayOps.removePolynomialTrend(test, coefs, STEP);
         org.junit.Assert.assertEquals(false, result);
         coefs = new double[0];
         result = ArrayOps.removePolynomialTrend(test, coefs, STEP);
         org.junit.Assert.assertEquals(false, result);
     }
     @Test
     public void testfindSubsetMean() {
         double val = ArrayOps.findSubsetMean(linepos, 0, 50);
         org.junit.Assert.assertEquals(2.5, val, EPSILON);
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.findSubsetMean(linepos, -1, 50), EPSILON);
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.findSubsetMean(linepos, 0, 2000), EPSILON);
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.findSubsetMean(linepos, 10, 10), EPSILON);
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.findSubsetMean(linepos, 10, 9), EPSILON);
         double[] test = null;
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.findSubsetMean(test, 0, 50), EPSILON);
         test = new double[0];
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.findSubsetMean(test, 0, 50), EPSILON);
     }
     @Test
     public void testRootMeanSquare() {
         double[] test1 = null;
         double[] test2 = new double[0];
         org.junit.Assert.assertEquals(0.0, ArrayOps.rootMeanSquare(posconstant,posconstant),EPSILON);
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.rootMeanSquare(test1,test1),EPSILON);
         org.junit.Assert.assertEquals(Double.MIN_VALUE, ArrayOps.rootMeanSquare(test2,test2),EPSILON);
     }
     @Test
     public void testCountsToPhysicalValues() {
         int[] test1 = null;
         int[] test2 = new int[0];
         double[] testout = new double[0];
         org.junit.Assert.assertArrayEquals(time, ArrayOps.countsToPhysicalValues(counter,0.5), EPSILON);
         org.junit.Assert.assertArrayEquals(testout, ArrayOps.countsToPhysicalValues(test1,0.5), EPSILON);
         org.junit.Assert.assertArrayEquals(testout, ArrayOps.countsToPhysicalValues(test2,0.5), EPSILON);
     }
     @Test
     public void testConvertArrayUnits() {
         double[] test1 = null;
         double[] test2 = new double[0];
         double[] testout = new double[0];
         org.junit.Assert.assertArrayEquals(negconstant, ArrayOps.convertArrayUnits(posconstant,-1.9), EPSILON);
         org.junit.Assert.assertArrayEquals(testout, ArrayOps.convertArrayUnits(test1,-1.9), EPSILON);
         org.junit.Assert.assertArrayEquals(testout, ArrayOps.convertArrayUnits(test2,-1.9), EPSILON);
     }
     @Test
     public void testPerform3PtSmoothing() {
         double[] test1 = null;
         double[] test2 = new double[0];
         double[] testout = new double[0];
         org.junit.Assert.assertArrayEquals(posconstant, ArrayOps.perform3PtSmoothing(posconstant), EPSILON);
         posconstant[10] = 3.0;
         org.junit.Assert.assertArrayEquals(smooth, ArrayOps.perform3PtSmoothing(posconstant), EPSILON);
         
         org.junit.Assert.assertArrayEquals(testout, ArrayOps.perform3PtSmoothing(test1), EPSILON);
         org.junit.Assert.assertArrayEquals(testout, ArrayOps.perform3PtSmoothing(test2), EPSILON);
     }
     @Test
     public void testFindZeroCrossing() {
         double[] test1 = null;
         double[] test2 = new double[0];
         org.junit.Assert.assertEquals(9,ArrayOps.findZeroCrossing(polysin, 12, 1));
         org.junit.Assert.assertEquals(84,ArrayOps.findZeroCrossing(polysin, 83, 0));
         org.junit.Assert.assertEquals(-1,ArrayOps.findZeroCrossing(posconstant, 80, 0));
         org.junit.Assert.assertEquals(-1,ArrayOps.findZeroCrossing(posconstant, 20, 1));
         
         org.junit.Assert.assertEquals(-2,ArrayOps.findZeroCrossing(test1, 80, 0));
         org.junit.Assert.assertEquals(-2,ArrayOps.findZeroCrossing(test2, 80, 0));
         org.junit.Assert.assertEquals(-2,ArrayOps.findZeroCrossing(posconstant, -80, 0));
         org.junit.Assert.assertEquals(-2,ArrayOps.findZeroCrossing(posconstant, 800, 0));
     }
     @Test
     public void testRemoveTrendWithBestFit() {
         double[] test1 = null;
         double[] test2 = new double[0];
         double[] fit = new double[LENGTH];
         org.junit.Assert.assertEquals(false, ArrayOps.removeTrendWithBestFit(test1, 2.0));
         org.junit.Assert.assertEquals(false, ArrayOps.removeTrendWithBestFit(test2, 2.0));
         org.junit.Assert.assertEquals(false, ArrayOps.removeTrendWithBestFit(linecentered, 0.0));
         
         System.arraycopy(linecentered, 0, fit, 0, LENGTH);
         org.junit.Assert.assertEquals(true, ArrayOps.removeTrendWithBestFit(fit, STEP));
         org.junit.Assert.assertArrayEquals(fit,zeroconstant,EPSILON);
         
         System.arraycopy(poly, 0, fit, 0, LENGTH);
         org.junit.Assert.assertEquals(true, ArrayOps.removeTrendWithBestFit(fit, STEP));
         org.junit.Assert.assertArrayEquals(fit,zeroconstant,EPSILON);
     }
     @Test
     public void testFindLinearTrend() {
         org.junit.Assert.assertArrayEquals(ArrayOps.findLinearTrend(polyline,STEP),linepos,EPSILON);
     }
//     @Test
//     public void testCompatibility() {
//         org.junit.Assert.assertArrayEquals(ArrayOps.Integrate(accel,0.005),vel,EPSILON);
//         org.junit.Assert.assertArrayEquals(ArrayOps.Integrate(vel,0.005),disp,EPSILON);
//     }
}
