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

import SmConstants.VFileConstants.MagnitudeType;
import SmProcessing.FilterCutOffThresholds;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class FilterCutOffThresholdsTest {
    private final double NOVAL = -999.99;
    private final double EPSILON = 0.00001;
    
    public FilterCutOffThresholdsTest() {
    }
    
    @Test
    public void selectMagsTest() {
        MagnitudeType magtype;
        FilterCutOffThresholds threshold = new FilterCutOffThresholds();
    
        magtype = threshold.SelectMagAndThresholds(6.0, NOVAL, NOVAL, NOVAL, NOVAL);
        org.junit.Assert.assertEquals(MagnitudeType.MOMENT, magtype);
        org.junit.Assert.assertEquals(6.0,threshold.getMagnitude(),EPSILON);
        org.junit.Assert.assertEquals(0.1,threshold.getLowCutOff(),EPSILON);
        org.junit.Assert.assertEquals(40.0,threshold.getHighCutOff(),EPSILON);

        magtype = threshold.SelectMagAndThresholds(NOVAL, 5.4, NOVAL, NOVAL, NOVAL);
        org.junit.Assert.assertEquals(MagnitudeType.M_LOCAL, magtype);
        org.junit.Assert.assertEquals(5.4,threshold.getMagnitude(),EPSILON);
        org.junit.Assert.assertEquals(0.3,threshold.getLowCutOff(),EPSILON);
        org.junit.Assert.assertEquals(35.0,threshold.getHighCutOff(),EPSILON);
    
        magtype = threshold.SelectMagAndThresholds(NOVAL, NOVAL, 4.4, NOVAL, NOVAL);
        org.junit.Assert.assertEquals(MagnitudeType.SURFACE, magtype);
        org.junit.Assert.assertEquals(4.4,threshold.getMagnitude(),EPSILON);
        org.junit.Assert.assertEquals(0.3,threshold.getLowCutOff(),EPSILON);
        org.junit.Assert.assertEquals(35.0,threshold.getHighCutOff(),EPSILON);
    
        magtype = threshold.SelectMagAndThresholds(NOVAL, NOVAL, NOVAL, 3.4, NOVAL);
        org.junit.Assert.assertEquals(MagnitudeType.M_OTHER, magtype);
        org.junit.Assert.assertEquals(3.4,threshold.getMagnitude(),EPSILON);
        org.junit.Assert.assertEquals(0.5,threshold.getLowCutOff(),EPSILON);
        org.junit.Assert.assertEquals(25.0,threshold.getHighCutOff(),EPSILON);

        magtype = threshold.SelectMagAndThresholds(NOVAL, NOVAL, NOVAL, NOVAL, NOVAL);
        org.junit.Assert.assertEquals(MagnitudeType.INVALID, magtype);
    }
}
