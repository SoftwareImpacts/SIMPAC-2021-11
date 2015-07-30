
package org.thema.graphab.model;

import java.util.Arrays;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

/**
 * Calculates a multivariate logistic regression.
 * 
 * @author Gilles Vuidel
 */
public class Logistic {

    /**
     * Represents a multivariate logistic function
     */
    public static class LogisticFunction implements MultivariateRealFunction {
        private RealVector beta;

        public LogisticFunction(double[] beta) {
            this.beta = MatrixUtils.createRealVector(beta);
        }

        @Override
        public double value(double[] point) {
            return 1 / (1 + Math.exp(-beta.dotProduct(point)));
        }
    }

    private static int maxIter = 500;
    private static double epsilon = 1e-10;

    private RealVector params;

    private RealMatrix A;
    private RealVector Y;
    private int nVar, n;

    private LogisticFunction estim;
    private Logistic constLog;

    /**
     * Creates a new logistic regression
     * @param a the matrix of explained variables
     * @param y the binary variable (0 or 1) to explain
     */
    public Logistic(double[][] a, double [] y) {
        Y = MatrixUtils.createRealVector(y);
        nVar = a[0].length;
        n = a.length;

        A = MatrixUtils.createRealMatrix(n, nVar+1);
        A.setSubMatrix(a, 0, 1);
        for(int i = 0; i < n; i++) {
            A.setEntry(i, 0, 1);
        }
    }

    private Logistic(double [] y) {
        Y = MatrixUtils.createRealVector(y);
        nVar = 0;
        n = y.length;

        A = MatrixUtils.createRealMatrix(n, 1);
        for(int i = 0; i < n; i++) {
            A.setEntry(i, 0, 1);
        }
    }

    /**
     * @return the coefficients of the logistic function, the first is the constant
     */
    public double[] getCoefs() {
        return params.getData();
    }
    
    /**
     * Calculates the logisitic regression and returns the coefficients
     * @return
     * @throws FunctionEvaluationException 
     */
    public double [] regression() throws FunctionEvaluationException  {
       
        RealVector X = new ArrayRealVector(nVar+1);

        double [] tmp = new double[nVar+1];
        Arrays.fill(tmp, 1e-5);
        RealMatrix ridge = MatrixUtils.createRealDiagonalMatrix(tmp);

        RealVector oldExpY = null;

        for(int i = 0; i < maxIter; i++) {
            RealVector adjY = A.operate(X);
            RealVector expY = adjY.copy();
            expY.mapToSelf(new UnivariateRealFunction() {
                @Override
                public double value(double x)  {
                    return 1 / (1 + Math.exp(-x));
                }
            });
            RealVector deriv = expY.copy();
            deriv.mapToSelf(new UnivariateRealFunction() {
                @Override
                public double value(double x) {
                    return x * (1-x);
                }
            });
            RealMatrix wadjY = MatrixUtils.createColumnRealMatrix(deriv.ebeMultiply(adjY).add(Y.subtract(expY)).getData());
            RealMatrix weights = MatrixUtils.createRealDiagonalMatrix(deriv.getData());

            RealMatrix res = A.transpose().multiply(weights).multiply(A).add(ridge).inverse().multiply(A.transpose()).multiply(wadjY);
            X = res.getColumnVector(0);

            if(oldExpY != null) {
                double err = expY.subtract(oldExpY).getL1Norm();
                if(Double.isNaN(err) || err < n*epsilon) {
                    break;
                }
            }
            oldExpY = expY;
        } 

        params = X;
        estim = new LogisticFunction(params.getData());

        if(nVar > 0) {
            constLog = new Logistic(Y.getData());
            constLog.regression();
        }

        return X.getData();
    }

    public LogisticFunction getEstimFunction() {
        return estim;
    }

    public double [] getEstimation() {
        double [] y = new double[n];
        for(int i = 0; i < n; i++) {
            y[i] = estim.value(A.getRow(i));
        }
        return y;
    }

    public double getLikelihood() {
        double prod = 1;
        for(int i = 0; i < n; i++) {
            prod *= Math.pow(estim.value(A.getRow(i)), Y.getEntry(i)) * Math.pow(1-estim.value(A.getRow(i)), 1-Y.getEntry(i));
        }
        return prod;
    }

    public double getDiffLikelihood() {
        return -2 * Math.log(constLog.getLikelihood() / getLikelihood());
    }

    public double getProbaTest() throws MathException {
        if(Double.isInfinite(getDiffLikelihood())) {
            return Double.NaN;
        }
        return 1 - new ChiSquaredDistributionImpl(nVar).cumulativeProbability(getDiffLikelihood());
    }

    public double getR2() {
        return 1 - Math.log(getLikelihood()) / Math.log(constLog.getLikelihood());
    }

    public double getAIC() {
        return 2 * nVar - 2 * Math.log(getLikelihood());
    }

}
