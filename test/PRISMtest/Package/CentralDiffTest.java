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
public class CentralDiffTest {
    
    static String dirname = "D:\\PRISM\\source_testbed\\central_diff";
    static String velfile = "D:\\PRISM\\source_testbed\\central_diff\\velocity.txt";
    static double[] velocity;
    double[] accel3;
    double[] accel5;
    double[] accel7;
    double[] accel9;
    static String[] filecontents;
    static int LENGTH = 1000;
    static double DT = 0.01;
    
    public CentralDiffTest() throws IOException {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        File name;
        TextFileReader infile;
        int next = 0;
        
        Path pathname = Paths.get(velfile);
        name = pathname.toFile();
        infile = new TextFileReader( name );
        filecontents = infile.readInTextFile();
        velocity = new double[filecontents.length];
        for (String num : filecontents) {
            velocity[next++] = Double.parseDouble(num);
        }
    }
    @Test
    public void diff_accel3() throws IOException {
    
        accel3 = ArrayOps.central_diff(velocity, DT, 3);
        TextFileWriter writeout = new TextFileWriter(dirname, "accel3.txt",accel3);
        writeout.writeOutArray();
    }
    @Test
    public void diff_accel5() throws IOException {
    
        accel5 = ArrayOps.central_diff(velocity, DT, 5);
        TextFileWriter writeout = new TextFileWriter(dirname, "accel5.txt",accel5);
        writeout.writeOutArray();
    }
    @Test
    public void diff_accel7() throws IOException {
    
        accel7 = ArrayOps.central_diff(velocity, DT, 7);
        TextFileWriter writeout = new TextFileWriter(dirname, "accel7.txt",accel7);
        writeout.writeOutArray();
    }
    @Test
    public void diff_accel9() throws IOException {
    
        accel9 = ArrayOps.central_diff(velocity, DT, 9);
        TextFileWriter writeout = new TextFileWriter(dirname, "accel9.txt",accel9);
        writeout.writeOutArray();
    }
}
