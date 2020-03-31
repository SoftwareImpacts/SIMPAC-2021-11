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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.TreeSet;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicGraph;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.common.collection.HashMap2D;
import org.thema.common.collection.HashMapList;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graph.pathfinder.EdgeWeighter;
import org.thema.graph.shape.GraphGroupLayer;
import org.thema.graphab.Project;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;
import org.thema.graphab.metric.Circuit;

/**
 *
 * @author Gilles Vuidel
 */
public class GraphGenerator {

    /** Type of graph without threshold ie. keep all links of the linkset */
    public static final int COMPLETE = 1;
    /** Type of graph with an edge threshold ie. keep links which cost (or distance) is less than threshold */
    public static final int THRESHOLD = 2;
    /** Minimum Spanning Tree graph. */
    public static final int MST = 3;

    private String name;
    private Linkset cost;
    private int type;
    private double threshold;
    private boolean intraPatchDist;
    private GraphGenerator parentGraph;
    
    private boolean saved = false;
    
    protected transient List<Graph> components;
    protected transient List<DefaultFeature> compFeatures;
    protected transient Graph graph, pathGraph;
    private transient GraphGroupLayer layers;
    protected transient HashMapList<Node, Node> node2PathNodes;

    /**
     * Creates a new graph.
     * @param name name of the graph
     * @param linkset the linkset
     * @param type the type of graph : COMPLETE, THRESHOLD or MST
     * @param threshold the threshold if any
     * @param intraPatchDist use intra patch distances for path calculation ?
     */
    public GraphGenerator(String name, Linkset linkset, int type, double threshold, boolean intraPatchDist) {
        this.name = name;
        this.cost = linkset;
        this.type = type;
        this.threshold = threshold;
        // intra patch distance can be used only if linkset contains real paths
        if(intraPatchDist && !cost.isRealPaths()) {
            throw new IllegalArgumentException("Intra patch distances can be used only with a linkset containing real paths");
        }
        this.intraPatchDist = intraPatchDist;
    }

    /**
     * Dupplicates the graph gen and changes the name of the graph 
     * @param name the graph name
     * @param gen the graph to dupplicate
     */
    public GraphGenerator(String name, GraphGenerator gen) {
        this.name = name;
        this.cost = gen.cost;
        this.type = gen.type;
        this.threshold = gen.threshold;
        this.intraPatchDist = gen.intraPatchDist;
        this.parentGraph = gen;
    }
    
    /**
     * Dupplicates the graph gen and changes the name of the graph by adding prefix
     * @param gen the graph to dupplicate
     * @param prefix the name prefix
     */
    public GraphGenerator(GraphGenerator gen, String prefix) {
        this.name = prefix + "_" + gen.name;
        this.cost = gen.cost;
        this.type = gen.type;
        this.threshold = gen.threshold;
        this.intraPatchDist = gen.intraPatchDist;
        this.parentGraph = gen;
    }
    
    /**
     * Creates a new graph, removing nodes and edges contained in the 2 collections
     * @param gen the parent graph
     * @param remIdNodes collection of patches id to remove
     * @param remIdEdges ollection of links id to remove
     */
    public GraphGenerator(GraphGenerator gen, Collection remIdNodes, Collection remIdEdges) {
        this(gen, Arrays.deepToString(remIdNodes.toArray()), Arrays.deepToString(remIdEdges.toArray()));
    }
    
    /**
     * Creates a new graph, removing nodes and edges contained in the 2 strings
     * @param gen the parent graph
     * @param remIdNodes the list of patch id to remove, separated by comma
     * @param remIdEdges the list of link id to remove, separated by comma
     * @see #stringToList(java.lang.String, boolean) 
     */
    public GraphGenerator(GraphGenerator gen, String remIdNodes, String remIdEdges) {
        this.name = gen.name + "!" + remIdNodes + "!" + remIdEdges;
        this.cost = gen.cost;
        this.type = gen.type;
        this.threshold = gen.threshold;
        this.intraPatchDist = gen.intraPatchDist;
        this.graph = dupGraphWithout(stringToList(remIdNodes, true), stringToList(remIdEdges, false));
        this.parentGraph = gen;
    }
            
    /**
     * Creates a sub graph containing the indComp component only.
     * @param gen the parent graph
     * @param indComp the component index
     */
    protected GraphGenerator(GraphGenerator gen, int indComp) {
        name = gen.name;
        cost = gen.cost;
        type = gen.type;
        threshold = gen.threshold;
        intraPatchDist = gen.intraPatchDist;
        graph = gen.getComponents().get(indComp);
        components = Collections.singletonList(graph);
        this.parentGraph = gen;
    }

    /**
     * @return true if intrapatch distances are used in pathfinder, false otherwise
     */
    public boolean isIntraPatchDist() {
        return intraPatchDist;
    }

    /**
     * Creates, if needed, and returns the graph.
     * @return the graph
     */
    public synchronized Graph getGraph() {
        if(graph == null) {
            createGraph();
        }

        return graph;
    }
    
    /**
     * Creates, if needed, and returns the path graph.
     * @return the path graph if intraPatchDist = true, the graph otherwise
     */
    protected synchronized Graph getPathGraph() {
        if(pathGraph == null) {
            createPathGraph();
        }

        return pathGraph;
    }

    /**
     * Returns a list of pathgraph nodes corresponding to the graph node if intraPatchDist = true,
     * returns the same node otherwise.
     * @param node a graph node
     * @return the list of pathgraph nodes corresponding to the graph node
     */
    protected List<Node> getPathNodes(Node node) {
        if(!isIntraPatchDist()) {
            return Collections.singletonList(node);
        }
        getPathGraph();
        return node2PathNodes.get(node);
    }


    /**
     * @return all nodes of the graph
     */
    public Collection<Node> getNodes() {
        return getGraph().getNodes();
    }
    
    /**
     * @return all edges of the graph
     */
    public Collection<Edge> getEdges() {
        return getGraph().getEdges();
    }
    
    /**
     * Attention version très lente
     * Parcours tous les noeuds pour trouver le bon
     * @param patch the patch
     * @return the node representing the patch in this graph or null
     */
    public Node getNode(Feature patch) {
        for(Node n : getNodes()) {
            if(n.getObject().equals(patch)) {
                return n;
            }
        }

        return null;
    }
    
    /**
     * Attention version très lente
     * Parcours tous les noeuds pour trouver le bon
     * @param patchId the patch identifier
     * @return the node corresponding to the patch id in this graph
     * @throws NoSuchElementException if no node correspond to the patch id
     */
    public Node getNode(Integer patchId) {
        for(Node n : getNodes()) {
            if(((Feature)n.getObject()).getId().equals(patchId)) {
                return n;
            }
        }
        throw new NoSuchElementException("Patch id : " + patchId);
    }
    
    /**
     * Attention version très lente
     * Parcours tous les noeuds pour trouver le bon
     * @param linkId the link identifier
     * @return the edge corresponding to the link id in this graph
     * @throws NoSuchElementException if no edge correspond to the link id
     */
    public Edge getEdge(String linkId) {
        for(Edge e : getEdges()) {
            if(((Feature)e.getObject()).getId().equals(linkId)) {
                return e;
            }
        }
        throw new NoSuchElementException("Link id : " + linkId);
    }

    /**
     * @return all links in this graph
     */
    public List<Path> getLinks() {
        ArrayList<Path> links = new ArrayList<>();
        for(Edge e : getEdges()) {
            links.add((Path)e.getObject());
        }
        
        return links;
    }
    
    /**
     * @param edge
     * @return the cost (or distance) of this edge
     */
    public final double getCost(Edge edge) {
        return getCost((Path)edge.getObject());
    }
    
    /**
     * @param edge
     * @return the flow of this edge
     */
    public final double getFlow(Edge edge, double alpha) {
        return Project.getPatchCapacity(edge.getNodeA()) * Project.getPatchCapacity(edge.getNodeB()) / Math.pow(getProject().getTotalPatchCapacity(), 2) 
        * Math.exp(-alpha * getCost((Path)edge.getObject()));
    }
    
    /**
     * @param link
     * @return he cost (or distance) of this link
     */
    public final double getCost(Path link) {
        return cost.isCostLength() ? link.getCost() : link.getDist();
    }

    /**
     * 
     * @param i the component index (start at zero)
     * @return a new GraphGenerator representing the ith component of this graph
     */
    public GraphGenerator getComponentGraphGen(int i) {
        return new GraphGenerator(this, i);
    }

    /**
     * Creates, if needed, and returns all graph components.
     * @return all components of this graph
     */
    public synchronized List<Graph> getComponents() {
        if(components == null) {
            createComponents();
        }
        return components;
    }

    /**
     * Slow version. Test the geometry covering of each component.
     * @param patch a patch
     * @return the component feature containing the patch
     */
    public DefaultFeature getComponentFeature(Feature patch) {
        for(DefaultFeature f : getComponentFeatures()) {
            if(f.getGeometry().covers(patch.getGeometry())) {
                return f;
            }
        }
        return null;
    }

    /**
     * Creates, if needed, and returns all feature components.
     * @return all feature components of this graph
     */
    public synchronized List<DefaultFeature> getComponentFeatures() {
        if(compFeatures == null) {
            createComponents();
        }

        return compFeatures;
    } 

    /**
     * Creates and return a pathfinder from nodeOrigin
     * @param nodeOrigin the starting node
     * @return the calculated pathfinder
     */
    public GraphPathFinder getPathFinder(Node nodeOrigin) {
         return new GraphPathFinder(nodeOrigin, this);
    }
    
    /**
     * Creates and return a pathfinder from nodeOrigin and stop calculation when the cost distance exceeds maxCost
     * @param nodeOrigin the starting node
     * @param maxCost maximal cost distance
     * @return the calculated pathfinder
     */
    public GraphPathFinder getPathFinder(Node nodeOrigin, double maxCost) {
         return new GraphPathFinder(nodeOrigin, maxCost, this);
    }
    
    /**
     * Creates and return a pathfinder from nodeOrigin where weight are not cost distance but flows : -ln(ai*aj/A^2) + alpha*cost.
     * @param nodeOrigin the starting node
     * @param alpha exponential coefficient
     * @return the calculated pathfinder
     */
    public GraphPathFinder getFlowPathFinder(Node nodeOrigin, double alpha) {
         return new GraphPathFinder(nodeOrigin, Double.NaN, alpha, this);
    }
    
    /**
     * @return the sum of the area of the patch nodes
     */
    public double getPatchArea() {
        double sum = 0;
        for(Object n : getGraph().getNodes()) {
            sum += Project.getPatchArea((Node)n);
        }
        return sum;
    }
    
    /**
     * @return the sum of the capacity of the patch nodes
     */
    public double getPatchCapacity() {
        double sum = 0;
        for(Object n : getGraph().getNodes()) {
            sum += Project.getPatchCapacity((Node)n);
        }
        return sum;
    }

    /**
     * Is the shapefile of the voronoï of the graph has been saved ?
     * @param saved 
     */
    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Creates, if needed, the layers and returns them.
     * @return the layers of the graph
     */
    public synchronized GraphGroupLayer getLayers() {
        if(layers == null) {
            layers = new GraphLayers(name, this, getProject().getCRS());   
        }

        return layers;
    }

    /**
     * Creates the graph and stores it in graph.
     */
    protected void createGraph() {
        BasicGraphBuilder gen = new BasicGraphBuilder();
        HashMap<DefaultFeature, Node> patchNodes = new HashMap<>();
        for(DefaultFeature p : getProject().getPatches()) {
            Node n = gen.buildNode();
            n.setObject(p);
            gen.addNode(n);
            patchNodes.put(p, n);
        }

        for(Path p : cost.getPaths()) {
            if(type != THRESHOLD || getCost(p) <= threshold) {
                Edge e = gen.buildEdge(patchNodes.get(p.getPatch1()), patchNodes.get(p.getPatch2()));
                e.setObject(p);
                gen.addEdge(e);
            }
        }

        graph = gen.getGraph();

        if(type == MST) {
            MinSpanTree span = new MinSpanTree(graph, new EdgeWeighter() {
                @Override
                public double getWeight(Edge e) {
                    return getCost(e);
                }

                @Override
                public double getToGraphWeight(double dist) {
                    return 0;
                }
            });

            graph = span.calcMST();
        }

    }
    
    /**
     * Calculates the pathgraph used for intrapatch distances and stores it in pathGraph.
     * Stores also the mapping between nodes of the upper graph and the pathgraph nodes.
     * If this graph does not use intrapatch distances set pathGraph to graph and node2PathNodes to null
     */
    private void createPathGraph() {

        if(!intraPatchDist) {
            pathGraph = getGraph();
            node2PathNodes = null;
            return;
        }
        
        HashMap2D<Node, Coordinate, Node> coord2PathNode = new HashMap2D<>(Collections.EMPTY_SET, Collections.EMPTY_SET, null);
        
        node2PathNodes = new HashMapList<>();

        BasicGraphBuilder gen = new BasicGraphBuilder();

        for(Edge edge : getEdges()) {
            Path p = (Path) edge.getObject();
            Coordinate c = p.getCoordinate(p.getPatch1());
            Node n1 = coord2PathNode.getValue(edge.getNodeA(), c);
            if(n1 == null) {
                n1 = gen.buildNode();
                n1.setObject(edge.getNodeA());
                gen.addNode(n1);
                node2PathNodes.putValue(edge.getNodeA(), n1);
                coord2PathNode.setValue(edge.getNodeA(), c, n1);
            }
            c = p.getCoordinate(p.getPatch2());
            Node n2 = coord2PathNode.getValue(edge.getNodeB(), c);
            if(n2 == null) {
                n2 = gen.buildNode();
                n2.setObject(edge.getNodeB());
                gen.addNode(n2);
                node2PathNodes.putValue(edge.getNodeB(), n2);
                coord2PathNode.setValue(edge.getNodeB(), c, n2);
            }
            Edge e = gen.buildEdge(n1, n2);
            e.setObject(edge);
            gen.addEdge(e);
        }

        for(Node node : getNodes()) {
            // add isolated patch
            if(!node2PathNodes.containsKey(node)) {
                Node n = gen.buildNode();
                n.setObject(node);
                gen.addNode(n);
                node2PathNodes.putValue(node, n);
            } else { // link all nodes of same patch
                Map<Coordinate, Node> nodes = coord2PathNode.getLine(node);
                List<Coordinate> coords = new ArrayList<>();
                for(Coordinate c : nodes.keySet()) {
                    if(nodes.get(c) != null) {
                        coords.add(c);
                    }
                }
                for(int i = 0; i < coords.size(); i++) {
                    for(int j = i+1; j < coords.size(); j++) {
                        Coordinate c1 = coords.get(i);
                        Coordinate c2 = coords.get(j);
                        Edge e = gen.buildEdge(nodes.get(c1), nodes.get(c2));
                        double[] costs = cost.getIntraLinkCost(c1, c2);
                        if(costs == null) {
                            throw new RuntimeException("No intra patch dist for " + node.getObject());
                        }
                        e.setObject(costs);
                        gen.addEdge(e);
                    }
                }
            }
        }

        pathGraph = gen.getGraph();
    }

    /**
     * Calculates the components of the graph g.
     * @param g the graph
     * @return a list of graph, one graph for each component
     */
    public static List<Graph> partition(Graph g) {
        HashSet<Node> nodes = new HashSet<>(g.getNodes());
        List<Graph> comps = new ArrayList<>();
        while(!nodes.isEmpty()) {
            Node n = nodes.iterator().next();
            nodes.remove(n);
            List<Node> part = new ArrayList<>();
            part.add(n);

            LinkedList<Node> queue = new LinkedList<>();
            queue.add(n);
            while(!queue.isEmpty()) {
                n = queue.poll();
                Iterator it = n.getRelated();
                while(it.hasNext()) {
                    Node node = (Node) it.next();
                    if(nodes.contains(node)) {
                       nodes.remove(node);
                       part.add(node);
                       queue.add(node);
                    }

                }
            }

            HashSet<Edge> edges = new HashSet<>();
            for (Node node : part) {
                edges.addAll(node.getEdges());
            }
            comps.add(new BasicGraph(part, edges));
        }

        return comps;

    }

    private synchronized void createComponents() {
        if(saved) {
            try {
                List<DefaultFeature> features = getProject().loadVoronoiGraph(name);
                compFeatures = new ArrayList<>(features);
                components = new ArrayList<>();
                
                if(compFeatures.size() == 1) {
                    components.add(new BasicGraph(new ArrayList(getGraph().getNodes()), new ArrayList(getGraph().getEdges())));
                } else {
                    // reorder features
                    for(DefaultFeature f : features) {                    
                        compFeatures.set(((Number)f.getId()).intValue()-1, f);
                    }

                    for(Feature f : compFeatures) {
                        List<Node> nodes = new ArrayList<>();
                        HashSet<Edge> edges = new HashSet<>();
                        for(Node n : getNodes()) {
                            if(f.getGeometry().covers(Project.getPatch(n).getGeometry())) {
                                nodes.add(n);
                                edges.addAll(n.getEdges());
                            }
                        }
                        components.add(new BasicGraph(nodes, edges));
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            compFeatures = new ArrayList<>();
            components = partition(getGraph());
            int i = 1;
            for(Graph gr : components) {
                List<Geometry> geoms = new ArrayList<>();
                for(Object o : gr.getNodes()) {
                    Feature f = (Feature)((Node)o).getObject();
                    geoms.add(getProject().getVoronoi((Integer)f.getId()).getGeometry());
                }
                Geometry g = CascadedPolygonUnion.union(geoms);
                List<String> attrNames = new ArrayList<>(0);
                compFeatures.add(new DefaultFeature(i, g, attrNames, new ArrayList(0)));
                i++;
            }
        }
    }

    /**
     * Computes the distance matrix between all patches.
     * Save the result matrix in a text file
     * @param file the file for storing the matrix
     * @throws IOException 
     */
    public void calcODMatrix(File file) throws IOException {
        ProgressBar bar = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("OD_matrix"), getNodes().size());
        
        Comparator<Node> cmpPatchId = new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return ((Comparable)Project.getPatch(n1).getId()).compareTo(Project.getPatch(n2).getId());
            }
        };
        TreeSet<Node> nodes = new TreeSet<>(cmpPatchId);
        nodes.addAll(getNodes());
        try(FileWriter w = new FileWriter(file)) {
            w.write("ID");
            for (Node n1 : nodes) {
                w.write("\t" + Project.getPatch(n1).getId());
            }
            for (Node n1 : nodes) {
                w.write("\n" + Project.getPatch(n1).getId());
                GraphPathFinder pathfinder = getPathFinder(n1);
                for (Node n2 : nodes) {
                    Double c = pathfinder.getCost(n2);
                    w.write("\t" + (c == null ? Double.NaN : c));
                }
                bar.incProgress(1);
            }
        } finally {
            bar.close();
        }
    }
    
    /**
     * Computes the resistance matrix between all patches using the graph as an electric circuit.
     * Save the result matrix in a text file
     * @param file the file for storing the matrix
     * @throws IOException 
     */
    public void calcODMatrixCircuit(File file) throws IOException {
        ProgressBar bar = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("OD_matrix_circuit"), getNodes().size());
        Comparator<Node> cmpPatchId = new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return ((Comparable)Project.getPatch(n1).getId()).compareTo(Project.getPatch(n2).getId());
            }
        };
        TreeSet<Node> nodes = new TreeSet<>(cmpPatchId);
        nodes.addAll(getNodes());
        Circuit circuit = new Circuit(this);
        try(FileWriter w = new FileWriter(file)) {
            w.write("ID");
            for (Node n1 : nodes) {
                w.write("\t" + Project.getPatch(n1).getId());
            }
            for (Node n1 : nodes) {
                w.write("\n" + Project.getPatch(n1).getId());
                for (Node n2 : nodes) {
                    double r = circuit.computeR(n1, n2);
                    w.write("\t" + r);
                }
                bar.incProgress(1);
            }
        } finally {
            bar.close();
        }
        
    }
    
    /**
     * @return the name of the graph
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @return the linkset used by this graph
     */
    public Linkset getLinkset() {
        return cost;
    }

    /**
     * May be COMPLETE, THRESHOLD or MST
     * @return the type of the graph
     */
    public int getType() {
        return type;
    }

    /**
     * @return the name of the graph
     */
    public String getName() {
        return name;
    }

    /**
     * @return the edge threshold
     */
    public double getThreshold() {
        return threshold;
    }

    public Project getProject() {
        return cost.getProject();
    }

    public GraphGenerator getParentGraph() {
        return parentGraph;
    }

    /**
     * @return graph informations
     */
    public String getInfo() {
        ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/graph/Bundle");
        
        String info = bundle.getString("NewGraphDialog.nameLabel.text") + " : " + name + "\n\n"
                + bundle.getString("NewGraphDialog.linksetLabel.text") + " : " + cost.getName();
        info += "\n\nType : ";
        switch(type) {
            case COMPLETE:
                info += bundle.getString("NewGraphDialog.completeRadioButton.text");
                break;
            case THRESHOLD:
                info += bundle.getString("NewGraphDialog.thresholdRadioButton.text") + String.format(" %g", threshold);
                break;
            case MST:
                info += bundle.getString("NewGraphDialog.mstRadioButton.text");
                break;
        }
        if(intraPatchDist) {
            info += "\n\n" + bundle.getString("NewGraphDialog.intraPatchCheckBox.text");
        }
        
        info += "\n\n# " + bundle.getString("nodes") + " : " + getGraph().getNodes().size();
        info += "\n# " + bundle.getString("edges") + " : " + getGraph().getEdges().size();
        info += "\n# " + bundle.getString("components") + " : " + getComponents().size();
        
        return info;
    }
    
    /**
     * Clones the graph and removes some nodes and edges
     * @param idNodes a collection of patch id to remove
     * @param idEdges a collection of path id to remove
     * @return a dupplicated graph without idNodes and idEdges
     */
    public final Graph dupGraphWithout(Collection idNodes, Collection idEdges) {
        Graph g = getGraph();

        BasicGraphBuilder builder = new BasicGraphBuilder();
        HashMap<Node, Node> mapNodes = new HashMap<>();
        for(Node node : (Collection<Node>)g.getNodes()) {
            if(idNodes.contains(((Feature)node.getObject()).getId())) {
                continue;
            }
            Node n = builder.buildNode();
            n.setID(node.getID());
            n.setObject(node.getObject());
            builder.addNode(n);
            mapNodes.put(node, n);
        }

        for(Edge edge : (Collection<Edge>)g.getEdges()) {
            if(idEdges.contains(((Feature)edge.getObject()).getId())) {
                continue;
            }
            if(!mapNodes.containsKey(edge.getNodeA()) || !mapNodes.containsKey(edge.getNodeB())) {
                continue;
            }
            Edge e = builder.buildEdge(mapNodes.get(edge.getNodeA()), mapNodes.get(edge.getNodeB()));
            e.setID(edge.getID());
            e.setObject(edge.getObject());
            builder.addEdge(e);
        }
        return builder.getGraph();
    }

    /**
     * Inverse function of Arrays.deepToString. Convert a string to a list.
     * The element are separated by comma.
     * The string can have squared brackets, the string "[1,2]" is the same as "1,2"
     * @param sArray the string representing an array
     * @param patch if true convet elements to integer
     * @return the list of elements contained in the string
     */
    private List stringToList(String sArray, boolean patch) {
        String[] split = sArray.replace("[", "").replace("]", "").split(",");
        List lst = new ArrayList();
        for(String s : split) {
            if(s.trim().isEmpty()) {
                continue;
            }
            if(patch) {
                // for patch id
                int id = Integer.parseInt(s.trim());
                lst.add(id);
            } else {
                // for link id
                lst.add(s.trim());
            }
        }
        return lst;
    }
}
