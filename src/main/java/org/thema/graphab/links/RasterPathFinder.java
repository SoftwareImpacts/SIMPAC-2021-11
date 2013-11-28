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
import java.awt.image.Raster;
import java.io.IOException;
import java.util.*;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.Feature;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */


public class RasterPathFinder implements SpacePathFinder {

    Raster rasterPatch;
    Raster costRaster;
    double [] cost;

    Project project;

    private final int CON = 8;
    private final int [] X = new int[] {-1, 0, 0, +1, -1, -1, +1, +1};
    private final int [] Y = new int[] {0, -1, +1, 0, -1, +1, -1, +1};
    private final double [] COST = new double[] {1.0, 1.0, 1.0, 1.0, 1.41421, 1.41421, 1.41421, 1.41421};
    private final int [] IND;


    private int xd, yd, wd, hd;
    private float [] dist;

    private int nUpdateBuf, queueSize, nbNodeAdded;

    public RasterPathFinder(Project prj, Raster codeRaster, double [] cost) throws IOException {
        this.project = prj;
        this.rasterPatch = project.getRasterPatch();
        this.costRaster = codeRaster;
        this.cost = cost;
        int w = rasterPatch.getWidth();
        IND = new int[] {-1, -w, +w, +1, -w-1, +w-1, -w+1, +w+1};
    }

    public RasterPathFinder(Project prj, Raster costRaster) throws IOException {
        this(prj, costRaster, null);
    }


    /**
     * Calcule les distances cout à partir du point p vers tous les 
     * destinations dests
     * @return les couts et longueurs des chemins de p vers les destinations
     */
    @Override
    public List<Double[]> calcPaths(Coordinate p, List<? extends Feature> dests) {
        nbNodeAdded = queueSize = 0;

        final int w = rasterPatch.getWidth();

        Coordinate cp = project.getSpace2grid().transform(p, new Coordinate());
        int rx = (int)cp.x;
        int ry = (int)cp.y;
        PriorityQueue<Node> queue = new PriorityQueue<Node>();

        initDistBuf(0, 0, rasterPatch.getWidth(), rasterPatch.getHeight());
        // starting node
        Node node = new Node(ry*w+rx, 0);
        queue.add(node);
        setDist(ry*w+rx, 0);

        while(!queue.isEmpty())
            updateNextNodes(queue);

        List<Double[]> distances = new ArrayList<Double[]>(dests.size());
        for(Feature dest : dests) {
            cp = project.getSpace2grid().transform(dest.getGeometry().getCentroid().getCoordinate(), new Coordinate());
            rx = (int)cp.x;
            ry = (int)cp.y;
            distances.add(new Double[]{(double)getDist(ry*w+rx), getPath(ry*w+rx).getLength()});
        }
        return distances;
    }
    
    /**
     * Calcule les distances cout à partir du point p vers tous les patch dont
     * la distance cout est inférieure ou égale à maxCost
     * @return les patch avec le cout et la longueur du chemin
     */
    @Override
    public HashMap<DefaultFeature, Path> calcPaths(Coordinate p, double maxCost, boolean realPath) {
        nbNodeAdded = queueSize = 0;
        DefaultFeature pointPatch = new DefaultFeature(p.toString(), new GeometryFactory().createPoint(p));

        final int w = rasterPatch.getWidth();

        Coordinate cp = project.getSpace2grid().transform(p, new Coordinate());
        int rx = (int)cp.x;
        int ry = (int)cp.y;
        PriorityQueue<Node> queue = new PriorityQueue<Node>();

        initDistBuf(rx-100, ry-100, 200, 200);
        // starting node
        Node node = new Node(ry*w+rx, 0);
        queue.add(node);
        setDist(ry*w+rx, 0);

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
     * @param realPath
     * @return
     */
    @Override
    public HashMap<Feature, Path> calcPaths(Feature oPatch, double maxCost, boolean realPath, boolean all) {

        nbNodeAdded = queueSize = 0;

        final int w = rasterPatch.getWidth();
        final int [] tab = new int[9];

        final int id = (Integer)oPatch.getId();

        Envelope env = oPatch.getGeometry().getEnvelopeInternal();
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(project.getSpace2grid());
        env = gEnv.getEnvelopeInternal();

        
        //  ajout dans la queue des pixels de bord
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
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

//        System.out.println("Nb increase : " + nUpdateBuf + " - Size (" + wd + "," + hd +
//                ") - Queue size : " + queueSize + " - Nb node added : " + nbNodeAdded);
        return distances;
    }

    @Override
    public HashMap<Feature, Path> calcPaths(Feature oPatch, Collection<Feature> dPatch) {

        nbNodeAdded = queueSize = 0;

        final int w = rasterPatch.getWidth();
        final int [] tab = new int[9];

        final int id = (Integer)oPatch.getId();

        Envelope env = oPatch.getGeometry().getEnvelopeInternal();
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(project.getSpace2grid());
        env = gEnv.getEnvelopeInternal();

        // et ajout dans la queue des pixels de bord
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
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

        env = oPatch.getGeometry().getEnvelopeInternal();
        for(Feature d : dPatch)
            env.expandToInclude(d.getGeometry().getEnvelopeInternal());
        gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(project.getSpace2grid());
        env = gEnv.getEnvelopeInternal();
        initDistBuf((int)env.getMinX(), (int)env.getMinY(), (int)env.getWidth(), (int)env.getHeight());


        // initialisation des distances à zéro pour le contour la tache origine
        for(Node n : queue)
            setDist(n.ind, 0);
        
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
            throw new RuntimeException("Impossible de calculer le chemin de " + id + " à " + Arrays.deepToString(destId.keySet().toArray()));

//        System.out.println("Nb increase : " + nUpdateBuf + " - Size (" + wd + "," + hd +
//                ") - Queue size : " + queueSize + " - Nb node added : " + nbNodeAdded);
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
     * @param x, y
     * @return an array with id of nearest patch, cost and dist
     */
    public double[] calcPathNearestPatch(int rx, int ry) {

        final int w = rasterPatch.getWidth();

        PriorityQueue<Node> queue = new PriorityQueue<Node>();

        initDistBuf(rx-100, ry-100, 200, 200);
        // starting node
        Node node = new Node(ry*w+rx, 0);
        queue.add(node);
        setDist(ry*w+rx, 0);

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

        nbNodeAdded = queueSize = 0;

        final int w = rasterPatch.getWidth();
        final int [] tab = new int[9];

        final int id = (Integer)oPatch.getId();

        Envelope env = oPatch.getGeometry().getEnvelopeInternal();
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(project.getSpace2grid());
        env = gEnv.getEnvelopeInternal();

        //  ajout dans la queue des pixels de bord
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
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

    private LineString getPath(int ind) {
        int w = rasterPatch.getWidth();
        ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
        coords.add(new Coordinate(ind % w + 0.5, ind / w + 0.5));
        Integer curInd = findMinNode(ind);
        while(curInd != null) {
            Coordinate coord = new Coordinate(curInd % w + 0.5, curInd / w + 0.5);
            coords.add(coord);
            curInd = findMinNode(curInd);
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

    private Integer findMinNode(int node) {
        float min = getDist(node);
        Integer best = null;
        for(int i = 0; i < CON; i++) {
            if(getDist(node+IND[i]) < min) {
                best = node+IND[i];
                min = getDist(node+IND[i]);
            }
        }
        return best;
    }

    private Node updateNextNodes(PriorityQueue<Node> queue) {
        final Node current = queue.poll();
        final int x = current.ind % rasterPatch.getWidth();
        final int y = current.ind / rasterPatch.getWidth();
        final double currentCost = getCost(x, y);

        for(int i = 0; i < CON; i++)
            if(isInside(x + X[i], y + Y[i])) {
                final double c = getCost(x+X[i], y+Y[i]);
                final float newCost = (float) (current.dist + COST[i] * (currentCost + c) / 2);
                final int ind = current.ind + IND[i];
                if(newCost < getDist(ind)) {
                    Node newNode = new Node(ind, newCost);
                    setDist(ind, newCost);
                    queue.add(newNode);
                    nbNodeAdded++;
                    if(queue.size() > queueSize)
                        queueSize = queue.size();
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
        nUpdateBuf = 0;
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x+w > rasterPatch.getWidth())
            w = rasterPatch.getWidth() - x;
        if(y+h > rasterPatch.getHeight())
            h = rasterPatch.getHeight() - y;
        dist = new float[w*h];
        Arrays.fill(dist, Float.MAX_VALUE);
        xd = x; yd = y;
        wd = w; hd = h;
    }

    private float getDist(int ind) {
        int x = getX(ind)-xd;
        int y = getY(ind)-yd;
        if(x < 0 || x >= wd || y < 0 || y >= hd) {
            resizeDistBuf();
            return Float.MAX_VALUE;
        } else
            return dist[y*wd+x];

    }

    private void setDist(int ind, float val) {
        int x = getX(ind)-xd;
        int y = getY(ind)-yd;
        if(x < 0 || x >= wd || y < 0 || y >= hd) {
            resizeDistBuf();
            setDist(ind, val);
        } else
            dist[y*wd+x] = val;
    }

    private void resizeDistBuf() {
        nUpdateBuf++;
//        System.out.println("Increase buffer size.");
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
        float [] buf = new float[w*h];
        Arrays.fill(buf, Float.MAX_VALUE);
        for(int i = 0; i < hd; i++)
            for(int j = 0; j < wd; j++)
                buf[(i+ yd-y)*w+j+(xd-x)] = dist[i*wd+j];

        xd = x; yd = y;
        wd = w; hd = h;
        dist = buf;

//        System.out.println("New Size (" + wd + "," + hd + ")");
    }

    private static class Node implements Comparable<Node>{
        int ind;
        float dist;

        public Node(int ind, float dist) {
            this.ind = ind;
            this.dist = dist;
        }

        public final int compareTo(Node o) {
            return dist == o.dist ? 0 : dist < o.dist ? -1 : 1;
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.ind != other.ind) {
                return false;
            }
            return true;
        }

        @Override
        public final int hashCode() {
            int hash = 7;
            hash = 43 * hash + this.ind;
            return hash;
        }

    }

}
