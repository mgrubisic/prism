/*******************************************************************************
 * Name: Java class ProcessStepsRecorder.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * Update by: Peter Ng, USGS, png@usgs.gov
 * 
 * Date: first release date Feb. 2015
 *       update May 2015 to incorporate tags for manual processing
 ******************************************************************************/

package SmUtilities;

import SmConstants.VFileConstants.BaselineType;
import SmConstants.VFileConstants.CorrectionOrder;
import SmConstants.VFileConstants.CorrectionType;
import SmConstants.VFileConstants.V2DataType;
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
public class ProcessStepsRecorder2 {
    private double eventOnsetTime;
    private CorrectionType ctype;
    private ArrayList<blcorrect> blist;
    public final static ProcessStepsRecorder2 INSTANCE = new ProcessStepsRecorder2();
    /**
     * Private constructor for the singleton class.
     */
    private ProcessStepsRecorder2(){
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
     * Adds a flag for the method of correction, such as manual or automatic
     * @param intype the correction type
     */
    public void addCorrectionType( CorrectionType intype ) {
        ctype = intype;
    }
    /**
     * Adds baseline correction information to the recorder.  Baseline corrections
     * are recorded by identifying the start and stop times for the baseline function
     * as well as the start and stop times of the application interval over which
     * baseline correction was applied.  The function itself is identified by its 
     * polynomial order of 1,2, or 3, or by SPLINE if the spline algorithm was used 
     * in adaptive baseline correction.
     * @param fstart start time for baseline function, in seconds
     * @param fstop stop time for baseline function
     * @param astart start time of application interval baseline correction was applied to
     * @param astop stop time of application interval for baseline correction was applied to
     * @param v2datatype V2 data to which baseline correction was applied
     * @param btype best fit or adaptive
     * @param intype order of polynomial subtracted, or SPLINE if used
     * @param cstep adaptive baseline correction step
     */
    public void addBaselineStep(double fstart, double fstop, double astart, double astop, 
        V2DataType v2datatype, BaselineType btype, CorrectionOrder intype, int cstep) {
        blcorrect entry = new blcorrect(fstart, fstop, astart, astop, v2datatype,
            btype, intype, cstep);
        blist.add(entry);
    }
    /**
     * Clears the steps in the recorder
     */
    public void clearSteps() {
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
        String timeformat = "%9.4f";
        String lineToAdd;
        int len = blist.size();
        ArrayList<String> outlist = new ArrayList<>();
        outlist.add(String.format("|<PROCESS> %1$s", ctype.name()));
        outlist.add(String.format("|<EONSET> event onset(sec)=%1$8s",
            String.format(timeformat,eventOnsetTime)));
        for (blcorrect blc : blist) {
            String blTag = (blc.getBaselineType().equals(BaselineType.ABC)) ?
                "VBLADAPT"+blc.getBaselineStep() :
                blc.getV2DataType().toString().substring(0, 1)+"BLCORR";
            
            outlist.add(String.format("|<%1s>SF: %2$8s, EF: %3$8s, SA: %4$8s, EA: %5$8s, ORDER: %6$s",
                blTag,
                String.format(timeformat,blc.getFunctionStart()),
                String.format(timeformat,blc.getFunctionStop()),
                String.format(timeformat,blc.getAppStart()),
                String.format(timeformat,blc.getAppStop()),
                blc.getOrder().name()));
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
        private final double fstart;
        private final double fstop;
        private final double astart;
        private final double astop;
        private final V2DataType v2datatype;
        private final BaselineType btype;
        private final CorrectionOrder order;
        private final int bstep;
        
        public blcorrect( double fstrt, double fstp, double astrt, double astp,
            V2DataType v2datatype, BaselineType btp, CorrectionOrder od, int bstp) {
            this.fstart = fstrt;
            this.fstop = fstp;
            this.astart = astrt;
            this.astop = astp;
            this.v2datatype = v2datatype;
            this.btype = btp;
            this.order = od;
            this.bstep = bstp;
        }
        
        public double getFunctionStart() {return fstart;}
        public double getFunctionStop() {return fstop;}
        public double getAppStart() {return astart;}
        public double getAppStop() {return astop;}
        public V2DataType getV2DataType() {return v2datatype;}
        public BaselineType getBaselineType() {return btype;}
        public CorrectionOrder getOrder() {return order;}
        public int getBaselineStep() {return bstep;}
    }
}
