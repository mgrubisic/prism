/*******************************************************************************
 * Name: Java class EventOnsetDetectionTest.java
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

package PRISMtest.Package;

import SmProcessing.EventOnsetCoefs;
import SmProcessing.EventOnsetDetection;
import SmUtilities.TextFileReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.BeforeClass;
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
    
    static String picktest = "/PRISMtest/Data/15481673.AZ.FRD.HNN.txt";
    static String[] fileContents;
    
    static double[] hnn;
    EventOnsetCoefs check = new EventOnsetCoefs();
    
    public EventOnsetDetectionTest() {
        e5 = new EventOnsetDetection( 0.005 );
        e10 = new EventOnsetDetection( 0.01 );
        e20 = new EventOnsetDetection( 0.02 );
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, URISyntaxException {
        int next = 0;
        URL url = EventOnsetDetectionTest.class.getResource(picktest);
        if (url != null) {
            File name = new File(url.toURI());
            TextFileReader infile = new TextFileReader( name );
            fileContents = infile.readInTextFile();
            hnn = new double[fileContents.length];
            for (String num : fileContents) {
                hnn[next++] = Double.parseDouble(num);
            }
        } else {
            System.out.println("url null");
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
    @Test
    public void check15481673AZFRDHNNOnset() {
        double[] test;
        int pick = e10.findEventOnset(hnn);
        org.junit.Assert.assertEquals(1640, pick);
        org.junit.Assert.assertEquals(1640, e10.getEventStart());
        org.junit.Assert.assertEquals(1140, e10.applyBuffer(5));
        org.junit.Assert.assertEquals(1140, e10.getBufferedStart());
        org.junit.Assert.assertEquals(5.0, e10.getBufferLength(),0.001);
        
        test = new double[0];
        org.junit.Assert.assertEquals(-1, e10.findEventOnset(test));
        test = null;
        org.junit.Assert.assertEquals(-1, e10.findEventOnset(test));
    }
}
