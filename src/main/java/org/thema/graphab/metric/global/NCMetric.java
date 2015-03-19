/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class NCMetric extends GlobalMetric {

    @Override
    public Double [] calcIndice(GraphGenerator g) {
        return new Double[]{(double) g.getComponents().size()};
    }

    @Override
    public String getShortName() {
        return "NC";
    }

    @Override
    public boolean isAcceptMethod(Project.Method method) {
        return method == Project.Method.GLOBAL;
    }
    
    @Override
    public Type getType() {
        return Type.TOPO;
    }
}
