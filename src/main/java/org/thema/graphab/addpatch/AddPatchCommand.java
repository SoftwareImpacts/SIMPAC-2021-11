/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.thema.common.ProgressBar;
import org.thema.common.collection.TreeMapList;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.IOImage;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.parallel.ExecutorService;

/**
 *
 * @author gvuidel
 */
public class AddPatchCommand {
    
    // Common parameters
    private final int nbPatch;
    private final GlobalMetric indice;
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

    public AddPatchCommand(int nbPatch, GlobalMetric indice, GraphGenerator gen, File capaFile, double res, int nbMultiPatch, int windowSize) {
        this.nbPatch = nbPatch;
        this.indice = indice;
        this.gen = gen;
        this.capaFile = capaFile;
        this.res = res;
        this.nbMultiPatch = nbMultiPatch;
        this.windowSize = windowSize;
    }

    public AddPatchCommand(int nbPatch, GlobalMetric indice, GraphGenerator gen, File shapeFile, String capaField) {
        this.nbPatch = nbPatch;
        this.indice = indice;
        this.gen = gen;
        this.shapeFile = shapeFile;
        this.capaField = capaField;
    }
    
    public boolean isGridVersion() {
        return shapeFile == null;
    }
    
    public void run(ProgressBar bar) throws Exception {
        metricValues = new TreeMap<Integer, Double>();
        
        if(isGridVersion()) {
            addedPatches = addPatchGrid(bar, metricValues, saveDetail);
        } else {
            addedPatches = addPatchShp(bar, metricValues, saveDetail);
        }
    }

    public List<DefaultFeature> getAddedPatches() {
        return addedPatches;
    }

    public TreeMap<Integer, Double> getMetricValues() {
        return metricValues;
    }
    
    private List<DefaultFeature> addPatchSimple(HashMap<Geometry, Double> testGeoms,  
            ProgressBar mon, TreeMap<Integer, Double> indiceValues, boolean saveDetail) throws Exception {
        
        Project project = Project.getProject();

        mon.setMaximum(nbPatch);

        List<DefaultFeature> addedPatches = new ArrayList<DefaultFeature>();
            
        double currentInd = new GraphMetricLauncher(indice, true).calcIndice(gen, new TaskMonitor.EmptyMonitor())[0];
        indiceValues.put(0, currentInd);
        mon.incProgress(1);
        Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, "Initial " + indice.getShortName() + " : " + currentInd); 
        
        Geometry bestGeom = null;
        double capa = 0;
        
        for(int i = 0; i < nbPatch; i++) {
            AddPatchTask task = new AddPatchTask(bestGeom, capa, gen.getName(), indice, testGeoms, mon.getSubProgress(1));
            ExecutorService.execute(task);
            TreeMapList<Double, Geometry> patchIndices = task.getResult();
            
            List<Geometry> bestGeoms = patchIndices.lastEntry().getValue();
            bestGeom = bestGeoms.get((int)(Math.random()*bestGeoms.size()));

            int step = i+1;
            capa = testGeoms.get(bestGeom);
            DefaultFeature patch = project.addPatch(bestGeom, capa);
            gen.getLinkset().addLinks(patch);
            
            patch = new DefaultFeature(patch, true, true);
            patch.addAttribute("Etape", step);
            patch.addAttribute("delta-" + indice.getDetailName(), patchIndices.lastKey()-currentInd);
            patch.addAttribute(indice.getDetailName(), patchIndices.lastKey());
            addedPatches.add(patch);
            
            indiceValues.put(addedPatches.size(), patchIndices.lastKey());
            currentInd = patchIndices.lastKey();
            
            // check if we obtain the same result after adding the patch "truly"
            double test = new GraphMetricLauncher(indice).calcIndice(new GraphGenerator(gen, ""), null)[0];
            if(Math.abs(test - currentInd) > 1e-10) {
//                throw new RuntimeException("Error incoherence");
                System.err.println("PB " + currentInd + " - " + test);
            }
            
            if(saveDetail) {
                List<DefaultFeature> debug = new ArrayList<DefaultFeature>();
                for(Double val : patchIndices.keySet())
                    for(Geometry g : patchIndices.get(val)) {
                        debug.add(new DefaultFeature(g.getCentroid().getX()+","+g.getCentroid().getY()+" - " + (step), g, Arrays.asList("Etape", indice.getDetailName()), 
                                Arrays.asList(step, val)));
                    }
                File dir = getResultDir();
                dir = new File(dir, "detail");
                dir.mkdirs();
                DefaultFeature.saveFeatures(debug, new File(dir, "detail_" + step + ".shp"));
            }

            Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, 
                    "Step " + step + " : 1 added patches " + bestGeom.getCoordinate() + " from " + bestGeoms.size() + " best points  for " + indice.getShortName() + " = " + patchIndices.lastKey()); 
        }

        return addedPatches;
    }
    
    private List<DefaultFeature> addPatchShp(ProgressBar bar, TreeMap<Integer, Double> indiceValues, boolean saveDetail) throws Exception {
        List<DefaultFeature> points = DefaultFeature.loadFeatures(shapeFile, false);
        HashMap<Geometry, Double> testGeoms = new HashMap<Geometry, Double>();
        for(Feature f : points)
            testGeoms.put(f.getGeometry(), 
                    capaField == null ? 1 : ((Number)f.getAttribute(capaField)).doubleValue());

        return addPatchSimple(testGeoms, bar, indiceValues, saveDetail);
    }
    
    private List<DefaultFeature> addPatchGrid(ProgressBar mon, TreeMap<Integer, Double> indiceValues, boolean saveDetail) throws Exception {
        
        Project project = Project.getProject();
        Rectangle2D rect = project.getZone();
        double dx = rect.getWidth() - Math.floor((rect.getWidth()) / res) * res;
        double dy = rect.getHeight() - Math.floor((rect.getHeight()) / res) * res;
        rect = new Rectangle2D.Double(rect.getX()+dx/2, rect.getY()+dy/2,
            rect.getWidth()-dx, rect.getHeight()-dy);
        
        GridCoverage2D capaCov = null;
        try {
            if(capaFile != null)
                capaCov = capaFile.getName().toLowerCase().endsWith(".tif") ? IOImage.loadTiff(capaFile) : IOImage.loadArcGrid(capaFile);
            
            if(nbMultiPatch <= 1) {
                GeometryFactory geomFactory = new GeometryFactory();
                HashMap<Geometry, Double> testPoints = new HashMap<Geometry, Double>();
                for(double y = rect.getMinY()+res/2; y < rect.getMaxY(); y += res) 
                    for(double x = rect.getMinX()+res/2; x < rect.getMaxX(); x += res) 
                        testPoints.put(geomFactory.createPoint(new Coordinate(x, y)), capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(x, y), new double[1])[0]);
                return addPatchSimple(testPoints, mon, indiceValues, saveDetail);
            }
            
            HashMap<Coordinate, Double> testPoints = new HashMap<Coordinate, Double>();
            for(double y = rect.getMinY()+res/2; y < rect.getMaxY(); y += res) 
                for(double x = rect.getMinX()+res/2; x < rect.getMaxX(); x += res) 
                    testPoints.put(new Coordinate(x, y), capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(x, y), new double[1])[0]);
            
            // sinon on continue avec la version multipatch
            mon.setMaximum((int)(nbPatch*rect.getWidth()/res*rect.getHeight()/res));
            double currentInd = new GraphMetricLauncher(indice, true).calcIndice(gen, new TaskMonitor.EmptyMonitor())[0];
            indiceValues.put(0, currentInd);
            
            Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, "Initial " + indice.getShortName() + " : " + currentInd); 
            List<DefaultFeature> addedPatches = new ArrayList<DefaultFeature>();
            HashMap<Point, Double> lastAddedPoints = new HashMap<Point, Double>();
            for(int i = 0; addedPatches.size() < nbPatch; i++) {
                int step = i+1;
                TreeMapList<Double, Set<Point>> pointIndices;
                
                if(ExecutorService.isMPIExecutor()) {
                    AddPatchMultiMPITask task = new AddPatchMultiMPITask(
                            lastAddedPoints, gen, indice, currentInd, res, nbMultiPatch, windowSize, 
                            new ArrayList<Coordinate>(testPoints.keySet()), capaCov, mon.getSubProgress(1));
                    ExecutorService.execute(task);
                    pointIndices = task.getResult();
                } else {
                    pointIndices = new TreeMapList<Double, Set<Point>>();
                    for(Coordinate coord : testPoints.keySet()) {
                        if(mon.isCanceled())
                            throw new CancellationException();
                        Point p = new GeometryFactory().createPoint(coord);
                        addPatchWindow(new LinkedList<Point>(Arrays.asList(p)), indice, gen, capaCov, 
                            currentInd, res, nbMultiPatch, windowSize, pointIndices, nbMultiPatch);
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
                    patch.addAttribute("delta-" + indice.getDetailName(), pointIndices.lastKey());
                    patch.addAttribute(indice.getDetailName(), currentInd);
                    addedPatches.add(patch);
                    lastAddedPoints.put(best, capa);
                }

                indiceValues.put(addedPatches.size(), currentInd);
                
                if(saveDetail) {
                    List<DefaultFeature> debug = new ArrayList<DefaultFeature>();
                    for(Point p : bests) 
                        debug.add(new DefaultFeature(p.getX()+","+p.getY()+" - " + (step), p, Arrays.asList("Etape", indice.getDetailName()), 
                                Arrays.asList(step, currentInd)));
                        
                    File dir = getResultDir();
                    dir = new File(dir, "detail");
                    dir.mkdirs();
                    DefaultFeature.saveFeatures(debug, new File(dir, "detail_" + step + ".shp"));
                }

                Logger.getLogger(AddPatchCommand.class.getName()).log(Level.INFO, 
                        "Step " + step + " : " + bests.size() + " added patches" + " from " + bestPoints.size() + " best points sets  for " + indice.getShortName() + " = " + pointIndices.lastKey()); 
            }
            
            return addedPatches;
        } finally {
            if(capaCov != null)
                capaCov.dispose(true);
        }
    }
    
    public void saveResults() throws Exception {
        String name = gen.getName() + "_" + indice.getDetailName();
        File dir = getResultDir();
        dir.mkdir();
        FileWriter w = new FileWriter(new File(dir, "addpatch-" + name + ".txt"));
        w.write("#patch\tmetric value\n");
        for(Integer nbP : metricValues.keySet())
            w.write(nbP + "\t" + metricValues.get(nbP) + "\n");
        w.close();

        DefaultFeature.saveFeatures(addedPatches, new File(dir, "addpatch-" + name + ".shp"));
        GraphGenerator newGraph = new GraphGenerator(gen, "");
        DefaultFeature.saveFeatures(newGraph.getLinks(), new File(dir, "links-" + name + ".shp"));
        newGraph.getLayers().getEdgeLayer().exportToShapefile(new File(dir, "topo-links-" + name + ".shp"));
        
        new RasterLayer("", new RasterShape(Project.getProject().getImageSource(), Project.getProject().getZone(), new RasterStyle(), true), Project.getProject().getCRS())
                .saveRaster(new File(dir, "landuse.tif"));
    }
    
    public File getResultDir() {
        String name = gen.getName() + "_" + indice.getDetailName();
        if(isGridVersion())
            return new File(Project.getProject().getDirectory(), "addpatch_n" + nbPatch + "_" + name + 
                    "_res" + res + "_multi" + nbMultiPatch + "_" + windowSize);
        else
            return new File(Project.getProject().getDirectory(), "addpatch_n" + nbPatch + "_" + name + 
                    "_shp" + shapeFile.getName());
    }
    
    private static void addPatchWindow(final LinkedList<Point> points, final GlobalMetric indice, GraphGenerator gen, final GridCoverage2D capaCov, 
            final double indInit, double res, int nbMultiPatch, int windowSize,
            final TreeMapList<Double, Set<Point>> pointIndices, int level) throws Exception {
        Project project = Project.getProject();
        Point point = points.getLast();
        if(!project.canCreatePatch(point))
            return ;
        double capa = capaCov == null ? 1 : capaCov.evaluate(new Point2D.Double(point.getX(), point.getY()), new double[1])[0];
        if(capa <= 0)
            return ;
        DefaultFeature patch = project.addPatch(point, capa);
        gen.getLinkset().addLinks(patch);
        final GraphGenerator graph = new GraphGenerator(gen, "");
        double indVal = (new GraphMetricLauncher(indice, true).calcIndice(graph, new TaskMonitor.EmptyMonitor())[0]
                - indInit) / points.size();
        pointIndices.putValue(indVal, new HashSet<Point>(points));
        
        // si c'est le premier voisinage on ne calcule que la moitié (à partir du centre) 
        // sinon on calcule pour tout le voisinage (peut être amélioré mais pas évident)
        double x = point.getX() - (level != nbMultiPatch ? windowSize*res : 0);
        double y = point.getY() - (level != nbMultiPatch ? windowSize*res : 0);
        List<Point> coords = new ArrayList<Point>();
        for(; y <= point.getY()+windowSize*res; y += res) {
            for(; x <= point.getX()+windowSize*res; x += res)
                if(x != point.getX() || y != point.getY())
                    coords.add(new GeometryFactory().createPoint(new Coordinate(x, y)));
            // on redémarre complètement à gauche pas comme à l'initial (si level == nbMultiPatch)
            x = point.getX() - windowSize*res;
        }
        
        // si il ne reste qu'un voisinage à tester on le fait en soft pour le paralléliser sinon on récurre
        if(level == 2)       
            new ParallelFExecutor(new SimpleParallelTask<Point>(coords) {
                @Override
                protected void executeOne(Point p) {
                    try {
                        double indVal = AddPatchTask.addPatchSoft(p, indice, graph, capaCov);
                        if(!Double.isNaN(indVal)) {
                            HashSet<Point> pointSet = new HashSet<Point>(points);
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
        else // sinon on récurre
           for(Point p : coords) {
                points.addLast(p);
                addPatchWindow(points, indice, gen, capaCov, indInit, res, nbMultiPatch, windowSize, pointIndices, level-1);
                points.removeLast();
            }
                
        gen.getLinkset().removeLinks(patch);
        project.removePointPatch(patch);
        
    }
    
        
}
