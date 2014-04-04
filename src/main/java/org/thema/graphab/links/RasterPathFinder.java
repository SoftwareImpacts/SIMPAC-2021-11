/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.awt.image.BandedSampleModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.*;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;

/**
 * This class is not thread safe.<br/>
 * Create one instance for each thread
 * 
 * @author gvuidel
 */


public final class RasterPathFinder implements SpacePathFinder {

    private Raster rasterPatch;
    private Raster costRaster;
    private double [] cost;

    private Project project;

    private final int CON = 8;
    private final int [] X = new int[] {-1, 0, 0, +1, -1, -1, +1, +1};
    private final int [] Y = new int[] {0, -1, +1, 0, -1, +1, -1, +1};
    private final double [] COST = new double[] {1.0, 1.0, 1.0, 1.0, 1.4142136, 1.4142136, 1.4142136, 1.4142136};
    private final int [] IND, IND_ANTE;

    private PriorityQueue<Node> queue;

    private int xd, yd, wd, hd;
    private double [] dist;
    private byte [] ante;


    /**
     * Create pathfinder from landscape map 
     * @param prj
     * @param codeRaster
     * @param cost
     * @throws IOException 
     */
    public RasterPathFinder(Project prj, Raster codeRaster, double [] cost) throws IOException {
        this.project = prj;
        this.rasterPatch = project.getRasterPatch();
        this.costRaster = codeRaster;
        this.cost = cost;
        int w = rasterPatch.getWidth();
        IND = new int[] {-1, -w, +w, +1, -w-1, +w-1, -w+1, +w+1};
        IND_ANTE = new int[] {+1, +w, -w, -1, w+1, -w+1, +w-1, -w-1};
    }

    /**
     * Create pathfinder from external cost raster
     * @param prj
     * @param costRaster
     * @throws IOException 
     */
    public RasterPathFinder(Project prj, Raster costRaster) throws IOException {
        this(prj, costRaster, null);
    }

    /**
     * Initialize pathfinder from origin coord
     * @param coord 
     */
    private void initCoord(Coordinate coord) {
        Coordinate cp = project.getSpace2grid().transform(coord, new Coordinate());
        initCoord((int)cp.x, (int)cp.y);
    }
    
    /**
     * Initialize pathfinder from origin (rx, ry) in raster coordinate
     * @param coord 
     */
    private void initCoord(int rx, int ry) {
        queue = new PriorityQueue<Node>();

        initDistBuf(rx-100, ry-100, 200, 200);
        final int w = rasterPatch.getWidth();
        // starting node
        Node node = new Node(ry*w+rx, 0);
        queue.add(node);
        setDist(ry*w+rx, 0);
    }

    /**
     * Initialize pathfinder from origin oPatch.<br/>
     * All pixel at the border of oPatch are initialize as origin
     * @param oPatch 
     */
    private void initPatch(Feature oPatch) {

        final int w = rasterPatch.getWidth();
        final int [] tab = new int[9];

        final int id = (Integer)oPatch.getId();

        Envelope env = oPatch.getGeometry().getEnvelopeInternal();
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(project.getSpace2grid());
        env = gEnv.getEnvelopeInternal();

        //  ajout dans la queue des pixels de bord
        queue = new PriorityQueue<Node>();
        for(int i = (int)env.getMinY(); i <= env.getMaxY(); i++)
            for(int j = (int)env.getMinX(); j <= env.getMaxX(); j++)
                if(rasterPatch.getSample(j, i, 0) == id) {
                    rasterPatch.getPixels(j-1, i-1, 3, 3, tab);
                    boolean border = false;
                    for(int k = 0; !border && k < 9; k++)
                        if(tab[k] != id)
                            border = true;
                    if(border)
                        queue.add(new Node(i*w+j, 0));

                }

        initDistBuf((int)env.getMinX()-100, (int)env.getMinY()-100, (int)env.getWidth()+200, (int)env.getHeight()+200);

        // initialisation des distances à zéro pour le contour la tache origine
        for(Node n : queue)
            setDist(n.ind, 0);
    }
    
    /**
     * Calcule les distances cout à partir du point p vers tous les 
     * destinations dests
     * @param p start point
     * @param dests destination points
     * @return les couts et longueurs des chemins de p vers les destinations
     */
    @Override
    public List<double[]> calcPaths(Coordinate p, List<Coordinate> dests) {
        initCoord(p);
        final int w = rasterPatch.getWidth();
        HashSet<Integer> indDests = new HashSet<Integer>();
        for(Coordinate dest : dests) {
            Coordinate cp = project.getSpace2grid().transform(dest, new Coordinate());
            int rx = (int)cp.x;
            int ry = (int)cp.y;
            indDests.add(ry*w+rx);
        }

        while(!queue.isEmpty() && !indDests.isEmpty()) {
            Node n = updateNextNodes(queue);
            indDests.remove(n.ind);
        }

        List<double[]> distances = new ArrayList<double[]>(dests.size());
        for(Coordinate dest : dests) {
            Coordinate cp = project.getSpace2grid().transform(dest, new Coordinate());
            int rx = (int)cp.x;
            int ry = (int)cp.y;
            distances.add(new double[]{(double)getDist(ry*w+rx), getPath(ry*w+rx).getLength()});
        }
        return distances;
    }
    
    /**
     * Calcule les distances cout à partir du point p vers tous les patch dont
     * la distance cout est inférieure ou égale à maxCost
     * @param p
     * @param maxCost
     * @param realPath
     * @return les patch avec le cout et la longueur du chemin
     */
    @Override
    public HashMap<DefaultFeature, Path> calcPaths(Coordinate p, double maxCost, boolean realPath) {

        initCoord(p);

        DefaultFeature pointPatch = new DefaultFeature(p.toString(), new GeometryFactory().createPoint(p));
        
        HashMap<DefaultFeature, Path> paths = new HashMap<DefaultFeature, Path>();
        while(!queue.isEmpty()) {
            Node current = updateNextNodes(queue);
            if(maxCost > 0 && current.dist > maxCost)
                break;
            int curId = rasterPatch.getSample(getX(current.ind), getY(current.ind), 0);
            if(curId > 0) {
                DefaultFeature dest = project.getPatch(curId);
                if(!paths.keySet().contains(dest)) {
                    LineString line = getPath(current.ind);
                    if(realPath)
                        paths.put(dest, new Path(pointPatch, dest, current.dist, line));
                    else
                        paths.put(dest, new Path(pointPatch, dest, current.dist, line.getLength()));
                }
            }
        }

        return paths;

    }

    /**
     * Calcule les chemins à partir de oPatch vers tous les patch dont l'id est supérieur à oPatch
     * si all == false et dont le cout est inférieur ou égal à maxCost
     * @param oPatch
     * @param maxCost
     * @param realPath
     * @param all
     * @return
     */
    @Override
    public HashMap<Feature, Path> calcPaths(Feature oPatch, double maxCost, boolean realPath, boolean all) {

        initPatch(oPatch);
        
        final int id = (Integer)oPatch.getId();

        HashMap<Feature, Path> distances = new HashMap<Feature, Path>();
        while(!queue.isEmpty()) {
            Node current = updateNextNodes(queue);
            if(maxCost > 0 && current.dist > maxCost)
                break;
            int curId = rasterPatch.getSample(getX(current.ind), getY(current.ind), 0);
            if(curId > 0 && curId != id && (all || curId > id)) {
                Feature dest = project.getPatch(curId);
                if(distances.keySet().contains(dest))
                    continue;

                LineString line = getPath(current.ind);
                distances.put(dest, realPath ? new Path(dest, oPatch, current.dist, line) :
                    new Path(dest, oPatch, current.dist, line.getLength()));
            }
        }

        return distances;
    }

    @Override
    public HashMap<Feature, Path> calcPaths(Feature oPatch, Collection<Feature> dPatch) {

        initPatch(oPatch);
        
        HashMap<Integer, Feature> destId = new HashMap<Integer, Feature>();
        for(Feature f : dPatch)
            destId.put((Integer)f.getId(), f);

        HashMap<Feature, Path> distances = new HashMap<Feature, Path>();
        while(!queue.isEmpty() && !destId.isEmpty()) {
            Node current = updateNextNodes(queue);

            int curId = rasterPatch.getSample(getX(current.ind), getY(current.ind), 0);
            if(curId > 0 && destId.keySet().contains(curId)) {
                Feature dest = destId.remove(curId);
                LineString line = getPath(current.ind);
                distances.put(dest, new Path(dest, oPatch, current.dist, line));
            }
        }

        if(!destId.isEmpty())
            throw new RuntimeException("Impossible de calculer le chemin de " + oPatch.getId() + " à " + Arrays.deepToString(destId.keySet().toArray()));

        return distances;
    }
    
    /**
     * Calc nearest patch from point p
     * @param p
     * @return an array with id of nearest patch, cost and dist
     */
    @Override
    public double [] calcPathNearestPatch(Point p) {
        Coordinate cp = project.getSpace2grid().transform(p.getCoordinate(), new Coordinate());
        return calcPathNearestPatch((int)cp.x, (int)cp.y);
    }

    /**
     * Calc nearest patch from raster coordinate
     * @param rx
     * @param ry
     * @return an array with id of nearest patch, cost and dist
     */
    public double[] calcPathNearestPatch(int rx, int ry) {

        initCoord(rx, ry);

        while(!queue.isEmpty()) {
            Node current = updateNextNodes(queue);

            int curId = rasterPatch.getSample(getX(current.ind), getY(current.ind), 0);
            if(curId > 0) {
                double len = getPath(current.ind).getLength();
                return new double[] {curId, current.dist, len};
            }
            
        }

        throw new RuntimeException("Impossible de calculer le chemin !");

    }

    /**
     * Calcule la surface en pixel autour du patch oPatch jusqu'à une distance maxCost
     * pour les codes du raster contenus dans codes
     * @param oPatch origin patch
     * @param maxCost max cost distance
     * @param codes set of codes included in sum
     * @param costWeighted if true sum is weighted by cost
     * @return
     */
    public double getNeighborhood(Feature oPatch, double maxCost, Raster rasterCode, HashSet<Integer> codes, boolean costWeighted) {

        initPatch(oPatch);

        double neighborhood = 0;
        double alpha = -Math.log(0.05) / maxCost;
        HashSet<Integer> counts = new HashSet<Integer>();

        while(!queue.isEmpty()) {
            Node current = updateNextNodes(queue);
            if(current.dist > maxCost)
                break;
            int code = rasterCode.getSample(getX(current.ind), getY(current.ind), 0);
            if(current.dist > 0 && codes.contains(code) && !counts.contains(current.ind)) {
                neighborhood += costWeighted ? Math.exp(-alpha*current.dist) : 1;
                counts.add(current.ind);
            }
        }

        return neighborhood;
    }
    
    /**
     * Return a raster of cost distances from oPatch.<br/>
     * The raster may be smaller than landscape raster if maxCost > 0s
     * @param oPatch patch origin
     * @param maxCost max cost distance, if 0 : no max
     * @return 
     */
    public Raster getDistRaster(Feature oPatch, double maxCost) {
        
        initPatch(oPatch);

        while(!queue.isEmpty()) {
            Node current = updateNextNodes(queue);
            if(maxCost > 0 && current.dist > maxCost)
                break;
        }
        
        return Raster.createRaster(new BandedSampleModel(DataBuffer.TYPE_DOUBLE, wd, hd, 1), 
                new DataBufferDouble(dist, dist.length), new java.awt.Point(xd, yd));
    }

    private LineString getPath(int ind) {
        int w = rasterPatch.getWidth();
        ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
        coords.add(new Coordinate(ind % w + 0.5, ind / w + 0.5));
        int curInd = ind;
        int idAnte = getAnte(ind);
        while(idAnte != -1) {
            curInd += IND_ANTE[idAnte];
            Coordinate coord = new Coordinate(curInd % w + 0.5, curInd / w + 0.5);
            coords.add(coord);
            idAnte = getAnte(curInd);
        }
        // simplify linestring
        ArrayList<Coordinate> simpCoords = new ArrayList<Coordinate>();
        for(int i = 1; i < coords.size()-1; i++) {
            Coordinate precCoord = coords.get(i-1);
            Coordinate coord = coords.get(i);
            Coordinate nextCoord = coords.get(i+1);
            double a1 = (coord.x - precCoord.x) / (coord.y - precCoord.y);
            double a2 = (nextCoord.x - coord.x) / (nextCoord.y - coord.y);
            if(a1 != a2)
                simpCoords.add(coord);
        }
        simpCoords.add(0, coords.get(0));
        simpCoords.add(coords.get(coords.size()-1));
        LineString line = new GeometryFactory().createLineString(simpCoords.toArray(new Coordinate[simpCoords.size()]));
        line.apply(project.getGrid2space());
        return line;
    }

    private Node updateNextNodes(PriorityQueue<Node> queue) {
        Node current = queue.poll();
        while(!queue.isEmpty() && current.dist > getDist(current.ind))
            current = queue.poll();
        
        final int x = current.ind % rasterPatch.getWidth();
        final int y = current.ind / rasterPatch.getWidth();
        final double currentCost = getCost(x, y);
            
        for(int i = 0; i < CON; i++)
            if(isInside(x + X[i], y + Y[i])) {
                final double c = getCost(x+X[i], y+Y[i]);
                final double newCost = current.dist + COST[i] * (currentCost + c) / 2;
                final int ind = current.ind + IND[i];
                if(newCost < getDist(ind)) {
                    Node newNode = new Node(ind, newCost);
                    setDist(ind, newCost);
                    setAnte(ind, (byte)i);
                    queue.add(newNode);
                }
            }
        
        return current;
    }

    private boolean isInside(int x, int y) {
        return rasterPatch.getSample(x, y, 0) > -1;// && (zone == null || zone.contains(x, y));
    }

    private double getCost(int x, int y) {
        return cost == null ? costRaster.getSampleDouble(x, y, 0) : cost[costRaster.getSample(x, y, 0)];
    }

    private int getX(int ind) {
        return ind % rasterPatch.getWidth();
    }
    private int getY(int ind) {
        return ind / rasterPatch.getWidth();
    }

    private void initDistBuf(int x, int y, int w, int h) {
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x+w > rasterPatch.getWidth())
            w = rasterPatch.getWidth() - x;
        if(y+h > rasterPatch.getHeight())
            h = rasterPatch.getHeight() - y;
        dist = new double[w*h];
        ante = new byte[w*h];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(ante, (byte)-1);
        xd = x; yd = y;
        wd = w; hd = h;
    }

    private double getDist(int ind) {
        int x = getX(ind)-xd;
        int y = getY(ind)-yd;
        if(x < 0 || x >= wd || y < 0 || y >= hd) {
            resizeDistBuf();
            return Double.MAX_VALUE;
        } else
            return dist[y*wd+x];

    }

    private void setDist(int ind, double val) {
        int x = getX(ind)-xd;
        int y = getY(ind)-yd;
        if(x < 0 || x >= wd || y < 0 || y >= hd) {
            resizeDistBuf();
            setDist(ind, val);
        } else
            dist[y*wd+x] = val;
    }
    
    private byte getAnte(int ind) {
        int x = getX(ind)-xd;
        int y = getY(ind)-yd;
        return ante[y*wd+x];
    }

    private void setAnte(int ind, byte val) {
        int x = getX(ind)-xd;
        int y = getY(ind)-yd;
        if(x < 0 || x >= wd || y < 0 || y >= hd) {
            resizeDistBuf();
            setAnte(ind, val);
        } else
            ante[y*wd+x] = val;
    }

    private void resizeDistBuf() {
        int w = (int)(wd*1.4+1);
        int h = (int)(hd*1.4+1);
        int x = xd - (int)(wd*0.2+1);
        int y = yd - (int)(hd*0.2+1);
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x+w > rasterPatch.getWidth())
            w = rasterPatch.getWidth() - x;
        if(y+h > rasterPatch.getHeight())
            h = rasterPatch.getHeight() - y;
        double [] buf = new double[w*h];
        Arrays.fill(buf, Double.MAX_VALUE);
        for(int i = 0; i < hd; i++)
            for(int j = 0; j < wd; j++)
                buf[(i+ yd-y)*w+j+(xd-x)] = dist[i*wd+j];
        
        byte [] bufAnte = new byte[w*h];
        Arrays.fill(bufAnte, (byte)-1);
        for(int i = 0; i < hd; i++)
            for(int j = 0; j < wd; j++)
                bufAnte[(i+ yd-y)*w+j+(xd-x)] = ante[i*wd+j];

        xd = x; yd = y;
        wd = w; hd = h;
        dist = buf;
        ante = bufAnte;
    }

    /**
     * Node for the PriorityQueue
     */
    private static class Node implements Comparable<Node>{
        int ind;
        double dist;

        public Node(int ind, double dist) {
            this.ind = ind;
            this.dist = dist;
        }

        public final int compareTo(Node o) {
            return dist == o.dist ? 0 : dist < o.dist ? -1 : 1;
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            
            return this.ind == ((Node)obj).ind;
        }

        @Override
        public final int hashCode() {
            int hash = 7;
            hash = 43 * hash + this.ind;
            return hash;
        }

    }

}
