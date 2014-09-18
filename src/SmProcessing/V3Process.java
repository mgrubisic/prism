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

import COSMOSformat.V2Component;
import static SmConstants.VFileConstants.NUM_T_PERIODS;
import static SmConstants.VFileConstants.NUM_V3_ARRAYS;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
/**
 *
 * @author jmjones
 */
public class V3Process {
    private double[][] spectra;
    private double[] T_periods;
    
    public V3Process(final V2Component v2acc, final V2Component v2vel,
                                                    final V2Component v2dis) throws IOException {
        spectra = new double[NUM_V3_ARRAYS][NUM_T_PERIODS];
        
        
        //Read in period table as resource
        String spectraValuesPath = "spectra/T_periods.txt";
        InputStream stream = V3Process.class.getResourceAsStream(spectraValuesPath);
        String nextLine;
        ArrayList<String> tempfile = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            while ((nextLine = reader.readLine()) != null) {
                tempfile.add(nextLine);
            }
        }
        T_periods = new double[tempfile.size()];
        if (tempfile.size() > 0){
            int next = 0;
            for (String each: tempfile){
                T_periods[next++] = Double.parseDouble(each);
            }
        } else {
            throw new IOException("Unable to find spectra periods text file");
        }
//        System.out.println("T_periods:");
//        for (double each : T_periods) {
//            System.out.println(each);
//        }
        
    }
    
    public void processV3Data() {
        //
//        System.out.println("V3 processing");
    }
    public double[][] getV3Array() {
        return spectra;
    }
    public int getV3ArrayLength() {
        return spectra.length;
    }
}
