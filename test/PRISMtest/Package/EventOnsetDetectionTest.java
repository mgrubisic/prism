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

import SmProcessing.EventOnsetCoefs;
import SmProcessing.EventOnsetDetection;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class EventOnsetDetectionTest {
    
    final double EPSILON = 0.001;
    EventOnsetDetection e5;
    EventOnsetDetection e10;
    EventOnsetDetection e20;
    
    static final String picktest = "D:/PRISM/ppicktest/15481673.AZ.FRD.HNN.txt";
    static String[] fileContents;
    
    double[] a1;
    static double[] hnn;
    
    EventOnsetCoefs check = new EventOnsetCoefs();
    String outdir = "D:/PRISM/filter_test/junit";

    
    public EventOnsetDetectionTest() {
        e5 = new EventOnsetDetection( 0.005 );
        e10 = new EventOnsetDetection( 0.01 );
        e20 = new EventOnsetDetection( 0.02 );
        
        a1 = new double[100];
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        int next = 0;
        Path name = Paths.get( picktest );
        TextFileReader infile = new TextFileReader( name.toFile() );
        fileContents = infile.readInTextFile();
        hnn = new double[fileContents.length];
        for (String num : fileContents) {
            hnn[next++] = Double.parseDouble(num);
        }
        System.out.println("first hnn: " + hnn[0]);
    }
    
    @Before
    public void setUp() throws IOException {
        for (int i = 0; i < a1.length; i++) {
            if (i < 20) {
                a1[i] = -1.0;
            } else if ((i >= 20) && (i < 40)){
                a1[i] = 1.0;
            } else {
                a1[i] = -1.0;
            }
        }
    }
    
    @Test
    public void checkCoefficients() {
        double[] AeB = check.getAeBCoefs(0.005);
        double[] Ae = check.getAeCoefs(0.005);
        double[] combined = new double[AeB.length + Ae.length];
        System.arraycopy(Ae,0,combined,0, Ae.length);
        System.arraycopy(AeB,0,combined,Ae.length, AeB.length);
        org.junit.Assert.assertArrayEquals(combined, e5.showCoefficients(), EPSILON);

        AeB = check.getAeBCoefs(0.01);
        Ae = check.getAeCoefs(0.01);
        combined = new double[AeB.length + Ae.length];
        System.arraycopy(Ae,0,combined,0, Ae.length);
        System.arraycopy(AeB,0,combined,Ae.length, AeB.length);
        org.junit.Assert.assertArrayEquals(combined, e10.showCoefficients(), EPSILON);
    
        AeB = check.getAeBCoefs(0.02);
        Ae = check.getAeCoefs(0.02);
        combined = new double[AeB.length + Ae.length];
        System.arraycopy(Ae,0,combined,0, Ae.length);
        System.arraycopy(AeB,0,combined,Ae.length, AeB.length);
        org.junit.Assert.assertArrayEquals(combined, e20.showCoefficients(), EPSILON);
    }
    
//    @Test
//    public void checkOnset() {
//        int pick = e10.findEventOnset(a1, 0.0);
//        org.junit.Assert.assertEquals(19, pick);
//    }
    
//    @Test
//    public void check15481673AZFRDHNNOnset() {
//        int pick = e10.findEventOnset(hnn);
//        System.out.println("15481673");
//        org.junit.Assert.assertEquals(1572, pick);
//    }
}
