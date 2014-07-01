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
import static SmUtilities.SmConfigConstants.BP_FILTER_CUTOFFHIGH;
import static SmUtilities.SmConfigConstants.BP_FILTER_CUTOFFLOW;
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
    private double VmeanToZero;
    private final int vel_unit_code;
    private final String vel_units;
    
    private double[] displace;
    private double DmaxVal;
    private int DmaxIndex;
    private double DavgVal;
    private final int dis_unit_code;
    private final String dis_units;
    
    private final V1Component inV1;
    private final double delta_t;
    private final double noRealVal;
    private final double lowcutoff;
    private final double highcutoff;
    
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
        String lowcut = config.getConfigValue(BP_FILTER_CUTOFFLOW);
        this.lowcutoff = (lowcut == null) ? DEFAULT_LOWCUT : Double.parseDouble(lowcut);

        String highcut = config.getConfigValue(BP_FILTER_CUTOFFHIGH);
        this.highcutoff = (highcut == null) ? DEFAULT_HIGHCUT : Double.parseDouble(highcut);
    }
    
    public void processV2Data() throws SmException {   
        ArrayStats stat;
        
        //Filter the acceleration
        double dtime = delta_t * MSEC_TO_SEC;
        double[] acc = inV1.getDataArray();
        
        //set up the filter coefficients and run
        ButterworthFilter filter = new ButterworthFilter();
        filter.calculateCoefficients(lowcutoff, highcutoff, dtime, NUM_POLES, true);
        accel = filter.applyFilter(acc);
        
        //Integrate the acceleration to get velocity.
        velocity = ArrayOps.Integrate( accel, delta_t);
        
        //Remove any linear trend from velocity
        ArrayOps.removeLinearTrend( velocity, dtime);
        stat = new ArrayStats( velocity );
        VmaxVal = stat.getPeakVal();
        VmaxIndex = stat.getPeakValIndex();
        VavgVal = stat.getMean();
        
        //Differentiate velocity for final acceleration
        accel = ArrayOps.Differentiate(velocity, delta_t);
        stat = new ArrayStats( accel );
        AmaxVal = stat.getPeakVal();
        AmaxIndex = stat.getPeakValIndex();
        AavgVal = stat.getMean();
        
        //Integrate the velocity to get displacement.
        displace = ArrayOps.Integrate( velocity, delta_t);
        stat = new ArrayStats( displace );
        DmaxVal = stat.getPeakVal();
        DmaxIndex = stat.getPeakValIndex();
        DavgVal = stat.getMean();
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
}
