
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.V0Component;
import COSMOSformat.COSMOScontentFormat;
import COSMOSformat.TextFileWriter;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import static COSMOSformat.VFileConstants.*;
import java.io.*;
import java.util.ArrayList;

import SmException.FormatException;
import SmProcessing.SmDataProcess;

/**
 *
 * @author jmjones
 */
// This class holds a record for each channel in the input V0 file.  The file
// may only contain one channel or could have multiple channels bundled together.
public class SmQueue {
    private final File fileName; //input file name and path
    private int numRecords;  //number of channels found in the file
    private ArrayList<COSMOScontentFormat> smlist;  //holds each channel as a record
    private String[] fileContents;  // the input file contents by line
    
    public SmQueue (File inFileName){
        this.fileName = inFileName;
        this.numRecords = 0;
    }

    //read in the file into a temp arrayList. If the read was good, copy the
    //arrayList into a regular array and return the length.
    public int readInVFile() throws IOException{
        int status = -1;
        String nextLine;
        ArrayList<String> tempfile = new ArrayList<>();
        System.out.println(System.lineSeparator());
        System.out.println("+++ Reading in file: " + this.fileName);

        try (BufferedReader bufReader = new BufferedReader(new FileReader(this.fileName))){
            while ((nextLine = bufReader.readLine()) != null) {
                tempfile.add(nextLine);
            }
            if (tempfile.size() > 0){
                fileContents = tempfile.toArray(new String[tempfile.size()]);
                status = fileContents.length;
            } else {
                fileContents = new String[0];
                throw new IOException("Empty file: " + fileName.toString());
            }
        return status;
        }
    }

    // Start with the COSMOS text file in an array of strings.  Create a record for
    // each channel in the file and fill the record with the header and data
    // arrays.  Let the record object determine how much of the file goes into
    // each channel record.  Create an arrayList of all the records contained
    // in the file so they can be processed individually.  Keeping them in
    // the list will also facilitate writing out the results either individually
    // or bundled.
    public int parseVFile(String dataType) throws FormatException, NumberFormatException {
        int currentLine = 0;
        int returnLine;
        smlist = new ArrayList<>();
        
        
        while (currentLine < fileContents.length) {
            if (dataType.equals( RAWACC )) {
                V0Component rec = new V0Component( dataType );
                returnLine = rec.loadComponent(currentLine, fileContents);
                currentLine = (returnLine > currentLine) ? returnLine : fileContents.length;
                smlist.add(rec);
            } else if (dataType.equals( UNCORACC )){
                V1Component rec = new V1Component( dataType, null );
                returnLine = rec.loadComponent(currentLine, fileContents);
                currentLine = (returnLine > currentLine) ? returnLine : fileContents.length;
                smlist.add(rec);                
            } else if (dataType.equals( CORACC ) || dataType.equals( VELOCITY ) || 
                                                    dataType.equals( DISPLACE )) {
                V2Component rec = new V2Component( dataType, null, null );
                returnLine = rec.loadComponent(currentLine, fileContents);
                currentLine = (returnLine > currentLine) ? returnLine : fileContents.length;
                smlist.add(rec);                
            } else {
                throw new FormatException("Invalid file data type: " + dataType);
            }
        }
        numRecords = smlist.size();
        System.out.println("+++ found " + smlist.size() + " record(s) in file");
        return smlist.size();
    }
    
    public int processQueueContents(SmProduct V1prod) throws FormatException {
        //under construction
        double[] array;
        for (COSMOScontentFormat rec : smlist) {
            //start a new thread for each processing piece?
            SmDataProcess process = new SmDataProcess();
            array = process.countsToValues((V0Component)rec);
            //need to get the mean offset value from countsToValues for the header
            V1Component V1 = new V1Component( UNCORACC, (V0Component)rec);
            V1.buildV1(array);
            TextFileWriter V1out = new TextFileWriter( V1.getChannelNum(),V1.V1ToText());
            V1prod.addProduct(V1out);
        }
        return 0;
    }
}