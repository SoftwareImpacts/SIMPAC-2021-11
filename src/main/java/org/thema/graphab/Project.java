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
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
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
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageBuilder;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.feature.SchemaException;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.common.Config;
import org.thema.common.JTS;
import org.thema.common.ProgressBar;
import org.thema.common.Util;
import org.thema.common.io.IOFile;
import org.thema.data.IOImage;
import org.thema.common.parallel.*;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.GlobalDataStore;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureGetter;
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
    
    private static final List<String> PATCH_ATTRS = Arrays.asList("Id", AREA_ATTR, PERIM_ATTR, CAPA_ATTR);
    
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
    
    private String wktCRS;
    
    private File demFile;

    private transient List<DefaultFeature> patches;
    private transient List<Feature> voronoi;

    private transient Links planarLinks;

    private transient DefaultGroupLayer rootLayer, linkLayers, exoLayers, graphLayers, analysisLayers;
    private transient STRtree patchIndex;

    private transient double totalPatchCapacity;

    private transient HashMap<Integer, Integer> removedCodes;
    private transient Ref<WritableRaster> srcRaster;
    private transient Ref<WritableRaster> patchRaster;
    private transient HashMap<File, SoftRef<Raster>> extRasters;

    public Project(String name, File prjPath, GridCoverage2D cov, TreeSet<Integer> codes, int code,
            double noData, boolean con8, double minArea, boolean simplify) throws IOException, SchemaException {

        this.name = name;
        dir = prjPath;
        this.codes = codes;
        patchCode = code;
        this.noData = noData;
        this.con8 = con8;
        this.minArea = minArea;
        this.simplify = simplify;
        capacityParams = new CapaPatchDialog.CapaPatchParam();
        
        Envelope2D gZone = cov.getEnvelope2D();
        zone = gZone.getBounds2D();
        CoordinateReferenceSystem crs = cov.getCoordinateReferenceSystem2D();
        if(crs != null)   {  
            wktCRS = crs.toWKT();
        }
        TreeMap<Integer, Envelope> envMap = new TreeMap<>();

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

        Envelope2D extZone = new Envelope2D(crs,
                gZone.x-resolution, gZone.y-resolution, gZone.width+2*resolution, gZone.height+2*resolution);

        ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Vectorizing..."), envMap.size());
        patches = new ArrayList<>();
        int n = 1, nbRem = 0;
        List<String> attrNames = new ArrayList<>(PATCH_ATTRS);

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

            monitor.incProgress(1);
            if(monitor.isCanceled()) {
                throw new CancellationException();
            }
        }

        if(patches.isEmpty()) {
            throw new IllegalStateException("There is no patch in the map. Check patch code and min area.");
        }
        monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));
        monitor.setIndeterminate(true);
        
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
        DefaultFeature.saveFeatures(patches, new File(dir, "patches.shp"), crs);
        
        clustCov = null;
        covBuilder = null;

        monitor.close();
        Logger.getLogger(MainFrame.class.getName()).log(Level.INFO, "Nb small patch removed : " + nbRem);

        WritableRaster voronoiR = rasterPatchs;
        neighborhoodEuclid(voronoiR);

        voronoi = (List<Feature>) vectorizeVoronoi(voronoiR);
        DefaultFeature.saveFeatures(voronoi, new File(prjPath, "voronoi.shp"), crs);

        exoDatas = new TreeMap<>();

        graphs = new TreeMap<>();

        costLinks = new TreeMap<>();
        
        removedCodes = new HashMap<>();
        
        MainFrame.project = this;
        
        save();
        
        createLayers();
    }
    
    /**
     * copy constructor for meta patch project
     * @param name
     * @param prj 
     */
    private Project(String name, Project prj) {
        this.name = name;
        codes = prj.codes;
        patchCode = prj.patchCode;
        noData = prj.noData;
        con8 = prj.con8;
        minArea = prj.minArea;
        simplify = prj.simplify;
        capacityParams = prj.capacityParams;
        resolution = prj.resolution;
        grid2space = prj.grid2space;
        space2grid = prj.space2grid;
        zone = prj.zone;
        wktCRS = prj.wktCRS;
        
        costLinks = new TreeMap<>();
        exoDatas = new TreeMap<>();
        graphs = new TreeMap<>();
    }
    
    // using strtree.nearest
    private void neighborhoodEuclid(final WritableRaster voronoi) throws IOException, SchemaException {
        ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Neighbor"), voronoi.getHeight());
        final STRtree index = new STRtree();
        final int NPOINTS = 50;
        final GeometryFactory factory = new GeometryFactory();
        List<DefaultFeature> simpPatches = patches;
        if(simplify) {
            simpPatches = simplify(patches);
        }
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

        planarLinks = createLinks(voronoi, monitor);

        monitor.setNote("Saving...");

        DefaultFeature.saveFeatures(planarLinks.getFeatures(), new File(dir, "links.shp"), getCRS());

        monitor.close();

    }

    public double[] getLastCosts() {
        for(Linkset cost : costLinks.values())
            if(cost.getType_dist() == Linkset.COST)
                return cost.getCosts();
        return null;
    }

    public Links getPlanarLinks() {
        return planarLinks;
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

    private Links createLinks(Raster voronoiRaster, ProgressBar monitor) {
        monitor.setNote("Create link set...");
        monitor.setProgress(0);

        Path.newSetOfPaths();
        
        Links links = new Links(patches.size());

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
                        links.addLink(new Path(f, f1));
                }
                id1  = voronoiRaster.getSample(x, y-1, 0);
                if(id1 > 0 && id != id1) {
                    Feature f1 = getPatch(id1);
                    if(!links.isLinkExist(f, f1))
                        links.addLink(new Path(f, f1));
                }
            }
        }

        return links;
    }

    public void addLinkset(Linkset cost, boolean save) throws IOException, SchemaException {
        ProgressBar progressBar = Config.getProgressBar();
        
        cost.compute(this, progressBar);

        costLinks.put(cost.getName(), cost);
        if(save) {
            if(cost.isRealPaths()) {
                DefaultFeature.saveFeatures(cost.getPaths(), new File(dir, cost.getName() + "-links.shp"), getCRS());
                cost.saveIntraLinks(getDirectory());
            }
            save(); 
            cost.saveLinks(getDirectory());
        }
        
        if(linkLayers != null) {
            Layer l = new LinkLayer(cost.getName());
            if(cost.getTopology() == Linkset.COMPLETE && cost.getDistMax() == 0)
                l.setVisible(false);
            linkLayers.addLayerFirst(l);
        }
        
        progressBar.close();
    }

    public void addPointset(Pointset exoData, List<String> attrNames, List<DefaultFeature> features, boolean save) throws SchemaException, IOException {
        for(Feature f : features){
            Coordinate c = f.getGeometry().getCoordinate();
            if(!zone.contains(c.x, c.y)) {
                throw new RuntimeException("Point outside zone !");
            }
        }

        attrNames = new ArrayList<>(attrNames);
        attrNames.remove(Project.EXO_IDPATCH);
        attrNames.remove(Project.EXO_COST);

        if(exoData.isAgreg()) {
            for(String attr : attrNames) {
                DefaultFeature.addAttribute(exoData.getName() + "." + attr, patches, Double.NaN);
            }
            DefaultFeature.addAttribute(exoData.getName() + ".NbPoint", patches, 0);
        }

        // recréé les features avec les 2 attributs en plus
        List<DefaultFeature> tmpLst = new ArrayList<>(features.size());
        List<String> attrs = new ArrayList<>(attrNames);
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

        SpacePathFinder pathFinder = getPathFinder(exoData.getLinkset());

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
                    double cost = exoData.getLinkset().isCostLength() ? res[1] : res[2];
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
                       double dist = exoData.getLinkset().isCostLength() ? distPatch.get(p).getCost() : distPatch.get(p).getDist();
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

        List<DefaultFeature> exoFeatures = new ArrayList<>();
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

        if(nErr > 0) {
            JOptionPane.showMessageDialog(null, nErr + " points have been removed -> no path found.");
        }
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

    private List<DefaultFeature> simplify(List<DefaultFeature> patches) {
        List<DefaultFeature> simpPatches = new ArrayList<DefaultFeature>();
        ProgressBar monitor = Config.getProgressBar("Simplify", patches.size());
        for(Feature f : patches) {
            simpPatches.add(new DefaultFeature(f.getId(), TopologyPreservingSimplifier.simplify(f.getGeometry(), resolution*1.5), null, null));
            monitor.incProgress(1);
        }
        monitor.close();

        return simpPatches;
    }

    public final DefaultFeature getPatch(int id) {
        if(id > 0 && id <= patches.size())
            return patches.get(id-1);
        else
            throw new IllegalArgumentException("Unknown patch id : " + id);
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

    private List<? extends Feature> vectorizeVoronoi(Raster voronoi) {
        ProgressBar monitor = Config.getProgressBar("Vectorize voronoi");
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
        monitor.setMaximum(envMap.size());
        for(Envelope env : envMap.values()) {
            env.expandBy(0.5);
            env.translate(0.5, 0.5);
        }

        List<DefaultFeature> features = new ArrayList<DefaultFeature>();
        for(Integer id : envMap.keySet()) {
            Geometry geom = vectorize(voronoi, envMap.get(id), id);
            geom.apply(grid2space);
            features.add(new DefaultFeature(id, geom, null, null));
            monitor.incProgress(1);
        }
        monitor.close();

        return features;
    }

    public static Geometry vectorize(Raster patchs, Envelope env, double val) {
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
    private static Coordinate [] simpRing(Coordinate[] coords) {
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

    public List<DefaultFeature> getPatches() {
        return patches;
    }

    public TreeSet<Integer> getCodes() {
        return codes;
    }

    public SpacePathFinder getPathFinder(Linkset linkset) throws IOException {
        if(linkset.getType_dist() == Linkset.CIRCUIT)
            throw new IllegalArgumentException("Circuit linkset is not supported");
        return linkset.getType_dist() == Linkset.EUCLID ?
            new EuclidePathFinder(this) : getRasterPathFinder(linkset);
    }
    
    public RasterPathFinder getRasterPathFinder(Linkset linkset) throws IOException {
        if(linkset.getType_dist() != Linkset.COST) {
            throw new IllegalArgumentException();
        }
        if(linkset.isExtCost()) {
            if(linkset.getExtCostFile().exists()) {
                Raster extRaster = getExtRaster(linkset.getExtCostFile());
                return new RasterPathFinder(this, extRaster, linkset.getCoefSlope());
            } else {
                throw new RuntimeException("Cost raster file " + linkset.getExtCostFile() + " not found");
            }
        } else {
            return new RasterPathFinder(this, getImageSource(), linkset.getCosts(), linkset.getCoefSlope());
        }
    }
    
    public CircuitRaster getRasterCircuit(Linkset linkset) throws IOException {
        if(linkset.getType_dist() != Linkset.CIRCUIT) {
            throw new IllegalArgumentException();
        }
        if(linkset.isExtCost()) {
            if(linkset.getExtCostFile().exists()) {
                Raster extRaster = getExtRaster(linkset.getExtCostFile());
                return new CircuitRaster(this, extRaster, true, linkset.isOptimCirc(), linkset.getCoefSlope());
            } else {
                throw new RuntimeException("Cost raster file " + linkset.getExtCostFile() + " not found");
            }
        } else {
            return new CircuitRaster(this, getImageSource(), linkset.getCosts(), true, linkset.isOptimCirc(), linkset.getCoefSlope());
        }
    }

    public void addGraph(GraphGenerator graphGen, boolean save) throws IOException, SchemaException {
        ProgressBar progressBar = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Create_graph..."));
        progressBar.setIndeterminate(true);
        graphs.put(graphGen.getName(), graphGen);
        
        if(save) {
            DefaultFeature.saveFeatures(graphGen.getComponentFeatures(),
                    new File(dir, graphGen.getName() + "-voronoi.shp"), getCRS());

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
        List<String> attrs = new ArrayList<>();
        for(String attr : patches.get(0).getAttributeNames()) {
            if(attr.endsWith("_" + graphName)) {
                attrs.add(attr);
            }
        }
        return attrs;
    }

    /**
     * 
     * @param graphName
     * @return list of detailed name metrics calculated for graph graphName
     */
    public List<String> getGraphPatchVar(String graphName) {
        List<String> attrs = new ArrayList<>();
        for(String attr : patches.get(0).getAttributeNames()) {
            if(attr.endsWith("_" + graphName)) {
                attrs.add(attr.substring(0, attr.length() - graphName.length() - 1));
            }
        }
        return attrs;
    }

    List<String> getGraphLinkAttr(GraphGenerator g) {
        List<String> attrs = new ArrayList<>();
        for(String attr : g.getLinkset().getPaths().get(0).getAttributeNames()) {
            if(attr.endsWith("_" + g.getName())) {
                attrs.add(attr);
            }
        }
        return attrs;
    }

    public void removeGraph(final String name) {
        GraphGenerator g = graphs.remove(name);

        for(String attr : getGraphPatchAttr(name)) {
            DefaultFeature.removeAttribute(attr, patches);
        }

        for(String attr : getGraphLinkAttr(g)) {
            DefaultFeature.removeAttribute(attr, g.getLinkset().getPaths());
        }

        try {
            g.getLinkset().saveLinks(getDirectory());
            savePatch();
            save();
        } catch (IOException | SchemaException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().startsWith(name + "-voronoi.") && f.getName().length()-3 == (name + "-voronoi.").length();
            }
        };
        for(File f : dir.listFiles(filter)) {
            f.delete();
        }

        for(Layer l : new ArrayList<>(graphLayers.getLayers())) {
            if(l.getName().equals(name)) {
                graphLayers.removeLayer(l);
            }
        }
    }

    public Collection<GraphGenerator> getGraphs() {
        return graphs.values();
    }
    
    public GraphGenerator getGraph(String name) {
        if(graphs.containsKey(name)) {
            return graphs.get(name);
        }
        if(name.contains("!")) {
            String[] split = name.split("!");
            return new GraphGenerator(graphs.get(split[0]), split[1], split[2]);
        }
        throw new IllegalArgumentException("Unknown graph " + name);
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

    public synchronized List<Feature> getVoronoi() {
        if(voronoi == null)
            try {
                List<DefaultFeature> features = GlobalDataStore.getFeatures(new File(dir, "voronoi.shp"), "Id", null);
                voronoi = new ArrayList<Feature>(features);
                for(Feature f : features)
                    voronoi.set((Integer)f.getId()-1, f);
            } catch (IOException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            }

        return voronoi;
    }

    public List<DefaultFeature> loadVoronoiGraph(String name) throws IOException {
        List<DefaultFeature> features = GlobalDataStore.getFeatures(new File(dir, name + "-voronoi.shp"), "Id", null);

        HashMap<Object, DefaultFeature> map = new HashMap<Object, DefaultFeature>();
        for(DefaultFeature f : features)
            map.put(f.getId(), f);

        File fCSV = new File(dir, name + "-voronoi.csv");
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

    public void createMetaPatchProject(String prjName, GraphGenerator graph, double alpha) throws IOException, SchemaException {
        File dir = new File(getDirectory(), prjName);
        dir.mkdir();
        IOFile.copyFile(new File(getDirectory(), "source.tif"), new File(dir, "source.tif"));
        List<Graph> components = graph.getComponents();
        int[] idMetaPatch = new int[getPatches().size()+1];
        for(int i = 0; i < components.size(); i++) {
            Graph comp = components.get(i);
            for(Node n : (Collection<Node>)comp.getNodes())
                idMetaPatch[(Integer)((Feature)n.getObject()).getId()] = i+1;
        }
        // create new raster patch
        WritableRaster newRaster = getRasterPatch().createCompatibleWritableRaster();
        newRaster.setRect(getRasterPatch());
        DataBufferInt buf = (DataBufferInt) newRaster.getDataBuffer();
        for(int i = 0; i < buf.getSize(); i++) {
            if(buf.getElem(i) <= 0)
                continue;
            buf.setElem(i, idMetaPatch[buf.getElem(i)]);
        }
        
        GridCoverage2D gridCov = new GridCoverageFactory().create("rasterpatch",
                newRaster, new Envelope2D(getCRS() != null ? getCRS() : DefaultGeographicCRS.WGS84, zone));
        new GeoTiffWriter(new File(dir, "patches.tif")).write(gridCov, null);
        
        List<DefaultFeature> metaPatches = new ArrayList<>();
        List<DefaultFeature> metaVoronois = new ArrayList<>();
        // create patches and voronoi features
        for(int i = 0; i < components.size(); i++) {
            Graph comp = components.get(i);
            List<Geometry> metaPatch = new ArrayList<>();
            List<Geometry> metaVoronoi = new ArrayList<>();
            double capa = 0;
            for(Node n : (Collection<Node>)comp.getNodes()) {
                DefaultFeature patch = (DefaultFeature)n.getObject();
                if(alpha > 0) {
                    GraphGenerator.PathFinder pathfinder = graph.getPathFinder(n);
                    for(Node n2 : (Collection<Node>)comp.getNodes()) {
//                        if(n == n2)
//                            continue;
                        capa += getPatchCapacity(n2)*Math.exp(-alpha*pathfinder.getCost(n2)) / comp.getNodes().size();
                    }
                } else
                    capa += getPatchCapacity(patch);
                metaPatch.add(patch.getGeometry());
                metaVoronoi.add(getVoronoi((Integer)patch.getId()).getGeometry());
            }
            Geometry patchGeom = JTS.flattenGeometryCollection(new GeometryFactory().buildGeometry(metaPatch));
            metaPatches.add(new DefaultFeature(i+1, patchGeom, PATCH_ATTRS, 
                    Arrays.asList(i+1, patchGeom.getArea(), patchGeom.getBoundary().getLength(), capa)));
            Geometry voronoiGeom = CascadedPolygonUnion.union(metaVoronoi);
            metaVoronois.add(new DefaultFeature(i+1, voronoiGeom, new ArrayList<String>(0), new ArrayList(0)));
        }
        
        DefaultFeature.saveFeatures(metaPatches, new File(dir, "patches.shp"), getCRS());
        DefaultFeature.saveFeatures(metaVoronois, new File(dir, "voronoi.shp"), getCRS());
        
        Links links = new Links(metaPatches.size());
        for(Path p : planarLinks.getFeatures()) {
            int id1 = idMetaPatch[(Integer)p.getPatch1().getId()];
            int id2 = idMetaPatch[(Integer)p.getPatch2().getId()];
            DefaultFeature p1 = metaPatches.get(id1-1);
            DefaultFeature p2 = metaPatches.get(id2-1);
            if(id1 == id2 || links.isLinkExist(p1, p2))
                continue;
            links.addLink(new Path(p1, p2));
        }
        
        DefaultFeature.saveFeatures(links.getFeatures(), new File(dir, "links.shp"), getCRS());
        
        Project prj = new Project(prjName, this);
        prj.dir = dir;
        prj.save();
    }
    
    public File getProjectFile() {
        return new File(dir, name + ".xml");
    }

    public File getDirectory() {
        return dir;
    }

    private void createLayers() {
        CoordinateReferenceSystem crs = getCRS();
        rootLayer = new DefaultGroupLayer(name, true);
        rootLayer.addLayerFirst(new FeatureLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Patch"), patches,
                new FeatureStyle(new Color(0x65a252), new Color(0x426f3c)), crs));

        linkLayers = new DefaultGroupLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Link_sets"));
        rootLayer.addLayerFirst(linkLayers);
       
        try {
            RasterLayer l = new RasterLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Landscape_map"), new RasterShape(
                getImageSource().getParent(), zone, new RasterStyle(), true), crs);
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
                planarLinks.getFeatures(), new LineStyle(new Color(0xbcc3ac)), crs);
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
        
    }

    public static synchronized Project loadProject(File file, boolean all) throws IOException {
        
        XStream xstream = new XStream();
        xstream.alias("Project", Project.class);
        xstream.alias("Pointset", Pointset.class);
        xstream.alias("Linkset", Linkset.class);
        xstream.alias("Graph", GraphGenerator.class);
        
        // pour compatibilité avec projet 1.1-beta2 et antérieur
        xstream.alias("org.thema.graphab.ExoData", Pointset.class);
        xstream.alias("org.thema.graphab.CostDistance", Linkset.class);
        xstream.alias("org.thema.graphab.GraphGenerator", GraphGenerator.class);
        
        Project prj;
        try (FileReader fr = new FileReader(file)) {
            prj = (Project) xstream.fromXML(fr);
        }

        prj.dir = file.getAbsoluteFile().getParentFile();
        
        ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Loading_project..."), 
                100*(2+prj.costLinks.size()) + 10*prj.exoDatas.size());
        List<DefaultFeature> features = GlobalDataStore.getFeatures(new File(prj.dir, "patches.shp"), 
                "Id", monitor.getSubProgress(100));
        prj.patches = new ArrayList<>(features);
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

        monitor.close();
        
        return prj;
    }


    public GridCoverage2D loadCoverage(File file) throws IOException {
        GridCoverage2D cov;
        if(file.getName().toLowerCase().endsWith(".tif"))
            cov = IOImage.loadTiffWithoutCRS(file);
        else if(file.getName().toLowerCase().endsWith(".asc"))
            cov = IOImage.loadArcGrid(file);
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
    
    public synchronized Raster getExtRaster(File file) throws IOException {
        Raster raster = null;
        
        if(extRasters == null)
            extRasters = new HashMap<File, SoftRef<Raster>>();
        if(extRasters.containsKey(file))
            raster = extRasters.get(file).get();
        
        if(raster == null) {
            raster = loadCoverage(file).getRenderedImage().getData();
            raster = raster.createTranslatedChild(1, 1);
            extRasters.put(file, new SoftRef<Raster>(raster));
        }
        
        return raster;
    }

    public static double getArea() {
        return getProject().zone.getWidth()*getProject().zone.getHeight();
    }

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

    public Raster getDemRaster() throws IOException {
        if(demFile.isAbsolute())
            return getExtRaster(demFile);
        else 
            return getExtRaster(new File(Project.getProject().getDirectory(), demFile.getPath()));
    }
    
    public void setDemFile(File demFile) throws IOException {
        String prjPath = Project.getProject().getDirectory().getAbsolutePath();
        if(demFile.getAbsolutePath().startsWith(prjPath)) 
            this.demFile = new File(demFile.getAbsolutePath().substring(prjPath.length()+1));
        else 
            this.demFile = demFile.getAbsoluteFile();
        // try loading DEM
        getDemRaster();
        save();
    }
    
    public boolean isDemExist() {
        return demFile != null;
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

    public CoordinateReferenceSystem getCRS() {
        if(wktCRS != null && !wktCRS.isEmpty())
            try {
                return CRS.parseWKT(wktCRS);
            } catch (FactoryException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.WARNING, null, ex);
            }
        return null;
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

    public DefaultFeature createPatch(Geometry geom, double capa) {
        List<String> attrNames = new ArrayList<String>(PATCH_ATTRS);
        List attrs = new ArrayList(Arrays.asList(new Double[attrNames.size()]));
        attrs.set(attrNames.indexOf(CAPA_ATTR), capa);
        attrs.set(attrNames.indexOf(AREA_ATTR), resolution*resolution);
        attrs.set(attrNames.indexOf(PERIM_ATTR), resolution*4);
        return new DefaultFeature(patches.size()+1, geom, attrNames, attrs);
    }
    
    public synchronized DefaultFeature addPatch(Point point, double capa) throws IOException {
        // tester si pas dans un patch ou touche un patch
        if(!canCreatePatch(point))
            throw new IllegalArgumentException("Patch already exists at the same position : " + point.toString());
        DefaultFeature patch = createPatch(point, capa);
        int id = (Integer)patch.getId();
        Coordinate cg = space2grid.transform(point.getCoordinate(), new Coordinate());
        // on passe les raster en strong reference pour qu'ils ne puissent pas être supprimé
        patchRaster = new StrongRef<WritableRaster>(getRasterPatch());
        srcRaster = new StrongRef<WritableRaster>(getImageSource());
        removedCodes.put(id, getImageSource().getSample((int)cg.x, (int)cg.y, 0));
        getRasterPatch().setSample((int)cg.x, (int)cg.y, 0, id);
        getImageSource().setSample((int)cg.x, (int)cg.y, 0, patchCode);
        
        patches.add(patch);
        patchIndex = null;
        return patch;
    }
    
    public synchronized DefaultFeature addPatch(Geometry geom, double capa) throws IOException {
        if(geom instanceof Point) { // pas sûr que ce soit utile...
            return addPatch((Point) geom, capa);
        }
        // tester si pas dans un patch ou touche un patch
        if(!canCreatePatch(geom))
            throw new IllegalArgumentException("Patch already exist at the same position");
                    
        DefaultFeature patch = createPatch(geom, capa);
        int id = (Integer)patch.getId();
        // on passe les raster en strong reference pour qu'ils ne puissent pas être supprimé
        patchRaster = new StrongRef<WritableRaster>(getRasterPatch());
        srcRaster = new StrongRef<WritableRaster>(getImageSource());

        GeometryFactory geomFactory = geom.getFactory();
        Geometry geomGrid = getSpace2grid().transform(geom);
        Envelope env = geomGrid.getEnvelopeInternal();
        for(double y = (int)env.getMinY() + 0.5; y <= Math.ceil(env.getMaxY()); y++) {
            for(double x = (int)env.getMinX() + 0.5; x <= Math.ceil(env.getMaxX()); x++) {
                Point p = geomFactory.createPoint(new Coordinate(x, y));
                if(geomGrid.contains(p)) {
                    getRasterPatch().setSample((int)x, (int)y, 0, id);
                    getImageSource().setSample((int)x, (int)y, 0, patchCode);
                }
            }
        }
        patches.add(patch);
        patchIndex = null;
        return patch;
    }

    public boolean canCreatePatch(Geometry geom) throws IOException {
        if(geom instanceof Point) {// pas sûr que ce soit utile...
            return canCreatePatch((Point) geom);
        }
        // tester si pas dans un patch ou touche un patch
        GeometryFactory geomFactory = geom.getFactory();
        Geometry geomGrid = getSpace2grid().transform(geom);
        Envelope env = geomGrid.getEnvelopeInternal();
        for(double y = (int)env.getMinY() + 0.5; y <= Math.ceil(env.getMaxY()); y++) {
            for(double x = (int)env.getMinX() + 0.5; x <= Math.ceil(env.getMaxX()); x++) {
                Point p = geomFactory.createPoint(new Coordinate(x, y));
                if(geomGrid.contains(p)) {
                    if(!canCreatePatch((Point)getGrid2space().transform(p)))
                        return false;
                }
            }
        }
        return true;
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
    
    public synchronized void removePointPatch(Feature patch) throws IOException {
        if(!(patch.getGeometry() instanceof Point))
            throw new IllegalArgumentException("Cannot remove patch with geometry different of Point");
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
            super(name, exoDatas.get(name).getFeatures(), null, Project.this.getCRS());
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
//                        RasterPathFinder pathfinder = getRasterPathFinder(exo.getLinkset());
//                        for(DefaultFeature fexo : Project.getProject().getPointset(name)) {
//                            double ifpc = 0;
//                            HashMap<DefaultFeature, Path> dists = pathfinder.calcPaths(fexo.getGeometry().getCentroid().getCoordinate(), max, false);
//                            for(DefaultFeature patch : dists.keySet()) {
//                                ifpc += Project.getPatchCapacity(patch) / (dists.get(patch).getLinkset()+1);
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
                }, zone, new LineStyle(new Color(costLinks.get(name).getTopology() == Linkset.PLANAR ? 0x25372b : 0xb8c45d)), 
                Project.this.getCRS());
        }

        @Override
        public JPopupMenu getContextMenu() {
            JPopupMenu menu = super.getContextMenu();
            menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove...")) {
                public void actionPerformed(ActionEvent e) {
                    List<String> exoNames = new ArrayList<String>();
                    for(Pointset exo : exoDatas.values())
                        if(exo.getLinkset().getName().equals(name))
                            exoNames.add(exo.getName());
                    if(!exoNames.isEmpty()) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Links_is_used_by_exogenous_data") +
                                Arrays.deepToString(exoNames.toArray()));
                        return;
                    }
                    List<String> graphNames = new ArrayList<String>();
                    for(GraphGenerator g : getGraphs())
                        if(g.getLinkset().getName().equals(name))
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
                new DeltaPCMetric()/*, new EntropyLocal2GlobalIndice(new BCLocalMetric()), new PCCircMetric()*/));
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
