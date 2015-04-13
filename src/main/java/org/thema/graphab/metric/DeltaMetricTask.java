package org.thema.graphab.metric;

import org.thema.graphab.metric.global.GlobalMetricLauncher;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.DeltaGraphGenerator;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.parallel.AbstractParallelTask;

/**
 * Task for calculating global metric in delta mode on nodes and/or edges.
 * 
 * Works on threaded and MPI environment.
 * @author Gilles
 */
public class DeltaMetricTask extends AbstractParallelTask<Map<Object, Double[]>, Map<Object, Double[]>>
        implements Serializable {

    private String graphName;
    private GlobalMetricLauncher launcher;

    private List ids;
    private Double[] init;

    private transient GraphGenerator gen;
    private transient Map<Object, Double[]> result;

    /**
     * Creates a new DeltaMetricTask
     * @param monitor progress monitor
     * @param gen the graph
     * @param launcher the global metric launcher
     * @param nodeEdge delta on nodes (1), on edges (2) or both (3)
     */
    public DeltaMetricTask(ProgressBar monitor, GraphGenerator gen, GlobalMetricLauncher launcher, int nodeEdge) {
        super(monitor);
        this.gen = gen;
        this.graphName = gen.getName();
        this.launcher = launcher;

        ids = new ArrayList();
        if((nodeEdge & 1) == 1) {
            for(Object n : gen.getGraph().getNodes()) {
                ids.add(((DefaultFeature)((Graphable)n).getObject()).getId());
            }
        }
        if((nodeEdge & 2) == 2) {
            for(Object n : gen.getGraph().getEdges()) {
                ids.add(((DefaultFeature)((Graphable)n).getObject()).getId());
            }
        }

        calcInit();
    }
    
    /**
     * Creates a new DeltaMetricTask
     * @param monitor progress monitor
     * @param gen the graph
     * @param launcher the global metric launcher
     * @param ids list of patch and/or link ids
     */
    public DeltaMetricTask(ProgressBar monitor, GraphGenerator gen, GlobalMetricLauncher launcher, List ids) {
        super(monitor);
        this.gen = gen;
        this.graphName = gen.getName();
        this.launcher = launcher;
        this.ids = ids;

        calcInit();
    }
    
    private void calcInit() {
        monitor.setNote("Etat initial...");
        init = launcher.calcMetric(gen, true, null);
        monitor.setNote("Delta...");
    }

    @Override
    public void init() {
        super.init(); 
        // useful for MPI mode, cause gen is transient, not serialized
        if(gen == null) {
            gen = Project.getProject().getGraph(graphName);
        }
    }

    @Override
    public Map<Object, Double[]> execute(int start, int end) {
        HashSet felems = new HashSet(ids.subList(start, end));

        DeltaGraphGenerator deltaGen = new DeltaGraphGenerator(gen);
        Graph graph = deltaGen.getGraph();
        List<Graphable> elems = new ArrayList<>(felems.size());
        for(Object n : graph.getNodes()) {
            if(felems.contains(((Feature)((Graphable)n).getObject()).getId())) {
                elems.add((Graphable)n);
            }
        }
        for(Object n : graph.getEdges()) {
            if(felems.contains(((Feature)((Graphable)n).getObject()).getId())) {
                elems.add((Graphable)n);
            }
        }

        Map<Object, Double[]> results = new HashMap<>();

        for(Graphable elem : elems) {
            if(isCanceled()) {
                return null;
            }
            deltaGen.removeElem(elem);
            Double[] res = launcher.calcMetric(deltaGen, false, null);
            DefaultFeature f = (DefaultFeature)elem.getObject();
            Double [] delta = new Double[init.length];
            for(int i = 0; i < init.length; i++) {
                double ind;
                if(init[i] == null) {
                    if(init[init.length-1] != null) {
                        ind = res[i] / init[init.length-1];
                    } else {
                        ind = res[i];
                    }
                } else {
                    ind = (init[i] - res[i]) / init[i];
                }
                if(Math.abs(ind) < 1e-14) {
                    ind = 0;     
                }
                delta[i] = ind;
            }

            results.put(f.getId(), delta);
            deltaGen.reset();
            incProgress(1);
        }

        return results;
    }

    @Override
    public int getSplitRange() {
        return ids.size();
    }

    @Override
    public void gather(Map<Object, Double[]> partRes) {
        if(result == null) {
            result = new HashMap<>();
        }
        result.putAll(partRes);
    }

    @Override
    public Map<Object, Double[]> getResult() {
        return result;
    }

    /**
     * @return initial metric values
     */
    public Double[] getInit() {
        return init;
    }

}
