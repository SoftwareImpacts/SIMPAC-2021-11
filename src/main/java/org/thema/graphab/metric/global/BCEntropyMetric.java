/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric.global;

import org.thema.graphab.metric.local.BCLocalMetric;

/**
 *
 * @author gvuidel
 */
public class BCEntropyMetric {

    public static class Node extends EntropyLocal2GlobalMetric {
        public Node() {
            super(new BCLocalMetric(), TypeElem.NODE);
        }
    }
    
    public static class Edge extends EntropyLocal2GlobalMetric {
        public Edge() {
            super(new BCLocalMetric(), TypeElem.EDGE);
        }
    }
    
}
