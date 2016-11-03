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

import org.thema.graphab.metric.local.BCLocalMetric;
import org.thema.graphab.metric.local.LocalSingleMetric;

/**
 * These classes are useful only for plugin metric management.
 * Do not use these classes directly
 * Remove these classes if this metric comes in graphab core.
 * 
 * @author Gilles Vuidel
 */
public class BCEntropyMetric {

    /**
     * Entropy index on BC local metric on nodes
     */
    public static class Node extends EntropyLocal2GlobalMetric {
        /**
         * Default constructor for plugin metric
         */
        public Node() {
            super(new BCLocalMetric(), TypeElem.NODE);
        }
        /**
         * Constructor for dupplicate method.
         * Do not use !
         * @param metric may be BCLocalMetric
         * @param type may be TypeElem.NODE
         */
        public Node(LocalSingleMetric metric, TypeElem type) {
            super(metric, type);
        }
    }
    
    /**
     * Entropy index on BC local metric on edges
     */
    public static class Edge extends EntropyLocal2GlobalMetric {
        /**
         * Default constructor for plugin metric
         */
        public Edge() {
            super(new BCLocalMetric(), TypeElem.EDGE);
        }
        /**
         * Constructor for dupplicate method.
         * Do not use !
         * @param metric may be BCLocalMetric
         * @param type may be TypeElem.EDGE
         */
        public Edge(LocalSingleMetric metric, TypeElem type) {
            super(metric, type);
        }
    }
    
}
