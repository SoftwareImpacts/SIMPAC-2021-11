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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.SchemaException;
import org.thema.common.ProgressBar;
import org.thema.common.collection.TreeMapList;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.data.IOImage;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.global.GlobalMetricLauncher;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.parallel.ExecutorService;

/**
 * Main class for add patch feature.
 * 
 * @author Gilles Vuidel
 */
public class AddPatchCommand {
    
    // Common parameters
    private final int nbPatch;
    private final GlobalMetric metric;
    private final GraphGenerator gen;
    private final boolean saveDetail = true;
     
    // Grid parameters
    private File capaFile;
    private double res;
    private int nbMultiPatch;
    private int windowSize;
    
    // Shape file parameters
    private File shapeFile;
    private String capaField;
    
    
    // results
    private List<DefaultFeature> addedPatches;
    private TreeMap<Integer, Double> metricValues;

    /**
     * Creates an add patch command where patches will be tested on a grid.
     * The patch are created at the center of the grid cell with one pixel size.
     * 
     * @param nbPatch number of patches to add
     * @param metric the metric to maximize
     * @param gen the graph used, must be a complete graph
     * @param capaFile raster file containing capacity of the new patches
     * @param res the resolution of the grid (cell size)
     * @param nbMultiPatch number of patches tested simultaneously
     * @param windowSize the window size in cell when nbMultiPatch > 1
     */
    public AddPatchCommand(int nbPatch, GlobalMetric metric, GraphGenerator gen, File capaFile, double res, int nbMultiPatch, int windowSize) {
        this.nbPatch = nbPatch;
        this.metric = metric;
        this.gen = gen;
        this.capaFile = capaFile;
        this.res = res;
        this.nbMultiPatch = nbMultiPatch;
        this.windowSize = windowSize;
    }

    /**
     * Creates an add patch command where patches will be tested on a shapefile.
     * @param nbPatch number of patches to add
     * @param metric the metric to maximize
     * @param gen the graph used, must be a complete graph
     * @param shapeFile th shapefile of patches to test, can be point or polygon geometry
     * @param capaField the field name containing capacity, can be null
     */
    public AddPatchCommand(int nbPatch, GlobalMetric metric, GraphGenerator gen, File shapeFile, String capaField) {
        this.nbPatch = nbPatch;
        this.metric = metric;
        this.gen = gen;
        this.shapeFile = shapeFile;
        this.capaField = capaField;
    }
    
    /**
     * @return true if the patches are tested on a grid, false for a shapefile
     */
    public boolean isGridSampling() {
        return shapeFile == null;
    }
    
    /**
     * Launch the add patch calculation.
     * @param bar the progress bar
     * @throws IOException
     * @throws SchemaException 
     */
    public void run(ProgressBar bar) throws IOException, SchemaException {
        metricValues = new TreeMap<>();
        
        if(isGridSampling()) {
            addedPatches = addPatchGrid(bar, metricValues, saveDetail);
        } else {
            addedPatches = addPatchShp(bar, metricValues, saveDetail);
        }
        
        saveResults();
    }

    /**
     * {@link #run(org.thema.common.ProgressBar) } must be called before
     * @return the list of added patches
     */
    public List<DefaultFeature> getAddedPatches() {
        return addedPatches;
    }

    /**
     * {@link #run(org.thema.common.ProgressBar) } must be called before
     * @return the metric values after adding each patch
     */
    public TreeMap<Integer, Double> getMetricValues() {
        return metricValues;
    }
    
    /**
     * Launch the add patch calculation when adding one patch at each step
     * @param testGeoms the geometries of the patches to test with their capacity
     * @param mon the progress bar
     * @param metricValues the resulting metric values
     * @param saveDetail save detail or not ?
     * @return the list of added patches
     * @throws IOException
     * @throws SchemaException 
     */
    private List<DefaultFeature> addPatchSimple(HashMap<Geometry, Double> testGeoms,  
            ProgressBar mon, TreeMap<Integer, Double> metricValues, boolean saveDetail) throws IOException, SchemaException {
        
        Project project = gen.getProject();

        mon.setMaximum(nbPatch*100);

        List<DefaultFeature> addedPatches = new ArrayList<>();
            
        double currentInd = new GlobalMetricLauncher(metric).calcMetric(gen, true, null)[0];
        metricValues.put(0, currentInd);

        Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, "Initial " + metric.getShortName() + " : " + currentInd); 
        
        Geometry bestGeom = null;
        double capa = 0;
        
        for(int i = 0; i < nbPatch; i++) {
            AddPatchTask task = new AddPatchTask(bestGeom, capa, gen, metric, testGeoms, mon.getSubProgress(100));
            ExecutorService.execute(task);
            TreeMapList<Double, Geometry> patchIndices = task.getResult();
            if(patchIndices.isEmpty()) {
                Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, "No more patches can be added !");
                return addedPatches;
            }
            List<Geometry> bestGeoms = patchIndices.lastEntry().getValue();
            bestGeom = bestGeoms.get((int)(Math.random()*bestGeoms.size()));

            int step = i+1;
            capa = testGeoms.get(bestGeom);
            DefaultFeature patch = project.addPatch(bestGeom, capa);
            gen.getLinkset().addLinks(patch);
            
            patch = new DefaultFeature(patch, true, true);
            patch.addAttribute("Etape", step);
            patch.addAttribute("delta-" + metric.getDetailName(), patchIndices.lastKey()-currentInd);
            patch.addAttribute(metric.getDetailName(), patchIndices.lastKey());
            addedPatches.add(patch);
            
            metricValues.put(addedPatches.size(), patchIndices.lastKey());
            currentInd = patchIndices.lastKey();
            
            // check if we obtain the same result after adding the patch "truly"
            double test = new GlobalMetricLauncher(metric).calcMetric(new GraphGenerator(gen, ""), true, null)[0];
            double err = Math.abs(test - currentInd) / test;
            if(err > 1e-3) {
                throw new RuntimeException("Metric precision under 1e-3 : " + err + " - m1 " + currentInd + " - m2 " + test);
            }
            
            if(saveDetail) {
                List<DefaultFeature> debug = new ArrayList<>();
                for(Double val : patchIndices.keySet()) {
                    for(Geometry g : patchIndices.get(val)) {
                        debug.add(new DefaultFeature(g.getCentroid().getX()+","+g.getCentroid().getY()+" - " + (step), g, Arrays.asList("Etape", metric.getDetailName()), 
                                Arrays.asList(step, val)));
                    }
                }
                File dir = getResultDir();
                dir = new File(dir, "detail");
                dir.mkdirs();
                DefaultFeature.saveFeatures(debug, new File(dir, "detail_" + step + ".shp"));
            }

            Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, 
                    "Step " + step + " : 1 added patches " + bestGeom.getCoordinate() + " from " + bestGeoms.size() + " best points  for " + metric.getShortName() + " = " + patchIndices.lastKey()); 
        }

        return addedPatches;
    }
    
    /**
     * Launch the add patch calculation for a shapefile.
     * Prepare the data and call {@link #addPatchSimple(java.util.HashMap, org.thema.common.ProgressBar, java.util.TreeMap, boolean) }
     * @param bar the progress bar
     * @param metricValues the resulting metric values
     * @param saveDetail save detail or not ?
     * @return the list of added patches
     * @throws IOException
     * @throws SchemaException 
     */
    private List<DefaultFeature> addPatchShp(ProgressBar bar, TreeMap<Integer, Double> metricValues, boolean saveDetail) throws IOException, SchemaException  {
        List<DefaultFeature> points = DefaultFeature.loadFeatures(shapeFile, false);
        HashMap<Geometry, Double> testGeoms = new HashMap<>();
        for(Feature f : points) {
            testGeoms.put(f.getGeometry(), 
                    capaField == null ? 1 : ((Number)f.getAttribute(capaField)).doubleValue());
        }
        return addPatchSimple(testGeoms, bar, metricValues, saveDetail);
    }
    
    /**
     * Launch the add patch calculation for a grid.
     * Prepare the data and call {@link #addPatchSimple }
     * if nbMultiPatch == 1.
     * Calls {@link #addPatchWindow } if nbMultiPatch > 1 and no MPI environment.
     * Uses {@link AddPatchMultiMPITask } if nbMultiPatch > 1 with MPI environment.
     * @param bar the progress bar
     * @param metricValues the resulting metric values
     * @param saveDetail save detail or not ?
     * @return the list of added patches
     * @throws IOException
     * @throws SchemaException 
     */
    private List<DefaultFeature> addPatchGrid(ProgressBar mon, TreeMap<Integer, Double> indiceValues, boolean saveDetail) throws IOException, SchemaException  {
        Project project = gen.getProject();
        Rectangle2D rect = project.getZone();
        double dx = rect.getWidth() - Math.floor((rect.getWidth()) / res) * res;
        double dy = rect.getHeight() - Math.floor((rect.getHeight()) / res) * res;
        rect = new Rectangle2D.Double(rect.getX()+dx/2, rect.getY()+dy/2,
            rect.getWidth()-dx, rect.getHeight()-dy);
        
        GridCoverage2D capaCov = null;
        try {
            if(capaFile != null) {
                capaCov = capaFile.getName().toLowerCase().endsWith(".tif") ? IOImage.loadTiff(capaFile) : IOImage.loadArcGrid(capaFile);
            }
            if(nbMultiPatch <= 1) {
                GeometryFactory geomFactory = new GeometryFactory();
                HashMap<Geometry, Double> testPoints = new HashMap<>();
                for(double y = rect.getMinY()+res/2; y < rect.getMaxY(); y += res) {
                    for (double x = rect.getMinX()+res/2; x < rect.getMaxX(); x += res) {
                        testPoints.put(geomFactory.createPoint(new Coordinate(x, y)), capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(x, y), new double[1])[0]);
                    }
                }
                return addPatchSimple(testPoints, mon, indiceValues, saveDetail);
            }
            
            HashMap<Coordinate, Double> testPoints = new HashMap<>();
            for(double y = rect.getMinY()+res/2; y < rect.getMaxY(); y += res) {
                for (double x = rect.getMinX()+res/2; x < rect.getMaxX(); x += res) {
                    testPoints.put(new Coordinate(x, y), capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(x, y), new double[1])[0]);
                }
            }
            
            // sinon on continue avec la version multipatch
            mon.setMaximum((int)(nbPatch*rect.getWidth()/res*rect.getHeight()/res));
            double currentInd = new GlobalMetricLauncher(metric).calcMetric(gen, true, null)[0];
            indiceValues.put(0, currentInd);
            
            Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, "Initial " + metric.getShortName() + " : " + currentInd); 
            List<DefaultFeature> addedPatches = new ArrayList<>();
            HashMap<Point, Double> lastAddedPoints = new HashMap<>();
            for(int i = 0; addedPatches.size() < nbPatch; i++) {
                int step = i+1;
                TreeMapList<Double, Set<Point>> pointIndices;
                
                if(ExecutorService.isMPIExecutor()) {
                    AddPatchMultiMPITask task = new AddPatchMultiMPITask(
                            lastAddedPoints, gen, metric, currentInd, res, nbMultiPatch, windowSize, 
                            new ArrayList<>(testPoints.keySet()), capaCov, mon.getSubProgress(1));
                    ExecutorService.execute(task);
                    pointIndices = task.getResult();
                } else {
                    pointIndices = new TreeMapList<>();
                    for(Coordinate coord : testPoints.keySet()) {
                        if(mon.isCanceled()) {
                            throw new CancellationException();
                        }
                        Point p = new GeometryFactory().createPoint(coord);
                        addPatchWindow(new LinkedList<>(Arrays.asList(p)), capaCov, 
                            currentInd, pointIndices, nbMultiPatch);
                        mon.incProgress(1);
                    }
                }

                lastAddedPoints.clear();
                List<Set<Point>> bestPoints = pointIndices.lastEntry().getValue();
                Set<Point> bests = bestPoints.get((int)(Math.random()*bestPoints.size()));
                currentInd += pointIndices.lastKey()*bests.size();
                
                for(Point best : bests) {
                    double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(best.getX(), best.getY()), new double[1])[0];
                    DefaultFeature patch = project.addPatch(best, capa);
                    gen.getLinkset().addLinks(patch);
                    patch = new DefaultFeature(patch, true, true);
                    patch.addAttribute("Etape", step);
                    patch.addAttribute("delta-" + metric.getDetailName(), pointIndices.lastKey());
                    patch.addAttribute(metric.getDetailName(), currentInd);
                    addedPatches.add(patch);
                    lastAddedPoints.put(best, capa);
                }

                indiceValues.put(addedPatches.size(), currentInd);
                
                if(saveDetail) {
                    List<DefaultFeature> debug = new ArrayList<>();
                    for(Point p : bests) {
                        debug.add(new DefaultFeature(p.getX()+","+p.getY()+" - " + (step), p, Arrays.asList("Etape", metric.getDetailName()),
                                Arrays.asList(step, currentInd)));
                    }
                        
                    File dir = getResultDir();
                    dir = new File(dir, "detail");
                    dir.mkdirs();
                    DefaultFeature.saveFeatures(debug, new File(dir, "detail_" + step + ".shp"));
                }

                Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, 
                        "Step " + step + " : " + bests.size() + " added patches" + " from " + bestPoints.size() + " best points sets  for " + metric.getShortName() + " = " + pointIndices.lastKey()); 
            }
            
            return addedPatches;
        } finally {
            if(capaCov != null) {
                capaCov.dispose(true);
            }
        }
    }
    
    private void saveResults() throws IOException, SchemaException {
        String name = gen.getName() + "_" + metric.getDetailName();
        File dir = getResultDir();
        dir.mkdir();
        try (FileWriter w = new FileWriter(new File(dir, "addpatch-" + name + ".txt"))) {
            w.write("#patch\tmetric value\n");
            for(Integer nbP : metricValues.keySet()) {
                w.write(nbP + "\t" + metricValues.get(nbP) + "\n");
            }
        }

        DefaultFeature.saveFeatures(addedPatches, new File(dir, "addpatch-" + name + ".shp"));
        GraphGenerator newGraph = new GraphGenerator(gen, "");
        DefaultFeature.saveFeatures(newGraph.getLinks(), new File(dir, "links-" + name + ".shp"));
        newGraph.getLayers().getEdgeLayer().exportToShapefile(new File(dir, "topo-links-" + name + ".shp"));
        
        new RasterLayer("", new RasterShape(gen.getProject().getImageSource(), gen.getProject().getZone(), new RasterStyle(), true), gen.getProject().getCRS())
                .saveRaster(new File(dir, "landuse.tif"));
    }
    
    private File getResultDir() {
        String name = gen.getName() + "_" + metric.getDetailName();
        if(isGridSampling()) {
            return new File(gen.getProject().getDirectory(), "addpatch_n" + nbPatch + "_" + name + 
                    "_res" + res + "_multi" + nbMultiPatch + "_" + windowSize);
        } else {
            return new File(gen.getProject().getDirectory(), "addpatch_n" + nbPatch + "_" + name + 
                    "_shp" + shapeFile.getName());
        }
    }
    
    /**
     * Add several patches at the same time and calculates the gain of the metric by added patch.
     * This method is called recursively, adding one patch for each call.
     * @param points the added patches, the last one will be added
     * @param capaCov the grid containing the capacity
     * @param indInit the initial metric value
     * @param pointIndices the resulting metrics gain associated with the set of added patches
     * @param level the current level from nbMultiPatch to 2
     * @throws IOException 
     */
    private void addPatchWindow(final LinkedList<Point> points, final GridCoverage2D capaCov, final double indInit, 
            final TreeMapList<Double, Set<Point>> pointIndices, int level) throws IOException {
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
        final GraphGenerator graph = new GraphGenerator(gen, "");
        double indVal = (new GlobalMetricLauncher(metric).calcMetric(graph, true, null)[0]
                - indInit) / points.size();
        pointIndices.putValue(indVal, new HashSet<>(points));
        
        // si c'est le premier voisinage on ne calcule que la moitié (à partir du centre) 
        // sinon on calcule pour tout le voisinage (peut être amélioré mais pas évident)
        double x = point.getX() - (level != nbMultiPatch ? windowSize*res : 0);
        double y = point.getY() - (level != nbMultiPatch ? windowSize*res : 0);
        List<Point> coords = new ArrayList<>();
        for(; y <= point.getY()+windowSize*res; y += res) {
            for(; x <= point.getX()+windowSize*res; x += res) {
                if (x != point.getX() || y != point.getY()) {
                    coords.add(new GeometryFactory().createPoint(new Coordinate(x, y)));
                }
            }
            // on redémarre complètement à gauche pas comme à l'initial (si level == nbMultiPatch)
            x = point.getX() - windowSize*res;
        }
        
        // si il ne reste qu'un voisinage à tester on le fait en soft pour le paralléliser sinon on récurre
        if(level == 2) {     
            new ParallelFExecutor(new SimpleParallelTask<Point>(coords) {
                @Override
                protected void executeOne(Point p) {
                    try {
                        double indVal = AddPatchTask.addPatchSoft(p, metric, graph, capaCov);
                        if(!Double.isNaN(indVal)) {
                            HashSet<Point> pointSet = new HashSet<>(points);
                            pointSet.add(p);
                            synchronized(pointIndices) {
                                pointIndices.putValue((indVal-indInit)/pointSet.size(), pointSet);
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }).executeAndWait();
        } else { // sinon on récurre
           for(Point p : coords) {
                points.addLast(p);
                addPatchWindow(points, capaCov, indInit, pointIndices, level-1);
                points.removeLast();
            }
        } 
        gen.getLinkset().removeLinks(patch);
        project.removePointPatch(patch);
        
    }
    
        
}
