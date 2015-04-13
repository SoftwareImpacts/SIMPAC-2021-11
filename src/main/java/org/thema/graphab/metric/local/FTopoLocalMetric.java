
package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Flux metric, topological version.
 * Does not calculate path, use only edges connected to the node.
 * 
 * @author Gilles Vuidel
 */
public class FTopoLocalMetric extends LocalMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        double sum = 0;
        for(Edge edge : (List<Edge>)node.getEdges()) {
            sum += Math.exp(-alphaParam.getAlpha() * gen.getCost(edge)) * Math.pow(Project.getPatchCapacity(node), alphaParam.getBeta());            
        }
        return sum;
    }


    @Override
    public String getShortName() {
        return "FTopo";
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
    public boolean calcNodes() {
        return true;
    }

    @Override
    public ParamPanel getParamPanel(Project project) {
        return alphaParam.getParamPanel(project);
    } 
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getLinkset().getTopology() == Linkset.COMPLETE;
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
