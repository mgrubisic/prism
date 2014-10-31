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
public class EventOnsetCoefs {

    private static final int DELTAT_TO_INT = 10000;
    
    private final Map<Integer, double[]> AeB;
    private final Map<Integer, double[]> Ae;
    
    private final double[] aeb_01 = { 0.00000, 
                                     -0.00004};
    
    private final double[] aeb_02 = {0.0000, 
                                     0.0000};
    
    private final double[] aeb_005 = {0.000, 
                                      0.00018};
    
    private final double[] aeb_002 = {0.0000, 
                                      0.00079};
    
    private final double[] ae_01 = {-0.00932, 
                                    -0.00004, 
                                     17.22045, 
                                     0.02357};
    
    private final double[] ae_02 = {-0.00066, 
                                    -0.00000, 
                                     0.24536, 
                                    -0.00020};
    
    private final double[] ae_005 = {-0.05590, 
                                      0.00018, 
                                    -70.09431, 
                                     -0.18977};
    
    private final double[] ae_002 = {0.55004, 
                                     0.00079, 
                                    -311.99721, 
                                    -0.04583};
    
    public EventOnsetCoefs() {
        AeB = new HashMap<>();
        AeB.put(100, aeb_01);
        AeB.put(200, aeb_02);
        AeB.put(50, aeb_005);
        AeB.put(20, aeb_002);
        
        Ae = new HashMap<>();
        Ae.put(100, ae_01);
        Ae.put(200, ae_02);
        Ae.put(50, ae_005);
        Ae.put(20, ae_002);
    }
    
    public double[] getAeBCoefs(double deltaT) {
        int key = (int)(deltaT * DELTAT_TO_INT);
        return AeB.get(key);
    }
    
    public double[] getAeCoefs(double deltaT) {
        int key = (int)(deltaT * DELTAT_TO_INT);
        return Ae.get(key);
    }
}
