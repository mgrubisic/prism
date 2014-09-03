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
import SmProcessing.ArrayStats;
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
    int LENGTH = 100;
    double EPSILON = 0.1;
    double BIG_EPSILON = 1.0;
    double STEP = 1.0;
    
    ArrayStats centerstat;
    ArrayStats posstat;
    ArrayStats negstat;
    
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
        
        Arrays.fill(posconstant, 2.0);
        Arrays.fill(zeroconstant, 0.0);
        Arrays.fill(negconstant, -3.8);
        for (int i = 0; i < LENGTH; i++) {
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
    }
    
    @Before
    public void setUp() {
        centerstat = new ArrayStats( linecentered );
        posstat = new ArrayStats( linepos );
        negstat = new ArrayStats( lineneg );
    }
    
     @Test
     public void testRemoveMean() {
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
         
         System.arraycopy(polypos, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrend(test, STEP);
         org.junit.Assert.assertArrayEquals(polysin, test, EPSILON);
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
     }
     @Test
     public void testIntegrateDifferentiate() {
         double[] test = new double[0];
         org.junit.Assert.assertArrayEquals(lineinte, ArrayOps.Differentiate(poly, STEP), BIG_EPSILON);
         org.junit.Assert.assertArrayEquals(posconstant, ArrayOps.Differentiate(lineinte, STEP), EPSILON);
         org.junit.Assert.assertArrayEquals(test, ArrayOps.Differentiate(lineinte, 0.0), EPSILON);
         org.junit.Assert.assertArrayEquals(poly, ArrayOps.Integrate(lineinte, STEP), EPSILON);
         org.junit.Assert.assertArrayEquals(lineinte, ArrayOps.Integrate(posconstant, STEP), EPSILON);
     }
     @Test
     public void testMakeTimeArray() {
         org.junit.Assert.assertArrayEquals(time, ArrayOps.makeTimeArray(1.0, LENGTH), EPSILON);
     }
     @Test
     public void testRemoveLinearTrendFromSubArray() {
         double[] test = new double[LENGTH];
         System.arraycopy(polyline, 0, test, 0, LENGTH);
         ArrayOps.removeLinearTrendFromSubArray(test, linepos ,STEP);
         org.junit.Assert.assertArrayEquals(polysin, test, EPSILON);
     }
     @Test
     public void testRemovePolynomialTrend() {
         double[] test = new double[LENGTH];
         System.arraycopy(poly, 0, test, 0, LENGTH);
         ArrayOps.removePolynomialTrend(test, 2, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(poly, 0, test, 0, LENGTH);
         ArrayOps.removePolynomialTrend(test, 3, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
         
         System.arraycopy(poly3order, 0, test, 0, LENGTH);
         ArrayOps.removePolynomialTrend(test, 3, STEP);
         org.junit.Assert.assertArrayEquals(zeroconstant, test, EPSILON);
     }
}
