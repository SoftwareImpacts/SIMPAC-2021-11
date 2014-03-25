
package org.thema.graphab.pointset;

import java.util.List;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.links.Linkset;

/**
 *
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

    public Pointset(String name, Linkset linkset, double maxCost, int agregType) {
        this.name = name;
        this.cost = linkset;
        this.maxCost = maxCost;
        this.agregType = agregType;
    }

    public List<DefaultFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<DefaultFeature> features) {
        this.features = features;
    }

    public Linkset getLinkset() {
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
        return "Name : " + name + "\nLinkset : " + cost.getName();
    }
    
}
