/*******************************************************************************
 * Name: Java class V2Process.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmProcessing;

import COSMOSformat.V1Component;
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.EventOnsetType;
import SmConstants.VFileConstants.MagnitudeType;
import SmConstants.VFileConstants.V2DataType;
import SmException.SmException;
import SmUtilities.ConfigReader;
import SmUtilities.ProcessStepsRecorder;
import static SmUtilities.SmConfigConstants.*;
import SmUtilities.SmDebugLogger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class handles the V2 processing, performing the necessary steps to process
 * the uncorrected acceleration into corrected acceleration, velocity and displacement.
 * It performs 2 QC checks to test the accuracy of the processing.
 * @author jmjones
 */
public class V2Process {
    //need 3 sets of these params, for each data type
    protected double[] accel;
    protected double ApeakVal;
    protected int ApeakIndex;
    protected double AavgVal;
    protected final int acc_unit_code;
    protected final String acc_units;
    
    protected double[] velocity;
    protected double VpeakVal;
    protected int VpeakIndex;
    protected double VavgVal;
    protected final int vel_unit_code;
    protected final String vel_units;
    
    protected double[] displace;
    protected double DpeakVal;
    protected int DpeakIndex;
    protected double DavgVal;
    protected final int dis_unit_code;
    protected final String dis_units;
    
    protected double initialVel;
    protected double initialDis;
    
    private int inArrayLength;
    protected double[] paddedaccel;
    protected final V1Component inV1;
    protected int data_unit_code;
    protected double dtime;
    protected double samplerate;
    protected double noRealVal;
    protected double lowcutoff;
    protected double highcutoff;
    protected double lowcutadj;
    protected double highcutadj;
    protected double mmag;
    protected double lmag;
    protected double smag;
    protected double omag;
    protected double magnitude;
    protected MagnitudeType magtype;
    protected BaselineType basetype;
    
    private int pickIndex;
    protected int startIndex;
    private double ebuffer;
    private EventOnsetType emethod;
    protected int numpoles;  // the filter order is rolloff*2
    protected double taperlength;
    private double preEventMean;
    private int trendRemovalOrder;
    private int calculated_taper;
    private boolean strongMotion;
    protected double smThreshold;
    private boolean needsABC;
    private String logtime;
    
    private V2Status procStatus;
    private QCcheck qcchecker;
    
    private ArrayList<String> errorlog;
    private boolean writeDebug;
    private boolean writeBaseline;
    private SmDebugLogger elog;
    private String[] logstart;
    private File V0name;
    private String channel;
    private String eventID;
    private double QCvelinitial;
    private double QCvelresidual;
    private double QCdisresidual;
    private int ABCnumparams;
    private int ABCwinrank;
    private int ABCpoly1;
    private int ABCpoly2;
    private int ABCbreak1;
    private int ABCbreak2;
    
    protected double bracketedDuration;
    protected double AriasIntensity;
    protected double HousnerIntensity;
    protected double channelRMS;
    protected double durationInterval;
    protected double cumulativeAbsVelocity;
    
    ProcessStepsRecorder stepRec;
    /**
     * Constructor gets the necessary header and configuration file parameters
     * and validates them.
     * @param v1rec the V1 component object holding the uncorrected acceleration
     * @param inName the name of the V0 input file
     * @param logtime the processing time
     * @throws SmException if unable to access valid header or configuration file
     * parameters
     */
    public V2Process(final V1Component v1rec, File inName, String logtime) 
                                                            throws SmException {
        double epsilon = 0.000001;
        this.inV1 = v1rec;
        this.lowcutadj = 0.0;
        this.highcutadj = 0.0;
        stepRec = ProcessStepsRecorder.INSTANCE;
        this.V0name = inName;
        this.channel = inV1.getChannel();
        this.eventID = inV1.getEventID();
        this.logtime = logtime;
        this.basetype = BaselineType.BESTFIT;
        
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
        this.VpeakVal = 0.0;
        this.ApeakVal = 0.0;
        this.DpeakVal = 0.0;
        this.inArrayLength = 0;
        
        this.bracketedDuration = 0.0;
        this.AriasIntensity = 0.0;
        this.HousnerIntensity = 0.0;
        this.channelRMS = 0.0;
        this.durationInterval = 0.0;
        this.cumulativeAbsVelocity = 0.0;
        this.initialVel = 0.0;
        this.initialDis = 0.0;
        this.preEventMean = 0.0;
        this.trendRemovalOrder = 0;
        this.calculated_taper = 0;
        this.strongMotion = false;
        this.smThreshold = 0.0;
        
        stepRec.clearSteps();
        
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
    }
    private void finishAutoConstructor() throws SmException {
        this.errorlog = new ArrayList<>();
        this.elog = SmDebugLogger.INSTANCE;
        ConfigReader config = ConfigReader.INSTANCE;
        this.writeDebug = false;
        this.writeBaseline = false;
        this.needsABC = false;
        this.QCvelinitial = 0.0;
        this.QCvelresidual = 0.0;
        this.QCdisresidual = 0.0;
        this.ABCnumparams = 0;
        this.ABCwinrank = 0;
        this.ABCpoly1 = 0;
        this.ABCpoly2 = 0;
        this.ABCbreak1 = 0;
        this.ABCbreak2 = 0;

        logstart = new String[2];
        logstart[0] = "\n";
        logstart[1] = "Prism Error/Debug Log Entry: " + logtime;
        try {
            String unitcode = config.getConfigValue(DATA_UNITS_CODE);
            this.data_unit_code = (unitcode == null) ? CMSQSECN : Integer.parseInt(unitcode);

            String lowcut = config.getConfigValue(BP_FILTER_CUTOFFLOW);
            this.lowcutoff = (lowcut == null) ? DEFAULT_LOWCUT : Double.parseDouble(lowcut);

            String highcut = config.getConfigValue(BP_FILTER_CUTOFFHIGH);
            this.highcutoff = (highcut == null) ? DEFAULT_HIGHCUT : Double.parseDouble(highcut);

            //The Butterworth filter implementation requires an even number for rolloff
            String filorder = config.getConfigValue(BP_FILTER_ORDER);
            this.numpoles = (filorder == null) ? DEFAULT_NUM_POLES : Integer.parseInt(filorder)/2;

            //The Butterworth filter taper length for the half cosine taper
            //this taperlength is the value in seconds from the configuration file
            String taplen = config.getConfigValue(BP_TAPER_LENGTH);
            this.taperlength = (taplen == null) ? DEFAULT_TAPER_LENGTH : Double.parseDouble(taplen);
            this.taperlength = (this.taperlength < 0.0) ? DEFAULT_TAPER_LENGTH : this.taperlength;  
            
            String pbuf = config.getConfigValue(EVENT_ONSET_BUFFER);
            this.ebuffer = (pbuf == null) ? DEFAULT_EVENT_ONSET_BUFFER : Double.parseDouble(pbuf);
            this.ebuffer = (this.ebuffer < 0.0) ? DEFAULT_EVENT_ONSET_BUFFER : this.ebuffer;
            
            String thold = config.getConfigValue(SM_THRESHOLD);
            this.smThreshold = (thold == null) ? DEFAULT_SM_THRESHOLD : Double.parseDouble(thold);
            this.smThreshold = ((this.smThreshold < 0.0) || (this.smThreshold > 100.0)) ? 
                                                DEFAULT_SM_THRESHOLD : this.smThreshold;
            
            String eventmethod = config.getConfigValue(EVENT_ONSET_METHOD);
            if (eventmethod == null) {
                this.emethod = DEFAULT_EVENT_ONSET_METHOD;
            } else if (eventmethod.equalsIgnoreCase("AIC")) {
                this.emethod = EventOnsetType.AIC;
            } else {
                this.emethod = EventOnsetType.PWD;
            }
        } catch (NumberFormatException err) {
            throw new SmException("Error extracting numeric values from configuration file");
        }
        qcchecker = new QCcheck();
        if (!qcchecker.validateQCvalues()){
            throw new SmException("Error extracting numeric values from configuration file");
        }
        String debugon = config.getConfigValue(DEBUG_TO_LOG);
        this.writeDebug = (debugon == null) ? false : 
                                        debugon.equalsIgnoreCase(DEBUG_TO_LOG_ON);
        
        String baselineon = config.getConfigValue(WRITE_BASELINE_FUNCTION);
        this.writeBaseline = (baselineon == null) ? false : 
                                    baselineon.equalsIgnoreCase(BASELINE_WRITE_ON);
    }
    /**
     * Controls the flow of automatic V2 processing, starting with event detection,
     * pre-event mean removal, integration to velocity, 
     * best fit trend removal, and the first QC.  If this
     * QC fails, adaptive baseline correction is attempted. If the first QC passed,
     * the baseline-corrected velocity is filtered, integrated to displacement, and
     * differentiated to corrected acceleration.  A final QC check is performed
     * in both best fit and adaptive baseline correction and the status of the
     * processing is returned.
     * @return the status of V2 processing, such as GOOD, FAILQC, etc.
     * @throws SmException if unable to perform processing
     * @throws IOException if unable to write out to log files
     */
    public V2Status processV2Data() throws SmException, IOException {
        finishAutoConstructor();
//        System.out.println("Start of V2 processing for " + V0name.toString() + " and channel " + channel);
        stepRec.addCorrectionType(CorrectionType.AUTO);
        // Correct units to CMSQSECN, if needed, and make copy of acc array
        double[] accraw = new double[0];
        double[] V1Array = inV1.getDataArray();
        inArrayLength = V1Array.length;

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
        
        writePrePwDdebug(accraw.length);
        //////////////////////////////
        //
        // Find event onset
        //
        //////////////////////////////
        findEventOnset(acc);
        if (pickIndex <= 0) { //No pick index detected, so skip all V2 processing
            procStatus  = V2Status.NOEVENT;
            errorlog.add("V2process: exit status = " + procStatus);
            writeOutErrorDebug();
            makeDebugCSV();
            return procStatus;
        }
        stepRec.addEventOnset(startIndex * dtime);
        // Update Butterworth filter low and high cutoff thresholds for later
        updateThresholds();
        
        ///////////////////////////////
        //
        // Remove pre-event mean from acceleration
        //
        ///////////////////////////////
        //Remove pre-event mean from acceleration record
        if (startIndex > 0) {
            double[] subset = Arrays.copyOfRange( accraw, 0, startIndex );
            ArrayStats accsub = new ArrayStats( subset );
            preEventMean = accsub.getMean();
            ArrayOps.removeValue(accraw, preEventMean);
            stepRec.addPreEventMean(preEventMean);
            errorlog.add(String.format("Pre-event mean of %10.6e removed from uncorrected acceleration",preEventMean));
        }
//        if (writeDebug) {
//            elog.writeOutArray(accraw, V0name.getName() + "_" + channel + "_initialBaselineCorrection.txt");
//        } 

        ///////////////////////////////
        //
        // Integrate Acceleration to Velocity and correct for missing initial value
        //
        ///////////////////////////////
        //Integrate the acceleration to get velocity, using 0 as first value estimate
        velocity = ArrayOps.Integrate( accraw, dtime, 0.0);
        //Now correct for unknown initial value by removing preevent mean (minus first val.)
        int intzero = ArrayOps.findZeroCrossing(velocity, startIndex, 0);
        if (intzero > 1) {
            double[] velset = Arrays.copyOfRange( velocity, 1, intzero );
            ArrayStats velsub = new ArrayStats( velset );
            ArrayOps.removeValue(velocity, velsub.getMean());
        }
        
        errorlog.add("acceleration integrated to velocity");
        if (writeBaseline) {
           elog.writeOutArray(velocity, V0name.getName() + "_" + channel + "_afterIntegrationToVel.txt");
        }
        ///////////////////////////////
        //
        // Remove Trend with Best Fit on a copy of velocity
        //
        ///////////////////////////////
        //Remove any linear or 2nd order polynomial trend from velocity
        double[] veltest = new double[velocity.length];
        System.arraycopy( velocity, 0, veltest, 0, velocity.length);
        trendRemovalOrder = ArrayOps.removeTrendWithBestFit( veltest, dtime);
        if (writeBaseline) { 
            elog.writeOutArray(veltest, V0name.getName() + "_" + channel + "_BestFitTrendRemovedVel.txt");                
        }
        //perform first QA check on velocity copy, check first and last sections of
        //velocity array - should be close to 0.0 with tolerances.  If not,
        //perform adaptive baseline correction.
        ///////////////////////////////
        //
        // First QC Test
        //
        ///////////////////////////////
        qcchecker.findWindow(lowcutadj, samplerate, startIndex);
        boolean passedQC = qcchecker.qcVelocity(veltest);
        if ( !passedQC ){
            trendRemovalOrder = -1;
            errorlog.add("Velocity QC1 failed:");
            errorlog.add(String.format("   initial velocity: %f,  limit %f",
                                        Math.abs(qcchecker.getInitialVelocity()),
                                              qcchecker.getInitVelocityQCval()));
            errorlog.add(String.format("   final velocity: %f,  limit %f",
                                  Math.abs(qcchecker.getResidualVelocity()), 
                                            qcchecker.getResVelocityQCval()));
            errorlog.add("Adaptive baseline correction beginning");
            ///////////////////////////////
            //
            // Baseline Correction
            //
            ///////////////////////////////
            needsABC = true;            
//            System.out.println("ABC needed");
            double[] goodrun = adaptiveCorrection();
            //If unable to perform any iterations in ABC, just exit with no V2
            if (procStatus == V2Status.NOABC) {
                errorlog.add("V2process: exit status = " + procStatus);
                writeOutErrorDebug();
                makeDebugCSV();
                return procStatus;
            }
            updateABCstats(goodrun);
        } else {
            ///////////////////////////////
            //
            // Passed first QC, so filter velocity and integrate, differentiate
            //
            ///////////////////////////////
            errorlog.add(String.format("Best fit trend of order %d removed from velocity", trendRemovalOrder));
            if (trendRemovalOrder == 1) {
                stepRec.addBaselineStep(0.0, (velocity.length*dtime), CorrectionOrder.ORDER1);
            } else {
                stepRec.addBaselineStep(0.0, (velocity.length*dtime), CorrectionOrder.ORDER2);
            }
            velocity = veltest;
            filterIntegrateDiff();
            ///////////////////////////////
            //
            // Second QC Test (also performed in ABC)
            //
            ///////////////////////////////
            boolean success = qcchecker.qcVelocity(velocity) && 
                                            qcchecker.qcDisplacement(displace);
            procStatus = (success) ? V2Status.GOOD : V2Status.FAILQC;
            QCvelinitial = Math.abs(qcchecker.getInitialVelocity());
            QCvelresidual = Math.abs(qcchecker.getResidualVelocity());
            QCdisresidual = Math.abs(qcchecker.getResidualDisplacement());
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
        }
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

        errorlog.add(String.format("Peak Velocity: %f",VpeakVal));
        ////////////////////////////
        //
        // strong motion computed parameters done if status is good
        //
        ////////////////////////////
        if (procStatus == V2Status.GOOD) {
            ComputedParams cp = new ComputedParams(accel, dtime, smThreshold);
            strongMotion = cp.calculateComputedParameters();
            if (strongMotion) {
                bracketedDuration = cp.getBracketedDuration();
                AriasIntensity = cp.getAriasIntensity();
                HousnerIntensity = cp.getHousnerIntensity();
                channelRMS = cp.getChannelRMS();
                durationInterval = cp.getDurationInterval();
                cumulativeAbsVelocity = cp.getCumulativeAbsVelocity();
                errorlog.add("Strong motion record");
            }
        }
        if ((writeDebug) || (procStatus != V2Status.GOOD)) {
            writeOutErrorDebug();
            makeDebugCSV();
        }
        return procStatus;
    }
    /**
     * Writes out the error log to file
     * @throws IOException if unable to write to file
     */
    private void writeOutErrorDebug() throws IOException {
        elog.writeToLog(logstart, LogType.DEBUG);
        String[] errorout = new String[errorlog.size()];
        errorout = errorlog.toArray(errorout);
        elog.writeToLog(errorout, LogType.DEBUG);
        errorlog.clear();
    }
    /**
     * Finds the event onset using the specified method and applies any optional buffer.
     * First any linear trend is removed, then the array is filtered, and then
     * the event onset technique is applied.
     * @param acc the input acceleration
     * @throws SmException if unable to calculate filter coefficients
     */
    private void findEventOnset(double[] acc) throws SmException {
        ArrayOps.removeLinearTrend( acc, dtime);
        
        //set up the filter coefficients and run
        ButterworthFilter filter = new ButterworthFilter();
        boolean valid = filter.calculateCoefficients(lowcutoff, highcutoff, 
                                                        dtime, numpoles, true);
        if (valid) {
            int calcSec = (int)(taperlength * dtime);
            filter.applyFilter(acc, taperlength, calcSec);  //filtered values are returned in acc
        } else {
            throw new SmException("Invalid bandpass filter input parameters");
        }
        errorlog.add(String.format("filtering before event onset detection, taperlength: %d", 
                                                        filter.getTaperlength()));
        // Find event onset
        if (emethod == EventOnsetType.PWD) {
            EventOnsetDetection depick = new EventOnsetDetection( dtime );
            pickIndex = depick.findEventOnset(acc);
            startIndex = depick.applyBuffer(ebuffer);
            errorlog.add("Event Detection algorithm: PwD method");
        } else {
            AICEventDetect aicpick = new AICEventDetect();
            pickIndex = aicpick.calculateIndex(acc, "ToPeak");
            startIndex = aicpick.applyBuffer(ebuffer, dtime);
            errorlog.add("Event Detection algorithm: modified AIC");
        }
        errorlog.add(String.format("pick index: %d, start index: %d",
                                                        pickIndex,startIndex));
        errorlog.add(String.format("pick time in seconds: %8.3f, buffered time: %8.3f",
                                          (pickIndex*dtime),(startIndex*dtime)));
    }
    /**
     * Updates the filter threshold values based on the EQ magnitude
     * @throws SmException if unable to extract value magnitude values from the header
     */
    private void updateThresholds() throws SmException {
        FilterCutOffThresholds threshold = new FilterCutOffThresholds();
        magtype = threshold.SelectMagAndThresholds(mmag, lmag, smag, omag, noRealVal);
        if (magtype == MagnitudeType.INVALID) {
            throw new SmException("All earthquake magnitude real header values are invalid.");
        }
        lowcutadj = threshold.getLowCutOff();
        highcutadj = threshold.getHighCutOff();
        magnitude = threshold.getMagnitude();
        errorlog.add(String.format("earthquake magnitude: %3.2f",magnitude));
    }
    /**
     * Steps for filtering velocity, integrating to displacement, and differentiating
     * to corrected acceleration in the case where ABC is not needed
     * @throws SmException if unable to calculate the filter coefficients
     */
    private void filterIntegrateDiff() throws SmException {
        double[] paddedvelocity;
        ButterworthFilter filter = new ButterworthFilter();
        errorlog.add("Acausal bandpass filter:");
        errorlog.add(String.format("  earthquake magnitude is %4.2f and M used is %s",
                                                    magnitude,magtype));
        errorlog.add(String.format("  adjusted lowcut: %4.2f and adjusted highcut: %4.2f Hz",
                                                lowcutadj, highcutadj));
        boolean valid = filter.calculateCoefficients(lowcutadj, highcutadj, 
                                            dtime, DEFAULT_NUM_POLES, true);
        if (valid) {
            paddedvelocity = filter.applyFilter(velocity, taperlength, startIndex);
        } else {
            throw new SmException("Invalid bandpass filter calculated parameters");
        }
        calculated_taper = filter.getTaperlength();
        errorlog.add(String.format("filtering after 1st QC, taperlength: %d", 
                                                        filter.getTaperlength()));
//            if (writeDebug) {
//               elog.writeOutArray(velocity, V0name.getName() + "_" + channel + "_velocityAfterFiltering.txt");
//            }
//            if (writeDebug) {
//               elog.writeOutArray(paddedvelocity, V0name.getName() + "_" + channel + "_paddedVelocityAfterFiltering.txt");
//            }
        //The velocity array was updated with the filtered values in the 
        //applyFilter call
        initialVel = velocity[0];
        double[] paddeddisplace;
        displace = new double[velocity.length];
        
        // Integrate padded velocity and extract displacement
        paddeddisplace = ArrayOps.Integrate( paddedvelocity, dtime, 0.0);
        System.arraycopy(paddeddisplace, filter.getPadLength(), displace, 0, displace.length);
        initialDis = displace[0];
        if (writeDebug) {
            errorlog.add("Velocity integrated to displacement");
        }
        
        // Differentiate velocity to corrected acceleration
        accel = new double[velocity.length];
        paddedaccel = ArrayOps.Differentiate(paddedvelocity, dtime);
        System.arraycopy(paddedaccel, filter.getPadLength(), accel, 0, accel.length);
        errorlog.add("Velocity differentiated to corrected acceleration");
//        if (writeDebug) {
//           elog.writeOutArray(paddedvelocity, V0name.getName() + "_" + channel + "_paddedVelocityAfterFiltering.txt");
//        }
//        if (writeDebug) {
//           elog.writeOutArray(paddeddisplace, V0name.getName() + "_" + channel + "_paddedDisplaceAfterIntegrate.txt");
//        }
//        if (writeDebug) {
//           elog.writeOutArray(paddedaccel, V0name.getName() + "_" + channel + "_paddedAccelAfterFiltering.txt");
//        }
    }
    /**
     * Calls adaptive baseline correction and extracts the results
     * @return the parameter array for the returned solution
     * @throws SmException if unable to get filter coefficients during ABC
     */
    private double[] adaptiveCorrection() throws SmException {
        ABC2 adapt = new ABC2(
            dtime,velocity,lowcutadj,highcutadj,numpoles,startIndex,taperlength);
        procStatus = adapt.findFit();
//        System.out.println("procstatus: " + procStatus.name());
        basetype = BaselineType.ABC;
        int solution = adapt.getSolution();
        double[] baseline = adapt.getBaselineFunction();
        double[] goodrun = adapt.getSolutionParms(solution);
        calculated_taper = adapt.getCalculatedTaperLength();
        ABCnumparams = adapt.getNumRuns();
        ABCwinrank = (procStatus == V2Status.GOOD) ? (solution + 1) : 0;
        adapt.clearParamsArray();
        accel = adapt.getABCacceleration();
        velocity = adapt.getABCvelocity();
        displace = adapt.getABCdisplacement();
        paddedaccel = adapt.getABCpaddedacceleration();
        initialVel = velocity[0];
        initialDis = displace[0];
        if (writeBaseline) { 
            elog.writeOutArray(baseline, (V0name.getName() + "_" + channel + "_baseline.txt"));
        } 
        return goodrun;
    }
    /**
     * Extracts the statistics from the ABC run and places into the class variables
     * @param goodrun the array containing the ABC parameters
     */
    private void updateABCstats(double[] goodrun) {
        QCvelinitial = goodrun[2];
        QCvelresidual = goodrun[3];
        QCdisresidual = goodrun[1];
        ABCpoly1 = (int)goodrun[6];
        ABCpoly2 = (int)goodrun[7];
        ABCbreak1 = (int)goodrun[4];
        ABCbreak2 = (int)goodrun[5];
        if (ABCpoly1 == 1) {
            stepRec.addBaselineStep(0.0, (ABCbreak1*dtime), CorrectionOrder.ORDER1);
        } else {
            stepRec.addBaselineStep(0.0, (ABCbreak1*dtime), CorrectionOrder.ORDER2);
        }
        stepRec.addBaselineStep((ABCbreak1*dtime), (ABCbreak2*dtime), CorrectionOrder.SPLINE);
        if (ABCpoly2 == 1) {
            stepRec.addBaselineStep((ABCbreak2*dtime), (velocity.length*dtime), CorrectionOrder.ORDER1);
        } else if (ABCpoly2 == 2) {
            stepRec.addBaselineStep((ABCbreak2*dtime), (velocity.length*dtime), CorrectionOrder.ORDER2);
        } else {
            stepRec.addBaselineStep((ABCbreak2*dtime), (velocity.length*dtime), CorrectionOrder.ORDER3);
        }
        errorlog.add("    length of ABC params: " + ABCnumparams);
        errorlog.add("    ABC: final status: " + procStatus.name());
        errorlog.add("    ABC: rank: " + ABCwinrank);
        errorlog.add("    ABC: poly1 order: " + ABCpoly1);
        errorlog.add("    ABC: poly2 order: " + ABCpoly2);
        errorlog.add("    ABC: start: " + ABCbreak1 + "  stop: " + ABCbreak2);
        errorlog.add(String.format("    ABC: velstart: %f,  limit %f", 
                        QCvelinitial,qcchecker.getInitVelocityQCval()));
        errorlog.add(String.format("    ABC: velend: %f,  limit %f",QCvelresidual, 
                                    qcchecker.getResVelocityQCval()));
        errorlog.add(String.format("    ABC: disend: %f,  limit %f",QCdisresidual, 
                                        qcchecker.getResDisplaceQCval()));
        errorlog.add(String.format("    ABC: calc. taperlength: %d", 
                                                    calculated_taper));
    }
    /**
     * Makes the CSV file contents for debug and writes to file
     * @throws IOException if unable to write to file
     */
    private void makeDebugCSV() throws IOException {
        String[] headerline = {"EVENT","MAG","NAME","CHANNEL",
            "ARRAY LENGTH","SAMP INTERVAL(SEC)","PICK INDEX","PICK TIME(SEC)",
            "PEAK VEL(CM/SEC)","TAPERLENGTH","PRE-EVENT MEAN","PEAK ACC(G)",
            "STRONG MOTION","EXIT STATUS","VEL INITIAL(CM/SEC)",
            "VEL RESIDUAL(CM/SEC)","DIS RESIDUAL(CM)","BASELINE CORRECTION",
            "POLY1","ABC POLY2","ABC 1ST BREAK","ABC 2ND BREAK",
            "ABC PARM LENGTH","ABC WIN RANK"};
        ArrayList<String> data = new ArrayList<>();
        data.add(eventID);                                  //event id
        data.add(String.format("%4.2f",magnitude));         //event magnitude
        data.add(V0name.getName());                        //record file name
        data.add(channel);                                  //channel number
        data.add(String.format("%d",inArrayLength));      //length of array
        data.add(String.format("%5.3f",dtime));             //sample interval
        data.add(String.format("%d",startIndex));            //event onset index
        data.add(String.format("%8.3f", startIndex*dtime));  //event onset time
        data.add(String.format("%8.5f",VpeakVal));          //peak velocity
        data.add(String.format("%d",calculated_taper));     //filter taperlength
        data.add(String.format("%8.6f",preEventMean));      //preevent mean removed
        data.add(String.format("%5.4f",ApeakVal*TO_G_CONVERSION)); //peak acc in g
        if (strongMotion) {
            data.add("YES");
        } else if (procStatus != V2Status.GOOD) {
            data.add("--");
        } else {
            data.add("NO");
        }
        data.add(procStatus.name());                       //exit status
        if ((procStatus == V2Status.GOOD) || (procStatus == V2Status.FAILQC)) {
            data.add(String.format("%8.6f",QCvelinitial));     //QC value initial velocity
            data.add(String.format("%8.6f",QCvelresidual));    //QC value residual velocity
            data.add(String.format("%8.6f",QCdisresidual));    //QC value residual displace
            data.add(basetype.name());
            if (basetype == BaselineType.BESTFIT) {
                data.add((trendRemovalOrder > 0) ? String.format("%d",trendRemovalOrder) : "");
            } else {
                data.add((ABCpoly1 > 0) ? String.format("%d",ABCpoly1) : "");
            }
            data.add((ABCpoly2 > 0) ? String.format("%d",ABCpoly2) : "");
            data.add((ABCbreak1 > 0) ? String.format("%d",ABCbreak1) : "");
            data.add((ABCbreak2 > 0) ? String.format("%d",ABCbreak2) : "");
            data.add((ABCnumparams > 0) ? String.format("%d",ABCnumparams) : "");
            data.add((ABCwinrank > 0) ? String.format("%d",ABCwinrank) : "");
        }
        elog.writeToCSV(data, headerline, "ParameterLog.csv");
        data.clear();
    }
    /**
     * Writes general debug information out to the error/debug log at the 
     * start of processing
     * @param arlen the length of the acceleration array
     */
    private void writePrePwDdebug(int arlen){
        errorlog.add("Start of V2 processing for " + V0name.toString() + " and channel " + channel);
        errorlog.add(String.format("EventID: %s",eventID));
        errorlog.add(String.format("time per sample in sec %4.3f",dtime));
        errorlog.add(String.format("sample rate (samp/sec): %4.1f",samplerate));
        errorlog.add(String.format("length of acceleration array: %d",arlen));
        errorlog.add("Event detection: remove linear trend, filter, event onset detection");
    }
    /**
     * Getter for the peak value for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the peak value
     */
    public double getPeakVal(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.ApeakVal;
        } else if (dType == V2DataType.VEL) {
            return this.VpeakVal;
        } else {
            return this.DpeakVal;
        }
    }
    /**
     * Getter for the peak index for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the peak index
     */
    public int getPeakIndex(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.ApeakIndex;
        } else if (dType == V2DataType.VEL) {
            return this.VpeakIndex;
        } else {
            return this.DpeakIndex;
        }
    }
    /**
     * Getter for the average value for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the average value
     */
    public double getAvgVal(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.AavgVal;
        } else if (dType == V2DataType.VEL) {
            return this.VavgVal;
        } else {
            return this.DavgVal;
        }
    }
    /**
     * Getter for the array for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the array reference
     */
    public double[] getV2Array(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.accel;
        } else if (dType == V2DataType.VEL) {
            return this.velocity;
        } else {
            return this.displace;
        }
    }
    /**
     * Getter for the array length for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the array length
     */
    public int getV2ArrayLength(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.accel.length;
        } else if (dType == V2DataType.VEL) {
            return this.velocity.length;
        } else {
            return this.displace.length;
        }
    }
    /**
     * Getter for the data unit code for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the data unit code
     */
    public int getDataUnitCode(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.acc_unit_code;
        } else if (dType == V2DataType.VEL) {
            return this.vel_unit_code;
        } else {
            return this.dis_unit_code;
        }
    }
    /**
     * Getter for the data units for the particular data type
     * @param dType the data type (ACC, VEL, DIS)
     * @return the data units text name
     */
    public String getDataUnits(V2DataType dType) {
        if (dType == V2DataType.ACC) {
            return this.acc_units;
        } else if (dType == V2DataType.VEL) {
            return this.vel_units;
        } else {
            return this.dis_units;
        }
    }
    /**
     * Getter for the adjusted filter low cutoff value
     * @return the adjusted low filter cutoff frequency
     */
    public double getLowCut() {
        return this.lowcutadj;
    }
    /**
     * Getter for the adjusted filter high cutoff value
     * @return the adjusted high filter cutoff frequency
     */
    public double getHighCut() {
        return this.highcutadj;
    }
    /**
     * Getter for the final QC status of the V2 processing
     * @return the QC exit status
     */
    public V2Status getQCStatus() {
        return this.procStatus;
    }
    /**
     * Getter for the event onset index
     * @return the event onset index
     */
    public int getPickIndex() {
        return this.pickIndex;
    }
    /**
     * Getter for the start index, which is the event index modified by the buffer value
     * @return the start index
     */
    public int getStartIndex() {
        return this.startIndex;
    }
    /**
     * Setter for the start index
     * @param instartindex the event start index
     */
    public void setStartIndex(int instartindex) {
        this.startIndex = instartindex;
    }
    /**
     * Getter for the Bracketed Duration for the real header
     * @return the bracketed duration
     */
    public double getBracketedDuration() {
        return bracketedDuration;
    }
    /**
     * Getter for the Arias Intensity value for the real header
     * @return the Arias Intensity
     */
    public double getAriasIntensity() {
        return AriasIntensity;
    }
    /**
     * Getter for the Housner Intensity value for the real header
     * @return the Housner Intensity
     */
    public double getHousnerIntensity() {
        return HousnerIntensity;
    }
    /**
     * Getter for the channel RMS for the real header
     * @return the channel RMS
     */
    public double getChannelRMS() {
        return channelRMS;
    }
    /**
     * Getter for the duration interval for the real header
     * @return the duration interval
     */
    public double getDurationInterval() {
        return durationInterval;
    }
    /**
     * Getter for the Cumulative absolute velocity for the real header
     * @return the cumulative absolute velociy
     */
    public double getCumulativeAbsVelocity() {
        return cumulativeAbsVelocity;
    }
    /**
     * Getter for the initial velocity value for the real header
     * @return the initial velocity value
     */
    public double getInitialVelocity() {
        return initialVel;
    }
    /**
     * Getter for the initial displacement value for the real header
     * @return the initial displacement value
     */
    public double getInitialDisplace() {
        return initialDis;
    }
    /**
     * Getter for the padded acceleration array for V3 processing
     * @return reference to the padded acceleration array
     */
    public double[] getPaddedAccel() {
        return paddedaccel;
    }
}
