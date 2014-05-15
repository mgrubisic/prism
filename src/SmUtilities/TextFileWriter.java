/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SmUtilities;

/**
 * 
 * @author jmjones
 */
public class TextFileWriter {
    private final int channelNum;
    private final String[] contents;
    
    public TextFileWriter( int channelNum, String[] contents) {
        this.channelNum = channelNum;
        this.contents = contents;
    }
    public int getChannelNum() {
        return this.channelNum;
    }
    public String[] getText() {
        return this.contents;
    }
}

