
package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Size of the Largest Component metric.
 * 
 * @author Gilles Vuidel
 */
public class SLCMetric extends GlobalMetric {

    @Override
    public Double [] calcMetric(GraphGenerator g) {
        double max = 0;
        for(int i = 0; i < g.getComponents().size(); i++) {
            double size = g.getComponentGraphGen(i).getPatchCapacity();
            if(size > max) {
                max = size;
            }
        }

        return new Double[]{max};
    }

    @Override
    public String getShortName() {
        return "SLC";
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
