
package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Flux metric.
 * 
 * @author Gilles Vuidel
 */
public class FLocalMetric extends LocalMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        PathFinder pathFinder = gen.getPathFinder((Node)g);
        double sum = 0;
        for(Node node : pathFinder.getComputedNodes()) {
            if (node != g) {
                sum += Math.exp(-alphaParam.getAlpha() * pathFinder.getCost(node)) * Math.pow(Project.getPatchCapacity(node), alphaParam.getBeta());            
            }
        }            
        
        return sum;
    }

    @Override
    public String getShortName() {
        return "F";
    }

    @Override
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
    public Type getType() {
        return Type.WEIGHT;
    }
}
