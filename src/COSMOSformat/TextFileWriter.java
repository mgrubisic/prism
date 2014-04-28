/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package COSMOSformat;

/**
 *
 * @author jmjones
 */
public class TextFileWriter {
    private final int channelNum;
    private final String[] contents;
    
    public TextFileWriter( int aChannelNum, String[] aContents) {
        this.channelNum = aChannelNum;
        this.contents = aContents;
    }
    public int getChannelNum() {
        return this.channelNum;
    }
    public String[] getText() {
        String [] outText = new String[contents.length];
        System.arraycopy(contents, 0, outText, 0, contents.length);
        return outText;
    }
}

