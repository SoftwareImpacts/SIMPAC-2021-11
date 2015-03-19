/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public final class AlphaParamMetric implements Serializable {
    
    public static final String DIST = "d";
    public static final String PROBA = "p";
    public static final String BETA = "beta";
    
    private double alpha = 0.0029957322735539907;
    private double d = 1000;
    private double p = 0.05;
    private double beta = 1.0;

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }
    
    public void setParams(double d, double p, double beta) {
        this.d = d;
        this.p = p;
        this.beta = beta;
        alpha = -Math.log(p) / d;
    }
    
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

        alpha = -Math.log(p) / d;
    }

    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(DIST, d);
        params.put(PROBA, p);
        params.put(BETA, beta);
        return params;
    }
    
    public ParamPanel getParamPanel(Project project) {
        return new DistProbaPanel(d, p, beta);
    }

    public void setParamFromDetailName(String detailName) {
        d = Double.parseDouble(detailName.substring(detailName.indexOf(DIST) + DIST.length(), detailName.indexOf("_"+PROBA)));
        p = Double.parseDouble(detailName.substring(detailName.indexOf(PROBA) + PROBA.length(), detailName.indexOf("_"+BETA)));
        beta = Double.parseDouble(detailName.substring(detailName.indexOf(BETA) + BETA.length()));
        alpha = -Math.log(p) / d;
    }
}
