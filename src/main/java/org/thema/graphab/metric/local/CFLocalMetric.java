/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Circuit;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.SingleValuePanel;

/**
 *
 * @author gvuidel
 */
public class CFLocalMetric extends AbstractBCLocalMetric<Node> {

    public static final String BETA = "beta";
    private double beta = 0;
    
    private transient Circuit circuit;
    
    @Override
    public Map<Object, Double> calcPartIndice(Node node, GraphGenerator g) {
        return circuit.computeCourantTo(node);
    }
    
    @Override
    public void startCalc(GraphGenerator gen) {
        super.startCalc(gen);
        circuit = new Circuit(gen, beta);
    }
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getType() != GraphGenerator.MST;
    }

    public String getShortName() {
        return "CF";
    }

    public void setParams(Map<String, Object> params) {
        beta = ((Number)params.get(BETA)).doubleValue();
    }

    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(BETA, beta);
        return params;
    }

    public ParamPanel getParamPanel(Project project) {
        return new SingleValuePanel(BETA, beta);
    }
    
    @Override
    public void setParamFromDetailName(String detailName) {
        beta = Double.parseDouble(detailName.substring(detailName.indexOf(BETA) + BETA.length()));
    }    

    @Override
    public Type getType() {
        return Type.WEIGHT;
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.NODE;
    }     
}
