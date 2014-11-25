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

import static SmConstants.VFileConstants.*;
import SmException.SmException;
import SmProcessing.AdaptiveBaselineCorrection;
import SmProcessing.ArrayOps;
import static SmProcessing.ArrayOps.makeTimeArray;
import SmProcessing.ArrayStats;
import SmProcessing.ButterworthFilter;
import SmProcessing.FilterCutOffThresholds;
import SmUtilities.PrismXMLReader;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author jmjones
 */
public class AdaptiveBaselineTest {
    static final String splinetest = "D:/PRISM/matlab_code/PRISM_V2/adaptiveBaseline/vel.txt";
    static final String adapttest = "D:/PRISM/adaptive_test/junit3/Vel.txt";
    static final String hn2test = "D:/PRISM/smtesting/V0fail/out/LinearTrendRemovedVel.txt";
    static String[] fileContents;
    
    static final int NUM_BREAKS = 5;
    
    double deltat1 = 0.005;
    double dtime2 = 0.01;
    double mag = 5.1;
    double noval = -999.99;
    double[] time;
    double[] time2;
    static double[] hnn;
    static double[] hn2;
    AdaptiveBaselineCorrection adapt;
    AdaptiveBaselineCorrection adapt2;
    
    int break1 = 2023;
    int break2 = 4636;
    int breakh1 = 1227;
//    int breakh1 = 1000;
    int breakh2 = 1377;
//    int break2 = 7038;
    
    String outdir = "D:/PRISM/adaptive_test/junit2";
    int degreeL = 1;
    int degreeP1 = 2;
    int degreeP2 = 2;
    int degreeS = 3;
    
    public AdaptiveBaselineTest() {
        FilterCutOffThresholds threshold = new FilterCutOffThresholds();
        threshold.SelectMagAndThresholds(mag, noval, noval, noval, noval);
        double lowcut = threshold.getLowCutOff();
        double highcut = threshold.getHighCutOff();
        time = ArrayOps.makeTimeArray(dtime2, hnn.length);
        time2 = ArrayOps.makeTimeArray(dtime2, hn2.length);
        adapt = new AdaptiveBaselineCorrection(dtime2, hnn, lowcut, highcut,2, 
                                                                    2023 );
        adapt2 = new AdaptiveBaselineCorrection(dtime2, hn2, lowcut, highcut,2, 
                                                                    1227 );
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        int next = 0;
        Path name = Paths.get( adapttest );
        TextFileReader infile = new TextFileReader( name.toFile() );
        fileContents = infile.readInTextFile();
        hnn = new double[fileContents.length];
        System.out.println("hnn filecontents: " + fileContents.length);
        for (String num : fileContents) {
            hnn[next++] = Double.parseDouble(num);
        }
        name = Paths.get( hn2test );
        infile = new TextFileReader( name.toFile() );
        fileContents = infile.readInTextFile();
        hn2 = new double[fileContents.length];
        next = 0;
        System.out.println("hn2 filecontents: " + fileContents.length);
        for (String num : fileContents) {
            hn2[next++] = Double.parseDouble(num);
        }
        try {
            PrismXMLReader xml = new PrismXMLReader();
            xml.readFile("D:/PRISM/config_files/prism_config.xml");
        } catch (ParserConfigurationException | SAXException | IOException err) {
            System.out.println("config read error");
        }
    }
//    @Test
//    public void checkRangesTest() {
//        int[] check = adapt.getConfigRanges();
//        org.junit.Assert.assertEquals(DEFAULT_NUM_BREAKS_LOWER,check[0]);
//        org.junit.Assert.assertEquals(DEFAULT_NUM_BREAKS_LOWER,check[1]);
//        org.junit.Assert.assertEquals(DEFAULT_SPLINE_ORDER_UPPER,check[2]);
//        org.junit.Assert.assertEquals(DEFAULT_SPLINE_ORDER_UPPER,check[3]);
//        org.junit.Assert.assertEquals(DEFAULT_1ST_POLY_ORD_LOWER,check[4]);
//        org.junit.Assert.assertEquals(DEFAULT_1ST_POLY_ORD_LOWER,check[5]);
//        org.junit.Assert.assertEquals(DEFAULT_2ND_POLY_ORD_LOWER,check[6]);
//        org.junit.Assert.assertEquals(DEFAULT_2ND_POLY_ORD_LOWER,check[7]);
//    }
//    @Test
//    public void makeBreaksTest() {
//        int[] breaks = adapt.makeBreaks(50, 100, 10);
//        org.junit.Assert.assertEquals(0,breaks[0]);
//        org.junit.Assert.assertEquals(5,breaks[1]);
//        org.junit.Assert.assertEquals(10,breaks[2]);
//        org.junit.Assert.assertEquals(15,breaks[3]);
//        org.junit.Assert.assertEquals(20,breaks[4]);
//        org.junit.Assert.assertEquals(25,breaks[5]);
//        org.junit.Assert.assertEquals(30,breaks[6]);
//        org.junit.Assert.assertEquals(35,breaks[7]);
//        org.junit.Assert.assertEquals(40,breaks[8]);
//        org.junit.Assert.assertEquals(45,breaks[9]);
//        org.junit.Assert.assertEquals(50,breaks[10]);
//    }
    @Test
    public void adaptiveBaselineTest() throws SmException {
        FilterCutOffThresholds threshold = new FilterCutOffThresholds();
        threshold.SelectMagAndThresholds(mag, noval, noval, noval, noval);
        double lowcut = threshold.getLowCutOff();
        double highcut = threshold.getHighCutOff();
        
        double[] result = adapt.makeCorrection( hnn, break1, break2,degreeP1, degreeP2 );
        double[] bnn = adapt.getBaselineFunction();
        double[] rms = adapt.getRMSvalues();
        
        ButterworthFilter filter = new ButterworthFilter();
        filter.calculateCoefficients(lowcut,highcut,dtime2,2,true);
        filter.applyFilter(result, break1);
        //remove any mean value
        ArrayStats velmean = new ArrayStats( result );
        ArrayOps.removeValue(result, velmean.getMean());
        
        TextFileWriter textsm2 = new TextFileWriter( "D:/PRISM/adaptive_test/junit4", "baseline_smooth.txt", bnn);
        try {
            textsm2.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        TextFileWriter textout = new TextFileWriter( "D:/PRISM/adaptive_test/junit4", "velocity1.txt", result);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        System.out.println("rms1: " + rms[0] + " rms2: " + rms[1] + " rms3: " + rms[2]);
    }
    @Test
    public void ABC2Test() throws SmException {
        FilterCutOffThresholds threshold = new FilterCutOffThresholds();
        threshold.SelectMagAndThresholds(mag, noval, noval, noval, noval);
        double lowcut = threshold.getLowCutOff();
        double highcut = threshold.getHighCutOff();
        double[] result = adapt2.makeCorrection( hn2, breakh1, breakh2,degreeP1, degreeP2 );
        double[] bnn = adapt2.getBaselineFunction();
        double[] rms = adapt2.getRMSvalues();
        
        ButterworthFilter filter = new ButterworthFilter();
        filter.calculateCoefficients(lowcut,highcut,dtime2,2,true);
        filter.applyFilter(result, breakh1);
        //remove any mean value
        ArrayStats velmean = new ArrayStats( result );
        ArrayOps.removeValue(result, velmean.getMean());
        
        TextFileWriter textsm2 = new TextFileWriter( "D:/PRISM/adaptive_test/junit4", "baseline_smooth_p3.txt", bnn);
        try {
            textsm2.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        TextFileWriter textout = new TextFileWriter( "D:/PRISM/adaptive_test/junit4", "velocity_ABC_p3.txt", result);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        System.out.println("rms1: " + rms[0] + " rms2: " + rms[1] + " rms3: " + rms[2]);
    }
//    @Test
//    public void test2DimSort() {
//        double[] test = new double{0.5,0.02,1.6};
//    }
}
