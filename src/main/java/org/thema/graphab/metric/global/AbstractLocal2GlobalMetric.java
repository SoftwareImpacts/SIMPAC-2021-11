/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
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

    public enum TypeElem {NODE, EDGE}
    
    private LocalMetric indice;
    protected TypeElem typeElem;
    private transient List<Double> values;

    
    public AbstractLocal2GlobalMetric(LocalMetric indice, TypeElem type) {
        typeElem = type;
        if(typeElem == TypeElem.NODE && !indice.calcNodes())
            throw new IllegalArgumentException("La métrique locale ne se calcule pas sur les noeuds");
        if(typeElem == TypeElem.EDGE && !indice.calcEdges())
            throw new IllegalArgumentException("La métrique locale ne se calcule pas sur les liens");
        if(typeElem == TypeElem.EDGE && !(indice instanceof PreCalcMetric))
            throw new IllegalArgumentException("La métrique locale ne peut pas se calculer pas sur les liens");
        this.indice = indice;
    }

    @Override
    public Object calcPartIndice(Object param, GraphGenerator g) {
        if(indice instanceof PreCalcMetric)
            return ((PreCalcMetric)indice).calcPartIndice(param, g);
        else
            return indice.calcIndice((Graphable)param, g);
    }

    @Override
    public void endCalc(GraphGenerator g) {
        if(indice instanceof PreCalcMetric) {
            ((PreCalcMetric)indice).endCalc(g);
            if(typeElem == TypeElem.NODE) {
                for(Node n : g.getNodes())
                    values.add(indice.calcIndice(n, g));
            } else {
                for(Edge e : g.getEdges())
                    values.add(indice.calcIndice(e, g));
            }
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
        return getPrefixShortName() + "#" + (typeElem == TypeElem.EDGE ? "e" : "") + indice.getShortName();
    }

    @Override
    public String getName() {
        return getPrefixName() + (typeElem == TypeElem.EDGE ? "(" + typeElem.toString().toLowerCase() + ") " : " ") + indice.getName();
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