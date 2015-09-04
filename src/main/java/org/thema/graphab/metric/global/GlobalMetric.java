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

import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Metric;

/**
 * Base class for global metric ie. the metric is calculated for the whole graph.
 * 
 * @author Gilles Vuidel
 */
public abstract class GlobalMetric extends Metric  {

    /**
     * Calculates the metric for the graph g
     * @param g the graph
     * @return the results of the metric
     */
    public abstract Double[] calcMetric(GraphGenerator g);

    /**
     * Returns the results name.
     * Default implementation returns the metric short name
     * @return the results names
     */
    public String[] getResultNames() {
        return new String[]{ getShortName() };
    }

    /**
     * Returns true if method != Method.LOCAL
     * @param method
     * @return 
     */
    @Override
    public boolean isAcceptMethod(Method method) {
        return method != Method.LOCAL;
    }

    public static String getDetailName(GlobalMetric metric, int indResult) {
        if(metric.getResultNames().length == 1) {
            return metric.getDetailName();
        }
        return metric.getDetailName() + "|" + metric.getResultNames()[indResult];
    }
}
