/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 *
 * @author gvuidel
 */
public class PCMetric extends AbstractPathMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public Double calcPartIndice(PathFinder finder, GraphGenerator g) {
        double sum = 0;
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        for(Node node : finder.getComputedNodes()) {
            sum += Math.pow(srcCapa * Project.getPatchCapacity(node), alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha()*finder.getCost(node));
        }
        return sum;
    }

    @Override
    public void mergePart(Object part) {
        indice += (Double)part;
    }

    @Override
    public void endCalc(GraphGenerator g) {
        indice = indice / Math.pow(Project.getArea(), 2);       
    }
    
    @Override
    public String getShortName() {
        return "PC";
    }
    
    public void setParams(double d, double p, double beta) {
        alphaParam.setParams(d, p, beta);
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
