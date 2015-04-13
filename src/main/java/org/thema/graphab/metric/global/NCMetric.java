
package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Number of Components metric.
 * 
 * @author Gilles Vuidel
 */
public class NCMetric extends GlobalMetric {

    @Override
    public Double [] calcMetric(GraphGenerator g) {
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
