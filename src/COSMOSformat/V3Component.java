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

package COSMOSformat;

import SmConstants.VFileConstants;
import static SmConstants.VFileConstants.DEFAULT_REAL_FIELDWIDTH;
import static SmConstants.VFileConstants.DELTA_T;
import static SmConstants.VFileConstants.MAX_LINE_LENGTH;
import static SmConstants.VFileConstants.MSEC_TO_SEC;
import SmException.FormatException;
import SmException.SmException;
import SmProcessing.V3Process;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.PROC_AGENCY_ABBREV;
import static SmUtilities.SmConfigConstants.PROC_AGENCY_CODE;
import SmUtilities.SmTimeFormatter;

/**
 *
 * @author jmjones
 */
public class V3Component extends COSMOScontentFormat {
    private VRealArray V3Data;
    private final V1Component parentV1;  //link back to the parent V1 record
    private final V2Component parentV2;

    public V3Component(String procType) {
        super(procType);
        this.parentV1 = null;
        this.parentV2 = null;
    }
    //Use this constructor when the V2 component is created from processing
    //done on a V1 component.  In this case, the contents of V2 are initialized
    //to the V1 values and updated during the processing.
    public V3Component( String procType, V2Component pV2) throws FormatException, 
                                                                SmException {
        super( procType );
        this.parentV1 = pV2.getParent();
        this.parentV2 = pV2;
        //Load the text header with parent V1 values.  Leave the update to the V2
        //values to the buildV2 method.
        this.noIntVal = pV2.noIntVal;
        this.noRealVal = pV2.noRealVal;
        this.textHeader = pV2.getTextHeader();
        //Load the headers with parent V1 values.
        //Leave updates for buildV2 method
        this.intHeader = new VIntArray(pV2.intHeader);        
        this.realHeader = new VRealArray(pV2.realHeader);
        this.setChannelNum();
        
        //The buildV2 method fills in these data values, the format line, and
        //the individual params for the real arrays.
        this.V3Data = new VRealArray();
        
        this.comments = pV2.getComments(); //leave update for processing, if any
        this.endOfData = pV2.endOfData; //leave update for buildV2
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        int current = startLine;
        
        V3Data = new VRealArray();
        current = V3Data.parseValues( current, infile);
        return current;
    }
    /**
     * Getter for the length of the data array
     * @return the number of values in the data array
     */
    public int getDataLength() {
        return V3Data.getNumVals();
    }
    /**
     * Getter for a copy of the data array reference.  Used to access the entire
     * array during data processing.
     * @return a copy of the array reference
     */
    public double[] getDataArray() {
        return V3Data.getRealArray();
    }
    public void buildV3(V3Process inVvals) throws SmException, FormatException {
        Double epsilon = 0.001;
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        StringBuilder eod = new StringBuilder(MAX_LINE_LENGTH);
        final double MSEC_TO_SEC = 1e-3;
        String realformat = "%8.3f";
        String freqformat = "%5.2f";
        double time;
        String unitsname;
        int unitscode;
        String eodname;

        SmTimeFormatter proctime = new SmTimeFormatter();
        ConfigReader config = ConfigReader.INSTANCE;
        
        this.realHeader.setFieldWidth(DEFAULT_REAL_FIELDWIDTH);
        this.realHeader.buildArrayParams( VFileConstants.SmArrayStyle.PACKED );
        this.setRealHeaderFormatLine();
        
        //Get the processing agency info from the config. data
        String agabbrev = config.getConfigValue(PROC_AGENCY_ABBREV);
        agabbrev = (agabbrev == null) ? agabbrev = "Unknown" : agabbrev;
        
        String agcode = config.getConfigValue(PROC_AGENCY_CODE);
        int agency_code = (agcode == null) ? 0 : Integer.parseInt(agcode);
        
        //get real header value 62 (it has already been validated in the processing)
        double delta_t = this.realHeader.getRealValue(DELTA_T);
    }
}
