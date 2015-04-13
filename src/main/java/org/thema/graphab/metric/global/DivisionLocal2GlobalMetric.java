
package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.local.LocalMetric;

/**
 * Agregate a local metric by division index.
 * 1 - sum pi^2 where pi = local metric for elem i / sum of local metric
 * @author Gilles Vuidel
 */
public class DivisionLocal2GlobalMetric extends AbstractLocal2GlobalMetric {

    /**
     * Creates a new DivisionLocal2GlobalMetric
     * @param metric the local metric to agregate
     * @param type agregate on nodes or on edges ?
     */
    public DivisionLocal2GlobalMetric(LocalMetric metric, TypeElem type) {
        super(metric, type);
    }

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues()) {
            sum += val;
        }
        
        double div = 0;
        for(Double val : getValues()) {
            div += Math.pow((val/sum), 2);
        }
        
        return new Double[]{1 - div};
    }

    @Override
    public String getPrefixShortName() {
        return "D";
    }

    @Override
    public String getPrefixName() {
        return "Division";
    }
    
}