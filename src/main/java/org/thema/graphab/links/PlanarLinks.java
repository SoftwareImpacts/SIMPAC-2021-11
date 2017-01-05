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

    /**
     * Creates a new empty planar topology with a given number of patches
     * @param nbPatch the number of patches
     */
    public PlanarLinks(int nbPatch) {
        conMap = new ArrayList<>(nbPatch);
        for(int i = 0; i < nbPatch; i++) {
            conMap.add(new HashMap<Integer, Path>());
        }
        links = new ArrayList<>(nbPatch*3);
    }

    /**
     * Creates the planar topology given the links between patches
     * @param paths the links between patches
     * @param nbPatch the number of patches
     */
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

    /**
     * 
     * @param patch the patch
     * @return the patch ids which are connected to the given patch
     */
    public Set<Integer> getNeighbors(Feature patch) {
        return conMap.get(getInd(patch)).keySet();
    }

    /**
     * Add a link between two patches
     * @param path the link
     */
    public synchronized void addLink(Path path) {
        conMap.get(getInd(path.getPatch1())).put((Integer)path.getPatch2().getId(), path);
        conMap.get(getInd(path.getPatch2())).put((Integer)path.getPatch1().getId(), path);
        links.add(path);
    }

    /**
     * @param f1 the first patch
     * @param f2 the second patch
     * @return true if f1 and f2 are directly connected
     */
    public boolean isLinkExist(Feature f1, Feature f2) {
        return conMap.get(getInd(f1)).containsKey(f2.getId());
    }
    
    private int getInd(Feature f) {
        return (Integer)f.getId()-1;
    }

    /**
     * @return all the links, may be empty
     */
    public List<Path> getFeatures() {
        return links;
    }

}
