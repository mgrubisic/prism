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
 * The V1Process class executes the steps necessary to generate a V1 product file
 * from a V0 input file.  It retrieves the recorder LSB and sensor sensitivity
 * values from the Real Header and the data units name and code from the 
 * configuration file.  Based on the units specified, the raw trace counts are
 * converted to physical values.  Then the mean of the data array is removed.
 * The mean value that was removed from the array is saved for updating the
 * header, and the final array peak value, peak value index, and new mean (which
 * should be 0.0) are calculated as well.
 * 
 * @author jmjones
 */
public class V1Process {
    private double[] accel;
    private double meanToZero;
    private double peakVal;
    private int peakIndex;
    private double avgVal;
    
    private final V0Component inV0;
    private final int data_unit_code;
    private final String data_units;
    private final double lsb;
    private final double sensitivity;
    private double conversion_factor;
    
    /**
     * The constructor for V1Process retrieves the recorder LSB (least significant
     * bit) and the sensor sensitivity from the Real Header and screens for
     * invalid values.  It also retrieves the data units name and code from
     * the configuration file if available.  If these values are not defined,
     * default values are used instead.
     * @param v0rec the reference to the parent V0 record
     * @throws SmException if unable to acquire needed real header or config
     * parameters
     */
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
        this.peakVal = Double.MIN_VALUE;  //Set default to large neg. number
        this.peakIndex = -1;  //Set index to out-of-range
        this.avgVal = 0.0;
    }
    /**
     * This method performs the actual processing by calling methods from the
     * processing api to convert the counts to physical values and remove the
     * mean.
     */
    public void processV1Data() {
        //Get the units from the config file and calculate conversion factor
        double conv;
        if (data_unit_code == CMSQSECN) {
            conv = RawTraceConversion.countToCMS(lsb, sensitivity, FROM_G_CONVERSION);
        } else {
            conv = RawTraceConversion.countToG(lsb, sensitivity);
        }
        conversion_factor = conv;
//        System.out.println("+++ V1 conversion: " + conv);
        
        //convert counts to physical values
        accel = ArrayOps.countsToPhysicalValues(inV0.getDataArray(), conv);
        
        //Remove the mean from the array and save for the Real Header
        ArrayStats accmean = new ArrayStats( accel );
        meanToZero = accmean.getMean();
        ArrayOps.removeMean(accel, meanToZero);
        
        ArrayStats stat = new ArrayStats( accel );
        avgVal = stat.getMean();
        peakVal = stat.getPeakVal();
        peakIndex = stat.getPeakValIndex();
    }
    /**
     * Getter for the mean value removed from the array.
     * @return the mean that was removed from the array
     */
    public double getMeanToZero() {
        return this.meanToZero;
    }
    /**
     * Getter for the peak value in the array (after mean removal).  This is the
     * largest number in the array, either positive or negative.
     * @return the peak value
     */
    public double getPeakVal() {
        return this.peakVal;
    }
    /** Getter for the index in the array where the peak value occurs.
     * @return the array index of the peak value
     */
    public int getPeakIndex() {
        return this.peakIndex;
    }
    /**
     * Getter for the average value of the final array, which should be 0.0.
     * @return the array mean
     */
    public double getAvgVal() {
        return this.avgVal;
    }
    /**
     * Getter for a reference to the data array
     * @return reference to the data array
     */
    public double[] getV1Array() {
        return this.accel;
    }
    /**
     * Getter for the length of the data array
     * @return length of the data array
     */
    public int getV1ArrayLength() {
        return this.accel.length;
    }
    /**
     * getter for the data unit code used in processing the V1 array
     * @return the data unit code (COSMOS format, Table 2)
     */
    public int getDataUnitCode() {
        return this.data_unit_code;
    }
    /**
     * Getter for the data units used in processing the V1 array
     * @return the data units (COSMOS format, Table 2)
     */
    public String getDataUnits() {
        return this.data_units;
    }
    /**
     * Getter for the calculated conversion factor used to convert the counts
     * to physical values.  This is the value returned from the RawTraceConversion
     * method.
     * @return the value used to convert counts to physical values
     */
    public double getConversionFactor() {
        return this.conversion_factor;
    }
}
