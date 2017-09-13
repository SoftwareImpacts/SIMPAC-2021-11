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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graph.pathfinder.Path;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Betweeness Centrality metric.
 * 
 * @author Gilles Vuidel
 */
public final class BCLocalMetric extends AbstractBCLocalMetric<GraphPathFinder> {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public HashMap<Object, Double> calcPartMetric(GraphPathFinder finder, GraphGenerator g) {
        HashMap<Object, Double> result = new HashMap<>();
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        for(Node node : finder.getComputedNodes()) {
            if (((Integer)Project.getPatch(finder.getNodeOrigin()).getId()) < (Integer)Project.getPatch(node).getId()) {
                Path path = finder.getPath(node);
                if (path == null) {
                    continue;
                }
                double v = Math.pow(Project.getPatchCapacity(node) * srcCapa, alphaParam.getBeta())
                        * Math.exp(-alphaParam.getAlpha() * finder.getCost(node));
                
                List<Node> nodes = path.getNodes();
                for (int i = 1; i < nodes.size()-1; i++) {
                    Feature f = (Feature)nodes.get(i).getObject();
                    if (result.containsKey(f.getId())) {
                        result.put(f.getId(), result.get(f.getId()) + v);
                    } else {
                        result.put(f.getId(), v);
                    }
                }
                for (Edge e : path.getEdges()) {
                    Feature f = (Feature)e.getObject();
                    if (result.containsKey(f.getId())) {
                        result.put(f.getId(), result.get(f.getId()) + v);
                    } else {
                        result.put(f.getId(), v);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getShortName() {
        return "BC";
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
    public TypeParam getTypeParam() {
        return TypeParam.PATHFINDER;
    }    
}
