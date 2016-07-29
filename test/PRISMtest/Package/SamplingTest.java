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

import SmProcessing.ArrayOps;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmjones
 */
public class SamplingTest {
    
    static String accelfile = "D:\\PRISM\\sampling_update\\accraw.txt";
    static String testname = "D:\\PRISM\\sampling_update";
//    static String dirname = "D:\\PRISM\\sampling_update\\sampling";
//    static String testname = "D:\\PRISM\\sampling_update\\sampling\\testvals";
//    static String testarr = "D:\\PRISM\\sampling_update\\sampling\\testvals\\just10vals.txt";
    static double epsilon = 0.000001;
    static double[] accel;
    static double[] testvals;
    static String[] filecontents;
    double[] velocity;
    double[] accelnosamp;
    double[] accelsamp;
    double[] velocitysamp;
    double[] accelaftersamp;
    double[] accelfinal;
    
    public SamplingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        File name;
        TextFileReader infile;
        int next = 0;
        
        Path pathname = Paths.get(accelfile);
        name = pathname.toFile();
        infile = new TextFileReader( name );
        filecontents = infile.readInTextFile();
        accel = new double[filecontents.length];
        for (String num : filecontents) {
            accel[next++] = Double.parseDouble(num);
        }
//        next = 0;
//        pathname = Paths.get(testarr);
//        name = pathname.toFile();
//        infile = new TextFileReader( name );
//        filecontents = infile.readInTextFile();
//        testvals = new double[filecontents.length];
//        for (String num : filecontents) {
//            testvals[next++] = Double.parseDouble(num);
//        }
    }
    @Test
    public void test4sample() throws IOException {
        double[] sampvals;
        sampvals = ArrayOps.resampleArray(accel, 4);
        TextFileWriter writeout = new TextFileWriter(testname, "sampled4vals.txt",sampvals);
        writeout.writeOutArray();
        double[] checkarr;
        checkarr = ArrayOps.unsampleArray( sampvals, accel.length, 4);
        org.junit.Assert.assertArrayEquals(accel, checkarr, epsilon);
    }
    @Test
    public void test2sample() throws IOException {
        double[] sampvals;
        sampvals = ArrayOps.resampleArray(accel, 2);
        TextFileWriter writeout = new TextFileWriter(testname, "sampled2vals.txt",sampvals);
        writeout.writeOutArray();
        double[] checkarr;
        checkarr = ArrayOps.unsampleArray( sampvals, accel.length, 2);
        org.junit.Assert.assertArrayEquals(accel, checkarr, epsilon);
    }
    @Test
    public void test3sample() throws IOException {
        double[] sampvals;
        sampvals = ArrayOps.resampleArray(accel, 3);
        TextFileWriter writeout = new TextFileWriter(testname, "sampled3vals.txt",sampvals);
        writeout.writeOutArray();
        double[] checkarr;
        checkarr = ArrayOps.unsampleArray( sampvals, accel.length, 3);
        org.junit.Assert.assertArrayEquals(accel, checkarr, epsilon);
    }
    @Test
    public void testnoarray() {
        double[] sampvals;
        double[] invals = null;
        //check for null input array in sampling
        sampvals = ArrayOps.resampleArray(invals, 4);
        org.junit.Assert.assertEquals(sampvals.length,0);
        //check for null input array in unsampling
        sampvals = ArrayOps.unsampleArray(sampvals, 10, 3);
        org.junit.Assert.assertEquals(sampvals.length,0);
    }
    @Test
    public void testemptyvals() throws IOException {
        double[] sampvals;
        double[] invals = new double[0];
        //check for empty array in sampling
        sampvals = ArrayOps.resampleArray(invals, 4);
        org.junit.Assert.assertEquals(sampvals.length,0);
        //check for invalid input array length in unsampling
        invals = new double[5];
        sampvals = ArrayOps.unsampleArray(sampvals, 10, 3);
        org.junit.Assert.assertEquals(sampvals.length,0);
    }
    @Test
    public void testinvalidsamp() throws IOException {
        double[] sampvals;
        //test for invalid sampling rate in sampling code
        sampvals = ArrayOps.resampleArray(testvals, 1);
        org.junit.Assert.assertEquals(sampvals.length,0);
        //check for invalid sampling rate in unsampling
        sampvals = ArrayOps.unsampleArray(sampvals, 10, 1);
        org.junit.Assert.assertEquals(sampvals.length,0);
        //check for invalid original array length in unsampling
        sampvals = ArrayOps.unsampleArray(sampvals, 2, 1);
        org.junit.Assert.assertEquals(sampvals.length,0);
    }
}
