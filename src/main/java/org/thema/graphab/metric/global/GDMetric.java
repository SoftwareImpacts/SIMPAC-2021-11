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
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;

/**
 * Graph Diameter metric.
 * The maximum shortest path in the graph.
 * 
 * @author Gilles Vuidel
 */
public class GDMetric extends AbstractPathMetric {

    @Override
    public Double calcPartMetric(GraphPathFinder finder, GraphGenerator g) {
        double max = 0;
        for(Node node : finder.getComputedNodes()) {
            if (finder.getCost(node) > max) {
                max = finder.getCost(node);
            }
        }
        
        return max;
    }

    @Override
    public void mergePart(Object part) {
        double val = (Double)part;
        if(val > metric) {
            metric = val;
        }
    }

    @Override
    public String getShortName() {
        return "GD";
    }
    
    @Override
    public Type getType() {
        return Type.TOPO;
    }
    
}
