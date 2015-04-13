
package org.thema.graphab.addpatch;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Path;

/**
 * GraphGenerator which can add new patch in a graph without creates it in the project (soft adding).
 * 
 * 
 * @author Gilles Vuidel
 */
public class AddPatchGraphGenerator extends GraphGenerator {
    private GraphGenerator gen;
    private Node addedElem;

    /**
     * Creates a new AddPatchGraphGenerator based on graph gen.
     * The graph gen is dupplicate.
     * @param gen the underlying graph
     */
    public AddPatchGraphGenerator(GraphGenerator gen) {
        super(gen, "AddPatch");
        this.gen = gen;
        this.addedElem = null;
                
        if(gen.isIntraPatchDist()) {
            throw new IllegalArgumentException("Intra patch distance is not supported");
        }
        
        graph = gen.dupGraphWithout(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }
    
    /**
     * Add a patch in this graph. The graph can only have one added patch.
     * Add also the links (edges) linking the patch with others.
     * @param geom the patch geometry (can be point or polygon)
     * @param capa the capacity of the patch
     * @throws IOException 
     * @throws IllegalStateException if the graph have already an added patch
     */
    public void addPatch(Geometry geom, double capa) throws IOException {
        if(addedElem != null) {
            throw new IllegalStateException("Graph already contains an added element");
        }
        
        BasicGraphBuilder builder = new BasicGraphBuilder();
        // crée le noeud
        DefaultFeature patch = Project.getProject().createPatch(geom, capa);
        Node node = builder.buildNode();
        node.setObject(patch);
        graph.getNodes().add(node);
        
        // puis crée les liens
        HashMap<DefaultFeature, Path> newLinks = getLinkset().calcNewLinks(patch);
        for(DefaultFeature d : newLinks.keySet()) {
            Path path = new Path(patch, d, newLinks.get(d).getCost(), newLinks.get(d).getDist());  
            if(getType() != THRESHOLD || getCost(path) <= getThreshold()) {
                Node nodeB = null;
                for(Node n : (Collection<Node>)getGraph().getNodes()) {
                    if(((Feature)n.getObject()).getId().equals(d.getId())) {
                        nodeB = n;
                        break;
                    }
                }
                if(nodeB == null) {
                    throw new IllegalStateException("Graph does not contain the patch node : " + d.getId());
                }
                Edge edge = builder.buildEdge(node, nodeB);
                edge.setObject(path);
                node.add(edge);
                nodeB.add(edge);
                graph.getEdges().add(edge);
            }
        }
        
        addedElem = node;
        
        components = null;
        compFeatures = null;
    }
    
    /**
     * Remove the added patch and his links.
     * {@link #addPatch } must be called before
     */
    public void reset() {
        graph.getNodes().remove(addedElem);

        for(Object o : addedElem.getEdges()) {
            Edge e = (Edge) o;
            graph.getEdges().remove(e);
            e.getOtherNode(addedElem).remove(e);
        }
        
        components = null;
        compFeatures = null;
        addedElem = null;
    }
   

    @Override
    protected void createGraph() {
        // do nothing
    }
}
