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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class CSVFileWriter {
    private File csvfolder;
    
    public CSVFileWriter( File infolder ) {
        csvfolder = infolder;
        if (!csvfolder.isDirectory()) {
            csvfolder.mkdir();
        }
    }
    /**
     * Writes the list out as a CSV file, with the
     * first line containing the column names
     * @param msg a list of the parameters for one record
     * @param headerline the column names to write out the first time
     * @param name the name of the file
     * @throws IOException if unable to write to the file
     */
    public void writeToCSV( ArrayList<String> msg, String[] headerline, 
                            String name, String time ) throws IOException {
        String[] values;
        String startTime = time.replace("-","_").replace(" ", "_").replace(":","_");
        StringBuilder sbheader = new StringBuilder();
        StringBuilder sbname = new StringBuilder();
        StringBuilder sbmsg = new StringBuilder();
        for (String each : msg) {
            sbmsg.append(each).append(",");
        }
        sbmsg.replace(sbmsg.length()-1, sbmsg.length(), "");
        String[] segments = name.split("\\.");
        sbname.append(segments[0]).append("_").append(startTime).append(".").append(segments[1]);
        
        Path outfile = Paths.get(csvfolder.toString(), sbname.toString());
        if (!outfile.toFile().exists()) {
            values = new String[2];
            for (String each : headerline) {
                sbheader.append(each).append(",");
            }
            sbheader.replace(sbheader.length()-1, sbheader.length(), "");
            values[0] = sbheader.toString();
            values[1] = sbmsg.toString();
        } else {
            values = new String[1];
            values[0] = sbmsg.toString();
        }
        TextFileWriter textfile = new TextFileWriter( outfile, values);
        textfile.appendToFile();
    }
}
