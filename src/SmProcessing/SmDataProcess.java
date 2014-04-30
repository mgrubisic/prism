/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmProcessing;

import COSMOSformat.V0Component;
import static COSMOSformat.VFileConstants.*;

/**
 *
 * @author jmjones
 */
public class SmDataProcess {
    
    public SmDataProcess() {
    }
    //use static factory methods instead of constructors
    
    
    //think about making SmDataProcess threadsafe.
    //Try to keep the api clean of V records?  Just operate on arrays and
    //parameters and let the caller build the products?
    
    //This needs to return the max value, the avg value, and the place in the
    //array where the max value occurred.  It also needs to return the mean
    //offset value.  Make all processing methods return an object with these
    //values as instance vars.
    
    public double[] countsToValues(V0Component v0rec) {
        
        int inLength = v0rec.getDataLength();
        int[] inArray = v0rec.getDataArray();
        double[] result = new double[inLength];
        double total = 0.0;
        double meanZero = 0.0;
        double recLSB = v0rec.getRealHeaderValue(RECORER_LSB);
        double recFSI = v0rec.getRealHeaderValue(RECORDER_FSI);
        double sensitivity = v0rec.getRealHeaderValue(SENSOR_SENITIVITY);
        double microToVolt = 1.0e-6;
//        double microToVolt = 1.0;
        
        //sensor calculation of volts per count and cm per sq. sec per volt
        //countToCMS units are cm per sq. sec per count
        //This is multiplied by each count to get the sensor value in cm per sq. sec
        double countToCMS = ((recFSI / recLSB) * microToVolt) * (FROM_G_CONVERSION / sensitivity);
        System.out.println("+++ countToCMS: " + countToCMS);
        for (int i = 0; i < inLength; i++) {
            result[i] = inArray[i] * countToCMS;
            total = total + result[i];
        }
        meanZero = total / inLength;
        System.out.println("+++ Mean Zero: " + meanZero);
        for (int i = 0; i < inLength; i++) {
            result[i] = result[i] - meanZero;
        }
        return result; //real array of raw acceleration counts converted to physical values
    }
}

