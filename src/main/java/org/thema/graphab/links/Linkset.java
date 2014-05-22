/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.strtree.STRtree;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.geotools.feature.SchemaException;
import org.thema.data.GlobalDataStore;
import org.thema.common.Config;
import org.thema.common.JTS;
import org.thema.common.collection.HashMapList;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.ParallelFTask;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public class Linkset {

    public static final int COMPLETE = 1;
    public static final int PLANAR = 2;

    public static final int EUCLID = 1;
    public static final int COST = 2;
    public static final int CIRCUIT = 4;

    public static final int COST_LENGTH = 1;
    public static final int DIST_LENGTH = 2;


    private String name;
    private int type;
    private int type_dist;
    private int type_length;
    private double [] costs;
    private boolean realPaths;
    private boolean removeCrossPatch;
    private double coefSlope;

    private double distMax;
    
    private File extCostFile;

    private boolean optimCirc;
    
    private transient List<Path> paths;
    private transient HashMap<MultiKey, double[]> intraLinks;
    
    /**
     * Jeu de lien en distance cout
     * @param name
     * @param type
     * @param costs
     * @param type_length
     * @param realPaths
     * @param removeCrossPatch
     * @param distMax 
     */
    public Linkset(String name, int type, double[] costs, int type_length, boolean realPaths, 
            boolean removeCrossPatch, double distMax, double coefSlope) {
        this.name = name;
        this.type = type;
        this.type_dist = COST;
        this.type_length = type_length;
        this.costs = Arrays.copyOf(costs, costs.length);
        this.realPaths = realPaths;
        this.removeCrossPatch = removeCrossPatch;
        this.distMax = distMax;
        this.coefSlope = coefSlope;
    }

    /**
     * Jeu de lien distance euclidienne
     * @param name
     * @param type
     * @param realPaths
     * @param distMax 
     */
    public Linkset(String name, int type, boolean realPaths, double distMax) {
        this.name = name;
        this.type = type;
        this.type_dist = EUCLID;
        this.type_length = DIST_LENGTH;
        this.distMax = distMax;
        this.realPaths = realPaths;
        this.removeCrossPatch = false;
    }


    /**
     * Jeu de lien avec des couts externes
     * @param name
     * @param type
     * @param type_length
     * @param realPaths
     * @param removeCrossPatch
     * @param distMax
     * @param extCostFile 
     */
    public Linkset(String name, int type, int type_length, boolean realPaths, 
            boolean removeCrossPatch, double distMax, File extCostFile, double coefSlope) {
        this.name = name;
        this.type = type;
        this.type_dist = COST;
        this.type_length = type_length;
        this.realPaths = realPaths;
        this.removeCrossPatch = removeCrossPatch;
        this.distMax = distMax;
        this.coefSlope = coefSlope;
        
        String prjPath = Project.getProject().getDirectory().getAbsolutePath();
        if(extCostFile.getAbsolutePath().startsWith(prjPath)) 
            this.extCostFile = new File(extCostFile.getAbsolutePath().substring(prjPath.length()+1));
        else 
            this.extCostFile = extCostFile.getAbsoluteFile();

    }
    
    /**
     * Jeu de lien distance circuit
     * @param name
     * @param type
     * @param costs
     * @param extCostFile
     * @param optimCirc
     */
    public Linkset(String name, int type, double[] costs, File extCostFile, boolean optimCirc) {
        if(costs != null && extCostFile != null)
            throw new IllegalArgumentException();
        this.name = name;
        this.type = type;
        this.type_dist = CIRCUIT;
        this.type_length = COST_LENGTH;
        this.distMax = Double.NaN;
        this.costs = costs;
        this.extCostFile = extCostFile;
        this.realPaths = false;
        this.removeCrossPatch = false;
        this.optimCirc = optimCirc;
    }

    public double getDistMax() {
        return distMax;
    }

    public String getName() {
        return name;
    }

    public boolean isRealPaths() {
        return realPaths;
    }

    public boolean isRemoveCrossPatch() {
        return removeCrossPatch;
    }

    public boolean isUseSlope() {
        return coefSlope != 0;
    }

    public double getCoefSlope() {
        return coefSlope;
    }
    
    public int getTopology() {
        return type;
    }

    public int getType_dist() {
        // compatibilité < 1.3
        if(type_dist == 3)
            return COST;
        return type_dist;
    }

    public int getType_length() {
        return type_length;
    }
    
    public boolean isExtCost() {
        return extCostFile != null;
    }

    public boolean isCostLength() {
        return type_length == COST_LENGTH;
    }

    public double[] getCosts() {
        return costs;
    }

    public File getExtCostFile() {
        if(extCostFile.isAbsolute())
            return extCostFile;
        else 
            return new File(Project.getProject().getDirectory(), extCostFile.getPath());
    }

    /**
     * Circuit specific option
     * @return true if raster size is optimized for circuit
     */
    public boolean isOptimCirc() {
        return optimCirc;
    }

    /**
     * load if it's not already the case and return all paths
     * @return 
     */
    public synchronized List<Path> getPaths() {
        if(paths == null) 
            try {
                loadPaths(Project.getProject(), new TaskMonitor.EmptyMonitor());
            } catch (IOException ex) {
                Logger.getLogger(Linkset.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return paths;
    }
    
    public double[] getIntraLinkCost(Coordinate c1, Coordinate c2) {
        if(c1.compareTo(c2) < 0)
            return getIntraLinks().get(new MultiKey(c1, c2));
        else
            return getIntraLinks().get(new MultiKey(c2, c1));
    }
    
    public double[] getIntraLinkCost(Path p1, Path p2) {
        Feature patch = Path.getCommonPatch(p1, p2);
        Coordinate c1 = p1.getCoordinate(patch);
        Coordinate c2 = p2.getCoordinate(patch);
        return getIntraLinkCost(c1, c2);
    }
    
    private synchronized HashMap<MultiKey, double[]> getIntraLinks() {
        if(!isRealPaths())
            throw new IllegalStateException("Intra patch links need real paths");
        
        if(intraLinks == null)
            try {
                loadIntraLinks(Project.getProject().getDirectory());
            } catch (Exception ex) {
                Logger.getLogger(Linkset.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return intraLinks;
    }

    /**
     * Return detailed informations of the linkset.<br/>
     * The language is local dependent
     * @return 
     */
    public String getInfo() {
        ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/links/Bundle");
        
        String info = bundle.getString("LinksetPanel.nameLabel.text") + " : " + name;
        info += "\n" + bundle.getString("LinksetPanel.topoPanel.border.title") + " : ";
        if(type == COMPLETE) {
            info += bundle.getString("LinksetPanel.completeRadioButton.text");
            if(distMax > 0)
                info += " " + bundle.getString("LinksetPanel.distMaxLabel.text") + " " + distMax;
        }
        else
            info += bundle.getString("LinksetPanel.planarRadioButton.text");
        info += "\n" + bundle.getString("LinksetPanel.distPanel.border.title") + " : ";
        switch(getType_dist()) {
            case EUCLID:
                info += bundle.getString("LinksetPanel.euclidRadioButton.text");
                break;
            case CIRCUIT:
                info += "Circuit\n";
            case COST:
                if(isExtCost()) {
                    info += bundle.getString("LinksetPanel.rasterRadioButton.text") + "\nFile : " + extCostFile.getAbsolutePath();
                } else {
                    info += bundle.getString("LinksetPanel.costRadioButton.text") + "\n";
                    for(Integer code : Project.getProject().getCodes())
                        info += code + " : " + costs[code] + "\n";
                }       
                if(isUseSlope())
                    info += "Use slope : " + coefSlope + "\n";
                break;
        }
        
        if(getType_dist() == COST) {
            info += "\n" + bundle.getString("LinksetPanel.impedancePanel.border.title") + " : ";
            if(type_length == COST_LENGTH)
                info += bundle.getString("LinksetPanel.costDistRadioButton.text");
            else
                info += bundle.getString("LinksetPanel.lengthRadioButton.text");
        }

        if(realPaths)
            info += "\n" + bundle.getString("LinksetPanel.realPathCheckBox.text");

        if(getType_dist() == COST && removeCrossPatch)
            info += "\n" + bundle.getString("LinksetPanel.removeCrossPatchCheckBox.text");

        info += "\n\n# links : " + paths.size();

        return info;
    }
    
    /**
     * 
     * @return the name of the linkset
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Compute all links defined in this linkset.<br/>
     * This method is called only once by the project
     * @param prj
     * @param progressBar
     * @throws Throwable 
     */
    public void compute(Project prj, ProgressBar progressBar) throws Throwable {
        progressBar.setNote("Create linkset " + getName());
        
        if(getType_dist() == Linkset.EUCLID) {
            calcEuclidLinkset(prj, progressBar);
        } else if(getType_dist() == Linkset.CIRCUIT) {
            calcCircuitLinkset(prj, progressBar);
        } else {
            calcCostLinkset(prj, progressBar);
        }
        progressBar.reset();
        progressBar.setNote("Create intra links...");
        if(isRealPaths())
            calcIntraLinks(prj, progressBar);
    }
    
    /**
     * Compute and return corridors of all paths existing in this linkset
     * @param prj
     * @param progressBar
     * @param maxCost maximal cost distance 
     * @return list of features where id equals id path and geometry is a polygon or multipolygon
     */
    public List<Feature> computeCorridor(final Project prj, ProgressBar progressBar, final double maxCost) {
        if(getType_dist() == Linkset.EUCLID) 
            throw new IllegalArgumentException("Euclidean linkset is not supported for corridor");
        
        final List<Feature> corridors = Collections.synchronizedList(new ArrayList<Feature>(getPaths().size()));
        
        ParallelFTask task = new SimpleParallelTask<Path>(getPaths(), progressBar) {
            @Override
            protected void executeOne(Path path) {
                try {
                    Geometry corridor;
                    if(getType_dist() == Linkset.COST)
                        corridor = calcCostCorridor(prj, path, maxCost);
                    else
                        corridor = calcCircuitCorridor(prj, path, maxCost);
                    if(!corridor.isEmpty())
                        corridors.add(new DefaultFeature(path.getId(), corridor));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

        };
        
        new ParallelFExecutor(task).executeAndWait();
        
        return corridors;
    }
    
    private Geometry calcCircuitCorridor(Project prj, Path path, double maxCost) throws Exception {
        CircuitRaster circuit = prj.getRasterCircuit(this);
        CircuitRaster.ODCircuit odCircuit = circuit.getODCircuit(path.getPatch1(), path.getPatch2());
        return odCircuit.getCorridor(maxCost);
    }

    private Geometry calcCostCorridor(Project prj, Path path, double maxCost) throws Exception {
        if(path.getCost() > maxCost)
            return new GeometryFactory().buildGeometry(Collections.EMPTY_LIST);
        RasterPathFinder pathfinder = prj.getRasterPathFinder(this);
        Raster r1 = pathfinder.getDistRaster(path.getPatch1(), maxCost);
        Raster r2 = pathfinder.getDistRaster(path.getPatch2(), maxCost);
        final Rectangle rect = r1.getBounds().intersection(r2.getBounds());

        final int id1 = (Integer)path.getPatch1().getId();
        final int id2 = (Integer)path.getPatch2().getId();
        WritableRaster corridor = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, rect.width, rect.height, 1, new Point(rect.x, rect.y));
        for(int y = rect.y; y < rect.getMaxY(); y++)
            for(int x = rect.x; x < rect.getMaxX(); x++) {
                int id = prj.getRasterPatch().getSample(x, y, 0);
                if(id != id1 && id != id2 &&
                        r1.getSampleDouble(x, y, 0)+r2.getSampleDouble(x, y, 0) <= maxCost)
                    corridor.setSample(x, y, 0, 1);
            }
        
        Geometry geom =  Project.vectorize(corridor, JTS.rectToEnv(rect), 1);
        return prj.getGrid2space().transform(geom);
    }
    
    private void calcCostLinkset(final Project prj, ProgressBar progressBar) throws Throwable {
        final boolean allLinks = getTopology() == Linkset.COMPLETE;
        final List<Path> links = Collections.synchronizedList(new ArrayList<Path>(prj.getPatches().size() * 4));
        Path.newSetOfPaths();
        long start = System.currentTimeMillis();

        ParallelFTask task = new AbstractParallelFTask(progressBar) {
            @Override
            protected Object execute(int start, int end) {
                try {
                    RasterPathFinder pathfinder = prj.getRasterPathFinder(Linkset.this);
                    for(Feature orig : prj.getPatches().subList(start, end)) {
                        if(isCanceled())
                            throw new CancellationException();
                        
                        HashMap<Feature, Path> paths;
                        if(allLinks) {
                            //Envelope env = dMax == 0 ? null : orig.getGeometry().buffer(dMax).getEnvelopeInternal();
                            paths = pathfinder.calcPaths(orig, getDistMax(), isRealPaths(), false);
                        } else {
                            List<Feature> dests = new ArrayList<Feature>();
                            for(Integer dId : prj.getPlanarLinks().getNeighbors(orig))
                                if(((Integer)orig.getId()) < dId)
                                    dests.add(prj.getPatch(dId));

                            if(dests.isEmpty())
                                continue;

                            paths = pathfinder.calcPaths(orig, dests);
                        }

                        for(Feature d : paths.keySet()) {
                            Path p = paths.get(d);
                            boolean add = true;
                            if(isRemoveCrossPatch() && isRealPaths()) {
                                List lst = prj.getPatchIndex().query(p.getGeometry().getEnvelopeInternal());
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
                    throw new RuntimeException(e);
                }
                return null;
            }

            public int getSplitRange() {
                return prj.getPatches().size();
            }
            public void finish(Collection results) {
            }
            public Object getResult() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        new ParallelFExecutor(task).executeAndWait();

        if(task.isCanceled())
            return ;

        System.out.println("Temps écoulé : " + (System.currentTimeMillis()-start));
        
        paths = links;
    }

    private void calcEuclidLinkset(final Project prj, ProgressBar progressBar) throws Throwable {
        final boolean allLinks = getTopology() == Linkset.COMPLETE;
        
        Path.newSetOfPaths();
        
        final List<Path> links = Collections.synchronizedList(new ArrayList<Path>(prj.getPatches().size() * 4));
        final STRtree index = prj.getPatchIndex();
        
        long start = System.currentTimeMillis();
        ParallelFTask task = new AbstractParallelFTask(progressBar) {
            @Override
            protected Object execute(int start, int end) {
                
                for(Feature orig : prj.getPatches().subList(start, end)) {
                    if(isCanceled())
                        return null;
                    if(allLinks) {
                        List<DefaultFeature> nearPatches = prj.getPatches();
                        if(getDistMax() > 0) {
                            Envelope env = orig.getGeometry().getEnvelopeInternal();
                            env.expandBy(getDistMax());
                            nearPatches = (List<DefaultFeature>)index.query(env);
                        }
                        
                        for(Feature dest : nearPatches)
                            if(((Integer)orig.getId()) < (Integer)dest.getId()){
                                Path p = Path.createEuclidPath(orig, dest);
                                if(getDistMax() == 0 || p.getDist() <= getDistMax())
                                    links.add(p);
                            }
                    } else
                        for(Integer dId : prj.getPlanarLinks().getNeighbors(orig)) {
                            Feature d = prj.getPatch(dId);
                            if(((Integer)orig.getId()) < dId)
                                links.add(Path.createEuclidPath(orig, d));
                        }

                    incProgress(1);
                }
                return null;
            }

            public int getSplitRange() {
                return prj.getPatches().size();
            }
            public void finish(Collection results) {
            }
            public Object getResult() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        new ParallelFExecutor(task).executeAndWait();

        if(task.isCanceled())
            return ;
        
        System.out.println("Temps écoulé : " + (System.currentTimeMillis()-start));
        paths = links;
    }
    
    private void calcCircuitLinkset(final Project prj, ProgressBar progressBar) throws Throwable {
        final boolean allLinks = getTopology() == Linkset.COMPLETE;
        final List<Path> links = Collections.synchronizedList(new ArrayList<Path>(prj.getPatches().size() * 4));
        Path.newSetOfPaths();
        long start = System.currentTimeMillis();
        final CircuitRaster circuit = costs != null ? 
                    new CircuitRaster(prj, prj.getImageSource(), costs, true, optimCirc)
                    : new CircuitRaster(prj, prj.getExtRaster(getExtCostFile()), true, optimCirc);
        ParallelFTask task;
        task = new AbstractParallelFTask(progressBar) {
            @Override
            protected Object execute(int start, int end) {
                try {
                    for(Feature orig : prj.getPatches().subList(start, end)) {
                        if(isCanceled())
                            throw new CancellationException();
                        
                        if(allLinks) {
                            for(Feature patch : prj.getPatches())
                                if((Integer)orig.getId() < (Integer)patch.getId()) {
                                    double r = circuit.getODCircuit(orig, patch).getR();
                                    links.add(new Path(orig, patch, r, Double.NaN));
                                }
                        } else {
                            for(Integer dId : prj.getPlanarLinks().getNeighbors(orig))
                                if(((Integer)orig.getId()) < dId) {
                                    double r = circuit.getODCircuit(orig, prj.getPatch(dId)).getR();
                                    links.add(new Path(orig, prj.getPatch(dId), r, Double.NaN));
                                }
                        }
                        
                        incProgress(1);
                    }
                    
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            public int getSplitRange() {
                return prj.getPatches().size();
            }
            public void finish(Collection results) {
            }
            public Object getResult() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        new ParallelFExecutor(task).executeAndWait();

        if(task.isCanceled())
            return ;

        System.out.println("Temps écoulé : " + (System.currentTimeMillis()-start));
        
        paths = links;
    }

    private void calcIntraLinks(final Project prj, ProgressBar progressBar) {
        final HashMapList<Feature, Path> mapLinks = new HashMapList<Feature, Path>();
        for(Path p : paths) {
            mapLinks.putValue(p.getPatch1(), p);
            mapLinks.putValue(p.getPatch2(), p);
        }
        
        final HashMap<MultiKey, double[]> mapIntraLinks = new HashMap<MultiKey, double[]>();
        SimpleParallelTask<Feature> task = new SimpleParallelTask<Feature>(new ArrayList<Feature>(mapLinks.keySet()), progressBar) {
            @Override
            protected void executeOne(Feature patch) {
                SpacePathFinder pathFinder;
                try {
                    pathFinder = prj.getPathFinder(Linkset.this);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                List<Path> links = mapLinks.get(patch);
                HashSet<Coordinate> pointSet = new HashSet<Coordinate>();
                for(Path link : links) {
                    pointSet.add(link.getCoordinate(patch));
                }
                
                List<Coordinate> pointList = new ArrayList<Coordinate>(pointSet);
                for(int i = 0; i < pointList.size()-1; i++) { 
                    Coordinate c1 = pointList.get(i);

                    List<Coordinate> dests = pointList.subList(i+1, pointList.size());
                    List<double[]> values = pathFinder.calcPaths(c1, dests);
                    for(int k = 0; k < values.size(); k++) 
                        synchronized(mapIntraLinks) {
                            if(c1.compareTo(dests.get(k)) < 0)
                                mapIntraLinks.put(new MultiKey(c1, dests.get(k)), values.get(k));
                            else
                                mapIntraLinks.put(new MultiKey(dests.get(k), c1), values.get(k));
                        }
                }
                
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        if(task.isCanceled()) 
            return;
        intraLinks = mapIntraLinks;
    }

    public void loadPaths(Project prj, ProgressBar mon) throws IOException {
        File fCSV = new File(prj.getDirectory(), name + "-links.csv");
        CSVReader r = new CSVReader(new FileReader(fCSV));
        String [] attrNames = r.readNext();
        Path.newSetOfPaths(Arrays.asList(attrNames).subList(4, attrNames.length));
        HashMap<Object, Path> map = new HashMap<Object, Path>();
        String [] tab;
        while((tab = r.readNext()) != null) {
            Path p = Path.deserialPath(tab, prj);
            map.put(p.getId(), p);
        }
        r.close();

        if(realPaths) {
            List<DefaultFeature> features = GlobalDataStore.getFeatures(
                    new File(prj.getDirectory(), name + "-links.shp"), "Id", mon);

            for(DefaultFeature f : features)
                map.get(f.getId()).setGeometry(f.getGeometry());

        }

        paths = new ArrayList<Path>(map.values());
    }
    
    public void saveLinks(File dir) throws IOException, SchemaException {
        CSVWriter w = new CSVWriter(new FileWriter(new File(dir, name + "-links.csv")));
        w.writeNext(getPaths().get(0).getAttributeNames().toArray(new String[0]));
        
        for(Path p : getPaths())
            w.writeNext(Path.serialPath(p));
        
        w.close();
    }
    
    private void loadIntraLinks(File dir) throws Exception {
        File fCSV = new File(dir, name + "-links-intra.csv");
        if(!fCSV.exists()) {
            calcIntraLinks(Project.getProject(), Config.getProgressBar("Compute intra links"));
            saveIntraLinks(dir);
        }
        CSVReader r = new CSVReader(new FileReader(fCSV));
        r.readNext();
        intraLinks = new HashMap<MultiKey, double[]>();
        String [] tab;
        while((tab = r.readNext()) != null) {
            String[] ordinates = tab[0].split("-");
            Coordinate c0 = new Coordinate(Double.parseDouble(ordinates[0]), Double.parseDouble(ordinates[1]));
            ordinates = tab[1].split("-");
            Coordinate c1 = new Coordinate(Double.parseDouble(ordinates[0]), Double.parseDouble(ordinates[1]));
            intraLinks.put(new MultiKey(c0, c1), new double[]{Double.parseDouble(tab[2]), Double.parseDouble(tab[3])});
        }
        r.close();
    }

    public void saveIntraLinks(File dir) throws IOException {
        File fCSV = new File(dir, name + "-links-intra.csv");
        CSVWriter w = new CSVWriter(new FileWriter(fCSV));
        w.writeNext(new String[]{"Coord1", "Coord2", "Cost", "Length"});
        
        for(MultiKey key : intraLinks.keySet()) {
            double [] val = intraLinks.get(key);
            Coordinate c0 = (Coordinate) key.getKey(0);
            Coordinate c1 = (Coordinate) key.getKey(1);
            w.writeNext(new String[]{c0.x + "-" + c0.y, c1.x + "-" + c1.y, ""+val[0], ""+val[1]});
        }
        
        w.close();
    }
    
    /**
     * Add links for the patch
     * The patch have to be added in the project before (Project.addPatch)
     * @param patch must be point geometry
     * @throws Exception 
     */
    public void addLinks(DefaultFeature patch) throws Exception {
        HashMap<DefaultFeature, Path> links = calcNewLinks(patch);
        for(DefaultFeature d : links.keySet()) 
            if(realPaths)
                paths.add(new Path(patch, d, links.get(d).getCost(), (LineString)links.get(d).getGeometry(), paths.get(0).getAttributeNames()));      
            else
                paths.add(new Path(patch, d, links.get(d).getCost(), links.get(d).getDist(), paths.get(0).getAttributeNames()));      
        
    }
    
    public HashMap<DefaultFeature, Path> calcNewLinks(DefaultFeature patch) throws Exception {
        if(type == PLANAR)
            throw new IllegalStateException("Planar topology is not supported !");
        SpacePathFinder pathfinder = Project.getProject().getPathFinder(this);
        HashMap<DefaultFeature, Path> newPaths = pathfinder.calcPaths(patch.getGeometry().getCentroid().getCoordinate(), distMax, realPaths);
        newPaths.remove(patch);
        return newPaths;
    }
    /**
     * Remove last links created for the new patch
     * Links must have been created by addLinks
     * @param patch 
     */
    public void removeLinks(DefaultFeature patch) {
        while(paths.get(paths.size()-1).getPatch1().equals(patch))
            paths.remove(paths.size()-1);
    }
}
