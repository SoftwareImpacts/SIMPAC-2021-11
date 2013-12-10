/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.model;

import au.com.bytecode.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.geometry.DirectPosition;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.DefaultFeatureCoverage;
import org.thema.drawshape.feature.Feature;
import org.thema.graphab.graph.DeltaAddGraphGenerator;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.model.Logistic.LogisticFunction;
import org.thema.graphab.MainFrame;
import org.thema.graphab.links.Path;
import org.thema.graphab.Project;
import org.thema.graphab.links.SpacePathFinder;
import org.thema.graphab.metric.local.LocalMetric;

/**
 *
 * @author gvuidel
 */
public class DiffLocalModel {
    
   
    
    public static HashMap<DefaultFeature, Double> diff(final Project project, Pointset exoData, String varName, GraphGenerator graph,
            final List<String> vars, final double [] coefs, final double alpha,
            final Map<String, GridCoverage2D> extVars, final boolean multiAttach,
            final double dMax, List<DefaultFeature> removalZones, TaskMonitor monitor) throws Throwable {
        
         
        if(!multiAttach)
            throw new IllegalArgumentException("Works only in multi attachment");
        
        HashMap<Geometry, HashMap<DefaultFeature, Path>> cache = multiAttach ? new HashMap<Geometry, HashMap<DefaultFeature, Path>>() : null;
        SpacePathFinder pathfinder = multiAttach ? project.getPathFinder(exoData.getLinkset()) : null;
        
        LogisticFunction function = new LogisticFunction(coefs);
        
        double sumInit = 0;
        for(Feature f : exoData.getFeatures()) 
            if(((Number)f.getAttribute(varName)).doubleValue() == 1) {
            Coordinate coord = f.getGeometry().getCentroid().getCoordinate();

            HashMap<DefaultFeature, Path> patchDists = new HashMap<DefaultFeature, Path>();
            if(multiAttach) {          
                patchDists = pathfinder.calcPaths(coord, dMax, false);
                cache.put(f.getGeometry(), patchDists);
            }
            if(patchDists.isEmpty()) {
                continue;
//                int idPatch = ((Number)f.getAttribute(Project.EXO_IDPATCH)).intValue();
//                DefaultFeature patch = project.getPatch(idPatch);
//                double dist = ((Number)f.getAttribute(Project.EXO_COST)).doubleValue();
//                patchDists.put(patch, new Double[] {dist, dist});
            }
            
            double [] xVal = new double[coefs.length];
            xVal[0] = 1;
            int k = 1;
            for(String var : vars) {
                if(var.startsWith("ext-"))
                    try {
                        xVal[k] = extVars.get(var).evaluate((DirectPosition)new DirectPosition2D(coord.x, coord.y), new double[1])[0];
                    } catch(PointOutsideCoverageException ex) {
                        xVal[k] = Double.NaN;
                    }
                else {
                    double sum = 0;
                    double weight = 0;
                    for(DefaultFeature patch : patchDists.keySet()) {
                        double w = Math.exp(-alpha * (exoData.getLinkset().isCostLength() ? patchDists.get(patch).getCost() : patchDists.get(patch).getDist()));
                        sum += ((Number)patch.getAttribute(var)).doubleValue() * w * w;
                        weight += w;
                    }
                    xVal[k] = sum / weight;
                }
                k++;
            }

            sumInit += function.value((xVal));
        }
        
        HashMap<String, LocalMetric> localIndices = new HashMap<String, LocalMetric>();
        
        for(String var : vars) {
            if(!var.startsWith("ext-") && var.endsWith("_" + graph.getName())) {
                String indiceName = var.substring(0, var.length()-graph.getName().length()-1);
                for(LocalMetric indice : Project.LOCAL_METRICS)
                    if(indiceName.startsWith(indice.getShortName() + "_")) {
                         if(localIndices.values().contains(indice))
                            throw new RuntimeException(indice.getShortName() + " is dupplicated !");
                        localIndices.put(var, indice);
                    }
            }
        }
        
        CSVWriter writer = new CSVWriter(new FileWriter(new File(project.getProjectDir(), "sumproba.csv")));
        
        HashMap<DefaultFeature, Double> sumProbas = new HashMap<DefaultFeature, Double>();
        
        DefaultFeatureCoverage patchCoverage = new DefaultFeatureCoverage(project.getPatches());
        monitor.setMaximum(removalZones.size());
        int i = 0;
        for(DefaultFeature remZone : removalZones) {
            monitor.setNote("" + i++ + "/" + removalZones.size());
            List<Feature> remPatches = patchCoverage.getFeaturesIn(remZone.getGeometry());
            List remPatchesId = new ArrayList();
            for(Feature f : remPatches)
                remPatchesId.add(f.getId());
            DeltaAddGraphGenerator deltaGraph = new DeltaAddGraphGenerator(graph, remPatchesId, Collections.EMPTY_LIST);
            
            for(String indiceName : localIndices.keySet()) {
                LocalMetric indice = localIndices.get(indiceName);
                if(indice.hasParams())
                    indice.setParamFromDetailName(indiceName.substring(0, indiceName.length()-graph.getName().length()-1));
            }
            for(LocalMetric indice : localIndices.values()) {
                boolean stop = MainFrame.calcLocalIndice(monitor.getSubMonitor(0, 100, 1), deltaGraph, indice, Double.NaN);
                if(stop)
                    throw new CancellationException();
            }
            
            double sumProba = 0;
            for(Feature f : exoData.getFeatures()) 
            if(((Number)f.getAttribute(varName)).doubleValue() == 1) {
                Coordinate coord = f.getGeometry().getCentroid().getCoordinate();

                HashMap<DefaultFeature, Path> patchDists = new HashMap<DefaultFeature, Path>();
                if(multiAttach) {
                    patchDists = new HashMap<DefaultFeature, Path>(cache.get(f.getGeometry()));
                    patchDists.keySet().removeAll(remPatches);
                }
                
                if(patchDists.isEmpty()) {
                    continue;
//                    int idPatch = ((Number)f.getAttribute(Project.EXO_IDPATCH)).intValue();
//                    DefaultFeature patch = project.getPatch(idPatch);
//                    double dist = ((Number)f.getAttribute(Project.EXO_COST)).doubleValue();
//                    patchDists.put(patch, new Double[] {dist, dist});
                }

                double [] xVal = new double[coefs.length];
                xVal[0] = 1;
                int k = 1;
                for(String var : vars) {
                    if(var.startsWith("ext-"))
                        try {
                            xVal[k] = extVars.get(var).evaluate((DirectPosition)new DirectPosition2D(coord.x, coord.y), new double[1])[0];
                        } catch(PointOutsideCoverageException ex) {
                            xVal[k] = Double.NaN;
                        }
                    else {
                        if(localIndices.containsKey(var)) 
                            var = var.replace("_" + graph.getName(), "_" + deltaGraph.getName());
                        
                        double sum = 0;
                        double weight = 0;
                        for(DefaultFeature patch : patchDists.keySet()) {
                            double w = Math.exp(-alpha * (exoData.getLinkset().isCostLength() ? patchDists.get(patch).getCost() : patchDists.get(patch).getDist()));
                            sum += ((Number)patch.getAttribute(var)).doubleValue() * w * w;
                            weight += w;
                        }
                        xVal[k] = sum / weight;
                    }
                    k++;
                }

                sumProba += function.value((xVal));

            }
            List<String> attrs = new ArrayList<String>();
            for(Object attr : remZone.getAttributes())
                attrs.add(attr.toString());
            attrs.add(String.valueOf(sumProba));
            attrs.add(String.valueOf(sumInit));
            attrs.add(String.valueOf(sumProba - sumInit));
            attrs.add(String.valueOf((sumProba - sumInit) / sumInit));
            writer.writeNext(attrs.toArray(new String[0]));
            writer.flush();
            
            sumProbas.put(remZone, sumProba);
            
        }
        
        writer.close();
        
        return sumProbas;
        
    }
}
