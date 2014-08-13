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

import COSMOSformat.V0Component;
import static SmConstants.VFileConstants.DEFAULT_NOINTVAL;
import static SmConstants.VFileConstants.RAWACC;
import static SmConstants.VFileConstants.STATION_CHANNEL_NUMBER;
import SmException.FormatException;
import SmException.SmException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * JUnit test class for V0Component and COSMOScontentFormat
 * @author jmjones
 */
public class V0ComponentTest {
    String[] infile;
    V0Component v0;
    double delta = 0.0001;
    
    public V0ComponentTest() {
        this.infile = new String[48];
    }
        
    @Before
    public void setUp() {
        infile[0] = "Raw acceleration counts   (Format v01.20 with 13 text lines) Src: 921az039.evt";
        infile[1] = "Rcrd of Wed Jan 15, 2014 01:35:00.0 PST";
        infile[2] = "Hypocenter: To be determined    H=   km       ML=     Mw= ";
        infile[3] = "Origin: To be determined ";
        infile[4] = "Statn No: 05- 13921 Code:CE-13921  CGS  Riverside - Limonite & Downey";
        infile[5] = "Coords: 33.975  -117.487   Site geology:  ";
        infile[6] = "Recorder: Etna   s/n 1614 ( 3 Chns of   3 at Sta) Sensor: FBA ";
        infile[7] = "Rcrd start time: 1/15/2014, 09:35:  .0 UTC (Q=5) RcrdId: 13921-L1614-14015.39";
        infile[8] = "Sta Chan  1: 360 deg (Rcrdr Chan  1)";
        infile[9] = "Raw record length =  56.000 sec, Uncor max =    20108 counts, at   25.205 sec.";
        infile[10]= "Processed: 01/15/14  (k2vol0 v0.1 CSMIP)";
        infile[11]= "Record not filtered.";
        infile[12]= "Values used when parameter or data value is unknown/unspecified:   -999, -999.0";
        infile[13]= " 100 Integer-header values follow on  10 lines, Format= (10I8)";
        infile[14]= "       0       1      50     120       1    -999    -999   13921    -999    -999";
        infile[15]= "       5       5       5       5    -999       1    -999    -999       6     360";
        infile[16]= "    -999       1       3    -999    -999    -999    -999    -999       1     109";
        infile[17]= "       3    1614       3       3      24      18    -999      39       1    2014";
        infile[18]= "      15       1      15       9      35       5       5    -999    -999      76";
        infile[19]= "       1       4    -999     360    -999    -999    -999    -999    -999    -999";
        infile[20]= "    -999    -999    -999    -999       0    -999    -999    -999    -999    -999";
        infile[21]= "    -999    -999    -999    -999       0       0       1    -999    -999       1";
        infile[22]= "     560       0       0       0       0       0       0       0     222       0";
        infile[23]= "       0     303    -999    -999    -999    -999    -999    -999    -999    -999";
        infile[24]= " 100 Real-header values follow on  17 lines, Format= (6F13.6)";
        infile[25]= "    33.975300  -117.486500   213.000000   371.000000  -999.000000  -999.000000";
        infile[26]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[27]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[28]= "  -999.000000  -999.000000  -999.000000      .298023     2.500000    25.000000";
        infile[29]= "    30.000000  -999.000000  -999.000000  -999.000000  -999.000000      .000000";
        infile[30]= "  -999.000000      .000000  -999.000000      .005000    56.000000  -999.000000";
        infile[31]= "  -999.000000  -999.000000  -999.000000   100.400000      .660000      .627000";
        infile[32]= "     2.500000     4.000000  -999.000000  -999.000000     1.000000  -999.000000";
        infile[33]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[34]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[35]= "  -999.000000     5.000000    56.000000 20108.000000    25.205000  3304.483000";
        infile[36]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[37]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[38]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[39]= "  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[40]= "      .000000    10.000000  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[41]= "  -999.000000  -999.000000  -999.000000  -999.000000";
        infile[42]= "   1 Comment line(s) follow, each starting with a \"|\":";
        infile[43]= "|";
        infile[44]= "      19 acceleration pts, approx  56 secs, units=counts (50),Format=(10I8)";
        infile[45]= "    3284    3334    3296    3284    3308    3242    3236    3324    3322    3262";
        infile[46]= "    3300    3334    3302    3266    3322    3336    3312    3298    3254";
        infile[47]= "End-of-data for Chan  1 acceleration";
        
        v0 = new V0Component(RAWACC);
    }
    @Test
    public void testConstructor() {
        org.junit.Assert.assertEquals(RAWACC, v0.getProcType());
        org.junit.Assert.assertEquals(-999, v0.getNoIntVal());
        org.junit.Assert.assertEquals(-999.0, v0.getNoRealVal(), delta);
    }
    @Test
    public void testLoadComponent() throws FormatException, SmException {
        int lineNum = v0.loadComponent(0, infile);
        org.junit.Assert.assertEquals(48, lineNum);
        org.junit.Assert.assertEquals(76, v0.getChannelNum());
    }
    @Test
    public void testParseHeader() throws FormatException, SmException {
        infile[12]= "Values used when parameter or data value is unknown/unspecified:   -933, -222.0";
        int lineNum = v0.loadComponent(0, infile);
        org.junit.Assert.assertEquals(RAWACC, v0.getProcType());
        org.junit.Assert.assertEquals(-933, v0.getNoIntVal());
        org.junit.Assert.assertEquals(-222.0, v0.getNoRealVal(), delta);
        String[] check = v0.getTextHeader();
        org.junit.Assert.assertEquals(infile[0], check[0]);
        org.junit.Assert.assertEquals(infile[12], check[12]);
        org.junit.Assert.assertEquals("", v0.getSensorLocation());
        infile[8] = "Sta Chan  1: 360 deg (Rcrdr Chan  1) Location:8th Floor: Center";
        lineNum = v0.loadComponent(0, infile);
        org.junit.Assert.assertEquals("8th Floor: Center", v0.getSensorLocation());
    }
    @Rule public ExpectedException expectedEx = ExpectedException.none();
    @Test
    public void testParseHeaderFormatError() throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("Unable to find number of text header lines at line 1");
        infile[0] = "Raw acceleration counts   (Format v01.20 with text lines) Src: 921az039.evt";
        int lineNum = v0.loadComponent(0, infile);
    }
    @Test
    public void testParseHeaderNumberError() throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("Unable to extract NoData values at line 13");
        infile[12] = "Values used when parameter or data value is unknown/unspecified:   -999";
        int lineNum = v0.loadComponent(0, infile);
    }
    @Test
    public void testParseHeaderNumberError2() throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        infile[12] = "Values used when parameter or data value is unknown/unspecified:   -999.0, -999";
        int lineNum = v0.loadComponent(0, infile);
    }
    @Test
    public void testParseComments() throws FormatException, SmException {
        int lineNum = v0.loadComponent(0, infile);
        String[] check = v0.getComments();
        org.junit.Assert.assertEquals(infile[42], check[0]);
        org.junit.Assert.assertEquals(infile[43], check[1]); 
        infile[42]= "   1 COMMENT line(s) follow, each starting with a \"|\":";
        lineNum = v0.loadComponent(0, infile);
        check = v0.getComments();
        org.junit.Assert.assertEquals(infile[42], check[0]);
        org.junit.Assert.assertEquals(infile[43], check[1]); 
    }
    @Test
    public void testParseCommentsEOF()  throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("EOF found before comments at line 43");
        String[] test = Arrays.copyOfRange(infile, 0, 42);
        int lineNum = v0.loadComponent(0, test);
    }
    @Test
    public void testParseCommentsTooShort()  throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("Error in comment length of 1");
        String[] test = Arrays.copyOfRange(infile, 0, 43);
        int lineNum = v0.loadComponent(0, test);
    }
    @Test
    public void testParseCommentsKeyword()  throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("Could not find comments at 43");
        infile[42]= "   1 extra line(s) follow, each starting with a \"|\":";
        int lineNum = v0.loadComponent(0, infile);
    }
    @Test
    public void testParseCommentsBadNum()  throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("Could not find number of comment lines at 43");
        infile[42]= "  xx Comment line(s) follow, each starting with a \"|\":";
        int lineNum = v0.loadComponent(0, infile);
    }
    @Test
    public void testParseEndOfData() throws FormatException, SmException {
        int lineNum = v0.loadComponent(0, infile);
        String check = v0.getEndOfData();
        org.junit.Assert.assertEquals(infile[47], check);
        infile[47]= "End-of-DATA for Chan  1 acceleration";
        lineNum = v0.loadComponent(0, infile);
        check = v0.getEndOfData();
        org.junit.Assert.assertEquals(infile[47], check);
    }
    @Test
    public void testParseEODKeyword()  throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("Could not find End-of-data at line 48");
        infile[47]= "End-o-file for Chan  1 acceleration";
        int lineNum = v0.loadComponent(0, infile);
    }
    @Test
    public void testParseEODTooShort()  throws FormatException, SmException {
        expectedEx.expect(FormatException.class);
        expectedEx.expectMessage("End-of-file found before end-of-data at line 48");
        String[] test = Arrays.copyOfRange(infile, 0, 47);
        int lineNum = v0.loadComponent(0, test);
    }
    @Test
    public void testGetChannelNumber()  throws SmException, FormatException {
        expectedEx.expect(SmException.class);
        expectedEx.expectMessage("Undefined station channel number in int. header, index " + (STATION_CHANNEL_NUMBER+1));
        int lineNum = v0.loadComponent(0, infile);
        
        v0.setIntHeaderValue(STATION_CHANNEL_NUMBER, v0.getNoIntVal());
        int num = v0.getIntHeaderValue(STATION_CHANNEL_NUMBER);
        org.junit.Assert.assertEquals(DEFAULT_NOINTVAL, num);
        
        v0.setChannelNum();
    }
    @Test
    public void testGetSetHeaderVals() throws IndexOutOfBoundsException, FormatException, SmException {
        int lineNum = v0.loadComponent(0, infile);
        v0.setIntHeaderValue(10, 10);
        v0.setRealHeaderValue(10, 10.25);
        org.junit.Assert.assertEquals(10,v0.getIntHeaderValue(10));
        org.junit.Assert.assertEquals(10.25, v0.getRealHeaderValue(10),delta);
    }
    @Test
    public void testSetIntHeaderValsRange() throws FormatException, SmException {
        expectedEx.expect(SmException.class);
        int lineNum = v0.loadComponent(0, infile);
        v0.setIntHeaderValue(1000, 10);
    }
    @Test
    public void testGetRealHeaderValsRange() throws FormatException, SmException {
        expectedEx.expect(SmException.class);
        int lineNum = v0.loadComponent(0, infile);
        double test = v0.getRealHeaderValue(-8);
    }
    @Test
    public void testDataArrayMethods() throws FormatException, SmException {
        int lineNum = v0.loadComponent(0, infile);
        org.junit.Assert.assertEquals(19, v0.getDataLength());
        int[] test = v0.getDataArray();
        org.junit.Assert.assertEquals(3300, test[10]);
    }
}
