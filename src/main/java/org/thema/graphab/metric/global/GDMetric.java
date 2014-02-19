/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.geotools.graph.structure.Node;
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
        for(Node node : finder.getComputedNodes()) 
            if(finder.getCost(node) > max)
                max = finder.getCost(node);
        
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
