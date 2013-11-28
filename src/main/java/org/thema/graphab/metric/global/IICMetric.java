/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;

/**
 *
 * @author gvuidel
 */
public class IICMetric extends GlobalMetric implements PreCalcMetric<Node> {

    private transient double indice;

    @Override
    public Double [] calcIndice(GraphGenerator g) {
        return new Double[]{indice / Math.pow(Project.getArea(), 2)};
    }


    public String getShortName() {
        return "IIC";
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }

    @Override
    public Double calcPartIndice(Node node, GraphGenerator g) {
        double sum = 0;

        DijkstraPathFinder finder = new DijkstraPathFinder(g.getGraph(), node, DijkstraPathFinder.NBEDGE_WEIGHTER);
        finder.calculate();
        for(DijkstraPathFinder.DijkstraNode n : finder.getComputedNodes())
            sum += Project.getPatchCapacity(n.node) * Project.getPatchCapacity(node) / (1+n.cost);
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
