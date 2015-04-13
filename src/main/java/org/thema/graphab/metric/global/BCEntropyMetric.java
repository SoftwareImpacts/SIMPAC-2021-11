
package org.thema.graphab.metric.global;

import org.thema.graphab.metric.local.BCLocalMetric;
import org.thema.graphab.metric.local.LocalMetric;

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
        public Node(LocalMetric metric, TypeElem type) {
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
        public Edge(LocalMetric metric, TypeElem type) {
            super(metric, type);
        }
    }
    
}
