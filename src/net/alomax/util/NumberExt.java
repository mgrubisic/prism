/* 
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2000 Anthony Lomax <anthony@alomax.net www.alomax.net>
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
 * Extensions to java.lang.String
 */
public class NumberExt {

    /**
     * Checks range of double values.
     *
     * Returns the double value.
     *
     */
    public static double checkRange(double val, double vmin, double vmax,
            boolean fix, double vReplace, boolean verbose, String name) {

        if (!inRange(val, vmin, vmax)) {
            if (verbose) {
                System.out.print(
                        "WARNING: Double value <" + name + "> = <" + val + "> out of range.");
            }
            if (fix) {
                val = vReplace;
                if (verbose) {
                    System.out.println(" Replaced with <" + vReplace + ">.");
                }
            } else {
                if (verbose) {
                    System.out.println("");
                }
            }
        }

        return (val);

    }

    public static boolean inRange(double val, double vmin, double vmax) {

        return (val >= vmin && val <= vmax);
    }

    /**
     * Checks range of int values.
     *
     * Returns the int value.
     *
     */
    public static int checkRange(int val, int vmin, int vmax,
            boolean fix, int vReplace, boolean verbose, String name) {

        if (!inRange(val, vmin, vmax)) {
            if (verbose) {
                System.out.print(
                        "WARNING: Int value <" + name + "> = <" + val + "> out of range.");
            }
            if (fix) {
                val = vReplace;
                if (verbose) {
                    System.out.println(" Replaced with <" + vReplace + ">.");
                }
            } else {
                if (verbose) {
                    System.out.println("");
                }
            }
        }

        return (val);

    }

    public static boolean inRange(int val, int vmin, int vmax) {

        return (val >= vmin && val <= vmax);
    }
}
