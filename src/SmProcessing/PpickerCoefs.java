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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jmjones
 */
public class PpickerCoefs {

    private static final int DELTAT_TO_INT = 10000;
    
    private final Map<Integer, double[]> AeB;
    private final Map<Integer, double[]> Ae;
    
    private final double[] aeb_01 = {-5.9231919e-05, 
                                      4.7394384e-02};
    
    private final double[] aeb_02 = {-3.4101151e-06, 
                                      8.6115879e-04};
    
    private final double[] aeb_005 = {1.5608976e-04, 
                                     -2.3877377e-01};
    
    private final double[] ae_01 = {1.0177871e-02, 
                                    2.3383825e+01, 
                                    2.5072487e-06, 
                                   -5.9231919e-05};
    
    private final double[] ae_02 = {-1.2814798e-03, 
                                     1.3462595e+00, 
                                     2.5362756e-06, 
                                    -3.4101151e-06};
    
    private final double[] ae_005 = {-1.4069967e-01, 
                                     -6.1621769e+01, 
                                      2.8894260e-06, 
                                      1.5608976e-04};
    
    public PpickerCoefs() {
        AeB = new HashMap<>();
        AeB.put(100, aeb_01);
        AeB.put(200, aeb_02);
        AeB.put(50, aeb_005);
        
        Ae = new HashMap<>();
        Ae.put(100, ae_01);
        Ae.put(200, ae_02);
        Ae.put(50, ae_005);
    }
    
    public double[] getAeBCoefs(double deltaT) {
        int key = (int)(deltaT * DELTAT_TO_INT);
        System.out.println("+++ AeB key: " + key);
        return AeB.get(key);
    }
    
    public double[] getAeCoefs(double deltaT) {
        int key = (int)(deltaT * DELTAT_TO_INT);
        System.out.println("+++ AeB key: " + key);
        return Ae.get(key);
    }
}
