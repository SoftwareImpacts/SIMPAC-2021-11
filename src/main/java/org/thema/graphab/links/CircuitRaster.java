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


package org.thema.graphab.links;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.DiagonalPreconditioner;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.Preconditioner;
import org.thema.common.JTS;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.util.SpatialOp;

/**
 * Calculates electrical circuit on raster map.
 * 
 * @author Gilles Vuidel
 */
public final class CircuitRaster {
    
    private final Raster rasterPatch;
    private final Raster costRaster;
    private final Raster demRaster;
    private final double [] cost;
    private final double coefSlope;
    
    private final boolean con8;
    private final boolean optimCirc;
    
    private final double resolution;
    private final Project project;
    
    // solver parameters
    /** Relative error precision for stop condition */
    public static double prec = 1e-6;
    /** Norm type for calculating error */
    public static Vector.Norm errNorm = Vector.Norm.Two;
    /** Types of initial solution vector */
    public enum InitVector {
        /** Choose automatically the best initial solution */
        ANY, 
        /** Flat vector initialize to zero but the two ending nodes */
        FLAT, 
        /** Use the euclidean distance to estimate initial solution vector */
        DIST
    }
    /** the type of initial solution vector */
    public static InitVector initVector = InitVector.FLAT;
       
    /**
     * Creates a new Circuit raster from the lanscape map.
     * @param prj the project
     * @param codeRaster the landscape map
     * @param cost th cost array associating codes map with cost
     * @param con8 8 connexity or 4 ?
     * @param optimCirc optimize execution time by reducing raster size ?
     * @param coefSlope the slope coefficient, 0 for no slope
     * @throws IOException 
     */
    public CircuitRaster(Project prj, Raster codeRaster, double [] cost, boolean con8, boolean optimCirc, double coefSlope) throws IOException {
        this.project = prj;
        this.rasterPatch = project.getRasterPatch();
        this.costRaster = codeRaster;
        this.cost = Arrays.copyOf(cost, cost.length);
        this.coefSlope = coefSlope;
        this.con8 = con8;
        this.optimCirc = optimCirc;
        
        demRaster = coefSlope != 0 ? project.getDemRaster() : null;
        resolution = project.getResolution();
    }

    /**
     * Creates a new Circuit raster from an external cost map.
     * @param prj the project
     * @param costRaster the cost map
     * @param con8 8 connexity or 4 ?
     * @param optimCirc optimize execution time by reducing raster size ?
     * @param coefSlope the slope coefficient, 0 for no slope
     * @throws IOException 
     */
    public CircuitRaster(Project prj, Raster costRaster, boolean con8, boolean optimCirc, double coefSlope) throws IOException {
        this(prj, costRaster, null, con8, optimCirc, coefSlope);
    }
    
    /**
     * Creates an electrical circuit between the points c1 and c2
     * @param c1 the point in world coordinate space emitting the current
     * @param c2 the point in world coordinate space receiving the current
     * @return the created circuit
     */
    public ODCircuit getODCircuit(Coordinate c1, Coordinate c2) {
        Rectangle compZone = new Rectangle(0, 0, rasterPatch.getWidth(), rasterPatch.getHeight());
        Coordinate tc1 = project.getSpace2grid().transform(c1, new Coordinate());
        Coordinate tc2 = project.getSpace2grid().transform(c2, new Coordinate());
        Point p1 = new Point((int)tc1.x, (int)tc1.y);
        Point p2 = new Point((int)tc2.x, (int)tc2.y);

        WritableRaster rasterZone = checkConnexity(p1, p2, compZone);

        return createCircuit(c1, c2, compZone, rasterZone);
    }
    
    /**
     * Creates an electrical circuit between the patch patch1 and patch2
     * @param patch1 the patch emitting the current
     * @param patch2 the patch receiving the current
     * @return the created circuit
     */
    public PatchODCircuit getODCircuit(Feature patch1, Feature patch2) {
        Rectangle compZone = new Rectangle(0, 0, rasterPatch.getWidth(), rasterPatch.getHeight());
        Rectangle zone = compZone;
        Raster rasterZone = null;
        if(optimCirc) {
            Geometry gEnv = new GeometryFactory().toGeometry(patch1.getGeometry().getEnvelopeInternal());
            gEnv.apply(project.getSpace2grid());
            Envelope env = gEnv.getEnvelopeInternal();
            gEnv = new GeometryFactory().toGeometry(patch2.getGeometry().getEnvelopeInternal());
            gEnv.apply(project.getSpace2grid());
            env.expandToInclude(gEnv.getEnvelopeInternal());
            boolean connex = false;
            while(!connex) {
                env.expandBy(2+env.maxExtent());
                zone = compZone.createIntersection(new Rectangle((int)env.getMinX(), (int)env.getMinY(), (int)env.getWidth(), (int)env.getHeight())).getBounds();
                rasterZone = checkConnexity(patch1, patch2, zone);
                connex = rasterZone != null;
                if(!connex) {
                    Logger.getLogger(CircuitRaster.class.getName()).info("Two patches (" + patch1 + "-" + patch2 + ") are not connected with rect : " + zone + " -> expand rect");
                }
            }
        } else {
            rasterZone = checkConnexity(patch1, patch2, zone);
        }
        // la connexité est bonne, maintenant on supprime les trous dans les taches
        WritableRaster rasterZone2 = checkConnexity(patch2, patch1, zone);
        for(int y = rasterZone2.getMinY(); y < rasterZone2.getMinY()+rasterZone2.getHeight(); y++) {
            for(int x = rasterZone2.getMinX(); x < rasterZone2.getMinX()+rasterZone2.getWidth(); x++) {
                if(rasterZone2.getSample(x, y, 0) != rasterZone.getSample(x, y, 0)) {
                    rasterZone2.setSample(x, y, 0, -1);
                }
            }
        }

        return (PatchODCircuit) createCircuit(patch1, patch2, zone, rasterZone2);
    }
    
    /**
     * Check the connexity between p1 and p2 in zone.
     * @param p1 the first point in raster coordinate
     * @param p2 the second point in raster coordinate
     * @param zone the restricted zone in raster coordinate
     * @return a raster of size zone, where pixel is 1 if connected and -1 if not, or null if p1 and p2 are not connex inside zone
     */
    private WritableRaster checkConnexity(Point p1, Point p2, Rectangle zone) {
        final int w = rasterPatch.getWidth();
        
        boolean connex = false;
        
        WritableRaster rasterZone = Raster.createBandedRaster(DataBuffer.TYPE_INT, zone.width, zone.height, 1, new java.awt.Point(zone.x, zone.y));
        for(int x = 0; x < zone.width; x++) {
            rasterZone.setSample(x+zone.x, zone.y, 0, -1);
            rasterZone.setSample(x+zone.x, (int)zone.getMaxY()-1, 0, -1);
        }
        for(int y = 0; y < zone.height; y++) {
            rasterZone.setSample(zone.x, y+zone.y, 0, -1);
            rasterZone.setSample((int)zone.getMaxX()-1, y+zone.y, 0, -1);
        }
        
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(p1.y*w+p1.x);
        rasterZone.setSample(p1.x, p1.y, 0, 1);
        rasterZone.setSample(p2.x, p2.y, 0, 1);
        
        final int [] dir, xd, yd;
        if(con8) {
            dir = new int[] {-w-1, -w, -w+1, -1, +1, +w-1, +w, +w+1};
            xd = new int[] {-1, 0, +1, -1, +1, -1, 0, +1};
            yd = new int[] {-1, -1, -1, 0, 0, +1, +1, +1};

        } else {
            dir = new int[] {-w, -1, +1, +w};
            xd = new int[] {0, -1, +1, 0};
            yd = new int[] {-1, 0, 0, +1};
        }
        
        while(!queue.isEmpty()) {
            final int ind = queue.poll();
            final int x = ind % w;
            final int y = ind / w;
            rasterZone.setSample(x, y, 0, 1);
            for(int d = 0; d < dir.length; d++) {
                final int x1 = x+xd[d];
                final int y1 = y+yd[d];
                if(p2.x == x1 && p2.y == y1) {
                    connex = true;
                } else {
                    if(rasterPatch.getSample(x1, y1, 0) != -1 && rasterZone.getSample(x1, y1, 0) == 0) {
                        rasterZone.setSample(x1, y1, 0, 1);
                        queue.add(ind+dir[d]);
                    }
                }
            }
        }
        
        int nbRem = 0;
        for(int y = 0; y < zone.height; y++) {
            for (int x = 0; x < zone.width; x++) {
                if (rasterZone.getSample(x+zone.x, y+zone.y, 0) == 0) {
                    rasterZone.setSample(x+zone.x, y+zone.y, 0, -1);
                    if (rasterPatch.getSample(x+zone.x, y+zone.y, 0) != -1) {
                        nbRem++;
                    }
                }
            }
        }
        
        if(nbRem > 0) {
            Logger.getLogger(CircuitRaster.class.getName()).info(nbRem + " pixels are not connected for points (" + p1 + "-" + p2 + ") with rect : " + zone + " -> they are ignored");
        }
        
        if(connex) {
            return rasterZone;
        } else {
            return null;
        }
    }
    
    private WritableRaster checkConnexity(Feature patch1, Feature patch2, Rectangle zone) {
        final int id1 = (Integer)patch1.getId();
        final int id2 = (Integer)patch2.getId();
        final int w = rasterPatch.getWidth();
        
        boolean connex = false;
        
        WritableRaster rasterZone = Raster.createBandedRaster(DataBuffer.TYPE_INT, zone.width, zone.height, 1, new java.awt.Point(zone.x, zone.y));
        for(int x = 0; x < zone.width; x++) {
            rasterZone.setSample(x+zone.x, zone.y, 0, -1);
            rasterZone.setSample(x+zone.x, (int)zone.getMaxY()-1, 0, -1);
        }
        for(int y = 0; y < zone.height; y++) {
            rasterZone.setSample(zone.x, y+zone.y, 0, -1);
            rasterZone.setSample((int)zone.getMaxX()-1, y+zone.y, 0, -1);
        }
        
        LinkedList<Integer> queue = new LinkedList<>();
        Envelope env = patch1.getGeometry().getEnvelopeInternal();
        env.expandToInclude(patch2.getGeometry().getEnvelopeInternal());
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(project.getSpace2grid());
        env = gEnv.getEnvelopeInternal();
        for(int i = (int)env.getMinY(); i <= env.getMaxY(); i++) {
            for (int j = (int)env.getMinX(); j <= env.getMaxX(); j++) {
                if(rasterPatch.getSample(j, i, 0) == id1) {
                    queue.add(i*w+j);
                    rasterZone.setSample(j, i, 0, 1);
                } else if(rasterPatch.getSample(j, i, 0) == id2) {
                    rasterZone.setSample(j, i, 0, 1);
                }
            }
        }
        
        final int [] dir, xd, yd;
        if(con8) {
            dir = new int[] {-w-1, -w, -w+1, -1, +1, +w-1, +w, +w+1};
            xd = new int[] {-1, 0, +1, -1, +1, -1, 0, +1};
            yd = new int[] {-1, -1, -1, 0, 0, +1, +1, +1};

        } else {
            dir = new int[] {-w, -1, +1, +w};
            xd = new int[] {0, -1, +1, 0};
            yd = new int[] {-1, 0, 0, +1};
        }
        
        while(!queue.isEmpty()) {
            final int ind = queue.poll();
            final int x = ind % w;
            final int y = ind / w;
            rasterZone.setSample(x, y, 0, 1);
            for(int d = 0; d < dir.length; d++) {
                int val = rasterPatch.getSample(x+xd[d], y+yd[d], 0);
                if(val == id2) {
                    connex = true;
                } else {
                    if(val != -1 && rasterZone.getSample(x+xd[d], y+yd[d], 0) == 0) {
                        rasterZone.setSample(x+xd[d], y+yd[d], 0, 1);
                        queue.add(ind+dir[d]);
                    }
                }
            }
        }
        
        int nbRem = 0;
        for(int y = 0; y < zone.height; y++) {
            for (int x = 0; x < zone.width; x++) {
                if (rasterZone.getSample(x+zone.x, y+zone.y, 0) == 0) {
                    rasterZone.setSample(x+zone.x, y+zone.y, 0, -1);
                    if (rasterPatch.getSample(x+zone.x, y+zone.y, 0) != -1) {
                        nbRem++;
                    }
                }
            }
        }
        
        if(nbRem > 0) {
            Logger.getLogger(CircuitRaster.class.getName()).info(nbRem + " pixels are not connected for patches (" + patch1 + "-" + patch2 + ") with rect : " + zone + " -> they are ignored");
        }
        
        if(connex) {
            return rasterZone;
        } else {
            return null;
        }
    }
    
    /**
     * Creates the circuit between patches or points
     * @param p1 must be a Coordinate or Feature (patch)
     * @param p2 must be Coordinate or Feature (patch)
     * @param zone the zone of the circuit
     * @param rasterZone raster created by checkConnexity
     * @return the created circuit
     */
    private ODCircuit createCircuit(Object p1, Object p2, Rectangle zone, Raster rasterZone) {
        final int id1, id2;
        final Point po1, po2;
        final boolean isPatch = p1 instanceof Feature;
        // is it patch
        if(isPatch) {
            id1 = (Integer)((Feature)p1).getId();
            id2 = (Integer)((Feature)p2).getId();
            po1 = po2 = null;
            if(id1 == id2) {
                throw new IllegalArgumentException("Same patches");
            }
        } else {
            id1 = id2 = -2;
            Coordinate tc1 = project.getSpace2grid().transform((Coordinate)p1, new Coordinate());
            Coordinate tc2 = project.getSpace2grid().transform((Coordinate)p2, new Coordinate());
            po1 = new Point((int)tc1.x, (int)tc1.y);
            po2 = new Point((int)tc2.x, (int)tc2.y);
        }
        
        final int w = zone.width;
        final int h = zone.height;

        int [] img2mat = new int[w*h];
        int i = 2;
        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int ind = y*w+x;
                if (x == 0 || y == 0 || x == w-1 || y == h-1) {
                    img2mat[ind] = -1;
                } else if (rasterZone.getSample(x+zone.x, y+zone.y, 0) == -1) {
                    img2mat[ind] = -1;
                } else if (isPatch && rasterPatch.getSample(x+zone.x, y+zone.y, 0) == id1) {
                    img2mat[ind] = 0;
                } else if (isPatch && rasterPatch.getSample(x+zone.x, y+zone.y, 0) == id2) {
                    img2mat[ind] = 1;
                } else if (!isPatch && po1.x == x+zone.x && po1.y == y+zone.y) {
                    img2mat[ind] = 0;
                } else if (!isPatch && po2.x == x+zone.x && po2.y == y+zone.y) {
                    img2mat[ind] = 1;
                } else {
                    img2mat[ind] = i;
                    i++;
                }
            }
        }
        
        final int [] dir, xd, yd;
        final double [] wd;
        if(con8) {
            dir = new int[] {-w-1, -w, -w+1, -1, +1, +w-1, +w, +w+1};
            xd = new int[] {-1, 0, +1, -1, +1, -1, 0, +1};
            yd = new int[] {-1, -1, -1, 0, 0, +1, +1, +1};
            wd = new double[] {1.4142136, 1, 1.4142136, 1, 1, 1.4142136, 1, 1.4142136};
        } else {
            dir = new int[] {-w, -1, +1, +w};
            xd = new int[] {0, -1, +1, 0};
            yd = new int[] {-1, 0, 0, +1};
            wd = new double[] {1, 1, 1, 1};
        }
        
        final int size = i;
        int [][] tab = new int[size][];
        TreeSet<Integer> indPatch1 = new TreeSet<>();
        TreeSet<Integer> indPatch2 = new TreeSet<>();
        TreeSet<Integer> buf = new TreeSet<>();
        int [] colsInt = new int[10];
        for(int y = 1; y < h-1; y++) {
            for (int x = 1; x < w-1; x++) {
                int indImg = y*w+x;
                int indMat = img2mat[indImg];
                if (indMat == -1) {
                    continue;
                }
                
                if(isPatch) {
                    buf.clear();
                    TreeSet<Integer> cols = indMat == 0 ? indPatch1 : indMat == 1 ? indPatch2 : buf;
                    for (int d : dir) {
                        int indMat2 = img2mat[indImg + d];
                        if (indMat2 != -1 && indMat2 != indMat) {
                            cols.add(indMat2);
                        }
                    }
                    if (indMat >= 2) {
                        if (cols.isEmpty()) {
                            throw new RuntimeException("Isolated pixel at (" + x + "," + y +")");
                        }
                        cols.add(indMat);
                        int [] col = new int[cols.size()];
                        i = 0;
                        for (Integer ind : cols) {
                            col[i++] = ind;
                        }
                        tab[indMat] = col;
                    }
                } else {
                    
                    i = 0;
                    for (int d : dir) {
                        int indMat2 = img2mat[indImg + d];
                        if (indMat2 != -1 && indMat2 != indMat) {
                            colsInt[i++] = indMat2;
                        }
                    }
                    
                    if (i == 0) {
                        throw new RuntimeException("Isolated pixel at (" + x + "," + y +")");
                    }
                    colsInt[i++] = indMat;
                    int [] col = Arrays.copyOf(colsInt, i);
                    Arrays.sort(col);
                    tab[indMat] = col;
                    
                }
            }
        }
        if(isPatch) {
            indPatch1.add(0);
            int [] col = new int[indPatch1.size()];
            i = 0;
            for(Integer ind : indPatch1) {
                col[i++] = ind;
            }
            tab[0] = col;
            indPatch2.add(1);
            col = new int[indPatch2.size()];
            i = 0;
            for(Integer ind : indPatch2) {
                col[i++] = ind;
            }
            tab[1] = col;
        }
        CompRowMatrix A = new CompRowMatrix(size, size, tab);
        for(int y = 1; y < h-1; y++) {
            for (int x = 1; x < w-1; x++) {
                int indImg = y*w+x;
                int indMat = img2mat[indImg];
                if (indMat == -1) {
                    continue;
                }
                int xf = x + zone.x;
                int yf = y + zone.y;
                double c = getCost(xf, yf);
                if (c <= 0) {
                    throw new RuntimeException("Null or negative resistance is forbidden");
                }
                double sum = 0;
                for(int d = 0; d < dir.length; d++) {
                    int indMat2 = img2mat[indImg + dir[d]];
                    if(indMat2 != -1 && indMat2 != indMat) {
                        double r = (getCost(xf+xd[d], yf+yd[d]) + c)*wd[d]/2 * (1 + (coefSlope != 0 ? getSlope(xf, yf, xd[d], yd[d], wd[d])*coefSlope : 0));
                        A.add(indMat, indMat2, -1/r);
                        sum += 1/r;
                    }
                }
                A.add(indMat, indMat, sum);
            }
        }
        
        // check matrix
//        double [] o = new double[size];
//        Arrays.fill(o, 1);
//        DenseVector I = new DenseVector(o, true);
//        A.mult(I, I);
//        if(I.norm(Vector.Norm.One) > 1e-10) {
//            throw new RuntimeException("A rows are not null : " + I.norm(Vector.Norm.One));
//        }
//        Matrix Im = new DenseMatrix(new DenseVector(o, true), true);
//        Im = Im.transAmult(A, new DenseMatrix(1, size));
//        if(Im.norm(Norm.One) > 1e-10) {
//            throw new RuntimeException("A columns are not null" + Im.norm(Norm.One));
//        }
        if(isPatch) {
            PatchODCircuit circuit = new PatchODCircuit();
            circuit.zone = zone;
            circuit.patch1 = (Feature) p1;
            circuit.patch2 = (Feature) p2;
            circuit.img2mat = img2mat;
            circuit.tabMatrix = tab;
            circuit.A = A;   
            return circuit;
        } else {
            PointODCircuit circuit = new PointODCircuit();
            circuit.zone = zone;
            circuit.c1 = (Coordinate) p1;
            circuit.c2 = (Coordinate) p2;
            circuit.img2mat = img2mat;
            circuit.tabMatrix = tab;
            circuit.A = A;   
            return circuit;
        }
    }

    private double getCost(int x, int y) {
        return cost == null ? costRaster.getSampleDouble(x, y, 0) : cost[costRaster.getSample(x, y, 0)];
    }       
    
    private double getSlope(int x, int y, int xd, int yd, double wd) {
        return Math.abs(demRaster.getSampleDouble(x+xd, y+yd, 0) - demRaster.getSampleDouble(x, y, 0)) 
                / (wd * resolution);
    }

    /**
     * Electrical circuit between 2 nodes, one emitting the current and one receiving the current.
     */
    public abstract class ODCircuit {
        protected Rectangle zone;
        protected int [] img2mat;
        protected int[][] tabMatrix;
        protected CompRowMatrix A;
        protected DenseVector U, Z;
        private int nbIter;
        private double initErrSum;
        
        /**
         * Solves the circuit if not yet done.
         * @return the global resistance between the two nodes
         */
        public double getR() {
            solve();
            return U.get(0) - U.get(1);
        }
        
        /**
         * Solves the circuit if not yet done.
         * Just for testing
         * @param maxCost
         * @return the map 
         */
        public Raster getCorridorMap(double maxCost) {
            solve(); 
            double threshold = 1 / (maxCost / getR());
            Raster current = getCurrentMap();
            WritableRaster corridor = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_BYTE, zone.width, zone.height, 1), 
                    zone.getLocation());
            for(int y = zone.y; y < zone.getMaxY(); y++) {
                for (int x = zone.x; x < zone.getMaxX(); x++) {
                    if (current.getSampleDouble(x, y, 0) >= threshold) {
                        corridor.setSample(x, y, 0, 1);
                    }
                }
            }
            
            return corridor;
        }
        
        /**
         * Solves the circuit if not yet done.
         * @return the current map of the circuit
         */
        public Raster getCurrentMap() {
            solve(); 
            WritableRaster current = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_FLOAT, zone.width, zone.height, 1), 
                     zone.getLocation());
            for(int y = 0; y < zone.height; y++) {
                for (int x = 0; x < zone.width; x++) {
                    int indMat = getIndMat(x, y);
                    if (indMat == -1) {
                        current.setSample(x+zone.x, y+zone.y, 0, Float.NaN);
                    } else if (indMat < 2) {
                        current.setSample(x+zone.x, y+zone.y, 0, Float.NaN);
                    } else {
                        double sum = 0;
                        int[] col = tabMatrix[indMat];
                        for (int ind : col) {
                            if(ind != indMat) {
                                sum += Math.abs((U.get(ind) - U.get(indMat)) * A.get(ind, indMat));
                            }
                        }
                        current.setSample(x+zone.x, y+zone.y, 0, sum/2);
                    }
                }
            }

            return current;
        }
        
        /**
         * @return the number of nodes of the circuit
         */
        public int getSize() {
            return A.numRows();
        }

        /**
         * @return the zone where the circuit has been calculated in raster coordinate
         */
        public Rectangle getZone() {
            return zone;
        }
        
        /**
         * @return the zone where the circuit has been calculated in world coordinate
         */
        public Envelope getEnvelope() {
            return project.getGrid2space().transform(JTS.geomFromRect(zone)).getEnvelopeInternal();
        }
        
        /**
         * @return the max error
         */
        public double getErrMax() {
            DenseVector Zres = new DenseVector(getSize());
            A.mult(U, Zres);
            return Zres.add(-1, Z).norm(Vector.Norm.Infinity);
        }
        
        /**
         * @return the max error ignoring the first two nodes (emitting and receiving the current)
         */
        public double getErrMaxWithoutFirst() {
            DenseVector Zres = new DenseVector(getSize());
            A.mult(U, Zres);
            Zres = new DenseVector(Arrays.copyOfRange(Zres.getData(), 2, Zres.size()));
            
            return Zres.add(-1, new DenseVector(Arrays.copyOfRange(Z.getData(), 2, Z.size())))
                    .norm(Vector.Norm.Infinity);
        }
        
        /**
         * The circuit must be solved before.
         * @return The root of sum of squares of errors
         */
        public double getErrSum() {
            return getErrSum(U);
        }
        
        private double getErrSum(DenseVector U) {
            DenseVector Zres = new DenseVector(getSize());
            A.mult(U, Zres);
            return Zres.add(-1, Z).norm(Vector.Norm.Two);
        }

        /**
         * The circuit must be solved before.
         * @return the number of iteration for solving the circuit
         */
        public int getNbIter() {
            return nbIter;
        }

        /**
         * The circuit must be solved before.
         * @return the initial root of sum of squares of errors
         */
        public double getInitErrSum() {
            return initErrSum;
        }
        
        /**
         * @return the world coordinate of the node emitting the current, or an approximation
         */
        protected abstract Coordinate getCoord1();
        
        /**
         * @return the world coordinate of the node receiving the current, or an approximation
         */
        protected abstract Coordinate getCoord2();
        
        /**
         * Solves the circuit if not yet done.
         * @return the solution vector representing the potential at each node of the circuit
         */
        public synchronized DenseVector solve() {
            if(U != null) {
                return U;
            }
            
            Preconditioner P = new DiagonalPreconditioner(getSize());
            P.setMatrix(A);

            // Z vector is null but the 2 first elements
            Z = new DenseVector(getSize());
            Z.set(0, 1);
            Z.set(1, -1);

            // Starting solution U
            double [] v = new double[getSize()];
            v[0] = 1;
            v[1] = -1;
            
            double [] vOptim = Arrays.copyOf(v, v.length);
            if(initVector != InitVector.FLAT) {
                // permet d'améliorer la précision d'un facteur 10 et un peu le temps d'exécution mais pas dans tous les cas...
                Coordinate c1 = project.getSpace2grid().transform(getCoord1(), new Coordinate());
                Coordinate c2 = project.getSpace2grid().transform(getCoord2(), new Coordinate());  
                Coordinate c = new Coordinate();
                for(int y = 0; y < zone.height; y++) { 
                    for(int x = 0; x < zone.width; x++) {
                        c.x = x + zone.x;
                        c.y = y + zone.y;
                        int indImg = y*zone.width+x;
                        int indMat = img2mat[indImg];
                        if(indMat < 2) {
                            continue;
                        }
                        vOptim[indMat] = (c.distance(c2) - c.distance(c1)) / (c.distance(c1) + c.distance(c2));
                    }
                }
            }
            
            if(initVector == InitVector.DIST || (initVector == InitVector.ANY && getErrSum(new DenseVector(v)) > getErrSum(new DenseVector(vOptim)))) {
                if(initVector == InitVector.ANY) {
                    Logger.getLogger(ODCircuit.class.getName()).info("Use distance initial vector");
                }
                U = new DenseVector(vOptim);
            } else {
                if(initVector == InitVector.ANY) {
                    Logger.getLogger(ODCircuit.class.getName()).info("Use flat initial vector");
                }
                U = new DenseVector(v);
            }
                
            initErrSum = getErrSum(U);
            IterativeSolver solver = new CG(U);
            solver.setPreconditioner(P);
            DefaultIterationMonitor mon = new DefaultIterationMonitor(100000, prec, 1e-50, 1e+5);
            mon.setNormType(errNorm);
            solver.setIterationMonitor(mon);
            long t = System.currentTimeMillis();
            try {
                // Start the solver, and check for problems
                solver.solve(A, Z, U);
            } catch (IterativeSolverNotConvergedException ex) {
                throw new RuntimeException(ex);
            }
            nbIter = mon.iterations();
            
            Logger.getLogger(ODCircuit.class.getName()).info("Row size " + A.numRows() + " - Solve in " + ((System.currentTimeMillis()-t) / 1000.0) + " s " 
                    + nbIter + " iter. - Err max " + getErrMax() + " - Err sum " + getErrSum());
            return U;
        }

        private int getIndMat(int x, int y) {
            return img2mat[y*zone.width+x];
        }
    }    
    
    public final class PatchODCircuit extends ODCircuit{

        private Feature patch1, patch2;
        
        /**
         * 
         * @param maxCost
         * @return a vectorial version of the corridor map
         */
        public Geometry getCorridor(double maxCost) {
            Raster r = getCorridorMap(maxCost);
            Geometry corridor = SpatialOp.vectorize(r, JTS.rectToEnv(zone), 1);
            corridor = project.getGrid2space().transform(corridor);
            List<Geometry> geomTouches = new ArrayList<>();
            for(int i = 0; i < corridor.getNumGeometries(); i++) {
                if (corridor.getGeometryN(i).intersects(patch1.getGeometry()) &&
                        corridor.getGeometryN(i).intersects(patch2.getGeometry())) {
                    geomTouches.add(corridor.getGeometryN(i));
                }
            }
            return new GeometryFactory().buildGeometry(geomTouches);
        }

        @Override
        protected Coordinate getCoord1() {
            return patch1.getGeometry().getCentroid().getCoordinate();
        }

        @Override
        protected Coordinate getCoord2() {
            return patch2.getGeometry().getCentroid().getCoordinate();
        }
    }    
    
    public final class PointODCircuit extends ODCircuit {

        private Coordinate c1, c2;

        @Override
        protected Coordinate getCoord1() {
            return c1;
        }

        @Override
        protected Coordinate getCoord2() {
            return c2;
        }
      
    }    
}
