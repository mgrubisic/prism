/*******************************************************************************
 * Name: Java class ProcessStepsRecorderTest.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package PRISMtest.Package;

import SmConstants.VFileConstants;
import SmUtilities.ProcessStepsRecorder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jmjones
 */
public class ProcessStepsRecorderTest {
    String[] test1;
    String[] test2;
    String[] empty;
    
    public ProcessStepsRecorderTest() {
        test1 = new String[4];
        test1[0] = "|<PROCESS> MANUAL";
        test1[1] = "|<EONSET> event onset(sec)=  31.980";
        test1[2] = "|<PREMEAN> Pre-event mean removed (cm/sq.sec) -2.24763e-04";
        test1[3] = "|<BLCORRECT> start(sec):    0.000, stop(sec):  132.000, order: ORDER2";
        
        empty = new String[3];
        empty[0] = "|<PROCESS> AUTO";
        empty[1] = "|<EONSET> event onset(sec)=   0.000";
        empty[2] = "|<PREMEAN> Pre-event mean removed (cm/sq.sec)  0.00000e+00";
        
        test2 = new String[7];
        test2[0] = "|<PROCESS> AUTO_W_REVIEW";
        test2[1] = "|<EONSET> event onset(sec)=   5.000";
        test2[2] = "|<PREMEAN> Pre-event mean removed (cm/sq.sec)  1.12345e+01";
        test2[3] = "|<BLCORRECT> start(sec):    0.000, stop(sec):    6.000, order: ORDER1";
        test2[4] = "|<BLCORRECT> start(sec):    6.000, stop(sec):   12.000, order: ORDER2";
        test2[5] = "|<BLCORRECT> start(sec):   12.000, stop(sec):   24.000, order: SPLINE";
        test2[6] = "|<BLCORRECT> start(sec):   24.000, stop(sec):  124.000, order: ORDER3";
    }
    
    @Test
    public void test1Recorder() {
        ProcessStepsRecorder stepRec = ProcessStepsRecorder.INSTANCE;
        stepRec.clearSteps();
        stepRec.addEventOnset(31.98);
        stepRec.addPreEventMean(-0.000224763);
        stepRec.addCorrectionType(VFileConstants.CorrectionType.MANUAL);
        stepRec.addBaselineStep(0.0, 132.00, VFileConstants.CorrectionOrder.ORDER2);
        String[] result = new String[4];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(test1,result);
        
        stepRec.clearSteps();
        result = new String[3];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(empty, result);
    }
    @Test
    public void test2Recorder() {
        ProcessStepsRecorder stepRec = ProcessStepsRecorder.INSTANCE;
        stepRec.clearSteps();
        stepRec.addEventOnset(5.0);
        stepRec.addPreEventMean(11.2345);
        stepRec.addCorrectionType(VFileConstants.CorrectionType.AUTO_W_REVIEW);
        stepRec.addBaselineStep(0.0, 6.00, VFileConstants.CorrectionOrder.ORDER1);
        stepRec.addBaselineStep(6.0, 12.00, VFileConstants.CorrectionOrder.ORDER2);
        stepRec.addBaselineStep(12.0, 24.00, VFileConstants.CorrectionOrder.SPLINE);
        stepRec.addBaselineStep(24.0, 124.00, VFileConstants.CorrectionOrder.ORDER3);
        String[] result = new String[7];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(test2,result);
    }
}
