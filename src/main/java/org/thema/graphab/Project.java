/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.thoughtworks.xstream.XStream;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.NoninvertibleTransformationException;
import com.vividsolutions.jts.index.strtree.ItemBoundable;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ProgressMonitor;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.feature.SchemaException;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.thema.GlobalDataStore;
import org.thema.common.Config;
import org.thema.common.Util;
import org.thema.common.io.IOImage;
import org.thema.common.parallel.*;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.feature.FeatureGetter;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.Layer;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.LineStyle;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graph.shape.GraphGroupLayer;
import org.thema.graphab.CapaPatchDialog.CapaPatchParam;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.pointset.PointsetDistanceDialog;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.*;
import org.thema.graphab.metric.Metric;
import org.thema.graphab.metric.global.*;
import org.thema.graphab.metric.local.*;
import org.thema.graphab.util.DistanceOp;
import org.thema.graphab.util.RSTGridReader;


/**
 *
 * @author gib
 */
public final class Project {
    
    public enum Method {GLOBAL, COMP, LOCAL, DELTA}

    public static final String CAPA_ATTR = "Capacity";
    public static final String AREA_ATTR = "Area";
    public static final String PERIM_ATTR = "Perim";

    public static final String EXO_IDPATCH = "IdPatch";
    public static final String EXO_COST = "Cost";
    
    private transient File dir;

    private String name;
    
    private TreeSet<Integer> codes;
    private int patchCode;
    private double noData;
    private boolean con8;
    /**
     * taille minimale pour un patch en unité du système de coordonnées
     * donc normalement m2
     */
    private double minArea;
    private boolean simplify;
    private CapaPatchDialog.CapaPatchParam capacityParams;

    private double resolution;
    private AffineTransformation grid2space, space2grid;

    private TreeMap<String, Linkset> costLinks;

    private TreeMap<String, Pointset> exoDatas;

    private TreeMap<String, GraphGenerator> graphs;

    private Rectangle2D zone;

    private transient List<DefaultFeature> patches;
    private transient List<Feature> voronoi;

    private transient Links planarLinks;

    private transient DefaultGroupLayer rootLayer, linkLayers, exoLayers, graphLayers, analysisLayers;
    private transient STRtree patchIndex;

    private transient double totalPatchArea, totalPatchCapacity;

    private transient HashMap<Integer, Integer> removedCodes;
    private transient Ref<WritableRaster> srcRaster;
    private transient Ref<WritableRaster> patchRaster;
    private transient HashMap<File, SoftRef<Raster>> extCostRasters;
    
    private transient List<Coordinate> centroids;

    public Project(String name, File prjPath, GridCoverage2D cov, TreeSet<Integer> codes, int code,
            double noData, boolean con8, double minArea, boolean simplify) throws Exception {

        this.name = name;
        dir = prjPath;
        this.codes = codes;
        patchCode = code;
        this.noData = noData;
        this.con8 = con8;
        this.minArea = minArea;
        this.simplify = simplify;
        capacityParams = new CapaPatchDialog.CapaPatchParam();

        //GridCoverage2D cov = IOImage.loadTiff(imgFile);

        Envelope2D gZone = cov.getEnvelope2D();
        zone = gZone.getBounds2D();

        TreeMap<Integer, Envelope> envMap = new TreeMap<Integer, Envelope>();

        WritableRaster rasterPatchs = extractPatch(cov.getRenderedImage(), (int)patchCode, noData, con8, envMap);      

        Logger.getLogger(MainFrame.class.getName()).log(Level.INFO, "Nb patch : " + envMap.size());

        GeometryFactory geomFac = new GeometryFactory();
        GridEnvelope2D range = cov.getGridGeometry().getGridRange2D();
        grid2space = new AffineTransformation(zone.getWidth() / range.getWidth(), 0,
                    zone.getMinX() - zone.getWidth() / range.getWidth(),
                0, -zone.getHeight() / range.getHeight(),
                    zone.getMaxY() + zone.getHeight() / range.getHeight());
        try {
            space2grid = grid2space.getInverse();
        } catch (NoninvertibleTransformationException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
        resolution = grid2space.getMatrixEntries()[0];

        Envelope2D extZone = new Envelope2D(gZone.getCoordinateReferenceSystem(),
                gZone.x-resolution, gZone.y-resolution, gZone.width+2*resolution, gZone.height+2*resolution);

        TaskMonitor monitor = new TaskMonitor(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Vectorizing..."), "", 0, envMap.size());
        patches = new ArrayList<DefaultFeature>();
        int i = 0, n = 1, nbRem = 0;
        List<String> attrNames = new ArrayList<String>(Arrays.asList("Id", AREA_ATTR, PERIM_ATTR, CAPA_ATTR));

        for(Integer id : envMap.keySet()) {
            Geometry g = geomFac.toGeometry(envMap.get(id));
            g.apply(grid2space);
            if(minArea == 0 || (g.getArea() / minArea) > (1-1E-9)) {
                Geometry geom = vectorize(rasterPatchs, envMap.get(id), id.doubleValue());
                geom.apply(grid2space);
                if(minArea == 0 || (geom.getArea() / minArea) > (1-1E-9)) {
                    List lst = new ArrayList(Arrays.asList(n, geom.getArea(), geom.getLength(), geom.getArea()));
                    patches.add(new DefaultFeature(n, geom, attrNames, lst));
                    recodePatch(rasterPatchs, geom, id, n);
                    n++;
                }
                else {
                    recodePatch(rasterPatchs, geom, id, 0);
                    nbRem++;
                }
            } else {
                recodePatch(rasterPatchs, g, id, 0);
                nbRem++;
            }

            monitor.setProgress(i++);
            monitor.setNote("" + i + " / " + envMap.size());
            if(monitor.isCanceled())
                throw new InterruptedException();
        }

        if(patches.isEmpty())
            throw new IllegalStateException("There is no patch in the map. Check patch code and min area.");
        
        monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));

        GridCoverageBuilder covBuilder = new GridCoverageBuilder();
        covBuilder.setEnvelope(extZone);
        covBuilder.setBufferedImage(new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
                    false, false, ColorModel.OPAQUE, DataBuffer.TYPE_INT),
                rasterPatchs, false, null));
        covBuilder.newVariable("Cluster", null);
        covBuilder.setSampleRange(1, patches.size());
        GridCoverage2D clustCov = covBuilder.getGridCoverage2D();
        new GeoTiffWriter(new File(prjPath, "source.tif")).write(cov, null);
        new GeoTiffWriter(new File(prjPath, "patches.tif")).write(clustCov, null);
        DefaultFeature.saveFeatures(patches, new File(dir, "patches.shp"));
        
        clustCov = null;
        covBuilder = null;

        monitor.close();
        Logger.getLogger(MainFrame.class.getName()).log(Level.INFO, "Nb small patch removed : " + nbRem);

//        rootLayer = new DefaultGroupLayer(name);
//
////        if(debug)
////            debugLayers = new DefaultGroupLayer("Debug");
//
//        rootLayer.addLayerFirst(new FeatureLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Patch"), patches,
//                new FeatureStyle(Color.BLUE, Color.GRAY)));
//
//        linkLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Link_sets"));
//        rootLayer.addLayerFirst(linkLayers);
//        
////        addDebugLayer(new RasterLayer("Patch id", new RasterShape(
////                rasterPatchs, extZone, new RasterStyle(), true)));
//        RasterLayer layer = new RasterLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Landscape_map"), new RasterShape(cov.getRenderedImage(), zone));
//        layer.setVisible(false);
//        layer.getStyle().setNoDataValue(noData);
//        rootLayer.addLayerLast(layer);
//
////        if(debug)
////            rootLayer.addLayerLast(debugLayers);

//        WritableRaster voronoiR = debug ? getRasterPatch() : rasterPatchs;
//        neighborhoodEuclid(voronoiR, simplify);

        WritableRaster voronoiR = rasterPatchs;
        neighborhoodEuclid2(voronoiR);

        voronoi = (List<Feature>) vectorizeVoronoi(voronoiR);
        DefaultFeature.saveFeatures(voronoi, new File(prjPath, "voronoi.shp"));

//        addDebugLayer(new FeatureLayer("Voronoi-vector", voronoi));
//        addDebugLayer(new RasterLayer("Voronoi", new RasterShape(
//                    voronoiR, extZone, new RasterStyle(), true)));

        exoDatas = new TreeMap<String, Pointset>();

        graphs = new TreeMap<String, GraphGenerator>();

        costLinks = new TreeMap<String, Linkset>();
        
        removedCodes = new HashMap<Integer, Integer>();
        
        MainFrame.project = this;
        
        save();
        
        createLayers();
    }

//    public Links neighborhoodCost(WritableRaster voronoi, Raster code, double [] cost) throws Exception {
//        ProgressMonitor monitor = new ProgressMonitor(null, "Neighbor", "", 0, voronoi.getHeight());
//        monitor.setProgress(1);
//        RasterPathFinder pathfinder = new RasterPathFinder(voronoi, code, cost, grid2space);
//
//        for(int y = 0; y < voronoi.getHeight(); y++) {
//            monitor.setProgress(y);
//            monitor.setNote("" + y + " / " + voronoi.getHeight());
//            if(monitor.isCanceled())
//                throw new InterruptedException();
//            for(int x = 0; x < voronoi.getWidth(); x++)
//                if(voronoi.getSample(x, y, 0) == 0) {
//                    double [] res = pathfinder.calcPathNearestPatch(x, y);
//                    voronoi.setSample(x, y, 0, res[0]);
//                }
//         }
//
//        Links links = createLinks("tmp", voronoi, monitor);
//
//        monitor.close();
//
//        return links;
//    }

    private void neighborhoodEuclid(WritableRaster voronoi) throws Exception {

        TaskMonitor monitor = new TaskMonitor(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Neighbor"), "", 0, voronoi.getHeight());
        monitor.setProgress(1);
        STRtree index = new STRtree();
        final int NPOINTS = 50;
        GeometryFactory factory = new GeometryFactory();
        List<DefaultFeature> simpPatches = patches;
        if(simplify)
            simpPatches = simplify();
        for(Feature f : simpPatches) {
            Geometry geom = f.getGeometry();
            if(geom.getNumPoints() > NPOINTS) {
                for(int i = 0; i < geom.getNumGeometries(); i++) {
                    Polygon p = (Polygon)geom.getGeometryN(i);
                    Coordinate [] coords = p.getExteriorRing().getCoordinates();
                    int ind = 0;
                    while(ind < coords.length-1) {
                        LineString line = factory.createLineString(Arrays.copyOfRange(coords, ind,
                                ind+NPOINTS+1 >= coords.length-1 ? coords.length : ind+NPOINTS+1));
                        DefaultFeature df = new DefaultFeature(f.getId(), line, null, null);
                        index.insert(line.getEnvelopeInternal(), df);
                        ind += NPOINTS;
                    }
                    for(int j = 0; j < p.getNumInteriorRing(); j++)
                        index.insert(p.getInteriorRingN(j).getEnvelopeInternal(),
                                new DefaultFeature(f.getId(), p.getInteriorRingN(j), null, null));
                }
            } else
                index.insert(geom.getEnvelopeInternal(), f);
        }


        int nbOptim = 0, nb = 0;

        Coordinate c = new Coordinate();
        Envelope env = new Envelope();
        List items = null;
        //HashMap<Feature, Double> distMap = new HashMap<Feature, Double>();

//        WritableRaster voronoi = Raster.createWritableRaster(new BandedSampleModel(
//                DataBuffer.TYPE_INT, rasterPatchs.getWidth(), rasterPatchs.getHeight(), 1), null);
//        voronoi.setRect(rasterPatchs);

//                WritableRaster voronoi = ((WritableRaster)rasterPatchs).createWritableChild(0, 0, rasterPatchs.getWidth(), rasterPatchs.getHeight(),
//                                0, 0, null);

        long time = System.currentTimeMillis();

        for(int y = 0; y < voronoi.getHeight(); y++) {
            monitor.setProgress(y);
            monitor.setNote("" + y + " / " + voronoi.getHeight());
            if(monitor.isCanceled())
                throw new InterruptedException();
            for(int x = 0; x < voronoi.getWidth(); x++)
                if(voronoi.getSample(x, y, 0) == 0) {
                    nb++;
                    c.x = x+0.5; c.y = y+0.5;
                    grid2space.transform(c, c);
                    Feature nearest = null;

                    //distMap.clear();

                    double dist = 2*resolution;
                    double min = Double.MAX_VALUE, min2 = Double.MAX_VALUE;
                    while(nearest == null) {
                        dist *= 2;
                        env.init(c);
                        env.expandBy(dist);
                        min = dist;
                        items = index.query(env);
                        for(Object item : items) {
                            //Double d =distMap.get((Feature)item);
                            Double d = null;
                            if(d == null) {
                                d = Double.MAX_VALUE;
                                Geometry geom = ((Feature)item).getGeometry();
                                if(geom instanceof LineString)
                                    d = DistanceOp.distancePointLine(c, geom.getCoordinates());
                                else
                                    for(int g = 0; g < geom.getNumGeometries(); g++) {
                                        Polygon poly = ((Polygon)geom.getGeometryN(g));
                                        double dd = DistanceOp.distancePointLine(c, poly.getExteriorRing().getCoordinates());
                                        if(dd < d)
                                            d = dd;
                                        int n = poly.getNumInteriorRing();
                                        for(int i = 0; i < n; i++) {
                                            dd = DistanceOp.distancePointLine(c, poly.getInteriorRingN(i).getCoordinates());
                                            if(dd < d)
                                                d = dd;
                                        }
                                    }

                            }

                            if(d < min) {
                                if(nearest != null)
                                    min2 = min;
                                min = d;
                                nearest = (Feature) item;
                            }
                            else {
                                // en cas d'égalité prendre le patch d'id minimum
                                if(d == min && nearest != null) {
                                    if((Integer)((Feature)item).getId() < (Integer)nearest.getId())
                                        nearest = (Feature) item;
                                }
                                // on conserve le second patch le plus proche
                                if(d < min2 && d < dist)
                                    min2 = d;
                                //distMap.put((Feature)item, d);
                            }
                        }

                    }
                    double d = (((min2 == Double.MAX_VALUE ? dist : min2) - min) / (2 * resolution));
                    // on garde une précision au centième pour éviter les problèmes de précisions à 10-15...
                    d = Math.round(d*100) / 100;
                    for(int j = 0; j <= d; j++)
                        //for(int i = (int)-d+j; i <= d-j; i++) {
                        for(int i = (int)Math.round(-d+j); i <= Math.round(d-j); i++) {
                            final int xi = x+i;
                            final int yj = y+j;
                            if(xi >= 0 && yj >= 0 && xi < voronoi.getWidth() && yj < voronoi.getHeight())
                                if(voronoi.getSample(xi, yj, 0) == 0) {
                                    voronoi.setSample(xi, yj, 0, (Integer)nearest.getId());
                                    nbOptim++;
                                }
                        }
                    nbOptim--;

                }
        }


        System.out.println("Temps calcul : " + (System.currentTimeMillis() - time) / 1000);
        System.out.println("Nb Optimisé : " + nbOptim + " - normal : " + nb);

        planarLinks = createLinks("Links", voronoi, monitor);

        monitor.setNote("Saving...");

        DefaultFeature.saveFeatures(planarLinks.getFeatures(), new File(dir, "links.shp"));
        save();

        monitor.close();

    }
    
    // using strtree.nearest
    private void neighborhoodEuclid2(final WritableRaster voronoi) throws Exception {

        TaskMonitor monitor = new TaskMonitor(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Neighbor"), "", 0, voronoi.getHeight());
        monitor.setProgress(1);
        final STRtree index = new STRtree();
        final int NPOINTS = 50;
        final GeometryFactory factory = new GeometryFactory();
        List<DefaultFeature> simpPatches = patches;
        if(simplify)
            simpPatches = simplify();
        for(Feature f : simpPatches) {
            Geometry geom = f.getGeometry();
            if(geom.getNumPoints() > NPOINTS) {
                for(int i = 0; i < geom.getNumGeometries(); i++) {
                    Polygon p = (Polygon)geom.getGeometryN(i);
                    Coordinate [] coords = p.getExteriorRing().getCoordinates();
                    int ind = 0;
                    while(ind < coords.length-1) {
                        LineString line = factory.createLineString(Arrays.copyOfRange(coords, ind,
                                ind+NPOINTS+1 >= coords.length-1 ? coords.length : ind+NPOINTS+1));
                        DefaultFeature df = new DefaultFeature(f.getId(), line, null, null);
                        index.insert(line.getEnvelopeInternal(), df);
                        ind += NPOINTS;
                    }
                    for(int j = 0; j < p.getNumInteriorRing(); j++)
                        index.insert(p.getInteriorRingN(j).getEnvelopeInternal(),
                                new DefaultFeature(f.getId(), p.getInteriorRingN(j), null, null));
                }
            } else
                index.insert(geom.getEnvelopeInternal(), f);
        }


        SimpleParallelTask task = new SimpleParallelTask.IterParallelTask(voronoi.getHeight(), monitor) {
            @Override
            protected void executeOne(Integer y) {
                Coordinate c = new Coordinate();
                Envelope env = new Envelope();
        
                for(int x = 0; x < voronoi.getWidth(); x++)
                    if(voronoi.getSample(x, y, 0) == 0) {
                        c.x = x+0.5; c.y = y+0.5;
                        grid2space.transform(c, c);
                        env.init(c);
                        DefaultFeature f = new DefaultFeature("c", factory.createPoint(c));
                        Feature nearest = (Feature)index.nearestNeighbour(env, f, new ItemDistance() {
                            @Override
                            public double distance(ItemBoundable i1, ItemBoundable i2) {
                                Geometry g1 = ((Feature)i1.getItem()).getGeometry();
                                Geometry g2 = ((Feature)i2.getItem()).getGeometry();
                                Coordinate c = g1 instanceof Point ? ((Point)g1).getCoordinate() : ((Point)g2).getCoordinate();
                                Geometry geom = g1 instanceof Point ? g2 : g1;
                                if(geom instanceof LineString)
                                    return DistanceOp.distancePointLine(c, geom.getCoordinates());
                                else {
                                    double d = Double.MAX_VALUE;
                                    for(int g = 0; g < geom.getNumGeometries(); g++) {
                                        Polygon poly = ((Polygon)geom.getGeometryN(g));
                                        double dd = DistanceOp.distancePointLine(c, poly.getExteriorRing().getCoordinates());
                                        if(dd < d)
                                            d = dd;
                                        int n = poly.getNumInteriorRing();
                                        for(int i = 0; i < n; i++) {
                                            dd = DistanceOp.distancePointLine(c, poly.getInteriorRingN(i).getCoordinates());
                                            if(dd < d)
                                                d = dd;
                                        }
                                    }
                                    return d;
                                }
                            }
                        });

                        voronoi.setSample(x, y, 0, (Integer)nearest.getId());
                    }
            }
        };
        
        long time = System.currentTimeMillis();
        
        new ParallelFExecutor(task).executeAndWait();

        System.out.println("Temps calcul : " + (System.currentTimeMillis() - time) / 1000);

        planarLinks = createLinks("Links", voronoi, monitor);

        monitor.setNote("Saving...");

        DefaultFeature.saveFeatures(planarLinks.getFeatures(), new File(dir, "links.shp"));
        save();

        monitor.close();

    }

    public double[] getLastCosts() {
        for(Linkset cost : costLinks.values())
            if(cost.getType_dist() == Linkset.COST)
                return cost.getCosts();
        return null;
    }

    public Linkset getLinkset(String linkName) {
        return costLinks.get(linkName);
    }

    public Set<String> getLinksetNames() {
        return costLinks.keySet();
    }
    
    public Collection<Linkset> getLinksets() {
        return costLinks.values();
    }

    private Links createLinks(String name, Raster voronoiRaster, ProgressMonitor monitor) {
        monitor.setNote("Create link set...");
        monitor.setProgress(0);

        Path.newSetOfPaths();
        
        Links links = new Links(name, patches.size());

        for(int y = 1; y < voronoiRaster.getHeight()-1; y++) {
            monitor.setProgress(y);
            monitor.setNote("" + y + " / " + voronoiRaster.getHeight());
            for(int x = 1; x < voronoiRaster.getWidth()-1; x++) {
                int id = voronoiRaster.getSample(x, y, 0);
                if(id <= 0)
                    continue;

                Feature f = getPatch(id);
                int id1 = voronoiRaster.getSample(x-1, y, 0);
                if(id1 > 0 && id != id1) {
                    Feature f1 = getPatch(id1);
                    if(!links.isLinkExist(f, f1))
                        links.addLink(f, f1, new Path(f, f1));
                }
                id1  = voronoiRaster.getSample(x, y-1, 0);
                if(id1 > 0 && id != id1) {
                    Feature f1 = getPatch(id1);
                    if(!links.isLinkExist(f, f1))
                        links.addLink(f, f1, new Path(f, f1));
                }
            }
        }

        return links;
    }

    public void addLinkset(Linkset cost, boolean save) throws Throwable {
        List<Path> paths;
        if(cost.getType_dist() == Linkset.EUCLID)
            paths = calcEuclidLinkset(cost.getTopology() == Linkset.COMPLETE, cost.getDistMax());
        else if(cost.getType_dist() == Linkset.COST) {
            Raster rSrc = getImageSource();
            paths = calcCostLinkset(rSrc, cost.getCosts(), cost.getTopology() == Linkset.COMPLETE, cost.getDistMax(),
                    cost.isRemoveCrossPatch(), cost.isRealPaths());
        } else {
            Raster costRaster = loadExtCostRaster(cost.getExtCostFile());
            paths = calcCostLinkset(costRaster, null,cost.getTopology() == Linkset.COMPLETE, cost.getDistMax(),
                    cost.isRemoveCrossPatch(), cost.isRealPaths());
        }

        if(paths != null) {
            cost.setPaths(paths);
            costLinks.put(cost.getName(), cost);
            if(save) {
                if(cost.isRealPaths())
                    DefaultFeature.saveFeatures(paths, new File(dir, cost.getName() + "-links.shp"));
                save(); 
                saveLinks(cost.getName());
            }
            if(linkLayers != null) {
                Layer l = new LinkLayer(cost.getName());
                if(cost.getTopology() == Linkset.COMPLETE && cost.getDistMax() == 0)
                    l.setVisible(false);
                linkLayers.addLayerFirst(l);
            }
        }
    }

    public void addPointset(Pointset exoData, List<String> attrNames, List<DefaultFeature> features, boolean save) throws Exception, SchemaException {
        for(Feature f : features){
            Coordinate c = f.getGeometry().getCoordinate();
            if(!zone.contains(c.x, c.y))
                throw new RuntimeException("Point outside zone !");
        }

        attrNames = new ArrayList<String>(attrNames);
        attrNames.remove(Project.EXO_IDPATCH);
        attrNames.remove(Project.EXO_COST);

        if(exoData.isAgreg()) {
            for(String attr : attrNames)
                DefaultFeature.addAttribute(exoData.getName() + "." + attr, patches, Double.NaN);
            DefaultFeature.addAttribute(exoData.getName() + ".NbPoint", patches, 0);
        }

        // recréé les features avec les 2 attributs en plus
        List<DefaultFeature> tmpLst = new ArrayList<DefaultFeature>(features.size());
        List<String> attrs = new ArrayList<String>(attrNames);
        attrs.add(Project.EXO_IDPATCH);
        attrs.add(Project.EXO_COST);
        for(Feature f : features) {
            List lst = new ArrayList(attrs.size());
            for(String attr : attrNames) {
                Object v = f.getAttribute(attr);
                if(v instanceof String)
                    v = Double.parseDouble(v.toString());
                lst.add(v);
            }
            lst.add(-1); lst.add(-1.0);
            tmpLst.add(new DefaultFeature(f.getId(), f.getGeometry(), attrs, lst));
        }
        features = tmpLst;
        //        ProgressMonitor monitor = new TaskMonitor(null, "Add exogenous data...", "Point inside patch", 0, 2*features.size());
        ProgressBar monitor = Config.getProgressBar("Add point set", 2*features.size());

        for(DefaultFeature f : features) {
            Envelope env = f.getGeometry().getEnvelopeInternal();
            env.expandBy(1);
            int nb = 0;
            List patch = getPatchIndex().query(env);
            for(Object p : patch) {
                DefaultFeature fp = (DefaultFeature) p;
                if(fp.getGeometry().intersects(f.getGeometry())) {
//                   if(exoData.isAgreg()) {
//                       for(String attr : attrNames) {
//                           double val = ((Number)fp.getAttribute(exoData.name + "." + attr)).doubleValue();
//                           if(Double.isNaN(val)) val = 0;
//                           double s = 0;
//                           if(f.getAttribute(attr) != null)
//                            s = ((Number)f.getAttribute(attr)).doubleValue();
//                           fp.setAttribute(exoData.name + "." + attr, s + val);
//                       }
//                       fp.setAttribute(exoData.name + ".NbPoint", 1+(Integer)fp.getAttribute(exoData.name + ".NbPoint"));
//                   }
                   f.setAttribute(EXO_IDPATCH, fp.getId());
                   f.setAttribute(EXO_COST, 0);
                   nb++;
                }
            }
            if(nb > 1)
                Logger.getLogger(Project.class.getName()).log(Level.WARNING, "Point intersect " + nb + "patches !!");
            monitor.incProgress(1);
        }

//        monitor.setNote("Point outside patch...");

        SpacePathFinder pathFinder = getPathFinder(exoData.getCost());

        int nErr = 0;
        for(DefaultFeature f : features) {
            monitor.incProgress(1);
            if(monitor.isCanceled()) {
                monitor.setNote(monitor.getNote() + " - canceled");
                return;
            }
            if(((Number)f.getAttribute(EXO_IDPATCH)).intValue() == -1) {
                try {
                    double [] res = pathFinder.calcPathNearestPatch((Point)f.getGeometry());
                    DefaultFeature p = getPatch((int)res[0]);
                    double cost = exoData.getCost().isCostLength() ? res[1] : res[2];
                    f.setAttribute(EXO_IDPATCH, p.getId());
                    f.setAttribute(EXO_COST, cost);
                } catch(Exception e) {
                    nErr++;
                    Logger.getLogger(Project.class.getName()).log(Level.WARNING, "Chemin non calculé pour le point " + f.getId(), e);
                    continue;
                }
            }
            
            if(exoData.isAgreg()) {
                double alpha = -Math.log(0.05) / exoData.getMaxCost();
                HashMap<DefaultFeature, Path> distPatch = pathFinder.calcPaths(f.getGeometry().getCoordinate(), exoData.getMaxCost(), false);
                for(DefaultFeature p : distPatch.keySet()) {
                    for(String attr : attrNames) {
                       double val = ((Number)p.getAttribute(exoData.getName() + "." + attr)).doubleValue();
                       if(Double.isNaN(val)) val = 0;
                       double dist = exoData.getCost().isCostLength() ? distPatch.get(p).getCost() : distPatch.get(p).getDist();
                       double s = 0;
                       if(f.getAttribute(attr) != null)
                          s = ((Number)f.getAttribute(attr)).doubleValue() * Math.exp(-alpha*dist);
                       p.setAttribute(exoData.getName() + "." + attr, s + val);
                   }
                   p.setAttribute(exoData.getName() + ".NbPoint", 1+(Integer)p.getAttribute(exoData.getName() + ".NbPoint"));
                }
            }
            
        }

//        if(exoData.agregType == Pointset.AG_MEAN) // Calc mean
//            for(DefaultFeature f : patches) {
//                int nbPoint = ((Number)f.getAttribute(exoData.name+".NbPoint")).intValue();
//                if(nbPoint > 0) {
//                    for(String attr : attrNames) {
//                       double val = ((Number)f.getAttribute(exoData.name + "." + attr)).doubleValue();
//                       f.setAttribute(exoData.name + "." + attr, val / nbPoint);
//                   }
//                }
//            }

        List<DefaultFeature> exoFeatures = new ArrayList<DefaultFeature>();
        for(DefaultFeature f : features)
            if(((Number)f.getAttribute(EXO_IDPATCH)).intValue() > 0)
                exoFeatures.add(f);

        exoData.setFeatures(exoFeatures);
        exoDatas.put(exoData.getName(), exoData);
        
        if(save) {
            DefaultFeature.saveFeatures(exoFeatures, new File(dir, "Exo-" + exoData.getName() + ".shp"));
            savePatch();
            save();
        }

        if(exoLayers != null) {
            exoLayers.setExpanded(true);
            exoLayers.addLayerFirst(new ExoLayer(exoData.getName()));
        }
        monitor.close();

        if(nErr > 0)
            JOptionPane.showMessageDialog(null, nErr + " points have been removed -> no path found.");
    }

    public synchronized DefaultGroupLayer getRootLayer() {
        if(rootLayer == null)
            createLayers();
        return rootLayer;
    }
    
    public synchronized DefaultGroupLayer getGraphLayers() {
        if(graphLayers == null)
            createLayers();
        return graphLayers;
    }
    
    public synchronized DefaultGroupLayer getAnalysisLayer() {
        if(analysisLayers == null) {
            analysisLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle")
                                                        .getString("MainFrame.analysisMenu.text"), true);
            getRootLayer().addLayerLast(analysisLayers);
        }
        return analysisLayers;
    }

    public void addLayer(Layer l) {
        getRootLayer().addLayerLast(l);
    }

    public void removeLayer(Layer l) {
        getRootLayer().removeLayer(l);
    }

    public synchronized STRtree getPatchIndex() {
        if(patchIndex == null) {
            patchIndex = new STRtree();
            for(Feature f : patches)
                patchIndex.insert(f.getGeometry().getEnvelopeInternal(), f);
        }

        return patchIndex;
    }



    private List<DefaultFeature> simplify() {
        List<DefaultFeature> simpPatches = new ArrayList<DefaultFeature>();
        TaskMonitor monitor = new TaskMonitor(null, "Simplify", "", 0, patches.size());
        int i = 0;
        for(Feature f : patches) {
            monitor.setProgress(i++);
            if(monitor.isCanceled()) {
                return null;
            }
            simpPatches.add(new DefaultFeature(f.getId(), TopologyPreservingSimplifier.simplify(f.getGeometry(), resolution*1.5), null, null));
        }
        monitor.close();
//        addDebugLayer(new FeatureLayer("Simp patch", simpPatches));

        return simpPatches;
    }

//    private void addDebugLayer(Layer l) {
//        if(debugLayers != null) {
//            l.setVisible(false);
//            debugLayers.addLayerLast(l);
//        }
//    }

    public final DefaultFeature getPatch(int id) {
        return patches.get(id-1);
    }
    
    public synchronized Coordinate getCentroid(Feature patch) {
        if(centroids == null) {
            centroids = new ArrayList<Coordinate>();
            for(Feature p : getPatches())
                centroids.add(p.getGeometry().getInteriorPoint().getCoordinate());
        }
        return centroids.get((Integer)patch.getId()-1);
    }

    public final Feature getVoronoi(int id) {
        return getVoronoi().get(id-1);
    }

    public Rectangle2D getZone() {
        return zone;
    }

    private void recodePatch(WritableRaster rasterPatchs, Geometry patch, int oldCode, int newCode) {

        Envelope env = patch.getEnvelopeInternal();
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(space2grid);
        env = gEnv.getEnvelopeInternal();

        for(int i = (int)env.getMinY(); i <= env.getMaxY(); i++)
            for(int j = (int)env.getMinX(); j <= env.getMaxX(); j++)
                if(rasterPatchs.getSample(j, i, 0) == oldCode)
                    ((WritableRaster)rasterPatchs).setSample(j, i, 0, newCode);

    }

    private List<Path> calcCostLinkset(final Raster costRaster, final double [] cost,
            final boolean allLinks, final double dMax, final boolean removeCrossPath, final boolean realPath) throws Throwable {
        ProgressBar progressBar = Config.getProgressBar("Create linkset...");
        
        // calcule le voronoi en distance cout
//        Links tmpLinks = neighborhoodCost(rPatch, costRaster, cost);

        final Vector<Path> links = new Vector<Path>(patches.size() * 4);
        Path.newSetOfPaths();
        long start = System.currentTimeMillis();

        ParallelFTask task = new AbstractParallelFTask(progressBar) {
            @Override
            protected Object execute(int start, int end) {
                try {
                RasterPathFinder pathfinder = new RasterPathFinder(Project.this, costRaster, cost);
                for(Feature orig : patches.subList(start, end)) {
                    if(isCanceled())
                        throw new CancellationException();

                        HashMap<Feature, Path> paths;
                        if(allLinks) {
                            //Envelope env = dMax == 0 ? null : orig.getGeometry().buffer(dMax).getEnvelopeInternal();
                            paths = pathfinder.calcPaths(orig, dMax, realPath, false);
                        } else {
                            List<Feature> dests = new ArrayList<Feature>();
                            for(Integer dId : planarLinks.getNeighbors(orig))
                                if(((Integer)orig.getId()) < dId)
                                    dests.add(getPatch(dId));

                            if(dests.isEmpty())
                                continue;

                            paths = pathfinder.calcPaths(orig, dests);
                        }

                        for(Feature d : paths.keySet()) {
                            Path p = paths.get(d);
                            boolean add = true;
                            if(removeCrossPath && realPath) {
                                List lst = getPatchIndex().query(p.getGeometry().getEnvelopeInternal());
                                for(Object o : lst) {
                                    Feature f = (Feature) o;
                                    if(f != orig && f != d && f.getGeometry().intersects(p.getGeometry())) {
                                        add = false;
                                        break;
                                    }
                                }
                            }
                            if(add)
                                links.add(p);
                        }
                   
                    incProgress(1);
                }

                } catch(Exception e) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, e);
                }
                return null;
            }

            public int getSplitRange() {
                return patches.size();
            }
            public void finish(Collection results) {
            }
            public Object getResult() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        new ParallelFExecutor(task).executeAndWait();

        if(task.isCanceled())
            return null;

        System.out.println("Temps écoulé : " + (System.currentTimeMillis()-start));
        progressBar.close();
        return links;
    }

    private List<Path> calcEuclidLinkset(final boolean allLinks, final double dMax) throws Throwable {
        Path.newSetOfPaths();

        ProgressBar progressBar = Config.getProgressBar("Euclidean distances");
        
        final Vector<Path> links = new Vector<Path>(patches.size() * 4);
        final STRtree index = getPatchIndex();
        
        long start = System.currentTimeMillis();
        ParallelFTask task = new AbstractParallelFTask(progressBar) {
            @Override
            protected Object execute(int start, int end) {
                
                for(Feature orig : patches.subList(start, end)) {
                    if(isCanceled())
                        return null;
                    if(allLinks) {
                        List<DefaultFeature> nearPatches = patches;
                        if(dMax > 0) {
                            Envelope env = orig.getGeometry().getEnvelopeInternal();
                            env.expandBy(dMax);
                            nearPatches = (List<DefaultFeature>)index.query(env);
                        }
                        
                        for(Feature dest : nearPatches)
                            if(((Integer)orig.getId()) < (Integer)dest.getId()){
                                Path p = Path.createEuclidPath(orig, dest);
                                if(dMax == 0 || p.getDist() <= dMax)
                                    links.add(p);
                            }
                    } else
                        for(Integer dId : planarLinks.getNeighbors(orig)) {
                            Feature d = getPatch(dId);
                            if(((Integer)orig.getId()) < dId)
                                links.add(Path.createEuclidPath(orig, d));
                        }

                    incProgress(1);
                }
                return null;
            }

            public int getSplitRange() {
                return patches.size();
            }
            public void finish(Collection results) {
            }
            public Object getResult() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        new ParallelFExecutor(task).executeAndWait();

        if(task.isCanceled())
            return null;

        progressBar.close();
        System.out.println("Temps écoulé : " + (System.currentTimeMillis()-start));
        return links;
    }

    private List<? extends Feature> vectorizeVoronoi(Raster voronoi) {
        TreeMap<Integer, Envelope> envMap = new TreeMap<Integer, Envelope>();

        for(int j = 1; j < voronoi.getHeight()-1; j++)
            for(int i = 1; i < voronoi.getWidth()-1; i++)
                if(voronoi.getSample(i, j, 0) > 0) {
                    int id = voronoi.getSample(i, j, 0);
                    Envelope env = envMap.get(id);
                    if(env == null)
                        envMap.put(id, new Envelope(new Coordinate(i, j)));
                    else
                        env.expandToInclude(i, j);

                }

        for(Envelope env : envMap.values()) {
            env.expandBy(0.5);
            env.translate(0.5, 0.5);
        }

        List<DefaultFeature> features = new ArrayList<DefaultFeature>();
        for(Integer id : envMap.keySet()) {
            Geometry geom = vectorize(voronoi, envMap.get(id), id);
            geom.apply(grid2space);
            features.add(new DefaultFeature(id, geom, null, null));
        }

        return features;
    }

    private Geometry vectorize(Raster patchs, Envelope env, double val) {
        GeometryFactory factory = new GeometryFactory();
        List<LineString> lines = new ArrayList<LineString>();

        for(int y = (int) env.getMinY(); y < env.getMaxY(); y++)
            for(int x = (int) env.getMinX(); x < env.getMaxX(); x++)
                if(patchs.getSampleDouble(x, y, 0) == val) {
                    // LEFT
                    if(patchs.getSampleDouble(x-1, y, 0) != val)
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x, y), new Coordinate(x, y+1)}));
                    // RIGHT
                    if(patchs.getSampleDouble(x+1, y, 0) != val)
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x+1, y), new Coordinate(x+1, y+1)}));
                    // TOP
                    if(patchs.getSampleDouble(x, y-1, 0) != val)
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x, y), new Coordinate(x+1, y)}));
                    // BOTTOM
                    if(patchs.getSampleDouble(x, y+1, 0) != val)
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x, y+1), new Coordinate(x+1, y+1)}));
                }

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();

        List<Polygon> finalPolys = new ArrayList<Polygon>();
        for(Object p : polys) {
            Polygon g = ((Polygon)p);
            double y = g.getEnvelopeInternal().getMinY();
            double minX = g.getEnvelopeInternal().getMaxX();
            for(Coordinate c : g.getCoordinates())
                if(c.y == y && c.x < minX)
                    minX = c.x;
            if(patchs.getSampleDouble((int)minX, (int)y, 0) == val)
                finalPolys.add(g);

        }

// remove points not needed on straight line
        List<Polygon> simpPolys = new ArrayList<Polygon>();
        for(Polygon p : finalPolys) {
            LinearRing [] interior = new LinearRing[p.getNumInteriorRing()];
            for(int i = 0; i < interior.length; i++)
                interior[i] = p.getFactory().createLinearRing(simpRing(p.getInteriorRingN(i).getCoordinates()));

            simpPolys.add(p.getFactory().createPolygon(p.getFactory().createLinearRing(
                    simpRing(p.getExteriorRing().getCoordinates())), interior));
        }

        return factory.buildGeometry(simpPolys);

    }

    /**
     * Remove unneeded points from straight line
     * @param coords
     * @return
     */
    private Coordinate [] simpRing(Coordinate[] coords) {
        ArrayList<Coordinate> newCoords = new ArrayList<Coordinate>();
        Coordinate prec = coords[coords.length-1], cur = coords[0], next;
        for(int i = 1; i < coords.length; i++) {
            next = coords[i];
            if(!(next.x-cur.x == cur.x-prec.x && next.y-cur.y == cur.y-prec.y))
                newCoords.add(cur);
            prec = cur;
            cur = next;
        }
        newCoords.add(new Coordinate(newCoords.get(0)));

        return newCoords.toArray(new Coordinate[newCoords.size()]);
    }

    /**
     *
     * @param r
     * @param code
     * @param con8
     * @return  un raster avec un bord d'un pixel de large ayant la valeur -1
     */
    private WritableRaster extractPatch(RenderedImage img, int code, double noData, boolean con8, Map<Integer, Envelope> envMap) {
        TaskMonitor monitor = new TaskMonitor(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Extract_patch"), "", 0, img.getHeight());
        WritableRaster clust = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_INT, img.getWidth()+2, img.getHeight()+2, 1), null);
        int k = 0;
        TreeSet<Integer> set = new TreeSet<Integer>();
        ArrayList<Integer> idClust = new ArrayList<Integer>();
        RandomIter r = RandomIterFactory.create(img, null);
        for(int j = 1; j <= img.getHeight(); j++) {
            monitor.setProgress(j-1);
            for(int i = 1; i <= img.getWidth(); i++) {
                int val = r.getSample(i-1, j-1, 0);
                if(val == code) {
                    set.add(clust.getSample(i-1, j, 0));
                    set.add(clust.getSample(i, j-1, 0));
                    if(con8) {
                        set.add(clust.getSample(i-1, j-1, 0));
                        set.add(clust.getSample(i+1, j-1, 0));
                    }
                    set.remove(0);
                    set.remove(-1);
                    if(set.isEmpty()) {
                        k++;
                        clust.setSample(i, j, 0, k);
                        idClust.add(k);
                    } else if(set.size() == 1) {
                        int id = set.iterator().next();
                        clust.setSample(i, j, 0, idClust.get(id-1));
                    } else {
                        int minId = Integer.MAX_VALUE;
                        for(Integer id : set) {
                            int min = getMinId(idClust, id);
                            if(min < minId)
                                minId = min;
                        }

                        for(Integer id : set)
                            idClust.set(getMinId(idClust, id)-1, minId);

//                        for(Integer id : set) {
//                            while(idClust.get(id-1) != minId) {
//                                int newId = idClust.get(id-1);
//                                idClust.set(id-1, minId);
//                                id = newId;
//                            }
//                        }

                        clust.setSample(i, j, 0, minId);

                    }
                    set.clear();
                } else if(val == noData)
                    clust.setSample(i, j, 0, -1);
            }
        }

        for(int j = 0; j < clust.getHeight(); j++) {
            clust.setSample(0, j, 0, -1);
            clust.setSample(clust.getWidth()-1, j, 0, -1);
        }
        for(int j = 0; j < clust.getWidth(); j++) {
            clust.setSample(j, 0, 0, -1);
            clust.setSample(j, clust.getHeight()-1, 0, -1);
        }

        for(int i = 0; i < idClust.size(); i++) {
            int m = i+1;
            while(idClust.get(m-1) != m)
                m = idClust.get(m-1);
            idClust.set(i, m);
        }


        for(int j = 1; j < clust.getHeight()-1; j++)
            for(int i = 1; i < clust.getWidth()-1; i++)
                if(clust.getSample(i, j, 0) > 0) {
                    int id = idClust.get(clust.getSample(i, j, 0)-1);
                    Envelope env = envMap.get(id);
                    if(env == null)
                        envMap.put(id, new Envelope(new Coordinate(i, j)));
                    else
                        env.expandToInclude(i, j);

                    clust.setSample(i, j, 0, id);
                }


        for(Envelope env : envMap.values()) {
            env.expandBy(0.5);
            env.translate(0.5, 0.5);
        }

        monitor.close();

        return clust;
    }

    private int getMinId(List<Integer> ids, int id) {
        while(ids.get(id-1) != id)
            id = ids.get(id-1);
        return id;
    }

    public List<Path> getPaths(String name) {
        return costLinks.get(name).getPaths();
    }

    public Collection<DefaultFeature> getPatches() {
        return patches;
    }

    public TreeSet<Integer> getCodes() {
        return codes;
    }

    public SpacePathFinder getPathFinder(Linkset cost) throws Exception {
        return cost.getType_dist() == Linkset.EUCLID ?
            new EuclidePathFinder(this) : getRasterPathFinder(cost);
    }
    
    public RasterPathFinder getRasterPathFinder(Linkset cost) throws Exception {
        switch(cost.getType_dist()) {
            case Linkset.COST:
                return new RasterPathFinder(this, getImageSource(), cost.getCosts());
            case Linkset.EXT_COST:
                if(cost.getExtCostFile().exists()) {
                    Raster extRaster = loadExtCostRaster(cost.getExtCostFile());
                    return new RasterPathFinder(this, extRaster);
                } else
                    throw new RuntimeException("Cost raster file " + cost.getExtCostFile() + " not found");
            default:
                throw new IllegalArgumentException();
        }
    }

    public void addGraph(GraphGenerator graphGen, boolean save) throws Exception {
        ProgressBar progressBar = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Create_graph..."));
        progressBar.setIndeterminate(true);
        graphs.put(graphGen.getName(), graphGen);
        
        if(save) {
            DefaultFeature.saveFeatures(graphGen.getComponentFeatures(),
                    new File(dir, graphGen.getName() + "-voronoi.shp"));

            graphGen.setSaved(true);
            save();
        }

        if(rootLayer != null) {
            rootLayer.setLayersVisible(false);
            graphLayers.setExpanded(true);
            GraphGroupLayer gl = graphGen.getLayers();
            gl.setExpanded(true);
            graphLayers.addLayerFirst(gl);
        }
        
        progressBar.close();
    }

    public List<String> getGraphPatchAttr(String graphName) {
        List<String> attrs = new ArrayList<String>();
        for(String attr : patches.get(0).getAttributeNames())
            if(attr.endsWith("_" + graphName))
                attrs.add(attr);
        return attrs;
    }

    /**
     * 
     * @param graphName
     * @return list of detailed name metrics calculated for graph graphName
     */
    public List<String> getGraphPatchVar(String graphName) {
        List<String> attrs = new ArrayList<String>();
        for(String attr : patches.get(0).getAttributeNames())
            if(attr.endsWith("_" + graphName))
                attrs.add(attr.substring(0, attr.length() - graphName.length() - 1));
        return attrs;
    }

    List<String> getGraphLinkAttr(GraphGenerator g) {
        List<String> attrs = new ArrayList<String>();
        for(String attr : g.getCostDistance().getPaths().get(0).getAttributeNames())
            if(attr.endsWith("_" + g.getName()))
                attrs.add(attr);
        return attrs;
    }

    public void removeGraph(final String name) {
        GraphGenerator g = graphs.remove(name);

        for(String attr : getGraphPatchAttr(name))
            DefaultFeature.removeAttribute(attr, patches);

        for(String attr : getGraphLinkAttr(g))
            DefaultFeature.removeAttribute(attr, g.getCostDistance().getPaths());

        try {
            saveLinks(g.getCostDistance().getName());
            savePatch();
            save();
        } catch (Exception ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().startsWith(name + "-voronoi.") && f.getName().length()-3 == (name + "-voronoi.").length();
            }
        };
        for(File f : dir.listFiles(filter))
            f.delete();

        for(Layer l : new ArrayList<Layer>(graphLayers.getLayers()))
            if(l.getName().equals(name))
                graphLayers.removeLayer(l);
    }

    public Collection<GraphGenerator> getGraphs() {
        return graphs.values();
    }
    
    public GraphGenerator getGraph(String name) {
        return graphs.get(name);
    }
    
    public Set<String> getGraphNames() {
        return graphs.keySet();
    }

    public Set<String> getPointsetNames() {
        return exoDatas.keySet();
    }
    
    public Pointset getPointset(String name) {
        return exoDatas.get(name);
    }

    public Collection<Pointset> getPointsets() {
        return exoDatas.values();
    }

    public void removeExoDataset(final String name) throws IOException, SchemaException {
        exoDatas.remove(name);
//        exoDataName.remove(name);

        List<String> attrs = new ArrayList<String>();
        for(String attr : patches.get(0).getAttributeNames())
            if(attr.startsWith(name + "."))
                attrs.add(attr);
        
        for(String attr : attrs)
            DefaultFeature.removeAttribute(attr, patches);


        savePatch();
        save();

        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().startsWith("Exo-" + name + ".") && f.getName().length()-3 == ("Exo-" + name + ".").length();
            }
        };
        for(File f : dir.listFiles(filter))
            f.delete();
    }

    private synchronized List<Feature> getVoronoi() {
        if(voronoi == null)
            try {
                List<DefaultFeature> features = GlobalDataStore.getFeatures(new File(dir.getAbsolutePath() + File.separator + "voronoi.shp"), "Id", null);
                voronoi = new ArrayList<Feature>(features);
                for(Feature f : features)
                    voronoi.set((Integer)f.getId()-1, f);
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }

        return voronoi;
    }

    public List<DefaultFeature> loadVoronoiGraph(String name) throws IOException {
        List<DefaultFeature> features = GlobalDataStore.getFeatures(new File(dir.getAbsolutePath() + File.separator + name + "-voronoi.shp"), "Id", null);

        HashMap<Object, DefaultFeature> map = new HashMap<Object, DefaultFeature>();
        for(DefaultFeature f : features)
            map.put(f.getId(), f);

        File fCSV = new File(dir.getAbsolutePath() + File.separator + name + "-voronoi.csv");
        if(fCSV.exists()) {
            CSVReader r = new CSVReader(new FileReader(fCSV));
            String [] attr = r.readNext();
            for(int i = 0; i < attr.length; i++)
                if(features.get(0).getAttributeNames().contains(attr[i]))
                    attr[i] = null;
                else
                    DefaultFeature.addAttribute(attr[i], features, Double.NaN);
            String [] tab = r.readNext();
            while(tab != null) {
                DefaultFeature f = map.get(Integer.parseInt(tab[0]));
                for(int i = 1; i < tab.length; i++)
                    if(attr[i] != null)
                        if(tab[i].isEmpty())
                            f.setAttribute(attr[i], null);
                        else
                            f.setAttribute(attr[i], Double.parseDouble(tab[i]));
                tab = r.readNext();
            }
            r.close();
        }

        return features;
    }

    public synchronized WritableRaster getRasterPatch() throws IOException {
        WritableRaster raster = patchRaster != null ? patchRaster.get() : null;
        if(raster == null) {
            RenderedImage img = IOImage.loadTiffWithoutCRS(new File(dir, "patches.tif")).getRenderedImage();
            if(img.getNumXTiles() == 1 && img.getNumYTiles() == 1)
                raster = (WritableRaster) img.getTile(0, 0);
            else
                raster = (WritableRaster) img.getData();
            patchRaster = new SoftRef<WritableRaster>(raster);
        }
        
        return raster;
    }

    public synchronized WritableRaster getImageSource() throws IOException {
        WritableRaster raster = srcRaster != null ? srcRaster.get() : null;
        if(raster == null) {
            Raster r = IOImage.loadTiffWithoutCRS(new File(dir, "source.tif")).getRenderedImage().getData();
            // on copie le raster dans un writableraster
            raster = r.createCompatibleWritableRaster();
            raster.setRect(r);
            raster = raster.createWritableTranslatedChild(1, 1);
            srcRaster = new SoftRef<WritableRaster>(raster);
        }
        return raster;
    }

    public void saveGraphVoronoi(String graph) throws IOException, SchemaException {
        List<DefaultFeature> comps = graphs.get(graph).getComponentFeatures();
        CSVWriter w = new CSVWriter(new FileWriter(dir.getAbsolutePath() + File.separator + graph + "-voronoi.csv"));
        List<String> header = new ArrayList<String>();
        header.add("Id");
        header.addAll(comps.get(0).getAttributeNames());
        w.writeNext(header.toArray(new String[0]));
        
        String [] tab = new String[comps.get(0).getAttributeNames().size()+1];
        for(Feature f : comps) {
            tab[0] = f.getId().toString();
            for(int i = 1; i < tab.length; i++)
                tab[i] = f.getAttribute(i-1).toString();
            w.writeNext(tab);
        }
        w.close();
    }

    public void savePatch() throws IOException, SchemaException {
        //DefaultFeature.saveFeatures(patches, new File(dir.getAbsolutePath() + File.separator + "patches.shp"));
        CSVWriter w = new CSVWriter(new FileWriter(dir.getAbsolutePath() + File.separator + "patches.csv"));
        w.writeNext(patches.get(0).getAttributeNames().toArray(new String[0]));
        String [] tab = new String[patches.get(0).getAttributeNames().size()];
        for(Feature f : getPatches()) {
            for(int i = 0; i < tab.length; i++)
                tab[i] = f.getAttribute(i).toString();
            w.writeNext(tab);
        }
        w.close();

    }

    public void saveLinks(String name) throws IOException, SchemaException {
        CSVWriter w = new CSVWriter(new FileWriter(dir.getAbsolutePath() + File.separator + name + "-links.csv"));
        w.writeNext(getPaths(name).get(0).getAttributeNames().toArray(new String[0]));
        
        for(Path p : getPaths(name))
            w.writeNext(Path.serialPath(p));
        
        w.close();
    }

    public void save() throws IOException {
        XStream xstream = new XStream();
        xstream.alias("Project", Project.class);
        xstream.alias("Pointset", Pointset.class);
        xstream.alias("Linkset", Linkset.class);
        xstream.alias("Graph", GraphGenerator.class);
        FileWriter fw = new FileWriter(getProjectFile());
        xstream.toXML(this, fw);
        fw.close();
    }

    public File getProjectFile() {
        return new File(dir, name + ".xml");
    }

    public File getProjectDir() {
        return dir;
    }

    private void createLayers() {
        rootLayer = new DefaultGroupLayer(name, true);
        rootLayer.addLayerFirst(new FeatureLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Patch"), patches,
                new FeatureStyle(new Color(0x65a252), new Color(0x426f3c))));

        linkLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Link_sets"));
        rootLayer.addLayerFirst(linkLayers);
       
        try {
            RasterLayer l = new RasterLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Landscape_map"), new RasterShape(
                getImageSource().getParent(), zone, new RasterStyle(), true));
            l.getStyle().setNoDataValue(noData);
            l.setVisible(false);
            l.setDrawLegend(false);
            rootLayer.addLayerLast(l);
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }

        for(String linkName : costLinks.keySet()) {
            Layer l = new LinkLayer(linkName);
            l.setVisible(false);
            linkLayers.addLayerLast(l);
        }

        Layer l = new FeatureLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Voronoi_links"), 
                planarLinks.getFeatures(), new LineStyle(new Color(0xbcc3ac)));
        l.setVisible(false);
        linkLayers.addLayerLast(l);

        graphLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Graphs"));
        rootLayer.addLayerFirst(graphLayers);      
        for(GraphGenerator g : graphs.values())
            try {
                l = g.getLayers();
                l.setVisible(false);
                graphLayers.addLayerLast(l);
            } catch(Exception e) {
                Logger.getLogger(Project.class.getName()).log(Level.WARNING, null, e);
            }
        
        exoLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Exo_data"));
        rootLayer.addLayerFirst(exoLayers);
        for(String dataset : exoDatas.keySet())  {
            l = new ExoLayer(dataset);
            l.setVisible(false);
            exoLayers.addLayerLast(l);
        }
//        // For debugging purpose        
//        List<Geometry> geoms = new ArrayList<Geometry>();
//        for(Feature patch : getPatches())
//            geoms.add(new GeometryFactory().createPoint(getCentroid(patch)));
//        rootLayer.addLayerFirst(new GeometryLayer("Centroid", new GeometryFactory().buildGeometry(geoms), new PointStyle()));
        
    }

    public static synchronized Project loadProject(File file, boolean all) throws Exception {
        
        XStream xstream = new XStream();
        xstream.alias("Project", Project.class);
        xstream.alias("Pointset", Pointset.class);
        xstream.alias("Linkset", Linkset.class);
        xstream.alias("Graph", GraphGenerator.class);
        
        // pour compatibilité avec projet 1.1-beta2 et antérieur
        xstream.alias("org.thema.graphab.ExoData", Pointset.class);
        xstream.alias("org.thema.graphab.CostDistance", Linkset.class);
        xstream.alias("org.thema.graphab.GraphGenerator", GraphGenerator.class);
        
        FileReader fr = new FileReader(file) ;

        Project prj = (Project) xstream.fromXML(fr);
        fr.close();

        prj.dir = file.getAbsoluteFile().getParentFile();
        
        ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Loading_project..."), 
                100*(2+prj.costLinks.size()) + 10*prj.exoDatas.size());
        List<DefaultFeature> features = GlobalDataStore.getFeatures(new File(prj.dir, "patches.shp"), 
                "Id", monitor.getSubProgress(100));
        prj.patches = new ArrayList<DefaultFeature>(features);
        for(DefaultFeature f : features)
            prj.patches.set((Integer)f.getId()-1, f);

        File fCSV = new File(prj.dir, "patches.csv");
        if(fCSV.exists()) {

            CSVReader r = new CSVReader(new FileReader(fCSV));
            List<String> attrNames = new ArrayList<String>(Arrays.asList(r.readNext()));

            String [] tab = r.readNext();
            while(tab != null) {
                int id = Integer.parseInt(tab[0]);
                DefaultFeature f = prj.getPatch(id);
                List values = new ArrayList();
                values.add(id);
                for(int i = 1; i < tab.length; i++)
                    values.add(Double.parseDouble(tab[i]));

                prj.patches.set(id-1, new DefaultFeature(f.getId(),
                        f.getGeometry(), attrNames, values));
                tab = r.readNext();
            }
            r.close();
        }

        features = GlobalDataStore.getFeatures(new File(prj.dir, "links.shp"), "Id", monitor.getSubProgress(100));
        List<Path> paths = new ArrayList<Path>(features.size());
        for(Feature f : features)
            paths.add(Path.loadPath(f, prj));

        prj.planarLinks = new Links("Links", paths, prj.patches.size());

        if(all)
            for(Linkset cost : prj.costLinks.values())
                cost.loadPaths(prj, monitor.getSubProgress(100));

        for(String name : prj.exoDatas.keySet()) {
            prj.exoDatas.get(name).setFeatures(GlobalDataStore.getFeatures(
                    new File(prj.dir,"Exo-" + name + ".shp"), "Id", monitor.getSubProgress(10)));
        }
        
        prj.removedCodes = new HashMap<Integer, Integer>();

//        prj.createLayers();
        monitor.close();
        
        return prj;
    }


    public GridCoverage2D loadCoverage(File file) throws Exception {
        GridCoverage2D cov;
        if(file.getName().toLowerCase().endsWith(".tif"))
            cov = IOImage.loadTiffWithoutCRS(file);
        else
            cov = new RSTGridReader(file).read(null);

        GridEnvelope2D grid = cov.getGridGeometry().getGridRange2D();
        Envelope2D env = cov.getEnvelope2D();
        double res = env.getWidth() / grid.getWidth();
        if(res != resolution)
            throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Resolution_does_not_match."));
        if(env.getWidth() != zone.getWidth() || env.getHeight() != zone.getHeight())
            throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Raster_extent_does_not_match."));
        if(Math.abs(env.getX() - zone.getX()) > res || Math.abs(env.getY() - zone.getY()) > res)
            throw new IllegalArgumentException(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Raster_position_does_not_match."));

        return cov;
    }
    
    public synchronized Raster loadExtCostRaster(File file) throws Exception {
        Raster raster = null;
        
        if(extCostRasters == null)
            extCostRasters = new HashMap<File, SoftRef<Raster>>();
        if(extCostRasters.containsKey(file))
            raster = extCostRasters.get(file).get();
        
        if(raster == null) {
            raster = loadCoverage(file).getRenderedImage().getData();
            raster = raster.createTranslatedChild(1, 1);
            extCostRasters.put(file, new SoftRef<Raster>(raster));
        }
        
        return raster;
    }
    
    public File getDirectory() {
        return dir;
    }
    
    public static double getArea() {
        return getProject().zone.getWidth()*getProject().zone.getHeight();
    }

//    public synchronized static double getTotalPatchArea() {
//        if(MainFrame.project.totalPatchArea == 0) {
//            double sum = 0;
//            for(Feature f : MainFrame.project.getPatches())
//                sum += ((Number)f.getAttribute(Project.AREA_ATTR)).doubleValue();
//            MainFrame.project.totalPatchArea = sum;
//        }
//
//        return MainFrame.project.totalPatchArea;
//    }

    public static double getPatchArea(org.geotools.graph.structure.Node node) {
        return getPatchArea((Feature)node.getObject());
    }

    public static double getPatchArea(Feature patch) {
        return ((Number)patch.getAttribute(Project.AREA_ATTR)).doubleValue();
    }

    public static double getTotalPatchCapacity() {
        if(MainFrame.project.totalPatchCapacity == 0) {
            double sum = 0;
            for(Feature f : MainFrame.project.getPatches())
                sum += ((Number)f.getAttribute(Project.CAPA_ATTR)).doubleValue();
            MainFrame.project.totalPatchCapacity = sum;
        }

        return MainFrame.project.totalPatchCapacity;
    }

    public static DefaultFeature getPatch(org.geotools.graph.structure.Node node) {
        return (DefaultFeature)node.getObject();
    }
    
    public static double getPatchCapacity(org.geotools.graph.structure.Node node) {
        return getPatchCapacity((Feature)node.getObject());
    }
    
    public static double getPatchCapacity(Feature patch) {
        return ((Number)patch.getAttribute(Project.CAPA_ATTR)).doubleValue();
    }

    public CapaPatchParam getCapacityParams() {
        return capacityParams;
    }

    public void setCapacityParams(CapaPatchParam capacityParams) {
        this.capacityParams = capacityParams;
    }
    
    public void setCapacity(DefaultFeature patch, double capa) {
        patch.setAttribute(Project.CAPA_ATTR, capa);
        totalPatchCapacity = 0;
    }
    
    public double getResolution() {
        return resolution;
    }

    public AffineTransformation getGrid2space() {
        return grid2space;
    }

    public AffineTransformation getSpace2grid() {
        return space2grid;
    }

    public int getPatchCode() {
        return patchCode;
    }
    
    /**
     * Check if point (x,y) in world coordinate lies in study area
     * @param x
     * @param y
     * @return
     */
    public boolean isInZone(double x, double y) throws IOException {
        if(!zone.contains(x, y))
            return false;
        Coordinate cg = space2grid.transform(new Coordinate(x, y), new Coordinate());
        return getImageSource().getSample((int)cg.x, (int)cg.y, 0) != noData;
    }

    public DefaultFeature createPointPatch(Point point, double capa) {
        List<String> attrNames = new ArrayList<String>(patches.get(0).getAttributeNames());
        List attrs = new ArrayList(Arrays.asList(new Double[attrNames.size()]));
        attrs.set(attrNames.indexOf(CAPA_ATTR), capa);
        attrs.set(attrNames.indexOf(AREA_ATTR), resolution*resolution);
        attrs.set(attrNames.indexOf(PERIM_ATTR), resolution*4);
        return new DefaultFeature(patches.size()+1, point, attrNames, attrs);
    }
    
    public synchronized DefaultFeature addPatch(Point point, double capa) throws IOException {
        // tester si pas dans un patch ou touche un patch
        if(!canCreatePatch(point))
            throw new IllegalArgumentException("Patch already exist at the same position : " + point.toString());
        DefaultFeature patch = createPointPatch(point, capa);
        int id = (Integer)patch.getId();
        Coordinate cg = space2grid.transform(point.getCoordinate(), new Coordinate());
        // on passe les raster en strong reference pour qu'ils ne puissent pas être supprimé
        patchRaster = new StrongRef<WritableRaster>(getRasterPatch());
        srcRaster = new StrongRef<WritableRaster>(getImageSource());
        removedCodes.put(id, getImageSource().getSample((int)cg.x, (int)cg.y, 0));
        getRasterPatch().setSample((int)cg.x, (int)cg.y, 0, id);
        getImageSource().setSample((int)cg.x, (int)cg.y, 0, patchCode);
        
        patches.add(patch);
        return patch;
    }

    public boolean canCreatePatch(Point p) throws IOException {
        if(!isInZone(p.getX(), p.getY()))
            return false;
        Coordinate cg = space2grid.transform(p.getCoordinate(), new Coordinate());
        if(getRasterPatch().getSample((int)cg.x, (int)cg.y, 0) > 0)
            return false;
        if(con8 && getRasterPatch().getSample((int)cg.x-1, (int)cg.y-1, 0) > 0)
            return false;
        if(getRasterPatch().getSample((int)cg.x-1, (int)cg.y, 0) > 0)
            return false;
        if(con8 && getRasterPatch().getSample((int)cg.x-1, (int)cg.y+1, 0) > 0)
            return false;
        if(getRasterPatch().getSample((int)cg.x, (int)cg.y-1, 0) > 0)
            return false;
        if(getRasterPatch().getSample((int)cg.x, (int)cg.y+1, 0) > 0)
            return false;
        if(con8 && getRasterPatch().getSample((int)cg.x+1, (int)cg.y-1, 0) > 0)
            return false;
        if(getRasterPatch().getSample((int)cg.x+1, (int)cg.y, 0) > 0)
            return false;
        if(con8 && getRasterPatch().getSample((int)cg.x+1, (int)cg.y+1, 0) > 0)
            return false;
        return true;
    }
    
    public synchronized void removePatch(Feature patch) throws IOException {
        Coordinate cg = space2grid.transform(patch.getGeometry().getCoordinate(), new Coordinate());
        if(getImageSource().getSample((int)cg.x, (int)cg.y, 0) != patchCode)
            throw new RuntimeException("No patch to remove at " + patch.getGeometry());
        int id = (Integer)patch.getId();
        if(id != patches.size())
            throw new RuntimeException("The patch to remove is not the last one - id : " + patch.getId());
        getImageSource().setSample((int)cg.x, (int)cg.y, 0, removedCodes.remove(id));
        getRasterPatch().setSample((int)cg.x, (int)cg.y, 0, 0);
        patches.remove(patches.size()-1);
    }
    
    
    class ExoLayer extends FeatureLayer {

        public ExoLayer(String name) {
            super(name, exoDatas.get(name).getFeatures());
        }

        @Override
        public JPopupMenu getContextMenu() {
            JPopupMenu menu = super.getContextMenu();
            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Export_all")) {
                public void actionPerformed(ActionEvent e) {
                    GraphGenerator gen = (GraphGenerator) JOptionPane.showInputDialog(null,
                            java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Select_graph"), java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Export..."), JOptionPane.PLAIN_MESSAGE,
                            null, getGraphs().toArray(), null);
                    if(gen == null)
                        return;
                    File f = Util.getFileSave(".csv");
                    if(f == null)
                        return;
                    try { 
                        CSVWriter w = new CSVWriter(new FileWriter(f));
                        List<DefaultFeature> exoData = getPointset(name).getFeatures();
                        List<String> attrNames = new ArrayList<String>(exoData.get(0).getAttributeNames());
                        attrNames.addAll(getPatches().iterator().next().getAttributeNames());
                        attrNames.addAll(gen.getComponentFeatures().get(0).getAttributeNames());
                        w.writeNext(attrNames.toArray(new String[attrNames.size()]));
                        String [] attrs = new String[attrNames.size()];
                        for(Feature exo : exoData) {
                            int n = exo.getAttributeNames().size();
                            for(int i = 0; i < n; i++)
                                attrs[i] = exo.getAttribute(i).toString();
                            Feature patch = getPatch((Integer)exo.getAttribute(EXO_IDPATCH));
                            for(int i = 0; i < patch.getAttributeNames().size(); i++)
                                attrs[i+n] = patch.getAttribute(i).toString();
                            n += patch.getAttributeNames().size();
                            Feature comp = gen.getComponentFeature(patch);
                            for(int i = 0; i < comp.getAttributeNames().size(); i++)
                                attrs[i+n] = comp.getAttribute(i).toString();
                            w.writeNext(attrs);
                        }

                        w.close();
                    } catch (Exception ex) {
                        Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });
//            menu.add(new AbstractAction("IFPC") {
//                public void actionPerformed(ActionEvent e) {
//                    Pointset exo = exoDatas.get(name);
//                    String res = JOptionPane.showInputDialog("Distance/Cost max :", 100);
//                    if(res == null || res.isEmpty())
//                        return;
//                    double max = Double.parseDouble(res);
//                    try {
//                        DefaultFeature.addAttribute("IFPC", Project.getProject().getPointset(name), Double.NaN);
//                        RasterPathFinder pathfinder = getRasterPathFinder(exo.getCost());
//                        for(DefaultFeature fexo : Project.getProject().getPointset(name)) {
//                            double ifpc = 0;
//                            HashMap<DefaultFeature, Path> dists = pathfinder.calcPaths(fexo.getGeometry().getCentroid().getCoordinate(), max, false);
//                            for(DefaultFeature patch : dists.keySet()) {
//                                ifpc += Project.getPatchCapacity(patch) / (dists.get(patch).getCost()+1);
//                            }
//                            fexo.setAttribute("IFPC", ifpc);
//                        }
//                    } catch (IOException ex) {
//                        Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            });

            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("DISTANCE MATRIX")) {
                public void actionPerformed(ActionEvent e) {
                    new PointsetDistanceDialog(null, exoDatas.get(name)).setVisible(true);
                }
            });

            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove...")) {
                public void actionPerformed(ActionEvent e) {
                    int res = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Do_you_want_to_remove_the_dataset_") + name + " ?");
                    if(res != JOptionPane.YES_OPTION)
                        return;
                    try {
                        removeExoDataset(name);
                        exoLayers.removeLayer(ExoLayer.this);
                    } catch (Exception ex) {
                        Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });

            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties...")) {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, exoDatas.get(name).getInfo());
                }
            });
            return menu;
        }

    }


    class LinkLayer extends FeatureLayer {
        
        public LinkLayer(final String name) {
            super(name, new FeatureGetter<Path>() {
                public Collection<Path> getFeatures() {
                    return costLinks.get(name).getPaths();
                }
            }, zone, new LineStyle(new Color(costLinks.get(name).getTopology() == Linkset.PLANAR ? 0x25372b : 0xb8c45d)));
        }

        @Override
        public JPopupMenu getContextMenu() {
            JPopupMenu menu = super.getContextMenu();
            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove...")) {
                public void actionPerformed(ActionEvent e) {
                    List<String> exoNames = new ArrayList<String>();
                    for(Pointset exo : exoDatas.values())
                        if(exo.getCost().getName().equals(name))
                            exoNames.add(exo.getName());
                    if(!exoNames.isEmpty()) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Links_is_used_by_exogenous_data") +
                                Arrays.deepToString(exoNames.toArray()));
                        return;
                    }
                    List<String> graphNames = new ArrayList<String>();
                    for(GraphGenerator g : getGraphs())
                        if(g.getCostDistance().getName().equals(name))
                            graphNames.add(g.getName());
                    int res = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Do_you_want_to_remove_the_links_") + name + " ?" +
                            (!graphNames.isEmpty() ? "\nGraph " + Arrays.deepToString(graphNames.toArray()) + java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("_will_be_removed.") : ""), java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove"), JOptionPane.YES_NO_OPTION);
                    if(res != JOptionPane.YES_OPTION)
                        return;
                    try {
                        for(String gName : graphNames) {
                            removeGraph(gName);
                            graphLayers.removeLayer(graphLayers.getLayer(gName));
                        }
                        costLinks.remove(name);
                        save();
                        linkLayers.removeLayer(LinkLayer.this);
                    } catch (Exception ex) {
                        Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });

            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties...")) {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, costLinks.get(name).getInfo());
                }
            });
            return menu;
        }

    }

    public static Project getProject() {
        return MainFrame.project;
    }
    
    public static String getDetailName(GlobalMetric indice, int indResult) {
        if(indice.getResultNames().length == 1)
            return indice.getDetailName();
        return indice.getDetailName() + "|" + indice.getResultNames()[indResult];
    }

    /**
     * Renvoie les métriques globales pour une méthode donnée
     * @param method
     * @return 
     */
    public static List<GlobalMetric> getGlobalMetricsFor(Method method) {
        List<GlobalMetric> indices = new ArrayList<GlobalMetric>();
        for(GlobalMetric ind : GLOBAL_METRICS)
            if(ind.isAcceptMethod(method))
                indices.add(ind);
        return indices;
    }
    
    public static List<LocalMetric> getLocalMetrics() {
        List<LocalMetric> indices = new ArrayList<LocalMetric>();
        for(LocalMetric ind : LOCAL_METRICS)
            indices.add(ind);
        return indices;
    }
    
    public static GlobalMetric getGlobalMetric(String shortName) {
        for(GlobalMetric ind : GLOBAL_METRICS)
            if(ind.getShortName().equals(shortName))
                return ind;
        throw new IllegalArgumentException("Unknown metric " + shortName);
    }

    public static LocalMetric getLocalMetric(String shortName) {
        for(LocalMetric ind : LOCAL_METRICS)
            if(ind.getShortName().equals(shortName))
                return ind;
        throw new IllegalArgumentException("Unknown metric " + shortName);
    }
    
    public static List<GlobalMetric> GLOBAL_METRICS;
    public static List<LocalMetric> LOCAL_METRICS;
    static {
        GLOBAL_METRICS = new ArrayList(Arrays.asList(new SumLocal2GlobalMetric(new FLocalMetric()), new PCMetric(), new IICMetric(), new CCPMetric(),
                new MSCMetric(), new SLCMetric(), new ECSMetric(), new GDMetric(), new HMetric(), new NCMetric(),
                new DeltaPCMetric()/*, new EntropyLocal2GlobalIndice(new BCLocalMetric())*/));
        LOCAL_METRICS = new ArrayList(Arrays.asList((LocalMetric)new FLocalMetric(), new BCLocalMetric(), new FPCLocalMetric(),
                new DgLocalMetric(), new CCLocalMetric(), new ClosenessLocalMetric(), new ConCorrLocalMetric(),
                new EccentricityLocalMetric()/*, new IFPCIndice(), new BCCircuitLocalIndice(),
                new CBCLocalIndice(), new CFLocalIndice(), new PCFLocalIndice()*/));
    }
    
    
    public static void loadPluginMetric() throws Exception {
        URL url = Project.class.getProtectionDomain().getCodeSource().getLocation();
        File dir = new File(url.toURI()).getParentFile();
        File loc = new File(dir, "plugins");

        if(!loc.exists())
            return;
        
        File[] flist = loc.listFiles(new FileFilter() {
            public boolean accept(File file) {return file.getPath().toLowerCase().endsWith(".jar");}
        });
        if(flist == null || flist.length == 0)
            return;
        URL[] urls = new URL[flist.length];
        for (int i = 0; i < flist.length; i++)
            urls[i] = flist[i].toURI().toURL();
        URLClassLoader ucl = new URLClassLoader(urls);

        loadPluginMetric(ucl);
    }
    
    public static void loadPluginMetric(ClassLoader loader) throws Exception {
       
        ServiceLoader<Metric> sl = ServiceLoader.load(Metric.class, loader);
        Iterator<Metric> it = sl.iterator();
        while (it.hasNext()) {
            Metric ind = it.next();
            if(ind instanceof GlobalMetric)
                GLOBAL_METRICS.add((GlobalMetric)ind);
            else if(ind instanceof LocalMetric)
                LOCAL_METRICS.add((LocalMetric)ind);
            else
                throw new RuntimeException("Class " +ind.getClass().getCanonicalName() + " does not inherit from GraphIndice or LocalIndice");
        }
    }

    public static interface Ref<T> {
        public T get();
    }
    
    public static class SoftRef<T> implements Ref<T>{

        SoftReference<T> ref;

        public SoftRef(T val) {
            ref = new SoftReference<T>(val);
        }
        
        @Override
        public T get() {
            return ref.get();   
        }
        
    }
    
    public static class StrongRef<T> implements Ref<T>{

        T ref;

        public StrongRef(T val) {
            ref = val;
        }
        
        @Override
        public T get() {
            return ref;   
        }
        
    }
}
