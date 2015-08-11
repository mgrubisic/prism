/*******************************************************************************
 * Name: Java class ConfigReaderTest.java
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

import SmUtilities.ConfigReader;
import org.junit.Test;

/**
 * JUnit test class for ConfigReader in SmUtilities
 * @author jmjones
 */
public class ConfigReaderTest {
    ConfigReader config = ConfigReader.INSTANCE;
    
    public ConfigReaderTest() {
    }
        
    @Test
    public void TestSetConfigValue() {
        config.setConfigValue("a", "1");
        config.setConfigValue("b", "2");
        config.setConfigValue("c", "3");
        config.setConfigValue("d", "4");
        org.junit.Assert.assertEquals("1", config.getConfigValue("a"));
        org.junit.Assert.assertEquals("2", config.getConfigValue("b"));
        org.junit.Assert.assertEquals("3", config.getConfigValue("c"));
        org.junit.Assert.assertEquals("4", config.getConfigValue("d"));
    }
    @Test
    public void TestGetConfigValue() {
        config.setConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyCode", "2");
        config.setConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyFullName", "U.S. Geological Survey");
        config.setConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyAbbreviation", "USGS");
        config.setConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyIRISCode", "NP");
        config.setConfigValue("PRISM/OutputFileFormat", "Single");
        config.setConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitCode", "04");
        config.setConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitName", "cm/sec/sec");        

        org.junit.Assert.assertEquals("2", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyCode"));
        org.junit.Assert.assertEquals("U.S. Geological Survey", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyFullName"));
        org.junit.Assert.assertEquals("USGS", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyAbbreviation"));
        org.junit.Assert.assertEquals("NP", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyIRISCode"));
        org.junit.Assert.assertEquals("Single", config.getConfigValue("PRISM/OutputFileFormat"));
        org.junit.Assert.assertEquals("04", config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitCode"));
        org.junit.Assert.assertEquals("cm/sec/sec", config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitName"));
    }
    @Test
    public void TestSingleton() {
        ConfigReader con1 = ConfigReader.INSTANCE;
        ConfigReader con2 = ConfigReader.INSTANCE;
        org.junit.Assert.assertEquals(true, (con1 == con2));
        con1.setConfigValue("a", "1");
        org.junit.Assert.assertEquals("1", con2.getConfigValue("a"));
    }
}
