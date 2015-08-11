/*******************************************************************************
 * Name: Java class ABCSortPairsTest.java
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

import SmUtilities.ABCSortPairs;
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
