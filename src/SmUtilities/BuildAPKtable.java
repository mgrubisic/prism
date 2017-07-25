/*
 * Copyright (C) 2017 jmjones
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

import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import COSMOSformat.V3Component;
import static SmConstants.VFileConstants.COSMOS_EPICENTRALDIST;
import static SmConstants.VFileConstants.COSMOS_LATITUDE;
import static SmConstants.VFileConstants.COSMOS_LONGITUDE;
import static SmConstants.VFileConstants.COSMOS_STATION_TYPE;
import static SmConstants.VFileConstants.MAX_LINE_LENGTH;
import static SmConstants.VFileConstants.PEAK_VAL;
import static SmConstants.VFileConstants.TO_G_CONVERSION;
import static SmConstants.VFileConstants.VALUE_SA_0P3;
import static SmConstants.VFileConstants.VALUE_SA_1P0;
import static SmConstants.VFileConstants.VALUE_SA_3P0;
import static SmConstants.VFileConstants.V_UNITS_INDEX;
import SmException.SmException;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class BuildAPKtable {
    private final String tablename = "apktable.csv";
    
    public BuildAPKtable() {}
    
    public void buildTable(V3Component v3Component, V1Component v1Component,
            V2Component v2ComponentAcc, V2Component v2ComponentVel, 
            V2Component v2ComponentDis, File csvFolder, String startTime) throws Exception {
        try {
            String[] headerline = {"EVENT","SCNL","STATION_TYPE","STATION_NAME","LAT","LON",
                "EPIC","FAULT","PGAV1","PGAV2","PGV","PGD","SA0P3","SA1P0","SA3P0"};
            ArrayList<String> data = new ArrayList<>();
            //event id
            RecordIDValidator rcdvalid = new RecordIDValidator(v1Component.getRcrdId());
            String event = (rcdvalid.isValidRcrdID()) ? rcdvalid.getEventID() : "not found";
            data.add(event);
            //SCNL code
            String scode = v1Component.getSCNLcode();
            data.add(scode);
            //station type
            int stationtype = v1Component.getIntHeaderValue(COSMOS_STATION_TYPE);
            data.add(String.format("%d", stationtype));
            //station name
            String stationname = v1Component.checkForStationName();
            data.add(stationname.replace(",", " "));
            //station latitude
            double lat = v1Component.getRealHeaderValue(COSMOS_LATITUDE);
            data.add(String.format("%10.5f",lat));
            //station longitude
            double lon = v1Component.getRealHeaderValue(COSMOS_LONGITUDE);
            data.add(String.format("%10.5f",lon));
            //epicentral distance
            double epic = v1Component.getRealHeaderValue(COSMOS_EPICENTRALDIST);
            data.add(String.format("%10.5f",epic));
            //fault
            data.add("( -- )");
            //PGAv1
            int units = v1Component.getIntHeaderValue(V_UNITS_INDEX);
            double pgav1 = v1Component.getRealHeaderValue(PEAK_VAL);
            pgav1 = (units == 2) ? pgav1 : (pgav1 * TO_G_CONVERSION);
            data.add(String.format("%15.6f",pgav1));
            //PGSv2
            
            units = v2ComponentAcc.getIntHeaderValue(V_UNITS_INDEX);
            double pgav2 = v2ComponentAcc.getRealHeaderValue(PEAK_VAL);
            pgav2 = (units == 2) ? pgav2 : (pgav2 * TO_G_CONVERSION);
            data.add(String.format("%15.6f",pgav2));
            //PGV
            double pgv = v2ComponentVel.getRealHeaderValue(PEAK_VAL);
            data.add(String.format("%15.6f",pgv));
            //PGD
            double pgd = v2ComponentDis.getRealHeaderValue(PEAK_VAL);
            data.add(String.format("%15.6f",pgd));
            //Sa at period 0.3 sec, 1 sec, 3 sec
            data.add(String.format("%15.6f",v3Component.getRealHeaderValue(VALUE_SA_0P3)));
            data.add(String.format("%15.6f",v3Component.getRealHeaderValue(VALUE_SA_1P0)));
            data.add(String.format("%15.6f",v3Component.getRealHeaderValue(VALUE_SA_3P0)));

            CSVFileWriter csvwrite = new CSVFileWriter( csvFolder );
            csvwrite.writeToCSV(data,headerline,tablename, startTime);
            data.clear();
        }
        catch (SmException ex) {
            throw new Exception("Apktable build Error:\n" + ex.getMessage());
        }
    }
}
