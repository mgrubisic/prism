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
import SmUtilities.ConfigReader;

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
    public void buildV2( String procType, V1Process inV1vals, ConfigReader config) {
        //fill in!!!
    }
    public void buildNewDataFormatLine(String units, int unitscode) throws SmException {
        ///update this for V2, with different data types!!!!
        //calculate the time by multiplying the number of data values by delta t
        String line;
        double dtime = this.getRealHeaderValue(DELTA_T);
        double calcTime = dtime * this.realHeader.getNumVals();
        String timeSec = Integer.toString((int)calcTime);
        String datType = "acceleration";
        line = String.format("%1$8s %2$13s pts, approx %3$4s secs, units=%4$7s(%5$02d), Format=",
                                     String.valueOf(V2Data.getNumVals()),datType,
                                                    timeSec, units, unitscode);
        V2Data.setFormatLine(line + V2Data.getNumberFormat());
    }
    public String[] V2ToText() {
        //add up the length of the text portions of the component, which are
        //the text header, the comments, and the end-of-data line.
        int totalLength;
        int currentLength = 0;
        int textLength = this.textHeader.length + this.comments.length + 1;
        
        //get the header and data arrays as text
        String[] intHeaderText = this.intHeader.numberSectionToText();
        String[] realHeaderText = this.realHeader.numberSectionToText();
        String[] V1DataText = this.V2Data.numberSectionToText();
        
        //add the array lengths to the text lengths to get the total and declare
        //an array of this length, then build it by combining all the component
        //pieces into a text version of the component.
        totalLength = textLength + intHeaderText.length + realHeaderText.length + 
                                                        V1DataText.length;
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
        System.arraycopy(V1DataText, 0, outText, currentLength, V1DataText.length);
        outText[totalLength-1] = this.endOfData;
        return outText;
    }
}
