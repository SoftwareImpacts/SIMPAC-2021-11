/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.Graph;
import org.thema.graphab.links.Path;

/**
 * Calculate Minimum Spanning Tree
 * based on Prim's algorithm
 * @author gvuidel
 */
public class MinSpanTree {

    public interface Weighter {
        public double getWeight(Edge e);
    }

    class SpanNode implements Comparable {
        Node node;
        Edge edge;
        double minWeight;

        public SpanNode(Node node, Edge edge, double minWeight) {
            this.node = node;
            this.edge = edge;
            this.minWeight = minWeight;
        }
        
        public int compareTo(Object o) {
            SpanNode n = (SpanNode) o;
            if(n == this)
                return 0;
            
            if(minWeight == n.minWeight) {
                if(edge == null && n.edge == null)
                    return 0;
                if(edge == null) return -1;
                if(n.edge == null) return 1;
                    
                return ((String)((Path)edge.getObject()).getId()).compareTo(((String)((Path)n.edge.getObject()).getId()));
            } else
                return minWeight > n.minWeight ? 1 : -1;
        }
        
    }

    PriorityQueue<SpanNode> queue;
    List<Edge> mstEdges;
    Set<Node> passNodes;

    Graph graph;
    Weighter weighter;

    public MinSpanTree(Graph graph, Weighter weighter) {
        this.graph = graph;
        this.weighter = weighter;
    }

    public Graph calcMST() {
        queue = new PriorityQueue<MinSpanTree.SpanNode>();
        mstEdges = new ArrayList<Edge>();
        passNodes = new HashSet<Node>();
        queue.add(new SpanNode((Node)graph.getNodes().iterator().next(), null, 0));

        while(!queue.isEmpty() && passNodes.size() < graph.getNodes().size()) {
            SpanNode sn = queue.poll();
            if(!passNodes.contains(sn.node)) {
                if(sn.edge != null) // pour le cas initial
                    mstEdges.add(sn.edge);
                passNodes.add(sn.node);
                for(Object o : sn.node.getEdges()) {
                    Edge e = (Edge) o;
                    Node node = e.getOtherNode(sn.node);
                    if(!passNodes.contains(node))
                        queue.add(new SpanNode(node, e, weighter.getWeight(e)));
                }
            }
        }

        if(passNodes.size() < graph.getNodes().size())
            throw new RuntimeException("Impossible de calculer le MST complet le graphe est il connexe ?");

        BasicGraphBuilder gen = new BasicGraphBuilder();

        HashMap<Node, Node> mapNodes = new HashMap<Node, Node>();
        for(Node n : passNodes) {
            Node node = gen.buildNode();
            node.setObject(n.getObject());
            gen.addNode(node);
            mapNodes.put(n, node);
        }
        for(Edge e : mstEdges) {
            Edge edge = gen.buildEdge(mapNodes.get(e.getNodeA()), mapNodes.get(e.getNodeB()));
            edge.setObject(e.getObject());
            gen.addEdge(edge);
        }


        return gen.getGraph();
    }

}
