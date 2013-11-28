/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.thema.drawshape.feature.Feature;


/**
 *
 * @author gvuidel
 */
public class Links {
    String name;
    List<HashMap<Integer, Path>> conMap;
    List<Path> links;

    public Links(String name, int size) {
        this.name = name;
        conMap = new ArrayList<HashMap<Integer, Path>>(size);
        for(int i = 0; i < size; i++)
            conMap.add(new HashMap<Integer, Path>());
        links = new ArrayList<Path>(size*3);
    }

    public Links(String name, List<Path> paths, int size) {
        this.name = name;
        links = paths;
        conMap = new ArrayList<HashMap<Integer, Path>>(size);
        for(int i = 0; i < size; i++)
            conMap.add(new HashMap<Integer, Path>());

        for(Path p : links) {
            conMap.get(getId(p.getPatch1())).put((Integer)p.getPatch2().getId(), p);
            conMap.get(getId(p.getPatch2())).put((Integer)p.getPatch1().getId(), p);
        }
    }

    public Set<Integer> getNeighbors(Feature f) {
        return conMap.get(getId(f)).keySet();
    }

    public synchronized void addLink(Feature f1, Feature f2, Path path) {
        conMap.get(getId(f1)).put((Integer)f2.getId(), path);
        conMap.get(getId(f2)).put((Integer)f1.getId(), path);
        links.add(path);
    }

    public boolean isLinkExist(Feature f1, Feature f2) {
        return conMap.get(getId(f1)).containsKey(f2.getId());
    }
    
    private int getId(Feature f) {
        return (Integer)f.getId()-1;
    }

    public List<Path> getFeatures() {
        return links;
    }


}
