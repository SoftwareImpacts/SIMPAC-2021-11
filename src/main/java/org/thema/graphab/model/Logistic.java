/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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

        /**
         * Creates a new LogisticFunction
         * @param beta the coefficients, first is the constant
         */
        public LogisticFunction(double[] beta) {
            this.beta = MatrixUtils.createRealVector(beta);
        }

        @Override
        public double value(double[] x) {
            return 1 / (1 + Math.exp(-beta.dotProduct(x)));
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
     * The method {@link #regression() } must be called before
     * @return the coefficients of the logistic function, the first is the constant
     */
    public double[] getCoefs() {
        return params.getData();
    }
    
    /**
     * Calculates the logisitic regression and returns the coefficients
     * @return the coefficients of the logistic function
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

    /**
     * The method {@link #regression() } must be called before
     * @return the estimated function
     */
    public LogisticFunction getEstimFunction() {
        return estim;
    }

    /**
     * The method {@link #regression() } must be called before
     * @return the estimated values (ŷ)
     */
    public double [] getEstimation() {
        double [] y = new double[n];
        for(int i = 0; i < n; i++) {
            y[i] = estim.value(A.getRow(i));
        }
        return y;
    }

    /**
     * The method {@link #regression() } must be called before
     * @return the likelihood of the regression
     */
    public double getLikelihood() {
        double prod = 1;
        for(int i = 0; i < n; i++) {
            prod *= Math.pow(estim.value(A.getRow(i)), Y.getEntry(i)) * Math.pow(1-estim.value(A.getRow(i)), 1-Y.getEntry(i));
        }
        return prod;
    }
        
    public double getLogLikelihood() {
        double sum = 0;
        for(int i = 0; i < n; i++) {
            double v = Math.log(estim.value(A.getRow(i)))*Y.getEntry(i);
            if(!Double.isNaN(v)) {
                sum += v;
            }
            v = Math.log(1-estim.value(A.getRow(i)))*(1-Y.getEntry(i));
            if(!Double.isNaN(v)) {
                sum += v;
            }
        }
        return sum;
    }

    /**
     * The method {@link #regression() } must be called before
     * @return the likelihood ratio
     */
    public double getDiffLikelihood() {
//        return -2 * Math.log(constLog.getLikelihood() / getLikelihood());
        return -2 * (constLog.getLogLikelihood() - getLogLikelihood());
    }

    /**
     * The method {@link #regression() } must be called before
     * @return the p-value of the chi square test
     */
    public double getProbaTest() throws MathException {
        if(Double.isInfinite(getDiffLikelihood())) {
            return Double.NaN;
        }
        return 1 - new ChiSquaredDistributionImpl(nVar).cumulativeProbability(getDiffLikelihood());
    }

    /**
     * The method {@link #regression() } must be called before
     * @return the r square of the regression
     */
    public double getR2() {
//        return 1 - Math.log(getLikelihood()) / Math.log(constLog.getLikelihood());
        return 1 - getLogLikelihood() / constLog.getLogLikelihood();
    }

    /**
     * The method {@link #regression() } must be called before
     * @return the AIC of the regression
     */
    public double getAIC() {
//        return 2 * nVar - 2 * Math.log(getLikelihood());
        return 2 * nVar - 2 * getLogLikelihood();
    }

}
