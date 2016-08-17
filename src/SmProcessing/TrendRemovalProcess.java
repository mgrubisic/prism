/*******************************************************************************
 * Name: Java class TrendRemovalProcess.java
 * Project: PRISM strong motion record processing using COSMOS data format
 * Written by: Jeanne Jones, USGS, jmjones@usgs.gov
 * 
 * This software is in the public domain because it contains materials that 
 * originally came from the United States Geological Survey, an agency of the 
 * United States Department of Interior. For more information, see the official 
 * USGS copyright policy at 
 * http://www.usgs.gov/visual-id/credit_usgs.html#copyright
 * 
 * Date: first release date Feb. 2015
 ******************************************************************************/
package SmProcessing;

import SmConstants.VFileConstants;
import SmException.SmException;
import java.util.Arrays;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

/**
 *
 * @author jmjones
 */
public class TrendRemovalProcess {
    private final int startIndex;
    private double[] velocity;
    private double preEventMean;
    private int trendRemovalOrder;
    
    public TrendRemovalProcess(int startInd) {
        this.startIndex = startInd;
    }
    public double[] removeTrends(double[] accel, double dtime) throws SmException {
        if (startIndex > 0) {
            double[] subset = Arrays.copyOfRange( accel, 0, startIndex );
            ArrayStats accsub = new ArrayStats( subset );
            preEventMean = accsub.getMean();
            ArrayOps.removeValue(accel, preEventMean);
        }
        //Integrate the acceleration to get velocity, using 0 as first value estimate
        velocity = ArrayOps.Integrate( accel, dtime, 0.0);
        //Now correct for unknown initial value by removing preevent mean (minus first val.)
        ArrayOps.CorrectForZeroInitialEstimate( velocity, startIndex );

        //Find any linear or 2nd order polynomial trend in velocity
        //get derivative of trend and remove this from acc
        double[] tcoefs = ArrayOps.findTrendWithBestFit( velocity, dtime);
        PolynomialFunction trendpoly = new PolynomialFunction( tcoefs );
        trendRemovalOrder = trendpoly.degree() - 1; //order of acc's trend
        PolynomialFunction diffPoly = trendpoly.polynomialDerivative();
        boolean trendSuccess = ArrayOps.removePolynomialTrend(accel, 
                                                diffPoly.getCoefficients(), dtime);
//        for ( double coval : tcoefs ) {
//            System.out.println("tcoefs: " + coval);
//        }
//        for ( double coval : diffPoly.getCoefficients()) {
//            System.out.println("dcoefs: " + coval);
//        }
        if (!trendSuccess) {
            throw new SmException("Unable to remove best fit differentiated trend from acceleration.");
        } else {
            velocity = ArrayOps.Integrate(accel, dtime, 0.0);
        }
        return velocity;
    }
    public double getPreEventMean() { return preEventMean; }
    public int getTrendRemovalOrder() { return trendRemovalOrder; }
}
