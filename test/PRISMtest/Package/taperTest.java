/*
 * Copyright (C) 2015 jmjones
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
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class taperTest {
    double[] test;
    double[] orig;
    ButterworthFilter filt;
    String dirname = "D:\\PRISM\\test\\taper";
    
    public taperTest() {
        test = new double[200];
        Arrays.fill(test,2.0);
        orig = new double[200];
        filt = new ButterworthFilter();
        Arrays.fill(orig,2.0);
    }
    
    @Test
    public void testTaper() throws IOException {
        filt.applyCosineTaper(test, 60);
        TextFileWriter writeout = new TextFileWriter(dirname, "tapered.txt",test);
        writeout.writeOutArray();
        writeout = new TextFileWriter(dirname, "original.txt",orig);
        writeout.writeOutArray();
    }
}
