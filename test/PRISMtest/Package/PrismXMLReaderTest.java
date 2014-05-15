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
import SmUtilities.PrismXMLReader;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

/**
 *
 * @author jmjones
 */
public class PrismXMLReaderTest {
    ConfigReader config;
    
    public PrismXMLReaderTest() {
        config = new ConfigReader();
    }
    @Test
    public void TestReadFile() throws ParserConfigurationException, IOException, SAXException {
        String filename = "D:\\PRISM\\config_files\\prism_config.xml";
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(filename, config);
        org.junit.Assert.assertEquals("2", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyCode"));
        org.junit.Assert.assertEquals("U.S. Geological Survey", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyFullName"));
        org.junit.Assert.assertEquals("USGS", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyAbbreviation"));
        org.junit.Assert.assertEquals("NP", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyIRISCode"));
        org.junit.Assert.assertEquals("Single", config.getConfigValue("PRISM/OutputFileFormat"));
        org.junit.Assert.assertEquals("04", config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitCode"));
        org.junit.Assert.assertEquals("cm/sec/sec", config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitName"));    
    }
    @Rule public ExpectedException expectedEx = ExpectedException.none();
    @Test
    public void TestIOExceptionOnRead()  throws ParserConfigurationException, IOException, SAXException {
        expectedEx.expect(IOException.class);
        String filename = "D:\\PRISM\\config_files\\bad_name.xml";
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(filename, config);
    }
    @Test
    public void TestSAXExceptionOnRead()  throws ParserConfigurationException, IOException, SAXException {
        expectedEx.expect(SAXException.class);
        String filename = "D:\\PRISM\\config_files\\bad_config.xml";
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(filename, config);
    }
    @Test
    public void TestEmptyOnRead()  throws ParserConfigurationException, IOException, SAXException {
        String filename = "D:\\PRISM\\config_files\\empty_config.xml";
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(filename, config);
        org.junit.Assert.assertEquals(null, config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitName"));    
    }    
}
