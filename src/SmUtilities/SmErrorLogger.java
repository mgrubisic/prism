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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author jmjones
 */
public class SmErrorLogger {
    private Path logfile;
    private static boolean logReady = false;
    private String logname = "ErrorLog.txt";
    public final static SmErrorLogger INSTANCE = new SmErrorLogger();
    private String finalFolder;

    private SmErrorLogger() {
    }
    public void initializeLogger( String outfolder ) throws IOException {
        finalFolder = outfolder;
        if (!logReady) {
            File logId = Paths.get(outfolder, "Logs").toFile();
            if (!logId.isDirectory()) {
                logId.mkdir();
            }
            this.logfile = Paths.get(logId.toString(),"ErrorLog.txt");
            logReady = true;
        }
    }
    public void writeToLog( String[] msg ) throws IOException {
        if (logReady) {
            TextFileWriter textfile = new TextFileWriter( logfile, msg);
            textfile.appendToFile();
        }
    }
    public void writeOutArray( double[] array, String name) {
        if (logReady) {
            TextFileWriter textout = new TextFileWriter( finalFolder, 
                                                         name, array);
            try {
                textout.writeOutArray();
            } catch (IOException err) {
                //Nothing to do if the error logger has an error.
            }
        }
    }
}
