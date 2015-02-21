/*******************************************************************************
 * Name: Java class SmException.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmException;

/**
 * Class for exceptions unique to the PRISM processing
 * @author jmjones
 */
public class SmException extends Exception {
    private final String message;
    /**
     * Default constructor
     * @param message the error message
     */
    public SmException (String message){
        this.message = message;
    }
    /**
     * Getter for the error message
     * @return the error message
     */
    @Override
    public String getMessage() {
        return message;
    }
}
