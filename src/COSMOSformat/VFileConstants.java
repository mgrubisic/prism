/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

/**
 * This class defines the constants used during strong motion processing, including
 * constants from the tables defined in the COSMOS Strong Motion Data Format document.
 * @author jmjones
 */
public final class VFileConstants {
    //text header markers
    public static final int HEADLINE_1_LENGTH = 59;
    public static final int END_OF_DATATYPE = 24;
    public static final int NUM_HEAD_START = 46;
    public static final int NUM_HEAD_END = 48;
    
    public static final int MAX_LINE_LENGTH = 80;
    public static final int SENSOR_LOCATION_LINE = 8;
    public static final int SENSOR_LOCATION_START = 46;
    public static final int NODATA_LINE = 12;
    public static final int END_OF_DATA_CHAN = 21;
    
    //data arrays, display default parameters
    public static final int DEFAULT_NOINTVAL = -999;
    public static final double DEFAULT_NOREALVAL = -999.0;
    public static final int DEFAULT_INT_FIELDWIDTH = 8;
    public static final int DEFAULT_REAL_FIELDWIDTH = 13;
    public static final int DEFAULT_REAL_PRECISION = 6;
    public static final int REAL_FIELDWIDTH_V1 = 10;
    public static final int REAL_PRECISION_V1 = 3;
    public static final String DEFAULT_REAL_DISPLAYTYPE = "F";
    public static final String DEFAULT_INT_DISPLAYTYPE = "I";
    
    //data product names
    public static final String RAWACC =   "Raw acceleration counts  ";
    public static final String UNCORACC = "Uncorrected acceleration ";
    public static final String CORACC =   "Corrected acceleration   ";
    public static final String VELOCITY = "Velocity data            ";
    public static final String DISPLACE = "Displacement data        ";
    public static final String SPECTRA =  "Response spectra         ";
    
    public enum V2DataType { ACC, VEL, DIS }
    
    //units names and codes
    public static final String CMSQSECT = "cm/sec2";
    public static final int CMSQSECN = 4;
    public static final String CMSECT = "cm/sec";
    public static final int CMSECN = 5;
    public static final String CMT = "cm";
    public static final int CMN = 6;
    
    public enum OutputStyle { SINGLE_CHANNEL, BUNDLED };
    
    //table 1 data physical parameter codes
    public static final int ACC_PARM_CODE = 1;
    public static final int VEL_PARM_CODE = 2;
    public static final int DIS_ABS_PARM_CODE = 3;
    
    //int header index values
    public static final int STATION_CHANNEL_NUMBER = 49;
    public static final int LENGTH_OF_RAW_RECORD = 34;
    public static final int PROCESSING_STAGE_INDEX = 0;
    public static final int DATA_PHYSICAL_PARAM_CODE = 1;
    public static final int V_UNITS_INDEX = 2;
    public static final int PROCESSING_AGENCY = 13;
    
    //real header index values
    public static final int MEAN_ZERO = 35;
    public static final int DELTA_T = 61;
    public static final int MAX_VAL = 63;
    public static final int MAX_VAL_TIME = 64;
    public static final int AVG_VAL = 65;
    public static final int RECORER_LSB = 21;  //recorder least significant bit in microvolts
    public static final int RECORDER_FSI = 22;  //recorder full scale input in volts
    public static final int SENSOR_SENSITIVITY = 41; //in volts per g
    public static final int SCALING_FACTOR = 87;
    
    //Processing stages
    public static final int V1_STAGE = 1;
    public static final int V2_STAGE = 2;
    public static final int V3_STAGE = 3;
    
    //misc. constants
    public static final double MSEC_TO_SEC = 1e-3; //milliseconds to seconds
    public static final double FROM_G_CONVERSION = 980.665; //cm per sq. sec per g

        //filtering constants
    public static final int NUM_POLES = 5;
    public static final double DEFAULT_HIGHCUT = 0.5;
    public static final double DEFAULT_LOWCUT = 30.0;

}
