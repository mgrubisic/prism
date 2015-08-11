/*******************************************************************************
 * Name: Java class FilterCutOffThresholdsTest.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

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
