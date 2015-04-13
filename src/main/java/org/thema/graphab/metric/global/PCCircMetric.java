
package org.thema.graphab.metric.global;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.Circuit;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.PreCalcMetric;

/**
 * Probability of Connectivity Circuit version.
 * The shortest path is replaced by the resistance of the circuit.
 * 
 * @author Gilles Vuidel
 */
public class PCCircMetric extends GlobalMetric implements PreCalcMetric<Node> {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    private double metric;
    private Circuit circuit;
    
    @Override
    public Double calcPartMetric(Node n1, GraphGenerator g) {
        double sum = 0;
        double srcCapa = Project.getPatchCapacity(n1);
        for(Node n2 : g.getNodes()) {
            double r = circuit.computeR(n1, n2);
            sum += Math.pow(srcCapa * Project.getPatchCapacity(n2), alphaParam.getBeta()) * Math.exp(-alphaParam.getAlpha()*r);
        }
        return sum;
    }

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        return new Double[]{metric};
    }

    @Override
    public void startCalc(GraphGenerator g) {
        metric = 0;
        circuit = new Circuit(g);
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.NODE;
    }
    
    @Override
    public void mergePart(Object part) {
        metric += (Double)part;
    }

    @Override
    public void endCalc(GraphGenerator g) {
        circuit = null;
        metric = metric / Math.pow(Project.getArea(), 2);       
    }
    
    @Override
    public String getShortName() {
        return "PCCirc";
    }
    
    @Override
    public void setParams(Map<String, Object> params) {
        alphaParam.setParams(params);
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        return alphaParam.getParams();
    }

    @Override
    public ParamPanel getParamPanel(Project project) {
        return alphaParam.getParamPanel(project);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }


}
