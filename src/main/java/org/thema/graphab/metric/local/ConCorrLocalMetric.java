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

import java.util.Iterator;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Connectivity Correlation metric.
 * 
 * @author Gilles Vuidel
 */
public class ConCorrLocalMetric extends LocalMetric {

    @Override
    public String getShortName() {
        return "CCor";
    }

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        if(node.getDegree() == 0) {
            return 0;
        }
        
        double sum = 0;
        int nb = 0;
        for(Iterator it = node.getRelated(); it.hasNext(); ) {
            sum += ((Node)it.next()).getDegree();
            nb++;
        }
        return node.getDegree() / (sum / nb);
    }

    @Override
    public boolean calcNodes() {
        return true;
    }

    @Override
    public Type getType() {
        return Type.TOPO;
    }
}
