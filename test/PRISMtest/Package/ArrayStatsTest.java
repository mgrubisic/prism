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

import SmProcessing.ArrayStats;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class ArrayStatsTest {
    double[] a1;
    double[]a2;
    double[] b1;
    ArrayStats stata1;
    ArrayStats statb1;
    ArrayStats stata2;
    ArrayStats statmode;
    double EPSILON = 0.001;
    double EPS2 = 0.1;
    int[] hista1 = {2,2,2,2,2,2,2,2,2,2};
    int[] histb1 = {2,2,2,2,2,2,2,2,2,2};
    int[] hista2 = {5,0,0,0,0,0,10,0,0,10};
    
    double[] mode = { 3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,
                        5.2,5.2,5.2,5.2,5.2,5.2,5.2,5.2,5.2,5.2,
                        -1.4,-1.4,-1.4,-1.4,-1.4,-1.4,-1.4,-1.4,-1.4,-1.4,
                        7.6,7.6,7.6,7.6,7.6,7.6,7.6,7.6,7.6,7.6,
                        -9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,
                        5.2,5.2,5.2,5.2,5.2,5.2,5.2,5.2,5.2,5.2,
                        -4.9,-4.9,-4.9,-4.9,-4.9,-4.9,-4.9,-4.9,-4.9,-4.9,
                        3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,
                        3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,
                        -6.6,-6.6,-6.6,-6.6,-6.6,-6.6,-6.6,-6.6,-6.6,-6.6,
                        7.6,7.6,7.6,7.6,7.6,7.6,7.6,7.6,7.6,7.6,
                        -9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,
                        -9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8,-9.8};
        
    public ArrayStatsTest() {
        this.a1 = new double[20];
        this.b1 = new double[20];
        this.a2 = new double[25];
    }
    
    @Before
    public void setUp() {
        for (int i = 1; i < 21; i++) {
            a1[i-1] = (double)i;
        }
        stata1 = new ArrayStats( a1 );
        for (int i = 0; i < 20; i++) {
            b1[i] = a1[i] * Math.pow(-1.0, i);
        }
        statb1 = new ArrayStats( b1 );
        
        for (int i = 1; i < 26; i++) {
            a2[i-1] = mode[i-1];
        }
        stata2 = new ArrayStats( a2 );
        
        statmode = new ArrayStats( mode );
    }
    
    @Test
    public void testBasicStats() {
        org.junit.Assert.assertEquals(1.0, stata1.getMinVal(), EPSILON);
        org.junit.Assert.assertEquals(20.0, stata1.getMaxVal(), EPSILON);
        org.junit.Assert.assertEquals(20.0, stata1.getPeakVal(), EPSILON);
        org.junit.Assert.assertEquals(19, stata1.getPeakValIndex());
        org.junit.Assert.assertEquals(10.5, stata1.getMean(), EPSILON);
        
        org.junit.Assert.assertEquals(-20.0, statb1.getMinVal(), EPSILON);
        org.junit.Assert.assertEquals(19.0, statb1.getMaxVal(), EPSILON);
        org.junit.Assert.assertEquals(-20.0, statb1.getPeakVal(), EPSILON);
        org.junit.Assert.assertEquals(19, statb1.getPeakValIndex());
        org.junit.Assert.assertEquals(-0.5, statb1.getMean(), EPSILON);
        
        org.junit.Assert.assertEquals(-1.4, stata2.getMinVal(), EPSILON);
        org.junit.Assert.assertEquals(5.2, stata2.getMaxVal(), EPSILON);
        org.junit.Assert.assertEquals(5.2, stata2.getPeakVal(), EPSILON);
        org.junit.Assert.assertEquals(10, stata2.getPeakValIndex());
        org.junit.Assert.assertEquals(3.04, stata2.getMean(), EPSILON);        
    }
    
    @Test
    public void testHistogram() {
        org.junit.Assert.assertArrayEquals(hista1, stata1.makeHistogram(10));
        org.junit.Assert.assertEquals(1.9, stata1.getHistogramInterval(), EPSILON);
        org.junit.Assert.assertArrayEquals(histb1, statb1.makeHistogram(10));
        org.junit.Assert.assertEquals(3.9, statb1.getHistogramInterval(), EPSILON);
        org.junit.Assert.assertArrayEquals(hista2, stata2.makeHistogram(10));
        org.junit.Assert.assertEquals(0.66, stata2.getHistogramInterval(), EPSILON);
    }
    @Test
    public void testModalMinimum() {
        org.junit.Assert.assertEquals(-8.93, statmode.getModalMinimum(10), EPSILON);
        org.junit.Assert.assertEquals(10.0, stata1.getModalMinimum(5), EPS2);
        org.junit.Assert.assertEquals(-1.4, stata2.getModalMinimum(5), EPS2);
    }
}
