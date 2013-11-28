/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.PreCalcMetric;
import org.thema.graphab.metric.local.LocalMetric;

/**
 * 
 * @author gvuidel
 */
public abstract class AbstractLocal2GlobalMetric extends GlobalMetric implements PreCalcMetric {

    private LocalMetric indice;
    private transient List<Double> values;

    public AbstractLocal2GlobalMetric(LocalMetric indice) {
        if(!indice.calcNodes())
            throw new IllegalArgumentException("L'indice ne se calcule que sur les noeuds");
        
        this.indice = indice;
    }

    @Override
    public Object calcPartIndice(Object param, GraphGenerator g) {
        if(indice instanceof PreCalcMetric)
            return ((PreCalcMetric)indice).calcPartIndice(param, g);
        else
            return indice.calcIndice((Node)param, g);
    }

    @Override
    public void endCalc(GraphGenerator g) {
        if(indice instanceof PreCalcMetric) {
            ((PreCalcMetric)indice).endCalc(g);
            for(Node n : g.getNodes())
                values.add(indice.calcIndice(n, g));
        }
    }

    @Override
    public void startCalc(GraphGenerator g) {
        if(indice instanceof PreCalcMetric)
            ((PreCalcMetric)indice).startCalc(g);
        
        values = new ArrayList<Double>();
    }

    @Override
    public void mergePart(Object part) {
        if(indice instanceof PreCalcMetric)
            ((PreCalcMetric)indice).mergePart(part);
        else
            values.add((Double)part);
    }

    @Override
    public TypeParam getTypeParam() {
        if(indice instanceof PreCalcMetric)
            return ((PreCalcMetric)indice).getTypeParam();
        else
            return TypeParam.NODE;
    }

    public LocalMetric getIndice() {
        return indice;
    }

    protected List<Double> getValues() {
        return values;
    }
    
    @Override
    public abstract AbstractLocal2GlobalMetric dupplicate(); 

    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return indice.isAcceptGraph(graph);
    }

    @Override
    public boolean isAcceptMethod(Project.Method method) {
        return method == Method.GLOBAL || method == Method.COMP;
    }
    
    public abstract String getPrefixShortName();
    public abstract String getPrefixName();
    
    @Override
    public String getShortName() {
        return getPrefixShortName() + "#" + indice.getShortName();
    }

    @Override
    public String getName() {
        return getPrefixName() + " " + indice.getName();
    }

    public void setParams(Map<String, Object> params) {
        indice.setParams(params);
    }

    public LinkedHashMap<String, Object> getParams() {
        return indice.getParams();
    }

    public ParamPanel getParamPanel(Project project) {
        return indice.getParamPanel(project);
    }

    @Override
    public void setParamFromDetailName(String detailName) {
        indice.setParamFromDetailName(detailName);
    }
    
    @Override
    public Type getType() {
        return indice.getType();
    }
    
}