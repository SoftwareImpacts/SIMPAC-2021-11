
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
 * Probability of Connectivity metric.
 * 
 * @author Gilles Vuidel
 */
public class PCMetric extends AbstractPathMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public Double calcPartMetric(PathFinder finder, GraphGenerator g) {
        double sum = 0;
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        for(Node node : finder.getComputedNodes()) {
            sum += Math.pow(srcCapa * Project.getPatchCapacity(node), alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha()*finder.getCost(node));
        }
        return sum;
    }

    @Override
    public void mergePart(Object part) {
        metric += (Double)part;
    }

    @Override
    public void endCalc(GraphGenerator g) {
        metric = metric / Math.pow(Project.getArea(), 2);       
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
    public Type getType() {
        return Type.WEIGHT;
    }
}
