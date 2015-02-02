/*******************************************************************************
 * Name: Java class Prism.java (program main)
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SMCOSMOScontrol;

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V0Component;
import static SmConstants.VFileConstants.CORACC;
import static SmConstants.VFileConstants.RAWACC;
import static SmConstants.VFileConstants.UNCORACC;
import SmException.FormatException;
import SmException.SmException;
import SmUtilities.ConfigReader;
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
 * the waveform processing algorithms to create the other data products, and then
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
//                    System.out.println("prism: infile " + each);
                    //This if stmt is used to test v2 reads only
//                    if (each.toString().endsWith("2")) {
//                        recordCount = smc.smqueue.parseVFile( CORACC );
//                        continue;
//                    }
                    // parse the raw acceleration file into channel record(s)
                    recordCount = smc.smqueue.parseVFile( RAWACC );
                    
                    //next is to process the records, then write out results
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
    //Get the list of .V* files in the input folder and return as an array of
    //file names.  Flag if the input folder doesn't contain any files.
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
    public String getInFolder()
    {
        return this.inFolder;
    }
    
    public String getOutFolder()
    {
        return this.outFolder;
    }
    
    public String getConfigFile()
    {
        return this.configFile;
    }
    
    public File[] getInVList()
    {
        return this.inVList;
    }
    
    public SmQueue getSmqueue()
    {
        return this.smqueue;
    }
        
    public void setInVList(File[] inVList)
    {
        this.inVList = inVList;
    }
}
