
package org.thema.graphab.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.EdgeWeighter;
import org.thema.graphab.Project;
import org.thema.graphab.links.Path;

/**
 * Pathfinder for the graphs.
 * Calculates all paths from one node (origin) to all nodes.
 * Uses dijkstra algorithm.
 * @author Gilles Vuidel
 */
public final class GraphPathFinder {
    private Node nodeOrigin;
    private DijkstraPathFinder pathfinder;
    private HashMap<Node, DijkstraPathFinder.DijkstraNode> computedNodes;
    private final GraphGenerator graph;

    GraphPathFinder(Node nodeOrigin, GraphGenerator graph) {
        this(nodeOrigin, Double.NaN, graph);
    }

    GraphPathFinder(Node nodeOrigin, double maxCost, double alpha, GraphGenerator graph) {
        this.graph = graph;
        this.nodeOrigin = nodeOrigin;
        pathfinder = getFlowPathFinder(nodeOrigin, maxCost, alpha);
        computedNodes = new HashMap<>();
        for (DijkstraPathFinder.DijkstraNode dn : pathfinder.getComputedNodes()) {
            computedNodes.put(dn.node, dn);
        }
    }

    GraphPathFinder(Node nodeOrigin, double maxCost, final GraphGenerator graph) {
        this.graph = graph;
        this.nodeOrigin = nodeOrigin;
        pathfinder = getDijkstraPathFinder(graph.getPathNodes(nodeOrigin), maxCost);
        computedNodes = new HashMap<>();
        for (DijkstraPathFinder.DijkstraNode dn : pathfinder.getComputedNodes()) {
            if (dn.node.getObject() instanceof Node) {
                Node node = (Node) dn.node.getObject();
                DijkstraPathFinder.DijkstraNode oldDn = computedNodes.get(node);
                if (oldDn == null || dn.cost < oldDn.cost) {
                    computedNodes.put(node, dn);
                }
            } else {
                computedNodes.put(dn.node, dn);
            }
        }
    }

    /**
     * Return the cost distance from the origin node to this node.
     * @param node the destination node
     * @return the cost distance or null if the node is not connected to the origin node
     */
    public Double getCost(Node node) {
        DijkstraPathFinder.DijkstraNode dn = computedNodes.get(node);
        if (dn == null) {
            return null;
        }
        return dn.cost;
    }

    /**
     * Returns the path from the origin node to this node
     * @param node the destination node
     * @return the path or null if the node is not connected to the origin node
     */
    public org.thema.graph.pathfinder.Path getPath(Node node) {
        org.thema.graph.pathfinder.Path p = pathfinder.getPath(computedNodes.get(node));
        if (graph.isIntraPatchDist() && p != null) {
            List<Edge> edges = new ArrayList<>(p.getEdges().size() / 2 + 1);
            for (Edge e : p.getEdges()) {
                if (e.getObject() instanceof Edge) {
                    edges.add((Edge) e.getObject());
                }
            }
            p = new org.thema.graph.pathfinder.Path(nodeOrigin, edges);
        }
        return p;
    }

    /**
     * @return the origin node
     */
    public Node getNodeOrigin() {
        return nodeOrigin;
    }

    /**
     * @return all nodes connected to the origin node ie. all nodes that have a path to the origin node
     */
    public Collection<Node> getComputedNodes() {
        return computedNodes.keySet();
    }

    private DijkstraPathFinder getDijkstraPathFinder(List<Node> startNodes, double maxCost) {
        DijkstraPathFinder finder = new DijkstraPathFinder(graph.getPathGraph(), startNodes, new EdgeWeighter() {
            @Override
            public double getWeight(Edge e) {
                if (e.getObject() instanceof Path) {
                    return graph.getCost((Path) e.getObject());
                } else if (e.getObject() instanceof Edge) {
                    return graph.getCost((Edge) e.getObject());
                } else if (graph.isIntraPatchDist()) {
                    double[] w = (double[]) e.getObject();
                    return graph.getLinkset().isCostLength() ? w[0] : w[1];
                } else {
                    throw new RuntimeException("Unknown object in the graph");
                }
            }

            @Override
            public double getToGraphWeight(double dist) {
                return 0;
            }
        });
        finder.calculate(maxCost);
        return finder;
    }

    private DijkstraPathFinder getFlowPathFinder(Node startNode, double maxCost, final double alpha) {
        DijkstraPathFinder finder = new DijkstraPathFinder(graph.getGraph(), startNode, new EdgeWeighter() {
            @Override
            public double getWeight(Edge e) {
                return -Math.log(Project.getPatchCapacity(e.getNodeA()) * Project.getPatchCapacity(e.getNodeB()) / Math.pow(Project.getTotalPatchCapacity(), 2)) + alpha * ((Path) e.getObject()).getCost();
            }

            @Override
            public double getToGraphWeight(double dist) {
                return 0;
            }
        });
        finder.calculate(maxCost);
        return finder;
    }
    
}
