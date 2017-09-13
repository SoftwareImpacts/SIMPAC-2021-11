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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.geotools.graph.structure.Graphable;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;
import org.thema.graphab.links.RasterPathFinder;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.SingleValuePanel;

/**
 * IFPC metric.
 * 
 * @author Gilles Vuidel
 */
public final class IFPCMetric extends LocalSingleMetric {

    private double dMax = 100;
    private RasterPathFinder pathfinder;

    @Override
    public String getName() {
        return "IFPC";
    }

    @Override
    public String getShortName() {
        return getName();
    }

    @Override
    public synchronized double calcSingleMetric(Graphable g, GraphGenerator gen) {
        Feature patch = (Feature) g.getObject();
        if(pathfinder == null) {
            try {
                pathfinder = gen.getProject().getRasterPathFinder(gen.getLinkset());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        double ifpc = 0;
        HashMap<Feature, Path> dists = pathfinder.calcPaths(patch, dMax, false, true);
        for(Feature f : dists.keySet()) {
            ifpc += Project.getPatchCapacity(f) / (dists.get(f).getCost());
        }
        return ifpc;
    }

    @Override
    public boolean calcNodes() {
        return true;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        dMax = (Double)params.get("Dmax");
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        return new LinkedHashMap(Collections.singletonMap("Dmax", (Object)dMax));
    }

    @Override
    public ParamPanel getParamPanel(Linkset linkset) {
        return new SingleValuePanel("Dmax", 100);
    }

    @Override
    public Type getType() {
        return Type.RASTER;
    }
}
