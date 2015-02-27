/*******************************************************************************
 * Name: Java class ProcessStepsRecorder.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmUtilities;

import SmConstants.VFileConstants.CorrectionOrder;
import SmConstants.VFileConstants.CorrectionType;
import java.util.ArrayList;

/**
 * This class records the V2 processing steps so they can be included in the
 * comments section of the product files. In addition to event onset time and pre-
 * event mean, this also records the baseline correction functions applied to
 * the trace, identifying them by start time, stop time, and function.  This class
 * is a singleton with a private constructor and is instantiated with
 * ProcessStepsRecorder stepRec = ProcessStepsRecorder.INSTANCE.
 * @author jmjones
 */
public class ProcessStepsRecorder {
    private double eventOnsetTime;
    private double preEventMean;
    private CorrectionType ctype;
    private ArrayList<blcorrect> blist;
    public final static ProcessStepsRecorder INSTANCE = new ProcessStepsRecorder();
    /**
     * Private constructor for the singleton class.
     */
    private ProcessStepsRecorder(){
        preEventMean = 0.0;
        eventOnsetTime = 0.0;
        ctype = CorrectionType.AUTO;
        blist = new ArrayList<>();
    }
    /**
     * Adds the event onset time to the recorder.
     * @param inonsettime event onset time in seconds
     */
    public void addEventOnset( double inonsettime ) {
        eventOnsetTime = inonsettime;
    }
    /**
     * Adds the pre-event mean that is subtracted from the entire uncorrected
     * acceleration array.  The mean value units are cm/sq.sec
     * @param inmean pre-event mean
     */
    public void addPreEventMean( double inmean ) {
        preEventMean = inmean;
    }
    /**
     * Adds a flag for the method of correction, such as manual or automatic
     * @param intype the correction type
     */
    public void addCorrectionType( CorrectionType intype ) {
        ctype = intype;
    }
    /**
     * Adds baseline correction information to the recorder.  Baseline corrections
     * are recorded by identifying the interval over which the baseline correction
     * was applied.  This is done by recording the start and stop times.  The
     * function itself is identified by its polynomial order of 1,2, or 3, or by
     * SPLINE if the spline algorithm was used in adaptive baseline correction.
     * @param instart start time for the baseline correction interval
     * @param instop stop time for the baseline correction interval
     * @param intype order of polynomial subtracted, or SPLINE if used
     */
    public void addBaselineStep(double instart, double instop, CorrectionOrder intype ) {
        blcorrect entry = new blcorrect(instart, instop, intype);
        blist.add(entry);
    }
    /**
     * Clears the steps in the recorder
     */
    public void clearSteps() {
        preEventMean = 0.0;
        eventOnsetTime = 0.0;
        ctype = CorrectionType.AUTO;
        blist.clear();
    }
    /**
     * Formats the processing steps for inclusion in the comments section of the
     * product files. Tags are added in front of each processing step and units are
     * specified.  The returned list of text can be added directly to the end
     * of the comment section.
     * @return a list of text to append to the comment section
     */
    public ArrayList<String> formatSteps() {
        String timeformat = "%8.3f";
        String meanformat = "%12.5e";
        String lineToAdd;
        int len = blist.size();
        ArrayList<String> outlist = new ArrayList<>();
        outlist.add(String.format("|<PROCESS> %1$s", ctype.name()));
        outlist.add(String.format("|<EONSET> event onset(sec)=%1$8s",
                                    String.format(timeformat,eventOnsetTime)));
        outlist.add(String.format("|<PREMEAN> Pre-event mean removed (cm/sq.sec) %1$12s",
                                        String.format(meanformat, preEventMean)));
        for (blcorrect blc : blist) {
            outlist.add(String.format("|<BLCORRECT> start(sec): %1$8s, stop(sec): %2$8s, order: %3$s",
                    String.format(timeformat,blc.getStart()),
                    String.format(timeformat,blc.getStop()),blc.getOrder().name()));
        }
        return outlist;
    }
    /**
     * This private class defines an object to hold one baseline correction entry.
     * It has fields for the start time, stop time, and the polynomial order.
     * Each object is created with the constructor and elements are accessed
     * through the getters.
     */
    private class blcorrect {
        private final double start;
        private final double stop;
        private final CorrectionOrder order;
        
        public blcorrect( double strt, double stp, CorrectionOrder od) {
            this.start = strt;
            this.stop = stp;
            this.order = od;
        }
        public double getStart() {
            return start;
        }
        public double getStop() {
            return stop;
        }
        public CorrectionOrder getOrder() {
            return order;
        }
    }
}
