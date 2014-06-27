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
import static SmUtilities.SmConfigConstants.BP_FILTER_CUTOFFHIGH;
import static SmUtilities.SmConfigConstants.BP_FILTER_CUTOFFLOW;
import static SmUtilities.SmConfigConstants.DATA_UNITS_CODE;
import static SmUtilities.SmConfigConstants.DATA_UNITS_NAME;


/**
 *
 * @author jmjones
 */
public class V1Process {
    private final double microToVolt = 1.0e-6;
    
    private double[] accel;
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
    
    private final double lowcutoff;
    private final double highcutoff;
    private final double delta_t;

    public V1Process(final V0Component v0rec, final ConfigReader config) throws SmException {
        double epsilon = 0.0001;
        double nodata = v0rec.getNoRealVal();
        this.inV0 = v0rec;
        
        //extract needed values from the V0 record and check if defined
        int V0arrLength = v0rec.getDataLength();
        this.lsb = v0rec.getRealHeaderValue(RECORER_LSB);
        this.fsi = v0rec.getRealHeaderValue(RECORDER_FSI);
        delta_t = v0rec.getRealHeaderValue(DELTA_T);
        this.sensitivity = v0rec.getRealHeaderValue(SENSOR_SENSITIVITY);
        if  (((lsb - 0.0) < epsilon) || ((lsb - nodata) < epsilon)){
            throw new SmException("Real header #" + (RECORER_LSB + 1) + 
                            ", recorder least sig. bit, is invalid: " + lsb);
        }
        if (((sensitivity - 0.0) < epsilon) || ((sensitivity - nodata) < epsilon)){
            throw new SmException("Real header #" + (SENSOR_SENSITIVITY + 1) + 
                            ", sensor sensitivity, is invalid: " + sensitivity);
        }
        if (((delta_t - nodata) < epsilon) || (delta_t < 0.0)){
            throw new SmException("Real header #62, delta t, is invalid: " + 
                                                                        delta_t);
        }        
        
        //Get config values or use defaults if not defined
        String unitname = config.getConfigValue(DATA_UNITS_NAME);
        this.data_units = (unitname == null) ? CMSQSECT : unitname;
        
        try {
            String unitcode = config.getConfigValue(DATA_UNITS_CODE);
            this.data_unit_code = (unitcode == null) ? CMSQSECN : Integer.parseInt(unitcode);

            String lowcut = config.getConfigValue(BP_FILTER_CUTOFFLOW);
            this.lowcutoff = (lowcut == null) ? DEFAULT_LOWCUT : Double.parseDouble(lowcut);

            String highcut = config.getConfigValue(BP_FILTER_CUTOFFHIGH);
            this.highcutoff = (highcut == null) ? DEFAULT_HIGHCUT : Double.parseDouble(highcut);
        } catch (NumberFormatException err) {
            throw new SmException("Error extracting numeric values from configuration file");
        }
        
        //initialize the results values
        this.meanToZero = 0.0;
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
        
        //convert counts to physical values
        double[] physVal = countsToValues(inV0.getDataArray(), conv);
        ArrayStats stat = new ArrayStats( physVal );
        meanToZero = stat.getMean();
        
        //save a copy of the original array for pre-mean removal
        double[] accraw = new double[physVal.length];
        System.arraycopy( physVal, 0, accraw, 0, physVal.length);
        
        //remove the mean
        for (int i = 0; i < physVal.length; i++) {
            physVal[i] = physVal[i] - meanToZero;
        }
        
        //filter the data
        double dtime = delta_t * MSEC_TO_SEC;
        System.out.println("+++ deltat: " + delta_t);
        System.out.println("+++ time step: " + dtime);
        System.out.println("+++ F lowcut: " + lowcutoff);
        System.out.println("+++ F highcut: " + highcutoff);
        System.out.println("+++ length before filter: " + physVal.length);
        
        //set up the filter coefficients and run
        ButterworthFilter filter = new ButterworthFilter();
        boolean calcWorked = filter.calculateCoefficients(lowcutoff, highcutoff, dtime, NUM_POLES, true);
        if (calcWorked) {
            accel = filter.applyFilter(physVal);
        } else {
            throw new SmException("Invalid filter values");
        }
        
        double[] b1 = filter.getB1();
        double[] b2 = filter.getB2();
        double[] fact = filter.getFact();
        for (int jj = 0; jj < b1.length; jj++) {
            System.out.format("+++ fact: %f  b1: %f  b2: %f%n", fact[jj],b1[jj],b2[jj]);
        }
        //P-wave picking
        Ppicker pick = new Ppicker( dtime );
        int startIndex = pick.pickPwave(accel);
        
        //Remove pre-event mean from acceleration record
        
        //Baseline correction (if needed)
        
        
        stat = new ArrayStats( accel );
        avgVal = stat.getMean();
        maxVal = stat.getPeakVal();
        maxIndex = stat.getPeakValIndex();
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
}
