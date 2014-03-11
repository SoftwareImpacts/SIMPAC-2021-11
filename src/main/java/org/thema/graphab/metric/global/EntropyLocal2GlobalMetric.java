/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.local.LocalMetric;

/**
 * 
 * @author gvuidel
 */
public class EntropyLocal2GlobalMetric extends AbstractLocal2GlobalMetric {


    public EntropyLocal2GlobalMetric(LocalMetric indice, TypeElem type) {
        super(indice, type);
    }

    @Override
    public Double[] calcIndice(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues()) {
            if(val < 0)
                throw new RuntimeException("Value < 0 not allowed for entropy");
            sum += val;
        }
        
        double e = 0;
        int nb = 0;
        for(Double val : getValues()) 
            if(val > 0) {
                e += (val/sum) * Math.log(val/sum);
                nb++;
            }
        
        return new Double[]{-e / Math.log(nb)};
    }

    @Override
    public EntropyLocal2GlobalMetric dupplicate() {
        return new EntropyLocal2GlobalMetric((LocalMetric)getIndice().dupplicate(), typeElem);
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