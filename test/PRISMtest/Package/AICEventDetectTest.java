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

import SmProcessing.AICEventDetect;
import SmProcessing.ArrayStats;
import SmUtilities.TextFileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class AICEventDetectTest {
    final double EPSILON = 0.001;
    AICEventDetect aicPeak;
    AICEventDetect aicWhole;
    
    static final String picktest = "D:/PRISM/ppicktest/15481673.AZ.FRD.HNN.txt";
    static String[] fileContents;
    
    static double[] hnn;    
    String outdir = "D:/PRISM/filter_test/junit";
    
    public AICEventDetectTest() {
        aicPeak = new AICEventDetect();
        aicWhole = new AICEventDetect();
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
    
     @Test
     public void checkEventDetection() {
         int pick1 = aicPeak.calculateIndex(hnn, "topeak");
//         int pick2 = aicPeak.calculateIndex(hnn, "whole");
         org.junit.Assert.assertEquals(1472, pick1);
//         org.junit.Assert.assertEquals(1572, pick2);
     }
}
