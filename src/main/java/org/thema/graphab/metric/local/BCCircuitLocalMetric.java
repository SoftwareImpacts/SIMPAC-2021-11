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
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.Circuit;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.PreCalcMetric;

/**
 * Betweeness Centrality Circuit version.
 * The shortest path is replaced by the current of the circuit.
 * 
 * @author gvuidel
 */
public class BCCircuitLocalMetric extends AbstractBCLocalMetric<GraphPathFinder> {
      
    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    private transient Circuit circuit;

    @Override
    public Map<Object, Double> calcPartMetric(GraphPathFinder finder, GraphGenerator g) {
        HashMap<Object, Double> result = new HashMap<>();
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        Node n1 = finder.getNodeOrigin();
        for(Node n2 : finder.getComputedNodes()) {
            if (((Integer)Project.getPatch(finder.getNodeOrigin()).getId()) < (Integer)Project.getPatch(n2).getId()) {
                double flow = Math.pow(Project.getPatchCapacity(n2) * srcCapa, alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha() * finder.getCost(n2));
                Map<Object, Double> courant = circuit.computeCourant(n1, n2, 1);
                for (Object id : courant.keySet()) {
                    if (result.containsKey(id)) {
                        result.put(id, courant.get(id)*flow + result.get(id));
                    } else {
                        result.put(id, courant.get(id)*flow);
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public void startCalc(GraphGenerator gen) {
        super.startCalc(gen);
        circuit = new Circuit(gen);
    }

    @Override
    public void endCalc(GraphGenerator g) {
        super.endCalc(g); 
        circuit = null;
    }

    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getType() != GraphGenerator.MST;
    }
    
    @Override
    public String getShortName() {
        return "BCCirc";
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

    @Override
    public PreCalcMetric.TypeParam getTypeParam() {
        return PreCalcMetric.TypeParam.PATHFINDER;
    }     
}
