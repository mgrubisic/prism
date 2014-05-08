/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

import static COSMOSformat.VFileConstants.*;
import SmException.FormatException;
import SmException.SmException;
import SmProcessing.V1Process;

/**
 *
 * @author jmjones
 */
public class V1Component extends COSMOScontentFormat {
    private VRealArray V1Data;  //raw acceleration counts
    private final V0Component parentV0;  //link back to the parent V0 record

    //Use this constructor when the V1 component is read in from a file and
    //filled in with the loadComponent method.  In this case, there is no parentV0
    //associated with this V1
    public V1Component( String procType) {
        super( procType );
        this.parentV0 = null;
    }
    
    //Use this constructor when the V1 component is created from processing
    //done on a V0 component.  In this case, the contents of V1 are initialized
    //to the V0 values and updated during the processing.
    public V1Component( String procType, V0Component pV0) throws FormatException, 
                                                                SmException {
        super( procType );
        this.parentV0 = pV0;
        //Load the text header with parent V0 values.  Leave the update to the V1
        //values to the buildV1 method.
        this.noIntVal = pV0.noIntVal;
        this.noRealVal = pV0.noRealVal;
        this.textHeader = pV0.getTextHeader();
        
        //Load the headers with parent V0 values.
        //Leave updates for buildV1 method
        this.intHeader = new VIntArray(pV0.intHeader);        
        this.realHeader = new VRealArray(pV0.realHeader);
        this.setChannelNum();
        
        //The buildV1 method fills in these data values, the format line, and
        //the individual params for the real array.
        this.V1Data = new VRealArray();
        
        this.comments = pV0.getComments(); //leave update for processing, if any
        this.endOfData = pV0.endOfData; //same as V0
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                        FormatException, NumberFormatException {
        int current = startLine;
        
        V1Data = new VRealArray();
        current = V1Data.parseValues( current, infile);
        return current;
    }
    //Once in this method, the V1Process object is no longer needed and its array
    //is transferred to the V1component object
    public void buildV1 (V1Process inVvals) throws FormatException {
        final double MSEC_TO_SEC = 1e-3;
        //update values in the text header
        this.textHeader[0] = this.textHeader[0].replaceAll(RAWACC, UNCORACC);
        //update the processing date to the current date!!!
        
        //transfer the data array and set all array values
        V1Data.setRealArray(inVvals.getV1Array());
        V1Data.setFieldWidth(REAL_FIELDWIDTH_V1);
        V1Data.setPrecision(REAL_PRECISION_V1);
        V1Data.setNumVals(inVvals.getV1ArrayLength());
        V1Data.buildArrayParams();
        this.buildNewDataFormatLine();
        
        //update the headers with the V1 values
        this.intHeader.setIntValue(PROCESSING_STAGE_INDEX, V1_STAGE);
        this.intHeader.setIntValue(V1_UNITS_INDEX, CM_SEC_SEC);
        this.intHeader.setIntValue(PROCESSING_AGENCY, PROCESSING_AGENCY_USGS);
        this.realHeader.setRealValue(MEAN_ZERO, inVvals.getMeanToZero());
        this.realHeader.setRealValue(MAX_VAL, inVvals.getMaxVal());
        this.realHeader.setRealValue(AVG_VAL, inVvals.getAvgVal());
        double time = (inVvals.getMaxIndex()) * MSEC_TO_SEC *
                                        this.realHeader.getRealValue(DELTA_T);
        this.realHeader.setRealValue(MAX_VAL_TIME, time);
    }
    public void buildNewDataFormatLine() {
        //calculate the time by multiplying the number of data values by delta t
        String line = "";
        double dtime = this.getRealHeaderValue(DELTA_T);
        double calcTime = dtime * this.realHeader.getNumVals();
        String timeSec = Integer.toString((int)calcTime);
        String datType = "uncor. accel.";
        line = String.format("%1$8s %2$13s pts, approx %3$4s secs, units=%4$7s(%5$2s), Format=",
                                     String.valueOf(V1Data.getNumVals()),datType,
                                                    timeSec, CMSQSECT, CMSQSECN);
        V1Data.setFormatLine(line + V1Data.getNumberFormat());
    }
    
    public String[] V1ToText() {
        //add up the length of the text portions of the component, which are
        //the text header, the comments, and the end-of-data line.
        int totalLength = 0;
        int currentLength = 0;
        int textLength = this.textHeader.length + this.comments.length + 1;
        
        //get the text versions of the header and data arrays
        String[] intHeaderText = this.intHeader.numberSectionToText();
        String[] realHeaderText = this.realHeader.numberSectionToText();
        String[] V1DataText = this.V1Data.numberSectionToText();
        
        //add the array lengths to the text lengths to get the total and declare
        //an array of this length, then build it by combining all the component
        //pieces into a text version of the component.
        totalLength = textLength + intHeaderText.length + realHeaderText.length + 
                                                             V1DataText.length;
        String[] outText = new String[totalLength];
        System.arraycopy(this.textHeader, 0, outText, currentLength, this.textHeader.length);
        currentLength = currentLength + this.textHeader.length;
        System.arraycopy(intHeaderText, 0, outText, currentLength, intHeaderText.length);
        currentLength = currentLength + intHeaderText.length;
        System.arraycopy(realHeaderText, 0, outText, currentLength, realHeaderText.length);
        currentLength = currentLength + realHeaderText.length;
        System.arraycopy(this.comments, 0, outText, currentLength, this.comments.length);
        currentLength = currentLength + this.comments.length;
        System.arraycopy(V1DataText, 0, outText, currentLength, V1DataText.length);
        outText[totalLength-1] = this.endOfData;
        return outText;
    }
}
