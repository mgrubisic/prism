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

import static SmConstants.VFileConstants.NUM_T_PERIODS;
import SmException.FormatException;
import SmProcessing.SpectraResources;
import SmUtilities.TextFileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class SpectraResourcesTest {
    SpectraResources spec;
    Path dirpath;
    String[] PeriodsText;
    Double EPSILON = .000001;
    static final String[] spectraFileNames =  { "CoefTable_50_0.txt",
                                                "CoefTable_50_0.02.txt",
                                                "CoefTable_50_0.05.txt",
                                                "CoefTable_50_0.1.txt",
                                                "CoefTable_50_0.2.txt",
                                                "CoefTable_100_0.txt",
                                                "CoefTable_100_0.02.txt",
                                                "CoefTable_100_0.05.txt",
                                                "CoefTable_100_0.1.txt",
                                                "CoefTable_100_0.2.txt",
                                                "CoefTable_200_0.txt",
                                                "CoefTable_200_0.02.txt",
                                                "CoefTable_200_0.05.txt",
                                                "CoefTable_200_0.1.txt",
                                                "CoefTable_200_0.2.txt" };
    String[][] coeftext;
    
    public SpectraResourcesTest() throws IOException {
        spec = new SpectraResources();
    }
    @Before
    public void setUp() throws IOException {
        Path relpath = Paths.get("");
        String abspath = relpath.toAbsolutePath().toString();
        System.out.println("default path: " + abspath);
        dirpath = Paths.get(abspath, "src/SmProcessing/spectra");
        System.out.println("spectra path: " + dirpath);
        Path periodname = Paths.get(dirpath.toString(), "T_periods.txt");
        TextFileReader reader = new TextFileReader(periodname.toFile());
        PeriodsText = reader.readInTextFile();
        Path coefname;
        coeftext = new String[spectraFileNames.length][];
        for (int i = 0; i < spectraFileNames.length; i++) {
            coefname = Paths.get(dirpath.toString(), spectraFileNames[i]);
            reader = new TextFileReader(coefname.toFile());
            coeftext[i] = reader.readInTextFile();
        }
    }
    @Test
    public void checkTPeriodText() {
        String[] ttext = spec.getTPeriodsText();
        for (int i = 0; i < ttext.length; i++) {
            org.junit.Assert.assertEquals(PeriodsText[i], ttext[i]);
        }
    }
    @Test
    public void checkTPeriodVals() throws FormatException {
        double[] tvals = spec.getTperiods();
        double testval;
        for (int i = 0; i < tvals.length; i++) {
            testval = Double.parseDouble(PeriodsText[i]);
            org.junit.Assert.assertEquals(testval, tvals[i], EPSILON);
        }
    }
    @Test
    public void checkCoefsText() {
        String[][] ctext = spec.getCoefsText();
        for (int i = 0; i < ctext.length; i++) {
            for (int j = 0; j < NUM_T_PERIODS; j++) {
                org.junit.Assert.assertEquals(coeftext[i][j],ctext[i][j]);
            }
        }
    }
}
