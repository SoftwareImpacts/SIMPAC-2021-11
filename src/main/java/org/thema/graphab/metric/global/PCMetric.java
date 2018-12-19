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
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Probability of Connectivity metric.
 * 
 * @author Gilles Vuidel
 */
public class PCMetric extends AbstractPathMetric {

    private AlphaParamMetric alphaParam = new AlphaParamMetric(false);
    
    @Override
    public Double calcPartMetric(GraphPathFinder finder, GraphGenerator g) {
        double sum = 0;
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        for(Node node : finder.getComputedNodes()) {
            sum += srcCapa * Project.getPatchCapacity(node) * Math.exp(-alphaParam.getAlpha()*finder.getCost(node));
        }
        return sum;
    }

    @Override
    public void mergePart(Object part) {
        metric += (Double)part;
    }

    @Override
    public void endCalc(GraphGenerator g) {
        metric = metric / Math.pow(g.getProject().getArea(), 2);       
    }
    
    @Override
    public String getShortName() {
        return "PC";
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
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getProject().getCapacityParams().isArea();
    }
    
    
}
