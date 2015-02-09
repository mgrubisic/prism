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

import SmProcessing.ArrayOps;
import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
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
public class FirstOrderFitTest {
    String velfile = "D:\\PRISM\\smtesting\\debug\\results\\test.txt";
    String outdir = "D:\\PRISM\\smtesting\\debug\\results";
    double timestep = 1.0;
    double[] vel;
    double[] velregression;
    double[] velpolyfit;
    int len;
    double[] time;

    String[] filecontents;

    public FirstOrderFitTest() {
        len = 2000;
        filecontents = new String[len];
    }
    
    @Before
    public void setUp() throws IOException {
        time = ArrayOps.makeTimeArray(timestep, len);
        int next = 0;
        Path name = Paths.get( velfile );
        TextFileReader infile = new TextFileReader( name.toFile() );
        filecontents = infile.readInTextFile();
        vel = new double[len];
        for (String num : filecontents) {
            vel[next++] = Double.parseDouble(num);
        }
    }
    @Test
    public void testRegression() throws IOException {
        velregression = ArrayOps.findLinearTrend( vel, timestep );
        TextFileWriter outfile = new TextFileWriter( outdir, "velregression.txt", velregression);
        outfile.writeOutArray();
    }
    
    @Test
    public void testPolynomialFit() throws IOException {
        double[] coefs;
        coefs = ArrayOps.findPolynomialTrend(vel, 1, timestep );
        velpolyfit = new double[len];
        PolynomialFunction basepoly = new PolynomialFunction( coefs );
        for (int j = 0; j < len; j++) {
            velpolyfit[j] = basepoly.value(time[j]);
        }
        TextFileWriter outfile = new TextFileWriter( outdir, "velpolyfit.txt", velpolyfit);
        outfile.writeOutArray();
    }
}
