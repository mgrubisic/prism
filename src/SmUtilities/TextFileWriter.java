/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 
 * @author jmjones
 */
public class TextFileWriter {
    private final Path outName;
    private final String[] contents;
    private final double[] array;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    
    public TextFileWriter( Path outfilename, String[] contents) {
        this.contents = contents;
        this.outName = outfilename;
        this.array = new double[0];
    }
    
    public TextFileWriter( String dir, String outname, double[] array) { //debug
        this.array = array;
        this.contents = new String[ array.length ];
        this.outName = Paths.get(dir, outname);
    }
    
    public void writeOutToFile() throws IOException {
        //try-with-resources automatically closes the resource upon completion.
        //At the end of the try, the file is closed, for both regular completion 
        //and exception.
        try (BufferedWriter writer = Files.newBufferedWriter(outName, ENCODING)) {
            for (String line : contents) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
    public void appendToFile() throws IOException {
        if (Files.notExists(outName)) {
            Files.createFile(outName);
        }
        //try-with-resources automatically closes the resource upon completion.
        //At the end of the try, the file is closed, for both regular completion 
        //and exception.
        try (BufferedWriter writer = Files.newBufferedWriter(outName, ENCODING, 
                                                    StandardOpenOption.APPEND)) {
            for (String line : contents) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
    public void writeOutArray( ) throws IOException {  //mainly for debug
        int len = array.length;
        for (int i = 0; i < len; i++) {
            contents[i] = Double.toString(array[i]);
        }
        writeOutToFile();
    }
}

