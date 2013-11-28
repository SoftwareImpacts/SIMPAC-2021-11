/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Circuit;

/**
 *
 * @author gvuidel
 */
public class CBCLocalMetric extends AbstractBCLocalMetric<Node> {

    public static final String COSTR = "costR";
    public static final String CAPAR = "capaR";
    public static final String CAPAEXP = "capaExp";
    public static final String BETA = "beta";
    private double costR = 1;
    private double capaR = 1;
    private double capaExp = 0;
    private double beta = 0;

    private transient Circuit circuit;
    
    @Override
    public Map<Object, Double> calcPartIndice(Node n, GraphGenerator g) {
        return circuit.computeCourantFrom(n);
    }

    @Override
    public void startCalc(GraphGenerator gen) {
        super.startCalc(gen);
        circuit = new Circuit(gen, costR, capaR, capaExp, beta);
    }
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getType() != GraphGenerator.MST;
    }

    public String getShortName() {
        return "CBC";
    }

    @Override
    public void setParams(Map<String, Object> params) {
        costR = ((Number)params.get(COSTR)).doubleValue();
        capaR = ((Number)params.get(CAPAR)).doubleValue();
        capaExp = ((Number)params.get(CAPAEXP)).doubleValue();
        beta = ((Number)params.get(BETA)).doubleValue();
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put(COSTR, costR);
        params.put(CAPAR, capaR);
        params.put(CAPAEXP, capaExp);
        params.put(BETA, beta);
        return params;
    }
    
    @Override
    public void setParamFromDetailName(String detailName) {
        costR = Double.parseDouble(detailName.substring(detailName.indexOf(COSTR) + COSTR.length(), detailName.indexOf("_"+CAPAR)));
        capaR = Double.parseDouble(detailName.substring(detailName.indexOf(CAPAR) + CAPAR.length(), detailName.indexOf("_"+CAPAEXP)));
        capaExp = Double.parseDouble(detailName.substring(detailName.indexOf(CAPAEXP) + CAPAEXP.length(), detailName.indexOf("_"+BETA)));
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
