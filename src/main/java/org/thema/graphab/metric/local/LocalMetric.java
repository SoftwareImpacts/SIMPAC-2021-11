/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import org.geotools.graph.structure.Graphable;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Metric;

/**
 *
 * @author gvuidel
 */
public abstract class LocalMetric extends Metric {
    
    public abstract double calcIndice(Graphable g, GraphGenerator gen);

    public boolean calcNodes() {
        return false;
    }

    public boolean calcEdges() {
        return false;
    }
}
