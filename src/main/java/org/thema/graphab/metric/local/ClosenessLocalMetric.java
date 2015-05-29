
package org.thema.graphab.metric.local;

import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;

/**
 * Closeness Centrality metric.
 * 
 * @author Gilles Vuidel
 */
public class ClosenessLocalMetric extends LocalMetric {

    @Override
    public String getShortName() {
        return "CCe";
    }

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        GraphPathFinder pathFinder = gen.getPathFinder(node);
        double sum = 0;
        int nb = 0;
        for(Node n : pathFinder.getComputedNodes()) { 
            if(n != node) {
                sum += pathFinder.getCost(n);
                nb++;
            }
        }
        
        return nb == 0 ? 0 : (sum / nb);
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
