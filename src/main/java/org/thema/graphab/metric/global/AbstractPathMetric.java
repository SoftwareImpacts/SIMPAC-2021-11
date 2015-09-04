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


package org.thema.graphab.metric.global;

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.metric.PreCalcMetric;


/**
 * Base class for global metric using path calculation.
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractPathMetric extends GlobalMetric implements PreCalcMetric<GraphPathFinder> {

    /** the metric result */
    protected transient double metric;

    @Override
    public Double [] calcMetric(GraphGenerator g) {
        return new Double[]{ metric };
    }

    /**
     * {@inheritDoc }
     * Default implementation does nothing.
     * @param g the graph
     */
    @Override
    public void endCalc(GraphGenerator g) {
    };

    /**
     * {@inheritDoc }
     * Initialize metric to 0
     * @param g the graph
     */
    @Override
    public void startCalc(GraphGenerator g) {
        metric = 0;
    }

    /**
     * {@inheritDoc }
     * @return {@link TypeParam.PATHFINDER}
     */
    @Override
    public TypeParam getTypeParam() {
        return TypeParam.PATHFINDER;
    }
}
