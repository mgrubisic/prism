
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SMCOSMOScontrol;

import COSMOSformat.V0Component;
import COSMOSformat.COSMOScontentFormat;
import SmUtilities.TextFileWriter;
import COSMOSformat.V1Component;
import COSMOSformat.V2Component;
import static COSMOSformat.VFileConstants.*;
import java.io.*;
import java.util.ArrayList;

import SmException.FormatException;
import SmException.SmException;
import SmProcessing.V1Process;
import SmUtilities.ConfigReader;
import SmUtilities.TextFileReader;

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

    //read in the input text file
    public void readInFile(File filename) throws IOException{
        TextFileReader infile = new TextFileReader( filename );
        fileContents = infile.readInTextFile();
    }

    // Start with the COSMOS text file in an array of strings.  Create a record for
    // each channel in the file and fill the record with the header and data
    // arrays.  Let the record object determine how much of the file goes into
    // each channel record.  Create an arrayList of all the records contained
    // in the file so they can be processed individually.  Keeping them in
    // the list will also facilitate writing out the results either individually
    // or bundled.
    public int parseVFile(String dataType) throws FormatException, 
                                        NumberFormatException, SmException {
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
    
    public int processQueueContents(SmProduct V1prod, ConfigReader config) throws FormatException, SmException {
        //under construction
        double[] array;
        for (COSMOScontentFormat rec : smlist) {
            //declare rec as a V0 channel record
            V0Component v0rec = (V0Component)rec;
            
            //create the V1 processing object and do the processing          
            V1Process v1val = new V1Process(v0rec, config);
            v1val.processV1Data();
            
            //create a V1 component to get the processing results
            V1Component V1 = new V1Component( UNCORACC, (V0Component)rec);
            V1.buildV1(v1val, config);
            
            //move results to the output queue
            TextFileWriter V1out = new TextFileWriter( V1.getChannelNum(),V1.V1ToText());
            V1prod.addProduct(V1out);
        }
        return 0;
    }
}