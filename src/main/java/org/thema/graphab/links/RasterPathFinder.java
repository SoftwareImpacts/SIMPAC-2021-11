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
package org.thema.graphab.links;

import java.awt.image.Raster;
import java.util.HashSet;
import org.thema.data.feature.Feature;

/**
 *
 * @author Gilles Vuidel
 */
public interface RasterPathFinder extends SpacePathFinder {
    
    /**
     * Calcule la surface en pixel autour du patch oPatch jusqu'à une distance maxCost
     * pour les codes du raster contenus dans codes
     * @param oPatch origin patch
     * @param maxCost max cost distance
     * @param codes set of codes included in sum
     * @param costWeighted if true sum is weighted by cost
     * @return
     */
    public double getNeighborhood(Feature oPatch, double maxCost, Raster rasterCode, HashSet<Integer> codes, boolean costWeighted);
    
    /**
     * Return a raster of cost distances from oPatch.<br/>
     * The raster may be smaller than landscape raster if maxCost > 0
     * @param oPatch patch origin
     * @param maxCost max cost distance, if 0 : no max
     * @return 
     */
    public Raster getDistRaster(Feature oPatch, double maxCost);
}
