
package org.thema.graphab.metric.local;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Circuit;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.SingleValuePanel;

/**
 * Potential Current Flow metric.
 * 
 * @author Gilles Vuidel
 */
public class PCFLocalMetric extends AbstractBCLocalMetric<Node> {


    public static final String BETA = "beta";
    private double beta = 0;
    
    private transient Circuit circuit;
    
    @Override
    public Map<Object, Double> calcPartMetric(Node node, GraphGenerator g) {
        Map<Object, Double> res = new HashMap<>();
        for(Node n2 : g.getNodes()) {
            if (node != n2) {
                Map<Object, Double> courant = circuit.computePotCourant(node, n2, Math.pow(Project.getPatchCapacity(node), beta));
                for (Object id : courant.keySet()) {
                    if (res.containsKey(id)) {
                        res.put(id, courant.get(id) + res.get(id));
                    } else {
                        res.put(id, courant.get(id));
                    }
                }
            }
        }
        return res;
    }
    
    @Override
    public void startCalc(GraphGenerator gen) {
        super.startCalc(gen);
        circuit = new Circuit(gen);
    }

    @Override
    public void endCalc(GraphGenerator g) {
        super.endCalc(g); 
        circuit = null;
    }
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getType() != GraphGenerator.MST;
    }
    
    @Override
    public String getShortName() {
        return "PCF";
    }

    @Override
    public void setParams(Map<String, Object> params) {
        beta = ((Number)params.get(BETA)).doubleValue();
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(BETA, beta);
        return params;
    }

    @Override
    public ParamPanel getParamPanel(Project project) {
        return new SingleValuePanel(BETA, beta);
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
