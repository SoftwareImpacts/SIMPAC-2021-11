
package org.thema.graphab.metric;

import org.thema.graphab.graph.GraphGenerator;

/**
 * Interface for metric which needs precalculations (local metric as BC) or can be decomposed for parallelization (global metric).
 * 
 * @author Gilles Vuidel
 */
public interface PreCalcMetric<T>  {

    /** The type of parameter for {@link #calcPartMetric } */
    enum TypeParam { NODE, EDGE, PATHFINDER }
    
    /**
     * Initialize the calculation for the graph g
     * @param g the graph
     */
    void startCalc(GraphGenerator g);
    
    /**
     * Calculates a part of the whole calculation.
     * This method must be thread safe !
     * @param param the element : node, edge or pathfinder of a node
     * @param g the graph
     * @return the partial result
     */
    Object calcPartMetric(T param, GraphGenerator g);

    /**
     * This method is called with the result of {@link #calcPartMetric}.
     * It merges partial results together.
     * This method is called by only one thread.
     * @param part the partial result
     */
    void mergePart(Object part);
    
    /**
     * This method is called after all {@link #calcPartMetric} and {@link #mergePart} calls to finalize the calculation if needed.
     * @param g the graph
     */
    void endCalc(GraphGenerator g);

    /**
     * @return the type of the parameter of {@link #calcPartMetric}
     */
    TypeParam getTypeParam();

}
