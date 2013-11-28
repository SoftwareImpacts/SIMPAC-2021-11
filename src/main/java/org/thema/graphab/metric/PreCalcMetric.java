/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric;

import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public interface PreCalcMetric<T>  {

    enum TypeParam { NODE, PATHFINDER }
    /**
     * @param param
     * @param g
     */
    public Object calcPartIndice(T param, GraphGenerator g);

    public void mergePart(Object part);
    
    public void endCalc(GraphGenerator g);

    public void startCalc(GraphGenerator g);
    
    public TypeParam getTypeParam();

}
