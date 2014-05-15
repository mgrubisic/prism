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

import SmUtilities.ConfigReader;
import org.junit.Test;

/**
 * JUnit test class for ConfigReader in SmUtilities
 * @author jmjones
 */
public class ConfigReaderTest {
    ConfigReader config;
    
    public ConfigReaderTest() {
    }
        
    @Test
    public void TestSetConfigValue() {
        config = new ConfigReader();
        config.setConfigValue("a", "1");
        config.setConfigValue("b", "2");
        config.setConfigValue("c", "3");
        config.setConfigValue("d", "4");
        org.junit.Assert.assertEquals("1", config.getConfigValue("a"));
        org.junit.Assert.assertEquals("2", config.getConfigValue("b"));
        org.junit.Assert.assertEquals("3", config.getConfigValue("c"));
        org.junit.Assert.assertEquals("4", config.getConfigValue("d"));
    }
    public void TestGetConfigValue() {
        config = new ConfigReader();
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
}
