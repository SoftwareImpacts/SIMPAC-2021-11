/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;

/**
 *
 * @author gvuidel
 */
public class GDMetric extends AbstractPathMetric {

    @Override
    public Double calcPartIndice(PathFinder finder, GraphGenerator g) {
        double max = 0;
        for(DijkstraPathFinder.DijkstraNode node : finder.getComputedNodes()) 
            if(node.cost > max)
                max = node.cost;
        
        return max;
    }

    @Override
    public void mergePart(Object part) {
        double val = (Double)part;
        if(val > indice)
            indice = val;
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
