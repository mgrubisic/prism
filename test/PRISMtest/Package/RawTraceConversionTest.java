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

import SmProcessing.RawTraceConversion;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class RawTraceConversionTest {
    private final double EPSILON = 0.000001;
    
    public RawTraceConversionTest() {
    }
    

    @Test
    public void testCountToG() {
        double result = RawTraceConversion.countToG(500000.0, 2.0);
        org.junit.Assert.assertEquals(result, 0.25, EPSILON);
    }
    public void testCountToCMS() {
        double result = RawTraceConversion.countToCMS(500000.0, 2.0, 5.0);
        org.junit.Assert.assertEquals(result, 1.25, EPSILON);
    }
}
