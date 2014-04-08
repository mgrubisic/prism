/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmException;

/**
 * Exception to be thrown when a file format is bad.
 * @original author Doug Given, Jiggle code
 * @version 1.0
 *
 * @author jmjones
 * Borrowed this class from Jiggle code, Mar 2014
 */
public class FormatException extends Exception {

  protected int lineNo = -1;
  protected String line = null;

  public FormatException() {
  }
  public FormatException(String msg) {
    super(msg);
  }
  /** Descriptive message and line number of text containing incorrectly formatted data.
     * @param msg
     * @param lineNumber */
  public FormatException(String msg, int lineNumber) {
    super(msg);
    lineNo = lineNumber;
  }
  /** Descriptive message and line of text containing incorrectly formatted data.
     * @param msg
     * @param line */
  public FormatException(String msg, String line) {
    super(msg);
    this.line = line;
  }
  /** Descriptive message and line number and line of text containing incorrectly formatted data.
     * @param msg
     * @param lineNumber
     * @param line */
  public FormatException(String msg, int lineNumber, String line) {
    super(msg);
    lineNo = lineNumber;
    this.line = line;
  }
  /** Descriptive message and if defined, the line number and/or line of text containing the incorrectly formatted data.
     * @return  */
  @Override
  public String toString() {
      String s = super.toString();
      if (lineNo > -1) s += "\n Error at line no. "+lineNo;
      if (line != null) s += "\n"+line;
      return s;
    }

}
