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

import SmProcessing.ButterworthFilter;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class ButterworthFilterTest {
    double EPSILON = 0.2;
    double[] sin20;
    double[] sin40;
    double[] mix;
    double pi = Math.PI;
    double d005 = 0.005;
    double d010 = 0.01;
    double d020 = 0.02;
    ButterworthFilter filter005 = new ButterworthFilter();
    int length = 10000;
    double taper = 0.25;
    int taper005 = 2 * (int)(taper / d005);
    
    String outdir = "D:/PRISM/filter_test/junit";
    
    public ButterworthFilterTest() {
        sin20 = new double[length];
        sin40 = new double[length];
        mix = new double[length];
    }
    
    @Before
    public void setUp() throws IOException {
        for (int i = 0; i < sin20.length; i++) {
            sin20[i] = Math.sin( 0.3306 * i);
            sin40[i] = Math.sin( 0.6612 * i);
            mix[i] = sin20[i] + sin40[i];
        }
        for (int i = 0; i < 100; i++ ) {
            sin20[i] = 0.0;
            mix[i] = 0.0;
            sin20[length-1-i] = 0.0;
            mix[length-1-i] = 0.0;
        }
        writeOutArray( sin20, outdir, "sin20.txt");
        writeOutArray( mix, outdir, "mixbefore.txt");
//        filter005.applyCosineTaper(sin20, taper005);
    }
    
    public void writeOutArray( double[] array, String dir, String outname ) throws IOException {
        int len = array.length;
        String[] text = new String[ len ];
        for (int i = 0; i < len; i++) {
            text[i] = Double.toString(array[i]);
        }
        Path filename = Paths.get(dir, outname);
        TextFileWriter textout = new TextFileWriter(filename, text);
        textout.writeOutToFile();
    }

     @Test
     public void testApplyFilter() throws IOException {
        boolean valid = filter005.calculateCoefficients(0.1, 30.0, 0.005, 2, true);
        org.junit.Assert.assertEquals( valid, true);
        filter005.applyFilter(mix, 0.25);
        writeOutArray( mix, outdir, "mixafter.txt");
        org.junit.Assert.assertArrayEquals(sin20, mix, EPSILON);
     }

     @Test
     public void testJustSin() throws IOException {
        boolean valid = filter005.calculateCoefficients(0.1, 30.0, 0.005, 2, true);
        double[] hold = Arrays.copyOf(sin20, length);
        filter005.applyFilter(sin20, 0.25);
        writeOutArray( sin20, outdir, "sin20after.txt");
        org.junit.Assert.assertArrayEquals(hold, sin20, EPSILON);
     }
}
