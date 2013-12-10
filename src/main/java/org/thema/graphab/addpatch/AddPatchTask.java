/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.addpatch;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.thema.common.TreeMapList;
import org.thema.common.distribute.AbstractDistributeTask;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.global.GlobalMetric;

/**
 *
 * @author gvuidel
 */
public class AddPatchTask extends AbstractDistributeTask<TreeMapList<Double, Point>, TreeMapList<Double, Point>> 
                    implements Serializable{

    // Patch to be added at init
    Point addedPoint;
    double capaPoint;
    
    HashMap<Coordinate, Double> testPoints;
    List<Coordinate> points;
    GlobalMetric indice;
    String graphName;
    
    transient GraphGenerator gen;
    transient TreeMapList<Double, Point> result;

    public AddPatchTask(Point addedPoint, double capaPoint, GraphGenerator gen, GlobalMetric indice, 
            HashMap<Coordinate, Double> testPoints, ProgressBar monitor) {
        super(monitor);
        this.addedPoint = addedPoint;
        this.capaPoint = capaPoint;
        this.indice = indice;
        this.gen = gen;
        this.graphName = gen.getName();
        this.testPoints = testPoints;
        points = new ArrayList<Coordinate>(testPoints.keySet());
    }

    @Override
    public void init() {
        super.init();
        gen = Project.getProject().getGraph(graphName);
        try {
            // si il y a un patch à ajouter et qu'il n'a pas encore été ajouté
            // utile seulement pour MPI
            if(addedPoint != null && Project.getProject().canCreatePatch(addedPoint)) {
                // add the new patch to the project and the graph
                DefaultFeature patch = Project.getProject().addPatch(addedPoint, capaPoint);
                gen.getLinkset().addLinks(patch);
            }
        } catch (Exception ex) {
            Logger.getLogger(AddPatchTask.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TreeMapList<Double, Point> execute(int start, int end) {
        TreeMapList<Double, Point> results = new TreeMapList<Double, Point>();
        for(Coordinate coord : points.subList(start, end)) {
            Point p = new GeometryFactory().createPoint(coord);
            try {
                double indVal = addPatchSoft(p, indice, gen, testPoints.get(coord));
                if(!Double.isNaN(indVal))
                    results.putValue(indVal, p);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return results;
    }

    @Override
    public void gather(TreeMapList<Double, Point> results) {
        if(result == null)
            result = new TreeMapList<Double, Point>();
        for(Double val : results.keySet())
            for(Point p : results.get(val))
                result.putValue(val, p);
    }
    
    @Override
    public int getSplitRange() {
        return testPoints.size();
    }
    
    @Override
    public TreeMapList<Double, Point> getResult() {
        return result;
    }
    
    public static double addPatchSoft(Point point, GlobalMetric indice, GraphGenerator gen, GridCoverage2D capaCov) throws Exception {
        double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(point.getX(), point.getY()), new double[1])[0];
        return addPatchSoft(point, indice, gen, capa);
    }
    
    public static double addPatchSoft(Point point, GlobalMetric indice, GraphGenerator gen, double capa) throws Exception {
        Project project = Project.getProject();
        if(!project.canCreatePatch(point))
            return Double.NaN;

        if(capa <= 0)
            return Double.NaN;
        AddPatchGraphGenerator graph = new AddPatchGraphGenerator(new GraphGenerator(gen, ""));
        graph.addPatch(point, capa);

        double indVal = new GraphMetricLauncher(indice, false).calcIndice(graph, new TaskMonitor.EmptyMonitor())[0];
        
        return indVal;
    }
    
}
