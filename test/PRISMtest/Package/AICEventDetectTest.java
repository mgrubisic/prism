/*******************************************************************************
 * Name: Java class AICEventDetectTest.java
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

import SmProcessing.AICEventDetect;
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
public class AICEventDetectTest {
    final double EPSILON = 0.001;
    AICEventDetect aicPeak;
//    AICEventDetect aicWhole;
    
    static String picktest = "/PRISMtest/Data/15481673.AZ.FRD.HNN.txt";
    static String[] fileContents;
    
    static double[] hnn;    
    
    public AICEventDetectTest() {
        aicPeak = new AICEventDetect();
//        aicWhole = new AICEventDetect();
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, URISyntaxException {
        int next = 0;
        URL url = AICEventDetectTest.class.getResource(picktest);
        if (url != null) {
            File name = new File(url.toURI());
            TextFileReader infile = new TextFileReader( name );
            fileContents = infile.readInTextFile();
//            System.out.println("length: " + fileContents.length);
            hnn = new double[fileContents.length];
            for (String num : fileContents) {
                hnn[next++] = Double.parseDouble(num);
            }
        } else {
            System.out.println("url null");
        }
    }
    
     @Test
     public void checkEventDetection() {
         double[] empty = new double[0];
         double[] test = null;
         String test2 = null;
         int pick1 = aicPeak.calculateIndex(hnn, "topeak");
         org.junit.Assert.assertEquals(1472, pick1);
         org.junit.Assert.assertEquals(1472,aicPeak.getIndex());
         
         org.junit.Assert.assertEquals(972,aicPeak.applyBuffer(5, 0.01));
         org.junit.Assert.assertEquals(972,aicPeak.getBufferedIndex());
         org.junit.Assert.assertEquals(0,aicPeak.applyBuffer(15, 0.01));
         org.junit.Assert.assertEquals(-1,aicPeak.applyBuffer(5, 0.0));
         
         org.junit.Assert.assertEquals(-1, aicPeak.calculateIndex(empty, "topeak"));
         org.junit.Assert.assertEquals(-1, aicPeak.calculateIndex(test, "topeak"));
         
         org.junit.Assert.assertEquals(1472, aicPeak.calculateIndex(hnn, ""));
         org.junit.Assert.assertEquals(1472, aicPeak.calculateIndex(hnn, "nnn"));
         org.junit.Assert.assertEquals(1472, aicPeak.calculateIndex(hnn, test2));
         
         
     }
}
