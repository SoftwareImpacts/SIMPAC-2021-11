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
public class BCEntropyMetric extends EntropyLocal2GlobalMetric {

    public BCEntropyMetric() {
        super(new BCLocalMetric());
    }
    
}
