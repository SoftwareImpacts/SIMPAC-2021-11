/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.local;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.graph.structure.Graphable;
import org.thema.data.feature.Feature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Path;
import org.thema.graphab.Project;
import org.thema.graphab.links.RasterPathFinder;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.SingleValuePanel;

/**
 *
 * @author gvuidel
 */
public class IFPCMetric extends LocalMetric {

    double dMax = 100;
    private RasterPathFinder pathfinder;

    public String getName() {
        return "IFPC";
    }

    public String getShortName() {
        return getName();
    }

    @Override
    public synchronized double calcIndice(Graphable g, GraphGenerator gen) {
        Feature patch = (Feature) g.getObject();
        if(pathfinder == null)
            try {
                pathfinder = Project.getProject().getRasterPathFinder(gen.getLinkset());
            } catch (Exception ex) {
                Logger.getLogger(IFPCMetric.class.getName()).log(Level.SEVERE, null, ex);
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

    public void setParams(Map<String, Object> params) {
        dMax = (Double)params.get("Dmax");
    }

    public LinkedHashMap<String, Object> getParams() {
        return new LinkedHashMap(Collections.singletonMap("Dmax", (Object)dMax));
    }

    public ParamPanel getParamPanel(Project project) {
        return new SingleValuePanel("Dmax", 100);
    }

    @Override
    public void setParamFromDetailName(String detailName) {
        dMax = Double.parseDouble(detailName.substring(detailName.indexOf("Dmax") + 4));
    }

    @Override
    public Type getType() {
        return Type.RASTER;
    }
}
