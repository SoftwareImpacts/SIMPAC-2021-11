
package org.thema.graphab.metric.global;

import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;

/**
 * Graph Diameter metric.
 * The maximum shortest path in the graph.
 * 
 * @author Gilles Vuidel
 */
public class GDMetric extends AbstractPathMetric {

    @Override
    public Double calcPartMetric(PathFinder finder, GraphGenerator g) {
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
