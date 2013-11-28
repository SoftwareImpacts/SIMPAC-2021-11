/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.Feature;

/**
 *
 * @author gvuidel
 */
public interface SpacePathFinder {

    /**
     * Calcule les distances à partir du point p vers tous les 
     * destinations dests
     * @return les couts et longueurs des chemins de p vers les destinations
     */
    public List<Double[]> calcPaths(Coordinate p, List<? extends Feature> dests);
    /**
     * Calcule les chemins à partir du point p vers tous les patch dont
     * la distance cout est inférieure ou égale à maxCost
     * @return
     */
    public HashMap<DefaultFeature, Path> calcPaths(Coordinate p, double maxCost, boolean realPath);

    /**
     * Calcule les chemins à partir de oPatch vers tous les patch dont l'id est supérieur à oPatch
     * si all == false
     * @param oPatch
     * @param realPath
     * @return
     */
    public HashMap<Feature, Path> calcPaths(Feature oPatch, double maxCost, boolean realPath, boolean all);

    public HashMap<Feature, Path> calcPaths(Feature oPatch, Collection<Feature> dPatch);
    
    /**
     * Calc nearest patch from point p
     * @param p
     * @return an array with id of nearest patch, cost and dist
     */
    public double [] calcPathNearestPatch(Point p);

}
