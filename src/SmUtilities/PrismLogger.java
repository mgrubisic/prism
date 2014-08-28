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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author jmjones
 */
public class PrismLogger {
    private Path logfile;
    private static boolean logReady = false;
    private String logname = "PrismLog.txt";
    public final static PrismLogger INSTANCE = new PrismLogger();
    
    private PrismLogger() {
    }
    public void initializeLogger( String outfolder ) throws IOException {
        if (!logReady) {
            File logId = Paths.get(outfolder, "Logs").toFile();
            if (!logId.isDirectory()) {
                logId.mkdir();
            }
            this.logfile = Paths.get(logId.toString(),"PrismLog.txt");
            logReady = true;
        }
    }
    public void writeToLog( String[] msg ) throws IOException {
        TextFileWriter textfile = new TextFileWriter( logfile, msg);
        textfile.appendToFile();
    }
}
