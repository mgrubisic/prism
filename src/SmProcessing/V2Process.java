/*
 * Copyright (C) 2014 jmjones
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SmProcessing;

import COSMOSformat.V1Component;
import static COSMOSformat.VFileConstants.*;
import COSMOSformat.VFileConstants.V2DataType;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.DATA_UNITS_CODE;
import static SmUtilities.SmConfigConstants.DATA_UNITS_NAME;

/**
 *
 * @author jmjones
 */
public class V2Process {
    //need 3 sets of these params, for each data type
    private double[] accel;
    private double AmaxVal;
    private int AmaxIndex;
    private double AavgVal;
    private final int acc_unit_code;
    private final String acc_units;
    
    private double[] velocity;
    private double VmaxVal;
    private int VmaxIndex;
    private double VavgVal;
    private final int vel_unit_code;
    private final String vel_units;
    
    private double[] displace;
    private double DmaxVal;
    private int DmaxIndex;
    private double DavgVal;
    private final int dis_unit_code;
    private final String dis_units;
    
    private final V1Component inV1;
    private DataVals result;
    private final double delta_t;
    private final double noRealVal;
    
    public V2Process(final V1Component v1rec, final ConfigReader config) throws SmException {
        double epsilon = 0.0001;
        this.inV1 = v1rec;
        
        //Get config values to cm/sec2 (acc), cm/sec (vel), cm (dis)
        this.acc_unit_code = CMSQSECN;
        this.vel_unit_code = CMSECN;
        this.dis_unit_code = CMN;
        
        this.acc_units = CMSQSECT;
        this.vel_units = CMSECT;
        this.dis_units = CMT;
        
        this.noRealVal = inV1.getNoRealVal();
        //verify that real header value delta t is defined and valid
        delta_t = inV1.getRealHeaderValue(DELTA_T);
        if (((delta_t - noRealVal) < epsilon) || (delta_t < 0.0)){
            throw new SmException("Real header #62, delta t, is invalid: " + 
                                                                        delta_t);
        }        
    }
    
    public void processV2Data() {        
        //Integrate the V1 acceleration to get velocity.  Scale results for
        //correct units if necessary.!!!
        result = SmIntegrate( inV1.getDataArray(), delta_t);
        velocity = result.array;
        VmaxVal = result.max;
        VmaxIndex = result.maxIndex;
        VavgVal = result.mean;
        
        //Integrate the velcity to get displacement.
        result = SmIntegrate( velocity, delta_t);
        displace = result.array;
        DmaxVal = result.max;
        DmaxIndex = result.maxIndex;
        DavgVal = result.mean;

        //Differentiate the velocity to get corrected acceleration.
        result = SmDifferentiate( velocity, delta_t);
        accel = result.array;
        AmaxVal = result.max;
        AmaxIndex = result.maxIndex;
        AavgVal = result.mean;    
    }
    private DataVals SmIntegrate( final double[] inArray, double deltat ){
        double max  = 0.0;
        double mean = 0.0;
        int index = 0;
        double total = 0.0;
        double[] calc = new double[ inArray.length ];
        double dt2 = deltat / 2.0;
        calc[0] = 0.0;
        for (int i = 1; i < calc.length; i++) {
            calc[i] = calc[i-1] + (inArray[i-1] + inArray[i])*dt2;
            total = total + calc[i];
            if (calc[i] > max){
                max = calc[i];
                index = i;
            }
        }
        mean = total / calc.length;
        return (new DataVals(calc, mean, max, index ));
    }
    private DataVals SmDifferentiate( final double[] inArray, double deltat ){
        int len = inArray.length;
        double[] calc = new double[ len ];
        calc[0] = (inArray[1] - inArray[0]) / deltat;
        double max  = calc[0];
        double mean = calc[0];
        int index = 0;
        double total = calc[0];
        for (int i = 1; i < len-2; i++) {
            calc[i] = (inArray[i+1] - inArray[i-1]) / (deltat * 2.0);
            total = total + calc[i];
            if (calc[i] > max){
                max = calc[i];
                index = i;
            }
        }
        calc[len-1] = (inArray[len-1] - inArray[len-2]) / deltat;
        total = total + calc[len-1];
        if (calc[len-1] > max) {
            max = calc[len-1];
            index = len-1;
            
        }
        mean = total / calc.length;
        return (new DataVals(calc, mean, max, index ));
    }
    public double getMaxVal(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.AmaxVal;
        } else if (dType == V2DataType.VEL) {
            return this.VmaxVal;
        } else {
            return this.DmaxVal;
        }
    }
    public int getMaxIndex(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.AmaxIndex;
        } else if (dType == V2DataType.VEL) {
            return this.VmaxIndex;
        } else {
            return this.DmaxIndex;
        }
    }
    public double getAvgVal(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.AavgVal;
        } else if (dType == V2DataType.VEL) {
            return this.VavgVal;
        } else {
            return this.DavgVal;
        }
    }
    public double[] getV2Array(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.accel;
        } else if (dType == V2DataType.VEL) {
            return this.velocity;
        } else {
            return this.displace;
        }
    }
    public int getV2ArrayLength(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.accel.length;
        } else if (dType == V2DataType.VEL) {
            return this.velocity.length;
        } else {
            return this.displace.length;
        }
    }
    public int getDataUnitCode(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.acc_unit_code;
        } else if (dType == V2DataType.VEL) {
            return this.vel_unit_code;
        } else {
            return this.dis_unit_code;
        }
    }
    public String getDataUnits(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.acc_units;
        } else if (dType == V2DataType.VEL) {
            return this.vel_units;
        } else {
            return this.dis_units;
        }
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
