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

import SmProcessing.QCcheck;
import SmUtilities.ConfigReader;
import SmUtilities.PrismXMLReader;
import static SmUtilities.SmConfigConstants.QC_INITIAL_VELOCITY;
import static SmUtilities.SmConfigConstants.QC_RESIDUAL_DISPLACE;
import static SmUtilities.SmConfigConstants.QC_RESIDUAL_VELOCITY;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author jmjones
 */
public class QCcheckTest {
    private final ConfigReader config = ConfigReader.INSTANCE;
    private final String filename = "/PRISMtest/Data/prism_config.xml";
    private final double EPSILON = 0.00001;
    private double[] velocity;
    private double[] displace;
    
    public QCcheckTest() {
        velocity = new double[10];
        Arrays.fill(velocity, 0.0);
        
        displace = new double[10];
        Arrays.fill(displace, 0.0);
    }
    
    @Before
    public void setUp() throws ParserConfigurationException, IOException, SAXException {
        InputStream ins = PrismXMLReaderTest.class.getResourceAsStream(filename);
        PrismXMLReader xml = new PrismXMLReader();
        xml.readFile(ins);
    }
    
    @Test
    public void ValdidateValuesTest() {
        QCcheck check = new QCcheck();
        org.junit.Assert.assertEquals(true, check.validateQCvalues());
        org.junit.Assert.assertEquals(0.1, check.getInitVelocityQCval(),EPSILON);
        org.junit.Assert.assertEquals(0.1, check.getResVelocityQCval(),EPSILON);
        org.junit.Assert.assertEquals(0.1, check.getResDisplaceQCval(),EPSILON);
        
        String hold = config.getConfigValue(QC_INITIAL_VELOCITY);
        config.setConfigValue(QC_INITIAL_VELOCITY, "abc");
        org.junit.Assert.assertEquals(false, check.validateQCvalues());
        config.setConfigValue(QC_INITIAL_VELOCITY, hold);
        
        hold = config.getConfigValue(QC_RESIDUAL_VELOCITY);
        config.setConfigValue(QC_RESIDUAL_VELOCITY, "xyz");
        org.junit.Assert.assertEquals(false, check.validateQCvalues());
        config.setConfigValue(QC_RESIDUAL_VELOCITY, hold);
        
        hold = config.getConfigValue(QC_RESIDUAL_DISPLACE);
        config.setConfigValue(QC_RESIDUAL_DISPLACE, "1*3");
        org.junit.Assert.assertEquals(false, check.validateQCvalues());
        config.setConfigValue(QC_RESIDUAL_DISPLACE, hold);
        
        config.setConfigValue(QC_INITIAL_VELOCITY, null);
        config.setConfigValue(QC_RESIDUAL_VELOCITY, null);
        config.setConfigValue(QC_RESIDUAL_DISPLACE, null);
        org.junit.Assert.assertEquals(true, check.validateQCvalues());
    }
    
    @Test
    public void CheckWindowTest() {
        QCcheck check = new QCcheck();
        org.junit.Assert.assertEquals(1234, check.findWindow(0.3, 200.0, 1234) );
        org.junit.Assert.assertEquals(600, check.findWindow(0.3, 200.0, 500) );
        org.junit.Assert.assertEquals(600, check.getQCWindow());
        
    }
    @Test
    public void QCVelocityTest() {
        QCcheck check = new QCcheck();
        org.junit.Assert.assertEquals(true, check.validateQCvalues());
        org.junit.Assert.assertEquals(0, check.findWindow(0.3, 00.0, 0));
        
        velocity[0] = 0.01;
        velocity[9] = 0.02;
        org.junit.Assert.assertEquals(true, check.qcVelocity(velocity));
        org.junit.Assert.assertEquals(0.01, check.getInitialVelocity(), EPSILON);
        org.junit.Assert.assertEquals(0.02, check.getResidualVelocity(), EPSILON);
        
        velocity[0] = 0.21;
        org.junit.Assert.assertEquals(false, check.qcVelocity(velocity));
        
        velocity[0] = 0.01;
        velocity[9] = 0.22;
        org.junit.Assert.assertEquals(false, check.qcVelocity(velocity));
        
        org.junit.Assert.assertEquals(5, check.findWindow(0.3, 00.0, 5));
        Arrays.fill(velocity, 0.04);
        velocity[3] = -0.03;
        velocity[6] = -0.02;
        org.junit.Assert.assertEquals(true, check.qcVelocity(velocity));
        org.junit.Assert.assertEquals(0.0225, check.getInitialVelocity(), EPSILON);
        org.junit.Assert.assertEquals(0.028, check.getResidualVelocity(), EPSILON);
    }
    @Test
    public void QCDisplacementTest() {
        QCcheck check = new QCcheck();
        org.junit.Assert.assertEquals(true, check.validateQCvalues());
        org.junit.Assert.assertEquals(0, check.findWindow(0.3, 00.0, 0));
        
        displace[9] = 0.01;
        org.junit.Assert.assertEquals(true, check.qcDisplacement(displace));
        org.junit.Assert.assertEquals(0.01, check.getResidualDisplacement(), EPSILON);
        
        displace[9] = 0.11;
        org.junit.Assert.assertEquals(false, check.qcDisplacement(displace));
        
        Arrays.fill(displace, 0.02);
        org.junit.Assert.assertEquals(4, check.findWindow(0.3, 00.0, 4));
        org.junit.Assert.assertEquals(true, check.qcDisplacement(displace));
        org.junit.Assert.assertEquals(0.02, check.getResidualDisplacement(), EPSILON);
        
        displace[7] = -0.02;
        org.junit.Assert.assertEquals(true, check.qcDisplacement(displace));
        org.junit.Assert.assertEquals(0.01, check.getResidualDisplacement(), EPSILON);
    }
}
