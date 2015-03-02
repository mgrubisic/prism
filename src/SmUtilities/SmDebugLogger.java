/*******************************************************************************
 * Name: Java class SmDebugLogger.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmUtilities;

import SmConstants.VFileConstants.LogType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class is a singleton instance of the prism debug logger.  This logger is used
 * to record the processing flow parameters to be used for debugging.
 * This logger is currently set up to create
 * a new logging file with the time appended to the name.
 * @author jmjones
 */
public class SmDebugLogger {
    private Path logfile;
    private Path troublefile;
    private static boolean logReady = false;
    private final String logname = "DebugLog.txt";
    private final String troublename = "TroubleLog.txt";
    public final static SmDebugLogger INSTANCE = new SmDebugLogger();
    private String finalFolder;
    private File logFolder;
    private String startTime;
    /**
     * Constructor for the logger is private as part of the
     * singleton implementation.  Access to the logger is through the INSTANCE 
     * variable:  SmDebugLogger logger = SmDebugLogger.INSTANCE.
     */
    private SmDebugLogger() {
    }
    /**
     * This method initializes the logger and checks if the log folder exists,
     * and if not it creates the log folder in the top level output folder
     * @param outfolder the top level output folder, where the log folder resides
     * @param time appended at the end of the file name
     * @throws IOException if unable to write out a file
     */
    public void initializeLogger( String outfolder, String time ) throws IOException {
        StringBuilder sb = new StringBuilder();
        finalFolder = outfolder;
        startTime = time.replace("-","_").replace(" ", "_").replace(":","_");
        if (!logReady) {
            File logId = Paths.get(outfolder, "Logs").toFile();
            logFolder = logId;
            if (!logId.isDirectory()) {
                logId.mkdir();
            }
            String[] segments = logname.split("\\.");
            sb.append(segments[0]).append("_").append(startTime).append(".").append(segments[1]);
            this.logfile = Paths.get(logId.toString(),sb.toString());

            segments = troublename.split("\\.");
            sb = new StringBuilder();
            sb.append(segments[0]).append("_").append(startTime).append(".").append(segments[1]);
            this.troublefile = Paths.get(logId.toString(),sb.toString());
            logReady = true;
        }
    }
    /**
     * Writes the array of text messages out to the log file, appending to the
     * end of the current file.
     * @param msg the list of messages to be written to the log
     * @param logger the type of log file, either debug or trouble
     * @throws IOException if unable to write to the file
     */
    public void writeToLog( String[] msg, LogType logger ) throws IOException {
        if (logReady) {
            if (logger == LogType.DEBUG) {
                TextFileWriter textfile = new TextFileWriter( logfile, msg);
                textfile.appendToFile();
            } else if (logger == LogType.TROUBLE) {
                TextFileWriter textfile = new TextFileWriter( troublefile, msg);
                textfile.appendToFile();                
            }
        }
    }
    /**
     * This method is used for debug, to write out data arrays as text files
     * for debugging.  The files are written into the topmost folder.
     * @param array the data array to be written out
     * @param name file name to be used for the file
     */
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
    /**
     * Writes the list of V2 processing parameters out as a CSV file, with the
     * first line containing the column names
     * @param msg a list of the parameters for one record
     * @param headerline the column names to write out the first time
     * @param name the name of the file
     * @throws IOException if unable to write to the file
     */
    public void writeToCSV( ArrayList<String> msg, String[] headerline, 
                                            String name ) throws IOException {
        String[] values;
        StringBuilder sbheader = new StringBuilder();
        StringBuilder sbname = new StringBuilder();
        StringBuilder sbmsg = new StringBuilder();
        for (String each : msg) {
            sbmsg.append(each).append(",");
        }
        sbmsg.replace(sbmsg.length()-1, sbmsg.length(), "");
        if (logReady) {
            String[] segments = name.split("\\.");
            sbname.append(segments[0]).append("_").append(startTime).append(".").append(segments[1]);
            Path outfile = Paths.get(logFolder.toString(), sbname.toString());
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
            TextFileWriter csvfile = new TextFileWriter( outfile, values);
            csvfile.appendToFile();
        }
    }
}