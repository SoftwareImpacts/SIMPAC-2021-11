
package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Class Coincidence Probability metric.
 * Sum of squared ratio of capacity components
 * Sum of pi^2 where pi = capacity of component i / total capacity
 * @author Gilles Vuidel
 */
public class CCPMetric extends GlobalMetric {

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        double total = g.getPatchCapacity();
        double sum = 0;
        for(int i = 0; i < g.getComponents().size(); i++) {
            double size = g.getComponentGraphGen(i).getPatchCapacity();
            sum += Math.pow(size / total, 2);
        }

        return new Double[]{sum};
    }

    @Override
    public String getShortName() {
        return "CCP";
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
