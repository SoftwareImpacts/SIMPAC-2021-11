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

import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;

/**
 * Integral Index of Connectivity metric.
 * 
 * @author Gilles Vuidel
 */
public class IICMetric extends GlobalMetric implements PreCalcMetric<Node> {

    private transient double metric;

    @Override
    public Double [] calcMetric(GraphGenerator g) {
        return new Double[]{metric / Math.pow(g.getProject().getArea(), 2)};
    }

    @Override
    public String getShortName() {
        return "IIC";
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }

    @Override
    public Double calcPartMetric(Node node, GraphGenerator g) {
        double sum = 0;

        DijkstraPathFinder finder = new DijkstraPathFinder(g.getGraph(), node, DijkstraPathFinder.NBEDGE_WEIGHTER);
        finder.calculate();
        for(DijkstraPathFinder.DijkstraNode n : finder.getComputedNodes()) {
            sum += Project.getPatchCapacity(n.node) * Project.getPatchCapacity(node) / (1+n.cost);
        }
        return sum;
    }

    @Override
    public void mergePart(Object part) {
        metric += (Double)part;
    }
    
    @Override
    public void endCalc(GraphGenerator g) {
        
    }

    @Override
    public void startCalc(GraphGenerator g) {
        metric = 0;
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.NODE;
    }     
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getProject().getCapacityParams().isCalcArea();
    }
}
