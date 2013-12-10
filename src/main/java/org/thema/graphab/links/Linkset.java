/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import au.com.bytecode.opencsv.CSVReader;
import com.vividsolutions.jts.geom.LineString;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.GlobalDataStore;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public class Linkset {

    public static final int COMPLETE = 1;
    public static final int PLANAR = 2;

    public static final int EUCLID = 1;
    public static final int COST = 2;
    public static final int EXT_COST = 3;

    public static final int COST_LENGTH = 1;
    public static final int DIST_LENGTH = 2;


    private String name;
    private int type;
    private int type_dist;
    private int type_length;
    private double [] costs;
    private boolean realPaths;
    private boolean removeCrossPatch;

    private double distMax;
    private File extCostFile;

    private transient List<Path> paths;
    
    /**
     * Jeu de lien en distance cout
     * @param name
     * @param type
     * @param costs
     * @param type_length
     * @param realPaths
     * @param removeCrossPatch
     * @param distMax 
     */
    public Linkset(String name, int type, double[] costs, int type_length, boolean realPaths, boolean removeCrossPatch, double distMax) {
        this.name = name;
        this.type = type;
        this.type_dist = COST;
        this.type_length = type_length;
        this.costs = Arrays.copyOf(costs, costs.length);
        this.realPaths = realPaths;
        this.removeCrossPatch = removeCrossPatch;
        this.distMax = distMax;
    }

    /**
     * Jeu de lien distance euclidienne
     * @param name
     * @param type
     * @param realPaths
     * @param distMax 
     */
    public Linkset(String name, int type, boolean realPaths, double distMax) {
        this.name = name;
        this.type = type;
        this.type_dist = EUCLID;
        this.type_length = DIST_LENGTH;
        this.distMax = distMax;
        this.realPaths = realPaths;
        this.removeCrossPatch = false;
    }


    /**
     * Jeu de lien avec des couts externes
     * @param name
     * @param type
     * @param type_length
     * @param realPaths
     * @param removeCrossPatch
     * @param distMax
     * @param extCostFile 
     */
    public Linkset(String name, int type, int type_length, boolean realPaths, boolean removeCrossPatch, double distMax, File extCostFile) {
        this.name = name;
        this.type = type;
        this.type_dist = EXT_COST;
        this.type_length = type_length;
        this.realPaths = realPaths;
        this.removeCrossPatch = removeCrossPatch;
        this.distMax = distMax;
        
        String prjPath = Project.getProject().getDirectory().getAbsolutePath();
        if(extCostFile.getAbsolutePath().startsWith(prjPath)) 
            this.extCostFile = new File(extCostFile.getAbsolutePath().substring(prjPath.length()+1));
        else 
            this.extCostFile = extCostFile.getAbsoluteFile();

    }

    public double getDistMax() {
        return distMax;
    }

    public String getName() {
        return name;
    }

    public boolean isRealPaths() {
        return realPaths;
    }

    public boolean isRemoveCrossPatch() {
        return removeCrossPatch;
    }

    public int getTopology() {
        return type;
    }

    public int getType_dist() {
        return type_dist;
    }

    public int getType_length() {
        return type_length;
    }
    
    public boolean isExtCost() {
        return type_dist == EXT_COST;
    }

    public boolean isCostLength() {
        return type_length == COST_LENGTH;
    }

    public double[] getCosts() {
        return costs;
    }

    public File getExtCostFile() {
        if(extCostFile.isAbsolute())
            return extCostFile;
        else 
            return new File(Project.getProject().getDirectory(), extCostFile.getPath());
    }

    public synchronized List<Path> getPaths() {
        if(paths == null) 
            try {
                loadPaths(Project.getProject(), new TaskMonitor.EmptyMonitor());
            } catch (IOException ex) {
                Logger.getLogger(Linkset.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

    public String getInfo() {
        ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/links/Bundle");
        
        String info = bundle.getString("LinksetPanel.nameLabel.text") + " : " + name;
        info += "\n" + bundle.getString("LinksetPanel.topoPanel.border.title") + " : ";
        if(type == COMPLETE) {
            info += bundle.getString("LinksetPanel.completeRadioButton.text");
            if(distMax > 0)
                info += " " + bundle.getString("LinksetPanel.distMaxLabel.text") + " " + distMax;
        }
        else
            info += bundle.getString("LinksetPanel.planarRadioButton.text");
        info += "\n" + bundle.getString("LinksetPanel.distPanel.border.title") + " : ";
        switch(type_dist) {
            case EUCLID:
                info += bundle.getString("LinksetPanel.euclidRadioButton.text");
                break;
            case COST:
                info += bundle.getString("LinksetPanel.costRadioButton.text") + "\n";
                for(Integer code : Project.getProject().getCodes())
                    info += code + " : " + costs[code] + "\n";
                break;
            case EXT_COST:
                info += bundle.getString("LinksetPanel.rasterRadioButton.text") + "\nFile : " + extCostFile.getAbsolutePath();
                break;
        }
        
        if(type_dist != EUCLID) {
            info += "\n" + bundle.getString("LinksetPanel.impedancePanel.border.title") + " : ";
            if(type_length == COST_LENGTH)
                info += bundle.getString("LinksetPanel.costDistRadioButton.text");
            else
                info += bundle.getString("LinksetPanel.lengthRadioButton.text");
        }

        if(realPaths)
            info += "\n" + bundle.getString("LinksetPanel.realPathCheckBox.text");

        if(type_dist != EUCLID && removeCrossPatch)
            info += "\n" + bundle.getString("LinksetPanel.removeCrossPatchCheckBox.text");

        info += "\n\n# links : " + paths.size();

        return info;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public void loadPaths(Project prj, ProgressBar mon) throws IOException {
        File fCSV = new File(prj.getProjectDir().getAbsolutePath() + File.separator + name + "-links.csv");
        CSVReader r = new CSVReader(new FileReader(fCSV));
        String [] attrNames = r.readNext();
        Path.newSetOfPaths(Arrays.asList(attrNames).subList(4, attrNames.length));
        HashMap<Object, Path> map = new HashMap<Object, Path>();
        String [] tab;
        while((tab = r.readNext()) != null) {
            Path p = Path.deserialPath(tab, prj);
            map.put(p.getId(), p);
        }
        r.close();

        if(realPaths) {
            List<DefaultFeature> features = GlobalDataStore.getFeatures(
                    new File(prj.getProjectDir().getAbsolutePath(), name + "-links.shp"),
                    "Id", mon);

            for(DefaultFeature f : features)
                map.get(f.getId()).setGeometry(f.getGeometry());

        }

        paths = new ArrayList<Path>(map.values());
    }

    /**
     * Add links for the patch
     * The patch have to be added in the project before (Project.addPatch)
     * @param patch must be point geometry
     * @throws Exception 
     */
    public void addLinks(DefaultFeature patch) throws Exception {
        HashMap<DefaultFeature, Path> links = calcNewLinks(patch);
        for(DefaultFeature d : links.keySet()) 
            if(realPaths)
                paths.add(new Path(patch, d, links.get(d).getCost(), (LineString)links.get(d).getGeometry(), paths.get(0).getAttributeNames()));      
            else
                paths.add(new Path(patch, d, links.get(d).getCost(), links.get(d).getDist(), paths.get(0).getAttributeNames()));      
        
    }
    
    public HashMap<DefaultFeature, Path> calcNewLinks(DefaultFeature patch) throws Exception {
        if(type == PLANAR)
            throw new IllegalStateException("Planar topology is not supported !");
        SpacePathFinder pathfinder = Project.getProject().getPathFinder(this);
        HashMap<DefaultFeature, Path> newPaths = pathfinder.calcPaths(patch.getGeometry().getCentroid().getCoordinate(), distMax, realPaths);
        newPaths.remove(patch);
        return newPaths;
    }
    /**
     * Remove last links created for the new patch
     * Links must have been created by addLinks
     * @param patch 
     */
    public void removeLinks(DefaultFeature patch) {
        while(paths.get(paths.size()-1).getPatch1().equals(patch))
            paths.remove(paths.size()-1);
    }
}
