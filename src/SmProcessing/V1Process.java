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
    
    public void countsToValues(final int[] inArray, V1Process inV1parm, double countConv) {
        
        int inLength = inArray.length;
        double total = 0.0;
        double meanZero = 0.0;
        double maxVal = 0.0;  //could the whole array be negative?
        int maxIndex = 0;
        double avgVal = 0.0;
        double[] result = inV1parm.getV1Array();
        
        System.out.println("+++ countToVals: " + countConv);
        for (int i = 0; i < inLength; i++) {
            result[i] = inArray[i] * countConv;
            total = total + result[i];
        }
        meanZero = total / inLength;
        System.out.println("+++ Mean Zero: " + meanZero);
        
        total = 0.0;
        for (int i = 0; i < inLength; i++) {
            result[i] = result[i] - meanZero;
            total = total + result[i];
            if (result[i] > maxVal) {
                maxVal = result[i];
                maxIndex = i;
            }
        }
        avgVal = total / inLength;
        inV1parm.setAvgVal(avgVal);
        inV1parm.setMaxVal(maxVal);
        inV1parm.setMaxIndex(maxIndex);
        inV1parm.setMeanToZero(meanZero);
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
