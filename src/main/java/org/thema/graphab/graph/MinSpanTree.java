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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.EdgeWeighter;
import org.thema.graphab.links.Path;

/**
 * Calculate Minimum Spanning Tree
 * based on Prim's algorithm.
 * @author Gilles Vuidel
 */
public class MinSpanTree {

    private class SpanNode implements Comparable {
        private Node node;
        private Edge edge;
        private double minWeight;

        public SpanNode(Node node, Edge edge, double minWeight) {
            this.node = node;
            this.edge = edge;
            this.minWeight = minWeight;
        }
        
        @Override
        public int compareTo(Object o) {
            SpanNode n = (SpanNode) o;
            if(n == this) {
                return 0;
            }
            
            if(minWeight == n.minWeight) {
                if(edge == null && n.edge == null) {
                    return 0;
                }
                if(edge == null) {
                    return -1;
                }
                if(n.edge == null) {
                    return 1;
                }
                    
                return ((String)((Path)edge.getObject()).getId()).compareTo(((String)((Path)n.edge.getObject()).getId()));
            } else {
                return minWeight > n.minWeight ? 1 : -1;
            }
        }
        
    }

    private PriorityQueue<SpanNode> queue;
    private List<Edge> mstEdges;
    private Set<Node> passNodes;

    private Graph graph;
    private EdgeWeighter weighter;

    /**
     * Creates a new MinSpanTree based on graph and edgeweighter
     * @param graph the graph, must be connected (only one component)
     * @param weighter the edge weighter
     */
    public MinSpanTree(Graph graph, EdgeWeighter weighter) {
        this.graph = graph;
        this.weighter = weighter;
    }

    /**
     * Calculates the minimum spanning tree.
     * @return a graph representing the minimum spnning tree
     * @throws IllegalArgumentException if the graph is not connected
     */
    public Graph calcMST() {
        queue = new PriorityQueue<>();
        mstEdges = new ArrayList<>();
        passNodes = new HashSet<>();
        queue.add(new SpanNode((Node)graph.getNodes().iterator().next(), null, 0));

        while(!queue.isEmpty() && passNodes.size() < graph.getNodes().size()) {
            SpanNode sn = queue.poll();
            if(!passNodes.contains(sn.node)) {
                if(sn.edge != null) { // pour le cas initial
                    mstEdges.add(sn.edge);
                }
                passNodes.add(sn.node);
                for(Object o : sn.node.getEdges()) {
                    Edge e = (Edge) o;
                    Node node = e.getOtherNode(sn.node);
                    if(!passNodes.contains(node)) {
                        queue.add(new SpanNode(node, e, weighter.getWeight(e)));
                    }
                }
            }
        }

        if(passNodes.size() < graph.getNodes().size()) {
            throw new IllegalArgumentException("Impossible de calculer le MST complet le graphe est il connexe ?");
        }

        BasicGraphBuilder gen = new BasicGraphBuilder();

        HashMap<Node, Node> mapNodes = new HashMap<>();
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
