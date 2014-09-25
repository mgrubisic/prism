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

package SmProcessing;

import static SmConstants.VFileConstants.NUM_COEF_VALS;
import static SmConstants.VFileConstants.NUM_T_PERIODS;
import static SmConstants.VFileConstants.V3_DAMPING_VALUES;
import static SmConstants.VFileConstants.V3_SAMPLING_RATES;
import SmException.FormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class SpectraResources {
    private boolean initialized = false;
    private static final double EPSILON = 0.001;
    private static final String periodFileName = "spectra/T_periods.txt";
    private static final String[] spectraFileNames = 
                                              { "spectra/CoefTable_50_0.txt",
                                                "spectra/CoefTable_50_0.02.txt",
                                                "spectra/CoefTable_50_0.05.txt",
                                                "spectra/CoefTable_50_0.1.txt",
                                                "spectra/CoefTable_50_0.2.txt",
                                                "spectra/CoefTable_100_0.txt",
                                                "spectra/CoefTable_100_0.02.txt",
                                                "spectra/CoefTable_100_0.05.txt",
                                                "spectra/CoefTable_100_0.1.txt",
                                                "spectra/CoefTable_100_0.2.txt",
                                                "spectra/CoefTable_200_0.txt",
                                                "spectra/CoefTable_200_0.02.txt",
                                                "spectra/CoefTable_200_0.05.txt",
                                                "spectra/CoefTable_200_0.1.txt",
                                                "spectra/CoefTable_200_0.2.txt" };
    private String[] T_periods;
    private String[][] coefs;
    
    public SpectraResources() throws IOException {
        //Note that this is not considered thread-safe
        if (!initialized) {
            coefs = new String[spectraFileNames.length][NUM_T_PERIODS];
            //Read in resource tables with periods and coefficients
            T_periods = readInResource(periodFileName);
            for (int i = 0; i < spectraFileNames.length; i++) {
                coefs[i] = readInResource( spectraFileNames[i]);
            }
            initialized = true;
        }
    }
    private String[] readInResource( String inName ) throws IOException {
        InputStream stream = SpectraResources.class.getResourceAsStream(inName);
        String nextLine;
        
        ArrayList<String> tempfile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            while ((nextLine = reader.readLine()) != null) {
                tempfile.add(nextLine);
            }
        }
        String[] outarray = new String[tempfile.size()];
        outarray = tempfile.toArray(outarray);
        tempfile.clear();
        return outarray;
    }
    public final double[] getTperiods() throws FormatException {
        int len = T_periods.length;
        double[] tout = new double[len];
        try {
            for (int i = 0; i < len; i++) {
                tout[i] = Double.parseDouble(T_periods[i]);
            }
        } catch (NumberFormatException err) {
            throw new FormatException("Unable to parse spectra periods file");
        }
        return tout;
    }
    public final double[][] getCoefArray( double samppersec, double damping) 
                                                        throws FormatException {
        int index;
        int samp = 0;
        int damp = 0;
        for (int i = 0; i < V3_SAMPLING_RATES.length; i++) {
            if (Math.abs(samppersec-V3_SAMPLING_RATES[i]) < EPSILON) {
                samp = i;
            }
        }
        for (int i = 0; i < V3_DAMPING_VALUES.length; i++) {
            if (Math.abs(damping-V3_DAMPING_VALUES[i]) < EPSILON) {
                damp = i;
            }
        }
        index = (samp*5) * damp;
        return reformatCoefArray(coefs[index],NUM_COEF_VALS);
    }
    private double[][] reformatCoefArray(String[] array, int cols ) 
                                                        throws FormatException {
        double[][] outarray = new double[array.length][cols];
        String[] vals;
        try {
            for (int i = 0; i < array.length; i++) {
                vals = array[i].split(" ");
                for(int j = 0; j < cols; j++) {
                    outarray[i][j] = Double.parseDouble(vals[j]);
                }
            }
        } catch (NumberFormatException err) {
            throw new FormatException("Unable to parse spectra coefficients file");
        }
        return outarray;
    }
    public final String[] getTPeriodsText() {
        return T_periods;
    }
    public final String[][] getCoefsText() {
        return coefs;
    }
}
