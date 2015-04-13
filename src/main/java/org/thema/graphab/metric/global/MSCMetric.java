
package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Mean Size of Components metric.
 * 
 * @author Gilles Vuidel
 */
public class MSCMetric extends GlobalMetric {

    @Override
    public Double [] calcMetric(GraphGenerator g) {
        return new Double[]{g.getPatchCapacity() / g.getComponents().size()};
    }

    @Override
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
