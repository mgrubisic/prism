/* 
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2001 Anthony Lomax <anthony@alomax.net www.alomax.net>
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

public class Sort {

    /** Shellsort using Hibbard increments
     *
     *  Hibbard increments: 1, 3, 7, ... , (2**k - 1)
     *
     *  (from Weiss, Data structures and algorithms in Java, Addison-Wesley, 1999,  sec 7.4
     *
     */
    public static void shellSort(String[] a, boolean increasingSort) {

        // find largest Hibbard increment
        int index = 0;
        while (intPow(2, index + 1) - 1 < a.length) {
            index++;
        }
        // do shell sort
        for (; index >= 0; index--) {
            int gap = intPow(2, index) - 1;
            for (int i = gap; i < a.length; i++) {
                String tmp = a[i];
                int j = i;
                for (; j >= gap && tmp.compareTo(a[j - gap]) < 0; j -= gap) {
                    a[j] = a[j - gap];
                }
                a[j] = tmp;
            }
        }

        // reverse order for decreasing sort
        if (!increasingSort) {
            for (int i = 0; i < a.length / 2; i++) {
                int iswap = a.length - i - 1;
                String tmp = a[i];
                a[i] = a[iswap];
                a[iswap] = tmp;
            }
        }

    }

    /** Shellsort using Hibbard increments
     *
     *  Hibbard increments: 1, 3, 7, ... , (2**k - 1)
     *
     *  (from Weiss, Data structures and algorithms in Java, Addison-Wesley, 1999,  sec 7.4
     *
     */
    public static void shellSort(double[] a, boolean increasingSort) {

        // find largest Hibbard increment
        int index = 0;
        while (intPow(2, index + 1) - 1 < a.length) {
            index++;
        }
        // do shell sort
        for (; index >= 0; index--) {
            int gap = intPow(2, index) - 1;
            for (int i = gap; i < a.length; i++) {
                double tmp = a[i];
                int j = i;
                for (; j >= gap && tmp < a[j - gap]; j -= gap) {
                    a[j] = a[j - gap];
                }
                a[j] = tmp;
            }
        }

        // reverse order for decreasing sort
        if (!increasingSort) {
            for (int i = 0; i < a.length / 2; i++) {
                int iswap = a.length - i - 1;
                double tmp = a[i];
                a[i] = a[iswap];
                a[iswap] = tmp;
            }
        }

    }

    /** Shellsort using Hibbard increments, sorts on a[n][0] values
     *
     *  Hibbard increments: 1, 3, 7, ... , (2**k - 1)
     *
     *  (from Weiss, Data structures and algorithms in Java, Addison-Wesley, 1999,  sec 7.4
     *
     */
    public static void shellSort(double[][] a, boolean increasingSort) {

        double[] tmp = new double[2];

        // find largest Hibbard increment
        int index = 0;
        while (intPow(2, index + 1) - 1 < a.length) {
            index++;
        }
        // do shell sort
        for (; index >= 0; index--) {
            int gap = intPow(2, index) - 1;
            for (int i = gap; i < a.length; i++) {
                tmp[0] = a[i][0];
                tmp[1] = a[i][1];
                int j = i;
                for (; j >= gap && tmp[0] < a[j - gap][0]; j -= gap) {
                    a[j][0] = a[j - gap][0];
                    a[j][1] = a[j - gap][1];
                }
                a[j][0] = tmp[0];
                a[j][1] = tmp[1];
            }
        }

        // reverse order for decreasing sort
        if (!increasingSort) {
            for (int i = 0; i < a.length / 2; i++) {
                int iswap = a.length - i - 1;
                tmp[0] = a[i][0];
                tmp[1] = a[i][1];
                a[i][0] = a[iswap][0];
                a[i][1] = a[iswap][1];
                a[iswap][0] = tmp[0];
                a[iswap][1] = tmp[1];
            }
        }

    }

    /** integer power function */
    protected static int intPow(int a, int b) {

        int ipow = 1;
        for (int i = 0; i < b; i++) {
            ipow *= a;
        }

        return (ipow);
    }
}

