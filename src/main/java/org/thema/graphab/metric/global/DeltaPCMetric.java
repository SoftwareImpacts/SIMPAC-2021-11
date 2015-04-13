
package org.thema.graphab.metric.global;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.DeltaGraphGenerator;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * PC decomposition for delta method.
 * This metric works only in delta mode, for calculating the decomposition of the PC metric:
 * dPCIntra, dPCFlux, dPCConnector
 * @author Gilles Vuidel
 */
public class DeltaPCMetric extends AbstractPathMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public boolean isAcceptMethod(Method method) {
        return method == Method.DELTA;
    }

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
    public Double[] calcMetric(GraphGenerator g) {
        // if an element of the graph has been removed 
        // calculates Intra and Flux for the node
        if(g instanceof DeltaGraphGenerator && ((DeltaGraphGenerator)g).getRemovedElem() != null) {
            Graphable remElem = ((DeltaGraphGenerator)g).getRemovedElem();
            double intra = 0, flux = 0;
            if(remElem instanceof Node) {
                Node remNode = (Node) remElem;
                intra = Math.pow(Project.getPatchCapacity(remNode), 2);
                GraphGenerator parentGraph = ((DeltaGraphGenerator)g).getParentGraph();
                PathFinder pathFinder = parentGraph.getPathFinder(parentGraph.getNode((Feature) remNode.getObject()));
                double srcCapa = Project.getPatchCapacity(remNode);
                for(Node node : pathFinder.getComputedNodes()) {
                    if (node.getObject() != remNode.getObject()) {
                        flux += Math.pow(srcCapa * Project.getPatchCapacity(node), alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha()*pathFinder.getCost(node));
                    }
                }
                
                flux = 2 * flux;
            }
            return new Double[] {intra, flux, metric+intra+flux};
        } else {
            // no element has been removed from the graph
            // return only the PC value
            return new Double[] {null, null, metric};
        }
    }

    @Override
    public String[] getResultNames() {
        return new String[] {"PCIntra", "PCFlux", "PCCon"};
    }

    @Override
    public String getShortName() {
        return "dPC";
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
