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

import SmProcessing.AdaptiveBaselineCorrection;
import SmProcessing.ArrayOps;
import static SmProcessing.ArrayOps.makeTimeArray;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class AdaptiveBaselineTest {
    static final String splinetest = "D:/PRISM/matlab_code/PRISM_V2/adaptiveBaseline/vel.txt";
    static String[] fileContents;
    
    static final int NUM_BREAKS = 10;
    
    double deltat = 0.005;
    double[] time;
    static double[] hnn;
    AdaptiveBaselineCorrection adapt;
    
    int break1 = 2023;
//    int break2 = 4636;
    int break2 = 7038;
    
    String outdir = "D:/PRISM/adaptive_test/junit2";
    int degreeL = 1;
    int degreeP1 = 2;
    int degreeP2 = 2;
    int degreeS = 3;
    
    public AdaptiveBaselineTest() {
        System.out.println("in constructor");
        time = ArrayOps.makeTimeArray(deltat, hnn.length);
        adapt = new AdaptiveBaselineCorrection(deltat, NUM_BREAKS, degreeP1,
                                                                degreeP2, degreeS);
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
    }
        
    @Test
    public void adaptiveBaselineTest() {
        adapt.makeCorrection(hnn, break1, break2, 1);
        double[] result = adapt.getBaselineCorrectedArray();
        double[] bnn = adapt.getBaselineFunction();
        double[] rms = adapt.getRMSvalues();
        
        TextFileWriter textsm2 = new TextFileWriter( "D:/PRISM/adaptive_test/junit2", "baseline_smooth.txt", bnn);
        try {
            textsm2.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        TextFileWriter textout = new TextFileWriter( "D:/PRISM/adaptive_test/junit2", "velocity.txt", result);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
            System.out.println("Error printing out result in MathSplineTest");
        }
        System.out.println("rms1: " + rms[0] + " rms2: " + rms[1] + " rms3: " + rms[2]);
    }
}
