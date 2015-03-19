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
public class MSCMetric extends GlobalMetric {

    public Double [] calcIndice(GraphGenerator g) {
        return new Double[]{g.getPatchCapacity() / g.getComponents().size()};
    }

    public String getShortName() {
        return "MSC";
    }
    
    @Override
    public boolean isAcceptMethod(Project.Method method) {
        return method == Project.Method.GLOBAL;
    }
    
    @Override
    public Type getType() {
        return Type.AREA;
    }
}
