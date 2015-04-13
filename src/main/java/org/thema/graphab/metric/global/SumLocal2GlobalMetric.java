
package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.local.LocalMetric;

/**
 * Global metric summing the results of a local metric on nodes or edges.
 * 
 * @author Gilles Vuidel
 */
public class SumLocal2GlobalMetric extends AbstractLocal2GlobalMetric {

    /**
     * Creates a new global metric.
     * @param metric the local metric to sum
     * @param typeElem apply the local metric on nodes or edges ?
     */
    public SumLocal2GlobalMetric(LocalMetric metric, TypeElem typeElem) {
        super(metric, typeElem);
    }

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues()) {
            sum += val;
        }
        return new Double[] {sum};
    }

    @Override
    public String getPrefixShortName() {
        return "S";
    }

    @Override
    public String getPrefixName() {
        return "Sum";
    }
    
}