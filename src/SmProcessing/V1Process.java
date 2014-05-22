/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmProcessing;

import COSMOSformat.V0Component;
import static COSMOSformat.VFileConstants.FROM_G_CONVERSION;
import static COSMOSformat.VFileConstants.RECORDER_FSI;
import static COSMOSformat.VFileConstants.RECORER_LSB;
import static COSMOSformat.VFileConstants.SENSOR_SENSITIVITY;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.DATA_UNITS_CODE;
import static SmUtilities.SmConfigConstants.DATA_UNITS_NAME;

/**
 *
 * @author jmjones
 */
public class V1Process {
    private final double microToVolt = 1.0e-6;
    
    private double[] V1Array;
    private double meanToZero;
    private double maxVal;
    private int maxIndex;
    private double avgVal;
    
    private final V0Component inV0;
    private final int data_unit_code;
    private final String data_units;
    private final double lsb;
    private final double fsi;
    private final double sensitivity;

    public V1Process(final V0Component v0rec, final ConfigReader config) throws SmException {
        double epsilon = 0.0001;
        double nodata = v0rec.getNoRealVal();
        this.inV0 = v0rec;
        
        //extract needed values from the V0 record and check if defined
        int V0arrLength = v0rec.getDataLength();
        this.lsb = v0rec.getRealHeaderValue(RECORER_LSB);
        this.fsi = v0rec.getRealHeaderValue(RECORDER_FSI);
        this.sensitivity = v0rec.getRealHeaderValue(SENSOR_SENSITIVITY);
        if  (((lsb - 0.0) < epsilon) || ((lsb - nodata) < epsilon)){
            throw new SmException("Real header #" + (RECORER_LSB + 1) + 
                            ", recorder least sig. bit, is invalid: " + lsb);
        }
//        if (((fsi - 0.0) < epsilon) || ((fsi - nodata) < epsilon)) {
//            throw new SmException("Real header #" + (RECORDER_FSI + 1) + 
//                     ", recorder full-scale input, is invalid: " + RECORDER_FSI);
//        }
        if (((sensitivity - 0.0) < epsilon) || ((sensitivity - nodata) < epsilon)){
            throw new SmException("Real header #" + (SENSOR_SENSITIVITY + 1) + 
                            ", sensor sensitivity, is invalid: " + sensitivity);
        }
        
        //Get config values with a default of cm/sec2 if not defined
        String unitcode = config.getConfigValue(DATA_UNITS_CODE);
        this.data_unit_code = (unitcode == null) ? 4 : Integer.parseInt(unitcode);
        
        String unitname = config.getConfigValue(DATA_UNITS_NAME);
        this.data_units = (unitname == null) ? "cm/sec2" : unitname;
        
        //initialize the results values
        this.meanToZero = 0.0;
        this.maxVal = Double.MIN_VALUE;  //Set default to large neg. number
        this.maxIndex = -1;  //Set index to out-of-range
        this.avgVal = 0.0;
    }
    
    public void processV1Data() {
        double conv = countToCMSConversion();
        DataVals result = countsToValues(inV0.getDataArray(), conv);
        V1Array = result.array;
        meanToZero = result.mean;
        maxVal = result.max;
        maxIndex = result.maxIndex;
    }
    
    public double countToGConversion() {
        double result = ( lsb * microToVolt) / sensitivity;
        return result;        
    }
    
    public double countToCMSConversion() {
        //sensor calculation of volts per count and cm per sq. sec per volt
        //countToCMS units are cm per sq. sec per count
        //This is multiplied by each count to get the sensor value in cm per sq. sec
        
        double result = (( lsb * microToVolt) / sensitivity) * FROM_G_CONVERSION;
        return result;
    }
    
    public DataVals countsToValues(final int[] inArray, final double countConv) {
        
        int length = inArray.length;
        double[] result = new double[length];
        double total = 0.0;
        double max = Double.MIN_VALUE;
        double firstMean = Double.MIN_VALUE;
        int maxid = -1;
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] * countConv;
            total = total + result[i];
        }
        firstMean = total / length;
        
        total = 0.0;
        for (int i = 0; i < length; i++) {
            result[i] = result[i] - firstMean;
            total = total + result[i];
            if (result[i] > max) {
                max = result[i];
                maxid = i;
            }
        }
        return (new DataVals( result, firstMean, max, maxid));
    }

    public double getMeanToZero() {
        return this.meanToZero;
    }
    public double getMaxVal() {
        return this.maxVal;
    }
    public int getMaxIndex() {
        return this.maxIndex;
    }
    public double getAvgVal() {
        return this.avgVal;
    }
    public double[] getV1Array() {
        return this.V1Array;
    }
    public int getV1ArrayLength() {
        return this.V1Array.length;
    }
    public int getDataUnitCode() {
        return this.data_unit_code;
    }
    public String getDataUnits() {
        return this.data_units;
    }
    class DataVals {
        public final double[] array;
        public final double max;
        public final double mean;
        public final int maxIndex;

        public DataVals(double[] inArray, double inMean, double inMax, int inIndex) {
            max = inMax;
            mean = inMean;
            maxIndex = inIndex;
            array = inArray;
        }
    }
}
