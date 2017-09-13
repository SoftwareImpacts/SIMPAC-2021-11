/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.links.Linkset;
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
    public Double calcPartMetric(GraphPathFinder finder, GraphGenerator g) {
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
                GraphPathFinder pathFinder = parentGraph.getPathFinder(parentGraph.getNode((Feature) remNode.getObject()));
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
    public ParamPanel getParamPanel(Linkset linkset) {
        return alphaParam.getParamPanel(linkset);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
