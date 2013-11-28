/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphGenerator.PathFinder;
import org.thema.graphab.metric.PreCalcMetric;


/**
 *
 * @author gvuidel
 */
public abstract class AbstractPathMetric extends GlobalMetric implements PreCalcMetric<PathFinder> {

    transient protected double indice;

    public Double [] calcIndice(GraphGenerator g) {
        return new Double[]{indice};
    }

    public void endCalc(GraphGenerator g) {};

    public void startCalc(GraphGenerator g) {
        indice = 0;
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.PATHFINDER;
    }
    
    

}
