/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.Project;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 *
 * @author gvuidel
 */
public class FPCLocalMetric extends LocalMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public double calcIndice(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        double srcCapa = Project.getPatchCapacity(node);
        PathFinder pathFinder = gen.getPathFinder(node);
        double sum = 0;
        for(DijkstraPathFinder.DijkstraNode n : pathFinder.getComputedNodes()) 
            sum += Math.exp(-alphaParam.getAlpha() * n.cost) * Math.pow(srcCapa * Project.getPatchCapacity(n.node), alphaParam.getBeta());            
        
        return sum / Math.pow(Project.getArea(), 2);
    }

    @Override
    public String getShortName() {
        return "FPC";
    }

    public boolean calcNodes() {
        return true;
    }
    
    @Override
    public void setParams(Map<String, Object> params) {
        alphaParam.setParams(params);
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        return alphaParam.getParams();
    }

    @Override
    public ParamPanel getParamPanel(Project project) {
        return alphaParam.getParamPanel(project);
    }

    @Override
    public void setParamFromDetailName(String detailName) {
        alphaParam.setParamFromDetailName(detailName);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
