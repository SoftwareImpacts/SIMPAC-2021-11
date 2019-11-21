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


package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.graph.ModGraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Interaction Flux Inter local metric.
 * 
 * @author Gilles Vuidel
 */
public final class IFInterLocalMetric extends LocalSingleMetric implements PathLocalMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    private double maxCost = Double.NaN;
    
    @Override
    public double calcSingleMetric(Graphable g, GraphGenerator gen) {
        ModGraphGenerator graph;
        if(gen instanceof ModGraphGenerator) {
            graph = (ModGraphGenerator)gen;
        } else if(gen.getParentGraph() instanceof ModGraphGenerator) {
            // case for component metric calculation
            graph = (ModGraphGenerator)gen.getParentGraph();
        } else {
            throw new RuntimeException("Modularity graph not found");
        }
        Node node = (Node) g;
        double srcCapa = Project.getPatchCapacity(node);
        DefaultFeature srcPatch = Project.getPatch(node);
        GraphPathFinder pathFinder = graph.getParentGraph().getPathFinder(graph.getParentGraph().getNode(srcPatch), maxCost);
        double sum = 0;
        for(Node n : pathFinder.getComputedNodes()) {
            if(!graph.sameCluster(srcPatch, Project.getPatch(n))) {
                sum += Math.exp(-alphaParam.getAlpha() * pathFinder.getCost(n)) * Math.pow(srcCapa * Project.getPatchCapacity(n), alphaParam.getBeta());            
            }
        }            
        
        return sum;
    }

    @Override
    public String getShortName() {
        return "IFI";
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
    public ParamPanel getParamPanel(Linkset linkset) {
        return alphaParam.getParamPanel(linkset);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }

    @Override
    public void setMaxCost(double maxCost) {
        this.maxCost = maxCost;
    }
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph instanceof ModGraphGenerator;
    }
}
