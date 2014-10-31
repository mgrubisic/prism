/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
    private String inFolder;
    private ArrayList<COSMOScontentFormat> V0List;
    private ArrayList<COSMOScontentFormat> V1List;
    private ArrayList<COSMOScontentFormat> V2List;
    private ArrayList<COSMOScontentFormat> V3List;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    private File stationDir;
    private File eventDir;
    private File logDir;
    private ArrayList<String> loglist;
    
    //The products package assumes a file name structure for the V0 files.
    //
    public SmProduct(final String inFolder, final String newFolder) {
        this.inFolder = inFolder;
        this.V0List = new ArrayList<>();
        this.V1List = new ArrayList<>();
        this.V2List = new ArrayList<>();
        this.V3List = new ArrayList<>();
        this.outFolder = newFolder;
        this.stationDir = new File( newFolder );
        this.eventDir = new File( newFolder );
        this.logDir = new File( newFolder );
        this.loglist = new ArrayList<>();        
//        this.loglist.add(inFileName.toString());       
    }
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
    public void setDirectories(String rcid, String scnlauth, String eventMarker, 
                                                            V2Status V2result) {
        String event;
        String station;
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        boolean validid = validateRcrdId( rcid );
        String[] sections = rcid.split("\\.");
        
        if ((!scnlauth.isEmpty()) && (validid)) {
            event = sb.append(sections[0]).append(".").append(sections[1]).toString(); 
        } else {
            event = "Orphan";
        }
        if (V2result == V2Status.GOOD) {
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
//        System.out.println("success: " + passedQA);
//        System.out.println("eventdir: " + this.eventDir.toString());
//        System.out.println("stationdir: " + this.stationDir.toString());
        
    }
    public String[] writeOutProducts() throws IOException {
        TextFileWriter textout;
        Iterator iter;
        Path outName = null;
        String[] contents;
//        System.out.println("productlist length: " + this.V1List.size());
        iter = this.V0List.iterator();
        while (iter.hasNext()) {
            V0Component rec0 = (V0Component)iter.next();
            contents = rec0.VrecToText();
            outName = buildFilename(rec0.getStationDir(), rec0.getFileName(),"V0",
                                                         rec0.getChannel(), "");
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
        }
        this.V0List.clear();
        
        iter = this.V1List.iterator();
        while (iter.hasNext()) {
            V1Component rec1 = (V1Component)iter.next();
            contents = rec1.VrecToText();
            outName = buildFilename(rec1.getStationDir(), rec1.getFileName(),"V1",
                                                         rec1.getChannel(), "");
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
        }
        this.V1List.clear();
        
        iter = this.V2List.iterator();
        while (iter.hasNext()) {
            V2Component rec2 = (V2Component)iter.next();
            contents = rec2.VrecToText();
            outName = buildFilename(rec2.getStationDir(), rec2.getFileName(),"V2",
                                                         rec2.getChannel(), "acc");
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
            
            //get velocity and write to file
            if (iter.hasNext()) {
                rec2 = (V2Component)iter.next();
                outName = buildFilename(rec2.getStationDir(), rec2.getFileName(),"V2",
                                                         rec2.getChannel(), "vel");
                contents = rec2.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            }
            //get displacement and write to file
            if (iter.hasNext()) {
                rec2 = (V2Component)iter.next();
                outName = buildFilename(rec2.getStationDir(), rec2.getFileName(),"V2",
                                                         rec2.getChannel(), "dis");
                contents = rec2.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.writeOutToFile();
                this.loglist.add(outName.toString());
            }
        }
        this.V2List.clear();
        
        iter = this.V3List.iterator();
        while (iter.hasNext()) {
            V3Component rec3 = (V3Component)iter.next();
            outName = buildFilename(rec3.getStationDir(), rec3.getFileName(),"V3",
                                                         rec3.getChannel(), "");
            contents = rec3.VrecToText();
            textout = new TextFileWriter(outName, contents);
            textout.writeOutToFile();
            this.loglist.add(outName.toString());
        }
        this.V3List.clear();
        
        String[] outlist = new String[loglist.size()];
        outlist = loglist.toArray(outlist);
        return outlist;
    }
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
//        System.out.println("fileExtension " + fileExtension);
//        System.out.println("name: " + name);
//        System.out.println("stationDir: " + this.stationDir);
        Path outName = Paths.get(outloc.toString(),fileExtension, name);
        return outName;
    }
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
    public void deleteV0AfterProcessing(File source) throws IOException {
//        System.out.println("filename: " + this.fileName.getName());
//        Files.deleteIfExists(source);
    }
}