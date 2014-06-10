/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmProcessing;

import COSMOSformat.V0Component;
import static COSMOSformat.VFileConstants.*;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.DATA_UNITS_CODE;
import static SmUtilities.SmConfigConstants.DATA_UNITS_NAME;
import net.alomax.freq.ButterworthFilter;


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
        //Where should the check for correct range go? !!!
        String unitcode = config.getConfigValue(DATA_UNITS_CODE);
        this.data_unit_code = (unitcode == null) ? CMSQSECN : Integer.parseInt(unitcode);
        
        String unitname = config.getConfigValue(DATA_UNITS_NAME);
        this.data_units = (unitname == null) ? CMSQSECT : unitname;
        
        //initialize the results values
        this.meanToZero = 0.0;
        this.maxVal = Double.MIN_VALUE;  //Set default to large neg. number
        this.maxIndex = -1;  //Set index to out-of-range
        this.avgVal = 0.0;
    }
    
    public void processV1Data() {
        //Get the units from the config file and calculate conversion factor
        double conv;
        if (data_unit_code == CMSQSECN) {
            conv = countToCMSConversion();
        } else {
            conv = countToGConversion();
        }
        
        //convert counts to physical values
        double[] physVal = countsToValues(inV0.getDataArray(), conv);
        
        //remove the mean
        DataVals result = removeMean(physVal);
        V1Array = result.array;
        meanToZero = result.mean;
        maxVal = result.max;
        maxIndex = result.maxIndex;
        
        //filter the data
        ButterworthFilter butter = new ButterworthFilter();
    }
    
    public double countToGConversion() {
        //test this!!!
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
    
    public double[] countsToValues(final int[] inArray, final double countConv) {
        
        int length = inArray.length;
        double[] result = new double[length];
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] * countConv;
        }
        return (result);
    }
    private DataVals removeMean(final double[] inArray) {
        
        int length = inArray.length;
        double[] result = new double[length];
        double total = 0.0;
        double maxhigh = Double.MIN_VALUE;
        double maxlow = Double.MAX_VALUE;
        double maxabs = Double.MIN_VALUE;
        double mean = Double.MIN_VALUE;
        int maxhighid = -1;
        int maxlowid = -1;
        int maxabsid = -1;
        
        for (double val : inArray ) {
             total = total + val;
        }
        mean = total / length;
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] - mean;
            if (result[i] > maxhigh) {
                maxhigh = result[i];
                maxhighid = i;
            }
            if (result[i] < maxlow) {
                maxlow = result[i];
                maxlowid = i;
            }
        }
        if (Math.abs(maxhigh) > Math.abs(maxlow)) {
            maxabs =  maxhigh;
            maxabsid = maxhighid;
        } else {
            maxabs =  maxlow;
            maxabsid = maxlowid;
            
        }
        return (new DataVals( result, mean, maxabs, maxabsid));
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
