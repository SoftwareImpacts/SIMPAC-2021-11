

package org.thema.graphab.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;
import org.thema.graphab.metric.global.GlobalMetric;

/**
 *
 * @author gvuidel
 */
public class BatchMetricTask extends AbstractParallelFTask<TreeMap<Double, Double[]>, TreeMap<Double, Double[]>> {

    String linkName;
    boolean distAbs;
    double min, inc, max;
    boolean intraPatchDist;
    GraphMetricLauncher launcher;

    transient TreeMap<Double, Double[]> result;
    transient GlobalMetric indice;

    transient protected List<Double> dists;

    public BatchMetricTask(TaskMonitor monitor, String linkName, boolean distAbs, 
            double min, double inc, double max, GraphMetricLauncher launcher, boolean intraPatchDist) {
        super(monitor);
        this.linkName = linkName;
        this.distAbs = distAbs;
        this.min = min;
        this.inc = inc;
        this.max = max;
        this.launcher = launcher;
        this.intraPatchDist = intraPatchDist;
    }

    protected BatchMetricTask() {}

    @Override
    public TreeMap<Double, Double[]> getResult() {
        return result;
    }

    @Override
    public void init() {
        indice = launcher.getIndice();
        
        // distance en abscisse
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
            // ou nb lien en abscisse
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
            Double[] res = launcher.calcIndice(gen, new TaskMonitor.EmptyMonitor());
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
//        else {
//            TreeMap<Double, Integer> nLinks = new TreeMap<Double, Integer>();
//            for(Path p : Project.getProject().getPaths(linkName)) {
//                double d = getPathDist(p);
//                if(nLinks.containsKey(d))
//                    nLinks.put(d, nLinks.get(d)+1);
//                else
//                    nLinks.put(d, 1);
//            }
//            for(Entry<Double, Integer> entry : nLinks.entrySet()) {
//                Double d = nLinks.higherKey(entry.getKey());
//                if(d != null)
//                    nLinks.put(d, entry.getValue() + nLinks.get(d));
//            }
//            TreeMap<Double, Double[]> newRes = new TreeMap<Double, Double[]>();
//            for(Double d : result.keySet()) {
//                List<Double[]> lst = result.get(d);
//                lst.add(new Double[]{d});
//                if(d == 0)
//                    newRes.put(0.0, lst);
//                else
//                    newRes.put(nLinks.get(d).doubleValue(), lst);
//            }
//            
//            result = newRes;
//        }
    }

}
