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


package org.thema.graphab.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graph.Modularity.Cluster;
import org.thema.graphab.links.Path;

/**
 * Creates a graph from the modularity partitionning.
 * Based on the parent graph and keep only intra cluster edges of the clustering.
 * @author Gilles Vuidel
 */
public class ModGraphGenerator extends GraphGenerator {
    
    private Map<Integer, Integer> clusters;
    
    /**
     * Creates a new graph based on the graph gen but keeping only intra cluster edges.
     * @param gen the parent graph
     * @param cluster the partitionning
     */
    public ModGraphGenerator(GraphGenerator gen, Set<Cluster> cluster) {
        super(gen, "mod"+cluster.size());
        clusters = new HashMap<>();
        for(Cluster c : cluster) {
            for(Node n : c.getNodes()) {
                clusters.put((Integer) Project.getPatch(n).getId(), c.getId());
            }
        }
    }
    
    @Override
    protected void createGraph() {
        BasicGraphBuilder gen = new BasicGraphBuilder();
        HashMap<Feature, Node> patchNodes = new HashMap<>();
        for(DefaultFeature p : getProject().getPatches()) {
            Node n = gen.buildNode();
            n.setObject(p);
            gen.addNode(n);
            patchNodes.put(p, n);
        }

        for(Path p : getLinkset().getPaths()) {
            if(clusters.get((Integer)p.getPatch1().getId()).equals(clusters.get((Integer)p.getPatch2().getId()))) {
                Edge e = gen.buildEdge(patchNodes.get(p.getPatch1()), patchNodes.get(p.getPatch2()));
                e.setObject(p);
                gen.addEdge(e);
            }
        }

        graph = gen.getGraph();

    }
}
