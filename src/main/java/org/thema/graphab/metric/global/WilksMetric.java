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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.Covariance;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.Project;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.ParamPanel;

/**
 *
 * @author Gilles Vuidel
 */
public class WilksMetric extends GlobalMetric {

    public static final String ATTRS = "attrs";
    public static final String NB_PATCH = "npatch";
    public static final String WEIGHT_AREA = "warea";

    private List<String> attributes;
    private int nbMinPatch = 5;
    private boolean weightArea = false;

    @Override
    public String getShortName() {
        return "W";
    }

    @Override
    public Double[] calcMetric(GraphGenerator gen) {
        if(attributes == null || attributes.size() <= 1) {
            throw new IllegalStateException("No enough attributes for Wilks metric -> check parameters.");
        }
        List<Graph> comps = gen.getComponents();
        int nPatch = 0, nComp = 0;
        double totArea = 0;
        RealMatrix intraCov = new Array2DRowRealMatrix(attributes.size(), attributes.size());
        List<double []> total = new ArrayList<>();
        for(Graph g : comps) {
            if(g.getNodes().size() >= nbMinPatch) {
                int n = g.getNodes().size();
                double area = 0;
                double [][] tab = new double[n][attributes.size()];
                int i = 0;
                for(Object node : g.getNodes()) {
                    Feature p = (Feature) ((Node)node).getObject();
                    int j = 0;
                    boolean isNaN = false;
                    for(String attr : attributes) {
                        double val = ((Number)p.getAttribute(attr)).doubleValue();
                        if(Double.isNaN(val)) {
                            isNaN = true;
                        }
                        tab[i][j++] = val;
                    }
                    if(!isNaN) {
                        i++;
                        area += Project.getPatchArea(p);
                    }
                }
                if(i < nbMinPatch) {
                    continue;
                }
                tab = Arrays.copyOf(tab, i);
                n = i;
                RealMatrix cov = new Covariance(tab, false).getCovarianceMatrix();
                intraCov = intraCov.add(cov.scalarMultiply(weightArea ? area : n));
                nPatch += n;
                totArea += area;
                nComp++;
                total.addAll(Arrays.asList(tab));
            }
        }

        if(nComp > 0) {
            RealMatrix totalCov = new Covariance(total.toArray(new double[0][]), false).getCovarianceMatrix();
            totalCov = totalCov.scalarMultiply(weightArea ? totArea : nPatch);
            double indice = intraCov.getDeterminant() / totalCov.getDeterminant();
            double khi2 = - Math.log(indice) * (nPatch - (nComp+attributes.size()+1) / 2.0);
            try {
                double p = 1 - new ChiSquaredDistributionImpl(attributes.size()*nComp).cumulativeProbability(khi2);
                return new Double[] {indice, p, (double)nComp};
            } catch (MathException ex) {
                Logger.getLogger(WilksMetric.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return new Double[] {0.0, 0.0, 0.0};
        }
        
    }

    @Override
    public ParamPanel getParamPanel(Linkset linkset) {
        return new WilksParamPanel(linkset.getProject().getPatches().iterator().next().getAttributeNames(),
                attributes, nbMinPatch, weightArea);
    }

    @Override
    public String[] getResultNames() {
        return new String[] {"Lambda", "Chi2", "Ncomp"};
    }

    @Override
    public void setParams(Map<String, Object> params) {
       attributes = (List<String>) params.get(ATTRS);
       nbMinPatch = (Integer)params.get(NB_PATCH);
       weightArea = (Boolean)params.get(WEIGHT_AREA);
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(ATTRS, attributes);
        params.put(NB_PATCH, nbMinPatch);
        params.put(WEIGHT_AREA, weightArea);
        return params;
    }

    @Override
    public Type getType() {
        return Type.TOPO;
    }
    
    @Override
    public boolean isAcceptMethod(Project.Method method) {
        return method == Project.Method.GLOBAL;
    }

}
