
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
     * Calculates the metric for node or edge g of the graph gen
     * @param g the graph element (node or edge)
     * @param gen the graph
     * @return the calculated metric
     */
    public abstract double calcMetric(Graphable g, GraphGenerator gen);

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
