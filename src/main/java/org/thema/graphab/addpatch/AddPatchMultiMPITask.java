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


package org.thema.graphab.addpatch;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.geotools.coverage.grid.GridCoverage2D;
import org.thema.common.ProgressBar;
import org.thema.common.collection.TreeMapList;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.global.GlobalMetricLauncher;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.graphab.mpi.MpiLauncher;
import org.thema.parallel.AbstractParallelTask;

/**
 * Parallel task testing the adding of a set of patches on a graph and calculates a metric for each.
 * This task is useful only for grid sampling with multi simultaneous patch adding in MPI environment.
 * Works only on MPI environment.
 * @author Gilles Vuidel
 */
public class AddPatchMultiMPITask extends AbstractParallelTask<TreeMapList<Double, Set<Point>>, TreeMapList<Double, Set<Point>>> 
                    implements Serializable {

    // Patches to be added at init
    private HashMap<Point, Double> addPoints;
    
    private int nbMultiPatch;
    private int windowSize;
    private double res;
    private GridCoverage2D capaCov;
    private List<Coordinate> testPoints;
    private GlobalMetric metric;
    private String graphName;
    private double indInit;
    
    private transient GraphGenerator gen;
    private transient TreeMapList<Double, Set<Point>> result;

    /**
     * Creates a new AddPatchMultiMPITask.
     * If nbMultiPatch == 1, use {@link AddPatchTask } instead. It is faster.
     * @param addPoints the patches added at a previous step with their capacity, can be null if no patch already added
     * @param gen the graph
     * @param metric the metric
     * @param indInit the initial metric value
     * @param res the resolution of the grid (cell size)
     * @param nbMultiPatch the number of patches added simultaneously
     * @param windowSize the window size in cell
     * @param testPoints the set of points to test
     * @param capaCov the grid containing capacity
     * @param monitor the progress bar
     */
    public AddPatchMultiMPITask(HashMap<Point, Double> addPoints, GraphGenerator gen, GlobalMetric metric, double indInit, double res, int nbMultiPatch, int windowSize,
            List<Coordinate> testPoints, GridCoverage2D capaCov, ProgressBar monitor) {
        super(monitor);
        this.addPoints = addPoints;
        this.metric = metric;
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
        if(gen == null) {
            gen = MpiLauncher.getProject().getGraph(graphName);
        }
        try {
            // si il y a des patch à ajouter 
            if(addPoints != null) {
                for(Point p : addPoints.keySet()) {
                    // on vérifie qu'il n'a pas déjà été ajouté (c'est le cas pour le master process)
                    if(gen.getProject().canCreatePatch(p)) {
                        // add the new patch to the project and the graph
                        DefaultFeature patch = gen.getProject().addPatch(p, addPoints.get(p));
                        gen.getLinkset().addLinks(patch);
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TreeMapList<Double, Set<Point>> execute(int start, int end) {
        TreeMapList<Double, Set<Point>> results = new TreeMapList<>();
        for(Coordinate coord : testPoints.subList(start, end)) {
            Point p = new GeometryFactory().createPoint(coord);
            try {
                addPatchWindow(new LinkedList<>(Arrays.asList(p)), results, nbMultiPatch);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return results;
    }

    @Override
    public void gather(TreeMapList<Double, Set<Point>> results) {
        if(result == null) {
            result = new TreeMapList<>();
        }
        for(Double val : results.keySet()) {
            for(Set<Point> p : results.get(val)) {
                result.putValue(val, p);
            }
        }
    }
    
    @Override
    public int getSplitRange() {
        return testPoints.size();
    }
    
    /**
     * @return the metrics gain by added patch associated with the set of added patches
     */
    @Override
    public TreeMapList<Double, Set<Point>> getResult() {
        return result;
    }
  
    /**
     * Add several patches at the same time and calculates the gain of the metric by added patch.
     * This method is called recursively, adding one patch for each call.
     * @param points the added patches, the last one will be added
     * @param pointMetrics the resulting metrics gain associated with the set of added patches
     * @param level the current level from nbMultiPatch to 2
     * @throws IOException 
     */
    private void addPatchWindow(LinkedList<Point> points, TreeMapList<Double, Set<Point>> pointMetrics, int level) throws IOException {
        Project project = gen.getProject();
        Point point = points.getLast();
        if(!project.canCreatePatch(point)) {
            return ;
        }
        double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(point.getX(), point.getY()), new double[1])[0];
        if(capa <= 0) {
            return ;
        }
        DefaultFeature patch = project.addPatch(point, capa);
        gen.getLinkset().addLinks(patch);
        GraphGenerator graph = new GraphGenerator(gen, "");
        double indVal = (new GlobalMetricLauncher(metric).calcMetric(graph, false, null)[0]
                - indInit) / points.size();
        pointMetrics.putValue(indVal, new HashSet<>(points));

        // si c'est le premier voisinage on ne calcule que la moitié (à partir du centre) 
        // sinon on calcule pour tout le voisinage (peut être amélioré mais pas évident)
        double x = point.getX() - (level != nbMultiPatch ? windowSize*res : 0);
        double y = point.getY() - (level != nbMultiPatch ? windowSize*res : 0);
        for(; y <= point.getY()+windowSize*res; y += res) {
            for(; x <= point.getX()+windowSize*res; x += res) {
                if(x == point.getX() && y == point.getY()) {
                    continue;
                }
                Point p = new GeometryFactory().createPoint(new Coordinate(x, y));
                // si il ne reste qu'un voisinage à tester on le fait en soft sinon on récurre
                if(level == 2) {
                    double val = AddPatchTask.addPatchSoft(p, metric, gen, capaCov);
                    if(!Double.isNaN(val)) {
                        HashSet<Point> pointSet = new HashSet<>(points);
                        pointSet.add(p);
                        pointMetrics.putValue((val-indInit)/pointSet.size(), pointSet);
                    }
                } else {
                    points.addLast(p);
                    addPatchWindow(points, pointMetrics, level-1);
                    points.removeLast();
                }
            }
            // on redémarre complètement à gauche
            x = point.getX() - windowSize*res;
        }
        
        gen.getLinkset().removeLinks(patch);
        project.removePointPatch(patch);
        
    }
    
}
