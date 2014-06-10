/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2007 Anthony Lomax <lomax@faille.unice.fr>
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


package net.alomax.math.stat;

import net.alomax.math.MathException;


/**
 * StatUtils provides static methods for computing statistics based on data
 * stored in double[] arrays.
 *
 */
public final class StatUtils {
    
    
    /**
     * Private Constructor
     */
    private StatUtils() {
    }
    
    
    /** returns weighted mean of an array of values and and array of weights */
    
    public static double mean(double[] values, double[] weights) throws MathException {
        
        if (values.length != weights.length)
            throw(new MathException("StatUtils.mean: number of values and number of weights not identical"));
        
        double wtSum = 0.0;
        double cumulate = 0.0;
        for(int i = 0; i < values.length; i++){
            cumulate += values[i] * weights[i];
            wtSum += weights[i];
        }
        
        return(cumulate / wtSum);
        
    }
    
    
    /** returns weighted geometric mean of an array of values and and array of weights */
    
    public static double geometricMean(double[] values, double[] weights) throws MathException {
        
        if (values.length != weights.length)
            throw(new MathException("StatUtils.geometricMean: number of values and number of weights not identical"));
        
        double wtSum = 0.0;
        double cumulate = 1.0;
        for(int i = 0; i < values.length; i++) {
            cumulate *= Math.pow(values[i], weights[i]);
            wtSum += weights[i];
        }
        
        return(Math.pow(cumulate, 1.0 / wtSum));
        
    }

    
}







