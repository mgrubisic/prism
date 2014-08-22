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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class OutputDirTest {
    Pattern officialname;
    StringBuilder sb = new StringBuilder(80);
    String pat;
    
    public OutputDirTest() {
        pat = sb.append("^")
                       .append("([a-zA-Z]+)")
                       .append("(\\.)")
                       .append("(\\d+)")
                       .append("(\\.)")
                       .append("([a-zA-Z]+)")
                       .append("(\\.)")
                       .append("([a-zA-Z]+)")
                       .append("(\\.)")
                       .append("([-\\s\\w]*)")
                       .append("(\\.)")
                       .append("\\w+")
                       .append("(\\.)")
                       .append("[vV][01]")
                       .append("$")
                       .toString();
        officialname = Pattern.compile(pat);
    }
    
     @Test
     public void testFileName() {
         Matcher m = officialname.matcher("CI.37183173.AZ.RDM.--.HNZ.V0");
         org.junit.Assert.assertEquals(true, m.matches());
         Matcher n = officialname.matcher("CI.37183173.AZ.RDM. .HNZ.V0");
         org.junit.Assert.assertEquals(true, n.matches());
         Matcher o = officialname.matcher("37183173.AZ.RDM.HNZ.V0");
         org.junit.Assert.assertEquals(false, o.matches());
         Matcher p = officialname.matcher("CI.37183173.AZ.RDM.--.HNZ.V1");
         org.junit.Assert.assertEquals(true, p.matches());
     }
}
