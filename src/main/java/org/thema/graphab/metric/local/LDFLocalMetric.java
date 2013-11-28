/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.drawshape.feature.Feature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.Project;
import org.thema.graphab.metric.DistProbaPanel;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.local.LocalMetric;

/**
 *
 * @author gvuidel
 */
public class LDFLocalMetric extends LocalMetric {

    private double k = 0.0029957322735539907;
    private double d = 1000;
    private double p = 0.05;
    private double a = 1.0;


    public double calcIndice(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        PathFinder pathFinder = gen.getPathFinder(node);
        double sum = 0;
        for(Node n : gen.getNodes()) {
            Double cost = pathFinder.getCost(n);
            if(cost != null && cost != 0) 
                sum += (1 - Math.exp(-k * cost)) * Math.pow(Project.getPatchCapacity(n), a);
        }
        return sum;
    }


    public String getShortName() {
        return "LDF";
    }

    public void setParams(Map<String, Object> params) {
        d = ((Number)params.get(DistProbaPanel.DIST)).doubleValue();
        p = ((Number)params.get(DistProbaPanel.PROBA)).doubleValue();
        a = ((Number)params.get(DistProbaPanel.A)).doubleValue();
        k = -Math.log(p) / d;
    }

    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(DistProbaPanel.DIST, d);
        params.put(DistProbaPanel.PROBA, p);
        params.put(DistProbaPanel.A, a);
        return params;
    }

    public boolean calcNodes() {
        return true;
    }

    public boolean calcEdges() {
        return false;
    }

    public ParamPanel getParamPanel(Project project) {
        return new DistProbaPanel(d, p, a);
    }

    @Override
    public void setParamFromDetailName(String detailName) {
        a = Double.parseDouble(detailName.substring(detailName.indexOf(DistProbaPanel.A) + DistProbaPanel.A.length(), detailName.indexOf(DistProbaPanel.DIST)));
        d = Double.parseDouble(detailName.substring(detailName.indexOf(DistProbaPanel.DIST) + DistProbaPanel.DIST.length(), detailName.indexOf(DistProbaPanel.PROBA)));
        p = Double.parseDouble(detailName.substring(detailName.indexOf(DistProbaPanel.PROBA) + DistProbaPanel.PROBA.length()));
        k = -Math.log(p) / d;
    }    
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
