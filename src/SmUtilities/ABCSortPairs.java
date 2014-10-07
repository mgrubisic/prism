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

package SmUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author jmjones
 */
public class ABCSortPairs {
    private SortedSet<SortVals> pairs = new TreeSet<>();
    
    public ABCSortPairs() {
    }
    public void addPair( double first, int second) {
        pairs.add(new SortVals(first, second));
    }
    public ArrayList<Integer> getSortedVals() {
        ArrayList<Integer> out = new ArrayList<>();
        if (!pairs.isEmpty()) {
            SortedSet<SortVals> sortByFirst = 
                new TreeSet<>(new Comparator<SortVals>()
                {
                    @Override
                    public int compare(SortVals a, SortVals b) {
                        double vala = a.getRMS();
                        double valb = b.getRMS();
                        return Double.compare(vala,valb);
                    }
                });
            sortByFirst.addAll(pairs);
            for (SortVals each : sortByFirst) {
                out.add(each.getIndex());
            }
        }
        return out;
    }
    
    public class SortVals {
        private double rmsval;
        private int index;
        
        public SortVals( double rval, int idx) {
            this.rmsval = rval;
            this.index = idx;
        }
        public double getRMS() {
            return rmsval;
        }
        public int getIndex() {
            return index;
        }
    }
}
