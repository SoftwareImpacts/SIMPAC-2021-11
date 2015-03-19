/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.DijkstraPathFinder.DijkstraNode;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;

/**
 *
 * @author gvuidel
 */
public class HMetric extends GlobalMetric implements PreCalcMetric<Node> {

    private transient double indice;
    
    public Double [] calcIndice(GraphGenerator g) {
        return new Double[]{indice / 2};
    }

    public String getShortName() {
        return "H";
    }
    
    @Override
    public Type getType() {
        return Type.TOPO;
    }

    @Override
    public Double calcPartIndice(Node node, GraphGenerator g) {
        double sum = 0;

        DijkstraPathFinder finder = new DijkstraPathFinder(g.getGraph(), node, DijkstraPathFinder.NBEDGE_WEIGHTER);
        finder.calculate();
        for(DijkstraNode n : finder.getComputedNodes()) {
            if (n.node != node) {
                sum += 1 / n.cost;
            }
        }
                
        return sum;
    }

    @Override
    public void mergePart(Object part) {
        indice += (Double)part;
    }
    
    @Override
    public void endCalc(GraphGenerator g) {
        
    }

    @Override
    public void startCalc(GraphGenerator g) {
        indice = 0;
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.NODE;
    }
}
