
package org.thema.graphab.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;

/**
 * A graph where one element can be removed and put back, one at a time.
 * Used for delta metric calculation.
 * 
 * @author Gilles Vuidel
 */
public class DeltaGraphGenerator extends GraphGenerator {

    private final GraphGenerator gen;
    private Graphable removedElem;

    private Graph remComp;
    private List<Graph> addComps;

    /**
     * Creates a new DeltaGraphGenerator based on the graph gen.
     * Dupplicates the parent graph.
     * @param gen the parent graph
     */
    public DeltaGraphGenerator(GraphGenerator gen) {
        super(gen, "Delta");
        this.gen = gen;
        this.removedElem = null;
        graph = gen.dupGraphWithout(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    /**
     * @return the current removed element, a node or an edge or null if no element is currently removed.
     */
    public Graphable getRemovedElem() {
        return removedElem;
    }

    /**
     * @return the parent graph
     */
    public GraphGenerator getParentGraph() {
        return gen;
    }

    /**
     * Removes the element elem from this graph. Only one element can be removed at a time.
     * 
     * @param elem the element to remove, a node or an edge
     * @throws IllegalStateException if an element is already removed
     */
    public void removeElem(Graphable elem) {
        if(removedElem != null) {
            throw new IllegalStateException("An element is already removed");
        }
        Graph gr = null;
        for(Iterator<Graph> it = getComponents().iterator(); it.hasNext() && gr == null; ) {
            Graph g = it.next();
            if(elem instanceof Edge && g.getEdges().contains(elem) ||
                    elem instanceof Node && g.getNodes().contains(elem)) {
                gr = g;
            }
        }

        if(elem instanceof Edge) {
            Edge e = (Edge) elem;
            graph.getEdges().remove(e);
            gr.getEdges().remove(e);
            e.getNodeA().remove(e);
            e.getNodeB().remove(e);
        } else {
            Node n = (Node) elem;
            gr.getNodes().remove(n);
            graph.getNodes().remove(n);
            for(Edge e : (Collection<Edge>)n.getEdges()) {
                gr.getEdges().remove(e);
                graph.getEdges().remove(e);
                e.getOtherNode(n).remove(e);
            }
        }

        remComp = gr;
        addComps = partition(gr);
        components.remove(gr);
        components.addAll(addComps);

        compFeatures = null;
        removedElem = elem;
        
        pathGraph = null;
        node2PathNodes = null;        
    }

    /**
     * If an element has been removed, put back the element in the graph.
     */
    public void reset() {
        if(removedElem == null) {
            return;
        }
        if(removedElem instanceof Edge) {
            Edge e = (Edge) removedElem;
            graph.getEdges().add(e);
            remComp.getEdges().add(e);
            e.getNodeA().add(e);
            e.getNodeB().add(e);
        } else {
            Node n = (Node) removedElem;
            graph.getNodes().add(n);
            remComp.getNodes().add(n);
            for(Edge e : (Collection<Edge>)n.getEdges()) {
                graph.getEdges().add(e);
                remComp.getEdges().add(e);
                e.getOtherNode(n).add(e);
            }
        }

        components.removeAll(addComps);
        components.add(remComp);

        compFeatures = null;
        removedElem = null;
        remComp = null;
        addComps = null;

        pathGraph = null;
        node2PathNodes = null;
    }
    
}
