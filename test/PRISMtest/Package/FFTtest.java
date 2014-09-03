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

import SmUtilities.TextFileReader;
import SmUtilities.TextFileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
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
public class FFTtest {
    static final String picktest = "D:/PRISM/ppicktest/15481673.AZ.FRD.HNN.txt";
    static final String filtertest = "D:/PRISM/filter_test/junit/15481673.AZ.FRD.HNN.V2.txt";
    static String[] fileContents;
    static final double deltat = 0.01;
    static double[] hnn;
    static double[] ann;
    String outdir = "D:/PRISM/filter_test/junit";
    static final int powerlength = 32768;
    
    public FFTtest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        int next = 0;
        Path name = Paths.get( picktest );
        TextFileReader infile = new TextFileReader( name.toFile() );
        fileContents = infile.readInTextFile();
        hnn = new double[fileContents.length];
        for (String num : fileContents) {
            hnn[next++] = Double.parseDouble(num);
        }
        next = 0;
        name = Paths.get( filtertest );
        infile = new TextFileReader( name.toFile() );
        fileContents = infile.readInTextFile();
        ann = new double[fileContents.length];
        for (String num : fileContents) {
            ann[next++] = Double.parseDouble(num);
        }
    }
    
     @Test
     public void checkffthnn() {
         double[] hnnpad = new double[powerlength];
         Arrays.fill(hnnpad, 0.0);
         System.arraycopy(hnn, 0, hnnpad, 0, hnn.length);
         
         FastFourierTransformer fft = new FastFourierTransformer( DftNormalization.STANDARD);
         Complex[] transfreq = fft.transform(hnnpad, TransformType.FORWARD);
         
         double[] mags = new double[powerlength];
         for (int i = 0; i < powerlength; i++) {
             mags[i]= Math.sqrt(Math.pow(transfreq[i].getReal(),2) + Math.pow(transfreq[i].getImaginary(),2));
         }
         
        TextFileWriter textout = new TextFileWriter( outdir, "15481673.AZ.FRD.HNN.FFTto exvals.txt", mags);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
        }
     }
    
     @Test
     public void checkfftann() {
         double[] annpad = new double[powerlength];
         Arrays.fill(annpad, 0.0);
         System.arraycopy(ann, 0, annpad, 0, hnn.length);
         
         FastFourierTransformer fft = new FastFourierTransformer( DftNormalization.STANDARD);
         Complex[] transfreq = fft.transform(annpad, TransformType.FORWARD);
         
         double[] mags = new double[powerlength];
         for (int i = 0; i < powerlength; i++) {
             mags[i]= Math.sqrt(Math.pow(transfreq[i].getReal(),2) + Math.pow(transfreq[i].getImaginary(),2));
         }
         
        TextFileWriter textout = new TextFileWriter( outdir, "15481673.AZ.FRD.HNN.FFTforV2accvalx.txt", mags);
        try {
            textout.writeOutArray();
        } catch (IOException err) {
        }
     }
}
