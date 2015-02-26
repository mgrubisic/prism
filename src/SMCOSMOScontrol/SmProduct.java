/*******************************************************************************
 * Name: Java class SmProduct.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SMCOSMOScontrol;

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V0Component;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import COSMOSformat.V3Component;
import static SmConstants.VFileConstants.MAX_LINE_LENGTH;
import SmConstants.VFileConstants.V2Status;
import SmUtilities.TextFileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the writing of products out to their files and the creation
 * of the directory structure.  This class assumes a file name structure for the V0 files.
 * @author jmjones
 */
public class SmProduct {
    private String outFolder;
    private String inFolder;
    private ArrayList<COSMOScontentFormat> V0List;
    private ArrayList<COSMOScontentFormat> V1List;
    private ArrayList<COSMOScontentFormat> V2List;
    private ArrayList<COSMOScontentFormat> V3List;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    private File stationDir;
    private File eventDir;
    private File finalDir;
    private File logDir;
    private ArrayList<String> loglist;
    
    /**
     * Constructor for the product class.
     * @param inFolder the input folder
     * @param newFolder the output folder, top level
     */
    public SmProduct(final String inFolder, final String newFolder) {
        this.inFolder = inFolder;
        this.V0List = new ArrayList<>();
        this.V1List = new ArrayList<>();
        this.V2List = new ArrayList<>();
        this.V3List = new ArrayList<>();
        this.outFolder = newFolder;
        this.stationDir = new File( newFolder );
        this.finalDir = new File( newFolder );
        this.eventDir = new File( newFolder );
        this.logDir = new File( newFolder );
        this.loglist = new ArrayList<>(); 
//        this.loglist.add(inFileName.toString());       
    }
    /**
     * Method to add a product to the product queue for later writing out to a file.
     * @param newprod the COSMOS object for the queue
     * @param ext the type of object, such as V0, V1, etc.
     */
    public void addProduct(COSMOScontentFormat newprod, String ext ) {
        if (ext.equalsIgnoreCase("V0")) {
            V0Component rec = (V0Component)newprod;
            rec.setStationDir(this.stationDir);
            this.V0List.add(rec);
        } else if (ext.equalsIgnoreCase("V1")) {
            V1Component rec = (V1Component)newprod;
            rec.setStationDir(this.stationDir);
            this.V1List.add(rec);
        } else if (ext.equalsIgnoreCase("V2")) {
            V2Component rec = (V2Component)newprod;
            rec.setStationDir(this.stationDir);
            this.V2List.add(rec);
        } else {
            V3Component rec = (V3Component)newprod;
            rec.setStationDir(this.stationDir);
            this.V3List.add(rec);
        }
    }
    /**
     * Method to set up the directory structure within the output directory.
     * @param rcid extracted from the input file, this defines the folder structure
     * according to the SCNL code
     * @param scnlauth must be present in the V0 for the standard folder structure
     * to be put in place.
     * @param eventMarker event time pulled from the file header in case of Orphan,
     * this event time will be used in place of the station name
     * @param V2result flag for the creation of a Trouble folder
     */
    public void setDirectories(String rcid, String scnlauth, String eventMarker, 
                                                            V2Status V2result) {
        String event;
        String station;
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        boolean validid = validateRcrdId( rcid );
        String[] sections = rcid.split("\\.");
        
        //determine the event and station names
        if ((!scnlauth.isEmpty()) && (validid)) {
            event = sb.append(sections[0]).append(".").append(sections[1]).toString(); 
        } else {
            event = "Orphan";
        }
        if (event.equals("Orphan")) {
            station = eventMarker;
        } else {
            sb = new StringBuilder(MAX_LINE_LENGTH);
            station = sb.append(sections[2]).append(".").append(sections[3]).toString();
        }
        //create the log folder
        File logId = Paths.get(this.outFolder, "Logs").toFile();
        if (!logId.isDirectory()) {
            logId.mkdir();
        }
        this.logDir = logId;
        
        //create the event and station directories
        File eventId = Paths.get(this.outFolder, event).toFile();
        if (!eventId.isDirectory()) {
            eventId.mkdir();
        }
        this.eventDir = eventId;
        
        File stationId = Paths.get(eventId.toString(), station).toFile();
        if (!stationId.isDirectory()) {
            stationId.mkdir();
        }
        
        if (V2result != V2Status.GOOD) {
            stationId = Paths.get(eventId.toString(), station, "Trouble").toFile();
            if (!stationId.isDirectory()) {
                stationId.mkdir();
            }
        }
        this.stationDir = stationId;
        
        //Create the V0 - V3 folders
        File V0Id = Paths.get(stationId.toString(), "V0").toFile();
        if (!V0Id.isDirectory()) {
            V0Id.mkdir();
        }
        File V1Id = Paths.get(stationId.toString(), "V1").toFile();
        if (!V1Id.isDirectory()) {
            V1Id.mkdir();
        }
        if (V2result != V2Status.NOEVENT) {
            File V2Id = Paths.get(stationId.toString(), "V2").toFile();
            if (!V2Id.isDirectory()) {
                V2Id.mkdir();
            }
        }
        if (V2result == V2Status.GOOD) {  //V3 processing only occurs on valid V2 products
            File V3Id = Paths.get(stationId.toString(), "V3").toFile();
            if (!V3Id.isDirectory()) {
                V3Id.mkdir();
            }        
        }
    }
    /**
     * Writes out each of the products to the respective folder, first creating
     * the full path name, then writing out the text file, then adding the name
     * of the file to the log list.
     * @return the text file list of file names (log list)
     * @throws IOException if unable to write out the file
     */
    public String[] writeOutProducts() throws IOException {
        TextFileWriter textout;
        Iterator iter;
        Path outName = null;
        String[] contents;
        String chanvalue;
        //write out V0s
        iter = this.V0List.iterator();
        while (iter.hasNext()) {
            V0Component rec0 = (V0Component)iter.next();
            contents = rec0.VrecToText();
            chanvalue = (V0List.size() > 1) ? rec0.getChannel() : "";
            outName = buildFilename(rec0.getStationDir(), rec0.getFileName(),"V0",
                                                         chanvalue, "");
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
        }
        this.V0List.clear();
        //write out V1s
        iter = this.V1List.iterator();
        while (iter.hasNext()) {
            V1Component rec1 = (V1Component)iter.next();
            contents = rec1.VrecToText();
            chanvalue = (V1List.size() > 1) ? rec1.getChannel() : "";
            outName = buildFilename(rec1.getStationDir(), rec1.getFileName(),"V1",
                                                         chanvalue, "");
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
        }
        this.V1List.clear();
        //write out V2s
        iter = this.V2List.iterator();
        while (iter.hasNext()) {
            V2Component rec2 = (V2Component)iter.next();
            contents = rec2.VrecToText();
            chanvalue = (V2List.size() > 3) ? rec2.getChannel() : "";
            outName = buildFilename(rec2.getStationDir(), rec2.getFileName(),"V2",
                                                         chanvalue, "acc");
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
            
            //get velocity and write to file
            if (iter.hasNext()) {
                rec2 = (V2Component)iter.next();
                outName = buildFilename(rec2.getStationDir(), rec2.getFileName(),"V2",
                                                         chanvalue, "vel");
                contents = rec2.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            }
            //get displacement and write to file
            if (iter.hasNext()) {
                rec2 = (V2Component)iter.next();
                outName = buildFilename(rec2.getStationDir(), rec2.getFileName(),"V2",
                                                         chanvalue, "dis");
                contents = rec2.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            }
        }
        this.V2List.clear();
        //write out V3s
        iter = this.V3List.iterator();
        while (iter.hasNext()) {
            V3Component rec3 = (V3Component)iter.next();
            chanvalue = (V3List.size() > 1) ? rec3.getChannel() : "";
            outName = buildFilename(rec3.getStationDir(), rec3.getFileName(),"V3",
                                                         chanvalue, "");
            contents = rec3.VrecToText();
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
        }
        this.V3List.clear();
        
        String[] outlist = new String[loglist.size()];
        outlist = loglist.toArray(outlist);
        loglist.clear();
        return outlist;
    }
    /**
     * Builds the output filename from a folder path, file name, file extension,
     * channel number, and V2 processing type extension
     * @param outloc the output folder for this file
     * @param fileName the file name
     * @param fileExtension the extension of V1, V2, etc.
     * @param channel the channel id if needed in the filename
     * @param ext an extension for V2s, such as 'acc', 'vel', or 'dis'
     * @return the full file path
     */
    public Path buildFilename(File outloc, String fileName, String fileExtension, 
                                                    String channel, String ext) {
        Path pathname = Paths.get(fileName);
        String name = pathname.getFileName().toString();
        String getExtensionRegex = "\\.(?i)V\\d(?i)c??$";
        Pattern extension = Pattern.compile( getExtensionRegex );
        Matcher matcher = extension.matcher(name);
        StringBuilder sb = new StringBuilder();
        if (!channel.isEmpty()) {
            sb.append(".");
            sb.append(channel);
        }
        if (!ext.isEmpty()) {
            sb.append(".");
            sb.append(ext);
        }
        sb.append(".");
        sb.append(fileExtension);
        name = matcher.replaceFirst(sb.toString());
        Path outName = Paths.get(outloc.toString(),fileExtension, name);
        return outName;
    }
    /**
     * Validates the format of the record id before its use in building directory names
     * @param id the record id
     * @return true if record id has a recognized format, false if it doesn't
     */
    public boolean validateRcrdId( String id ) {
        StringBuilder sb = new StringBuilder(80);
        String pat = sb.append("^")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("(\\w+)")
                       .append("(\\.)")
                       .append("[\\w-]+")
                       .append("$")
                       .toString();
        Pattern officialname = Pattern.compile(pat);
        Matcher m = officialname.matcher(id);
        return m.matches();
    }
    /**
     * Removes the input file from the input directory, if it exists
     * @param source the input file
     * @throws IOException if unable to delete the file
     */
    public void deleteV0AfterProcessing(File source) throws IOException {
//        System.out.println("filename: " + this.fileName.getName());
//        Files.deleteIfExists(source);
    }
    /**
     * Builds a trouble log from the list of all log files.  If there are no
     * files going to trouble folders, the list returned has size 0.
     * @param inlog the list of all output files
     * @return a list of output files going to trouble folders, or a list of
     * length 0 if no trouble files found.
     */
    public String[] buildTroubleLog(String[] inlog) {
        ArrayList<String> trouble = new ArrayList<>();
        String[] outlist = new String[0];
        for (String name : inlog) {
            if (name.contains("Trouble")) {
                trouble.add(name);
            }
        }
        if (trouble.size() > 0) {
            outlist = new String[trouble.size()];
            outlist = trouble.toArray(outlist);
            trouble.clear();
        }
        return outlist;
    }
}