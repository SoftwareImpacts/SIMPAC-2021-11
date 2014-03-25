/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import java.util.HashMap;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;

/**
 *
 * @author gvuidel
 */
public abstract class AbstractBCLocalMetric<T> extends LocalMetric implements PreCalcMetric<T> {
  
    private transient HashMap<Object, Double> mapVal;

    @Override
    public void endCalc(GraphGenerator g) {
    }

    @Override
    public void startCalc(GraphGenerator g) {
        mapVal = new HashMap<Object, Double>();
        for(Node node : g.getNodes())
            mapVal.put(((Feature)node.getObject()).getId(), 0.0);
        for(Edge edge : g.getEdges())
            mapVal.put(((Feature)edge.getObject()).getId(), 0.0);
    }

    @Override
    public void mergePart(Object part) {
        Map<Object, Double> courant = (Map<Object, Double>) part;
        for(Object id : courant.keySet())
            mapVal.put(id, courant.get(id) + mapVal.get(id));
    }

    
    @Override
    public double calcIndice(Graphable g, GraphGenerator gen) {
        return mapVal.get(((Feature)g.getObject()).getId());
    }

    @Override
    public boolean calcNodes() {
        return true;
    }

    @Override
    public boolean calcEdges() {
        return true;
    }

}
