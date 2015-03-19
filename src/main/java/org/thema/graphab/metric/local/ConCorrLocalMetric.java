/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import java.util.Iterator;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class ConCorrLocalMetric extends LocalMetric {


    @Override
    public String getShortName() {
        return "CCor";
    }

    @Override
    public double calcIndice(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        if(node.getDegree() == 0) {
            return 0;
        }
        
        double sum = 0;
        int nb = 0;
        for(Iterator it = node.getRelated(); it.hasNext(); ) {
            sum += ((Node)it.next()).getDegree();
            nb++;
        }
        return node.getDegree() / (sum / nb);
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
