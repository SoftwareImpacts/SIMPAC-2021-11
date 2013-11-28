/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class DgLocalMetric extends LocalMetric {


    @Override
    public String getShortName() {
        return "Dg";
    }

    @Override
    public double calcIndice(Graphable g, GraphGenerator gen) {
        return ((Node)g).getDegree();
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
