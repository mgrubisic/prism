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
import SmProcessing.V2Process;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.PROC_AGENCY_ABBREV;
import static SmUtilities.SmConfigConstants.PROC_AGENCY_CODE;
import SmUtilities.SmTimeFormatter;

/**
 *
 * @author jmjones
 */
public class V2Component extends COSMOScontentFormat {
    private VRealArray V2Data;
    private final V0Component parentV0;  //link back to the parent V0 record
    private final V1Component parentV1;  //link back to the parent V1 record

    public V2Component( String procType){
        super( procType );
        this.parentV0 = null;
        this.parentV1 = null;
    }
    //Use this constructor when the V2 component is created from processing
    //done on a V1 component.  In this case, the contents of V2 are initialized
    //to the V1 values and updated during the processing.
    public V2Component( String procType, V1Component pV1) throws FormatException, 
                                                                SmException {
        super( procType );
        this.parentV1 = pV1;
        this.parentV0 = pV1.getParent();
        //Load the text header with parent V1 values.  Leave the update to the V2
        //values to the buildV2 method.
        this.noIntVal = pV1.noIntVal;
        this.noRealVal = pV1.noRealVal;
        this.textHeader = pV1.getTextHeader();
        
        //Load the headers with parent V1 values.
        //Leave updates for buildV2 method
        this.intHeader = new VIntArray(pV1.intHeader);        
        this.realHeader = new VRealArray(pV1.realHeader);
        this.setChannelNum();
        
        //The buildV2 method fills in these data values, the format line, and
        //the individual params for the real arrays.
        this.V2Data = new VRealArray();
        
        this.comments = pV1.getComments(); //leave update for processing, if any
        this.endOfData = pV1.endOfData; //leave update for buildV2
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        int current = startLine;
        
        V2Data = new VRealArray();
        current = V2Data.parseValues( current, infile);
        return current;
    }
    public V1Component getParent() {
        return this.parentV1;
    }
    public void buildV2acc( String procType, V2Process inVvals, ConfigReader config) throws SmException, FormatException {
        Double epsilon = 0.001;
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        StringBuilder eod = new StringBuilder(MAX_LINE_LENGTH);
        final double MSEC_TO_SEC = 1e-3;
        String realformat = "%8.3f";

        SmTimeFormatter proctime = new SmTimeFormatter();
        
        //verify that real header value delta t is defined and valid
        double delta_t = this.realHeader.getRealValue(DELTA_T);
        if (((delta_t - this.noRealVal) < epsilon) || (delta_t < 0.0)){
            throw new SmException("Real header #62, delta t, is invalid: " + 
                                                                        delta_t);
        }        
        double time = (inVvals.getMaxIndex(V2DataType.ACC)) * MSEC_TO_SEC * delta_t;

        //Get the processing agency info from the config. data
        String agabbrev = config.getConfigValue(PROC_AGENCY_ABBREV);
        if (agabbrev == null) {
            agabbrev = "Unknown";
        }
        String agcode = config.getConfigValue(PROC_AGENCY_CODE);
        int agency_code = (agcode == null) ? 0 : Integer.parseInt(agcode);
        
        //Get units info from V1 processing object
        String unitsname = inVvals.getDataUnits(V2DataType.ACC);
        int unitscode = inVvals.getDataUnitCode(V2DataType.ACC);
        
        //Get the current processing time
        String val = proctime.getGMTdateTime();
        System.out.println("+++ time: " + val);
        //update values in the text header
        this.textHeader[0] = UNCORACC.concat(this.textHeader[0].substring(END_OF_DATATYPE));
        this.textHeader[10] = sb.append("Processed:").append(val).append(", ")
                                .append(agabbrev).append(", Max = ")
                                .append(String.format(realformat,inVvals.getMaxVal(V2DataType.ACC)))
                                .append(" ").append(unitsname).append(" at ")
                                .append(String.format(realformat,time))
                                .append(" sec").toString();
        
        //transfer the data array and set all array values
        V2Data.setRealArray(inVvals.getV2Array(V2DataType.ACC));
        V2Data.setFieldWidth(REAL_FIELDWIDTH_V1);
        V2Data.setPrecision(REAL_PRECISION_V1);
        V2Data.setNumVals(inVvals.getV2ArrayLength(V2DataType.ACC));
        V2Data.buildArrayParams();
        this.buildNewDataFormatLine(unitsname, unitscode, "acceleration");
        
        //update the headers with the V1 values
        this.intHeader.setIntValue(PROCESSING_STAGE_INDEX, V2_STAGE);
        this.intHeader.setIntValue(V1_UNITS_INDEX, unitscode);
        this.intHeader.setIntValue(PROCESSING_AGENCY, agency_code);
        this.realHeader.setRealValue(MAX_VAL, inVvals.getMaxVal(V2DataType.ACC));
        this.realHeader.setRealValue(AVG_VAL, inVvals.getAvgVal(V2DataType.ACC));
        this.realHeader.setRealValue(MAX_VAL_TIME, time);
        
        //Update the end-of-data line with the new data type
        this.endOfData = eod.append(this.endOfData,0,END_OF_DATA_CHAN)
                            .append(" ")
                            .append(String.valueOf(unitscode))
                            .append(" acceleration").toString();
    }
    public void buildNewDataFormatLine(String units, int unitscode, 
                                        String dataType) throws SmException {
        //calculate the time by multiplying the number of data values by delta t
        String line;
        double dtime = this.getRealHeaderValue(DELTA_T);
        double calcTime = dtime * this.realHeader.getNumVals();
        String timeSec = Integer.toString((int)calcTime);
        line = String.format("%1$8s %2$13s pts, approx %3$4s secs, units=%4$7s(%5$02d), Format=",
                                     String.valueOf(V2Data.getNumVals()),dataType,
                                                    timeSec, units, unitscode);
        V2Data.setFormatLine(line + V2Data.getNumberFormat());
    }
    @Override
    public String[] VrecToText() {
        //add up the length of the text portions of the component, which are
        //the text header, the comments, and the end-of-data line.
        int totalLength;
        int currentLength = 0;
        int textLength = this.textHeader.length + this.comments.length + 1;
        
        //get the header and data arrays as text
        String[] intHeaderText = this.intHeader.numberSectionToText();
        String[] realHeaderText = this.realHeader.numberSectionToText();
        String[] V2DataText = this.V2Data.numberSectionToText();
        
        //add the array lengths to the text lengths to get the total and declare
        //an array of this length, then build it by combining all the component
        //pieces into a text version of the component.
        totalLength = textLength + intHeaderText.length + realHeaderText.length + 
                                                        V2DataText.length;
        String[] outText = new String[totalLength];
        System.arraycopy(this.textHeader, 0, outText, currentLength, 
                                                        this.textHeader.length);
        currentLength = currentLength + this.textHeader.length;
        System.arraycopy(intHeaderText, 0, outText, currentLength, 
                                                            intHeaderText.length);
        currentLength = currentLength + intHeaderText.length;
        System.arraycopy(realHeaderText, 0, outText, currentLength, 
                                                          realHeaderText.length);
        currentLength = currentLength + realHeaderText.length;
        System.arraycopy(this.comments, 0, outText, currentLength, this.comments.length);
        currentLength = currentLength + this.comments.length;
        System.arraycopy(V2DataText, 0, outText, currentLength, V2DataText.length);
        outText[totalLength-1] = this.endOfData;
        return outText;
    }
}
