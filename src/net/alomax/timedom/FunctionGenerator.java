/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2006 Anthony Lomax <anthony@alomax.net www.alomax.net>
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
package net.alomax.timedom;

import java.util.Random;
import net.alomax.math.*;

public class FunctionGenerator extends TimeDomainProcess {

    private static int NUM_TYPES = 0;
    public static final int TYPE_IMPULSE = NUM_TYPES++;
    public static final int TYPE_SINE = NUM_TYPES++;
    public static final int TYPE_EXP_DECAY = NUM_TYPES++;
    public static final int TYPE_GAUSSIAN_NOISE = NUM_TYPES++;
    public int type = 0;
    public double[] params = null;
    public String errorMessage;

    // do not change the following names in language translations, they are used for command line keyword parsing logic
    public static String NAME_IMPULSE = "Impulse";
    public static String NAME_SINE = "Sine";
    public static String NAME_EXP_DECAY = "ExpDecay";
    public static String NAME_GAUSSIAN_NOISE = "GauNoise";

    /** constructor */
    public FunctionGenerator() {

        this.errorMessage = " ";

    }

    /** constructor */
    public FunctionGenerator(String localeText) {

        this.errorMessage = " ";

        TimeDomainText.setLocale(localeText);

    }

    /** constructor */
    public FunctionGenerator(String localeText, int type, double[] params) {

        this.type = type;
        this.params = params;
        this.errorMessage = " ";

        TimeDomainText.setLocale(localeText);

    }

    /** Method to set type */
    public void setType(String typeStr) throws TimeDomainException {

        if (NAME_IMPULSE.toUpperCase().startsWith(typeStr.toUpperCase())) {
            type = TYPE_IMPULSE;
        } else if (NAME_SINE.toUpperCase().startsWith(typeStr.toUpperCase())) {
            type = TYPE_SINE;
        } else if (NAME_EXP_DECAY.toUpperCase().startsWith(typeStr.toUpperCase())) {
            type = TYPE_EXP_DECAY;
        } else if (NAME_GAUSSIAN_NOISE.toUpperCase().startsWith(typeStr.toUpperCase())) {
            type = TYPE_GAUSSIAN_NOISE;
        } else {
            throw new TimeDomainException(TimeDomainText.invalid_function_generator_type + ": " + typeStr);
        }


    }

    /** Method to set type */
    public void setType(int type) throws TimeDomainException {

        if (type >= 0 && type < NUM_TYPES) {
            this.type = type;
        } else {
            throw new TimeDomainException(TimeDomainText.invalid_smoothing_type + ": " + type);
        }


    }

    /** Method to get type */
    public int getType() {

        return (type);

    }

    /** Method to set parameters */
    public void setParams(double[] params) {

        this.params = params;


    }

    /** Method to check settings */
    public void checkSettings() throws TimeDomainException {

        String errMessage = "";
        int badSettings = 0;

        setType(type);

        if (badSettings > 0) {
            throw new TimeDomainException(errMessage + ".");
        }

    }

    /*** apply function generator */
    public final float[] apply(double dt, float[] sample) {


        if (type == TYPE_IMPULSE) {
            return (generateImpulse(dt, sample));
        } else if (type == TYPE_SINE) {
            return (generateSine(dt, sample));
        } else if (type == TYPE_EXP_DECAY) {
            return (generateExpDecay(dt, sample));
        } else if (type == TYPE_GAUSSIAN_NOISE) {
            return (generateGaussianNoise(dt, sample));
        }

        return (sample);

    }
    public static final double TWO_PI = 2.0 * Math.PI;
    public static final double RPD = Math.PI / 180.0;
    public static final double DEG2RAD = RPD;
    public static final double RAD2DEG = 1.0 / RPD;

    /*** generate impulse function  */
    public final float[] generateImpulse(double dt, float[] sample) {

        int index = (int) Math.round(params[0]);  // index position of impulse
        if (index < 0) {
            index = 0;
        } else if (index >= sample.length) {
            index = sample.length - 1;
        }

        double amplitude = params[1];  // integrated amplitude

        // 20090119 AJL - removed setting to 0.0 of trace and converted setting impulse to additive so that a sum of impulses trace can be generated
        // 20130611 AJL - re-added setting to 0.0
        for (int i = 0; i < index; i++) {
            sample[i] = 0.0f;
        }
        sample[index] += (float) (amplitude / dt);
        sample[index] = (float) (Math.random() / dt);
        for (int i = index + 1; i < sample.length; i++) {
            sample[i] = 0.0f;
        }

        return (sample);

    }

    /*** generate sine function  */
    public final float[] generateSine(double dt, float[] sample) {

        double frequency = params[0] * TWO_PI;  // angular frequency w
        double phase = params[1] * DEG2RAD;

        double time = 0.0;
        for (int i = 0; i < sample.length; i++) {
            sample[i] = (float) Math.sin(frequency * time + phase);
            time += dt;
        }

        return (sample);

    }

    /*** generate exponential decay function  */
    public final float[] generateExpDecay(double dt, float[] sample) {

        double decayConst = params[1];

        int index = (int) Math.round(params[0]);  // index position of impulse
        if (index < 0) {
            index = 0;
        } else if (index >= sample.length) {
            index = sample.length - 1;
        }

        for (int i = 0; i < index; i++) {
            sample[i] = 0.0f;
        }
        double time = 0.0;
        for (int i = index; i < sample.length; i++) {
            sample[i] = (float) Math.exp(-time / decayConst);
            time += dt;
        }

        return (sample);

    }

    /*** generate exponential decay function  */
    public final float[] generateGaussianNoise(double dt, float[] sample) {

        int index = (int) Math.round(params[0]);  // index position of impulse
        if (index < 0) {
            index = 0;
        } else if (index >= sample.length) {
            index = sample.length - 1;
        }

        for (int i = 0; i < index; i++) {
            sample[i] = 0.0f;
        }
        Random random = new Random();
        for (int i = index; i < sample.length; i++) {
            sample[i] = (float) random.nextGaussian();
        }

        return (sample);

    }

    /**  Update fields in TimeSeries object */
    public void updateFields(TimeSeries timeSeries) {
        //timeSeries.ampUnits = PhysicalUnits.NO_UNITS;
    }

    /** Returns true if this process modifies trace amplitude
     *
     * @return    true if this process modifies trace amplitude.
     */
    public boolean amplititudeModified() {

        return (true);

    }

    @Override
    public double[] apply(double dt, double[] sample) throws TimeDomainException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}	// End class GaussianFilter


