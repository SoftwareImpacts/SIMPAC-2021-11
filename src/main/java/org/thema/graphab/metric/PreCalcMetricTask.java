
package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.common.ProgressBar;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric.TypeParam;
import org.thema.parallel.AbstractParallelTask;

/**
 * Task for executing PreCalcMetric.
 * Works on threaded and MPI environment.
 * @author Gilles Vuidel
 */
public class PreCalcMetricTask extends AbstractParallelTask<Void, List> implements Serializable {

    private PreCalcMetric metric;
    private double maxCost;
    private String graphName;
    private List<Integer> ids;
    
    private transient HashMap<Object, Graphable> mapElem;
    private transient GraphGenerator gen;

    /**
     * Creates a new PreCalcMetricTask
     * @param gen the graph 
     * @param metric the metric to calculate on the graph
     * @param maxCost max distance for path metric
     * @param monitor the progress monitor
     */
    public PreCalcMetricTask(GraphGenerator gen, PreCalcMetric metric, double maxCost, ProgressBar monitor) {
        super(monitor);
        this.gen = gen;
        this.graphName = gen.getName();
        this.metric = metric;
        this.maxCost = maxCost;
        
        ids = new ArrayList<>();
        if(metric.getTypeParam() == TypeParam.EDGE) {
            for(Edge e : gen.getEdges()) { 
                ids.add((Integer)((Feature)e.getObject()).getId());
            }
        } else {
            for(Node n : gen.getNodes()) { 
                ids.add((Integer)((Feature)n.getObject()).getId());
            }
        }
        metric.startCalc(gen);
    }

    @Override
    public void init() {
        super.init();
        // useful only for MPI
        if(gen == null) {
            gen = Project.getProject().getGraph(graphName);
            if(gen == null) {
                throw new IllegalStateException("Graph " + graphName + " not found in project.\n Modified graph cannot be used in MPI environment");
            }
        }
        
        mapElem = new HashMap<>();
        if(metric.getTypeParam() == TypeParam.EDGE) {
            for(Edge e : gen.getEdges()) { 
                Object id = ((Feature)e.getObject()).getId();
                mapElem.put(id, e);
            }
        } else {
            for(Node n : gen.getNodes()) {
                Integer id = (Integer)((Feature)n.getObject()).getId();
                mapElem.put(id, n);
            }
        }
    }
    
    @Override
    public List execute(int start, int end) {
        List results = new ArrayList(end-start);
        for(Integer id : ids.subList(start, end)) {
            Graphable elem = mapElem.get(id);
            if(metric.getTypeParam() == TypeParam.PATHFINDER) {
                results.add(metric.calcPartMetric(gen.getPathFinder((Node)elem, maxCost), gen));
            } else {
                results.add(metric.calcPartMetric(elem, gen));
            }
            
            incProgress(1);
        }
        return results;
    }

    @Override
    public void finish() {
        metric.endCalc(gen);
    }

    @Override
    public void gather(List results) {
        for(Object res : results) {
            metric.mergePart(res);
        }
    }

    /**
     * The result can be retrieved by the metric.
     * @return null
     */
    @Override
    public Void getResult() {
        return null;
    }

    @Override
    public int getSplitRange() {
        return metric.getTypeParam() == TypeParam.EDGE ? gen.getEdges().size() : gen.getNodes().size();
    }
}
