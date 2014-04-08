/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmException;

/**
 *
 * @author jmjones
 */
public class SmException extends Exception {
    private String message;
    public SmException (String message){
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
