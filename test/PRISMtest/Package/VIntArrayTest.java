/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package PRISMtest.Package;

import COSMOSformat.VIntArray;
import SmException.FormatException;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author jmjones
 */
public class VIntArrayTest {
    String[] header;
    String[] data;
    VIntArray hi;
    VIntArray di;
    
    public VIntArrayTest() {
        this.header = new String[3];
        this.data = new String[4];
    }
    
    @Before
    public void setUp() throws FormatException {
        header[0] = "  20 Integer-header values follow on  2 lines, Format= (10I8)";
        header[1] = "       0       1      50     120       1    -999    -999   13921    -999    -999";
        header[2] = "       5       5       5       5    -999       1    -999    -999       6     360";
        data[0] = "      30 acceleration pts, approx  56 secs, units=counts (50),Format=(10I8)";
        data[1] = "    3284    3334    3296    3284    3308    3242    3236    3324    3322    3262";
        data[2] = "    3300    3334    3302    3266    3322    3336    3312    3298    3254    3346";
        data[3] = "    3318    3258    3364    3290    3216    3302    3304    3310    3318    3256";
        hi = new VIntArray();
        di = new VIntArray();
    }
    
    @Test
    public void testFormatLineH() throws FormatException {
        hi.parseNumberFormatLine(header[0]);
        org.junit.Assert.assertEquals(20, hi.getNumVals());
        org.junit.Assert.assertEquals("(10I8)", hi.getNumberFormat());
        org.junit.Assert.assertEquals(10, hi.getValsPerLine());
        org.junit.Assert.assertEquals(8, hi.getFieldWidth());
    }
    @Test
    public void testFormatLineD() throws FormatException {
        di.parseNumberFormatLine(data[0]);
        org.junit.Assert.assertEquals(30, di.getNumVals());
        org.junit.Assert.assertEquals("(10I8)", di.getNumberFormat());
        org.junit.Assert.assertEquals(10, di.getValsPerLine());
        org.junit.Assert.assertEquals(8, di.getFieldWidth());
    }
    @Test
    public void testParseValuesH() throws FormatException {
        hi.parseValues(0, header);
        org.junit.Assert.assertEquals(20, hi.getNumVals());
        org.junit.Assert.assertEquals("(10I8)", hi.getNumberFormat());
        org.junit.Assert.assertEquals(10, hi.getValsPerLine());
        org.junit.Assert.assertEquals(8, hi.getFieldWidth());
        org.junit.Assert.assertEquals(2, hi.getNumLines());
        org.junit.Assert.assertEquals(0, hi.getIntValue(0));
        org.junit.Assert.assertEquals(360, hi.getIntValue(19));
    }
    @Test
    public void testParseValuesD() throws FormatException {
        di.parseValues(0, data);
        org.junit.Assert.assertEquals(30, di.getNumVals());
        org.junit.Assert.assertEquals("(10I8)", di.getNumberFormat());
        org.junit.Assert.assertEquals(10, di.getValsPerLine());
        org.junit.Assert.assertEquals(8, di.getFieldWidth());
        org.junit.Assert.assertEquals(3, di.getNumLines());
        org.junit.Assert.assertEquals(3284, di.getIntValue(0));
        org.junit.Assert.assertEquals(3256, di.getIntValue(29));
    }
}
