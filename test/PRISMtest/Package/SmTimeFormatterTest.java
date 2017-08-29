/*
 * Copyright (C) 2017 jmjones
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

import SmUtilities.SmTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmjones
 */
public class SmTimeFormatterTest {
    ZonedDateTime newtime, extratime;
    String teststring;
    
    public SmTimeFormatterTest() {
    }
    
    @Before
    public void setUp() {
        newtime = ZonedDateTime.parse("2015-03-29T12:26:10Z[UTC]");
        extratime = newtime.plusNanos(123456000);
        teststring = "2015/03/29 12:26:10.123 UTC";
    }

    @Test
    public void testGMTtime() {
        SmTimeFormatter gmt = new SmTimeFormatter();
//        System.out.println(gmt.getGMTdateTime());
    }
    @Test
    public void testUTCtime() {
        SmTimeFormatter utc = new SmTimeFormatter(extratime);
        org.junit.Assert.assertEquals(2015,utc.getUTCyear());
        org.junit.Assert.assertEquals(3, utc.getUTCmonth());
        org.junit.Assert.assertEquals(29, utc.getUTCday());
        org.junit.Assert.assertEquals(12, utc.getUTChour());
        org.junit.Assert.assertEquals(26, utc.getUTCminute());
        org.junit.Assert.assertEquals(10, utc.getUTCjustSecond());
        org.junit.Assert.assertEquals(123456000, utc.getUTCjustNano());
        org.junit.Assert.assertEquals(10.123456, utc.getUTCsecond(),0.000001);
        org.junit.Assert.assertEquals(teststring, utc.getUTCdateTime());
    }
}
