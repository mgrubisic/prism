/*******************************************************************************
 * Name: Java class PrismXMLReaderTest.java
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
import SmUtilities.PrismXMLReader;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

/**
 *
 * @author jmjones
 */
public class PrismXMLReaderTest {
    ConfigReader config = ConfigReader.INSTANCE;
    String filename = "/PRISMtest/Data/prism_config.xml";
    
    public PrismXMLReaderTest() {
    }
    @Test
    public void TestReadFile() throws ParserConfigurationException, IOException, SAXException {
        InputStream ins = PrismXMLReaderTest.class.getResourceAsStream(filename);
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(ins);
        org.junit.Assert.assertEquals("2", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyCode"));
        org.junit.Assert.assertEquals("U.S. Geological Survey", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyFullName"));
        org.junit.Assert.assertEquals("USGS", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyAbbreviation"));
        org.junit.Assert.assertEquals("NP", config.getConfigValue("PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyIRISCode"));
        org.junit.Assert.assertEquals("04", config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitCode"));
        org.junit.Assert.assertEquals("cm/sec2", config.getConfigValue("PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitName"));    

        org.junit.Assert.assertEquals("SingleColumn", config.getConfigValue("PRISM/OutputArrayFormat"));
        org.junit.Assert.assertEquals("0.1", config.getConfigValue("PRISM/QCparameters/InitialVelocity"));
        org.junit.Assert.assertEquals("0.1", config.getConfigValue("PRISM/QCparameters/ResidualVelocity"));
        org.junit.Assert.assertEquals("0.1", config.getConfigValue("PRISM/QCparameters/ResidualDisplacement"));
        org.junit.Assert.assertEquals("4", config.getConfigValue("PRISM/BandPassFilterParameters/BandPassFilterOrder"));
        org.junit.Assert.assertEquals("3.0", config.getConfigValue("PRISM/BandPassFilterParameters/BandPassTaperLength"));    

        org.junit.Assert.assertEquals("20.0", config.getConfigValue("PRISM/BandPassFilterParameters/BandPassFilterCutoff/CutoffHigh"));
        org.junit.Assert.assertEquals("0.1", config.getConfigValue("PRISM/BandPassFilterParameters/BandPassFilterCutoff/CutoffLow"));
        org.junit.Assert.assertEquals("5", config.getConfigValue("PRISM/StrongMotionThreshold"));
        org.junit.Assert.assertEquals("0.0", config.getConfigValue("PRISM/EventOnsetBufferAmount"));
        org.junit.Assert.assertEquals("PWD", config.getConfigValue("PRISM/EventDetectionMethod"));
        org.junit.Assert.assertEquals("No", config.getConfigValue("PRISM/DeleteInputV0"));    

        org.junit.Assert.assertEquals("On", config.getConfigValue("PRISM/DebugToLog"));
        org.junit.Assert.assertEquals("Off", config.getConfigValue("PRISM/WriteBaselineFunction"));
        org.junit.Assert.assertEquals("1", config.getConfigValue("PRISM/AdaptiveBaselineCorrection/FirstPolyOrder/LowerLimit"));
        org.junit.Assert.assertEquals("2", config.getConfigValue("PRISM/AdaptiveBaselineCorrection/FirstPolyOrder/UpperLimit"));
        org.junit.Assert.assertEquals("1", config.getConfigValue("PRISM/AdaptiveBaselineCorrection/ThirdPolyOrder/LowerLimit"));
        org.junit.Assert.assertEquals("3", config.getConfigValue("PRISM/AdaptiveBaselineCorrection/ThirdPolyOrder/UpperLimit"));    
    }
    
    @Rule public ExpectedException expectedEx = ExpectedException.none();
    @Test
    public void TestExceptionOnRead()  throws ParserConfigurationException, 
                                            IOException, SAXException {
        expectedEx.expect(IllegalArgumentException.class);
        String badfilename = "/PRISMtest/Data/bad_name.xml";
        InputStream ins = PrismXMLReaderTest.class.getResourceAsStream(badfilename);
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(ins);
    }
    @Test
    public void TestSAXExceptionOnRead()  throws ParserConfigurationException, IOException, SAXException {
        expectedEx.expect(SAXException.class);
        String badfilename = "/PRISMtest/Data/bad_config.xml";
        InputStream ins = PrismXMLReaderTest.class.getResourceAsStream(badfilename);
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(ins);
    }
    @Test
    public void TestExceptionOnSchema()  throws ParserConfigurationException, IOException, SAXException {
        expectedEx.expect(IOException.class);
        String badfilename = "/PRISMtest/Data/bad_schema.xml";
        InputStream ins = PrismXMLReaderTest.class.getResourceAsStream(badfilename);
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(ins);
    }
    @Test
    public void TestEmptyOnRead()  throws ParserConfigurationException, IOException, SAXException {
        org.junit.Assert.assertEquals(null, config.getConfigValue("NoSuchTagInTheFile"));    
    }    
}
