/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder.DijkstraNode;
import org.thema.graphab.graph.DeltaGraphGenerator;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.Project;
import org.thema.graphab.Project.Method;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 *
 * @author gvuidel
 */
public class DeltaPCMetric extends AbstractPathMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public boolean isAcceptMethod(Method method) {
        return method == Method.DELTA;
    }

    @Override
    public Double calcPartIndice(PathFinder finder, GraphGenerator g) {
        double sum = 0;
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        for(DijkstraNode node : finder.getComputedNodes()) 
            sum += Math.pow(srcCapa * Project.getPatchCapacity(node.node), alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha()*node.cost);
        
        return sum;
    }

    @Override
    public void mergePart(Object part) {
        indice += (Double)part;
    }

    @Override
    public Double[] calcIndice(GraphGenerator g) {
//        indice = indice / Math.pow(Project.getArea(), 2);
        if(g instanceof DeltaGraphGenerator && ((DeltaGraphGenerator)g).getRemovedElem() != null) {
            Graphable remElem = ((DeltaGraphGenerator)g).getRemovedElem();
            double intra = 0, flux = 0;
            if(remElem instanceof Node) {
                intra = Math.pow(Project.getPatchCapacity((Node)remElem), 2);// / Math.pow(Project.getArea(), 2);
                PathFinder pathFinder = ((DeltaGraphGenerator)g).getParentGraph().getPathFinder((Node)remElem);
                double srcCapa = Project.getPatchCapacity((Node)remElem);
                for(DijkstraNode node : pathFinder.getComputedNodes()) 
                    if(node.node != remElem)
                        flux += Math.pow(srcCapa * Project.getPatchCapacity(node.node), alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha()*node.cost);
                
                flux = 2 * flux;// / Math.pow(Project.getArea(), 2);
            }
            return new Double[] {intra, flux, indice+intra+flux};
        } else
            return new Double[] {null, null, indice};
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
    public void setParamFromDetailName(String detailName) {
        alphaParam.setParamFromDetailName(detailName);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
