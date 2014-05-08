/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmProcessing;

import static COSMOSformat.VFileConstants.FROM_G_CONVERSION;

/**
 *
 * @author jmjones
 */
public class V1Process {
    private double[] array;
    private double meanToZero;
    private double maxVal;
    private int maxIndex;
    private double avgVal;

    public V1Process(int inArrayLength) {
        this.array = new double[inArrayLength];
        this.meanToZero = 0.0;
        this.maxVal = 0.0;
        this.maxIndex = 0;
        this.avgVal = 0.0;
    }
    
    public double countToCMSConversion(double lsb, double fsi, double sensitivity) {
        //sensor calculation of volts per count and cm per sq. sec per volt
        //countToCMS units are cm per sq. sec per count
        //This is multiplied by each count to get the sensor value in cm per sq. sec
        double microToVolt = 1.0e-6;
        double epsilon = 0.001;
        double result = 0.0;
        if  ((lsb-0.0)>epsilon && (fsi-0.0)>epsilon && (sensitivity-0.0)>epsilon){
            result = ((fsi / lsb) * microToVolt) * (FROM_G_CONVERSION / sensitivity);
        }
        return result;
    }
    
    public void countsToValues(final int[] inArray, double countConv) {
        
        int length = array.length;
        double total = 0.0;
        
        System.out.println("+++ countToVals: " + countConv);
        for (int i = 0; i < length; i++) {
            array[i] = inArray[i] * countConv;
            total = total + array[i];
        }
        meanToZero = total / length;
        System.out.println("+++ Mean Zero: " + meanToZero);
        
        total = 0.0;
        for (int i = 0; i < length; i++) {
            array[i] = array[i] - meanToZero;
            total = total + array[i];
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIndex = i;
            }
        }
        this.avgVal = total / length;
    }

    public double getMeanToZero() {
        return this.meanToZero;
    }
    public void setMeanToZero(double inMeanTZ) {
        this.meanToZero = inMeanTZ;
    }
    public double getMaxVal() {
        return this.maxVal;
    }
    public void setMaxVal(double inMaxV) {
        this.maxVal = inMaxV;
    }
    public int getMaxIndex() {
        return this.maxIndex;
    }
    public void setMaxIndex(int inInd) {
        this.maxIndex = inInd;
    }
    public double getAvgVal() {
        return this.avgVal;
    }
    public void setAvgVal( double inAvgV) {
        this.avgVal = inAvgV;
    }
    public double[] getV1Array() {
        return this.array;
    }
    public int getV1ArrayLength() {
        return this.array.length;
    }
}
