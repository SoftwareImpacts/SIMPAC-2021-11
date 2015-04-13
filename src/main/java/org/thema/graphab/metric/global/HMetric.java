
package org.thema.graphab.metric.global;

import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.DijkstraPathFinder.DijkstraNode;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;

/**
 * Harary index.
 * 
 * @author Gilles Vuidel
 */
public class HMetric extends GlobalMetric implements PreCalcMetric<Node> {

    private transient double metric;
    
    @Override
    public Double [] calcMetric(GraphGenerator g) {
        return new Double[]{metric / 2};
    }

    @Override
    public String getShortName() {
        return "H";
    }
    
    @Override
    public Type getType() {
        return Type.TOPO;
    }

    @Override
    public Double calcPartMetric(Node node, GraphGenerator g) {
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
        metric += (Double)part;
    }
    
    @Override
    public void endCalc(GraphGenerator g) {
        
    }

    @Override
    public void startCalc(GraphGenerator g) {
        metric = 0;
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.NODE;
    }
}
