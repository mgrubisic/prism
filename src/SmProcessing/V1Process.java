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
    private double[] array;
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
            throw new SmException("Real header #22, recorder least sig. bit, is invalid: " 
                                                                        + lsb);
        }
        if (((fsi - 0.0) < epsilon) || ((fsi - nodata) < epsilon)) {
            throw new SmException("Real header #23, recorder full-scale input, is invalid: " 
                                                                        + fsi);
        }
        if (((sensitivity - 0.0) < epsilon) || ((sensitivity - nodata) < epsilon)){
            throw new SmException("Real header #42, sensor sensitivity, is invalid: " 
                                                                + sensitivity);
        }
        //Set the length of the V1 data array to the same as the V0
        this.array = new double[V0arrLength];
        
        //Get config values with a default of cm/sec2 if not defined
        String unitcode = config.getConfigValue(DATA_UNITS_CODE);
        this.data_unit_code = (unitcode == null) ? 4 : Integer.parseInt(unitcode);
        
        String unitname = config.getConfigValue(DATA_UNITS_NAME);
        this.data_units = (unitname == null) ? "cm/sec2" : unitname;
        
        //initialize the results values
        this.meanToZero = 0.0;
        this.maxVal = -1.0e10;  //Set default to large neg. number
        this.maxIndex = -1;  //Set index to out-of-range
        this.avgVal = 0.0;
    }
    
    public void processV1Data() {
        double conv = countToCMSConversion();
        countsToValues(inV0.getDataArray(), conv);
    }
    
    public double countToGConversion() {
        double microToVolt = 1.0e-6;
        double result = ((fsi / lsb) * microToVolt) * (sensitivity);
        return result;        
    }
    
    public double countToCMSConversion() {
        //sensor calculation of volts per count and cm per sq. sec per volt
        //countToCMS units are cm per sq. sec per count
        //This is multiplied by each count to get the sensor value in cm per sq. sec
        double microToVolt = 1.0e-6;
        double result = ((fsi / lsb) * microToVolt) * (FROM_G_CONVERSION * sensitivity);
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
        return this.array;
    }
    public int getV1ArrayLength() {
        return this.array.length;
    }
    public int getDataUnitCode() {
        return this.data_unit_code;
    }
    public String getDataUnits() {
        return this.data_units;
    }
}
