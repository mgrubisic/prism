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

import SmProcessing.EventOnsetDetection;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class EventOnsetDetectionTest {
    EventOnsetDetection e5;
    EventOnsetDetection e10;
    EventOnsetDetection e20;
    
    public EventOnsetDetectionTest() {
        e5 = new EventOnsetDetection( 0.005 );
        e10 = new EventOnsetDetection( 0.01 );
        e20 = new EventOnsetDetection( 0.02 );
    }
    
    @Before
    public void setUp() {
    }
    
    @Test
    public void checkCoefficients() {
    
    }
}
