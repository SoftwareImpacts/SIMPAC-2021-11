/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.graph;

import java.util.Collection;
import java.util.List;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;

/**
 * @deprecated 
 * @author gvuidel
 */
public class CompletePathFinder extends DijkstraPathFinder {

    public CompletePathFinder(Graph graph, List<Node> starts, EdgeWeighter weighter) {
        super(graph, starts, weighter);
    }

    @Override
    public Collection<DijkstraNode> getComputedNodes() {
        throw new RuntimeException("Not implemented !");
    }

    @Override
    public Double getCost(Node n) {
        if(n == this.getSource())
            return 0.0;
        Edge edge = n.getEdge((Node)getSource());
        if(edge == null || (edge instanceof DirectedEdge && n != edge.getNodeB()))
            return null;

        return weighter.getWeight(edge);
    }

    @Override
    public Node getParent(Node n) {
        throw new RuntimeException("Not implemented !");
    }

    @Override
    public org.thema.graph.pathfinder.Path getPath(Node n) {
        throw new RuntimeException("Not implemented !");
    }


    @Override
    public void calculate() {
    }

    @Override
    protected void cont(DijkstraNode current) {
    }

    @Override
    protected void init(Collection<DijkstraNode> nodes) {
//        nodemap = new HashMap<Node, DijkstraNode>();
//
//        for(DijkstraNode current : nodes) {
//            nodemap.put(current.node, current);
//            current.visited = true;
//            Iterator itr = current.node.getEdges().iterator();
//            while(itr.hasNext()) {
//                Edge e = (Edge) itr.next();
//                Node related = e.getOtherNode(current.node);
//                if(related instanceof DirectedNode && related != e.getNodeB())
//                    continue;
//                DijkstraNode reldn = nodemap.get(related);
//                if(reldn != null && reldn.visited)
//                    continue;
//                //calculate cost from current node to related node
//                double cost = weighter.getWeight(e) + current.cost + (nodeWeighter != null ? nodeWeighter.getWeight(current.node, current.from, e) : 0);
//                if (reldn == null || cost < reldn.cost) {
//                    reldn = new DijkstraNode(related, cost);
//                    nodemap.put(related, reldn);
//                    reldn.from = e;
//                }
//            }
//        }
    }

    @Override
    protected DijkstraNode next() {
        return null;
    }


}
