/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.util.AffineTransformation;
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
import java.util.logging.Level;
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

/**
 *
 * @author gvuidel
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
    
    public CircuitRaster(Project prj, Raster codeRaster, double [] cost, boolean con8, boolean optimCirc, double coefSlope) throws IOException {
        this.project = prj;
        this.rasterPatch = project.getRasterPatch();
        this.costRaster = codeRaster;
        this.cost = cost;
        this.coefSlope = coefSlope;
        this.con8 = con8;
        this.optimCirc = optimCirc;
        
        demRaster = coefSlope != 0 ? project.getDemRaster() : null;
        resolution = project.getResolution();
    }

    public CircuitRaster(Project prj, Raster costRaster, boolean con8, boolean optimCirc, double coefSlope) throws IOException {
        this(prj, costRaster, null, con8, optimCirc, coefSlope);
    }
    
    public ODCircuit getODCircuit(Feature patch1, Feature patch2) {
        
        Rectangle compZone = new Rectangle(0, 0, rasterPatch.getWidth(), rasterPatch.getHeight());
        Rectangle zone = compZone;
        Raster rasterZone = rasterPatch;
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
                if(zone.equals(compZone)) {
                    connex = true;
                } else {
                    rasterZone = checkConnexity(patch1, patch2, zone);
                    connex = rasterZone != null;
                }
                if(!connex) {
                    System.out.println("Two patches (" + patch1 + "-" + patch2 + ") are not connected with rect : " + zone + " -> expand rect");
                }
            }
        }
        return createCircuit(patch1, patch2, zone, rasterZone);
    }
    
    private Raster checkConnexity(Feature patch1, Feature patch2, Rectangle zone) {
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
        Geometry gEnv = new GeometryFactory().toGeometry(patch1.getGeometry().getEnvelopeInternal());
        gEnv.apply(project.getSpace2grid());
        Envelope env = gEnv.getEnvelopeInternal();
        for(int i = (int)env.getMinY(); i <= env.getMaxY(); i++) {
            for (int j = (int)env.getMinX(); j <= env.getMaxX(); j++) {
                if(rasterPatch.getSample(j, i, 0) == id1) {
                    queue.add(i*w+j);
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
                if(val != -1 && rasterZone.getSample(x+xd[d], y+yd[d], 0) == 0) {
                    rasterZone.setSample(x+xd[d], y+yd[d], 0, 1);
                    queue.add(ind+dir[d]);
                    if(val == id2) {
                        connex = true;
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
            System.out.println(nbRem + " pixels are not connected for patches (" + patch1 + "-" + patch2 + ") with rect : " + zone + " -> they are ignored");
        }
        
        if(connex) {
            return rasterZone;
        } else {
            return null;
        }
    }
    
    private ODCircuit createCircuit(Feature patch1, Feature patch2, Rectangle zone, Raster rasterZone) {
        final int id1 = (Integer)patch1.getId();
        final int id2 = (Integer)patch2.getId();
        if(id1 == id2) {
            throw new IllegalArgumentException("Same patches");
        }
        
        final int w = zone.width;
        final int h = zone.height;

        int [] img2mat = new int[w*h];
        int [] mat2img = new int[w*h];
        Arrays.fill(mat2img, -1);
        int i = 2;
        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int ind = y*w+x;
                if (x == 0 || y == 0 || x == w-1 || y == h-1) {
                    img2mat[ind] = -1;
                } else if (rasterZone.getSample(x+zone.x, y+zone.y, 0) == -1) {
                    img2mat[ind] = -1;
                } else if (rasterPatch.getSample(x+zone.x, y+zone.y, 0) == id1) {
                    img2mat[ind] = 0;
                } else if (rasterPatch.getSample(x+zone.x, y+zone.y, 0) == id2) {
                    img2mat[ind] = 1;
                } else {
                    img2mat[ind] = i;
                    mat2img[i] = ind;
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
        for(int y = 1; y < h-1; y++) {
            for (int x = 1; x < w-1; x++) {
                int indImg = y*w+x;
                int indMat = img2mat[indImg];
                if (indMat == -1) {
                    continue;
                }
                TreeSet<Integer> cols = indMat == 0 ? indPatch1 : indMat == 1 ? indPatch2 : new TreeSet<Integer>();
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
            }
        }
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
        ODCircuit circuit = new ODCircuit();
        circuit.zone = zone;
        circuit.patch1 = patch1;
        circuit.patch2 = patch2;
        circuit.img2mat = img2mat;
        circuit.mat2img = mat2img;
        circuit.tabMatrix = tab;
        circuit.A = A;
        return circuit;
    }

    private double getCost(int x, int y) {
        return cost == null ? costRaster.getSampleDouble(x, y, 0) : cost[costRaster.getSample(x, y, 0)];
    }       
    
    private double getSlope(int x, int y, int xd, int yd, double wd) {
        return Math.abs(demRaster.getSampleDouble(x+xd, y+yd, 0) - demRaster.getSampleDouble(x, y, 0)) 
                / (wd * resolution);
    }
    
    public final class ODCircuit {
        private Rectangle zone;
        private Feature patch1, patch2;
        private int [] img2mat, mat2img;
        private int[][] tabMatrix;
        private CompRowMatrix A;
        private DenseVector U, Z;
        
        
        public double getR() {
            solve();
            return U.get(0) - U.get(1);
        }
        
        public Geometry getCorridor(double maxCost) {
            Raster r = getCorridorMap(maxCost);
            Geometry corridor = Project.vectorize(r, new Envelope(0, zone.width, 0, zone.height), 1);
            corridor = AffineTransformation.translationInstance(zone.x, zone.y).transform(corridor);
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
        
        public Raster getCorridorMap(double maxCost) {
            solve(); 
            double threshold = 1 / (maxCost / getR());
            Raster current = getCurrentMap();
            WritableRaster corridor = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_BYTE, zone.width, zone.height, 1), null);
            for(int y = 0; y < zone.height; y++) {
                for (int x = 0; x < zone.width; x++) {
                    if (current.getSampleDouble(x, y, 0) >= threshold) {
                        corridor.setSample(x, y, 0, 1);
                    }
                }
            }
            
            return corridor;
        }
        
//        public Raster getCorridorMap(double part) {
//            solve(); 
//            WritableRaster corridor = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_BYTE, zone.width, zone.height, 1), null);
//            LinkedList<Integer> queue = new LinkedList<Integer>();
//
//            queue.add(0);
//            
//            while(!queue.isEmpty()) {
//                int indMat = queue.poll();
//                double tot = 0;
//                TreeMapList<Double, Integer> distribCurrent = new TreeMapList<Double, Integer>();
//                for(int ind : tabMatrix[indMat])
//                    if(ind >= 2) {
//                        double i = (U.get(indMat) - U.get(ind)) * -A.get(indMat, ind);
//                        if(i > 0) {
//                            distribCurrent.putValue(i, ind);
//                            tot += i;
//                        }
//                    }
//
//                double sum = 0;
//                for(Double cur : distribCurrent.descendingKeySet()) {
//                    for(Integer ind : distribCurrent.get(cur)) {
//                        if(sum > tot*part)
//                            continue;
//                        sum += cur;
//                        int indImg = mat2img[ind];
//                        if(corridor.getSample(indImg%zone.width, indImg/zone.width, 0) == 0) {
//                            queue.add(ind);
//                            corridor.setSample(indImg%zone.width, indImg/zone.width, 0, 1);
//                        }
//                    }
//                }
//            
//            }
//            
//            return corridor;
//        }
        
        public Raster getCurrentMap() {
            solve(); 
            WritableRaster current = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_FLOAT, zone.width, zone.height, 1), null);
            for(int y = 0; y < zone.height; y++) {
                for (int x = 0; x < zone.width; x++) {
                    int indMat = getIndMat(x, y);
                    if (indMat == -1) {
                        current.setSample(x, y, 0, Float.NaN);
                    } else if (indMat < 2) {
                        current.setSample(x, y, 0, Float.NaN);
                    } else {
                        double sum = 0;
                        int[] col = tabMatrix[indMat];
                        for (int ind : col) {
                            if(ind != indMat) {
                                sum += Math.abs((U.get(ind) - U.get(indMat)) * A.get(ind, indMat));
                            }
                        }
                        current.setSample(x, y, 0, sum/2);
                        //                    corridor.setSample(x, y, 0, U.get(indMat));
                    }
                }
            }

            return current;
        }
        
        public int getSize() {
            return A.numRows();
        }

        public Rectangle getZone() {
            return zone;
        }
        
        public Envelope getEnvelope() {
            return project.getGrid2space().transform(JTS.geomFromRect(zone)).getEnvelopeInternal();
        }
        
        public double getErrMax() {
            DenseVector Zres = new DenseVector(getSize());
            A.mult(U, Zres);
            return Zres.add(-1, Z).norm(Vector.Norm.Infinity);
        }
        
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
    //        Arrays.fill(v, 1);
            v[0] = 1;
            v[1] = -1;

//            // permet d'améliorer la précision d'un facteur 10 et un peu le temps d'exécution mais pas dans tous les cas...
//            // TODO à tester plus en profondeur
//            Coordinate c1 = project.getSpace2grid().transform(patch1.getGeometry().getCentroid().getCoordinate(), new Coordinate());
//            Coordinate c2 = project.getSpace2grid().transform(patch2.getGeometry().getCentroid().getCoordinate(), new Coordinate());  
//            Coordinate c = new Coordinate();
//            for(int y = 0; y < zone.height; y++) 
//                for(int x = 0; x < zone.width; x++) {
//                    c.x = x + zone.x;
//                    c.y = y + zone.y;
//                    int indImg = y*zone.width+x;
//                    int indMat = img2mat[indImg];
//                    if(indMat < 2)
//                        continue;
//                    v[indMat] = (c.distance(c2) - c.distance(c1)) / (c.distance(c1) + c.distance(c2));
//                }


            U = new DenseVector(v);

            IterativeSolver solver = new CG(U);
            solver.setPreconditioner(P);
            solver.setIterationMonitor(new DefaultIterationMonitor(100000, 1e-6, 1e-50, 1e+5));
            try {
                // Start the solver, and check for problems
                solver.solve(A, Z, U);
            } catch (IterativeSolverNotConvergedException ex) {
                Logger.getLogger(org.thema.graphab.metric.Circuit.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
            
            return U;
        }

        private int getIndMat(int x, int y) {
            return img2mat[y*zone.width+x];
        }
    }    
}
