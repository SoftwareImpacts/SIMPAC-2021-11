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

import org.thema.graphab.metric.global.GlobalMetricLauncher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;

/**
 * Task for calculating a GlobalMetric on several thresholded graphs.
 * The result contains for each graph threshold the value(s) of the metric.
 * 
 * Works only in threaded environment.
 * 
 * @author Gilles Vuidel
 */
public class BatchGraphMetricTask extends AbstractParallelFTask<TreeMap<Double, Double[]>, TreeMap<Double, Double[]>> {

    private Linkset linkset;
    private boolean distAbs;
    private double min, inc, max;
    private boolean intraPatchDist;
    private GlobalMetricLauncher launcher;

    private transient TreeMap<Double, Double[]> result;
    private transient List<Double> dists;

    /**
     * Creates a new BatchGraphMetricTask.
     * @param monitor the progression monitor
     * @param linkset the linkset for graph creation
     * @param distAbs is range values are in distance or in number of links ?
     * @param min the minimum threshold in distance or nb links
     * @param inc the increment between 2 threshold in distance or nb links
     * @param max the maximum threshold in distance or nb links
     * @param launcher the global metric launcher
     * @param intraPatchDist include intra patch distance when creating the graphs ?
     */
    public BatchGraphMetricTask(ProgressBar monitor, Linkset linkset, boolean distAbs, 
            double min, double inc, double max, GlobalMetricLauncher launcher, boolean intraPatchDist) {
        super(monitor);
        this.linkset = linkset;
        this.distAbs = distAbs;
        this.min = min;
        this.inc = inc;
        this.max = max;
        this.launcher = launcher;
        this.intraPatchDist = intraPatchDist;
    }

    /**
     * {@inheritDoc }
     * @return for each distance threshold the value(s) of the metric.
     */
    @Override
    public TreeMap<Double, Double[]> getResult() {
        return result;
    }

    @Override
    public void init() {
        // distance range
        if(distAbs) {
            TreeSet<Double> distSet = new TreeSet<>();
            for(Path p : linkset.getPaths()) {
                distSet.add(getPathDist(p));
            }

            dists = new ArrayList<>();
            int size = -1;
            for(double t = min; t <= max; t += inc) {
                int n = distSet.headSet(t, true).size();
                if(n > size) {
                    size = n;
                    dists.add(t);
                }
            }
            // or nb links range
        } else {
            ArrayList<Double> distLst = new ArrayList<>();
            for(Path p : linkset.getPaths()) {
                distLst.add(getPathDist(p));
            }
            Collections.sort(distLst);
            TreeSet<Double> distSet = new TreeSet<>();

            for(double n = min; n <= max; n += inc) {
                if (n == 0) {
                    distSet.add(0.0);
                } else {
                    distSet.add(distLst.get((int)n-1));
                }
            }
            
            dists = new ArrayList<>(distSet);
        }

        monitor.setMaximum(dists.size());
    }


    private double getPathDist(Path p) {
        return linkset.isCostLength() ? p.getCost() : p.getDist();
    }

    @Override
    protected TreeMap<Double, Double[]> execute(int start, int end) {
        TreeMap<Double, Double[]> results = new TreeMap<>();
        for(double t : dists.subList(start, end)) {
            if(isCanceled()) {
                return null;
            }
            GraphGenerator gen = new GraphGenerator("g", linkset,
                    GraphGenerator.PRUNED, t, intraPatchDist);
            Double[] res = launcher.calcMetric(gen, false, null);
            results.put(t, res);
            incProgress(1);
        }

        return results;
    }


    @Override
    public int getSplitRange() {
        return dists.size();
    }

    @Override
    public void finish(Collection<TreeMap<Double, Double[]>> results) {
        result = new TreeMap<>();
        for(TreeMap<Double, Double[]> o : results) {
            result.putAll(o);
        }

        if(distAbs) {
            //insert non calculated elem
            for(double t = min; t <= max; t += inc) {
                if(!result.containsKey(t)) {
                    result.put(t, result.floorEntry(t).getValue());
                }
            }
        } 
    }

}
