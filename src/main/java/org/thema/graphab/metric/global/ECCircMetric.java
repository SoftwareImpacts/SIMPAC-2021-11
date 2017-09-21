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

import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.Circuit;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.PreCalcMetric;

/**
 * Equivalent Connectivity Circuit version.
 * The shortest path is replaced by the resistance of the circuit.
 * 
 * @author Gilles Vuidel
 */
public class ECCircMetric extends GlobalMetric implements PreCalcMetric<Node> {

    private AlphaParamMetric alphaParam = new AlphaParamMetric(false);
    private double metric;
    private Circuit circuit;
    
    @Override
    public Double calcPartMetric(Node n1, GraphGenerator g) {
        double sum = 0;
        double srcCapa = Project.getPatchCapacity(n1);
        for(Node n2 : g.getNodes()) {
            double r = circuit.computeR(n1, n2);
            sum += srcCapa * Project.getPatchCapacity(n2) * Math.exp(-alphaParam.getAlpha()*r);
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
        metric = Math.sqrt(metric);       
    }
    
    @Override
    public String getShortName() {
        return "ECCirc";
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
    public ParamPanel getParamPanel(Linkset linkset) {
        return alphaParam.getParamPanel(linkset);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }


}
