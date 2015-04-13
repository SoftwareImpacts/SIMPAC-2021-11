
package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.metric.PreCalcMetric;


/**
 * Base class for global metric using path calculation.
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractPathMetric extends GlobalMetric implements PreCalcMetric<PathFinder> {

    /** the metric result */
    protected transient double metric;

    @Override
    public Double [] calcMetric(GraphGenerator g) {
        return new Double[]{ metric };
    }

    /**
     * {@inheritDoc }
     * Default implementation does nothing.
     * @param g the graph
     */
    @Override
    public void endCalc(GraphGenerator g) {
    };

    /**
     * {@inheritDoc }
     * Initialize metric to 0
     * @param g the graph
     */
    @Override
    public void startCalc(GraphGenerator g) {
        metric = 0;
    }

    /**
     * {@inheritDoc }
     * @return {@link TypeParam.PATHFINDER}
     */
    @Override
    public TypeParam getTypeParam() {
        return TypeParam.PATHFINDER;
    }
}
