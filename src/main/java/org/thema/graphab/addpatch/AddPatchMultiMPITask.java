/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.addpatch;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.thema.common.collection.TreeMapList;
import org.thema.parallel.AbstractParallelTask;
import org.thema.common.ProgressBar;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.global.GlobalMetric;

/**
 *
 * @author gvuidel
 */
public class AddPatchMultiMPITask extends AbstractParallelTask<TreeMapList<Double, Set<Point>>, TreeMapList<Double, Set<Point>>> 
                    implements Serializable{

    // Patches to be added at init
    HashMap<Point, Double> addPoints;
    
    int nbMultiPatch;
    int windowSize;
    double res;
    GridCoverage2D capaCov;
    List<Coordinate> testPoints;
    GlobalMetric indice;
    String graphName;
    double indInit;
    
    transient GraphGenerator gen;
    transient TreeMapList<Double, Set<Point>> result;

    public AddPatchMultiMPITask(HashMap<Point, Double> addPoints, GraphGenerator gen, GlobalMetric indice, double indInit, double res, int nbMultiPatch, int windowSize,
            List<Coordinate> testPoints, GridCoverage2D capaCov, ProgressBar monitor) {
        super(monitor);
        this.addPoints = addPoints;
        this.indice = indice;
        this.gen = gen;
        this.graphName = gen.getName();
        this.indInit = indInit;
        this.res = res;
        this.nbMultiPatch = nbMultiPatch;
        this.windowSize = windowSize;
        this.testPoints = testPoints;
        this.capaCov = capaCov;
    }

    @Override
    public void init() {
        super.init();
        gen = Project.getProject().getGraph(graphName);
        try {
            // si il y a des patch à ajouter 
            if(addPoints != null)
                for(Point p : addPoints.keySet())
                    // on vérifie qu'il n'a pas déjà été ajouté (c'est le cas pour le master process)
                    if(Project.getProject().canCreatePatch(p)) {
                        // add the new patch to the project and the graph
                        DefaultFeature patch = Project.getProject().addPatch(p, addPoints.get(p));
                        gen.getLinkset().addLinks(patch);
                    }
        } catch (Exception ex) {
            Logger.getLogger(AddPatchMultiMPITask.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TreeMapList<Double, Set<Point>> execute(int start, int end) {
        TreeMapList<Double, Set<Point>> results = new TreeMapList<Double, Set<Point>>();
        for(Coordinate coord : testPoints.subList(start, end)) {
            Point p = new GeometryFactory().createPoint(coord);
            try {
                addPatchWindow(new LinkedList<Point>(Arrays.asList(p)), indInit, results, nbMultiPatch);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return results;
    }

    @Override
    public void gather(TreeMapList<Double, Set<Point>> results) {
        if(result == null)
            result = new TreeMapList<Double, Set<Point>>();
        for(Double val : results.keySet())
            for(Set<Point> p : results.get(val))
                result.putValue(val, p);
    }
    
    @Override
    public int getSplitRange() {
        return testPoints.size();
    }
    
    @Override
    public TreeMapList<Double, Set<Point>> getResult() {
        return result;
    }
  
    
    private double addPatchWindow(LinkedList<Point> points, double indInit, TreeMapList<Double, Set<Point>> pointIndices, int level) throws Exception {
        Project project = Project.getProject();
        Point point = points.getLast();
        if(!project.canCreatePatch(point))
            return Double.NaN;
        double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(point.getX(), point.getY()), new double[1])[0];
        if(capa <= 0)
            return Double.NaN;
        DefaultFeature patch = project.addPatch(point, capa);
        gen.getLinkset().addLinks(patch);
        GraphGenerator graph = new GraphGenerator(gen, "");
        double indVal = (new GraphMetricLauncher(indice, false).calcIndice(graph, new TaskMonitor.EmptyMonitor())[0]
                - indInit) / points.size();
        pointIndices.putValue(indVal, new HashSet<Point>(points));

        // si c'est le premier voisinage on ne calcule que la moitié (à partir du centre) 
        // sinon on calcule pour tout le voisinage (peut être amélioré mais pas évident)
        double x = point.getX() - (level != nbMultiPatch ? windowSize*res : 0);
        double y = point.getY() - (level != nbMultiPatch ? windowSize*res : 0);
        for(; y <= point.getY()+windowSize*res; y += res) {
            for(; x <= point.getX()+windowSize*res; x += res) {
                if(x == point.getX() && y == point.getY())
                    continue;
                Point p = new GeometryFactory().createPoint(new Coordinate(x, y));
                // si il ne reste qu'un voisinage à tester on le fait en soft sinon on récurre
                if(level == 2) {
                    double val = AddPatchTask.addPatchSoft(p, indice, gen, capaCov);
                    if(!Double.isNaN(val)) {
                        HashSet<Point> pointSet = new HashSet<Point>(points);
                        pointSet.add(p);
                        pointIndices.putValue((val-indInit)/pointSet.size(), pointSet);
                    }
                } else {
                    points.addLast(p);
                    addPatchWindow(points, indInit, pointIndices, level-1);
                    points.removeLast();
                }
            }
            // on redémarre complètement à gauche
            x = point.getX() - windowSize*res;
        }
        
        gen.getLinkset().removeLinks(patch);
        project.removePointPatch(patch);
        
        return indVal;
    }
    
}
