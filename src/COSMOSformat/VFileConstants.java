/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

/**
 *
 * @author jmjones
 */
public final class VFileConstants {
    public static final int HEADLINE_1_LENGTH = 59;
    public static final int DT_START = 0;
    public static final int DT_END = 24;
    public static final int COSVER_START = 35;
    public static final int COSVER_END = 40;
    public static final int NUM_HEAD_START = 46;
    public static final int NUM_HEAD_END = 48;
    
    public static final int HEADLINE_2_LENGTH = 80;
    public static final int EQINFO_START = 0;
    public static final int EQINFO_END = 79;
    public static final int NODATA_LINE = 12;
    
    public static final String RAWACC = "Raw acceleration counts";
    public static final String UNCORACC = "Uncorrected acceleration";
    public static final String CORACC = "Corrected acceleration";
    public static final String VELOCITY = "Velocity data";
    public static final String DISPLACE = "Displacement data";
    public static final String SPECTRA = "Response spectra";
}
