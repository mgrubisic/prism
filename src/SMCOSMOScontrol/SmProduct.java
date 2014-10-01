/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import COSMOSformat.V3Component;
import static SmConstants.VFileConstants.MAX_LINE_LENGTH;
import SmConstants.VFileConstants.OutputStyle;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.OUT_FILE_FORMAT;
import SmUtilities.TextFileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jmjones
 */
public class SmProduct {
    private String outFolder;
    private File fileName;
    private ArrayList<COSMOScontentFormat> V1List;
    private ArrayList<COSMOScontentFormat> V2List;
    private ArrayList<COSMOScontentFormat> V3List;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    private OutputStyle bundleFlag;
    private File stationDir; //value is set in the setDirectories method
    private File eventDir;  //value is set in the setDirectories method
    private File logDir;  //value is set in the setDirectories method
    private int numInInputList;
    private ArrayList<String> loglist;
    
    //The products package assumes a file name structure for the V0 files.
    //
    public SmProduct(final File inFileName, final String newFolder) {
        this.fileName = inFileName;
        this.V1List = new ArrayList<>();
        this.V2List = new ArrayList<>();
        this.V3List = new ArrayList<>();
        this.outFolder = newFolder;
        this.numInInputList= 1;
        this.stationDir = new File( newFolder );
        this.eventDir = new File( newFolder );
        this.logDir = new File( newFolder );
        this.loglist = new ArrayList<>();
        
        this.loglist.add(inFileName.toString());
        
        ConfigReader config = ConfigReader.INSTANCE;
        String outStyle = config.getConfigValue(OUT_FILE_FORMAT);
        if ((outStyle == null) || (outStyle.contentEquals("SingleChannel"))) {
            this.bundleFlag = OutputStyle.SINGLE_CHANNEL;
        } else {
            this.bundleFlag = OutputStyle.BUNDLED;
        }
    }
    public void addProduct(COSMOScontentFormat newprod, String ext ) {
        if (ext.equalsIgnoreCase("V1")) {
            this.V1List.add((V1Component)newprod);
        } else if (ext.equalsIgnoreCase("V2")) {
            this.V2List.add((V2Component)newprod);
        } else {
            this.V3List.add((V3Component)newprod);
        }
    }
    public void setDirectories(String eventMarker, boolean passedQA, int numlist) {
        String event;
        String station;
        this.numInInputList = numlist;
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        String name = this.fileName.getName();        
        boolean validname = validateFileName( name );
        String[] sections = name.split("\\.");
        
        if (validname) {
            event = sb.append(sections[0]).append(".").append(sections[1]).toString(); 
        } else {
            event = "Orphan";
        }
        if (passedQA) {
            if (event.equals("Orphan")) {
                station = eventMarker;
            } else {
                sb = new StringBuilder(MAX_LINE_LENGTH);
                station = sb.append(sections[2]).append(".").append(sections[3]).toString();
            }
        } else {
               station = "Trouble";         
        }
        
        File logId = Paths.get(this.outFolder, "Logs").toFile();
        if (!logId.isDirectory()) {
            logId.mkdir();
        }
        this.logDir = logId;
        
        File eventId = Paths.get(this.outFolder, event).toFile();
        if (!eventId.isDirectory()) {
            eventId.mkdir();
        }
        this.eventDir = eventId;
        
        File stationId = Paths.get(eventId.toString(), station).toFile();
        if (!stationId.isDirectory()) {
            stationId.mkdir();
        }
        this.stationDir = stationId;
        
//        System.out.println("event: " + this.eventDir + " and station: " + this.stationDir);
        File V0Id = Paths.get(stationId.toString(), "V0").toFile();
        if (!V0Id.isDirectory()) {
            V0Id.mkdir();
        }
        File V1Id = Paths.get(stationId.toString(), "V1").toFile();
        if (!V1Id.isDirectory()) {
            V1Id.mkdir();
        }
        File V2Id = Paths.get(stationId.toString(), "V2").toFile();
        if (!V2Id.isDirectory()) {
            V2Id.mkdir();
        }
        if (passedQA) {  //V3 processing only occurs on valid V2 products
            File V3Id = Paths.get(stationId.toString(), "V3").toFile();
            if (!V3Id.isDirectory()) {
                V3Id.mkdir();
            }        
        }
    }
    
    public String[] writeOutProducts() throws IOException {
        TextFileWriter textout;
        Iterator iter;
        Path outName = null;
        String[] contents;
        boolean added;
//        System.out.println("productlist length: " + this.V1List.size());
        
        iter = this.V1List.iterator();
        added = false;
        while (iter.hasNext()) {
            V1Component rec1 = (V1Component)iter.next();
            if (this.bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                outName = buildFilename(this.bundleFlag, "V1",rec1.getChannelNum());
            } else {
                outName = buildFilename(this.bundleFlag, "V1", 0);
            }
            contents = rec1.VrecToText();
            textout = new TextFileWriter(outName, contents);
            if (this.bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            } else {
                textout.appendToFile();
                if (!added) {
                    this.loglist.add(outName.toString());
                    added = true;
                }
            }
        }
        this.V1List.clear();
        
        iter = this.V2List.iterator();
        added = false;
        while (iter.hasNext()) {
            V2Component rec2 = (V2Component)iter.next();
            if (this.bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                outName = buildFilename(this.bundleFlag, "V2",rec2.getChannelNum());
            } else {
                outName = buildFilename(this.bundleFlag, "V2", 0);
            }
            contents = rec2.VrecToText();
            textout = new TextFileWriter(outName, contents);
            if (this.bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            } else {
                textout.appendToFile();
                if (!added) {
                    this.loglist.add(outName.toString());
                    added = true;
                }
            }
            //get velocity and append to file
            if (iter.hasNext()) {
                rec2 = (V2Component)iter.next();
                contents = rec2.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.appendToFile();
            }
            //get displacement and append
            if (iter.hasNext()) {
                rec2 = (V2Component)iter.next();
                contents = rec2.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.appendToFile();
            }
        }
        this.V2List.clear();
        
        iter = this.V3List.iterator();
        added = false;
        while (iter.hasNext()) {
            V3Component rec3 = (V3Component)iter.next();
            if (this.bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                outName = buildFilename(this.bundleFlag, "V3",rec3.getChannelNum());
            } else {
                outName = buildFilename(this.bundleFlag, "V3", 0);
            }
            contents = rec3.VrecToText();
            textout = new TextFileWriter(outName, contents);
            if (this.bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            } else {
                textout.appendToFile();
                if (!added) {
                    this.loglist.add(outName.toString());
                    added = true;
                }
            }
        }
        this.V3List.clear();
        
        String[] outlist = new String[loglist.size()];
        outlist = loglist.toArray(outlist);
        return outlist;
    }
    public Path buildFilename( OutputStyle aBundleFlag, String fileExtension, int aChannelNum) {
        String startName = this.fileName.getName();
        String name = "";
        String getExtensionRegex = "\\.(?i)V\\d$";
        Pattern extension = Pattern.compile( getExtensionRegex );
        Matcher matcher = extension.matcher(startName);
        if ((aBundleFlag == OutputStyle.SINGLE_CHANNEL) && (this.numInInputList > 1)) {
            String channel = String.valueOf(aChannelNum);
            name = matcher.replaceFirst("." + channel + "." + fileExtension);
        } else {
            name = matcher.replaceFirst("." + fileExtension);
        }
//        System.out.println("fileExtension " + fileExtension);
//        System.out.println("name: " + name);
//        System.out.println("stationDir: " + this.stationDir);
        Path outName = Paths.get(this.stationDir.toString(),fileExtension, name);
        return outName;
    }
    public boolean validateFileName( String name ) {
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
                       .append("([-\\s\\w]*)")
                       .append("(\\.)")
                       .append("\\w+")
                       .append("(\\.)")
                       .append("[vV][0123]")
                       .append("$")
                       .toString();
        Pattern officialname = Pattern.compile(pat);
        Matcher m = officialname.matcher(name);
        return m.matches();
    }
    public void moveV0AfterProcessing() throws IOException {
//        System.out.println("stationDir: " + this.stationDir.toString());
//        System.out.println("filename: " + this.fileName.getName());
        Path target = Paths.get(this.stationDir.toString(),"V0", this.fileName.getName());
        Path source = this.fileName.toPath();
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
//        Files.deleteIfExists(source);
    }
}