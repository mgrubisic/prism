/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2007 Anthony Lomax <anthony@alomax.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


/*
 * AJLProcess.java
 *
 * Created on 6 June 2007
 */

package net.alomax.util;

import java.io.*;
/**
 *
 * @author  Anthony Lomax
 */


public class ProcessUtil {
    
    public StringBuffer errorStr = null;
    public StringBuffer standardStr = null;
    
    
    /** constructor */
    
    public ProcessUtil() {
        
        ;
        
    }
    
    
    /** returns error output */
    
    public static StringBuffer getErrorOut(Process process) throws IOException {
        
        BufferedInputStream processErrorStream = new BufferedInputStream(process.getErrorStream());
        StringBuffer errStr = new StringBuffer(1024);
        int ibyte;
        while ((ibyte = processErrorStream.read()) >= 0)
            errStr.append((char) ibyte);
        
        return(errStr);
        
    }
    
    
    
    /** returns Standart output */
    
    public static StringBuffer getStandardOut(Process process) throws IOException {
        
        BufferedInputStream processStandardStream = new BufferedInputStream(process.getInputStream());
        StringBuffer stdStr = new StringBuffer(1024);
        int ibyte;
        while ((ibyte = processStandardStream.read()) >= 0)
            stdStr.append((char) ibyte);
        
        return(stdStr);
        
    }
    
    
    /** execute a process */
    
    public int exec(String commandString) throws Exception {
        
        return(exec(commandString, false));
        
    }
        

        
    /** execute a process */
    
    public int exec(String[] commandArray, boolean verbose) throws Exception {
        
        //System.out.println("ProcessUtil: command is: " + commandString);
        
        Process process = Runtime.getRuntime().exec(commandArray);
        
        return(monitorProcess(process, verbose));
        
    }

        
    /** execute a process */
    
    public int exec(String commandString, String[] envp, File dir, boolean verbose) throws Exception {
        
        //System.out.println("ProcessUtil: command is: " + commandString);
        
        Process process = Runtime.getRuntime().exec(commandString, envp, dir);
        
        return(monitorProcess(process, verbose));
        
    }
    
    
    /** execute a process */
    
    public int exec(String[] commandArray, String[] envp, File dir, boolean verbose) throws Exception {
        
        //System.out.println("ProcessUtil: command is: " + commandString);
        
        Process process = Runtime.getRuntime().exec(commandArray, envp, dir);
        
        return(monitorProcess(process, verbose));
        
    }

        
    /** execute a process */
    
    public int exec(String commandString, boolean verbose) throws Exception {
        
        //System.out.println("ProcessUtil: command is: " + commandString);
        
        Process process = Runtime.getRuntime().exec(commandString);
        
        return(monitorProcess(process, verbose));
        
    }
    
    
    /** monitor a process */
    
    public int monitorProcess(Process process, boolean verbose) throws Exception {
        
        BufferedInputStream processErrorStream = new BufferedInputStream(process.getErrorStream());
        errorStr = new StringBuffer(1024);
        BufferedInputStream processStandardStream = new BufferedInputStream(process.getInputStream());
        standardStr = new StringBuffer(1024);
        
        int exitValue = -1;
        int n = 0;
        while (true) {
            while (processErrorStream.available() > 0) {
                char c = (char) processErrorStream.read();
                errorStr.append(c);
                if (verbose)
                    System.err.print(c);
            }
            while (processStandardStream.available() > 0) {
                char c = (char) processStandardStream.read();
                standardStr.append(c);
                if (verbose)
                    System.out.print(c);
            }
            try {
                exitValue = process.exitValue();
                break;
            } catch (IllegalThreadStateException e) {
                ;
            }
            try {
                Thread.currentThread().sleep(100);
            } catch (Exception e) {;}
        }
        
        processErrorStream.close();
        processStandardStream.close();
        process.destroy();
        
        return(exitValue);
        
        
    }
    
    
}








