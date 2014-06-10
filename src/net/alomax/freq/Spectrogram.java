/*
 * Adapted from gov.usgs.vdx.data.wave.Spectrogram
 * by A. Lomax, 03 Jun 2013
 */
package net.alomax.freq;

//package gov.usgs.vdx.data.wave;
import net.alomax.math.Cmplx;
import net.alomax.math.TimeSeries;
import net.alomax.math.TimeSeriesFloat;

//import gov.usgs.math.Util;
//import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
/**
 * An immutable class for calculating spectrograms. User defines bin size, fft itest, and amount of overlap. The signal is windowed using a Gaussian
 * filter with user specifiable alpha-value.
 *
 * @author Peter Cervelli
 */
public class Spectrogram {

    /**
     * Axis scaling text placement type constants
     */
    public static enum FrequencyAxis {

        LINFREQ,
        LOGFREQ,
    };
    /**
     * Axis scaling text placement type
     */
    public FrequencyAxis frequencyAxisType = FrequencyAxis.LOGFREQ;
    //

    /**
     * Axis scaling text placement type constants
     */
    public static enum MethodType {

        GAUSSIAN,
        BUTTERWORTH,
        AMPLITUDE,
    };
    /**
     * Axis scaling text placement type
     */
    public MethodType methodType = MethodType.GAUSSIAN;
    //
    //public static final double MAX_NUM_FFT = 8192;
    //public static final double MAX_NUM_FFT = 4096;
    public static final double DEFAULT_TAPER_PERCENT = 25.0;
    public static final int DEFAULT_MAX_SIZE_FFT = 2048;
    //
    public static final double DEFAULT_MULTIPLIER = 20;
    public static final double REFERENCE_AMPLITUDE = 1;
    //
    protected double samplingRate;
    protected double taperPercent;
    protected double maxSizeFFT;
    protected int nFreqBins;
    protected int nTimeBins;
    protected TimeSeries timeSeries = null;
    protected int nSamplesPerSegment;
    protected int nPointsSignalFFT;
    protected int nSamplesOffsetBetweenSegments;
    protected Cmplx[][] signalFFT = null;
    protected double[] freqValues = null;
    //protected double[] window;
    protected double[][] spectraAmplitude = null;
    protected double[][] logSpectraAmplitude = null;

    /**
     * Complete constructor
     *
     */
    public Spectrogram(TimeSeries timeSeries, double taperPercent, double maxSizeFFT, int nTimeBins, int nFreqBins,
            FrequencyAxis frequencyAxisType, MethodType methodType, double freqMin, double freqMax) {

        //long startTime = System.currentTimeMillis();

        this.samplingRate = 1.0 / timeSeries.sampleInt;

        this.timeSeries = timeSeries;
        this.taperPercent = taperPercent;
        this.maxSizeFFT = maxSizeFFT;

        this.nTimeBins = nTimeBins;
        this.nFreqBins = nFreqBins;
        this.frequencyAxisType = frequencyAxisType;
        this.methodType = methodType;

        initializeSignalFFTarrays();

        if (frequencyAxisType.equals(FrequencyAxis.LINFREQ)) {
            this.freqValues = computeFrequencyBinsLinear(freqMin, freqMax);
        } else if (frequencyAxisType.equals(FrequencyAxis.LOGFREQ)) {
            this.freqValues = computeFrequencyBinsLogarithmic(freqMin, freqMax);
        }
        computeSpectraAmplitude();

        // check values
        if (signalFFT.length > 1 && taperPercent < 4.999) {
            System.out.println("WARNING: Spectrogram: taperPercent=" + taperPercent
                    + " is very small (< 5%), spectrogram may be unstable at low frequencies.");
        } else if (taperPercent > 25.001) {
            System.out.println("WARNING: Spectrogram: taperPercent=" + taperPercent
                    + " is too large (> 25%), spectrogram amplitudes may be underestimated.");
        }

        //System.out.println("DEBUG: computeSpectraAmplitude() ellapsed time = " + ((double) (System.currentTimeMillis() - startTime) / 1000.0));

    }

    /**
     * Returns the method type.
     */
    public MethodType getMethodType() {
        return methodType;
    }

    /**
     * Returns frequency axis type.
     */
    public FrequencyAxis getFrequencyAxisType() {

        return frequencyAxisType;

    }

    /**
     * Returns number of freqValues bins, which is equal to the number of rows in the "spectraAmplitude" array.
     */
    public int getNFreqBins() {

        return nFreqBins;

    }

    /**
     * Returns number of time bins, which is equal to the number of columns in the "spectraAmplitude" array.
     */
    public int getNTimeBins() {

        return nTimeBins;

    }

    /**
     * Returns the user defined sampling rate.
     */
    public double getSamplingRate() {

        return samplingRate;

    }

    /**
     * Returns the FFT itest.
     */
    public int getNfft() {

        return nPointsSignalFFT;

    }

    /**
     * Returns an array of freqValues values corresponding to the rows of the "spectraAmplitude" array.
     */
    public double[] getFreqValues() {

        return freqValues;

    }

    /**
     * Returns minimum of the freqValues values corresponding to the rows of the "spectraAmplitude" array.
     *
     * @return
     */
    public double getFreqMin() {

        if (freqValues == null || freqValues.length < 1) {
            return (Double.NaN);
        }

        return freqValues[0];

    }

    /**
     * Returns minimum of the freqValues values corresponding to the rows of the "spectraAmplitude" array.
     *
     * @return
     */
    public double getFreqMax() {

        if (freqValues == null || freqValues.length < 1) {
            return (Double.NaN);
        }

        return freqValues[freqValues.length - 1];

    }

    /**
     * Returns a String specifying the frequency units.
     */
    public String getFreqUnits() {

        return ("Hz");

    }

    /**
     * Returns an array of spectra amplitudes.
     */
    public double[][] getSpectraAmplitude() {

        if (spectraAmplitude == null) {
            computeSpectraAmplitude();
        }
        return spectraAmplitude;

    }

    /**
     * Returns the minimum value of the "spectraAmplitude" array.
     */
    public double getMinSpectraAmplitude() {

        double[][] specAmp = getSpectraAmplitude();

        double specMin = Double.MAX_VALUE;
        for (int i = 0; i < nFreqBins; i++) {
            for (int j = 0; j < nTimeBins; j++) {
                if (specAmp[j][i] < specMin) {
                    specMin = specAmp[j][i];
                }
            }
        }
        return specMin;

    }

    /**
     * Returns the maximum value of the "spectraAmplitude" array.
     */
    public double getMaxSpectraAmplitude() {

        double[][] specAmp = getSpectraAmplitude();

        int imax = -1, jmax = -1;

        double specMax = Double.MIN_VALUE;
        for (int i = 0; i < nFreqBins; i++) {
            for (int j = 0; j < nTimeBins; j++) {
                if (specAmp[j][i] > specMax) {
                    specMax = specAmp[j][i];
                    imax = i;
                    jmax = j;
                }
            }
        }
        //System.out.println("DEBUG: specMax=" + specMax + " nt,nf=" + jmax + "," + imax);
        return specMax;

    }

    /**
     * Returns an array of log10 of the spectra amplitudes times the default multiplier.
     */
    public double[][] getLogSpectraAmplitude() {

        return getLogSpectraAmplitude(DEFAULT_MULTIPLIER, REFERENCE_AMPLITUDE);

    }

    /**
     * Returns an array of log10 of the scaled spectra amplitudes times the specified multiplier.
     *
     * @param multiplier Multiplier
     */
    public double[][] getLogSpectraAmplitude(double multiplier, double reference_amplitude) {

        if (logSpectraAmplitude == null) {
            computeLogSpectraAmplitude(multiplier, reference_amplitude);
        }

        return logSpectraAmplitude;
    }

    /**
     * Returns the minimum value of the "spectraAmplitude" array.
     */
    public double getMinLogSpectraAmplitude() {

        double[][] specAmp = getLogSpectraAmplitude();

        double specMin = Double.MAX_VALUE;
        for (int i = 0; i < nFreqBins; i++) {
            for (int j = 0; j < nTimeBins; j++) {
                if (specAmp[j][i] < specMin) {
                    specMin = specAmp[j][i];
                }
            }
        }
        return specMin;

    }

    /**
     * Returns the maximum value of the "spectraAmplitude" array.
     */
    public double getMaxLogSpectraAmplitude() {

        double[][] specAmp = getLogSpectraAmplitude();

        int imax = -1, jmax = -1;

        double specMax = Double.MIN_VALUE;
        for (int i = 0; i < nFreqBins; i++) {
            for (int j = 0; j < nTimeBins; j++) {
                if (specAmp[j][i] > specMax) {
                    specMax = specAmp[j][i];
                    imax = i;
                    jmax = j;
                }
            }
        }
        //System.out.println("DEBUG: specMax=" + specMax + " nt,nf=" + jmax + "," + imax);
        return specMax;

    }

    /**
     * Initialize the FFTs array
     */
    protected void initializeSignalFFTarrays() {

        int nSegments = 0;
        int itest = timeSeries.getSampleAsFloat().length;
        while (itest > 0) {
            itest -= maxSizeFFT;
            nSegments++;
        }
        nSamplesPerSegment = timeSeries.getSampleAsFloat().length / nSegments;
        int numSignalFFT = 2 * nSegments - 1;
        signalFFT = new Cmplx[numSignalFFT][];
        nSamplesOffsetBetweenSegments = nSamplesPerSegment / 2;
        // find power of two >= to num of points in fdata
        nPointsSignalFFT = 1;
        while (nPointsSignalFFT < nSamplesPerSegment) {
            nPointsSignalFFT *= 2;
        }



        //System.out.println("DEBUG: timeSeries.getSampleAsFloat().length=" + timeSeries.getSampleAsFloat().length);
        //System.out.println("DEBUG: numSignalFFT=" + numSignalFFT);
        //System.out.println("DEBUG: nSamplesPerSegment=" + nSamplesPerSegment);
        //System.out.println("DEBUG: nSamplesOffsetBetweenSegments=" + nSamplesOffsetBetweenSegments);

    }

    /**
     * Computes the freqValues array
     */
    protected final double[] computeFrequencyBinsLinear(double freqMin, double freqMax) {

        double[] omega = new double[nFreqBins];
        double omega_max = samplingRate / 2.0; // highest freq in fft
        double omega_min = samplingRate / (2.0 * (double) nPointsSignalFFT); // lowest freq in fft
        if (freqMin > 0.0 && freqMax > 0.0) {
            omega_min = freqMin;
            omega_max = freqMax;
        }
        double delta = (omega_max - omega_min) / (double) (nFreqBins - 1);      // delta freq for spectrogram rows
        double omega_sum = omega_min;
        for (int i = 0; i < nFreqBins; i++) {
            omega[i] = omega_sum;
            //System.out.println("DEBUG: lin i=" + i + " omega[i]=" + omega[i]);
            omega_sum += delta;
        }
        return omega;
    }

    /**
     * Computes the freqValues array
     */
    protected final double[] computeFrequencyBinsLogarithmic(double freqMin, double freqMax) {

        double[] omega = new double[nFreqBins];
        double omega_max = Math.log10(samplingRate / 2.0); // highest freq in fft
        double omega_min = Math.log10(samplingRate / (2.0 * (double) nPointsSignalFFT)); // lowest freq in fft
        if (freqMin > 0.0 && freqMax > 0.0) {
            omega_min = Math.log10(freqMin);
            omega_max = Math.log10(freqMax);
        }
        double delta = (omega_max - omega_min) / (double) (nFreqBins - 1);      // delta freq for spectrogram rows
        //System.out.println("DEBUG: log delta=" + delta);
        double omega_sum = omega_min;
        for (int i = 0; i < nFreqBins; i++) {
            omega[i] = Math.pow(10.0, omega_sum);
            //System.out.println("DEBUG: log i=" + i + " omega[i]=" + omega[i]);
            omega_sum += delta;
        }
        return omega;
    }

    /**
     * Computes the spectra amplitudes with the FFT.
     */
    protected final void computeSpectraAmplitude() {

        // initializations
        spectraAmplitude = new double[nTimeBins][nFreqBins];
        double dtime = 1.0 / samplingRate;
        double columnIndex2signalIndex = (double) (timeSeries.getSampleAsFloat().length - 1) / (double) (nTimeBins - 1);
        double referenceFrequency = freqValues[(freqValues.length - 1) / 2];
        if (frequencyAxisType.equals(FrequencyAxis.LOGFREQ)) {
            referenceFrequency = freqValues[(freqValues.length - 1)];
        }

        // local arrays and objects
        Cmplx[] workFFT = new Cmplx[nPointsSignalFFT];
        float[] signalSamples = timeSeries.getSampleAsFloat();
        float[] samples = new float[nSamplesPerSegment];
        TimeSeries timeSeriesCopy = null;
        FrequencyProcesses envelope = new FrequencyProcesses(FrequencyProcesses.ENVELOPE);

        for (int irow = 0; irow < nFreqBins; irow++) {

            int lastColumnEnd = -1;

            for (int nSignalFFT = 0; nSignalFFT < signalFFT.length; nSignalFFT++) {

                //System.out.println("DEBUG: nSignalFFT=" + nSignalFFT);

                // set start and end time bins (columns)
                int indexStart = nSignalFFT * nSamplesOffsetBetweenSegments;    // signal window start index in signal timeseries
                int ioffset = nSignalFFT > 0 ? nSamplesPerSegment / 4 : 0;      // offset forward to start of usable part of signal window
                int nColumnStart = (int) Math.ceil((double) (indexStart + ioffset) / columnIndex2signalIndex);  // start time bin
                ioffset = nSignalFFT < signalFFT.length - 1 ? nSamplesPerSegment / 4 : 0;      // offset back to end of usable part of signal window
                int nColumnEnd = (int) Math.floor((double) (indexStart + (nSamplesPerSegment - 1) - ioffset) / columnIndex2signalIndex);
                if (nColumnStart <= lastColumnEnd) {    // start must be after last time bin computed
                    nColumnStart = lastColumnEnd + 1;
                }
                //System.out.print("DEBUG: nColumnStart=" + nColumnStart + "  nColumnEnd=" + nColumnEnd + "  lastColumnEnd=" + lastColumnEnd + "  nTimeBins=" + nTimeBins);
                if (nColumnStart > nColumnEnd) {    // start must be at or before end
                    //System.out.println(" -> skip");
                    continue;
                }

                // calculate signal segment FFT if not already done
                if (signalFFT[nSignalFFT] == null) {
                    if (timeSeriesCopy == null) {
                        timeSeriesCopy = new TimeSeriesFloat(samples, timeSeries.sampleInt, timeSeries.lagTime, timeSeries.ampUnits, timeSeries.timeUnits);
                    }
                    int length = nSamplesPerSegment;
                    if (indexStart + length > signalSamples.length) {
                        length = signalSamples.length - length;
                    }
                    System.arraycopy(signalSamples, indexStart, samples, 0, length);
                    timeSeriesCopy.cosineTaper(taperPercent);
                    signalFFT[nSignalFFT] = Cmplx.fft(timeSeriesCopy.getSampleAsFloat());
                }

                // copy signal segment FFT to working array
                for (int n = 0; n < signalFFT[nSignalFFT].length; n++) {
                    workFFT[n] = new Cmplx(signalFFT[nSignalFFT][n]);
                }

                // set filter method
                FrequencyDomainProcess filter = null;
                if (methodType.equals(MethodType.GAUSSIAN)) {
                    // apply gaussian filter at required freq for this row
                    double alpha = 100.0;
                    //filter = new GaussianFilter(freqValues[irow], alpha);
                    //filter = new GaussianFilter(freqValues[irow], alpha * (freqValues[irow] / referenceFrequency) * (freqValues[irow] / referenceFrequency));
                    filter = new GaussianFilter(freqValues[irow], alpha * (freqValues[irow] / referenceFrequency));
                } else if (methodType.equals(MethodType.BUTTERWORTH)) {
                    //double factor = 1.0 + (referenceFrequency / freqValues[irow]) / 15.0;
                    //double factor = 1.0 + referenceFrequency / 15.0;
                    double factor = 1.3;
                    filter = new ButterworthFilter(freqValues[irow] / factor, freqValues[irow] * factor, 8);
                } else if (methodType.equals(MethodType.AMPLITUDE)) {
                    //double factor = 1.0 + (referenceFrequency / freqValues[irow]) / 15.0;
                    //double factor = 1.0 + referenceFrequency / 15.0;
                    double factor = 1.3;
                    filter = new AmplitudeFilter(freqValues[irow] / factor, freqValues[irow] * factor, 8);
                }

                //if (irow == 10) {
                //System.out.println("DEBUG: irow=" + irow + " freqValues[irow]=" + freqValues[irow] + " alpha=" + alpha);
                //}
                // apply filter method
                filter.apply(dtime, workFFT);

                // apply envelope function
                float[] invFFTgaussian = Cmplx.fftInverse(workFFT, nSamplesPerSegment);
                envelope.preProcess(dtime, invFFTgaussian);
                envelope.apply(dtime, workFFT);
                float[] invFFT = Cmplx.fftInverse(workFFT, nSamplesPerSegment);
                envelope.postProcess(dtime, invFFT);

                // calculate amplitude for required time bins (columns)
                //for (int icol = 0; icol < nTimeBins; icol++) {
                for (int icol = nColumnStart; icol <= nColumnEnd; icol++) {
                    int index = (int) ((double) icol * columnIndex2signalIndex) - indexStart;
                    if (index > 0 && index < invFFT.length) {
                        spectraAmplitude[icol][irow] = Math.abs(invFFT[index]);
                        lastColumnEnd = icol;
                        //System.out.println(" -> PLOT");
                    } else {
                        //System.out.println(" -> no plot: n=" + index + "/" + invFFT.length);
                    }
                    //if (icol == 50) {
                    //    System.out.println("DEBUG: (" + icol + "," + irow + ") sa=" + spectraAmplitude[icol][irow] + " icol=" + icol + " index=" + index);
                    //}
                }
            }

        }

    }

    /**
     * Computes the spectra amplitudes with the FFT.
     */
    protected final void computeLogSpectraAmplitude(double multiplier, double reference_amplitude) {

        double minSpecAmpCutoff = getMaxSpectraAmplitude() / 1.0e6;
        double specAmp;

        logSpectraAmplitude = new double[nTimeBins][nFreqBins];
        for (int i = 0; i < nFreqBins; i++) {
            for (int j = 0; j < nTimeBins; j++) {
                specAmp = Math.max(minSpecAmpCutoff, spectraAmplitude[j][i]);
                logSpectraAmplitude[j][i] = multiplier * Math.log10(specAmp / reference_amplitude);
            }
        }

    }
}
