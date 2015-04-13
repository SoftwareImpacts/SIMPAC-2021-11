
package org.thema.graphab.metric.global;

import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Metric;

/**
 * Bas class for global metric ie. the metric is calculated for the whole graph.
 * 
 * @author Gilles Vuidel
 */
public abstract class GlobalMetric extends Metric  {

    /**
     * Calculates the metric for the graph g
     * @param g the graph
     * @return the results of the metric
     */
    public abstract Double[] calcMetric(GraphGenerator g);

    /**
     * Returns the results name.
     * Default implementation returns the metric short name
     * @return the results name
     */
    public String[] getResultNames() {
        return new String[]{ getShortName() };
    }

    /**
     * Returns true if method != Method.LOCAL
     * @param method
     * @return 
     */
    @Override
    public boolean isAcceptMethod(Method method) {
        return method != Method.LOCAL;
    }

}
