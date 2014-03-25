/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import org.thema.common.swing.TaskMonitor;
import org.thema.parallel.ExecutorService;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.global.GlobalMetric;

/**
 *
 * @author gvuidel
 */
public class GraphMetricLauncher implements Serializable {

    private GlobalMetric refIndice;
    private boolean threaded;
    private double maxCost;

    public GraphMetricLauncher(GlobalMetric indice) {
        this(indice, Double.NaN, false);
    }
    
    public GraphMetricLauncher(GlobalMetric indice, boolean threaded) {
        this(indice, Double.NaN, threaded);
    }

    public GraphMetricLauncher(GlobalMetric indice, double maxCost) {
        this(indice, maxCost, false);
    }

    public GraphMetricLauncher(GlobalMetric indice, double maxCost, boolean threaded) {
        this.refIndice = indice;
        this.maxCost = maxCost;
        this.threaded = threaded;
    }

    protected GraphMetricLauncher() {
    }

    public GlobalMetric getIndice() {
        return refIndice;
    }
    
    public Double[] calcIndice(final GraphGenerator graph, TaskMonitor monitor) {
        if(monitor == null)
            monitor = new TaskMonitor.EmptyMonitor();
        GlobalMetric indice = (GlobalMetric) refIndice.dupplicate();
        
        monitor.setMaximum(100);

        if(indice instanceof PreCalcMetric) {
            PreCalcMetricTask pathTask = new PreCalcMetricTask(graph, (PreCalcMetric)indice, maxCost, monitor.getSubMonitor(0, 100, 100));
            if(threaded)
                ExecutorService.execute(pathTask);
            else
                ExecutorService.executeSequential(pathTask);
            if(pathTask.isCanceled()) 
                throw new CancellationException();
        }

        monitor.setNote(indice.getName());

        Double[] res = indice.calcIndice(graph);
        monitor.setProgress(100);
        return res;
    }


}
