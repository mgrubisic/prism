/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmProcessing;

import COSMOSformat.V0Component;
import static SmConstants.VFileConstants.*;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;

/**
 *
 * @author jmjones
 */
public class V1Process {
    private final double MICRO_TO_VOLT = 1.0e-6;
    
    private double[] accel;
    private double meanToZero;
    private double maxVal;
    private int maxIndex;
    private double avgVal;
    
    private final V0Component inV0;
    private final int data_unit_code;
    private final String data_units;
    private final double lsb;
    private final double sensitivity;
    private double conversion_factor;
    
    public V1Process(final V0Component v0rec) throws SmException {
        double epsilon = 0.0001;
        double nodata = v0rec.getNoRealVal();
        this.inV0 = v0rec;
        this.meanToZero = 0.0;
        ConfigReader config = ConfigReader.INSTANCE;
        
        //extract needed values from the V0 record and check if defined
        this.lsb = v0rec.getRealHeaderValue(RECORER_LSB);
        this.sensitivity = v0rec.getRealHeaderValue(SENSOR_SENSITIVITY);
        if  ((Math.abs(lsb - 0.0) < epsilon) || (Math.abs(lsb - nodata) < epsilon)){
            throw new SmException("Real header #" + (RECORER_LSB + 1) + 
                            ", recorder least sig. bit, is invalid: " + lsb);
        }
        if ((Math.abs(sensitivity - 0.0) < epsilon) || (Math.abs(sensitivity - nodata) < epsilon)){
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
        System.out.println("+++ V1 conversion: " + conv);
        
        //convert counts to physical values
        accel = ArrayOps.countsToPhysicalValues(inV0.getDataArray(), conv);
        
        //Remove the mean from the array and save for the Real Header
        ArrayStats accmean = new ArrayStats( accel );
        meanToZero = accmean.getMean();
        ArrayOps.removeMean(accel, meanToZero);
        
        ArrayStats stat = new ArrayStats( accel );
        avgVal = stat.getMean();
        maxVal = stat.getPeakVal();
        maxIndex = stat.getPeakValIndex();
    }
    
    public double countToGConversion() {
        double result = ( lsb * MICRO_TO_VOLT) / sensitivity;
        return result;        
    }
    
    public double countToCMSConversion() {
        //sensor calculation of volts per count and cm per sq. sec per volt
        //countToCMS units are cm per sq. sec per count
        //This is multiplied by each count to get the sensor value in cm per sq. sec
        
        double result = (( lsb * MICRO_TO_VOLT) / sensitivity) * FROM_G_CONVERSION;
        return result;
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
