/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.geotools.graph.structure.Node;
import org.thema.parallel.AbstractParallelTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.PreCalcMetric.TypeParam;

/**
 * 
 * @author gvuidel
 */
public class PreCalcMetricTask extends AbstractParallelTask<Void, List> implements Serializable {

    PreCalcMetric indice;
    double maxCost;
    String graphName;
    List<Integer> ids;
    
    transient HashMap<Integer, Node> patchNodes;
    transient GraphGenerator gen;

    public PreCalcMetricTask(GraphGenerator gen, PreCalcMetric indice, double maxCost, TaskMonitor monitor) {
        super(monitor);
        this.gen = gen;
        this.graphName = gen.getName();
        this.indice = indice;
        this.maxCost = maxCost;
        
        ids = new ArrayList<Integer>(gen.getNodes().size());
        for(Node n : gen.getNodes()) 
            ids.add((Integer)((Feature)n.getObject()).getId());
        
        indice.startCalc(gen);
    }

    @Override
    public void init() {
        super.init();
        if(gen == null) {
            gen = Project.getProject().getGraph(graphName);
            if(gen == null)
                throw new IllegalStateException("Graph " + graphName + " not found in project.\n Modified graph cannot be used in MPI environment");
        }
        
        patchNodes = new HashMap<Integer, Node>(gen.getNodes().size());
        for(Node n : gen.getNodes()) {
            int id = (Integer)((Feature)n.getObject()).getId();
            patchNodes.put(id, n);
        }
    }
    
    @Override
    public List execute(int start, int end) {
        List results = new ArrayList(end-start);
        for(Integer id : ids.subList(start, end)) {
            Node node = patchNodes.get(id);
            if(indice.getTypeParam() == TypeParam.PATHFINDER)
                results.add(indice.calcPartIndice(gen.getPathFinder(node, maxCost), gen));
            else
                results.add(indice.calcPartIndice(node, gen));
            
            incProgress(1);
        }
        return results;
    }

    @Override
    public void finish() {
        indice.endCalc(gen);
    }

    @Override
    public void gather(List results) {
        for(Object res : results)
            indice.mergePart(res);
    }

    @Override
    public Void getResult() {
        return null;
    }

    @Override
    public int getSplitRange() {
        return gen.getNodes().size();
    }
}
