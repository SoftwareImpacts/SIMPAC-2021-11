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
public class ClosenessLocalMetric extends LocalMetric {


    @Override
    public String getShortName() {
        return "CCe";
    }

    @Override
    public double calcIndice(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        PathFinder pathFinder = gen.getPathFinder(node);
        double sum = 0;
        int nb = 0;
        for(Node n : pathFinder.getComputedNodes()) 
            if(n != node) {
                sum += pathFinder.getCost(n);
                nb++;
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
