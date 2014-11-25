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
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.EventOnsetType;
import SmConstants.VFileConstants.MagnitudeType;
import SmConstants.VFileConstants.V2DataType;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.*;
import SmUtilities.SmErrorLogger;
import SmUtilities.SmTimeFormatter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author jmjones
 */
public class V2Process {
    //need 3 sets of these params, for each data type
    private double[] accel;
    private double ApeakVal;
    private int ApeakIndex;
    private double AavgVal;
    private final int acc_unit_code;
    private final String acc_units;
    
    private double[] velocity;
    private double VpeakVal;
    private int VpeakIndex;
    private double VavgVal;
    private final int vel_unit_code;
    private final String vel_units;
    
    private double[] displace;
    private double DpeakVal;
    private int DpeakIndex;
    private double DavgVal;
    private final int dis_unit_code;
    private final String dis_units;
    
    private final V1Component inV1;
    private final int data_unit_code;
    private final double dtime;
    private final double samplerate;
    private final double noRealVal;
    private final double lowcutoff;
    private final double highcutoff;
    private double lowcutadj;
    private double highcutadj;
    private double mmag;
    private double lmag;
    private double smag;
    private double omag;
    private double magnitude;
    private MagnitudeType magtype;
    
    private int pickIndex;
    private int startIndex;
    private double ebuffer;
    private EventOnsetType emethod;
    private final int numpoles;  // the filter order is 2*numpoles
    private double taperlength;
    
    private V2Status procStatus;
    private QCcheck qcchecker;
    
    private ArrayList<String> errorlog;
    private boolean writeDebug;
    private SmErrorLogger elog;
    private String[] logstart;
    private final File V0name;
    private final String channel;
    
    private double[] bracketedTimes;
    private double bracketedDuration;
    private double AriasIntensity;
        
    public V2Process(final V1Component v1rec, File inName) throws SmException {
        double epsilon = 0.0001;
        this.inV1 = v1rec;
        this.lowcutadj = 0.0;
        this.highcutadj = 0.0;
        errorlog = new ArrayList<>();
        elog = SmErrorLogger.INSTANCE;
        ConfigReader config = ConfigReader.INSTANCE;
        writeDebug = false;
        this.V0name = inName;
        this.channel = inV1.getChannel();
        
        //Get config values to cm/sec2 (acc), cm/sec (vel), cm (dis)
        this.acc_unit_code = CMSQSECN;
        this.vel_unit_code = CMSECN;
        this.dis_unit_code = CMN;
        
        this.acc_units = CMSQSECT;
        this.vel_units = CMSECT;
        this.dis_units = CMT;
        this.pickIndex = 0;
        this.startIndex = 0;
        this.procStatus = V2Status.NOEVENT;
        
        this.bracketedDuration = 0.0;
        this.AriasIntensity = 0.0;
        
        SmTimeFormatter timer = new SmTimeFormatter();
        String logtime = timer.getGMTdateTime();
        logstart = new String[2];
        logstart[0] = "\n";
        logstart[1] = "Prism Error/Debug Log Entry: " + logtime;
        
        this.noRealVal = inV1.getNoRealVal();
        //verify that real header value delta t is defined and valid
        double delta_t = inV1.getRealHeaderValue(DELTA_T);
        if ((Math.abs(delta_t - noRealVal) < epsilon) || (delta_t < 0.0)){
            throw new SmException("Real header #62, delta t, is invalid: " + 
                                                                        delta_t);
        }
        boolean match = false;
        dtime = delta_t * MSEC_TO_SEC;    
        samplerate = 1.0 / dtime;
        for (double each : V3_SAMPLING_RATES) {
            if (Math.abs(each - samplerate) < epsilon) {
                match = true;
            }
        }
        if (!match) {
            throw new SmException("Real header #62, delta t value, " + 
                                        delta_t + " is out of expected range");
        }
        //Get the earthquake magnitude from the real header array.
        this.mmag = inV1.getRealHeaderValue(MOMENT_MAGNITUDE);
        this.lmag = inV1.getRealHeaderValue(LOCAL_MAGNITUDE);
        this.smag = inV1.getRealHeaderValue(SURFACE_MAGNITUDE);
        this.omag = inV1.getRealHeaderValue(OTHER_MAGNITUDE);
        
        try {
            String unitcode = config.getConfigValue(DATA_UNITS_CODE);
            this.data_unit_code = (unitcode == null) ? CMSQSECN : Integer.parseInt(unitcode);

            String lowcut = config.getConfigValue(BP_FILTER_CUTOFFLOW);
            this.lowcutoff = (lowcut == null) ? DEFAULT_LOWCUT : Double.parseDouble(lowcut);

            String highcut = config.getConfigValue(BP_FILTER_CUTOFFHIGH);
            this.highcutoff = (highcut == null) ? DEFAULT_HIGHCUT : Double.parseDouble(highcut);

            //The Butterworth filter implementation requires an even number of poles (and order)
            String filorder = config.getConfigValue(BP_FILTER_ORDER);
            this.numpoles = (filorder == null) ? DEFAULT_NUM_POLES : Integer.parseInt(filorder)/2;

            //The Butterworth filter taper length for the half cosine taper
            String taplen = config.getConfigValue(BP_TAPER_LENGTH);
            this.taperlength = (taplen == null) ? DEFAULT_TAPER_LENGTH : Double.parseDouble(taplen);
            
            String pbuf = config.getConfigValue(EVENT_ONSET_BUFFER);
            this.ebuffer = (pbuf == null) ? DEFAULT_EVENT_ONSET_BUFFER : Double.parseDouble(pbuf);
            
            String eventmethod = config.getConfigValue(EVENT_ONSET_METHOD);
            if (eventmethod == null) {
                this.emethod = DEFAULT_EVENT_ONSET_METHOD;
            } else if (eventmethod.equalsIgnoreCase("AIC")) {
                this.emethod = EventOnsetType.AIC;
            } else {
                this.emethod = EventOnsetType.DE;
            }
        } catch (NumberFormatException err) {
            throw new SmException("Error extracting numeric values from configuration file");
        }
        qcchecker = new QCcheck();
        if (!qcchecker.validateQCvalues()){
            throw new SmException("Error extracting numeric values from configuration file");
        }
        String debugon = config.getConfigValue(DEBUG_TO_LOG);
        this.writeDebug = (debugon == null) ? false : debugon.equalsIgnoreCase(DEBUG_TO_LOG_ON);
        
        this.ebuffer = (this.ebuffer < 0.0) ? DEFAULT_EVENT_ONSET_BUFFER : this.ebuffer;
        this.taperlength = (this.taperlength < 0.0) ? DEFAULT_TAPER_LENGTH : this.taperlength;  
    }
    
    public V2Status processV2Data() throws SmException, IOException {  
        double[] accraw = new double[0];
        //save a copy of the original array for pre-mean removal
        double[] V1Array = inV1.getDataArray();
        //Check for units of g and adjust before proceeding.
        if (data_unit_code == CMSQSECN) {
            accraw = new double[V1Array.length];
            System.arraycopy( V1Array, 0, accraw, 0, V1Array.length);
        } else if (data_unit_code == GLN) {
            accraw = ArrayOps.convertArrayUnits(V1Array, FROM_G_CONVERSION);
        } else {
            throw new SmException("V1 file units are unsupported for processing");
        }
        double[] acc = new double[accraw.length];
        System.arraycopy( accraw, 0, acc, 0, accraw.length);
        
        System.out.println("Start of V2 processing for " + V0name.toString() + " and channel " + channel);
        //Pick P-wave and remove baseline
        errorlog.add("Start of V2 processing for " + V0name.toString() + " and channel " + channel);
        //remove linear trend before finding event onset
        errorlog.add(String.format("time per sample in sec %4.3f",dtime));
        errorlog.add(String.format("sample rate (samp/sec): %4.1f",samplerate));
        errorlog.add(String.format("length of acceleration array: %d",accraw.length));
        errorlog.add("Event detection: remove linear trend, filter, event onset detection");
        
        ArrayOps.removeLinearTrend( acc, dtime);
        
        //set up the filter coefficients and run
        ButterworthFilter filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcutoff, highcutoff, 
                                                        dtime, numpoles, true);
        if (valid) {
            filter.applyFilter(acc, 0);  //filtered values are returned in acc
        } else {
            throw new SmException("Invalid bandpass filter input parameters");
        }
        //Find the start of the event
        if (emethod == EventOnsetType.DE) {
            EventOnsetDetection depick = new EventOnsetDetection( dtime );
            pickIndex = depick.findEventOnset(acc);
            startIndex = depick.applyBuffer(ebuffer);
            errorlog.add("Event Detection algorithm: damping energy method");
        } else {
            AICEventDetect aicpick = new AICEventDetect();
            pickIndex = aicpick.calculateIndex(acc, "ToPeak");
            startIndex = aicpick.applyBuffer(ebuffer, dtime);
            errorlog.add("Event Detection algorithm: modified Akaike Information Criterion");
        }
        errorlog.add(String.format("pick index: %d, start index: %d",
                                                        pickIndex,startIndex));
        errorlog.add(String.format("pick time in seconds: %8.3f, buffered time: %8.3f",
                                          (pickIndex*dtime),(startIndex*dtime)));
        if (pickIndex <= 0) {
            //No pick index detected, so skip all V2 processing
            procStatus  = V2Status.NOEVENT;
            errorlog.add("V2process: exit status = " + procStatus);
            System.out.println("V2process: exit status = " + procStatus);
            writeOutErrorDebug();
            return procStatus;
        }
        //Remove pre-event mean from acceleration record
        if (startIndex > 0) {
            double[] subset = Arrays.copyOfRange( accraw, 0, startIndex );
            ArrayStats accsub = new ArrayStats( subset );
            ArrayOps.removeValue(accraw, accsub.getMean());
            errorlog.add("Pre-event mean removed from uncorrected acceleration");
        }
        else {
            ArrayStats accmean = new ArrayStats( accraw );
            ArrayOps.removeValue(accraw, accmean.getMean());
            errorlog.add("Full array mean removed from uncorrected acceleration");
        }

//        if (writeDebug) {
//            elog.writeOutArray(accraw, V0name.getName() + "_" + channel + "_initialBaselineCorrection.txt");
//        } 

        //Integrate the acceleration to get velocity.
        velocity = ArrayOps.Integrate( accraw, dtime);
        errorlog.add("acceleration integrated to velocity (trapezoidal method)");
//        if (writeDebug) {
//           elog.writeOutArray(velocity, V0name.getName() + "_" + channel + "_afterIntegrationToVel.txt");
//        }
        //Remove any linear or 2nd order polynomial trend from velocity
        ArrayOps.removeTrendWithBestFit( velocity, dtime);
        errorlog.add("linear/poly trend removed from velocity");
//        if (writeDebug) {
//           elog.writeOutArray(velocity, V0name.getName() + "_" + channel + "_LinearTrendRemovedVel.txt");
//        }
        //Update Butterworth filter low and high cutoff thresholds for later
        FilterCutOffThresholds threshold = new FilterCutOffThresholds();
        magtype = threshold.SelectMagAndThresholds(mmag, lmag, smag, omag, noRealVal);
        if (magtype == MagnitudeType.INVALID) {
            throw new SmException("All earthquake magnitude real header values are invalid.");
        }
        lowcutadj = threshold.getLowCutOff();
        highcutadj = threshold.getHighCutOff();
        magnitude = threshold.getMagnitude();
        errorlog.add(String.format("earthquake magnitude: %3.2f",magnitude));

        //perform first QA check on velocity, check first and last sections of
        //velocity array - should be close to 0.0 with tolerances.  If not,
        //perform adaptive baseline correction.
        ///////////////////////////////
        //
        // First QC Test
        //
        ///////////////////////////////
        qcchecker.findWindows(lowcutadj, samplerate, pickIndex);
        boolean passedQC = qcchecker.qcVelocity(velocity);
        if ( !passedQC ){
            errorlog.add("Velocity QC1 failed:");
            errorlog.add(String.format("   initial velocity: %f,  limit %f",
                                        Math.abs(qcchecker.getInitialVelocity()),
                                              qcchecker.getInitVelocityQCval()));
            errorlog.add(String.format("   final velocity: %f,  limit %f",
                                  Math.abs(qcchecker.getResidualVelocity()), 
                                            qcchecker.getResVelocityQCval()));
            errorlog.add("Adaptive baseline correction beginning");
            System.out.println("failed QC1");
        ///////////////////////////////
        //
        // Adaptive Baseline Correction
        //
        ///////////////////////////////
            AdaptiveBaselineCorrection adapt = new AdaptiveBaselineCorrection(
                        dtime,velocity,lowcutadj,highcutadj,numpoles,pickIndex);
            procStatus = adapt.startIterations();
            
            //If unable to perform any iterations in ABC, just exit with no V2
            if (procStatus == V2Status.NOABC) {
                System.out.println("V2process: exit status = " + procStatus);
                errorlog.add("V2process: exit status = " + procStatus);
                writeOutErrorDebug();
                return procStatus;
            }
            int solution = adapt.getSolution();
            double[] parms = adapt.getSolutionParms(solution);
            double[] baseline = adapt.getBaselineFunction();
            ArrayList<double[]> params = adapt.getParameters();
            double[] goodrun = params.get( solution );
            if (writeDebug) {
                elog.writeOutArray(baseline, (V0name.getName() + "_" + channel + "_baseline.txt"));
            } 
            errorlog.add("    length of ABC params: " + params.size());
            errorlog.add("    ABC: found passing solution");
            errorlog.add("    ABC: winning rank: " + solution);
            errorlog.add("    ABC: poly1 order: " + goodrun[6]);
            errorlog.add("    ABC: poly2 order: " + goodrun[7]);
            errorlog.add("    ABC: start: " + goodrun[4] + "  stop: " + goodrun[5]);
            errorlog.add(String.format("    ABC: velstart: %f,  limit %f", 
                                  goodrun[2],qcchecker.getInitVelocityQCval()));
            errorlog.add(String.format("    ABC: velend: %f,  limit %f",goodrun[3], 
                                             qcchecker.getResVelocityQCval()));
            errorlog.add(String.format("    ABC: disend: %f,  limit %f",goodrun[1], 
                                                qcchecker.getResDisplaceQCval()));
            accel = adapt.getABCacceleration();
            velocity = adapt.getABCvelocity();
            displace = adapt.getABCdisplacement();
            adapt.clearParamsArray();
        } else {
            //determine new filter coefs based on earthquake magnitude
            filter = new ButterworthFilter();
            errorlog.add("Acausal bandpass filter:");
            errorlog.add("  earthquake magnitude is " + magnitude + " and M used is " + magtype);
            errorlog.add(String.format("  adjusted lowcut: %4.2f and adjusted highcut: %4.2f Hz",
                                                            lowcutadj, highcutadj));
            valid = filter.calculateCoefficients(lowcutadj, highcutadj, 
                                                dtime, DEFAULT_NUM_POLES, true);
            if (valid) {
                filter.applyFilter(velocity, pickIndex);
            } else {
                throw new SmException("Invalid bandpass filter calculated parameters");
            }
//            if (writeDebug) {
//               elog.writeOutArray(velocity, V0name.getName() + "_" + channel + "_velocityAfterFiltering.txt");
//            }
           //Integrate the velocity to get displacement.
            displace = ArrayOps.Integrate( velocity, dtime);
            errorlog.add("Velocity integrated to displacement (trapezoidal method)");

            //Differentiate velocity for final acceleration
            accel = ArrayOps.Differentiate(velocity, dtime);
            errorlog.add("Velocity differentiated to corrected acceleration");

            ///////////////////////////////
            //
            // Second QC Test (also performed in ABC)
            //
            ///////////////////////////////
            boolean success = qcchecker.qcVelocity(velocity) && 
                                            qcchecker.qcDisplacement(displace);
            procStatus = (success) ? V2Status.GOOD : V2Status.FAILQC;
        }
        if (procStatus == V2Status.FAILQC) {
            errorlog.add("Final QC failed - V2 processing unsuccessful:");
            errorlog.add(String.format("   initial velocity: %f, limit %f",
                                        Math.abs(qcchecker.getInitialVelocity()),
                                              qcchecker.getInitVelocityQCval()));
            errorlog.add(String.format("   final velocity: %f, limit %f",
                                  Math.abs(qcchecker.getResidualVelocity()), 
                                            qcchecker.getResVelocityQCval()));
            errorlog.add(String.format("   final displacement,: %f, limit %f",
                                  Math.abs(qcchecker.getResidualDisplacement()),
                                            qcchecker.getResDisplaceQCval()));
            System.out.println("failed QC2");
        }
        System.out.println("V2process: exit status = " + procStatus);
        errorlog.add("V2process: exit status = " + procStatus);
        
        //calculate final array params for headers
        ArrayStats statVel = new ArrayStats( velocity );
        VpeakVal = statVel.getPeakVal();
        VpeakIndex = statVel.getPeakValIndex();
        VavgVal = statVel.getMean();

        ArrayStats statDis = new ArrayStats( displace );
        DpeakVal = statDis.getPeakVal();
        DpeakIndex = statDis.getPeakValIndex();
        DavgVal = statDis.getMean();

        ArrayStats statAcc = new ArrayStats( accel );
        ApeakVal = statAcc.getPeakVal();
        ApeakIndex = statAcc.getPeakValIndex();
        AavgVal = statAcc.getMean();
        
        if ((writeDebug) || (procStatus == V2Status.FAILQC)) {
            errorlog.add(String.format("Peak Velocity (abs): %f",Math.abs(VpeakVal)));
            errorlog.add(String.format("Peak Velocity * 0.01: %f",(Math.abs(VpeakVal)*0.01)));
            errorlog.add(String.format("Peak Displace (abs): %f",Math.abs(DpeakVal)));
            errorlog.add(String.format("Peak Displace * 0.01: %f",(Math.abs(DpeakVal)*0.01)));
            writeOutErrorDebug();
        }
        
        //if status is GOOD, calculate computed parameters for headers
        if (procStatus == V2Status.GOOD) {
            bracketedTimes = ArrayOps.findBracketedDuration(accel, 
                                                        TO_G_CONVERSION, dtime);
            bracketedDuration = bracketedTimes[0];
            AriasIntensity = ArrayOps.calculateAriasIntensity( accel, 
                                                    ARIAS_INTENSITY_CONST, dtime);
            System.out.println(String.format("bracketedDuration: %f",bracketedDuration));
            System.out.println(String.format("AriasIntensity: %f",AriasIntensity));
        }

//        System.out.println("V2process: exit staus = " + procStatus);
        return procStatus;
    }
    public void writeOutErrorDebug() throws IOException {
        elog.writeToLog(logstart);
        String[] errorout = new String[errorlog.size()];
        errorout = errorlog.toArray(errorout);
        elog.writeToLog(errorout);
        errorlog.clear();
    }
    
    public double getPeakVal(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.ApeakVal;
        } else if (dType == V2DataType.VEL) {
            return this.VpeakVal;
        } else {
            return this.DpeakVal;
        }
    }
    public int getPeakIndex(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.ApeakIndex;
        } else if (dType == V2DataType.VEL) {
            return this.VpeakIndex;
        } else {
            return this.DpeakIndex;
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
    public double getLowCut() {
        return this.lowcutadj;
    }
    public double getHighCut() {
        return this.highcutadj;
    }
    public V2Status getQCStatus() {
        return this.procStatus;
    }
    public int getPickIndex() {
        return this.pickIndex;
    }
    public int getStartIndex() {
        return this.startIndex;
    }
    public double getBracketedDuration() {
        return bracketedDuration;
    }
}
