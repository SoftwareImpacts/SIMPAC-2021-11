/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.graphab.metric.global;

import java.lang.reflect.InvocationTargetException;
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
 * Base class for agregating local metric into a global metric.
 * If the local metric implements PreCalcMetric, delegates the methods to the local metric.
 * If not, implements PreCalcMetric on typeElem.
 * Subclass must have the same constructor signature or override {@link #dupplicate() }.
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractLocal2GlobalMetric extends GlobalMetric implements PreCalcMetric {

    /** Calculates on nodes or edges */
    public enum TypeElem { NODE, EDGE }
    
    private LocalMetric metric;
    private TypeElem typeElem;
    private transient List<Double> values;

    /**
     * Creates a new global metric for agregating the local metric on nodes or edges
     * @param metric the local metric
     * @param type calculates local metric on nodes or edges ?
     */
    public AbstractLocal2GlobalMetric(LocalMetric metric, TypeElem type) {
        typeElem = type;
        if(typeElem == TypeElem.NODE && !metric.calcNodes()) {
            throw new IllegalArgumentException("La métrique locale ne se calcule pas sur les noeuds");
        }
        if(typeElem == TypeElem.EDGE && !metric.calcEdges()) {
            throw new IllegalArgumentException("La métrique locale ne se calcule pas sur les liens");
        }
        this.metric = metric;
    }

    @Override
    public Object calcPartMetric(Object param, GraphGenerator g) {
        if(metric instanceof PreCalcMetric) {
            return ((PreCalcMetric)metric).calcPartMetric(param, g);
        } else {
            return metric.calcMetric((Graphable)param, g);
        }
    }

    @Override
    public void endCalc(GraphGenerator g) {
        if(metric instanceof PreCalcMetric) {
            ((PreCalcMetric)metric).endCalc(g);
            if(typeElem == TypeElem.NODE) {
                for(Node n : g.getNodes()) {
                    values.add(metric.calcMetric(n, g));
                }
            } else {
                for(Edge e : g.getEdges()) {
                    values.add(metric.calcMetric(e, g));
                }
            }
        }
    }

    @Override
    public void startCalc(GraphGenerator g) {
        if(metric instanceof PreCalcMetric) {
            ((PreCalcMetric)metric).startCalc(g);
        }
        
        values = new ArrayList<>();
    }

    @Override
    public void mergePart(Object part) {
        if(metric instanceof PreCalcMetric) {
            ((PreCalcMetric)metric).mergePart(part);
        } else {
            values.add((Double)part);
        }
    }

    @Override
    public TypeParam getTypeParam() {
        if(metric instanceof PreCalcMetric) {
            return ((PreCalcMetric)metric).getTypeParam();
        } else {
            return typeElem == TypeElem.NODE ? TypeParam.NODE : TypeParam.EDGE;
        }
    }

    /**
     * @return the type of element : node or edge
     */
    public TypeElem getTypeElem() {
        return typeElem;
    }

    /**
     * @return the local metric
     */
    public LocalMetric getLocalMetric() {
        return metric;
    }

    /**
     * @return the values calculated by the local metric for nodes or edges
     */
    protected List<Double> getValues() {
        return values;
    }
    
    @Override
    public AbstractLocal2GlobalMetric dupplicate() {
        try {
            return this.getClass().getConstructor(LocalMetric.class, TypeElem.class).newInstance(metric.dupplicate(), typeElem);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    } 

    /**
     * {@inheritDoc }
     * Delegates to the local metric
     */
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return metric.isAcceptGraph(graph);
    }

    /**
     * Accepts only global and component methods
     * @param method
     * @return 
     */
    @Override
    public boolean isAcceptMethod(Project.Method method) {
        return method == Method.GLOBAL || method == Method.COMP;
    }
    
    /**
     * @return the short name prefix
     */
    public abstract String getPrefixShortName();
    
    /**
     * @return the prefix for the full name
     */
    public abstract String getPrefixName();
    
    @Override
    public String getShortName() {
        return getPrefixShortName() + "#" + (typeElem == TypeElem.EDGE ? "e" : "") + metric.getShortName();
    }

    @Override
    public String getName() {
        return getPrefixName() + (typeElem == TypeElem.EDGE ? "(" + typeElem.toString().toLowerCase() + ") " : " ") + metric.getName();
    }

    /**
     * {@inheritDoc }
     * Delegates to the local metric
     */
    @Override
    public void setParams(Map<String, Object> params) {
        metric.setParams(params);
    }

    /**
     * {@inheritDoc }
     * Delegates to the local metric
     */
    @Override
    public LinkedHashMap<String, Object> getParams() {
        return metric.getParams();
    }

    /**
     * {@inheritDoc }
     * Delegates to the local metric
     */
    @Override
    public ParamPanel getParamPanel(Project project) {
        return metric.getParamPanel(project);
    }

    /**
     * {@inheritDoc }
     * Delegates to the local metric
     */
    @Override
    public void setParamFromDetailName(String detailName) {
        metric.setParamFromDetailName(detailName);
    }
    
    /**
     * {@inheritDoc }
     * Delegates to the local metric
     */
    @Override
    public Type getType() {
        return metric.getType();
    }
    
}