/*
 * Copyright (C) 2014 jmjones
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SmUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
public class TextFileReader {
    File fileName;
    String[] contents;
    
    public TextFileReader(File filename) throws IOException {
        Path check = filename.toPath();
        if (Files.isReadable(check)) {
            this.fileName = filename;
        } else {
            throw new IOException("Unable to read file " + filename);
        }
    }
    //read in the file into a temp arrayList. If the read was good, copy the
    //arrayList into a regular array and return.
    public String[] readInTextFile() throws IOException{
        String nextLine;
        ArrayList<String> tempfile = new ArrayList<>();
//        System.out.println("+++ Reading in file: " + this.fileName);

        try (BufferedReader bufReader = new BufferedReader(new FileReader(this.fileName))){
            while ((nextLine = bufReader.readLine()) != null) {
                tempfile.add(nextLine);
            }
            if (tempfile.size() > 0){
                contents = tempfile.toArray(new String[tempfile.size()]);
            } else {
                contents = new String[0];
                throw new IOException("Empty file: " + this.fileName);
            }
        return contents;
        }
    }

}
