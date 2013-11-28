/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;

/**
 *
 * @author gvuidel
 */
public class EccentricityLocalMetric extends LocalMetric {


    @Override
    public String getShortName() {
        return "Ec";
    }

    @Override
    public double calcIndice(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        PathFinder pathFinder = gen.getPathFinder(node);
        double max = 0;
        for(DijkstraPathFinder.DijkstraNode n : pathFinder.getComputedNodes()) 
            if(n.cost > max) 
                max = n.cost;
            
        return max;
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
