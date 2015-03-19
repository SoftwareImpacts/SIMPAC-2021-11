/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author gvuidel
 */
public class Logistic {

    public static class LogisticFunction implements MultivariateRealFunction {
        RealVector beta;

        public LogisticFunction(double[] beta) {
            this.beta = MatrixUtils.createRealVector(beta);
        }

        public double value(double[] point) {
            return 1 / (1 + Math.exp(-beta.dotProduct(point)));
        }
    }

    public static int maxIter = 500;
    public static double epsilon = 1e-10;

    RealVector params;

    RealMatrix A;
    RealVector Y;
    int nVar, n;

    private LogisticFunction estim;
    private Logistic constLog;

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

    public double[] getCoefs() {
        return params.getData();
    }
    
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
                public double value(double x)  {
                    return 1 / (1 + Math.exp(-x));
                }
            });
            RealVector deriv = expY.copy();
            deriv.mapToSelf(new UnivariateRealFunction() {
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

    public double [] getEstimation() {
        LogisticFunction func = new LogisticFunction(params.getData());
        double [] y = new double[n];
        for(int i = 0; i < n; i++) {
            y[i] = func.value(A.getRow(i));
        }
        return y;
    }

    private LogisticFunction getEstimFunction() {
        return estim;
    }

    public double estim(double [] x) {
        double [] xc = new double[nVar+1];
        xc[0] = 1;
        for(int i = 1; i < xc.length; i++) {
            xc[i] = x[i-1];
        }
        return getEstimFunction().value(xc);
    }

    private double getLikelihood(double [] beta) {
        LogisticFunction func = new LogisticFunction(beta);
        double prod = 1;

        for(int i = 0; i < n; i++) {
            prod *= Math.pow(func.value(A.getRow(i)), Y.getEntry(i)) * Math.pow(1-func.value(A.getRow(i)), 1-Y.getEntry(i));
        }

        return prod;
    }

    public double getLikelihood() {
        return getLikelihood(params.getData());
    }

    public double getDiffLikelihood() {
//        double [] betaConst = new double[params.getDimension()];
//        betaConst[0] = params.getEntry(0);
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
        return 2 * nVar - 2 * Math.log(getLikelihood(params.getData()));
    }

    public static void main(String [] args) throws Exception {
        double [][] x = new double [][]
            {{    1},
            {    2},
            {    3},
            {    4},
            {    5},
            {    6},
            {    7},
            {    8},
            {    9},
            {   10}};
        double [] y = new double [] {0,   0,   0,   0,   1,   0,   1,   0,   1,   1};
        Logistic log = new Logistic(x, y);
        double [] coef = log.regression();
        
        System.out.println("Attendu  : -4.35746  0.66216");
        System.out.println("intercept : " + coef[0] + " - val : " + coef[1]);
        System.out.println("Likelihood  : " + log.getLikelihood());
        System.out.println("p  : " + log.getProbaTest());
        System.out.println("AIC  : " + log.getAIC());
    }
}
