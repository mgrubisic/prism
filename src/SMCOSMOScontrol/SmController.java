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

/**
 *
 * @author jmjones
 */

// Main class for the PRISM strong motion analysis tool.  This is the controller
// for the batch processing mode, to be started from the command line or
// automatically by other software.  This class takes an input folder as a param.,
// reads in *.V0 files in the folder and then processes each file in turn.
// Processing involves reading in the file and parsing into record(s), running
// the signal processing algorithms to create the other data products, and then
// writing out the data in the different formats, either individually or bundled.
public class SmController {
    // set up temp. values for input parms (also using netbeans args option)
    private static final String CONFIGFILE = "";
    
    private final String inFolder;
    private final String outFolder;
    private final String configFile;
    
    // data structures for the controller
    private File[] inVList;
    private SmQueue smqueue;
    private SmProduct V1product;

    /**
     * @param args the command line arguments
     * @throws SmException.SmException
     */
    //When going through the list of input files, simply report any problems with
    //an individual file and move directly to the next file.  Attempt to process
    //all the files in the list.
    public SmController (String infolder, String outfolder, String config) {
        this.inFolder = infolder;
        this.outFolder = outfolder;
        this.configFile = config;
    }
    
    public static void main(String[] args) throws SmException { 
        String config = "";
        int lineCount = 0; 
        int recordCount = 0;
        // 
        try {
            // make a method checkArgs here
            if (args.length < 2){
                throw new SmException("Input and output directories must provided.");
            }
            if (args.length == 3){
                config = args[2];
            }
            SmController smc = new SmController( args[0], args[1], config );                

            //get the list of filenames in the input directory
            try {
                smc.inVList = smc.getFileList( smc.inFolder, "*.v0" );
            }
            catch (IOException err) {
                throw new SmException("Unable to access V0 file list: " + err.getMessage());
            }
            //get each filename, read in, parse, process, write it out
            for (File each: smc.inVList){
                smc.smqueue = new SmQueue( each );
                smc.V1product = new SmProduct(each, "V1", smc.outFolder);
                try {
                    lineCount = smc.smqueue.readInVFile();

                    // parse the raw acceleration file into channel record(s)
                    recordCount = smc.smqueue.parseVFile( RAWACC );

                    //next is to process the records, then write out results
                    smc.smqueue.processQueueContents(smc.V1product);
                    smc.V1product.writeOutProducts();
                    //this is a mess!
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
    
    //Get the list of .V0C files in the input folder and return as an array of
    //file names.  Flag if the input folder doesn't contain any files.
    private File[] getFileList(String filePath, String exten) throws IOException {
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
