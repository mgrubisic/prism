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
public class filterCoefficientsTest {
    ButterworthFilter filter = new ButterworthFilter();
    double highcutoff;
    double dtime;
    double taperlength;
    double[] acc;
    double[] lowcutrange = {0.1, 0.3, 0.5};
    
    public filterCoefficientsTest() {
        highcutoff = 20.0;
        dtime = 0.01;
        taperlength = 2.0;
        acc = new double[1000];
        Arrays.fill(acc,2.0);
    }
    
    @Test
    public void testPadLength() {
        int calcSec;
        boolean valid;
        int padlength;
        int taplength;
        double[] b1_coefs;
        double[] b2_coefs;
        double[] fact;
        for ( double lowcutoff : lowcutrange ) {
            System.out.println("lowcutoff: " + lowcutoff);
            for (int numroll = 1; numroll < 9; numroll++) {
                valid = filter.calculateCoefficients(lowcutoff, highcutoff, 
                                                                dtime, numroll, true);
                if (valid) {
                    calcSec = (int)(taperlength * dtime);
                    filter.applyFilter(acc, taperlength, calcSec);
                    padlength = filter.getPadLength();
                    b1_coefs = filter.getB1();
                    b2_coefs = filter.getB2();
                    fact = filter.getFact();
                    System.out.println("pad length: " + padlength);
                    System.out.println("numroll: " + numroll);
                    for (int j = 0; j < b1_coefs.length; j++) {
                        System.out.println("   b1: " + b1_coefs[j] + "   b2: " + 
                                            b2_coefs[j] + "   fact: " + fact[j]);                        
                    }
                } else {
                    System.out.println("invalid filter coef parms");
                }
            }
        }
    }
}
