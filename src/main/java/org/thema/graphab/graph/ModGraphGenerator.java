
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
