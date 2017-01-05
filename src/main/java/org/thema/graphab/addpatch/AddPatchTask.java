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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * Parallel task testing the adding of one patch at a time (from a set of patches) on a graph and calculates a metric for each.
 * Works in threaded and MPI environment.
 * @author Gilles Vuidel
 */
public class AddPatchTask extends AbstractParallelTask<TreeMapList<Double, Geometry>, TreeMapList<Double, Geometry>> 
                    implements Serializable {

    // Patch to be added at init, useful only for MPI env
    private Geometry addedGeom;
    private double capaGeom;
    
    private HashMap<Geometry, Double> testGeoms;
    private List<Geometry> geoms;
    private GlobalMetric metric;
    private String graphName;
    
    private transient GraphGenerator gen;
    private transient TreeMapList<Double, Geometry> result;

    /**
     * Creates a new AddPatchTask.
     * 
     * @param addedGeom geometry of the previous added patch, can be null if no patch already added
     * @param capaGeom capacity of the previous added patch, can be Double.NaN if no patch already added
     * @param gen the graph
     * @param metric the metric to maximize
     * @param testGeoms the set of patch geometries to test, with their capacity
     * @param monitor th progress bar
     */
    public AddPatchTask(Geometry addedGeom, double capaGeom, GraphGenerator gen, GlobalMetric metric, 
            HashMap<Geometry, Double> testGeoms, ProgressBar monitor) {
        super(monitor);
        this.addedGeom = addedGeom;
        this.capaGeom = capaGeom;
        this.metric = metric;
        this.gen = gen;
        this.graphName = gen.getName();
        this.testGeoms = testGeoms;
        geoms = new ArrayList<>(testGeoms.keySet());
    }

    @Override
    public void init() {
        super.init();
        // useful for mpi only cause gen is transient
        if(gen == null) {
            gen = MpiLauncher.getProject().getGraph(graphName);
        }
        try {
            // si il y a un patch à ajouter et qu'il n'a pas encore été ajouté
            // utile seulement pour MPI
            if(addedGeom != null && gen.getProject().canCreatePatch(addedGeom)) {
                // add the new patch to the project and the graph
                DefaultFeature patch = gen.getProject().addPatch(addedGeom, capaGeom);
                gen.getLinkset().addLinks(patch);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        gen = new GraphGenerator(gen, "");
    }

    @Override
    public TreeMapList<Double, Geometry> execute(int start, int end) {
        TreeMapList<Double, Geometry> results = new TreeMapList<>();
        for(Geometry geom : geoms.subList(start, end)) {
            try {
                double indVal = addPatchSoft(geom, metric, gen, testGeoms.get(geom));
                if(!Double.isNaN(indVal)) {
                    results.putValue(indVal, geom);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return results;
    }

    @Override
    public void gather(TreeMapList<Double, Geometry> results) {
        if(result == null) {
            result = new TreeMapList<>();
        }
        for(Double val : results.keySet()) {
            for (Geometry g : results.get(val)) {
                result.putValue(val, g);
            }
        }
    }
    
    @Override
    public int getSplitRange() {
        return testGeoms.size();
    }
    
    /**
     * @return the metric value associated with each patch geometry
     */
    @Override
    public TreeMapList<Double, Geometry> getResult() {
        return result;
    }
    
    /**
     * Add a patch to the graph and calculate the metric.
     * The graph gen is dupplicated before adding the patch.
     * The project is not modified : soft method.
     * @param point the point representing the patch
     * @param metric the metric
     * @param gen the graph
     * @param capaCov the grid containing the capacity
     * @return the value of the metric after adding the patch to the graph or NaN if the patch cannot be added or if capacity &lt;= 0
     * @throws IOException 
     */
    public static double addPatchSoft(Point point, GlobalMetric metric, GraphGenerator gen, GridCoverage2D capaCov) throws IOException {
        double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(point.getX(), point.getY()), new double[1])[0];
        return addPatchSoft(point, metric, gen, capa);
    }
    
    /**
     * Add a patch to the graph and calculate the metric.
     * The graph gen is dupplicated before adding the patch.
     * The project is not modified : soft method.
     * @param geom the geometry of the patch
     * @param metric the metric
     * @param gen the graph
     * @param capa the capacity of the patch
     * @return the value of the metric after adding the patch to the graph or NaN if the patch cannot be added or if capacity &lt;= 0
     * @throws IOException 
     */
    public static double addPatchSoft(Geometry geom, GlobalMetric metric, GraphGenerator gen, double capa) throws IOException {
        Project project = gen.getProject();
        if(!project.canCreatePatch(geom)) {
            return Double.NaN;
        }

        if(capa <= 0) {
            return Double.NaN;
        }
        AddPatchGraphGenerator graph = new AddPatchGraphGenerator(gen);
        graph.addPatch(geom, capa);
        
        double indVal = new GlobalMetricLauncher(metric).calcMetric(graph, false, null)[0];
        
        return indVal;
    }
    
}
