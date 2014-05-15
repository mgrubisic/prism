/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.V1Component;
import static COSMOSformat.VFileConstants.RAWACC;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import SmException.SmException;
import SmException.FormatException;
import SmUtilities.ConfigReader;
import SmUtilities.TextFileReader;
import SmUtilities.PrismXMLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Main class for the PRISM strong motion analysis tool.  This is the controller
 * for the batch processing mode, to be started from the command line or
 * automatically by other software.  This class takes an input folder as a param.,
 * reads in *.V0 files in the folder and then processes each file in turn.
 * Processing involves reading in the file and parsing into record(s), running
 * the signal processing algorithms to create the other data products, and then
 * writing out the data in the different formats, either individually or bundled.
 * @author jmjones
 */
public class SmController {
    private final String inFolder;
    private final String outFolder;
    private String configFile;
    private ConfigReader config;
    // data structures for the controller
    private File[] inVList;
    private SmQueue smqueue;
    private SmProduct V1product;
    private SmProduct V2product;
    private SmProduct V3product;

    public SmController (String[] args) throws SmException {
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
    
    public static void main(String[] args) throws SmException, IOException, ParserConfigurationException, SAXException { 
        String config = "";
        int lineCount = 0; 
        int recordCount = 0;
        double NANO_TO_SECOND = 1.0e-9; //for timing tests
        // 
        try {
            SmController smc = new SmController( args );    
            
//            int cores = Runtime.getRuntime().availableProcessors();
//            System.out.println("Number of cores: " + cores);

            //get the list of filenames in the input directory
            try {
                smc.inVList = smc.getFileList( smc.inFolder, "*.[vV]0" );
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
                smc.smqueue = new SmQueue( each );
                smc.V1product = new SmProduct(each, "V1", smc.outFolder);
                //add V2 and V3 products eventually
                try {
//                    long startTime = System.nanoTime();
                    smc.smqueue.readInFile( each );
//                    long readTime = System.nanoTime() - startTime;
                    // parse the raw acceleration file into channel record(s)
//                    startTime = System.nanoTime();
                    recordCount = smc.smqueue.parseVFile( RAWACC );
//                    long parseTime = System.nanoTime() - startTime;
                    //next is to process the records, then write out results
//                    startTime = System.nanoTime();
                    smc.smqueue.processQueueContents(smc.V1product, smc.config);
//                    long processTime = System.nanoTime() - startTime;
//                    startTime = System.nanoTime();

                    //add another method here to handle moving the V0 file
                    //out of the input directory and into its pass/fail location.
                    //create the directories for writing out.  Append directory
                    //names to writeOutProducts call?
                    smc.V1product.writeOutProducts();
//                    long writeTime = System.nanoTime() - startTime;
                    //this is a mess!
//                    System.out.println("+++ read time: " + readTime*NANO_TO_SECOND);
//                    System.out.println("+++ parse time: " + parseTime*NANO_TO_SECOND);
//                    System.out.println("+++ process time: " + processTime*NANO_TO_SECOND);
//                    System.out.println("+++ write time: " + writeTime*NANO_TO_SECOND);
                }
                catch (FormatException | IOException | NumberFormatException err) {
                    //log the exact error msg and move on to the next file
                    //print stack trace to log for numberformatexception?
                    System.out.println("Unable to read/process/write file " + each.toString());
                    System.out.println(err.getMessage());
                }
            }
        } 
        catch (SmException err){
            System.err.println(err.getMessage());
        }
    }
    public void readConfigFile( String filename ) throws SmException {

        config = new ConfigReader();
        try {
            PrismXMLReader xml = new PrismXMLReader();
            xml.readFile(filename, config);
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
                throw new IOException("No " + exten + " files found in directory " + this.inFolder);
            }
            return inList.toArray(finalList);
        }
    }
}
