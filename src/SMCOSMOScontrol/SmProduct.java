/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import COSMOSformat.VFileConstants;
import COSMOSformat.VFileConstants.OutputStyle;
import SmUtilities.ConfigReader;
import static SmUtilities.SmConfigConstants.OUT_FILE_FORMAT;
import SmUtilities.TextFileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    
    public SmProduct(final File inFileName, final String newExt, 
                        final String newFolder, final ConfigReader config) {
        this.fileName = inFileName;
        this.productList = new ArrayList<>();
        this.fileExtension = newExt;
        this.outFolder = newFolder;
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
    //fix this to deal with single or bundled out to file!!!
    public void writeOutProducts() throws IOException {
        Path outName;
        String[] contents = new String[0];
        for (COSMOScontentFormat each : this.productList) {
            outName = buildFilename(each.getChannelNum(), bundleFlag);
            if (fileExtension.compareToIgnoreCase("V1") == 0) {
                V1Component rec = (V1Component)each;
                contents = rec.VrecToText();
            } else if (fileExtension.compareToIgnoreCase("V2") == 0) {
                V2Component rec = (V2Component)each;
                contents = rec.VrecToText();
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outName, ENCODING)) {
                for (String line : contents) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }
    public Path buildFilename( int aChannelNum, OutputStyle aBundleFlag) {
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
        Path outName = Paths.get(this.outFolder, name);
        return outName;
    }
}
