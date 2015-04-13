
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
import org.thema.graphab.Project;
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

    private String linkName;
    private boolean distAbs;
    private double min, inc, max;
    private boolean intraPatchDist;
    private GlobalMetricLauncher launcher;

    private transient TreeMap<Double, Double[]> result;
    private transient List<Double> dists;

    /**
     * Creates a new BatchGraphMetricTask.
     * @param monitor the progress monitor
     * @param linkName the linkset name for graph creation
     * @param distAbs is range values are in distance or in number of links ?
     * @param min the minimum threshold in distance or nb links
     * @param inc the increment between 2 threshold in distance or nb links
     * @param max the maximum threshold in distance or nb links
     * @param launcher the global metric launcher
     * @param intraPatchDist include intra patch distance when creating the graphs ?
     */
    public BatchGraphMetricTask(ProgressBar monitor, String linkName, boolean distAbs, 
            double min, double inc, double max, GlobalMetricLauncher launcher, boolean intraPatchDist) {
        super(monitor);
        this.linkName = linkName;
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
            for(Path p : Project.getProject().getPaths(linkName)) {
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
            for(Path p : Project.getProject().getPaths(linkName)) {
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
        return Project.getProject().getLinkset(linkName).getType_length() == Linkset.COST_LENGTH ?
            p.getCost() : p.getDist();
    }

    @Override
    protected TreeMap<Double, Double[]> execute(int start, int end) {
        TreeMap<Double, Double[]> results = new TreeMap<>();
        for(double t : dists.subList(start, end)) {
            if(isCanceled()) {
                return null;
            }
            GraphGenerator gen = new GraphGenerator("g", Project.getProject().getLinkset(linkName),
                    GraphGenerator.THRESHOLD, t, intraPatchDist);
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
