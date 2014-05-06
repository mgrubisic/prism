/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package PRISMtest.Package;

import COSMOSformat.VRealArray;
import SmException.FormatException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmjones
 */
public class VRealArrayTest {
    String[] header;
    String[] data;
    VRealArray hi;
    VRealArray di;
    String[] headerbits;
    static double epsilon = 0.001;
    
    public VRealArrayTest() {
        this.header = new String[3];
        this.data = new String[4];
        this.headerbits = new String[12];
    }
    
    @Before
    public void setUp() {
        header[0] = "  12 Real-header values follow on  17 lines, Format= (6F13.6)";
        header[1] = "    33.975300  -117.486500   213.000000   371.000000  -999.000000  -999.000000";
        header[2] = "  -999.000000  -999.000000  -999.000000     0.298023     2.500000    25.000000";

        data[0] = "      24 uncor. accel. pts, approx  500 secs, units=cm/sec2(04), Format=(8F10.3)";
        data[1] = "    -0.269     0.387    -0.111    -0.269     0.046    -0.820    -0.898     0.256";
        data[2] = "     0.230    -0.557    -0.059     0.387    -0.033    -0.505     0.230     0.414";
        data[3] = "     0.099    -0.085    -0.662     0.545     0.177    -0.610     0.781    -0.190";

        headerbits[0] = "    33.975300";
        headerbits[1] = "  -117.486500";
        headerbits[2] = "   213.000000";
        headerbits[3] = "   371.000000";
        headerbits[4] = "  -999.000000";
        headerbits[5] = "  -999.000000";
        headerbits[6] = "  -999.000000";
        headerbits[7] = "  -999.000000";
        headerbits[8] = "  -999.000000";
        headerbits[9] = "     0.298023";
        headerbits[10] = "     2.500000";
        headerbits[11] = "    25.000000";

        hi = new VRealArray();
        di = new VRealArray();
    }
    @Test
    public void testConstructor()  {
        org.junit.Assert.assertEquals(13, hi.getFieldWidth());
        org.junit.Assert.assertEquals(6, hi.getPrecision());
        org.junit.Assert.assertEquals("F", hi.getDisplayType());
    }
    
    @Test
    public void testFormatLineH() throws FormatException {
        hi.parseNumberFormatLine(header[0]);
        org.junit.Assert.assertEquals(12, hi.getNumVals());
        org.junit.Assert.assertEquals("(6F13.6)", hi.getNumberFormat());
        org.junit.Assert.assertEquals(6, hi.getValsPerLine());
        org.junit.Assert.assertEquals(13, hi.getFieldWidth());
        org.junit.Assert.assertEquals(6, hi.getPrecision());
    }
    @Test
    public void testFormatLineD() throws FormatException {
        di.parseNumberFormatLine(data[0]);
        org.junit.Assert.assertEquals(24, di.getNumVals());
        org.junit.Assert.assertEquals("(8F10.3)", di.getNumberFormat());
        org.junit.Assert.assertEquals(8, di.getValsPerLine());
        org.junit.Assert.assertEquals(10, di.getFieldWidth());
        org.junit.Assert.assertEquals(3, di.getPrecision());
    }
    @Test
    public void testParseValuesH() throws FormatException {
        hi.parseValues(0, header);
        org.junit.Assert.assertEquals(12, hi.getNumVals());
        org.junit.Assert.assertEquals("(6F13.6)", hi.getNumberFormat());
        org.junit.Assert.assertEquals(6, hi.getValsPerLine());
        org.junit.Assert.assertEquals(13, hi.getFieldWidth());
        org.junit.Assert.assertEquals(2, hi.getNumLines());
        org.junit.Assert.assertEquals(6, hi.getPrecision());
        org.junit.Assert.assertEquals(33.9753, hi.getRealValue(0), epsilon);
        org.junit.Assert.assertEquals(25.0, hi.getRealValue(11), epsilon);
    }
    @Test
    public void testParseValuesD() throws FormatException {
        di.parseValues(0, data);
        org.junit.Assert.assertEquals(24, di.getNumVals());
        org.junit.Assert.assertEquals("(8F10.3)", di.getNumberFormat());
        org.junit.Assert.assertEquals(8, di.getValsPerLine());
        org.junit.Assert.assertEquals(10, di.getFieldWidth());
        org.junit.Assert.assertEquals(3, di.getNumLines());
        org.junit.Assert.assertEquals(3, di.getPrecision());
        org.junit.Assert.assertEquals(-0.269, di.getRealValue(0), epsilon);
        org.junit.Assert.assertEquals(-0.190, di.getRealValue(23), epsilon);
    }
    public void testSetRealValue() throws FormatException {
        di.parseValues(0, data);
        di.setRealValue(5, 333.332);
        org.junit.Assert.assertEquals(333.332, di.getRealValue(5), epsilon);
    }
    @Test
    public void testGetRealArray() throws FormatException {
        hi.parseValues(0, header);
        double[] array = new double[hi.getNumVals()];
        array = hi.getRealArray();
        org.junit.Assert.assertEquals(array[9], 0.298023, epsilon);
    }
    @Test
    public void testArrayToText() throws FormatException {
        hi.parseNumberFormatLine(header[0]);
        hi.parseValues(0, header);
        ArrayList<String> textList = hi.arrayToText();
        org.junit.Assert.assertArrayEquals(headerbits, textList.toArray(new String[textList.size()]));
    }
    @Test
    public void testCopyConstructor() throws FormatException {
        hi.parseNumberFormatLine(header[0]);
        hi.parseValues(0, header);
        VRealArray test = new VRealArray( hi );
        org.junit.Assert.assertEquals(hi.getNumVals(), test.getNumVals());
        org.junit.Assert.assertEquals(hi.getNumberFormat(), test.getNumberFormat());
        org.junit.Assert.assertEquals(hi.getValsPerLine(), test.getValsPerLine());
        org.junit.Assert.assertEquals(hi.getFieldWidth(), test.getFieldWidth());
        org.junit.Assert.assertEquals(hi.getNumLines(), test.getNumLines());
        org.junit.Assert.assertEquals(hi.getRealValue(0), test.getRealValue(0), epsilon);
        org.junit.Assert.assertEquals(hi.getRealValue(11), test.getRealValue(11), epsilon);
    }
    @Test
    public void testNumberSectionToTextH() throws FormatException {
        hi.parseNumberFormatLine(header[0]);
        hi.parseValues(0, header);
        String[] textList = hi.numberSectionToText();
        org.junit.Assert.assertArrayEquals(header, textList);
    }
    @Test
    public void testNumberSectionToTextD() throws FormatException {
        di.parseNumberFormatLine(data[0]);
        di.parseValues(0, data);
        String[] textList = di.numberSectionToText();
        org.junit.Assert.assertArrayEquals(data, textList);
    }
    
}
