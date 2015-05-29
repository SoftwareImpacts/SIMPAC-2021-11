
package org.thema.graphab.graph;

import java.util.Collection;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;

/**
 * A graph where removed elements (nodes or edges) can be added and removed one at a time.
 * 
 * @author Gilles Vuidel
 */
public class DeltaAddGraphGenerator extends GraphGenerator {

    private GraphGenerator gen;
    private Graphable addedElem;

    /**
     * Creates a new DeltaAddGraphGenerator.
     * Dupplicates the parent graph and removes all remIdNodes and remIdEdges
     * @param gen the parent graph
     * @param remIdNodes the collection of patches id to remove
     * @param remIdEdges the collection of links id to remove
     */
    public DeltaAddGraphGenerator(GraphGenerator gen, Collection remIdNodes, Collection remIdEdges) {
        super(gen, "Delta");
        this.gen = gen;
        addedElem = null;
        graph = gen.dupGraphWithout(remIdNodes, remIdEdges);
    }

    /**
     * Add an element (node or edge) removed from the parent graph.
     * The patch or link must be exist in the parent graph.
     * @param id the identifier of the patch or the link to add
     * @throws IllegalStateException if an element has already been added
     */
    public void addElem(Object id) {

        if(addedElem != null) {
            throw new IllegalStateException("Graph already contains an added element");
        }
        
        Graphable elem = null;
        for(Object n : gen.getGraph().getNodes()) {
            if(((Feature)((Graphable)n).getObject()).getId().equals(id)) {
                elem = (Graphable) n;
                break;
            }
        }
        if(elem == null) {
            for(Object e : gen.getGraph().getEdges()) {
                if(((Feature)((Graphable)e).getObject()).getId().equals(id)) {
                    elem = (Graphable) e;
                    break;
                }
            }
        }
        if(elem == null) {
            throw new IllegalArgumentException("Unknown elem id : " + id);
        }

        BasicGraphBuilder builder = new BasicGraphBuilder();

        if(elem instanceof Edge) {
            Edge e = (Edge) elem;
            Node nodeA = getNode((Feature)e.getNodeA().getObject());
            Node nodeB = getNode((Feature)e.getNodeB().getObject());
            Edge edge = builder.buildEdge(nodeA, nodeB);
            edge.setObject(e.getObject());
            graph.getEdges().add(edge);
            nodeA.add(edge);
            nodeB.add(edge);
            addedElem = edge;
        } else {
            Node n = (Node) elem;
            Node node = builder.buildNode();
            node.setObject(n.getObject());
            graph.getNodes().add(node);
            for(Object o : n.getEdges()) {
                Edge e = (Edge) o;
                Node nodeB = getNode((Feature)e.getOtherNode(n).getObject());
                if(nodeB == null) { // dans le cas où nodeB a aussi été enlevé
                    continue;
                }
                Edge edge = builder.buildEdge(node, nodeB);
                edge.setObject(e.getObject());
                graph.getEdges().add(edge);
                node.add(edge);
                nodeB.add(edge);
            }
            addedElem = node;
        }

        components = null;
        compFeatures = null;
        
        pathGraph = null;
        node2PathNodes = null;
    }

    /**
     * If an element has been added, removes this element from the graph.
     */
    public void reset() {
        if(addedElem instanceof Edge) {
            Edge e = (Edge) addedElem;
            graph.getEdges().remove(e);
            e.getNodeA().remove(e);
            e.getNodeB().remove(e);
        } else {
            Node n = (Node) addedElem;
            graph.getNodes().remove(n);
            for(Object o : n.getEdges()) {
                Edge e = (Edge) o;
                graph.getEdges().remove(e);
                e.getOtherNode(n).remove(e);
            }
        }

        components = null;
        compFeatures = null;
        addedElem = null;
        
        pathGraph = null;
        node2PathNodes = null;
    }

}
