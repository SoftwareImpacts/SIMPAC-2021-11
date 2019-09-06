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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;

/**
 * Calculates paths between patches or between points.
 * 
 * @author Gilles Vuidel
 */
public interface SpacePathFinder {

    /**
     * Calcule les distances à partir du point p vers toutes les 
     * destinations dests
     * @param p the origin coordinate
     * @param dests the destination coordinates
     * @return cost and length of paths between p and ech dests
     */
    public List<double[]> calcPaths(Coordinate p, List<Coordinate> dests);
    
    /**
     * Calcule les distances à partir du point p vers toutes les 
     * destinations dests en restant à l'intérieur de la tache
     * @param p the origin coordinate
     * @param dests the destination coordinates
     * @return cost and length of paths between p and ech dests
     */
    public List<double[]> calcPathsInsidePatch(Coordinate p, List<Coordinate> dests);
    
    /**
     * Calcule les chemins à partir du point p vers tous les patch dont la distance cout est inférieure ou égale à maxCost
     * @param p the origin coordinate
     * @param maxCost maximal distance, zero for no maximum
     * @param realPath keep the real path or just a straight line between centroid patches ?
     * @return for each destination patch the path from oPatch
     */
    public HashMap<DefaultFeature, Path> calcPaths(Coordinate p, double maxCost, boolean realPath);
    
    /**
     * Calcule les chemins à partir de la géométrie geom vers tous les patch dont
     * la distance cout est inférieure ou égale à maxCost
     * @param geom can be Point or Polygonal
     * @param maxCost maximal distance, zero for no maximum
     * @param realPath keep the real path or just a straight line between centroid patches ?
     * @return for each destination patch the path from oPatch
     */
    public HashMap<DefaultFeature, Path> calcPaths(Geometry geom, double maxCost, boolean realPath);

    
    /**
     * Calc nearest patch from point p
     * @param p the origin point
     * @return an array with id of nearest patch, cost and dist
     */
    public double [] calcPathNearestPatch(Point p);

}
