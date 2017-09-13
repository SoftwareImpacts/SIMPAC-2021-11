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


package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.thema.graphab.Project;
import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;

/**
 * Base class for metric.
 * Metric must have a constructor without parameter or override {@link #dupplicate() }.
 * 
 * @author Gilles Vuidel
 */
public abstract class Metric implements Serializable  {
    
    /** Types of metric for classification */
    public enum Type { WEIGHT, AREA, TOPO, RASTER }
    
    /**
     * @return the short name of the metric 
     */
    public abstract String getShortName();
    /**
     * @return the type of the metric
     * @see Type
     */
    public abstract Type getType();
    
    /**
     * The full name of the metric is retrieved from 
     * org/thema/graphab/metric/global/Bundle properties file.
     * The key is the short name metric.
     * If the entry does not exist in the Bundle file, return only the short name.
     * @return the full name of the metric
     */
    public String getName() {
        ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/metric/global/Bundle");
        String desc = null;
        if(bundle.containsKey(getShortName())) {
            desc = bundle.getString(getShortName());
        }
        if(desc == null) {
            return getShortName();
        } else {
            return desc + " (" + getShortName() + ")";
        }
    }

    /**
     * @return {@link #getName() }
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns the results name.
     * Default implementation returns the metric short name
     * @return the results names
     */
    public String[] getResultNames() {
        return new String[]{ getShortName() };
    }
    
    /**
     * The short name with the parameters (if any) separated by underscore.
     * metric_param1val1_param2val2
     * Ex : PC_d1000_p0.05_beta1
     * @return the short name with the metric parameters
     */
    public String getDetailName() {
        String str = getShortName();
        Map<String, Object> params = getParams();
        for(String param : params.keySet()) {
            str += "_" + param + format(params.get(param));
        }
        return str;
    }

    /**
     * The short name with the parameters (if any) separated by underscore and the result name (if any) separated by a pipe
     * @param indResult the index of the result
     * @return the short name with the metric parameters and the result name if any
     */
    public String getDetailName(int indResult) {
        if(getResultNames().length == 1) {
            return getDetailName();
        }
        return getDetailName() + "|" + getResultNames()[indResult];
    }
    
    /**
     * Is this metric can be calculated on this graph.
     * Default implementation returns always true.
     * @param graph the graph
     * @return true if this metric can be calculated on this graph
     */
    public boolean isAcceptGraph(GraphGenerator graph) {
        return true;
    }

    /**
     * Is this metric can be calculated with this method.
     * Default implementation returns always true.
     * @param method the method (global, local, component or delta)
     * @return true if this metric can be calculated with this method
     */
    public boolean isAcceptMethod(Method method) {
        return true;
    }

    /**
     * Do this metric has parameters ?
     * @return true if the metric has parameters
     */
    public boolean hasParams() {
        return !getParams().isEmpty();
    }
    
    /**
     * Set the parameters of this metric.
     * Default implementation does nothing.
     * This method must be overriden if the metric has parameter.
     * @param params 
     * @throws IllegalStateException if params is not empty
     */
    public void setParams(Map<String, Object> params) {
        if(!params.isEmpty()) {
            throw new IllegalStateException("This metric has no parameter or this method must be overriden !");
        }
    }
    
    /**
     * Returns a map containing parameter name and value.
     * Default implementation returns an empty map.
     * This method must be overriden if the metric has parameter.
     * @return the parameter map
     */
    public LinkedHashMap<String, Object> getParams() {
        return new LinkedHashMap<>();
    }
    
    /**
     * Returns a panel for editing parameters.
     * Default implementation returns {@link DefaultParamPanel}
     * @param linkset the selected linkset 
     * @return a panel for editing parameters
     */
    public ParamPanel getParamPanel(Linkset linkset) {
        return new DefaultParamPanel(this.dupplicate());
    }
    
    /**
     * Sets the parameters from a detail name.
     * Default implementation parse only double type parameter.
     * This method must be overriden if the metric has non numeric parameter.
     * @param detailName the detail name
     */
    public void setParamFromDetailName(String detailName) {
        String[] tokens = detailName.split("_");
        tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
        if(tokens.length != getParams().size()) {
            throw new IllegalArgumentException("Bad number of parameters for " + detailName);
        }
        HashMap<String, Object> params = new HashMap<>();
        Pattern pattern = Pattern.compile("([a-zA-Z]+)([-+]?[0-9]+\\.?[0-9]*)");
        for(String token : tokens) {
            Matcher matcher = pattern.matcher(token);
            matcher.find();
            params.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
        }
        
        setParams(params);
    }
    
    /**
     * Creates a copy of this metric.
     * Creates a new instance of the metric and copy parameters.
     * @return a clone of this metric
     */
    public Metric dupplicate() {
        try {
            return Metric.dupplicate(this);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Creates a copy of this metric.
     * Creates a new instance of the metric and copy parameters with {@link #setParams }
     * @param <T>
     * @param metric the metric to copy
     * @return a new copy
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    protected static <T extends Metric>  T dupplicate(T metric) throws InstantiationException, IllegalAccessException  {
        T dupIndice = (T) metric.getClass().newInstance();
        dupIndice.setParams(metric.getParams());
        return dupIndice;
    }

    /**
     * Use String.valueOf method, but avoid the trailing .0 for floating point number storing integral value.
     * If the value is a floating point number containing an integral number, cast the value to int.
     * @param val the value to convert in String
     * @return a string representing val
     */
    public static String format(Object val) {
        if(val instanceof Number && ((Number)val).doubleValue() == ((Number)val).intValue()) {
            return String.valueOf(((Number)val).intValue());
        } else {
            return String.valueOf(val);
        }
    }    
}
