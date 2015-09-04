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

import java.util.HashSet;
import java.util.Iterator;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Clustering Coefficient metric.
 * 
 * @author Gilles Vuidel
 */
public class CCLocalMetric extends LocalMetric {

    @Override
    public String getShortName() {
        return "CC";
    }

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;

        HashSet<Node> related = new HashSet<>();
        for(Iterator it = node.getRelated(); it.hasNext(); ) {
            related.add((Node)it.next());
        }

        if(related.size() <= 1) {
            return 0;
        }
        
        double sum = 0;
        for(Node rel : related) {
            for(Iterator itRel = rel.getRelated(); itRel.hasNext(); ) {
                Node n = (Node) itRel.next();
                if(related.contains(n)) {
                    sum++;
                }
            }
        }

        return sum / (related.size()*(related.size()-1));
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
