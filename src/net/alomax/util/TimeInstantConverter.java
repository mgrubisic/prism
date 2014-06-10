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



/** Interface for converting TimeInstants to reference times */

public abstract interface TimeInstantConverter {


 	/** Method to convert a reference time in seconds to a TimeInstant.
	 */

	public abstract TimeInstant refTimeToTimeInstant(double time);


 	/** Method to convert a TimeInstant to a reference time in seconds
	 */

	public abstract double timeInstantToRefTime(TimeInstant timeInstant);


}	// end interface TimeInstantConverter

