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
public class SumLocal2GlobalMetric extends AbstractLocal2GlobalMetric {


    public SumLocal2GlobalMetric(LocalMetric indice) {
        super(indice, TypeElem.NODE);
    }

    @Override
    public Double[] calcIndice(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues())
            sum += val;
        return new Double[] {sum};
    }

    @Override
    public SumLocal2GlobalMetric dupplicate() {
        return new SumLocal2GlobalMetric((LocalMetric)getIndice().dupplicate());
    }

    @Override
    public String getPrefixShortName() {
        return "S";
    }

    @Override
    public String getPrefixName() {
        return "";
    }


    
    
}