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

/**
 *
 * @author jmjones
 */
public class RawTraceConversion {
    
    private RawTraceConversion(){
    }
    public static double countToG(final double inlsb, final double inSensitivity) {
        double MICRO_TO_VOLT = 1.0e-6;
        double result = ( inlsb * MICRO_TO_VOLT) / inSensitivity;
        return result;        
    }
    
    //sensor calculation of volts per count and cm per sq. sec per volt
    //countToCMS units are cm per sq. sec per count
    //This is multiplied by each count to get the sensor value in cm per sq. sec
    public static double countToCMS(final double inlsb, final double inSensitivity,
                                        double conversion) {
        double MICRO_TO_VOLT = 1.0e-6;
        double result = (( inlsb * MICRO_TO_VOLT) / inSensitivity) * conversion;
        return result;
    }
    
}
