/*
 * Copyright (C) 2015 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thema.graphab.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;

/**
 * Spatial operations mainly for project creation.
 * 
 * @author Gilles Vuidel
 */
public final class SpatialOp {
    
    /**
     * Extract patch from landscape map
     * @param img the landscape map
     * @param codes the patch codes in the landscape map
     * @param noData nodata value if any, or NaN
     * @param con8 if 8 connex or 4 connex ?
     * @param maxSize maximum width or height of patch envelope in pixel
     * @param envMap out parameter containing envelope of each extracted patch
     * @return  a raster containing patch id, 0 outside patch and -1 for nodata, the raster is increased of one pixel border to -1
     */
    public static WritableRaster extractPatch(RenderedImage img, Set<Integer> codes, double noData, boolean con8, int maxSize, Map<Integer, Envelope> envMap) {
        ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Extract_patch"), img.getHeight());
        WritableRaster clust = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_INT, img.getWidth()+2, img.getHeight()+2, 1), null);
        int k = 0;
        TreeSet<Integer> set = new TreeSet<>();
        ArrayList<Integer> idClust = new ArrayList<>();
        RandomIter r = RandomIterFactory.create(img, null);
        for(int j = 1; j <= img.getHeight(); j++) {
            monitor.setProgress(j-1);
            for(int i = 1; i <= img.getWidth(); i++) {
                int val = r.getSample(i-1, j-1, 0);
                if(codes.contains(val)) {
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
                            if(min < minId) {
                                minId = min;
                            }
                        }

                        for(Integer id : set) {
                            idClust.set(getMinId(idClust, id)-1, minId);
                        }

                        clust.setSample(i, j, 0, minId);

                    }
                    set.clear();
                } else if(val == noData) {
                    clust.setSample(i, j, 0, -1);
                }
            }
        }

        // sets the border to -1
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
            while(idClust.get(m-1) != m) {
                m = idClust.get(m-1);
            }
            idClust.set(i, m);
        }
        
        // add offset in id to prevent id superposition while vectorization in parallel mode
        int add = idClust.size();
        for(int i = 0; i < idClust.size(); i++) {
            idClust.set(i, idClust.get(i)+add);
        }

        int maxId = 0;
        for(int j = 1; j < clust.getHeight()-1; j++) {
            for(int i = 1; i < clust.getWidth()-1; i++) {
                if(clust.getSample(i, j, 0) > 0) {
                    int id = idClust.get(clust.getSample(i, j, 0)-1);
                    Envelope env = envMap.get(id);
                    if(env == null) {
                        envMap.put(id, new Envelope(new Coordinate(i, j)));
                    } else {
                        env.expandToInclude(i, j);
                    }

                    clust.setSample(i, j, 0, id);
                    if(maxId < id) {
                        maxId = id;
                    }
                }
            }
        }

        if(maxSize > 0) {
            Envelope clustEnv = new Envelope(1, clust.getWidth()-2, 1, clust.getHeight()-2);
            for(int id : new ArrayList<>(envMap.keySet())) {
                Envelope env = envMap.get(id);
                if(env.getWidth() <= maxSize && env.getHeight() <= maxSize) {
                    continue;
                }
                int nx = (int)Math.ceil((env.getWidth()+1) / maxSize);
                int ny = (int)Math.ceil((env.getHeight()+1) / maxSize);
                int startx = (int)(env.getMinX() - Math.round(nx*maxSize - (env.getWidth()+1)) / 2);
                int starty = (int)(env.getMinY() - Math.round(ny*maxSize - (env.getHeight()+1)) / 2);
                for(int j = 0; j < ny; j++) {
                    for(int i = 0; i < nx; i++) {
                        Envelope e = new Envelope(startx + i*maxSize, startx + (i+1)*maxSize-1, starty + j*maxSize, starty + (j+1)*maxSize-1);
                        e = e.intersection(clustEnv);
                        maxId++;
                        boolean found = false;
                        for(int y = (int)e.getMinY(); y <= e.getMaxY(); y++) {
                            for(int x = (int)e.getMinX(); x <= e.getMaxX(); x++) {
                                if(clust.getSample(x, y, 0) == id) {
                                    clust.setSample(x, y, 0, maxId);
                                    found = true;
                                }
                            }
                        }
                        if(found) {
                            envMap.put(maxId, e);
                        }
                    }
                }
                envMap.remove(id);
            }
        }

        for(Envelope env : envMap.values()) {
            env.expandBy(0.5);
            env.translate(0.5, 0.5);
        }

        monitor.close();

        return clust;
    }

    private static int getMinId(List<Integer> ids, int id) {
        while(ids.get(id-1) != id) {
            id = ids.get(id-1);
        }
        return id;
    }    
    
    /**
     * Change the code of a patch in the patch raster.
     * Used during project creation
     * @param raster the patch raster
     * @param patch the polygon of the patch
     * @param oldCode the current code for the patch
     * @param newCode the new code for the patch
     * @param space2grid world to grid transformation
     */
    public static void recode(WritableRaster raster, Geometry patch, int oldCode, int newCode, AffineTransformation space2grid) {
        Envelope env = patch.getEnvelopeInternal();
        Geometry gEnv = new GeometryFactory().toGeometry(env);
        gEnv.apply(space2grid);
        env = gEnv.getEnvelopeInternal();

        for(int i = (int)env.getMinY(); i <= env.getMaxY(); i++) {
            for(int j = (int)env.getMinX(); j <= env.getMaxX(); j++) {
                if(raster.getSample(j, i, 0) == oldCode) {
                    ((WritableRaster)raster).setSample(j, i, 0, newCode);
                }
            }
        }

    }

    /**
     * Vectorize the voronoi raster
     * @param voronoi the voronoi raster created by {@link Project#neighborhoodEuclid }
     * @param grid2space grid to world coordinate transformation
     * @return the vectorized voronoi features
     */
    public static List<? extends Feature> vectorizeVoronoi(Raster voronoi, AffineTransformation grid2space) {
        ProgressBar monitor = Config.getProgressBar("Vectorize voronoi");
        TreeMap<Integer, Envelope> envMap = new TreeMap<>();
        for(int j = 1; j < voronoi.getHeight()-1; j++) {
            for(int i = 1; i < voronoi.getWidth()-1; i++) {
                if(voronoi.getSample(i, j, 0) > 0) {
                    int id = voronoi.getSample(i, j, 0);
                    Envelope env = envMap.get(id);
                    if(env == null) {
                        envMap.put(id, new Envelope(new Coordinate(i, j)));
                    } else {
                        env.expandToInclude(i, j);
                    }
                    
                }
            }
        }
        monitor.setMaximum(envMap.size());
        for(Envelope env : envMap.values()) {
            env.expandBy(0.5);
            env.translate(0.5, 0.5);
        }

        List<DefaultFeature> features = new ArrayList<>();
        SimpleParallelTask<Integer> task = new SimpleParallelTask<Integer>(
                new ArrayList<Integer>(envMap.keySet()), monitor) {
            @Override
            protected void executeOne(Integer id) {
                Geometry geom = vectorize(voronoi, envMap.get(id), id);
                geom.apply(grid2space);
                synchronized(this) {
                    features.add(new DefaultFeature(id, geom, null, null));
                }
            }
        };
        new ParallelFExecutor(task).executeAndWait();
        
        monitor.close();

        return features;
    }

    /**
     * Vectorize a polygon from a raster given the code and the region
     * @param raster the raster
     * @param env the region of interest in raster coordinate
     * @param code the pixel code to vectorize
     * @return a Polygon or MultiPolygon covering all pixels of value code in the region env
     */
    public static Geometry vectorize(Raster raster, Envelope env, double code) {
        GeometryFactory factory = new GeometryFactory();
        List<LineString> lines = new ArrayList<>();

        for(int y = (int) env.getMinY(); y < env.getMaxY(); y++) {
            for(int x = (int) env.getMinX(); x < env.getMaxX(); x++) {
                if(raster.getSampleDouble(x, y, 0) == code) {
                    // LEFT
                    if(raster.getSampleDouble(x-1, y, 0) != code) {
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x, y), new Coordinate(x, y+1)}));
                    }
                    // RIGHT
                    if(raster.getSampleDouble(x+1, y, 0) != code) {
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x+1, y), new Coordinate(x+1, y+1)}));
                    }
                    // TOP
                    if(raster.getSampleDouble(x, y-1, 0) != code) {
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x, y), new Coordinate(x+1, y)}));
                    }
                    // BOTTOM
                    if(raster.getSampleDouble(x, y+1, 0) != code) {
                        lines.add(factory.createLineString(new Coordinate[] {new Coordinate(x, y+1), new Coordinate(x+1, y+1)}));
                    }
                }
            }
        }

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();

        List<Polygon> finalPolys = new ArrayList<>();
        for(Object p : polys) {
            Polygon g = ((Polygon)p);
            double y = g.getEnvelopeInternal().getMinY();
            double minX = g.getEnvelopeInternal().getMaxX();
            for(Coordinate c : g.getCoordinates()) {
                if(c.y == y && c.x < minX) {
                    minX = c.x;
                }
            }
            if(raster.getSampleDouble((int)minX, (int)y, 0) == code) {
                finalPolys.add(g);
            }

        }

        // remove points not needed on straight line
        List<Polygon> simpPolys = new ArrayList<>();
        for(Polygon p : finalPolys) {
            LinearRing [] interior = new LinearRing[p.getNumInteriorRing()];
            for(int i = 0; i < interior.length; i++) {
                interior[i] = p.getFactory().createLinearRing(simpRing(p.getInteriorRingN(i).getCoordinates()));
            }

            simpPolys.add(p.getFactory().createPolygon(p.getFactory().createLinearRing(
                    simpRing(p.getExteriorRing().getCoordinates())), interior));
        }

        return factory.buildGeometry(simpPolys);
    }

    /**
     * Remove unneeded points from straight line.
     * Used by {@link #vectorize}
     * @param coords
     * @return new array without unneeded points
     */
    private static Coordinate [] simpRing(Coordinate[] coords) {
        ArrayList<Coordinate> newCoords = new ArrayList<>();
        Coordinate prec = coords[coords.length-1], cur = coords[0], next;
        for(int i = 1; i < coords.length; i++) {
            next = coords[i];
            if(!(next.x-cur.x == cur.x-prec.x && next.y-cur.y == cur.y-prec.y)) {
                newCoords.add(cur);
            }
            prec = cur;
            cur = next;
        }
        newCoords.add(new Coordinate(newCoords.get(0)));

        return newCoords.toArray(new Coordinate[newCoords.size()]);
    }
    
     /**
     * Simplify geometries to speed up the voronoi calculation.
     * @param patches the patches (not modified)
     * @param resolution the precision for the simplification
     * @return new features with simplified geometries
     */
    public static List<DefaultFeature> simplify(List<DefaultFeature> patches, double resolution) {
        List<DefaultFeature> simpPatches = new ArrayList<>();
        ProgressBar monitor = Config.getProgressBar("Simplify", patches.size());
        for(Feature f : patches) {
            simpPatches.add(new DefaultFeature(f.getId(), TopologyPreservingSimplifier.simplify(f.getGeometry(), resolution*1.5), null, null));
            monitor.incProgress(1);
        }
        monitor.close();

        return simpPatches;
    }

}
