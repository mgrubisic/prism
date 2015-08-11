/*******************************************************************************
 * Name: Java class FormatException.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/

package SmException;

/**
 * Class for exceptions to be thrown when a file format is bad.
 * original author Doug Given, Jiggle code
 * version 1.0
 *
 * @author jmjones, Doug Given
 * Borrowed this class from Jiggle code, Mar 2014
 */
public class FormatException extends Exception {

  protected int lineNo = -1;
  protected String line = null;

  /**
   * Default constructor
   */
  public FormatException() {
  }
  /**
   * Extends the standard exception by providing message text
   * @param msg the exception message text
   */
  public FormatException(String msg) {
    super(msg);
  }
  /** Descriptive message and line number of text containing incorrectly formatted data.
     * @param msg the exception message text
     * @param lineNumber line number in file that is incorrectly formatted
     */
  public FormatException(String msg, int lineNumber) {
    super(msg);
    lineNo = lineNumber;
  }
  /** Descriptive message and line of text containing incorrectly formatted data.
     * @param msg error message
     * @param line line of text to attach to message
     */
  public FormatException(String msg, String line) {
    super(msg);
    this.line = line;
  }
  /** Descriptive message and line number and line of text containing incorrectly formatted data.
     * @param msg descriptive error message
     * @param lineNumber line number of format error in file
     * @param line line contents
     */
  public FormatException(String msg, int lineNumber, String line) {
    super(msg);
    lineNo = lineNumber;
    this.line = line;
  }
  /** Descriptive message and if defined, the line number and/or line of text 
   * containing the incorrectly formatted data.
     * @return  the formatted string error message with line and line number
     */
  @Override
  public String toString() {
      String s = super.toString();
      if (lineNo > -1) s += "\n Error at line no. "+lineNo;
      if (line != null) s += "\n"+line;
      return s;
    }

}
