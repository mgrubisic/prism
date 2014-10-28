
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
import static SmConstants.VFileConstants.*;
import SmConstants.VFileConstants.V2DataType;
import SmException.FormatException;
import SmException.SmException;
import SmProcessing.V1Process;
import SmProcessing.V2Process;
import SmProcessing.V3Process;
import SmUtilities.TextFileReader;
import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author jmjones
 */
// This class holds a record for each channel in the input V0 file.  The file
// may only contain one channel or could have multiple channels bundled together.
public class SmQueue {
    private final File fileName; //input file name and path
    private ArrayList<COSMOScontentFormat> smlist;  //holds each channel as a record
    private String[] fileContents;  // the input file contents by line
    
    public SmQueue (File inFileName){
        this.fileName = inFileName;
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
                V1Component rec = new V1Component( dataType );
                returnLine = rec.loadComponent(currentLine, fileContents);
                currentLine = (returnLine > currentLine) ? returnLine : fileContents.length;
                smlist.add(rec);                
            } else if ((dataType.equals( CORACC )) || (dataType.equals( VELOCITY )) ||
                                                 (dataType.equals( DISPLACE ))) {
                //Peek at current line to see what piece of V2 is next.
                if (fileContents[currentLine].matches("(?s).*(?i)Velocity.*")) {
                    dataType = VELOCITY;
                } else if (fileContents[currentLine].matches("(?s).*(?i)Displace.*"))  {
                    dataType = DISPLACE;
                } else {
                    dataType = CORACC;
                }
                V2Component rec = new V2Component( dataType );
                returnLine = rec.loadComponent(currentLine, fileContents);
                currentLine = (returnLine > currentLine) ? returnLine : fileContents.length;
                smlist.add(rec);                
            } else {
                throw new FormatException("Invalid file data type: " + dataType);
            }
        }
        return smlist.size();
    }
    public void processQueueContents(SmProduct Vprod) 
                                throws FormatException, SmException, IOException {

        for (COSMOScontentFormat rec : smlist) {
            //declare rec as a V0 channel record
            V0Component v0rec = (V0Component)rec;
            v0rec.setFileName(this.fileName.toString());
            v0rec.checkForRcrdIdAndAuth();
            
            //create the V1 processing object and do the processing          
            V1Process v1val = new V1Process(v0rec);
            v1val.processV1Data();
            
            //create a V1 component to get the processing results
            V1Component v1rec = new V1Component( UNCORACC, v0rec);
            v1rec.buildV1(v1val);
           
            //Create the V2 processing object and do the processing.  V2 processing
            //produces 3 V2 objects: corrected acceleration, velocity, and displacement
            V2Process v2val = new V2Process(v1rec, this.fileName);
            System.out.println("V0 file: " + this.fileName);
            V2Status V2result = v2val.processV2Data();
            
            //create the V2 components to get the processing results
            V2Component V2acc = new V2Component( CORACC, v1rec );
            V2acc.buildV2(V2DataType.ACC, v2val);
            V2Component V2vel = new V2Component( VELOCITY, v1rec );
            V2vel.buildV2(V2DataType.VEL, v2val);
            V2Component V2dis = new V2Component( DISPLACE, v1rec );
            V2dis.buildV2(V2DataType.DIS, v2val);
            
            Vprod.setDirectories(v0rec.getRcrdId(),v0rec.getSCNLauth(), 
                                                V2acc.getEventDateTime(),V2result);
            Vprod.addProduct(v0rec, "V0");
            Vprod.addProduct(v1rec, "V1");
            if (V2result != V2Status.NOEVENT) {
                Vprod.addProduct(V2acc, "V2");
                Vprod.addProduct(V2vel, "V2");
                Vprod.addProduct(V2dis, "V2");
            }
            if (V2result == V2Status.GOOD) {
                //Create the V3 processing object and do the processing.  V3
                //processing produces 1  V3 object: response spectra.
                V3Process v3val = new V3Process(V2acc, V2vel, V2dis);
                v3val.processV3Data();
                V3Component V3rec = new V3Component( SPECTRA, V2acc);
                V3rec.buildV3(v3val);
                Vprod.addProduct(V3rec, "V3");
            }
        }
    }
    public ArrayList<COSMOScontentFormat> getSmList() {
        return smlist;
    }
}