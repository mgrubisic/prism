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
import static SmUtilities.SmConfigConstants.*;

/**
 *
 * @author jmjones
 */
public class V1Process {
    private final double microToVolt = 1.0e-6;
    
    private double[] accel;
    private double maxVal;
    private int maxIndex;
    private double avgVal;
    
    private final V0Component inV0;
    private final int data_unit_code;
    private final String data_units;
    private final double lsb;
    private final double sensitivity;
    private double conversion_factor;
    
    public V1Process(final V0Component v0rec, final ConfigReader config) throws SmException {
        double epsilon = 0.0001;
        double nodata = v0rec.getNoRealVal();
        this.inV0 = v0rec;
        
        //extract needed values from the V0 record and check if defined
        this.lsb = v0rec.getRealHeaderValue(RECORER_LSB);
        this.sensitivity = v0rec.getRealHeaderValue(SENSOR_SENSITIVITY);
        if  (((lsb - 0.0) < epsilon) || ((lsb - nodata) < epsilon)){
            throw new SmException("Real header #" + (RECORER_LSB + 1) + 
                            ", recorder least sig. bit, is invalid: " + lsb);
        }
        if (((sensitivity - 0.0) < epsilon) || ((sensitivity - nodata) < epsilon)){
            throw new SmException("Real header #" + (SENSOR_SENSITIVITY + 1) + 
                            ", sensor sensitivity, is invalid: " + sensitivity);
        }
        
        //Get config values or use defaults if not defined
        try {
            String unitname = config.getConfigValue(DATA_UNITS_NAME);
            this.data_units = (unitname == null) ? CMSQSECT : unitname;
            
            String unitcode = config.getConfigValue(DATA_UNITS_CODE);
            this.data_unit_code = (unitcode == null) ? CMSQSECN : Integer.parseInt(unitcode);

        } catch (NumberFormatException err) {
            throw new SmException("Error extracting numeric values from configuration file");
        }
        
        //initialize the results values
        this.conversion_factor = 0.0;
        this.maxVal = Double.MIN_VALUE;  //Set default to large neg. number
        this.maxIndex = -1;  //Set index to out-of-range
        this.avgVal = 0.0;
    }
    
    public void processV1Data() throws SmException {
        //Get the units from the config file and calculate conversion factor
        double conv;
        if (data_unit_code == CMSQSECN) {
            conv = countToCMSConversion();
        } else {
            conv = countToGConversion();
        }
        conversion_factor = conv;
        
        //convert counts to physical values
        accel = countsToValues(inV0.getDataArray(), conv);
        ArrayStats stat = new ArrayStats( accel );
        avgVal = stat.getMean();
        maxVal = stat.getPeakVal();
        maxIndex = stat.getPeakValIndex();
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
    
    public double[] countsToValues(final int[] inArray, final double countConv) {
        
        int length = inArray.length;
        double[] result = new double[length];
        
        for (int i = 0; i < length; i++) {
            result[i] = inArray[i] * countConv;
        }
        return (result);
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
        return this.accel;
    }
    public int getV1ArrayLength() {
        return this.accel.length;
    }
    public int getDataUnitCode() {
        return this.data_unit_code;
    }
    public String getDataUnits() {
        return this.data_units;
    }
    public double getConversionFactor() {
        return this.conversion_factor;
    }
}
