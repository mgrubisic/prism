/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 1999 Anthony Lomax <lomax@faille.unice.fr>
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


package net.alomax.util;


/**
 *  Represents binary data storage types
 */

public class BinaryType {

	/* BIG_ENDIAN
		Storing the most significant byte in the lowest memory
		address, which is the address of the data.
		Most UNIXes (for example, all System V) and the Internet
		are Big Endian. Motorola 680x0 microprocessors (and therefore
		Macintoshes), Hewlett-Packard PA-RISC, and Sun SuperSPARC
		processors are Big Endian. The Silicon Graphics MIPS and
		IBM/Motorola PowerPC processors are both Little and Big
		Endian (bi-endian).
	*/
	public static final int BIG_ENDIAN = 0;
	public static final int SUN_UNIX = BIG_ENDIAN;

	/* LITTLE_ENDIAN
		Specifies that the least significant byte is stored in the
		lowest-memory address, which is the address of the data.
		The Intel 80X86 and Pentium and DEC Alpha RISC processors
		are Little Endian.
		Windows NT and OSF/1 are Little Endian.
		Little Endian is the less common UNIX implementation.
	*/
	public static final int LITTLE_ENDIAN = 1;
	public static final int PC_INTEL = LITTLE_ENDIAN;


	// static fields
	public static String[] binaryTypes = {
		"SUN_UNIX", "PC_INTEL"
	};


	/** Converts binary type string to binary type */

	public static int stringToBinaryType(String binaryTypeStr) {

		if (binaryTypeStr.equalsIgnoreCase("LITTLE_ENDIAN")
			|| binaryTypeStr.equalsIgnoreCase("PC_INTEL"))
			return(LITTLE_ENDIAN);
		else
			return(BIG_ENDIAN);

	}


	/** Converts binary type integer to binary type */

	public static int intToBinaryType(int iBinaryType) {

		if (iBinaryType == LITTLE_ENDIAN)
			return(LITTLE_ENDIAN);
		else
			return(BIG_ENDIAN);

	}


	/** Inverts a binary type */

	public static int invert(int iBinaryType) {

		if (iBinaryType == BIG_ENDIAN)
			return(LITTLE_ENDIAN);
		else
			return(BIG_ENDIAN);

	}


	/** Inverts a binary type */

	public static String invert(String binaryTypeStr) {

		if (binaryTypeStr.equalsIgnoreCase("LITTLE_ENDIAN") || binaryTypeStr.equalsIgnoreCase("PC_INTEL"))
			return("SUN_UNIX");
		else
			return("PC_INTEL");


	}

        
        
    /** returns operating system architecture bnary type */
    
    public static int getArchitectureBinaryType() {
        
        String os_arch = System.getProperty("os.arch");
        
        int binaryType = BinaryType.SUN_UNIX;
        if (os_arch.indexOf("amd") >= 0 | os_arch.indexOf("x86") >= 0)
            binaryType = BinaryType.PC_INTEL;
        
        return(binaryType);
        
    }

        
        
    /** returns operating system architecture number of machine bits (i.e. 32, 64*/
    
    public static int getArchitectureMachineBits() {
        
        String os_arch = System.getProperty("os.arch");
        
        int machineBits = 32;
        if (os_arch.indexOf("64") >= 0)
            machineBits = 64;

        return(machineBits);
        
    }
    
    
    



}

