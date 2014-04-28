/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.TextFileWriter;
import static COSMOSformat.VFileConstants.BUNDLED;
import static COSMOSformat.VFileConstants.SINGLE_CHANNEL;
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
    private ArrayList<TextFileWriter> productList;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    
    public SmProduct(File inFileName, String newExt, String newFolder) {
        this.fileName = inFileName;
        this.productList = new ArrayList<>();
        this.fileExtension = newExt;
        this.outFolder = newFolder;
    }
    public void addProduct(TextFileWriter newprod ) {
        this.productList.add(newprod);
    }
    public void writeOutProducts() throws IOException {
        Path outName;
        String[] contents;
        int bundleFlag = SINGLE_CHANNEL;
        if (this.productList.size() > 1) {
            bundleFlag = BUNDLED;
        }
        for (TextFileWriter each : this.productList) {
            outName = buildFilename(each.getChannelNum(), bundleFlag);
            contents = each.getText();
            try (BufferedWriter writer = Files.newBufferedWriter(outName, ENCODING)) {
                for (String line : contents) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }
    private Path buildFilename( int aChannelNum, int aBundleFlag) {
        String startName = this.fileName.getName();
        String name = "";
        String getExtensionRegex = "\\.(?i)V\\d$";
        Pattern extension = Pattern.compile( getExtensionRegex );
        Matcher matcher = extension.matcher(startName);
        if (aBundleFlag == BUNDLED) {
            String channel = String.valueOf(aChannelNum);
            name = matcher.replaceFirst("." + channel + "." + this.fileExtension);
        } else {
            name = matcher.replaceFirst("." + this.fileExtension);
        }
        Path outName = Paths.get(this.outFolder, name);
        return outName;
    }
}
