/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.*;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.thema.common.parallel.TaskMonitor;
import org.thema.common.distribute.AbstractDistributeTask;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.DeltaGraphGenerator;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class DeltaMetricTask extends AbstractDistributeTask<Map<Object, Double[]>, Map<Object, Double[]>>
        implements Serializable {

    String graphName;
    GraphMetricLauncher launcher;

    List ids;
    Double[] init;

    transient Map<Object, Double[]> result;

    public DeltaMetricTask(TaskMonitor monitor, GraphGenerator gen, GraphMetricLauncher launcher, int nodeEdge) {
        super(monitor);
        this.graphName = gen.getName();
        this.launcher = launcher;

        ids = new ArrayList();
        if((nodeEdge & 1) == 1)
            for(Object n : gen.getGraph().getNodes())
                ids.add(((DefaultFeature)((Graphable)n).getObject()).getId());
        if((nodeEdge & 2) == 2)
            for(Object n : gen.getGraph().getEdges())
                ids.add(((DefaultFeature)((Graphable)n).getObject()).getId());

        monitor.popupNow();
        monitor.setNote("Etat initial...");
        DeltaGraphGenerator deltaGen = new DeltaGraphGenerator(gen);
        init = launcher.calcIndice(deltaGen, null);
        monitor.setNote("Delta...");
    }
    
    public DeltaMetricTask(TaskMonitor monitor, GraphGenerator gen, GraphMetricLauncher launcher, List ids) {
        super(monitor);
        this.graphName = gen.getName();
        this.launcher = launcher;
        this.ids = ids;

        monitor.popupNow();
        monitor.setNote("Etat initial...");
        init = launcher.calcIndice(gen, null);
        monitor.setNote("Delta...");
    }

    @Override
    public Map<Object, Double[]> execute(int start, int end) {
        HashSet felems = new HashSet(ids.subList(start, end));

        DeltaGraphGenerator deltaGen = new DeltaGraphGenerator(Project.getProject().getGraph(graphName));
        Graph graph = deltaGen.getGraph();
        List<Graphable> elems = new ArrayList<Graphable>(felems.size());
        for(Object n : graph.getNodes())
            if(felems.contains(((Feature)((Graphable)n).getObject()).getId()))
                elems.add((Graphable)n);
        for(Object n : graph.getEdges())
            if(felems.contains(((Feature)((Graphable)n).getObject()).getId()))
                elems.add((Graphable)n);

        Map<Object, Double[]> results = new HashMap<Object, Double[]>();

        for(Graphable elem : elems) {
            if(isCanceled())
                return null;

            deltaGen.removeElem(elem);
            Double[] res = launcher.calcIndice(deltaGen, new TaskMonitor.EmptyMonitor());
            DefaultFeature f = (DefaultFeature)elem.getObject();
            Double [] delta = new Double[init.length];
            for(int i = 0; i < init.length; i++) {
                double ind;
                if(init[i] == null) {
                    if(init[init.length-1] != null)
                        ind = res[i] / init[init.length-1];
                    else
                        ind = res[i];
                } else
                    ind = (init[i] - res[i]) / init[i];

                if(Math.abs(ind) < 1e-14)
                    ind = 0;     
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
        if(result == null)
            result = new HashMap<Object, Double[]>();
        result.putAll(partRes);
    }

    @Override
    public Map<Object, Double[]> getResult() {
        return result;
    }

    public Double[] getInit() {
        return init;
    }

}
