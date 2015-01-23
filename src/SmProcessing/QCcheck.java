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

package SmProcessing;

import static SmConstants.VFileConstants.DEFAULT_QA_INITIAL_VELOCITY;
import static SmConstants.VFileConstants.DEFAULT_QA_RESIDUAL_DISPLACE;
import static SmConstants.VFileConstants.DEFAULT_QA_RESIDUAL_VELOCITY;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.QC_INITIAL_VELOCITY;
import static SmUtilities.SmConfigConstants.QC_RESIDUAL_DISPLACE;
import static SmUtilities.SmConfigConstants.QC_RESIDUAL_VELOCITY;

/**
 *
 * @author jmjones
 */
public class QCcheck {
    private double lowcut;
    private int eindex;
    private double qcvelinit;
    private double qcvelres;
    private double qcdisres;
    
    private int window;
    private double velstart;
    private double velend;
    private double disend;
    
    public QCcheck() {
    }
    public boolean validateQCvalues() {
        ConfigReader config = ConfigReader.INSTANCE;
        try {
            String qainitvel = config.getConfigValue(QC_INITIAL_VELOCITY);
            this.qcvelinit = (qainitvel == null) ? DEFAULT_QA_INITIAL_VELOCITY : 
                                                    Double.parseDouble(qainitvel);

            String qaendvel = config.getConfigValue(QC_RESIDUAL_VELOCITY);
            this.qcvelres = (qaendvel == null) ? DEFAULT_QA_RESIDUAL_VELOCITY : 
                                                    Double.parseDouble(qaendvel);

            String qaenddis = config.getConfigValue(QC_RESIDUAL_DISPLACE);
            this.qcdisres = (qaenddis == null) ? DEFAULT_QA_RESIDUAL_DISPLACE : 
                                                    Double.parseDouble(qaenddis);
        } catch (NumberFormatException err) {
            return false;
        }
        return true;
    }
    public int findWindow(double lowcutoff, double samprate, int eventIndex) {
        this.lowcut = lowcutoff;
        this.eindex = eventIndex;
        
        int lclength = (int)(Math.round(1.0 / lowcut) * samprate);
        window = Math.max(eindex, lclength);
        return window;
    }
    public boolean qcVelocity(double[] velocity) {
        boolean pass = false;
        int vellen = velocity.length;
        int velwindowstart;
        int velwindowend;
        if (window > 0) {
            velwindowstart = ArrayOps.findZeroCrossing(velocity, window, 0);
            velstart = (velwindowstart > 0) ? 
                    ArrayOps.findSubsetMean(velocity, 0, velwindowstart) : 
                                                                    velocity[0];
            velwindowend = ArrayOps.findZeroCrossing(velocity, vellen-window-1, vellen-1);
            velend = (velwindowend > 0) ? 
                    ArrayOps.findSubsetMean(velocity, velwindowend, vellen) : 
                                                             velocity[vellen-1];
        } else {
            velstart = velocity[0];
            velend = velocity[vellen-1];
        }
        if ((Math.abs(velstart) <= qcvelinit) && (Math.abs(velend) <= qcvelres)){
            pass = true;
        }
        return pass;
    }
    public boolean qcDisplacement(double[] displace) {
        boolean pass = false;
        int dislen = displace.length;
        int diswindowend;
        if (window > 0) {
            diswindowend = ArrayOps.findZeroCrossing(displace, dislen-window-1, dislen-1);
            disend = (diswindowend > 0) ? 
                    ArrayOps.findSubsetMean(displace, diswindowend, dislen) : 
                                                            displace[dislen-1];
        } else {
           disend = displace[dislen-1];
        }
        if ((Math.abs(disend) <= qcdisres)) {
            pass = true;
        }
        return pass;        
    }
    public double getInitialVelocity() {
        return velstart;
    }
    public double getResidualVelocity() {
        return velend;
    }
    public double getResidualDisplacement() {
        return disend;
    }
    public double getInitVelocityQCval() {
        return qcvelinit;
    }
    public double getResVelocityQCval() {
        return qcvelres;
    }
    public double getResDisplaceQCval() {
        return qcdisres;
    }
    public int getQCWindow() {
        return window;
    }
}
