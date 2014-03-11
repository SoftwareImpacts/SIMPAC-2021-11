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
public class DivisionLocal2GlobalMetric extends AbstractLocal2GlobalMetric {


    public DivisionLocal2GlobalMetric(LocalMetric indice, TypeElem type) {
        super(indice, type);
    }

    @Override
    public Double[] calcIndice(GraphGenerator g) {
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
    public DivisionLocal2GlobalMetric dupplicate() {
        return new DivisionLocal2GlobalMetric((LocalMetric)getIndice().dupplicate(), typeElem);
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