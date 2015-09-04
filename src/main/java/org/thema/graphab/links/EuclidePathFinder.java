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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;

/**
 * Euclidean pathfinder.
 * 
 * @author Gilles Vuidel
 */
public class EuclidePathFinder implements SpacePathFinder {

    private Project project;

    /**
     * Creates a new Euclidean pathfinder
     * @param project the current project
     */
    public EuclidePathFinder(Project project) {
        this.project = project;
    }

    @Override
    public List<double[]> calcPaths(Coordinate p, List<Coordinate> dests) {
        List<double[]> distances = new ArrayList<>();
        for(Coordinate dest : dests) {
            double d = p.distance(dest);
            distances.add(new double[]{d, d});
        }
        return distances;
    }
    
    @Override
    public HashMap<DefaultFeature, Path> calcPaths(Coordinate p, double maxCost, boolean realPath) {
        return calcPaths(new GeometryFactory().createPoint(p), maxCost, realPath);
    }
    
    @Override
    public HashMap<DefaultFeature, Path> calcPaths(Geometry geom, double maxCost, boolean realPath) {
        Collection<DefaultFeature> nearPatches = project.getPatches();
        if(maxCost > 0) {
            Envelope env = new Envelope(geom.getEnvelopeInternal());
            env.expandBy(maxCost);
            nearPatches = (List<DefaultFeature>)project.getPatchIndex().query(env);
        }
       
        DefaultFeature geomPatch = new DefaultFeature(geom.getCentroid().getCoordinate().toString(), geom);
        HashMap<DefaultFeature, Path> paths = new HashMap<>();
        for(DefaultFeature patch : nearPatches) {
            double d = patch.getGeometry().distance(geom);
            if(maxCost == 0 || d <= maxCost) {
                if(realPath) {
                    paths.put(patch, Path.createEuclidPath(geomPatch, patch));
                } else {
                    paths.put(patch, new Path(geomPatch, patch, d, d));
                }
            }
        }
        
        return paths;
    }

    /**
     * Unsupported operation !
     * @param oPatch
     * @param maxCost
     * @param realPath
     * @param all
     * @return 
     * @throws UnsupportedOperationException
     */
    @Override
    public HashMap<Feature, Path> calcPaths(Feature oPatch, double maxCost, boolean realPath, boolean all) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Unsupported operation !
     * @param oPatch
     * @param dPatch
     * @return 
     * @throws UnsupportedOperationException
     */
    @Override
    public HashMap<Feature, Path> calcPaths(Feature oPatch, Collection<Feature> dPatch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] calcPathNearestPatch(Point p) {
        Feature patch = nearestPatch(p);
        double dist = p.distance(patch.getGeometry());
        return new double[] {((Number)patch.getId()).doubleValue(), dist, dist};
    }

    private DefaultFeature nearestPatch(Point p) {
        DefaultFeature nearestPatch = null;
        double dist = project.getResolution();
        double min = Double.MAX_VALUE;
        while(min == Double.MAX_VALUE) {
            dist *= 2;
            Envelope env = new Envelope(p.getCoordinate());
            env.expandBy(dist);
            List items = project.getPatchIndex().query(env);
            for(Object item : items) {
                DefaultFeature patch = (DefaultFeature) item;
                double d = patch.getGeometry().distance(p);
                if(d < min && d <= dist) {
                    min = d;
                    nearestPatch = patch;
                }

            }
        }
        return nearestPatch;
    }
}
