
package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Local Probability of Connectivity.
 * PC index for one patch.
 * 
 * @author Gilles Vuidel
 */
public class FPCLocalMetric extends LocalMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        double srcCapa = Project.getPatchCapacity(node);
        GraphPathFinder pathFinder = gen.getPathFinder(node);
        double sum = 0;
        for(Node n : pathFinder.getComputedNodes()) {
            sum += Math.exp(-alphaParam.getAlpha() * pathFinder.getCost(n)) * Math.pow(srcCapa * Project.getPatchCapacity(n), alphaParam.getBeta());            
        }            
        
        return sum / Math.pow(gen.getProject().getArea(), 2);
    }

    @Override
    public String getShortName() {
        return "FPC";
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
