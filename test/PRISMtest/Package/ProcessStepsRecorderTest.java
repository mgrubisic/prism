/*******************************************************************************
 * Name: Java class ProcessStepsRecorderTest.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package PRISMtest.Package;

import SmConstants.VFileConstants;
import SmUtilities.ProcessStepsRecorder2;
import org.junit.Test;

/**
 *
 * @author jmjones
 */
public class ProcessStepsRecorderTest {
    String[] test1;
    String[] test2;
    String[] testresamp;
    String[] testtrim;
    String[] empty;
    
    public ProcessStepsRecorderTest() {
        test1 = new String[4];
        test1[0] = "|<PROCESS> MANUAL";
        test1[1] = "|<EONSET> event onset(sec)=  31.9800";
        test1[2] = "|<ABLC>SF:   0.0000, EF:  31.9800, SA:   0.0000, EA: 132.0000, ORDER:  MEAN";
        test1[3] = "|<VBLC>SF:   0.0000, EF: 132.0000, SA:   0.0000, EA: 132.0000, ORDER:ORDER2";
        
        empty = new String[2];
        empty[0] = "|<PROCESS> AUTO";
        empty[1] = "|<EONSET> event onset(sec)=   0.0000";
        
        test2 = new String[6];
        test2[0] = "|<PROCESS> AUTO";
        test2[1] = "|<EONSET> event onset(sec)=   5.0000";
        test2[2] = "|<ABLC>SF:   0.0000, EF:   5.0000, SA:   0.0000, EA: 124.0000, ORDER:  MEAN";
        test2[3] = "|<VBLABC1>SF:   0.0000, EF:  12.0000, SA:   0.0000, EA:  12.0000, ORDER:ORDER1";
        test2[4] = "|<VBLABC2>SF:  12.0000, EF:  24.0000, SA:  12.0000, EA:  24.0000, ORDER:SPLINE";
        test2[5] = "|<VBLABC3>SF:  24.0000, EF: 124.0000, SA:  24.0000, EA: 124.0000, ORDER:ORDER3";

        testresamp = new String[3];
        testresamp[0] = "|<PROCESS> AUTO";
        testresamp[1] = "|<RESAMPLE> Data resampled to 200.00 samples/sec";
        testresamp[2] = "|<EONSET> event onset(sec)=  15.3200";
        
        testtrim = new String[3];
        testtrim[0] = "|<PROCESS> MANUAL";
        testtrim[1] = "|<TRIM> 260 samp. of beginning, 400 samp. of end of original record";
        testtrim[2] = "|<EONSET> event onset(sec)=  14.6600";
    }
    
    @Test
    public void test1Recorder() {
        ProcessStepsRecorder2 stepRec = ProcessStepsRecorder2.INSTANCE;
        stepRec.clearSteps();
        stepRec.addEventOnset(31.98);
        stepRec.addBaselineStep(0.0, 31.98,0.0, 132.00, 
                                VFileConstants.V2DataType.ACC, 
                                VFileConstants.BaselineType.BESTFIT, 
                                VFileConstants.CorrectionOrder.MEAN, 0);
        stepRec.addCorrectionType(VFileConstants.CorrectionType.MANUAL);
        stepRec.addBaselineStep(0.0, 132.00, 0.0, 132.00,
                                VFileConstants.V2DataType.VEL,
                                VFileConstants.BaselineType.BESTFIT,
                                VFileConstants.CorrectionOrder.ORDER2, 0);
        String[] result = new String[4];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(test1,result);
        
        stepRec.clearSteps();
        result = new String[2];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(empty, result);
    }
    @Test
    public void test2Recorder() {
        ProcessStepsRecorder2 stepRec = ProcessStepsRecorder2.INSTANCE;
        stepRec.clearSteps();
        stepRec.addEventOnset(5.0);
        stepRec.addBaselineStep(0.0,5.0,0.0,124.0,
                                VFileConstants.V2DataType.ACC, 
                                VFileConstants.BaselineType.BESTFIT, 
                                VFileConstants.CorrectionOrder.MEAN, 0);
        stepRec.addCorrectionType(VFileConstants.CorrectionType.AUTO);
        stepRec.addBaselineStep(0.0, 12.00,0.0, 12.00,
                                VFileConstants.V2DataType.VEL,
                                VFileConstants.BaselineType.ABC,
                                VFileConstants.CorrectionOrder.ORDER1,1);
        stepRec.addBaselineStep(12.0, 24.00,12.0, 24.00,
                                 VFileConstants.V2DataType.VEL,
                                 VFileConstants.BaselineType.ABC,
                                VFileConstants.CorrectionOrder.SPLINE,2);
        stepRec.addBaselineStep(24.0, 124.00,24.0, 124.00,
                               VFileConstants.V2DataType.VEL,
                               VFileConstants.BaselineType.ABC,
                                VFileConstants.CorrectionOrder.ORDER3,3);
        String[] result = new String[6];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(test2,result);
    }
    @Test
    public void testRecorderResample() {
        ProcessStepsRecorder2 stepRec = ProcessStepsRecorder2.INSTANCE;
        stepRec.clearSteps();
        stepRec.addCorrectionType(VFileConstants.CorrectionType.AUTO);
        stepRec.addEventOnset(15.32);
        stepRec.addResampling(200.00);
        String[] result = new String[3];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(testresamp,result);
    }
    @Test
    public void testRecorderTrim() {
        ProcessStepsRecorder2 stepRec = ProcessStepsRecorder2.INSTANCE;
        stepRec.clearSteps();
        stepRec.addCorrectionType(VFileConstants.CorrectionType.MANUAL);
        stepRec.addEventOnset(14.66);
        stepRec.addTrimIndicies(260,400);
        String[] result = new String[3];
        result = stepRec.formatSteps().toArray(result);
        org.junit.Assert.assertArrayEquals(testtrim,result);
    }
}
