/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.graphab.Project;
import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public abstract class Metric implements Serializable  {
    
    public enum Type {WEIGHT, AREA, TOPO, RASTER}
    
    public abstract String getShortName();
    public abstract Type getType();
    
    public String getName() {
        String desc = java.util.ResourceBundle.getBundle("org/thema/graphab/metric/global/Bundle").getString(getShortName());
        if(desc == null) {
            return getShortName();
        } else {
            return desc + " (" + getShortName() + ")";
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getDetailName() {
        String str = getShortName();
        Map<String, Object> params = getParams();
        for(String param : params.keySet()) {
            str += "_" + param + format(params.get(param));
        }
        return str;
    }

    public boolean isAcceptGraph(GraphGenerator graph) {
        return true;
    }

    public boolean isAcceptMethod(Method method) {
        return true;
    }

    public boolean hasParams() {
        return !getParams().isEmpty();
    }
    
    public void setParams(Map<String, Object> params) {}
    
    public LinkedHashMap<String, Object> getParams() {
        return new LinkedHashMap<>();
    }
    
    public ParamPanel getParamPanel(Project project) {
        return new DefaultParamPanel(this.dupplicate());
    }
    
    public void setParamFromDetailName(String detailName) {}
    
    public Metric dupplicate() {
        try {
            return Metric.dupplicate(this);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Metric.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    protected static <T extends Metric>  T dupplicate(T indice) throws InstantiationException, IllegalAccessException  {
        T dupIndice = (T) indice.getClass().newInstance();
        dupIndice.setParams(indice.getParams());
        return dupIndice;
    }

    public static String format(Object val) {
        if(val instanceof Number && ((Number)val).doubleValue() == ((Number)val).intValue()) {
            return String.valueOf(((Number)val).intValue());
        } else {
            return String.valueOf(val);
        }
    }    
}
