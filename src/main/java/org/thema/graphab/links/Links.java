/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.thema.data.feature.Feature;


/**
 *
 * @author gvuidel
 */
public class Links {
    List<HashMap<Integer, Path>> conMap;
    List<Path> links;

    public Links(int size) {
        conMap = new ArrayList<HashMap<Integer, Path>>(size);
        for(int i = 0; i < size; i++)
            conMap.add(new HashMap<Integer, Path>());
        links = new ArrayList<Path>(size*3);
    }

    public Links(String name, List<Path> paths, int size) {
        links = paths;
        conMap = new ArrayList<HashMap<Integer, Path>>(size);
        for(int i = 0; i < size; i++)
            conMap.add(new HashMap<Integer, Path>());

        for(Path p : links) {
            conMap.get(getInd(p.getPatch1())).put((Integer)p.getPatch2().getId(), p);
            conMap.get(getInd(p.getPatch2())).put((Integer)p.getPatch1().getId(), p);
        }
    }

    public Set<Integer> getNeighbors(Feature f) {
        return conMap.get(getInd(f)).keySet();
    }

    public synchronized void addLink(Path path) {
        conMap.get(getInd(path.getPatch1())).put((Integer)path.getPatch2().getId(), path);
        conMap.get(getInd(path.getPatch2())).put((Integer)path.getPatch1().getId(), path);
        links.add(path);
    }

    public boolean isLinkExist(Feature f1, Feature f2) {
        return conMap.get(getInd(f1)).containsKey(f2.getId());
    }
    
    private int getInd(Feature f) {
        return (Integer)f.getId()-1;
    }

    public List<Path> getFeatures() {
        return links;
    }


}
