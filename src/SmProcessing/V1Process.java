/*******************************************************************************
 * Name: Java class V1Process.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

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
public class V1Process extends V1ProcessFormat {
    private final V0Component inV0;
    private final int data_unit_code;
    private final String data_units;
    private final double lsb;
    private final double sensitivity;
    
    /**
     * The extended constructor for V1Process retrieves the recorder LSB (least significant
     * bit) and the sensor sensitivity from the Real Header and screens for
     * invalid values.  It also retrieves the data units name and code from
     * the configuration file if available.  If these values are not defined,
     * default values are used instead.
     * @param v0rec the reference to the parent V0 record
     * @throws SmException if unable to acquire needed real header or configuration
     * parameters
     */
    public V1Process(final V0Component v0rec) throws SmException {
        super();
        double epsilon = 0.0001;
        double nodata = v0rec.getNoRealVal();
        this.inV0 = v0rec;
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
    }
    /**
     * This method performs the actual processing by calling methods from the
     * processing api to convert the counts to physical values and remove the
     * mean.
     */
    @Override
    public void processV1Data(){
        //Get the units from the config file and calculate conversion factor
        double conv;
        if (data_unit_code == CMSQSECN) {
            conv = RawTraceConversion.countToCMS(lsb, sensitivity, FROM_G_CONVERSION);
        } else {
            conv = RawTraceConversion.countToG(lsb, sensitivity);
        }

        //convert counts to physical values
        accel = ArrayOps.countsToPhysicalValues(inV0.getDataArray(), conv);
        
        //Remove the mean from the array and save for the Real Header
        meanToZero = ArrayOps.findAndRemoveMean(accel);
        
        //Find the new mean (should now be zero) and the location and mag. of peak value
        ArrayStats stat = new ArrayStats( accel );
        avgVal = stat.getMean();
        peakVal = stat.getPeakVal();
        peakIndex = stat.getPeakValIndex();
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
}
