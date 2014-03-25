/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
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
 *
 * @author gvuidel
 */
public class EuclidePathFinder implements SpacePathFinder {

    Project project;

    public EuclidePathFinder(Project project) {
        this.project = project;
    }

    /**
     * Calcule la distance euclidienne à partir du point p vers tous les 
     * destinations dests
     * @return les distances euclidiennes de p vers les destinations
     */
    public List<double[]> calcPaths(Coordinate p, List<Coordinate> dests) {
        List<double[]> distances = new ArrayList<double[]>();
        for(Coordinate dest : dests) {
            double d = p.distance(dest);
            distances.add(new double[]{d, d});
        }
        return distances;
    }
    
    /**
     * TODO ne crée pas le chemin réel quand realPath = true 
     * @param p
     * @param maxCost
     * @param realPath
     * @return 
     */
    public HashMap<DefaultFeature, Path> calcPaths(Coordinate p, double maxCost, boolean realPath) {
        Collection<DefaultFeature> nearPatches = project.getPatches();
        if(maxCost > 0) {
            Envelope env = new Envelope(p);
            env.expandBy(maxCost);
            nearPatches = (List<DefaultFeature>)project.getPatchIndex().query(env);
        }
       
        Point point = new GeometryFactory().createPoint(p);
        DefaultFeature pointPatch = new DefaultFeature(p.toString(), point);
        HashMap<DefaultFeature, Path> paths = new HashMap<DefaultFeature, Path>();

        for(DefaultFeature patch : nearPatches) {
            double d = patch.getGeometry().distance(point);
            if(maxCost == 0 || d <= maxCost)
                paths.put(patch, new Path(pointPatch, patch, d, d));

        }
        
        return paths;
    }

    public HashMap<Feature, Path> calcPaths(Feature oPatch, double maxCost, boolean realPath, boolean all) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HashMap<Feature, Path> calcPaths(Feature oPatch, Collection<Feature> dPatch) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
