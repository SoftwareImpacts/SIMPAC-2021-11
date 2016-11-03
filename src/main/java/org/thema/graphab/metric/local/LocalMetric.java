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

import org.geotools.graph.structure.Graphable;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Metric;

/**
 * Base class for local metric ie. metric calculated on node and/or edge.
 * Subclass must override {@link #calcNodes()} and/or {@link #calcEdges() }
 * 
 * @author Gilles Vuidel
 */
public abstract class LocalMetric extends Metric {
    
    /**
     * Calculates the metrics for node or edge g of the graph gen
     * @param g the graph element (node or edge)
     * @param gen the graph
     * @return the calculated metrics
     */
    public abstract Double[] calcMetric(Graphable g, GraphGenerator gen);

    /**
     * Is this metric can be calculated on nodes ?
     * Default implementation returns false.
     * @return true if this metric calculates on node
     */
    public boolean calcNodes() {
        return false;
    }

    /**
     * Is this metric can be calculated on edges ?
     * Default implementation returns false.
     * @return true if this metric calculates on edge
     */
    public boolean calcEdges() {
        return false;
    }
}
