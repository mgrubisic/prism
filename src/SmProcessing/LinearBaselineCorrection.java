/*
 * Copyright (C) 2015 jmjones
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

import SmConstants.VFileConstants;
import SmException.SmException;
import SmUtilities.SmDebugLogger;

/**
 *
 * @author jmjones
 */
public class LinearBaselineCorrection {
    private final int RESULT_PARMS = 14;
    
    private double dtime;
    private double lowcut;
    private double highcut;
    private int numpoles;
    private int estart;
    private double taplength;
    private double[] velocity;
    private double[] velstart;
    private double[] displace;
    private double[] accel;
    private double[] bnn;
    private SmDebugLogger elog;
    private boolean writeArrays;
    private int taplength_calculated;
    private double[] onerun;
    
    public LinearBaselineCorrection(double delttime, double[] invel, 
                                      double lowcut,double highcut,int numpoles,
                                      int ppick, double taplengthtime) {
        this.dtime = delttime;
        this.estart = ppick;
        this.taplength = taplengthtime;
        this.velstart = invel;
        this.lowcut = lowcut;
        this.highcut = highcut;
        this.numpoles = numpoles;
        this.elog = SmDebugLogger.INSTANCE;
        this.taplength_calculated = 0;
        onerun = new double[RESULT_PARMS];
    }
        
    public VFileConstants.V2Status findFit() throws SmException {
        int vlen = velstart.length;
        velocity = new double[vlen];
        boolean success;
        ButterworthFilter filter;
        double[] paddedvelocity;
        double[] paddeddisplace;
        VFileConstants.V2Status status;
        QCcheck qcchecker = new QCcheck();
        if (!qcchecker.validateQCvalues()){
            throw new SmException("Error extracting QC values from configuration file");
        }
        qcchecker.findWindow(lowcut, (1.0/dtime), estart);
        filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcut, 
                                                highcut,dtime, numpoles, true);
        if (!valid) {
            throw new SmException("ABC: Invalid bandpass filter input parameters");
        }
        
        velocity = makeCorrection(velstart, estart);
        //now filter velocity
        paddedvelocity = filter.applyFilter(velocity, taplength, estart);
        taplength_calculated = filter.getTaperlength();
        //remove any mean value
        ArrayStats velmean = new ArrayStats( paddedvelocity );
        ArrayOps.removeValue(paddedvelocity, velmean.getMean());
        //integrate to get displacement, differentiate
        //for acceleration
        paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
        displace = new double[velocity.length];
        System.arraycopy(paddedvelocity, filter.getPadLength(), velocity, 0, velocity.length);
        System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
        accel = ArrayOps.Differentiate(velocity, dtime);
        qcchecker.qcVelocity(velocity);
        qcchecker.qcDisplacement(displace);
        //store the results in an array for comparison  
        onerun[0] = 0.0;
        onerun[1] = Math.abs(qcchecker.getResidualDisplacement());
        onerun[2] = Math.abs(qcchecker.getInitialVelocity());
        onerun[3] = Math.abs(qcchecker.getResidualVelocity());
        onerun[4] = estart;
        onerun[5] = 0;
        onerun[6] = 1;
        onerun[7] = 1;

        success = (onerun[2] <= qcchecker.getInitVelocityQCval()) && 
                    (onerun[3] <= qcchecker.getResVelocityQCval()) && 
                        (onerun[1] <= qcchecker.getResDisplaceQCval());
        onerun[8] = (success) ? 1 : 0;  //QC flag of pass or fail
        onerun[9] = 0.0;
        onerun[10] = 0.0;
        onerun[11] = 0.0;
        onerun[12] = velocity[0];
        onerun[13]= displace[0];

        status = (success) ? VFileConstants.V2Status.GOOD : VFileConstants.V2Status.FAILQC;
        return status;
    }
    
    public double[] makeCorrection(double[] array, int breakpt ) {
        double[] h1;
        double[] h2;
        double[] b1;
        double[] b2;
        double[] result = new double[ array.length ];

        //break apart the input array into 2 segments, using the breakpt value
        h1 = new double[breakpt];
        h2 = new double[array.length-breakpt];
        System.arraycopy(array, 0,       h1, 0, breakpt);
        System.arraycopy(array, breakpt, h2, 0, array.length-breakpt);
                
        //find the best straight line fit for each segment
        b1 = new double[breakpt];
        b2 = new double[array.length-breakpt];
        b1 = ArrayOps.findLinearTrend( h1, dtime );
        b2 = ArrayOps.findLinearTrend( h2, dtime );

        //put the 2 first-order baseline functions together
        bnn = new double[array.length];
        System.arraycopy(b1, 0, bnn, 0,       b1.length);
        System.arraycopy(b2, 0, bnn, breakpt, b2.length);
        
        //Remove the baseline function from the input array
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i] - bnn[i];
        }
        return result;
    }
    public double[] getBaselineFunction() {
        return bnn;
    }
    public double[] getABCvelocity() {
        return velocity;
    }
    public double[] getABCdisplacement() {
        return displace;
    }
    public double[] getABCacceleration() {
        return accel;
    }
    public double[] getParams() {
        return onerun;
    }
    public int getCalculatedTaperLength() {
        return this.taplength_calculated;
    }
}
