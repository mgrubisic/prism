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

import SmUtilities.ABCSortPairs;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class ABCSortPairsTest {
    double[] sortlist = {0.02,0.15,0.022,0.596,0.01,0.73,0.145,0.001,0.36,0.597};
    int[] order = {8,5,1,3,7,2,9,4,10,6};
    ABCSortPairs sorter;
    
    public ABCSortPairsTest() {
        sorter = new ABCSortPairs();
    }
    
    @Test
    public void sortPairsTest() {
        int[] outvals;
        for (int i = 0; i < sortlist.length; i++) {
            sorter.addPair(sortlist[i], i+1);
        }
        outvals = sorter.getSortedVals();
        Integer exp;
        Integer act;
        for (int i = 0; i < outvals.length; i++) {
            exp = order[i];
            act = outvals[i];
            org.junit.Assert.assertEquals(exp.longValue(),act.longValue());
        }
    }
}
