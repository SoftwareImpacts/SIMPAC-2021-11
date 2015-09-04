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

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import org.thema.common.ProgressBar;
import org.thema.common.swing.TaskMonitor;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric;
import org.thema.graphab.metric.PreCalcMetricTask;
import org.thema.parallel.ExecutorService;

/**
 * Class for calculating a global metric on a graph.
 * 
 * @author Gilles Vuidel
 */
public class GlobalMetricLauncher implements Serializable {

    private GlobalMetric refMetric;
    private double maxCost;

    /**
     * Creates a new GlobalMetricLauncher without maxCost
     * @param metric the global metric
     */
    public GlobalMetricLauncher(GlobalMetric metric) {
        this(metric, Double.NaN);
    }

     /**
     * Creates a new GlobalMetricLauncher with maxCost
     * @param metric the global metric
     * @param maxCost the max distance (for path metric only)
     */
    public GlobalMetricLauncher(GlobalMetric metric, double maxCost) {
        this.refMetric = metric;
        this.maxCost = maxCost;
    }

    /**
     * Default constructor for serialization
     */
    protected GlobalMetricLauncher() {
    }

    /**
     * @return the global metric
     */
    public GlobalMetric getMetric() {
        return refMetric;
    }
    
    /**
     * Calculates the metric on the graph.
     * The metric is dupplicated before the calculation
     * @param graph the graph used for calculating the metric
     * @param threaded parallelize the calculation (for PreCalcMetric only)
     * @param monitor the progress monitor, may be null
     * @return the resulting metric value(s)
     */
    public Double[] calcMetric(GraphGenerator graph, boolean threaded, ProgressBar monitor) {
        if(monitor == null) {
            monitor = new TaskMonitor.EmptyMonitor();
        }
        GlobalMetric metric = (GlobalMetric) refMetric.dupplicate();
        
        monitor.setMaximum(100);

        if(metric instanceof PreCalcMetric) {
            PreCalcMetricTask pathTask = new PreCalcMetricTask(graph, (PreCalcMetric)metric, maxCost, monitor);
            if(threaded) {
                ExecutorService.execute(pathTask);
            } else {
                ExecutorService.executeSequential(pathTask);
            } 
            if(pathTask.isCanceled()) {
                throw new CancellationException();
            }
        }

        monitor.setNote(metric.getName());

        Double[] res = metric.calcMetric(graph);
        monitor.setProgress(100);
        return res;
    }

}
