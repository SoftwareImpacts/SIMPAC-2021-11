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


package org.thema.graphab.pointset;

import java.util.List;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.links.Linkset;

/**
 * A set of points connected to nearest patch with distance defined by a linkset.
 * The class is serialized in the xml project file.
 * @author Gilles Vuidel
 */
public class Pointset {
    public static final int AG_NONE = 0;
    public static final int AG_SUM = 1;

    private final String name;
    private final Linkset cost;
    private final double maxCost;
    private final int agregType;

    private transient List<DefaultFeature> features;

    /**
     * Creates a new Pointset.
     * The calculation, saving and loading of the pointset is done by the Project class.
     * 
     * @param name the name of the pointset
     * @param linkset the linkset associated
     * @param maxCost the max distance for agregating point attributes to patches or 0
     * @param agregType the type of agregation : AG_NONE or AG_SUM
     */
    public Pointset(String name, Linkset linkset, double maxCost, int agregType) {
        this.name = name;
        this.cost = linkset;
        this.maxCost = maxCost;
        this.agregType = agregType;
    }

    /**
     * Returns the features representing the point set.
     * {@link#setFeatures} must be called before
     * @return the features representing the point set or null
     */
    public List<DefaultFeature> getFeatures() {
        return features;
    }

    /**
     * Set the features representing the point set.
     * @param features 
     */
    public void setFeatures(List<DefaultFeature> features) {
        this.features = features;
    }

    /**
     * @return the linkset associated with this point set
     */
    public Linkset getLinkset() {
        return cost;
    }

    /**
     * @return the max distance for agregating point attributes to patches or 0
     */
    public double getMaxCost() {
        return maxCost;
    }

    /**
     * @return the name of the point set
     */
    public String getName() {
        return name;
    }

    /**
     * Return true if agregType != AG_NONE and maxCost > 0
     * @return true if point set fields are summarized in patches
     */
    public boolean isAgreg() {
        return agregType != AG_NONE && maxCost > 0;
    }
    
    @Override
    public String toString() {
        return name;
    }

    /**
     * @return a String containing informations about this point set
     */
    public String getInfo() {
        return "Name : " + name + "\nLinkset : " + cost.getName();
    }
    
}
