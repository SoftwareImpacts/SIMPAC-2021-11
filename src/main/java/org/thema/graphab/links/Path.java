/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.util.DistanceOp;

/**
 *
 * @author gib
 */
public class Path extends DefaultFeature {
    public static final String COST_ATTR = "Dist";
    public static final String DIST_ATTR = "DistM";
    private static List<String> DEFAULT_ATTR_NAMES = Arrays.asList("ID1", "ID2", COST_ATTR, DIST_ATTR);
    private static List<String> ATTR_NAMES = new ArrayList<String>(DEFAULT_ATTR_NAMES);

    private Feature patch1, patch2;
    private double cost;

    public Path(Feature patch1, Feature patch2) {
        this(patch1, patch2, 0, 0);
    }

    public Path(Feature patch1, Feature patch2, double cost, double dist) {
        this(patch1, patch2, cost, dist, ATTR_NAMES);
    }
    
    public Path(Feature patch1, Feature patch2, double cost, double dist, List<String> attrNames) {
        super(patch1.getId().toString() + "-" + patch2.getId().toString(),
                patch1.getGeometry().getFactory().createLineString(new Coordinate[] {
                    patch1.getGeometry().getCentroid().getCoordinate(),
                    patch2.getGeometry().getCentroid().getCoordinate()}),
            attrNames, new ArrayList(Arrays.asList(patch1.getId(), patch2.getId(),
                cost, dist)));
        this.patch1 = patch1;
        this.patch2 = patch2;
        this.cost = cost;
        
        for(int i = 4; i < attrNames.size(); i++)
            this.addAttribute(attrNames.get(i), null);
    }

    public Path(Feature patch1, Feature patch2, double cost, LineString path) {
        this(patch1, patch2, cost, path, ATTR_NAMES);
    }
    
    public Path(Feature patch1, Feature patch2, double cost, LineString path, List<String> attrNames) {
        super(patch1.getId().toString() + "-" + patch2.getId().toString(), path, 
                attrNames, new ArrayList(Arrays.asList(patch1.getId(), patch2.getId(), cost, path.getLength())));
        this.patch1 = patch1;
        this.patch2 = patch2;
        this.cost = cost;
        
        for(int i = 4; i < attrNames.size(); i++)
            this.addAttribute(attrNames.get(i), null);
    }

    private Path(Feature f) {
        super(f, false);
    }

    public double getCost() {
        return cost;
    }

    public double getDist() {
        return ((Number)getAttribute(DIST_ATTR)).doubleValue();
    }

    public Feature getPatch1() {
        return patch1;
    }

    public Feature getPatch2() {
        return patch2;
    }

    public Coordinate getCoordinate(Feature patch) {
        Coordinate[] coords = getGeometry().getCoordinates();
        if(getPatch1() == patch)
            return coords[0];
        else
            return coords[coords.length-1];
    }
    
    public static Feature getCommonPatch(Path p1, Path p2) {
        if(p1.getPatch1() == p2.getPatch1() || p1.getPatch1() == p2.getPatch2())
            return p1.getPatch1();
        if(p1.getPatch2() == p2.getPatch1() || p1.getPatch2() == p2.getPatch2())
            return p1.getPatch2();
        
        throw new RuntimeException("No common patch between path " + p1.getId() + " and " + p2.getId());
    }

    public static Path createEuclidPath(Feature patch1, Feature patch2) {

        Coordinate [] coords = DistanceOp.nearestPoints(patch1.getGeometry(), patch2.getGeometry());
        LineString path = patch1.getGeometry().getFactory().createLineString(coords);
        float dist = (float) path.getLength();

        return new Path(patch1, patch2, dist, path);
    }

    public static Path loadPath(Feature f, Project prj) {
        Path p = new Path(f);
        p.patch1 = prj.getPatch((Integer)f.getAttribute(ATTR_NAMES.get(0)));
        p.patch2 = prj.getPatch((Integer)f.getAttribute(Path.ATTR_NAMES.get(1)));
        p.cost = ((Number)f.getAttribute(Path.ATTR_NAMES.get(2))).floatValue();
        return p;
    }

    public static void newSetOfPaths() {
        ATTR_NAMES = new ArrayList<String>(DEFAULT_ATTR_NAMES);
    }
    public static void newSetOfPaths(List<String> attrNames) {
        ATTR_NAMES = new ArrayList<String>(DEFAULT_ATTR_NAMES);
        ATTR_NAMES.addAll(attrNames);
    }

    public static String[] serialPath(Path p) {
        String [] elems = new String[p.getAttributeNames().size()];
        elems[0] = p.patch1.getId().toString();
        elems[1] = p.patch2.getId().toString();
        elems[2] = String.valueOf(p.getCost());//String.format("%g", p.getCost());
        elems[3] = String.valueOf(p.getDist());//String.format("%g", p.getDist());
        for(int i = 4; i < p.getAttributeNames().size(); i++)
            elems[i] = p.getAttribute(i).toString();
        return elems;
    }

    public static Path deserialPath(String [] line, Project prj) {
        Feature patch1 = prj.getPatch(Integer.parseInt(line[0]));
        Feature patch2 = prj.getPatch(Integer.parseInt(line[1]));
        double cost = Double.parseDouble(line[2]);
        double dist = Double.parseDouble(line[3]);
        Path p = new Path(patch1, patch2, cost, dist);
        for(int i = 4; i < line.length; i++)
            p.addAttribute(ATTR_NAMES.get(i), Double.parseDouble(line[i]));
        return p;
    }
}
