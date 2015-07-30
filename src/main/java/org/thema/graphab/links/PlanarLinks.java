
package org.thema.graphab.links;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.thema.data.feature.Feature;


/**
 * Stores planar topology.
 * 
 * @author Gilles Vuidel
 */
public class PlanarLinks {
    private List<HashMap<Integer, Path>> conMap;
    private List<Path> links;

    public PlanarLinks(int nbPatch) {
        conMap = new ArrayList<>(nbPatch);
        for(int i = 0; i < nbPatch; i++) {
            conMap.add(new HashMap<Integer, Path>());
        }
        links = new ArrayList<>(nbPatch*3);
    }

    public PlanarLinks(List<Path> paths, int nbPatch) {
        links = paths;
        conMap = new ArrayList<>(nbPatch);
        for(int i = 0; i < nbPatch; i++) {
            conMap.add(new HashMap<Integer, Path>());
        }

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
