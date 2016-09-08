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
import SmProcessing.ResampSinc;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class ResampSincTest {
    static String yfile = "D:\\PRISM\\source_testbed\\resampsinc\\y.txt";
    static String tongafile = "D:\\PRISM\\source_testbed\\test\\resampling\\Tonga_HNZ.txt";
    static String testloc = "D:\\PRISM\\source_testbed\\test\\resampsinc";
    static String chirpfile = "D:\\PRISM\\source_testbed\\resampling\\chirp_100sps_analytical_1024.txt";
    static String chirploc = "D:\\PRISM\\source_testbed\\test\\resampsinc";
    static double[] yarray;
    static double[] tonga;
    static double[] chirp;
    static String[] filecontents;
    static int SPS = 100;
    
    public ResampSincTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        File name;
        TextFileReader infile;
        int next = 0;
        
        Path pathname = Paths.get(yfile);
        name = pathname.toFile();
        infile = new TextFileReader( name );
        filecontents = infile.readInTextFile();
        yarray = new double[filecontents.length];
        for (String num : filecontents) {
            yarray[next++] = Double.parseDouble(num);
        }
        next = 0;
        pathname = Paths.get(tongafile);
        name = pathname.toFile();
        infile = new TextFileReader( name );
        filecontents = infile.readInTextFile();
        tonga = new double[filecontents.length];
        for (String num : filecontents) {
            tonga[next++] = Double.parseDouble(num);
        }
        next = 0;
        pathname = Paths.get(chirpfile);
        name = pathname.toFile();
        infile = new TextFileReader( name );
        filecontents = infile.readInTextFile();
        chirp = new double[filecontents.length];
        for (String num : filecontents) {
            chirp[next++] = Double.parseDouble(num);
        }
    }
    @Test
    public void testNeedsResampling() {
        ResampSinc resamp = new ResampSinc();
        org.junit.Assert.assertEquals(true,resamp.needsResampling(100));
        org.junit.Assert.assertEquals(false,resamp.needsResampling(200));
    }
    @Test
    public void testCalcResamplingRate() {
        ResampSinc resamp = new ResampSinc();
        org.junit.Assert.assertEquals(200,resamp.calcNewSamplingRate(100));
        org.junit.Assert.assertEquals(2,resamp.getFactor());
        org.junit.Assert.assertEquals(200,resamp.getNewSamplingRate());
        org.junit.Assert.assertEquals(-1,resamp.calcNewSamplingRate(200));
        org.junit.Assert.assertEquals(-1,resamp.calcNewSamplingRate(0));
    }
    @Test
    public void testResampleArray() throws SmException {
        ResampSinc resamp = new ResampSinc();
        double[] yparray = resamp.resample(yarray, SPS);
        System.out.println("y array");
        for (double val: yarray) {
            System.out.println(val);
        }
        System.out.println("yp array");
        for (double val: yparray) {
            System.out.println(val);
        }
        double[] timenew = resamp.getNewStepArray();
        System.out.println("time new");
        for (double val: timenew) {
            System.out.println(val);
        }        
    }
//    @Test
//    public void testResampleArrayV1() throws SmException, IOException {
//        ResampSinc resamp = new ResampSinc();
//        double[] tongap = resamp.resample(tonga, SPS);
//        TextFileWriter writeout = new TextFileWriter(testloc, "Tonga_HNZ_200.txt",tongap);
//        writeout.writeOutArray();
//    }
//    @Test
//    public void testResampleChirp() throws SmException, IOException {
//        ResampSinc resamp = new ResampSinc();
//        double[] chirpout = resamp.resample(chirp, SPS);
//        TextFileWriter writeout = new TextFileWriter(testloc, "chirp_200.txt",chirpout);
//        writeout.writeOutArray();
//    }
}
