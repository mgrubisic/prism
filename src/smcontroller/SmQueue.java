/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package smcontroller;

import COSMOSformat.V0Format;
import static COSMOSformat.VFileConstants.RAWACC;
import java.io.*;
import java.util.ArrayList;

import SmException.FormatException;

/**
 *
 * @author jmjones
 */
// This class holds a record for each channel in the input V0 file.  The file
// may only contain one channel or could have multiple channels bundled together.
public class SmQueue {
    private final File inFile; //input file name and path
    private int numRecords;  //number of channels found in the file
    private ArrayList<V0Format> smlist;  //holds each channel as a record
    private String[] rawAccelFile;  // the input file contents by line
    
    public SmQueue (File inFile){
        this.inFile = inFile;
        this.numRecords = 0;
    }

    //read in the file into a temp arrayList. If the read was good, copy the
    //arrayList into a regular array and return the length.
    public int readInV0() throws IOException{
        int status = -1;
        String nextLine;
        ArrayList<String> tempfile = new ArrayList<>();
        System.out.println(System.lineSeparator());
        System.out.println("Reading in file: " + this.inFile);

        try (BufferedReader bufReader = new BufferedReader(new FileReader(this.inFile))){
            while ((nextLine = bufReader.readLine()) != null) {
                tempfile.add(nextLine);
            }
            if (tempfile.size() > 0){
                rawAccelFile = tempfile.toArray(new String[tempfile.size()]);
                status = rawAccelFile.length;
            } else {
                rawAccelFile = new String[0];
                throw new IOException("Empty file: " + inFile.toString());
            }
        return status;
        }
    }

    // Start with the V0 text file in an array of strings.  Create a record for
    // each channel in the file and fill the record with the header and data
    // arrays.  Let the record object determine how much of the file goes into
    // each channel record.  Create an arrayList of all the records contained
    // in the file so they can be processed individually.  Keeping them in
    // the list will also facilitate writing out the results either individually
    // or bundled.
    public int parseV0() throws FormatException, NumberFormatException {
        int currentLine = 0;
        int returnLine;
        smlist = new ArrayList<>();
        
        while (currentLine < rawAccelFile.length) {
            V0Format rec = new V0Format( RAWACC );
            System.out.println("currentline: " + currentLine +
                                      " filelength: " + rawAccelFile.length);
            returnLine = rec.loadComponent(currentLine, rawAccelFile);

            if (returnLine > currentLine) {
                currentLine = returnLine;
            } else {
                currentLine = rawAccelFile.length;
            }
            smlist.add(rec);
        }
        numRecords = smlist.size();
        System.out.println("found " + smlist.size() + " record(s) in file");
        return smlist.size();
    }
}