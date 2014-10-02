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
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.SmArrayStyle;
import static SmConstants.VFileConstants.V3_DAMPING_VALUES;
import SmException.FormatException;
import SmException.SmException;
import SmProcessing.V3Process;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.OUT_ARRAY_FORMAT;
import static SmUtilities.SmConfigConstants.PROC_AGENCY_ABBREV;
import static SmUtilities.SmConfigConstants.PROC_AGENCY_CODE;
import SmUtilities.SmTimeFormatter;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class V3Component extends COSMOScontentFormat {
    private String V3DampingValues;
    private ArrayList<VRealArray> V3Data;
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
        //Load the headers with parent V2 values.
        //Leave updates for buildV3 method
        this.intHeader = new VIntArray(pV2.intHeader);        
        this.realHeader = new VRealArray(pV2.realHeader);
        this.setChannelNum();
        
        //The buildV2 method fills in these data values, the format line, and
        //the individual params for the real arrays.
        this.V3Data = new ArrayList<>();
        
        this.comments = pV2.getComments(); //leave update for processing, if any
        this.endOfData = pV2.endOfData; //leave update for buildV3
    }
    @Override
    public int parseDataSection (int startLine, String[] infile) throws 
                                                            FormatException {
        int current = startLine;
        V3DampingValues = infile[current++];
        VRealArray Periods = new VRealArray();
        current = Periods.parseValues(current, infile);
        V3Data.add(Periods);
        VRealArray fftVals = new VRealArray();
        current = fftVals.parseValues(current, infile);
        V3Data.add(fftVals);
        VRealArray spectra;
        for (int i = 0; i < NUM_V3_SPECTRA_ARRAYS; i++) {
            spectra = new VRealArray();
            current = spectra.parseValues(current, infile);
            V3Data.add(spectra);
        }
        return current;
    }
    /**
     * Getter for the length of the data array
     * @return the number of values in the data array
     */
    public int getDataLength() {
        return V3Data.size();
    }
    /**
     * Getter for a copy of the data array reference.  Used to access each array
     * of the V3 array sequence
     * @param arrnum the index of the array to retrieve from the V3 array list
     * @return a copy of the array reference
     */
    public double[] getDataArray(int arrnum) {
        VRealArray varray;
        varray = V3Data.get(arrnum);
        return varray.getRealArray();
    }
    public void buildV3(V3Process inVvals) throws SmException, FormatException {
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        StringBuilder eod = new StringBuilder(MAX_LINE_LENGTH);
        double time;
        String unitsname;
        int unitscode;
        String eodname;
        String line;
        SmArrayStyle packtype;

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
        //Get the array output format of single column per channel or packed
        String arrformat = config.getConfigValue(OUT_ARRAY_FORMAT);
        if ((arrformat == null) || (arrformat.contentEquals("Packed"))) {
            packtype = SmArrayStyle.PACKED;
        } else {
            packtype = SmArrayStyle.SINGLE_COLUMN;
        }
        //Make the V3 damping values line
        V3DampingValues = sb.append(String.format("%1$4s", String.valueOf(V3_DAMPING_VALUES.length)))
                .append(" damping values for which spectra are computed:")
                .append(String.format("%1$04.2f,", V3_DAMPING_VALUES[0]))
                .append(String.format("%1$04.2f,", V3_DAMPING_VALUES[1]))
                .append(String.format("%1$04.2f,", V3_DAMPING_VALUES[2]))
                .append(String.format("%1$04.2f,", V3_DAMPING_VALUES[3]))
                .append(String.format("%1$04.2f", V3_DAMPING_VALUES[4]))
                .toString();
        
        //transfer the data arrays, starting with the periods
        VRealArray Periods = new VRealArray();
        Periods.setRealArray(inVvals.getV3Array(0));
        Periods.setNumVals(NUM_T_PERIODS);
        Periods.setPrecision(REAL_PRECISION_V3);
        Periods.setFieldWidth(REAL_FIELDWIDTH_V3);
        Periods.setDisplayType("F");
        Periods.buildArrayParams(packtype);
        line = buildNewDataFormatLine(SECT, SECN, "periods","",0);
        Periods.setFormatLine(line + Periods.getNumberFormat());
        V3Data.add(Periods);
        
        VRealArray fftvals = new VRealArray();
        fftvals.setRealArray(inVvals.getV3Array(1));
        fftvals.setNumVals(NUM_T_PERIODS);
        fftvals.setPrecision(REAL_PRECISION_V3);
        fftvals.setFieldWidth(REAL_FIELDWIDTH_V3);
        fftvals.setDisplayType("E");
        fftvals.buildArrayParams(packtype);
        line = buildNewDataFormatLine(CMSECT, CMSECN, "fft","",0);
        fftvals.setFormatLine(line + fftvals.getNumberFormat());
        V3Data.add(fftvals);
        
        VRealArray sarray;
        int arrcount = 2;
        for (int s = 0; s < V3_DAMPING_VALUES.length; s++) {
            sarray = new VRealArray();
            sarray.setRealArray(inVvals.getV3Array(arrcount++));
            sarray.setNumVals(NUM_T_PERIODS);
            sarray.setPrecision(REAL_PRECISION_V3);
            sarray.setFieldWidth(REAL_FIELDWIDTH_V3);
            sarray.setDisplayType("E");
            sarray.buildArrayParams(packtype);
            line = buildNewDataFormatLine(CMT, CMN, "spectra","Sd",
                                                            V3_DAMPING_VALUES[s]);
            sarray.setFormatLine(line + sarray.getNumberFormat());
            V3Data.add(sarray);

            sarray = new VRealArray();
            sarray.setRealArray(inVvals.getV3Array(arrcount++));
            sarray.setNumVals(NUM_T_PERIODS);
            sarray.setPrecision(REAL_PRECISION_V3);
            sarray.setFieldWidth(REAL_FIELDWIDTH_V3);
            sarray.setDisplayType("E");
            sarray.buildArrayParams(packtype);
            line = buildNewDataFormatLine(CMSECT, CMSECN, "spectra","Sv",
                                                            V3_DAMPING_VALUES[s]);
            sarray.setFormatLine(line + sarray.getNumberFormat());
            V3Data.add(sarray);

            sarray = new VRealArray();
            sarray.setRealArray(inVvals.getV3Array(arrcount++));
            sarray.setNumVals(NUM_T_PERIODS);
            sarray.setPrecision(REAL_PRECISION_V3);
            sarray.setFieldWidth(REAL_FIELDWIDTH_V3);
            sarray.setDisplayType("E");
            sarray.buildArrayParams(packtype);
            line = buildNewDataFormatLine(CMSQSECT, CMSQSECN, "spectra","Sa",
                                                            V3_DAMPING_VALUES[s]);
            sarray.setFormatLine(line + sarray.getNumberFormat());
            V3Data.add(sarray);
        }
        
        //Get the current processing time
        String val = proctime.getGMTdateTime();
        //update values in the text header
        this.textHeader[0] = SPECTRA.concat(this.textHeader[0].substring(END_OF_DATATYPE));
        
        //Update the end-of-data line with the new data type
        this.endOfData = eod.append(this.endOfData,0,END_OF_DATA_CHAN)
                            .append(" ")
                            .append(String.valueOf(this.channelNum))
                            .append(" response spectra").toString();
    }
    /**
     * This method creates a new data format line for the V3 component data arrays.
     * It calculates the time based on the number of data values and delta t
     * and gets the physical units from the configuration file.
     * @param units the numeric code for the type of units, COSMOS table 2
     * @param unitsCode code containing the type of units (cm, cm/sec, etc.)
     * @throws SmException from setFormatLine
     */
    public String buildNewDataFormatLine(String units, int unitsCode, String atype,
                                 String stype, double damp) throws SmException {
        StringBuilder line = new StringBuilder();
        String datType;
        String outline;
        if (atype.equalsIgnoreCase("periods")) {
            datType = " periods at which spectra computed,      units=";
            outline = line.append(String.format("%1$4s", String.valueOf(NUM_T_PERIODS)))
                        .append(datType)
                        .append(String.format("%1$7s",String.valueOf(units)))
                        .append(String.format("(%1$02d),Format=",unitsCode))
                        .toString();
        } else if (atype.equalsIgnoreCase("fft")) {
            datType = " values of approx Fourier spectrum,      units=";
            outline = line.append(String.format("%1$4s", String.valueOf(NUM_T_PERIODS)))
                        .append(datType)
                        .append(String.format("%1$7s",String.valueOf(units)))
                        .append(String.format("(%1$02d),Format=",unitsCode))
                        .toString();
        } else {
            outline= line.append(String.format("%1$4s values of %2$2s",
                                            String.valueOf(NUM_T_PERIODS),stype))
                        .append(String.format(" for Damping =%1$4s",String.valueOf(damp)))
                        .append(",         units=")
                        .append(String.format("%1$7s",String.valueOf(units)))
                        .append(String.format("(%1$02d),Format=",unitsCode))
                        .toString();
        }
        return outline;
    }
//    public String getDataFormatLine() {
//        return V3Data.getFormatLine();
//    }
    @Override
    public String[] VrecToText() {
        //add up the length of the text portions of the component, which are
        //the text header, the comments, and the end-of-data line.
        int totalLength;
        int currentLength = 0;
        int textLength = this.textHeader.length + this.comments.length + 2;
        
        //get the header and data arrays as text
        String[] intHeaderText = this.intHeader.numberSectionToText();
        String[] realHeaderText = this.realHeader.numberSectionToText();
        
        String[] varr;
        int datasize = 0;
        ArrayList<String[]> V3DataText = new ArrayList<>();
        for (VRealArray each :  V3Data) {
            varr = each.numberSectionToText();
            V3DataText.add(varr);
            datasize += varr.length;
        }
        //add the array lengths to the text lengths to get the total and declare
        //an array of this length, then build it by combining all the component
        //pieces into a text version of the component.
        totalLength = textLength + intHeaderText.length + realHeaderText.length + 
                                                        datasize;
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
        outText[currentLength] = V3DampingValues;
        currentLength++;
        
        for (String[] each : V3DataText) {
            System.arraycopy(each, 0, outText, currentLength, each.length);
            currentLength += each.length;
        }
        V3DataText.clear();
        outText[totalLength-1] = this.endOfData;
        return outText;
    }
}
