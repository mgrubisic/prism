/*******************************************************************************
 * Name: Java class Prism.java (program main)
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SMCOSMOScontrol;

import static SmConstants.VFileConstants.RAWACC;
import SmException.FormatException;
import SmException.SmException;
import SmUtilities.PrismLogger;
import SmUtilities.PrismXMLReader;
import SmUtilities.SmDebugLogger;
import SmUtilities.SmTimeFormatter;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Main class for the PRISM strong motion analysis tool.  This is the controller
 * for the batch processing mode, to be started from the command line or
 * automatically by other software.  This class takes an input folder as a param.,
 * reads in *.V0 files in the folder and then processes each file in turn.
 * Processing involves reading in the file and parsing into record(s), running
 * the waveform processing algorithms to create the data products, and then
 * writing out the data in the different formats.
 * @author jmjones
 */
public class Prism {
    private final String inFolder;
    private final String outFolder;
    private String configFile;

    // data structures for the controller
    private File[] inVList;
    private SmQueue smqueue;
    private SmProduct Vproduct;
    /**
     * Constructor for PRISM main
     * @param args input arguments
     * @throws SmException error processing a COSMOS file
     */
    public Prism (String[] args) throws SmException {
        this.configFile = "";
        if (args.length > 1) {
            File inDir = new File(args[0]);
            File outDir = new File(args[1]);
            if (inDir.isDirectory() && outDir.isDirectory()) {
                this.inFolder = args[0];
                this.outFolder = args[1]; 
            } else {
                throw new SmException("Input and output directories are not recognized.");
            }
            if (args.length == 3) {
                Path configval = Paths.get(args[2]);
                if (Files.isReadable(configval)) {
                    this.configFile = args[2];
                } else {
                    throw new SmException("Unable to read configuration file.");
                }
            }
        } else {
            throw new SmException("Input and output directories must provided.");
        }
    }
    /**
     * Main method starts the loggers, reads in the configuration file, 
     * reads in all .v0 or .v0c file names in the input folder, processes each file
     * in turn, and then deletes the input file.
     * @param args input string arguments, input folder, output folder, optional
     * configuration file (full path names)
     * @throws SmException if a fatal error occurs during processing
     * @throws IOException if unable to read in the files or file names
     */
    public static void main(String[] args) throws SmException, IOException { 
        String config = "";
        int lineCount = 0; 
        int recordCount = 0;
        double NANO_TO_SECOND = 1.0e-9; //for timing tests
        // 
        try {
            Prism smc = new Prism( args ); 
            
            SmTimeFormatter timer = new SmTimeFormatter();
            PrismLogger log = PrismLogger.INSTANCE;
            SmDebugLogger errlog = SmDebugLogger.INSTANCE;
            String logtime = timer.getGMTdateTime();
            String[] startLog = new String[2];
            startLog[0] = "\n";
            startLog[1] = "Prism Log Entry: " + logtime;
            try {
                log.initializeLogger(smc.outFolder, logtime);
                log.writeToLog(startLog);
                errlog.initializeLogger(smc.outFolder, logtime);
            } 
            catch (IOException err) {
                throw new SmException("Unable to open the log files: " + err.getMessage());
            }
            //get the list of filenames in the input directory
            try {
                smc.inVList = smc.getFileList( smc.inFolder, "*.[vV]0*" );
            }
            catch (IOException err) {
                throw new SmException("Unable to access V0 file list: " + err.getMessage());
            }
            //get the configuration file
            if ( !smc.configFile.isEmpty()  ) {
                smc.readConfigFile( smc.configFile );
            }
            //Get each filename, read in, parse, process, write it out. When  
            //going through the list of input files, report any problems 
            //with an individual file and move directly to the next file.  
            //Attempt to process all the files in the list.
            for (File each: smc.inVList){
                smc.smqueue = new SmQueue( each, logtime );
                smc.Vproduct = new SmProduct(smc.inFolder, smc.outFolder);
                try {
                    smc.smqueue.readInFile( each );
                    
                    // parse the raw acceleration file into channel record(s)
                    recordCount = smc.smqueue.parseVFile( RAWACC );
                    
                    //process the records, then write out results
                    smc.smqueue.processQueueContents(smc.Vproduct);

                    String[] outlist = smc.Vproduct.writeOutProducts();
                    log.writeToLog(outlist);
                    smc.Vproduct.deleteV0AfterProcessing(each);
                }
                catch (FormatException | IOException | SmException err) {
                    String[] logtxt = new String[2];
                    logtxt[0] = "Unable to process file " + each.toString();
                    logtxt[1] = "\t" + err.getMessage();
                    log.writeToLog(logtxt);
                }
            }
        } 
        catch (SmException err){
            System.err.println(err.getMessage());
        }
    }
    /**
     * Reads in the configuration file and parses the xml
     * @param filename the configuration file name
     * @throws SmException if unable to read in or parse the file
     */
    public void readConfigFile( String filename ) throws SmException {

        try {
            PrismXMLReader xml = new PrismXMLReader();
            xml.readFile(filename);
        } catch (ParserConfigurationException | SAXException err) {
            throw new SmException("Unable to parse configuration file " + filename);
        } catch (IOException err) {
            throw new SmException("Unable to read configuration file " + filename);
        }
    }
    /**
     * Gets the list of v0 files in the input folder and returns an array of
     * file names
     * @param filePath directory path to the files
     * @param exten file extension to pick up
     * @return list of file names
     * @throws IOException if the folder is empty
     */
    public File[] getFileList(String filePath, String exten) throws IOException {
        Path dir = Paths.get(filePath);
        ArrayList<File> inList = new ArrayList<>();
        try (DirectoryStream<Path> stream =
                                    Files.newDirectoryStream(dir, exten)) {
            for (Path entry: stream) {
                File name = new File( this.inFolder,entry.getFileName().toString());
                inList.add(name);
            }
            File[] finalList = new File[inList.size()];
            if (inList.isEmpty()) {
                throw new IOException("No files found in directory " + this.inFolder);
            }
            return inList.toArray(finalList);
        }
    }
    /**
     * Gets the input folder name
     * @return input folder name
     */
    public String getInFolder()
    {
        return this.inFolder;
    }
    /**
     * Gets the output folder name
     * @return output folder name
     */
    public String getOutFolder()
    {
        return this.outFolder;
    }
    /**
     * Gets the configuration file name
     * @return the configuration file name
     */
    public String getConfigFile()
    {
        return this.configFile;
    }
    /**
     * Gets the list of input files
     * @return the list of input files
     */
    public File[] getInVList()
    {
        return this.inVList;
    }
    /**
     * Gets the processing queue
     * @return the processing queue
     */
    public SmQueue getSmqueue()
    {
        return this.smqueue;
    }
    /**
     * Sets the input file list to the given list
     * @param inVList list to set as the input file list
     */
    public void setInVList(File[] inVList)
    {
        this.inVList = inVList;
    }
}
