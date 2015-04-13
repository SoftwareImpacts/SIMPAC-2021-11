
package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.local.LocalMetric;

/**
 * Agregate a local metric by shannon entropy.
 * 
 * @author Gilles Vuidel
 */
public class EntropyLocal2GlobalMetric extends AbstractLocal2GlobalMetric {

    /**
     * Creates a new EntropyLocal2GlobalMetric.
     * @param metric the local metric to agregate
     * @param type agregate on nodes or on edges ?
     */
    public EntropyLocal2GlobalMetric(LocalMetric indice, TypeElem type) {
        super(indice, type);
    }

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues()) {
            if(val < 0) {
                throw new RuntimeException("Value < 0 not allowed for entropy");
            }
            sum += val;
        }
        
        double e = 0;
        int nb = 0;
        for(Double val : getValues()) { 
            if(val > 0) {
                e += (val/sum) * Math.log(val/sum);
                nb++;
            }
        }
        
        return new Double[]{-e / Math.log(nb)};
    }

    @Override
    public String getPrefixShortName() {
        return "E";
    }

    @Override
    public String getPrefixName() {
        return "Entropy";
    }
    
}