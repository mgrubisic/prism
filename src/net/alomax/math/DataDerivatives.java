/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2002 Anthony Lomax <anthony@alomax.net; www.alomax.net>
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



package net.alomax.math;



/** Base class for sets of normalized DataDerivatives */

public class DataDerivatives {

	public double[] deriv = null;


    /** Constructor */

    public DataDerivatives(double[] deriv) {

		this(deriv, false);

    }



    /** Constructor */

    public DataDerivatives(double[] deriv, boolean normalize) {

		if (deriv != null) {
			this.deriv = deriv;
			if (normalize)
				normalize();
		} else {
			this.deriv = new double[0];
		}

    }



	/** normalize derivative vector */

	public void normalize() {

		double norm = 0.0;
		for (int i = 0; i < deriv.length; i++)
			norm += deriv[i] * deriv[i];

		norm = Math.sqrt(norm);

		if (norm < Double.MIN_VALUE)
			return;

		for (int i = 0; i < deriv.length; i++)
			deriv[i] /= norm;

	}



    /** returns a String value for this DataDerivatives */

    public String toString() {

		StringBuffer strBuf = new StringBuffer("(");
		for (int j = 0; j < deriv.length - 1; j++)
				strBuf.append((float) deriv[j] + ", ");
		strBuf.append((float) deriv[deriv.length - 1]);
		strBuf.append(")");

		return(strBuf.toString());

    }

}