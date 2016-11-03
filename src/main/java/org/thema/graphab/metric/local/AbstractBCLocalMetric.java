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
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;

/**
 * Base class for all Betweeness Centrality local metric.
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractBCLocalMetric<T> extends LocalSingleMetric implements PreCalcMetric<T> {
  
    private transient HashMap<Object, Double> mapVal;

    @Override
    public void endCalc(GraphGenerator g) {
    }

    @Override
    public void startCalc(GraphGenerator g) {
        mapVal = new HashMap<>();
        for(Node node : g.getNodes()) {
            mapVal.put(((Feature)node.getObject()).getId(), 0.0);
        }
        for(Edge edge : g.getEdges()) {
            mapVal.put(((Feature)edge.getObject()).getId(), 0.0);
        }
    }

    @Override
    public void mergePart(Object part) {
        Map<Object, Double> courant = (Map<Object, Double>) part;
        for(Object id : courant.keySet()) {
            mapVal.put(id, courant.get(id) + mapVal.get(id));
        }
    }
    
    @Override
    public double calcSingleMetric(Graphable g, GraphGenerator gen) {
        return mapVal.get(((Feature)g.getObject()).getId());
    }

    @Override
    public boolean calcNodes() {
        return true;
    }

    @Override
    public boolean calcEdges() {
        return true;
    }

}
