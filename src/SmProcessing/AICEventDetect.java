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

package SmProcessing;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * This class computes AIC for the input array and picks the P (event) onset.
 * It uses an abbreviated form of the Akaike Information Criterion to locate 
 * the global minimum.  
 * See papers by Maeda, N. (1985). A method for reading
 * and checking phase times in autoprocessing system of seismic wave data, 
 * Zisin Jishin 38, 365-379.
 * 
 * The result is the index of the event offset in the input array.
 * index = min(AIC(n)) + 1
 * AIC(n) = k*log(var(x([1,k])) + (n-k-1)*log(var(x[k+1,n]))
 * Translated into Java (in 2014) from matlab code by Erol Kalkan (in 2014), 
 * from algorithms in Fortran and R by Bill Ellsworth.
 * 
 * @author jmjones
 */
public class AICEventDetect {
    private int index;
    private int bufferedIndex;
    private double[] array;
    private double bufferVal;
    private final double EPSILON = 0.00001;
    
    public AICEventDetect() {
        this.index = 0;
        this.bufferedIndex = 0;
        this.bufferVal = 0.0;
    }
    
    public int calculateIndex( double[] InArray, String pickrange ) {
        if ((InArray == null) || (InArray.length == 0)) {
            index = -1;
            return index;
        }
        String range = ((pickrange == null) || pickrange.isEmpty() || 
                (!pickrange.equalsIgnoreCase("Full"))) ? "to_peak" : pickrange;
        array = new double[InArray.length];
        double[] arrnew;
        ArrayStats arrstats;
        
        //Make a copy of the array for calculations
        System.arraycopy(InArray, 0, array, 0, InArray.length);
        
        //Remove the median value from the array
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = 0; i < array.length; i++) {
            stats.addValue(array[i]);
        }
        double median = stats.getPercentile(50);
        ArrayOps.removeValue(array, median);
        
        //window the array based on the pickrange
        if (range.equalsIgnoreCase("to_peak")) {
            arrstats = new ArrayStats( array );
            int indpeak = arrstats.getPeakValIndex();
            arrnew = new double[indpeak];
            System.arraycopy(array, 0 , arrnew, 0, indpeak);
//            System.out.println("Inarray peak: " + indpeak);
        } else {
            arrnew = array;
        }
        double[] temp = aicval(arrnew);
        
        //select the minimum value from this array, get the index and add 1
        //to it for the global minimum.
        arrstats = new ArrayStats( temp );
        index = arrstats.getMinValIndex() + 1;
        return index;
    }
    private double[] aicval( double[] segment ) {
        double[] vararray = new double[segment.length];
        double[] temp;
        double s1;
        double s2;
        SummaryStatistics sumstats;
                
        for (int i = 0; i < segment.length-1; i++) {
            
            //compute the variance in the first part of the array
            temp = new double[i];
            System.arraycopy(segment,0,temp,0,i);
            sumstats = new SummaryStatistics();
            for (double each : temp) {
                sumstats.addValue(each);
            }
            s1 = sumstats.getVariance();
            s1 = (s1 > 0.0) ? Math.log(s1) : 0.0;
            
            //compute the variance in the second part of the array
            temp = new double[segment.length-i];
            System.arraycopy(segment,i,temp,0,segment.length-i);
            sumstats = new SummaryStatistics();
            for (double each : temp) {
                sumstats.addValue(each);
            }
            s2 = sumstats.getVariance();
            s2 = (s2 > 0.0) ? Math.log(s2) : 0.0;
            
            vararray[i] = i * s1 + (segment.length-i) * s2;
        }
        return vararray;
    }
    public int applyBuffer( double buffer, double dtime ) {
        if (Math.abs(dtime - 0.0) < EPSILON) {
            bufferedIndex = -1;
        } else {
            bufferVal = buffer;
            bufferedIndex = index - (int)Math.round(bufferVal/dtime);
            bufferedIndex = (bufferedIndex < 0) ? 0 : bufferedIndex;
        }
        return bufferedIndex;
    }
    public int getIndex() {
        return index;
    }
    public int getBufferedIndex() {
        return bufferedIndex;
    }
}
