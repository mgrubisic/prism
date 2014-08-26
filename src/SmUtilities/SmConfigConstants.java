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

package SmUtilities;

/**
 * This class holds the constants to access the configuration file values.
 * @author jmjones
 */
public class SmConfigConstants {
    public static final String PROC_AGENCY_CODE = "PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyCode";
    public static final String PROC_AGENCY_NAME = "PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyFullName";
    public static final String PROC_AGENCY_ABBREV = "PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyAbbreviation";
    public static final String PROC_AGENCY_IRIS = "PRISM/ProcessingAgency/StrongMotionNetworkCode/AgencyIRISCode";
    
    public static final String OUT_FILE_FORMAT = "PRISM/OutputFileFormat";
    public static final String OUT_ARRAY_FORMAT = "PRISM/OutputArrayFormat";
    
    public static final String DATA_UNITS_CODE = "PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitCode";
    public static final String DATA_UNITS_NAME = "PRISM/DataUnitsForCountConversion/DataUnitCodes/DataUnitName";
    
    public static final String BASELINE_CORRECTION_TYPE = "PRISM/BaselineCorrectionType";
    
    public static final String QA_INITIAL_VELOCITY = "QAparameters/InitialVelocity";
    public static final String QA_RESIDUAL_VELOCITY = "QAparameters/ResidualVelocity";
    public static final String QA_RESIDUAL_DISPLACE = "QAparameters/ResidualDisplacement";
    
    public static final String STATION_FILTER_TABLE = "PRISM/StationFilterTable";
    
    public static final String BP_FILTER_ORDER = "PRISM/BandPassFilterParameters/BandPassFilterOrder";
    public static final String BP_TAPER_LENGTH = "PRISM/BandPassFilterParameters/BandPassTaperLength";
    public static final String BP_FILTER_CUTOFFHIGH = "PRISM/BandPassFilterParameters/BandPassFilterCutoff/CutoffHigh";
    public static final String BP_FILTER_CUTOFFLOW = "PRISM/BandPassFilterParameters/BandPassFilterCutoff/CutoffLow";
    
    public static final String EVENT_ONSET_BUFFER = "PRISM/EventOnsetBufferAmount";
}
