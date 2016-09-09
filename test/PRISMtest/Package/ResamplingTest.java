/*
 * Copyright (C) 2016 jmjones
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

import SmException.SmException;
import SmProcessing.Resampling;
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
public class ResamplingTest {

    static String data = "/PRISMtest/Data/resample_in.txt";
    static String results = "/PRISMtest/Data/resample_out.txt";
    static double[] yarray;
    static double[] yparray;
    static String[] filecontents;
    static int SPS = 100;
    static double EPSILON = 0.000001;
    
    public ResamplingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, URISyntaxException {
        TextFileReader infile;
        int next = 0;
        File name;
        
        URL url = EventOnsetDetectionTest.class.getResource(data);
        if (url != null) {
            name = new File(url.toURI());
            infile = new TextFileReader( name );
            filecontents = infile.readInTextFile();
            yarray = new double[filecontents.length];
            for (String num : filecontents) {
                yarray[next++] = Double.parseDouble(num);
            }
        }
        next = 0;
        url = EventOnsetDetectionTest.class.getResource(results);
        if (url != null) {
            name = new File(url.toURI());
            infile = new TextFileReader( name );
            filecontents = infile.readInTextFile();
            yparray = new double[filecontents.length];
            for (String num : filecontents) {
                yparray[next++] = Double.parseDouble(num);
            }
        }
    }

    @Test
    public void testNeedsResampling() {
        Resampling resamp = new Resampling();
        org.junit.Assert.assertEquals(true,resamp.needsResampling(100));
        org.junit.Assert.assertEquals(false,resamp.needsResampling(200));
    }
    @Test
    public void testCalcResamplingRate() {
        Resampling resamp = new Resampling();
        org.junit.Assert.assertEquals(200,resamp.calcNewSamplingRate(100));
        org.junit.Assert.assertEquals(2,resamp.getFactor());
        org.junit.Assert.assertEquals(200,resamp.getNewSamplingRate());
        org.junit.Assert.assertEquals(-1,resamp.calcNewSamplingRate(200));
        org.junit.Assert.assertEquals(-1,resamp.calcNewSamplingRate(0));
    }
    @Test
    public void testResampleArray() throws SmException {
        Resampling resamp = new Resampling();
        double[] yptest = resamp.resampleArray(yarray, SPS);
        org.junit.Assert.assertArrayEquals(yptest, yparray, EPSILON);
    }
}
