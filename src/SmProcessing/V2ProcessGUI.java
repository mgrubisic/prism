/*******************************************************************************
 * Name: Java class V2ProcessGUI.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Aug. 2017
 ******************************************************************************/

package SmProcessing;

import COSMOSformat.V1Component;
import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.CMN;
import static SmConstants.VFileConstants.CMSECN;
import static SmConstants.VFileConstants.CMSECT;
import static SmConstants.VFileConstants.CMSQSECN;
import static SmConstants.VFileConstants.CMSQSECT;
import static SmConstants.VFileConstants.CMT;
import static SmConstants.VFileConstants.DEFAULT_DIFFORDER;
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.LOCAL_MAGNITUDE;
import static SmConstants.VFileConstants.MOMENT_MAGNITUDE;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import static SmConstants.VFileConstants.OTHER_MAGNITUDE;
import static SmConstants.VFileConstants.SURFACE_MAGNITUDE;
import SmException.SmException;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.DIFFERENTIATION_ORDER;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author jmjones
 */
public class V2ProcessGUI {
    private File v1File;
    private V1Component v1Rec;

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
    
    private double initialVel;
    private double initialDis;
    
    private double[] paddedaccel;
    private double dtime;
    private double samplerate;
    private double orig_samplerate;
    private boolean needresampling;
    private double noRealVal;
    private double lowcutadj;
    private double highcutadj;
    private double mmag;
    private double lmag;
    private double smag;
    private double omag;
    private double magnitude;
    
    private int numroll;  // the filter order is rolloff*2
    private int filterOrder;
    private double taperLength;
    private double smThreshold;
    private double eventOnset;
    private int startIndex;
    private ArrayList<BLCStep> blcSteps = new ArrayList<>();
    
    private double snapshotEventOnset;
    private double[] snapshotAcc;
    private double[] snapshotVel;
    private double[] snapshotDis;
    private double bracketedDuration;
    private double AriasIntensity;
    private double HousnerIntensity;
    private double RMSacceleration;
    private double durationInterval;
    private double cumulativeAbsVelocity;
    
    private int diffOrder;
    /**
     * Constructor for the class assembles the necessary parameters for processing,
     * such as EQ magnitude values and filter cutoff thresholds, then loads the
     * V1 uncorrected acceleration array into the V2 acceleration working array
     * and integrates twice to calculate velocity and displacement.
     * @param v1Rec
     * @param v1File
     * @param filterOrder
     * @param smThreshold
     * @param taperLength
     * @param eventOnset
     * @throws SmException if the earthquake magnitude values are invalid
     */
    public V2ProcessGUI(final V1Component v1Rec, final File v1File, int filterOrder, 
        double smThreshold, double taperLength, double eventOnset, int diffOrder) throws SmException {
        
        this.v1File = v1File;
        this.v1Rec = v1Rec;
        this.filterOrder = filterOrder;
        this.numroll = filterOrder/2;
        this.smThreshold = smThreshold;
        this.taperLength = taperLength;
        this.dtime = v1Rec.getRealHeaderValue(DELTA_T)*MSEC_TO_SEC;
        this.samplerate = 1.0 / this.dtime;
        this.orig_samplerate = this.samplerate;
        this.needresampling = false;
        this.diffOrder = diffOrder;
        
        //Get config values to cm/sec2 (acc), cm/sec (vel), cm (dis)
        this.acc_unit_code = CMSQSECN;
        this.vel_unit_code = CMSECN;
        this.dis_unit_code = CMN;
        this.acc_units = CMSQSECT;
        this.vel_units = CMSECT;
        this.dis_units = CMT;
        
        this.bracketedDuration = 0.0;
        this.AriasIntensity = 0.0;
        this.HousnerIntensity = 0.0;
        this.RMSacceleration = 0.0;
        this.durationInterval = 0.0;
        this.cumulativeAbsVelocity = 0.0;
        this.initialVel = 0.0;
        this.initialDis = 0.0;

        this.setEventOnset(eventOnset, this.dtime);
        this.noRealVal = this.v1Rec.getNoRealVal();
        
        //Get the earthquake magnitude from the real header array.
        this.mmag = v1Rec.getRealHeaderValue(MOMENT_MAGNITUDE);
        this.lmag = v1Rec.getRealHeaderValue(LOCAL_MAGNITUDE);
        this.smag = v1Rec.getRealHeaderValue(SURFACE_MAGNITUDE);
        this.omag = v1Rec.getRealHeaderValue(OTHER_MAGNITUDE);
        
        FilterCutOffThresholds threshold = new FilterCutOffThresholds(samplerate);
        VFileConstants.MagnitudeType magtype = threshold.SelectMagAndThresholds(mmag, lmag, smag, omag, noRealVal);
        if (magtype == VFileConstants.MagnitudeType.INVALID) {
            throw new SmException("All earthquake magnitude real header values are invalid.");
        }
        this.lowcutadj = threshold.getLowCutOff();
        this.highcutadj = threshold.getHighCutOff();
        this.magnitude = threshold.getMagnitude();
        
        // Get the acceleration from V1 and calculate velocity and displacement.
        double[] v1Array = this.v1Rec.getDataArray();
        
        // Re-sample, if necessary.
        Resampling reSampling = new Resampling();
        this.needresampling = reSampling.needsResampling((int)this.samplerate);
        if (this.needresampling) {
            this.accel = reSampling.resampleArray(v1Array, (int)this.samplerate);
            this.samplerate = reSampling.getNewSamplingRate();
            this.dtime = 1.0/this.samplerate;
        }
        else {
            this.accel = new double[v1Array.length];
            System.arraycopy(v1Array,0,this.accel,0,v1Array.length);
        }
        
        // Integrate the acceleration to get velocity.
        this.velocity = new double[this.accel.length];
        this.velocity = integrateAnArray( this.accel, this.dtime, eventOnset);
        
        //Integrate the velocity to get displacement.
        this.displace = new double[this.accel.length];
        this.displace = integrateAnArray( this.velocity, this.dtime, eventOnset);
    }
    /**
     * Cycles through a list of V2processGUI objects and filters the acceleration
     * in each object according to the low and high filter corners specified by
     * the user.
     * @param v2ProcessGUIs list of objects to filter
     * @param filterRangeLow low pass cutoff frequency
     * @param filterRangeHigh high pass cutoff frequency
     * @return list of V2ProcessGUI objects with acceleration filtered
     * @throws SmException
     */
    public static ArrayList<V2ProcessGUI> filterV2ProcessGUIList(ArrayList<V2ProcessGUI> v2ProcessGUIs,
        double filterRangeLow, double filterRangeHigh) throws Exception {
        
        try {
            // Filter acceleration data for each V2ProcessGUI object.
            for (V2ProcessGUI v2ProcessGUI : v2ProcessGUIs) {
                // Set filter corners.
                v2ProcessGUI.setLowFilterCorner(filterRangeLow);
                v2ProcessGUI.setHighFilterCorner(filterRangeHigh);
                
                // Apply filter and get new acc, vel, and dis
                v2ProcessGUI.filterProcessing();
            }
            return v2ProcessGUIs;
        }
        catch (Exception ex ) {
            throw ex;
        }
    }
    /**
     * Performs the steps needed to integrate an array when the initial array
     * value is unknown and needs to be estimated from a portion of the array.
     * @param inArr array to be integrated
     * @param dTime sample interval in seconds
     * @param eventOnset event onset time in seconds
     * @return a new array containing the integrated values with adjustment
     */
    public final double[] integrateAnArray( double[] inArr, double dTime, double eventOnset) {
        double[] outArr;
        int eStart = (int)(eventOnset / dTime);
        outArr = ArrayOps.integrate( inArr, dTime, 0.0);        
        ArrayOps.correctForZeroInitialEstimate(inArr, eStart);
        return outArr;
    }
    /**
     * Updates the event onset value for recording in the comment section of the
     * V2 file
     * @param eventOnset the updated event onset time in seconds
     * @param dTime the sample interval in seconds
     */
    public final void setEventOnset(double eventOnset, double dTime) {
        this.eventOnset = eventOnset;
        this.startIndex = (int)(eventOnset / dTime);
    }    
    /**
     * Calculates the baseline function based on the type of function selected
     * and the range of the input array over which the baseline should be calculated
     * @param inArr input array to use for baseline calculations
     * @param dTime the sampling interval for the input array, in seconds/sample
     * @param fStart the starting time for baseline estimation
     * @param fStop the ending time for baseline estimation
     * @param cType the type of baseline correction to calculate, with valid values
     * of MEAN, ORDER1, ORDER2
     * @return a new array the same length as the original array, containing the
     * baseline function calculated over the specified interval.  If the baseline
     * function is specified over an interval that doesn't cross the x-axis, it
     * is extended to the axis.  Other values outside the range are set to 0.
     * @throws SmException.SmException
     */
    public final double[] getBaselineFunction(double[] inArr, double dTime, double fStart, 
        double fStop, VFileConstants.CorrectionOrder cType) throws SmException {
        
        double[] baseline = new double[inArr.length];
        int fStartAdj = (int)(fStart / dTime);
        int fStopAdj = (int)(fStop / dTime);
        double[] subset = Arrays.copyOfRange( inArr, fStartAdj, fStopAdj );
        double[] coefs;
        double current;
        double adjacent;
        
        if (cType == VFileConstants.CorrectionOrder.MEAN) {
            //find the mean of the subarray and return a baseline function of
            //a horizontal line with the mean as the y value
            ArrayStats arrStats = new ArrayStats( subset );
            Arrays.fill(baseline, arrStats.getMean());
        } 
        else if ((cType == VFileConstants.CorrectionOrder.ORDER1) || 
            (cType == VFileConstants.CorrectionOrder.ORDER2)) {
            
            //Find the polynomial trend of the subarray, and fill it in to the
            //baseline array within the range. Extend the function beyond fstart
            //and fstop until a zero crossing is reached, the peak value is
            //exceeded, or an end of the array is reached.
            
            Arrays.fill(baseline, 0.0);
            int cOrder = (cType == VFileConstants.CorrectionOrder.ORDER1) ? 1 : 2;
            coefs =  ArrayOps.findPolynomialTrend(subset, cOrder, dTime);
            double[] time = ArrayOps.makeTimeArray(dTime, inArr.length);
            ArrayStats arrStats = new ArrayStats( inArr );
            double peakVal = arrStats.getPeakVal();
            
            for (int i = fStartAdj; i < fStopAdj; i++) {
                baseline[i] = calcPolynomialValue(coefs, time[i-fStartAdj]);
            }
            for (int i = fStopAdj; i < baseline.length; i++) {
                current = calcPolynomialValue(coefs, time[i-fStartAdj]);
                adjacent = calcPolynomialValue(coefs, time[i-fStartAdj-1]);
                if (Math.abs(current) > Math.abs(peakVal)) { //exceeded peak value
                    break;
                } else if ((current * adjacent) < 0.0) { //zero crossing
                    baseline[i] = current;
                    break;
                } else {
                    baseline[i] = current;
                }
            }
             //note negative value for time on the reverse fill
            for (int i = fStartAdj-1; i >= 0; i--) {
                current = calcPolynomialValue(coefs, (-1.0*time[fStartAdj-i]));
                adjacent = calcPolynomialValue(coefs, (-1.0*time[fStartAdj-i-1]));
                if (Math.abs(current) > Math.abs(peakVal)) { //exceeded peak value
                    break;
                } else if ((current * adjacent) < 0.0) { //zero crossing
                    baseline[i] = current;
                    break;
                } else {
                    baseline[i] = current;
                }
            }
        } 
        else {
            throw new SmException("Invalid baseline function order.");
        }
        return baseline;
    }
    /**
     * local method to calculate the y value given an x value and the function
     * coefficients
     * @param coefs array of coefficients for the function to calculate, with the
     * coefficients in increasing polynomial order
     * @param xVal the x-value to use for calculating the y-value
     * @return the calculated y-value
     */
    private double calcPolynomialValue(double[] coefs, double xVal) {
        double yVal = 0.0;
        for (int i = 0; i < coefs.length; i++) {
            yVal = yVal + Math.pow(xVal,i) * coefs[i];
        }
        return yVal;
    }
    /**
     * Modifies the input array by subtracting the baseline array over the interval
     * specified by start and stop.
     * @param inArr the input array, this array is modified
     * @param baseline the baseline array, assumed to be the same length as the input array
     * @param dTime the sample interval in seconds
     * @param aStart the starting time (seconds) for subtraction, inclusive
     * @param aStop the ending time(seconds) for subtraction, exclusive
     */
    public final void makeBaselineCorrection(VFileConstants.V2DataType v2DataType, double[] inBaseline, 
        double dTime, double aStart, double aStop){
        int aStartAdj = (int)(aStart / dTime);
        int aStopAdj = (int)(aStop / dTime);
        double[] baseline = inBaseline;
        if (v2DataType == VFileConstants.V2DataType.VEL) {
            baseline = ArrayOps.differentiate(inBaseline, dTime, diffOrder);
        }
        for (int i = aStartAdj; i < aStopAdj; i++) {
            accel[i] = accel[i] - baseline[i];
        }
    }
    
    /**
     * Filters the acceleration time series.
     * The current acceleration array is filtered and then integrated once to calculate
     * velocity and twice to calculate displacement.  
     * @throws SmException if filter parameters are invalid.
     */
    public final void filterProcessing() throws SmException {
        
        FilterAndIntegrateProcess filtint = new FilterAndIntegrateProcess(lowcutadj,highcutadj,numroll,
                                taperLength,startIndex);
        
        filtint.filterAndIntegrate(accel,dtime);
        paddedaccel = filtint.getPaddedAccel();
        velocity = filtint.getVelocity();
        displace = filtint.getDisplacement();
    }
    /**
     * Previews the V2 processing by filtering the active time series and calculating
     * the other two time series.  If input data type is ACC, then the current
     * acceleration array is filtered and then integrated once to calculate
     * velocity and twice to calculate displacement.  If the input type is VEL,
     * then the current velocity array is filtered and then integrated once to 
     * displacement and differentiated to acceleration.  
     * @param v2DataType Type of input array to filter, valid values are ACC and VEL
     * @throws SmException if an input type of DIS is entered, or if the calculated
     * filter parameters are invalid.
     */
    public final void previewProcessing( VFileConstants.V2DataType v2DataType) throws SmException {
        
        if ((v2DataType == VFileConstants.V2DataType.DIS) || (v2DataType == VFileConstants.V2DataType.VEL)) {
            throw new SmException("Unable to filter velocity or displacement in preview.");
        } 
        // Save snapshots of data.
        this.snapshotEventOnset = eventOnset;
        
        if (accel != null) {
            this.snapshotAcc = new double[accel.length];
            System.arraycopy(accel, 0, this.snapshotAcc, 0, accel.length);
        }
        if (velocity != null) {
            this.snapshotVel = new double[velocity.length];
            System.arraycopy(velocity, 0, this.snapshotVel, 0, velocity.length);
        }
        if (displace != null) {
            this.snapshotDis = new double[displace.length];
            System.arraycopy(displace, 0, this.snapshotDis, 0, displace.length);
        }
        filterProcessing();
    }

    public final void undoPreviewProcessing() {
        setEventOnset(this.snapshotEventOnset,this.dtime);
        
        if (snapshotAcc != null) {
            accel = new double[snapshotAcc.length];
            System.arraycopy(snapshotAcc, 0, accel, 0, snapshotAcc.length);
        }
        
        if (snapshotVel != null) {
            velocity = new double[snapshotVel.length];
            System.arraycopy(snapshotVel, 0, velocity, 0, snapshotVel.length);
        }
        
        if (snapshotDis != null) {
            displace = new double[snapshotDis.length];
            System.arraycopy(snapshotDis, 0, displace, 0, snapshotDis.length);
        }   
    }
    /**
     * Calculates additional parameters needed when creating the V2 product files.
     * This assumes that the preview method has been run before this call.
     * @return a status of GOOD to flag successful V2 processing
     */
    public final VFileConstants.V2Status commitProcessing() { //flag for previewProcessing complete?
        //get final stats and values for the headers
        ArrayStats statAcc = new ArrayStats( accel );
        ApeakVal = statAcc.getPeakVal();
        ApeakIndex = statAcc.getPeakValIndex();
        AavgVal = statAcc.getMean();

        ArrayStats statVel = new ArrayStats( velocity );
        VpeakVal = statVel.getPeakVal();
        VpeakIndex = statVel.getPeakValIndex();
        VavgVal = statVel.getMean();
        initialVel = velocity[0];
        
        ArrayStats statDis = new ArrayStats( displace );
        DpeakVal = statDis.getPeakVal();
        DpeakIndex = statDis.getPeakValIndex();
        DavgVal = statDis.getMean();
        initialDis = displace[0];

        //compute strong motion parameters if needed
        ComputedParams cp = new ComputedParams(accel, this.dtime, smThreshold);
        boolean strongMotion = cp.calculateComputedParameters();
        if (strongMotion) {
            bracketedDuration = cp.getBracketedDuration();
            AriasIntensity = cp.getAriasIntensity();
            HousnerIntensity = cp.getHousnerIntensity();
            RMSacceleration = cp.getRMSacceleration();
            durationInterval = cp.getDurationInterval();
            cumulativeAbsVelocity = cp.getCumulativeAbsVelocity();
        }
        return VFileConstants.V2Status.GOOD;
    }
    public class BLCStep {
        private final double fStart;
        private final double fStop;
        private final double aStart;
        private final double aStop;
        private final VFileConstants.V2DataType v2DataTypeOrig;
        private final VFileConstants.V2DataType v2DataTypeFinal;
        private final VFileConstants.BaselineType bType;
        private final VFileConstants.CorrectionOrder cTypeOrig;
        private final VFileConstants.CorrectionOrder cTypeFinal;
        private final int cStep;
       
        public BLCStep( double fStart, double fStop, double aStart, double aStop,
            VFileConstants.V2DataType v2DataTypeOrig, VFileConstants.V2DataType v2DataTypeFinal,
            VFileConstants.BaselineType bType, VFileConstants.CorrectionOrder cTypeOrig, 
            VFileConstants.CorrectionOrder cTypeFinal, int cStep) {
            this.fStart = fStart;
            this.fStop = fStop;
            this.aStart = aStart;
            this.aStop = aStop;
            this.v2DataTypeOrig = v2DataTypeOrig;
            this.v2DataTypeFinal = v2DataTypeFinal;
            this.bType = bType;
            this.cTypeOrig = cTypeOrig;
            this.cTypeFinal = cTypeFinal;
            this.cStep = cStep;
        }
        
        public double getFunctionStart() {return fStart;}
        public double getFunctionStop() {return fStop;}
        public double getAppStart() {return aStart;}
        public double getAppStop() {return aStop;}
        public VFileConstants.V2DataType getV2DataTypeOrig() {return v2DataTypeOrig;}
        public VFileConstants.V2DataType getV2DataTypeFinal() {return v2DataTypeFinal;}
        public VFileConstants.BaselineType getBaselineType() {return bType;}
        public VFileConstants.CorrectionOrder getCorrectionOrderTypeOrig() {return cTypeOrig;}
        public VFileConstants.CorrectionOrder getCorrectionOrderTypeFinal() {return cTypeFinal;}
        public int getCorrectionStep() {return cStep;}
        
        @Override
        public String toString() {
            String timeformat = "%9.4f";
            String dType = v2DataTypeFinal.toString().substring(0,1);
            String blTag = (bType.equals(VFileConstants.BaselineType.ABC)) ?
                dType+"BLABC"+cStep :
                dType+"BLC";
            
            return String.format("|<%1s>SF:%2s, EF:%3s, SA:%4s, EA:%5s, ORDER:%6s",
                blTag,
                String.format(timeformat,fStart),
                String.format(timeformat,fStop),
                String.format(timeformat,aStart),
                String.format(timeformat,aStop),
                cTypeFinal.name());
        }
    }    
    public File getV1File() {return this.v1File;}
    public V1Component getV1Component() {return this.v1Rec;}
    public double[] getAcceleration() {return this.accel;}
    public double[] getVelocity() {return this.velocity;}
    public double[] getDisplacement() {return this.displace;}
    public double getDTime() {return this.dtime;}
    public double getEventOnset() {return this.eventOnset;}
    public double getLowFilterCorner() {return this.lowcutadj;}
    public double getHighFilterCorner() {return this.highcutadj;}
    public double getNoRealValue() {return this.noRealVal;}
    public int getDiffOrder() {return this.diffOrder;}
    public void setLowFilterCorner(double low) {this.lowcutadj = low;}
    public void setHighFilterCorner(double high) {this.highcutadj = high;}    
}
