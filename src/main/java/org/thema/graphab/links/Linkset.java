
package org.thema.graphab.links;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.common.Config;
import org.thema.common.JTS;
import org.thema.common.ProgressBar;
import org.thema.common.collection.HashMap2D;
import org.thema.common.collection.HashMapList;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.ParallelFTask;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.GlobalDataStore;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.parallel.AbstractParallelTask;
import org.thema.parallel.ExecutorService;
import org.thema.parallel.ParallelTask;

/**
 * Represents a set of links (ie. paths) between the patches of the project.
 * The topology can be COMPLETE or PLANAR.
 * The distance can be EUCLID, COST or CIRCUIT
 * 
 * @author Gilles Vuidel
 */
public class Linkset {

    /** Linkset type (ie. topology) complete or planar */
    public static final int COMPLETE = 1;
    /** Linkset type (ie. topology) complete or planar */
    public static final int PLANAR = 2;

    /** Linkset distance : euclidean */
    public static final int EUCLID = 1;
    /** Linkset distance : raster cost */
    public static final int COST = 2;
    /** Linkset distance : raster circuit */
    public static final int CIRCUIT = 4;

    /** Linkset length type for cost distance : cumulated cost */
    public static final int COST_LENGTH = 1;
    /** Linkset length type for cost distance : least cost length */
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
    
    private transient Project project;
    private transient List<Path> paths;
    private transient HashMap<MultiKey, double[]> intraLinks;
    
    /**
     * Creates a linkset with cost distance from landscape map codes.
     * @param project the project where adding this linkset
     * @param name linkset name
     * @param type linkset type (ie. topology) COMPLETE or PLANAR
     * @param costs cost for each landscape code
     * @param type_length length COST_LENGTH or DIST_LENGTH
     * @param realPaths are real paths stored
     * @param removeCrossPatch remove links crossing patch ?
     * @param distMax max cost distance for complete topology only or 0 for no max
     * @param coefSlope coefficient for slope or 0 to avoid slope calculation
     */
    public Linkset(Project project, String name, int type, double[] costs, int type_length, boolean realPaths, 
            boolean removeCrossPatch, double distMax, double coefSlope) {
        this(project, name, type, costs, null, type_length, realPaths, removeCrossPatch, distMax, coefSlope);
    }

    /**
     * Creates a linkset with euclidean distance.
     * @param project the project where adding this linkset
     * @param name linkset name
     * @param type linkset type (ie. topology) COMPLETE or PLANAR
     * @param realPaths are real paths stored
     * @param distMax max cost distance for complete topology only or 0 for no max
     */
    public Linkset(Project project, String name, int type, boolean realPaths, double distMax) {
        this.project = project;
        this.name = name;
        this.type = type;
        this.type_dist = EUCLID;
        this.type_length = DIST_LENGTH;
        this.distMax = distMax;
        this.realPaths = realPaths;
        this.removeCrossPatch = false;
    }


    /**
     * Creates a linkset with cost distance from external map.
     * @param project the project where adding this linkset
     * @param name linkset name
     * @param type linkset type (ie. topology) COMPLETE or PLANAR
     * @param type_length length COST_LENGTH or DIST_LENGTH
     * @param realPaths are real paths stored
     * @param removeCrossPatch remove links crossing patch ?
     * @param distMax max cost distance for complete topology only or 0 for no max
     * @param extCostFile raster file containing costs
     * @param coefSlope coefficient for slope or 0 to avoid slope calculation
     */
    public Linkset(Project project, String name, int type, int type_length, boolean realPaths, 
            boolean removeCrossPatch, double distMax, File extCostFile, double coefSlope) {
        this(project, name, type, null, extCostFile, type_length, realPaths, removeCrossPatch, distMax, coefSlope);
    }
    
    /**
     * Creates a linkset with cost distance from external map or from landscape map codes.
     * @param project the project where adding this linkset
     * @param name linkset name
     * @param type linkset type (ie. topology) COMPLETE or PLANAR
     * @param costs cost for each landscape code or null
     * @param type_length length COST_LENGTH or DIST_LENGTH
     * @param realPaths are real paths stored
     * @param removeCrossPatch remove links crossing patch ?
     * @param distMax max cost distance for complete topology only or 0 for no max
     * @param extCostFile raster file containing costs or null
     * @param coefSlope coefficient for slope or 0 to avoid slope calculation
     */
    public Linkset(Project project, String name, int type, double[] costs, File extCostFile, int type_length, boolean realPaths, 
            boolean removeCrossPatch, double distMax, double coefSlope) {
        if(costs != null && extCostFile != null) {
            throw new IllegalArgumentException();
        }
        this.project = project;
        this.name = name;
        this.type = type;
        this.type_dist = COST;
        this.type_length = type_length;
        if(costs != null) {
            this.costs = Arrays.copyOf(costs, costs.length);
        }
        this.realPaths = realPaths;
        this.removeCrossPatch = removeCrossPatch;
        this.distMax = distMax;
        this.coefSlope = coefSlope;
        if(extCostFile != null) {
            String prjPath = project.getDirectory().getAbsolutePath();
            if(extCostFile.getAbsolutePath().startsWith(prjPath)) {
                this.extCostFile = new File(extCostFile.getAbsolutePath().substring(prjPath.length()+1));
            } else {
                this.extCostFile = extCostFile.getAbsoluteFile();
            }
        }
    }
    
    /**
     * Creates a linkset with circuit distance from external map or from landscape map codes.
     * @param project the project where adding this linkset
     * @param name linkset name
     * @param type linkset type (ie. topology) COMPLETE or PLANAR
     * @param costs cost for each landscape code or null
     * @param extCostFile raster file containing costs or null
     * @param coefSlope coefficient for slope or 0 to avoid slope calculation
     * @param optimCirc optimize raster size for circuit calculation ?
     */
    public Linkset(Project project, String name, int type, double[] costs, File extCostFile, boolean optimCirc, double coefSlope) {
        if(costs != null && extCostFile != null) {
            throw new IllegalArgumentException();
        }
        this.project = project;
        this.name = name;
        this.type = type;
        this.type_dist = CIRCUIT;
        this.type_length = COST_LENGTH;
        this.distMax = Double.NaN;
        if(costs != null) {
            this.costs = Arrays.copyOf(costs, costs.length);
        }
        this.realPaths = false;
        this.removeCrossPatch = false;
        this.optimCirc = optimCirc;
        this.coefSlope = coefSlope;
        
        if(extCostFile != null) {
            String prjPath = project.getDirectory().getAbsolutePath();
            if(extCostFile.getAbsolutePath().startsWith(prjPath)) {
                this.extCostFile = new File(extCostFile.getAbsolutePath().substring(prjPath.length()+1));
            } else {
                this.extCostFile = extCostFile.getAbsoluteFile();
            }
        }
    }

    /**
     * @return the project attached to this linkset
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project attached with this linkset. Used only for project loading.
     * @param project 
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @return the max distance for links or zero
     */
    public double getDistMax() {
        return distMax;
    }

    /**
     * @return the name of the linkset
     */
    public String getName() {
        return name;
    }

    /**
     * @return real paths are stored ?
     */
    public boolean isRealPaths() {
        return realPaths;
    }

    /**
     * @return links crossing patches are removed ?
     */
    public boolean isRemoveCrossPatch() {
        return removeCrossPatch;
    }

    /**
     * @return slope is used for cost calculation ?
     */
    public boolean isUseSlope() {
        return coefSlope != 0;
    }

    /**
     * @return the slope coefficient
     */
    public double getCoefSlope() {
        return coefSlope;
    }

    /**
     * @return the topology : COMPLETE or PLANAR
     */
    public int getTopology() {
        return type;
    }

    /**
     * @return the distance type : EUCLID, COST or CIRCUIT
     */
    public int getType_dist() {
        // compatibilité < 1.3
        if(type_dist == 3) {
            return COST;
        }
        return type_dist;
    }

    /**
     * Useful for cost distance only.
     * @return the type length : COST_LENGTH or DIST_LENGTH
     */
    public int getType_length() {
        return type_length;
    }

    /**
     * @return is external cost map ?
     */
    public boolean isExtCost() {
        return extCostFile != null;
    }

    /**
     * Useful for cost distance only.
     * @return true if the type length is COST_LENGTH
     */    
    public boolean isCostLength() {
        return type_length == COST_LENGTH;
    }

    /**
     * Used only for COST distance without external cost map
     * @return the cost for each code of the landscape map or null
     */
    public double[] getCosts() {
        return costs;
    }

    /**
     * Used only for COST distance with external cost map
     * @return the file containing the costs or null
     */
    public File getExtCostFile() {
        if(extCostFile == null) {
            return null;
        } else if(extCostFile.isAbsolute()) {
            return extCostFile;
        } else {
            return new File(project.getDirectory(), extCostFile.getPath());
        }
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
     * @return the paths of the linkset
     */
    public synchronized List<Path> getPaths() {
        if(paths == null)  {
            try {
                loadPaths(new TaskMonitor.EmptyMonitor());
            } catch (IOException ex) {
                Logger.getLogger(Linkset.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return paths;
    }
    
    /**
     * Returns the cost and the length between 2 coordinates of the border of a patch.
     * The two coordinates must correspond to the endpoint of two paths connected to the same patch.
     * @param c1 the first coordinate
     * @param c2 the second coordinate
     * @return the cost and the length (may be the same for euclidean distance)
     */
    public double[] getIntraLinkCost(Coordinate c1, Coordinate c2) {
        if(c1.compareTo(c2) < 0) {
            return getIntraLinks().get(new MultiKey(c1, c2));
        } else {
            return getIntraLinks().get(new MultiKey(c2, c1));
        }
    }
    
    /**
     * Returns the cost and the length between 2 paths connected to the same patch.
     * @param p1 the first path
     * @param p2 the second path
     * @return the cost and the length (may be the same for euclidean distance)
     */
    public double[] getIntraLinkCost(Path p1, Path p2) {
        Feature patch = Path.getCommonPatch(p1, p2);
        Coordinate c1 = p1.getCoordinate(patch);
        Coordinate c2 = p2.getCoordinate(patch);
        return getIntraLinkCost(c1, c2);
    }
    
    private synchronized HashMap<MultiKey, double[]> getIntraLinks() {
        if(!isRealPaths()) {
            throw new IllegalStateException("Intra patch links need real paths");
        }
        if(intraLinks == null) {
            try {
                loadIntraLinks();
            } catch (IOException ex) {
                Logger.getLogger(Linkset.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return intraLinks;
    }
    
    /**
     * Estimates the cost from a distance. 
     * Do a linear regression in double log  between cost and length of all links.
     * @param distance
     * @return the cost corresponding to the distance
     */
    public double estimCost(double distance) {
        if(type_dist == EUCLID) {
            return distance;
        }
        XYSeries s =  new XYSeries("regr");
        for(Feature f : getPaths()) {
            s.add(Math.log(((Number)f.getAttribute(Path.DIST_ATTR)).doubleValue()), Math.log(((Number)f.getAttribute(Path.COST_ATTR)).doubleValue()));
        }
        XYSeriesCollection dataregr = new XYSeriesCollection(s);

        double [] coef = Regression.getOLSRegression(dataregr, 0);
        return Math.exp(Math.log(distance) * coef[1] + coef[0]);
    }

    /**
     * Returns detailed informations of the linkset.<br/>
     * The language is local dependent
     * @return 
     */
    public String getInfo() {
        ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/links/Bundle");
        
        String info = bundle.getString("LinksetPanel.nameLabel.text") + " : " + name;
        info += "\n" + bundle.getString("LinksetPanel.topoPanel.border.title") + " : ";
        if(type == COMPLETE) {
            info += bundle.getString("LinksetPanel.completeRadioButton.text");
            if(distMax > 0) {
                info += " " + bundle.getString("LinksetPanel.distMaxLabel.text") + " " + distMax;
            }
        }
        else {
            info += bundle.getString("LinksetPanel.planarRadioButton.text");
        }
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
                    for(Integer code : project.getCodes()) {
                        info += code + " : " + costs[code] + "\n";
                    }
                }       
                if(isUseSlope()) {
                    info += "Use slope : " + coefSlope + "\n";
                }
                break;
        }
        
        if(getType_dist() == COST) {
            info += "\n" + bundle.getString("LinksetPanel.impedancePanel.border.title") + " : ";
            if(type_length == COST_LENGTH) {
                info += bundle.getString("LinksetPanel.costDistRadioButton.text");
            } else {
                info += bundle.getString("LinksetPanel.lengthRadioButton.text");
            }
        }

        if(realPaths) {
            info += "\n" + bundle.getString("LinksetPanel.realPathCheckBox.text");
        }

        if(getType_dist() == COST && removeCrossPatch) {
            info += "\n" + bundle.getString("LinksetPanel.removeCrossPatchCheckBox.text");
        }

        info += "\n\n# links : " + paths.size();

        return info;
    }
    
    /**
     * @return the name of the linkset
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Return a new virtual linkset for circuit calculation based on cost linkset.
     * Return this if this linkset is already a circuit linkset
     * @return a new linkset or this
     * @throws IllegalArgumentException if the linkset is euclidean
     */
    public Linkset getCircuitVersion() {
        if(getType_dist() == Linkset.CIRCUIT) {
            return this;
        } else if(getType_dist() == Linkset.EUCLID) {
            throw new IllegalArgumentException("No circuit from euclidean linkset");
        }
        
        return new Linkset(project, name+"_circ", type, costs, extCostFile, true, coefSlope);
    }
    
    /**
     * Return a new virtual linkset for cost calculation based on circuit linkset.
     * Return this if this linkset is already a cost linkset
     * @return a new linkset or this
     * @throws IllegalArgumentException if the linkset is euclidean
     */
    public Linkset getCostVersion() {
        if(getType_dist() == Linkset.COST) {
            return this;
        } else if(getType_dist() == Linkset.EUCLID) {
            throw new IllegalArgumentException("No cost from euclidean linkset");
        }
        
        return new Linkset(project, name+"_cost", type, costs, extCostFile, COST_LENGTH, true, false, Double.NaN, coefSlope);
    }
    
    /**
     * Compute all links defined in this linkset.<br/>
     * This method is called only once by the project
     * @param progressBar
     * @throws IOException 
     */
    public void compute(ProgressBar progressBar) throws IOException {
        progressBar.setNote("Create linkset " + getName());
        
        if(getType_dist() == Linkset.EUCLID) {
            calcEuclidLinkset(progressBar);
        } else if(getType_dist() == Linkset.CIRCUIT) {
            calcCircuitLinkset(progressBar);
        } else {
            calcCostLinkset(progressBar);
        }
        progressBar.reset();
        progressBar.setNote("Create intra links...");
        if(isRealPaths()) {
            calcIntraLinks(progressBar);
        }
    }
    
    /**
     * Compute and return corridors of all paths existing in this linkset
     * @param progressBar
     * @param maxCost maximal cost distance 
     * @return list of features where id is equal to id path and geometry is a polygon or multipolygon
     */
    public List<Feature> computeCorridor(ProgressBar progressBar, final double maxCost) {
        if(getType_dist() == Linkset.EUCLID) {
            throw new IllegalArgumentException("Euclidean linkset is not supported for corridor");
        }
        final List<Feature> corridors = Collections.synchronizedList(new ArrayList<Feature>(getPaths().size()));
        
        ParallelFTask task = new SimpleParallelTask<Path>(getPaths(), progressBar) {
            @Override
            protected void executeOne(Path path) {
                try {
                    Geometry corridor;
                    if(getType_dist() == Linkset.COST) {
                        corridor = calcCostCorridor(path, maxCost);
                    } else {
                        corridor = calcCircuitCorridor(path, maxCost);
                    }
                    if(!corridor.isEmpty()) {
                        corridors.add(new DefaultFeature(path.getId(), corridor));
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        
        return corridors;
    }
    
    private Geometry calcCircuitCorridor(Path path, double maxCost) throws IOException {
        CircuitRaster circuit = project.getRasterCircuit(this);
        CircuitRaster.PatchODCircuit odCircuit = circuit.getODCircuit(path.getPatch1(), path.getPatch2());
        return odCircuit.getCorridor(maxCost);
    }

    private Geometry calcCostCorridor(Path path, double maxCost) throws IOException {
        if(path.getCost() > maxCost) {
            return new GeometryFactory().buildGeometry(Collections.EMPTY_LIST);
        }
        RasterPathFinder pathfinder = project.getRasterPathFinder(this);
        Raster r1 = pathfinder.getDistRaster(path.getPatch1(), maxCost);
        Raster r2 = pathfinder.getDistRaster(path.getPatch2(), maxCost);
        final Rectangle rect = r1.getBounds().intersection(r2.getBounds());

        final int id1 = (Integer)path.getPatch1().getId();
        final int id2 = (Integer)path.getPatch2().getId();
        WritableRaster corridor = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, rect.width, rect.height, 1, new Point(rect.x, rect.y));
        for(int y = rect.y; y < rect.getMaxY(); y++) {
            for(int x = rect.x; x < rect.getMaxX(); x++) {
                int id = project.getRasterPatch().getSample(x, y, 0);
                if(id != id1 && id != id2 &&
                        r1.getSampleDouble(x, y, 0)+r2.getSampleDouble(x, y, 0) <= maxCost) {
                    corridor.setSample(x, y, 0, 1);
                }
            }
        }
        Geometry geom =  Project.vectorize(corridor, JTS.rectToEnv(rect), 1);
        return project.getGrid2space().transform(geom);
    }
    
    private void calcCostLinkset(ProgressBar progressBar) {
        final boolean allLinks = getTopology() == Linkset.COMPLETE;
        
        Path.newSetOfPaths();
        long start = System.currentTimeMillis();

        ParallelTask<List<Path>, List<Path>> task = new AbstractParallelTask<List<Path>, List<Path>>(progressBar) {
            private List<Path> result = new ArrayList<>();
            @Override
            public List<Path> execute(int start, int end) {
                List<Path> links = new ArrayList<>();
                try {
                    RasterPathFinder pathfinder = project.getRasterPathFinder(Linkset.this);
                    for(Feature orig : project.getPatches().subList(start, end)) {
                        if(isCanceled()) {
                            throw new CancellationException();
                        }
                        HashMap<Feature, Path> paths;
                        if(allLinks) {
                            paths = pathfinder.calcPaths(orig, getDistMax(), isRealPaths(), false);
                        } else {
                            List<Feature> dests = new ArrayList<>();
                            for(Integer dId : project.getPlanarLinks().getNeighbors(orig)) {
                                if(((Integer)orig.getId()) < dId) {
                                    dests.add(project.getPatch(dId));
                                }
                            }
                            if(dests.isEmpty()) {
                                continue;
                            }
                            paths = pathfinder.calcPaths(orig, dests);
                        }
                        
                        for(Feature d : paths.keySet()) {
                            Path p = paths.get(d);
                            boolean add = true;
                            if(isRemoveCrossPatch() && isRealPaths()) {
                                List lst = project.getPatchIndex().query(p.getGeometry().getEnvelopeInternal());
                                for(Object o : lst) {
                                    Feature f = (Feature) o;
                                    if(f != orig && f != d && f.getGeometry().intersects(p.getGeometry())) {
                                        add = false;
                                        break;
                                    }
                                }
                            }
                            if(add) {
                                links.add(p);
                            }
                        }  
                        incProgress(1);
                    }
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
                return links;
            }
            @Override
            public int getSplitRange() {
                return project.getPatches().size();
            }

            @Override
            public List<Path> getResult() {
                return result;
            }
            @Override
            public void gather(List<Path> results) {
                result.addAll(results);
            }
        };

        ExecutorService.execute(task);

        if(task.isCanceled()) {
            throw new CancellationException();
        }
        
        Logger.getLogger(Linkset.class.getName()).info("Temps écoulé : " + (System.currentTimeMillis()-start));
        
        paths = task.getResult();
    }

    private void calcEuclidLinkset(ProgressBar progressBar) {
        final boolean allLinks = getTopology() == Linkset.COMPLETE;
        
        Path.newSetOfPaths();
        
        final STRtree index = project.getPatchIndex();
        
        long start = System.currentTimeMillis();
        ParallelTask<List<Path>, List<Path>> task = new AbstractParallelTask<List<Path>, List<Path>>(progressBar) {
            private List<Path> result = new ArrayList<>();
            @Override
            public List<Path> execute(int start, int end) {  
                List<Path> links = new ArrayList<>();
                for(Feature orig : project.getPatches().subList(start, end)) {
                    if(isCanceled()) {
                        throw new CancellationException();
                    }
                    if(allLinks) {
                        List<DefaultFeature> nearPatches = project.getPatches();
                        if(getDistMax() > 0) {
                            Envelope env = orig.getGeometry().getEnvelopeInternal();
                            env.expandBy(getDistMax());
                            nearPatches = (List<DefaultFeature>)index.query(env);
                        }
                        
                        for(Feature dest : nearPatches) {
                            if (((Integer)orig.getId()) < (Integer)dest.getId()) {
                                Path p = Path.createEuclidPath(orig, dest);
                                if (getDistMax() == 0 || p.getDist() <= getDistMax()) {
                                    links.add(p);
                                }
                            }
                        }
                    } else {
                        for (Integer dId : project.getPlanarLinks().getNeighbors(orig)) {
                            Feature d = project.getPatch(dId);
                            if (((Integer)orig.getId()) < dId) {
                                links.add(Path.createEuclidPath(orig, d));
                            }
                        }
                    }

                    incProgress(1);
                }
                return links;
            }

            @Override
            public int getSplitRange() {
                return project.getPatches().size();
            }

            @Override
            public List<Path> getResult() {
                return result;
            }

            @Override
            public void gather(List<Path> results) {
                result.addAll(results);
            }
        };

        ExecutorService.execute(task);

        if(task.isCanceled()) {
            throw new CancellationException();
        }
        Logger.getLogger(Linkset.class.getName()).info("Temps écoulé : " + (System.currentTimeMillis()-start));
        paths = task.getResult();
    }
       
    private void calcCircuitLinkset(ProgressBar progressBar) throws IOException {
        final boolean allLinks = getTopology() == Linkset.COMPLETE;
        final List<Path> links = Collections.synchronizedList(new ArrayList<Path>(project.getPatches().size() * 4));
        Path.newSetOfPaths();
        long start = System.currentTimeMillis();
        final CircuitRaster circuit = project.getRasterCircuit(this);
        final FileWriter w = new FileWriter(new File(project.getDirectory(), getName() + "-stats.csv"));
        w.write("Id1,Id2,Area1,Area2,W,H,T,Iter,InitSErr,R,MErr,M2Err,SErr\n");
        ParallelFTask task = new AbstractParallelFTask(progressBar) {
            @Override
            protected Object execute(int start, int end) {
                for(Feature orig : project.getPatches().subList(start, end)) {
                    if(isCanceled()) {
                        throw new CancellationException();
                    }
                    if(allLinks) {
                        for(Feature patch : project.getPatches()) {
                            if((Integer)orig.getId() < (Integer)patch.getId()) {
                                double r = circuit.getODCircuit(orig, patch).getR();
                                links.add(new Path(orig, patch, r, Double.NaN));
                            }
                        }
                    } else {
                        for(Integer dId : project.getPlanarLinks().getNeighbors(orig)) {
                            if(((Integer)orig.getId()) < dId) {
                                DefaultFeature dest = project.getPatch(dId);
                                long t1 = System.currentTimeMillis();
                                CircuitRaster.PatchODCircuit odCircuit = circuit.getODCircuit(orig, dest);
                                odCircuit.solve();
                                long t2 = System.currentTimeMillis();
                                double r = odCircuit.getR();
                                synchronized (Linkset.this) {
                                    try {
                                        w.write(orig.getId() + "," + dest.getId() + "," + Project.getPatchArea(orig) + "," + Project.getPatchArea(dest) + "," +
                                                odCircuit.getZone().getWidth() + "," + odCircuit.getZone().getHeight() + "," + (t2 - t1) / 1000.0 + "," + odCircuit.getNbIter() + "," +
                                                odCircuit.getInitErrSum() + "," + r + "," + odCircuit.getErrMax() + "," + odCircuit.getErrMaxWithoutFirst() + "," + odCircuit.getErrSum() + "\n");
                                        w.flush();
                                    } catch (IOException ex) {
                                        Logger.getLogger(Linkset.class.getName()).log(Level.WARNING, null, ex);
                                    }
                                }
                                links.add(new Path(orig, project.getPatch(dId), r, Double.NaN));
                            }
                        }
                    }
                    
                    incProgress(1);
                }
                    
                return null;
            }
            @Override
            public int getSplitRange() {
                return project.getPatches().size();
            }
            @Override
            public void finish(Collection results) {
            }
            @Override
            public Object getResult() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };

        new ParallelFExecutor(task).executeAndWait();
        w.close();
        if(task.isCanceled()) {
            throw new CancellationException();
        }
        
        Logger.getLogger(Linkset.class.getName()).info("Temps écoulé : " + (System.currentTimeMillis()-start));
        
        paths = links;
    }

    private void calcIntraLinks(ProgressBar progressBar) {
        final HashMapList<Feature, Path> mapLinks = new HashMapList<>();
        for(Path p : paths) {
            mapLinks.putValue(p.getPatch1(), p);
            mapLinks.putValue(p.getPatch2(), p);
        }
        
        final HashMap<MultiKey, double[]> mapIntraLinks = new HashMap<>();
        SimpleParallelTask<Feature> task = new SimpleParallelTask<Feature>(new ArrayList<>(mapLinks.keySet()), progressBar) {
            @Override
            protected void executeOne(Feature patch) {
                SpacePathFinder pathFinder;
                try {
                    pathFinder = project.getPathFinder(Linkset.this);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                List<Path> links = mapLinks.get(patch);
                HashSet<Coordinate> pointSet = new HashSet<>();
                for(Path link : links) {
                    pointSet.add(link.getCoordinate(patch));
                }
                
                List<Coordinate> pointList = new ArrayList<>(pointSet);
                for(int i = 0; i < pointList.size()-1; i++) { 
                    Coordinate c1 = pointList.get(i);

                    List<Coordinate> dests = pointList.subList(i+1, pointList.size());
                    List<double[]> values = pathFinder.calcPaths(c1, dests);
                    for(int k = 0; k < values.size(); k++) {
                        synchronized(mapIntraLinks) {
                            if(c1.compareTo(dests.get(k)) < 0) {
                                mapIntraLinks.put(new MultiKey(c1, dests.get(k)), values.get(k));
                            } else {
                                mapIntraLinks.put(new MultiKey(dests.get(k), c1), values.get(k));
                            }
                        }
                    }
                }
                
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        if(task.isCanceled())  {
            throw new CancellationException();
        }
        intraLinks = mapIntraLinks;
    }

    /**
     * Loads the links from the shapefile and/or csv file
     * @param mon
     * @throws IOException 
     */
    public void loadPaths(ProgressBar mon) throws IOException {
        File fCSV = new File(project.getDirectory(), name + "-links.csv");
        HashMap<Object, Path> map = new HashMap<>();
        try (CSVReader r = new CSVReader(new FileReader(fCSV))) {
            String [] attrNames = r.readNext();
            if(attrNames != null) {
                Path.newSetOfPaths(Arrays.asList(attrNames).subList(4, attrNames.length));
            }
            String [] tab;
            while((tab = r.readNext()) != null) {
                Path p = Path.deserialPath(tab, project);
                map.put(p.getId(), p);
            }
        }

        if(realPaths && !map.isEmpty()) {
            List<DefaultFeature> features = GlobalDataStore.getFeatures(
                    new File(project.getDirectory(), name + "-links.shp"), "Id", mon);

            for(DefaultFeature f : features) {
                map.get(f.getId()).setGeometry(f.getGeometry());
            }
        }

        paths = new ArrayList<>(map.values());
    }
    
    /**
     * Saves the links into a csv file in the project directory
     * @throws IOException
     */
    public void saveLinks() throws IOException {
        try (CSVWriter w = new CSVWriter(new FileWriter(new File(project.getDirectory(), name + "-links.csv")))) {
            if(!getPaths().isEmpty()) {
                w.writeNext(getPaths().get(0).getAttributeNames().toArray(new String[0]));
            }
            for(Path p : getPaths()) {
                w.writeNext(Path.serialPath(p));
            }
        }
    }
    
    private void loadIntraLinks() throws IOException {
        File fCSV = new File(project.getDirectory(), name + "-links-intra.csv");
        // for project compatibility
        if(!fCSV.exists()) {
            calcIntraLinks(Config.getProgressBar("Compute intra links"));
            saveIntraLinks();
        }
        try (CSVReader r = new CSVReader(new FileReader(fCSV))) {
            r.readNext();
            intraLinks = new HashMap<>();
            String [] tab;
            while((tab = r.readNext()) != null) {
                String[] ordinates = tab[0].split("-");
                Coordinate c0 = new Coordinate(Double.parseDouble(ordinates[0]), Double.parseDouble(ordinates[1]));
                ordinates = tab[1].split("-");
                Coordinate c1 = new Coordinate(Double.parseDouble(ordinates[0]), Double.parseDouble(ordinates[1]));
                intraLinks.put(new MultiKey(c0, c1), new double[]{Double.parseDouble(tab[2]), Double.parseDouble(tab[3])});
            }
        }
    }

    /**
     * Saves intra links. Called only by the project at linkset creation
     * @throws IOException 
     */
    public void saveIntraLinks() throws IOException {
        File fCSV = new File(project.getDirectory(), name + "-links-intra.csv");
        try (CSVWriter w = new CSVWriter(new FileWriter(fCSV))) {
            w.writeNext(new String[]{"Coord1", "Coord2", "Cost", "Length"});
            
            for(MultiKey key : intraLinks.keySet()) {
                double [] val = intraLinks.get(key);
                Coordinate c0 = (Coordinate) key.getKey(0);
                Coordinate c1 = (Coordinate) key.getKey(1);
                w.writeNext(new String[]{c0.x + "-" + c0.y, c1.x + "-" + c1.y, ""+val[0], ""+val[1]});
            }
        }
    }
    
    /**
     * Calculates and add links for the new patch
     * The patch have to be added in the project before (Project.addPatch)
     * @param patch must be a point geometry
     * @throws IOException 
     */
    public void addLinks(DefaultFeature patch) throws IOException {
        HashMap<DefaultFeature, Path> links = calcNewLinks(patch);
        for(DefaultFeature d : links.keySet()) {
            if(realPaths) {
                paths.add(new Path(patch, d, links.get(d).getCost(), (LineString)links.get(d).getGeometry(), paths.get(0).getAttributeNames()));      
            } else {
                paths.add(new Path(patch, d, links.get(d).getCost(), links.get(d).getDist(), paths.get(0).getAttributeNames()));      
            }
        }
    }
    
    /**
     * Calculates links for the new patch.
     * Does not support planar topology.
     * @param patch must be a point geometry
     * @return the new links
     * @throws IOException 
     * @throws IllegalStateException if the topology is PLANAR
     */
    public HashMap<DefaultFeature, Path> calcNewLinks(DefaultFeature patch) throws IOException {
        if(type == PLANAR) {
            throw new IllegalStateException("Planar topology is not supported !");
        }
        SpacePathFinder pathfinder = project.getPathFinder(this);
        HashMap<DefaultFeature, Path> newPaths = pathfinder.calcPaths(patch.getGeometry(), distMax, realPaths);
        newPaths.remove(patch); 
        return newPaths;
    }
    
    /**
     * Remove last links created for the new patch.
     * The links must have been created by addLinks
     * @param patch 
     */
    public void removeLinks(DefaultFeature patch) {
        while(paths.get(paths.size()-1).getPatch1().equals(patch)) {
            paths.remove(paths.size()-1);
        }
    }
    
    /**
     * Extract for each path the number of pixels for each cost
     * @return a 2D mapping (Path, cost) -> #pixels
     * @throws IOException 
     */
    public HashMap2D<Path, Double, Integer> extractCostFromPath() throws IOException {
        if(!isRealPaths()) {
            throw new IllegalStateException("Linkset must have real path.");
        }
        if(getType_dist() != COST) {
            throw new IllegalStateException("Linkset must have cost from landscape.");
        }
        AffineTransformation trans = project.getSpace2grid();
        WritableRaster land = project.getImageSource();
        
        Set<Double> costSet = new TreeSet<>();
        for(double c : costs) {
            costSet.add(c);
        }
        HashMap2D<Path, Double, Integer> map = new HashMap2D<>(getPaths(), costSet, 0);
        
        for(Path p : getPaths()) {                
            Set<Coordinate> pixels = new HashSet<>();
            Geometry g = trans.transform(p.getGeometry());
            LengthIndexedLine index = new LengthIndexedLine(g);
            for(int l = 0; l <= g.getLength(); l++) {
                Coordinate c = index.extractPoint(l);
                pixels.add(new Coordinate((int)c.x, (int)c.y));
            }
            // end point
            Coordinate c = index.extractPoint(index.getEndIndex());
            pixels.add(new Coordinate((int)c.x, (int)c.y));
            
            for(Coordinate pixel : pixels) {
                double cost = costs[land.getSample((int)pixel.x, (int)pixel.y, 0)];
                map.setValue(p, cost, map.getValue(p, cost) + 1);
            }
        }
        
        return map;
    }
}
