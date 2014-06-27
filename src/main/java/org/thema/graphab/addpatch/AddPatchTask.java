/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.addpatch;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.thema.common.ProgressBar;
import org.thema.common.collection.TreeMapList;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.parallel.AbstractParallelTask;

/**
 *
 * @author gvuidel
 */
public class AddPatchTask extends AbstractParallelTask<TreeMapList<Double, Geometry>, TreeMapList<Double, Geometry>> 
                    implements Serializable{

    // Patch to be added at init
    Geometry addedGeom;
    double capaGeom;
    
    HashMap<Geometry, Double> testGeoms;
    List<Geometry> geoms;
    GlobalMetric indice;
    String graphName;
    
    transient GraphGenerator gen;
    transient TreeMapList<Double, Geometry> result;

    public AddPatchTask(Geometry addedGeom, double capaGeom, String graphName, GlobalMetric indice, 
            HashMap<Geometry, Double> testGeoms, ProgressBar monitor) {
        super(monitor);
        this.addedGeom = addedGeom;
        this.capaGeom = capaGeom;
        this.indice = indice;
        this.graphName = graphName;
        this.testGeoms = testGeoms;
        geoms = new ArrayList<Geometry>(testGeoms.keySet());
    }

    @Override
    public void init() {
        super.init();
        try {
            // si il y a un patch à ajouter et qu'il n'a pas encore été ajouté
            // utile seulement pour MPI
            if(addedGeom != null && Project.getProject().canCreatePatch(addedGeom)) {
                // add the new patch to the project and the graph
                DefaultFeature patch = Project.getProject().addPatch(addedGeom, capaGeom);
                gen.getLinkset().addLinks(patch);
            }
        } catch (Exception ex) {
            Logger.getLogger(AddPatchTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        
        gen = new GraphGenerator(Project.getProject().getGraph(graphName), "");
    }

    @Override
    public TreeMapList<Double, Geometry> execute(int start, int end) {
        TreeMapList<Double, Geometry> results = new TreeMapList<Double, Geometry>();
        for(Geometry geom : geoms.subList(start, end)) {
            try {
                double indVal = addPatchSoft(geom, indice, gen, testGeoms.get(geom));
                if(!Double.isNaN(indVal))
                    results.putValue(indVal, geom);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return results;
    }

    @Override
    public void gather(TreeMapList<Double, Geometry> results) {
        if(result == null)
            result = new TreeMapList<Double, Geometry>();
        for(Double val : results.keySet())
            for(Geometry g : results.get(val))
                result.putValue(val, g);
    }
    
    @Override
    public int getSplitRange() {
        return testGeoms.size();
    }
    
    @Override
    public TreeMapList<Double, Geometry> getResult() {
        return result;
    }
    
    public static double addPatchSoft(Point point, GlobalMetric indice, GraphGenerator gen, GridCoverage2D capaCov) throws Exception {
        double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(point.getX(), point.getY()), new double[1])[0];
        return addPatchSoft(point, indice, gen, capa);
    }
    
    public static double addPatchSoft(Geometry geom, GlobalMetric indice, GraphGenerator gen, double capa) throws Exception {
        Project project = Project.getProject();
        if(!project.canCreatePatch(geom))
            return Double.NaN;

        if(capa <= 0)
            return Double.NaN;
        AddPatchGraphGenerator graph = new AddPatchGraphGenerator(gen);
        graph.addPatch(geom, capa);
        
        double indVal = new GraphMetricLauncher(indice, false).calcIndice(graph, new TaskMonitor.EmptyMonitor())[0];
        
        return indVal;
    }
    
}
