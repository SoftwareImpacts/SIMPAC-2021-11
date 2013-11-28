/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.pointset;

import java.util.List;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.graphab.links.Linkset;

/**
 *
 * @author gvuidel
 */
public class Pointset {
    public static final int AG_NONE = 0;
    public static final int AG_SUM = 1;

    private String name;
    private Linkset cost;
    private double maxCost;
    private int agregType;

    private transient List<DefaultFeature> features;

    public Pointset(String name, Linkset cost, double maxCost, int agregType) {
        this.name = name;
        this.cost = cost;
        this.maxCost = maxCost;
        this.agregType = agregType;
    }

    public List<DefaultFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<DefaultFeature> features) {
        this.features = features;
    }

    public Linkset getCost() {
        return cost;
    }

    public double getMaxCost() {
        return maxCost;
    }

    public String getName() {
        return name;
    }

    public boolean isAgreg() {
        return agregType != AG_NONE && maxCost > 0;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public String getInfo() {
        return "Name : " + name + "\nLinks : " + cost.getName();
    }
    
}
