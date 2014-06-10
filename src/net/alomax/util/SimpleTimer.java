/*
 * SimpleTimer.java
 *
 * Created on 26 May 2004, 13:15
 */

package net.alomax.util;

/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2004 Anthony Lomax <anthony@alomax.net>
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

/**
 *
 * @author  user
 */
public class SimpleTimer extends Thread {
    
    
    protected long millisecDely;
    protected SimpleTimerClient client = null;
    protected boolean cancel = false;
    
    /** constructor */
    
    /** Creates a new instance of SimpleTimer */
    public SimpleTimer(long millisecDely, SimpleTimerClient client) {
        
        this.millisecDely = millisecDely;
        this.client = client;
        
    }
    
    
    /** run method */
    
    public void cancel() {
        
        cancel = true;
        
    }
    
    
    /** run method */
    
    public void run() {
        
        try {
            while (true) {
                if (cancel)
                    return;
                sleep(millisecDely);
                if (cancel)
                    return;
                client.simpleTimerEvent(this);
            }
            
        } catch (Exception e) {		// InterruptedException
            ;
        }
        
        
    }
    
    
} // end - class Animator


