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
import java.nio.file.StandardOpenOption;

/**
 * 
 * @author jmjones
 */
public class TextFileWriter {
    private final Path outName;
    private final String[] contents;
    private final Charset ENCODING = StandardCharsets.UTF_8;
    
    public TextFileWriter( Path outfilename, String[] contents) {
        this.contents = contents;
        this.outName = outfilename;
    }
    
    public void writeOutToFile() throws IOException {
        System.out.println("writing, 1st line: " + contents[0]);
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
        System.out.println("appending, 1st line: " + contents[0]);
        try (BufferedWriter writer = Files.newBufferedWriter(outName, ENCODING, 
                                                    StandardOpenOption.APPEND)) {
            for (String line : contents) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}

