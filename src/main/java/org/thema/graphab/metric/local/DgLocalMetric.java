
package org.thema.graphab.metric.local;

import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Degree metric.
 * 
 * @author Gilles Vuidel
 */
public class DgLocalMetric extends LocalMetric {

    @Override
    public String getShortName() {
        return "Dg";
    }

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        return ((Node)g).getDegree();
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
