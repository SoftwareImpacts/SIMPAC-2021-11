/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
