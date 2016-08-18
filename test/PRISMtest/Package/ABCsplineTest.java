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
import SmProcessing.ABC2;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmjones
 */
public class ABCsplineTest {
    double[] polysin;
    double[] polyline;
    double[] time;
    static double[] velocity;
    static int LENGTH = 100;
    static int break1 = 30;
    static int break2 = 60;
    static int velbreak1 = 3325;
    static int velbreak2 = 4325;
    ABC2 adapt;
    static String[] filecontents;
    static String dirname = "C:\\Users\\jmjones\\Documents\\prism_2016_08_17\\spline";
    static String velfile = "C:\\Users\\jmjones\\Documents\\prism_2016_08_17\\spline\\NP1798_Velocity.txt";
    
    public ABCsplineTest() throws SmException {
        polysin = new double[LENGTH];
        polyline = new double[LENGTH];
        time = new double[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            time[i] = i;
            polysin[i] = Math.sin((double)i);
            polyline[i] = polysin[i] + (0.1 * i);
        }
        adapt = new ABC2(1.0,polysin,polysin, 0.1,30.0,8,10,10.0);
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
    
    @Before
    public void setUp() {
    }

    @Test
    public void splineWang() throws SmException, IOException {
        TextFileWriter writeout = new TextFileWriter(dirname, "polysin.txt",polysin);
        writeout.writeOutArray();
        double[] result = new double[LENGTH];
        System.arraycopy( polysin,0,result,0,LENGTH);
        for (int i = break1+1; i < break2; i++) {result[i] = 0.0;}
        writeout = new TextFileWriter(dirname, "polysinZeroed.txt",result);
        writeout.writeOutArray();
        adapt.getSplineSmooth(result, break1, break2, 1);
        writeout = new TextFileWriter(dirname, "polysinSpline.txt",result);
        writeout.writeOutArray();
    }
    @Test
    public void splineApache() throws SmException, IOException {
        double[] result = new double[velocity.length];
        System.arraycopy( velocity,0,result,0,velocity.length);
        adapt.connectSegmentsWithSpline(velocity, velbreak1, velbreak2, result, 0.01);
        TextFileWriter writeout = new TextFileWriter(dirname, "polysinSplineApache.txt",result);
        writeout.writeOutArray();
    }
}
