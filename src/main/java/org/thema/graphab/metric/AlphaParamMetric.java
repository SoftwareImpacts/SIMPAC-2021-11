
package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.thema.graphab.Project;

/**
 * Class for delegating parameter management for metrics having parameters :
 * d, p, beta 
 * and alpha = -Math.log(p) / d;
 * 
 * @author Gilles Vuidel
 */
public final class AlphaParamMetric implements Serializable {
    
    public static final String DIST = "d";
    public static final String PROBA = "p";
    public static final String BETA = "beta";
    
    private double alpha = 0.0029957322735539907;
    private double d = 1000;
    private double p = 0.05;
    private double beta = 1.0;

    /**
     * @return alpha = -Math.log(p) / d;
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * @return beta the exponent of the capacity
     */
    public double getBeta() {
        return beta;
    }
    
    /**
     * Set the parameters and calculate alpha
     * @param d the distance
     * @param p the probability
     * @param beta the exponent of the capacity
     */
    public void setParams(double d, double p, double beta) {
        this.d = d;
        this.p = p;
        this.beta = beta;
        alpha = getAlpha(d, p);
    }
    
    /**
     * Set the parameters and calculate alpha
     * @param params map containing the 3 parameters
     */
    public void setParams(Map<String, Object> params) {
        if(params.containsKey(DIST)) {
            d = ((Number)params.get(DIST)).doubleValue();
        } else {
            throw new IllegalArgumentException("Parameter " + DIST + " not found");
        }
        if(params.containsKey(PROBA)) {
            p = ((Number)params.get(PROBA)).doubleValue();
        } else {
            throw new IllegalArgumentException("Parameter " + PROBA + " not found");
        }
        if(params.containsKey(BETA)) {
            beta = ((Number)params.get(BETA)).doubleValue();
        } else {
            throw new IllegalArgumentException("Parameter " + BETA + " not found");
        }

        alpha = getAlpha(d, p);
    }

    /**
     * @return the parameters name and value
     */
    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(DIST, d);
        params.put(PROBA, p);
        params.put(BETA, beta);
        return params;
    }
    
    /**
     * @param project not used
     * @return DistProbaPanel for editing parameters
     */
    public ParamPanel getParamPanel(Project project) {
        return new DistProbaPanel(d, p, beta);
    }
    
    /**
     * Calculates alpha exponent given distance and probability
     * @param dist distance
     * @param proba probability
     * @return alpha exponent
     */
    public static double getAlpha(double dist, double proba) {
        return -Math.log(proba) / dist;
    }
}
