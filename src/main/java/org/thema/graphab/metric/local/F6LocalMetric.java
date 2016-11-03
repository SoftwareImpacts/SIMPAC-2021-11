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


package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.metric.DefaultParamPanel;
import org.thema.graphab.metric.ParamPanel;

/**
 * Local Probability of Connectivity.
 * PC index for one patch.
 * 
 * @author Gilles Vuidel
 */
public class F6LocalMetric extends LocalMetric {

    private double dq = 100;
    private double bq = 0;
    private double dd = 1000;
    private double bd = 0;
    
    @Override
    public Double[] calcMetric(Graphable g, GraphGenerator gen) {
        final Node node = (Node) g;
        final double ai = Project.getPatchCapacity(node);
        final GraphPathFinder pathFinder = gen.getPathFinder(node);
        double qin = 0, qout = 0, din = 0, dout = 0, min = 0, mout = 0;
        for(Node n : pathFinder.getComputedNodes()) {
            if(n == node) {
                continue;
            }
            final double dist = pathFinder.getCost(n);
            final double wq = 1 / (1 + Math.exp(bq * (dist - dq)));
            final double wm = 1 / (1 + Math.exp(-bd * (dist - dd))); 
            final double wd = 1 - wq - wm;
            final double aj = Project.getPatchCapacity(n);
            
            qin += aj * wq;
            qout += ai * wq;
            din += aj * wd;
            dout += ai * wd;
            min += aj * wm;
            mout += ai * wm;
        }            
        
        return new Double [] {qin, qout, din, dout, min, mout };
    }

    @Override
    public String[] getResultNames() {
        return new String[]{"Qin", "Qout", "Din", "Dout", "Min", "Mout"};
    }
    
    @Override
    public String getShortName() {
        return "F6";
    }

    @Override
    public boolean calcNodes() {
        return true;
    }
    
    @Override
    public void setParams(Map<String, Object> params) {
        dq = (double) params.get("dq");
        bq = (double) params.get("bq");
        dd = (double) params.get("dd");
        bd = (double) params.get("bd");
        
        if(bq <= 0) {
            bq = 5 / dq;
        }
        if(bd <= 0) {
            bd = 5 / dd;
        }
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("dq", dq);
        params.put("bq", bq);
        params.put("dd", dd);
        params.put("bd", bd);
        return params;
    }

    @Override
    public ParamPanel getParamPanel(Project project) {
        return new DefaultParamPanel(this);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
