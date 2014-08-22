/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
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
    private final String outFolder;
    private final File fileName;
    private final String fileExtension;
    private ArrayList<COSMOScontentFormat> productList;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    private OutputStyle bundleFlag;
    private File stationDir;
    
    //The products package assumes a file name structure for the V0 files.
    //
    public SmProduct(final File inFileName, final String newExt, 
                                                    final String newFolder) {
        this.fileName = inFileName;
        this.productList = new ArrayList<>();
        this.fileExtension = newExt;
        this.outFolder = newFolder;
        ConfigReader config = ConfigReader.INSTANCE;
        String outStyle = config.getConfigValue(OUT_FILE_FORMAT);
        if (outStyle.compareToIgnoreCase("bundled") == 0) {
            this.bundleFlag = OutputStyle.BUNDLED;
        } else {
            this.bundleFlag = OutputStyle.SINGLE_CHANNEL;
        }
    }
    public void addProduct(COSMOScontentFormat newprod ) {
        this.productList.add(newprod);
    }
    public void createDirectories() {
        String event;
        String station;
        StringBuilder sb = new StringBuilder(MAX_LINE_LENGTH);
        String name = this.fileName.getName();        
        boolean validname = validateFileName( name );
        
        if (validname) {
            String[] sections = name.split("\\.");
            event = sb.append(sections[0]).append(".").append(sections[1]).toString(); 
            sb = new StringBuilder(MAX_LINE_LENGTH);
            station = sb.append(sections[2]).append(".").append(sections[3]).toString(); 
        } else {
            event = "Orphan";
            station = "";
        }
        
        File eventId = Paths.get(this.outFolder, event).toFile();
        if (!eventId.isDirectory()) {
            eventId.mkdir();
        }
        File stationId = Paths.get(eventId.toString(), station).toFile();
        if (!stationId.isDirectory()) {
            stationId.mkdir();
        }
        this.stationDir = stationId;
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
        File V3Id = Paths.get(stationId.toString(), "V3").toFile();
        if (!V3Id.isDirectory()) {
            V3Id.mkdir();
        }        
    }
    
    public void writeOutProducts() throws IOException {
        createDirectories();
        Path outName = null;
        if (bundleFlag == OutputStyle.BUNDLED) {
            outName = buildFilename(bundleFlag, 0);
            Files.deleteIfExists(outName);
        }
        String[] contents;
        System.out.println("productlist length: " + this.productList.size());
        
        Iterator iter = this.productList.iterator();
        while (iter.hasNext()) {
            COSMOScontentFormat each = (COSMOScontentFormat)iter.next();
            if (bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                outName = buildFilename(bundleFlag, each.getChannelNum());
            }
            
            if (fileExtension.compareToIgnoreCase("V1") == 0) {
                V1Component rec = (V1Component)each;
                contents = rec.VrecToText();
                TextFileWriter textout = new TextFileWriter(outName, contents);
                if (bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                    textout.writeOutToFile();
                } else {
                    textout.appendToFile();
                }
            } else if (fileExtension.compareToIgnoreCase("V2") == 0) {
                //get corrected acceleration
                V2Component rec = (V2Component)each;
                contents = rec.VrecToText();
                TextFileWriter textout = new TextFileWriter(outName, contents);
                if (bundleFlag == OutputStyle.SINGLE_CHANNEL) {
                    textout.writeOutToFile();
                } else {
                    textout.appendToFile();
                }
                //get velocity and append to file
                ///add error checking for end of list !!!
                each = (COSMOScontentFormat)iter.next();
                rec = (V2Component)each;
                contents = rec.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.appendToFile();
                //get displacement and append
                each = (COSMOScontentFormat)iter.next();
                rec = (V2Component)each;
                contents = rec.VrecToText();
                textout = new TextFileWriter(outName, contents);
                textout.appendToFile();
            } else {
                System.out.println("+++ Ready to write out V3");
            }
        }
    }
    public Path buildFilename( OutputStyle aBundleFlag, int aChannelNum) {
        String startName = this.fileName.getName();
        String name = "";
        String getExtensionRegex = "\\.(?i)V\\d$";
        Pattern extension = Pattern.compile( getExtensionRegex );
        Matcher matcher = extension.matcher(startName);
        if (aBundleFlag == OutputStyle.SINGLE_CHANNEL) {
            String channel = String.valueOf(aChannelNum);
            name = matcher.replaceFirst("." + channel + "." + this.fileExtension);
        } else {
            name = matcher.replaceFirst("." + this.fileExtension);
        }
        Path outName = Paths.get(this.stationDir.toString(),this.fileExtension, name);
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
        System.out.println("stationDir: " + this.stationDir.toString());
        System.out.println("filename: " + this.fileName.getName());
        Path target = Paths.get(this.stationDir.toString(),"V0", this.fileName.getName());
        Path source = this.fileName.toPath();
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
//        Files.deleteIfExists(source);
    }
}
