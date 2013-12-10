/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.graph;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.graph.shape.GraphGroupLayer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.basic.BasicGraph;
import org.thema.common.Config;
import org.thema.common.parallel.ProgressBar;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.feature.FeatureGetter;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.style.CircleStyle;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.LineStyle;
import org.thema.drawshape.style.table.FeatureAttributeCollection;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.DijkstraPathFinder.DijkstraNode;
import org.thema.graph.pathfinder.DijkstraPathFinder.EdgeWeighter;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.MainFrame;
import org.thema.graphab.links.Path;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public class GraphGenerator {

    public final class PathFinder {
        Node nodeOrigin;
        DijkstraPathFinder pathfinder;

        public PathFinder(Node nodeOrigin) {
            this(nodeOrigin, Double.NaN);
        }
        
        public PathFinder(Node nodeOrigin, double maxCost) {
            this.nodeOrigin = nodeOrigin;
            pathfinder = getDijkstraPathFinder(nodeOrigin, maxCost);
        }

//        /**
//         * create a flow pathfinder
//         * @param patchOrigin
//         * @param alpha
//         */
//        public PathFinder(Node nodeOrigin, double maxCost, double alpha) {
//            this.nodeOrigin = nodeOrigin;
//            pathfinder = getFlowPathFinder(nodeOrigin, maxCost, alpha);
//        }
        
        public Double getCost(Node n) {
            Double cost = pathfinder.getCost(n);
            if(INTRA_CENTROID && intraPatchDist) {
                org.thema.graph.pathfinder.Path path = getPath(n);
                double d = ((Path)path.getEdges().get(0).getObject()).distToPatch(Project.getPatch(nodeOrigin)) +
                        ((Path)path.getEdges().get(path.getEdges().size()-1).getObject()).distToPatch(Project.getPatch(n));
                if(getLinkset().getType_dist() != Linkset.EUCLID) 
                    d *= getLinkset().getCosts()[Project.getProject().getPatchCode()] / Project.getProject().getResolution();
                cost -= d;
                if(cost < 0) cost = 0.0;
            }
            return cost;
        }
        
        public org.thema.graph.pathfinder.Path getPath(Node n) {
            return pathfinder.getPath(n);
        }

        public Collection<DijkstraNode> getComputedNodes() {
            return pathfinder.getComputedNodes();
        }
        
        public Node getNodeOrigin() {
            return nodeOrigin;
        }
    }

    public static final int COMPLETE = 1;
    public static final int THRESHOLD = 2;
    public static final int MST = 3;
    
    /**
     * Si vrai, calcule les chemins en passant par le centroide de la tache sans utiliser de nodeweighter
     * Si faux, utilise nodeweighter (pose problème en delta car chemin pas forcément optimal)
     */
    private static final boolean INTRA_CENTROID = false;
    
    String name;
    Linkset cost;
    int type;
    double threshold;
    boolean intraPatchDist;

    boolean saved = false;

    protected transient List<Graph> components;
    protected transient List<DefaultFeature> compFeatures;
    protected transient Graph graph;
    private transient GraphGroupLayer layers;

    public GraphGenerator(String name, Linkset linkset, int type, double threshold, boolean intraPatchDist) {
        this.name = name;
        this.cost = linkset;
        this.type = type;
        this.threshold = threshold;
        this.intraPatchDist = intraPatchDist;
    }

    public GraphGenerator(GraphGenerator gen, String prefix) {
        this.name = prefix + "_" + gen.name;
        this.cost = gen.cost;
        this.type = gen.type;
        this.threshold = gen.threshold;
        this.intraPatchDist = gen.intraPatchDist;
    }
    
    protected GraphGenerator() {}
    protected GraphGenerator(GraphGenerator gen, int indComp) {
        name = gen.name;
        cost = gen.cost;
        type = gen.type;
        threshold = gen.threshold;
        intraPatchDist = gen.intraPatchDist;
        graph = gen.getComponents().get(indComp);
        components = Collections.singletonList(graph);
    }

    public boolean isIntraPatchDist() {
        return intraPatchDist;
    }

    public synchronized Graph getGraph() {
        if(graph == null)
            createGraph();

        return graph;
    }

    public Collection<Node> getNodes() {
        return getGraph().getNodes();
    }
    
    public Collection<Edge> getEdges() {
        return getGraph().getEdges();
    }
    
    /**
     * Attention version très lente
     * Parcours tous les noeuds pour trouver le bon
     * @param patch
     * @return 
     */
    public Node getNode(Feature patch) {
        for(Node n : getNodes())
            if(n.getObject().equals(patch))
                return n;

        return null;
    }

    public List<Path> getLinks() {
        ArrayList<Path> links = new ArrayList<Path>();
        for(Edge e : getEdges())
            links.add((Path)e.getObject());
        
        return links;
    }
    
    public final double getCost(Edge edge) {
        return getCost((Path)edge.getObject());
    }
    
    public final double getCost(Path link) {
        return cost.isCostLength() ? link.getCost() : link.getDist();
    }

    public GraphGenerator getComponentGraphGen(int i) {
        return new GraphGenerator(this, i);
    }

    public synchronized List<Graph> getComponents() {
        if(components == null)
            components = partition(getGraph());
        return components;
    }

    public DefaultFeature getComponentFeature(Feature patch) {
        for(DefaultFeature f : getComponentFeatures())
            if(f.getGeometry().covers(patch.getGeometry()))
                return f;
        return null;
    }

    public synchronized List<DefaultFeature> getComponentFeatures() {
        if(compFeatures == null)
            createVoronoi();

        return compFeatures;
    } 

    public PathFinder getPathFinder(Node nodeOrigin) {
         return new PathFinder(nodeOrigin);
    }
    
    public PathFinder getPathFinder(Node nodeOrigin, double maxCost) {
         return new PathFinder(nodeOrigin, maxCost);
    }

//    /**
//     * return a flow pathfinder
//     * @param patchOrigin
//     * @param alpha
//     * @return
//     */
//    public PathFinder getFlowPathFinder(Node nodeOrigin, double alpha) {
//        return new PathFinder(nodeOrigin, Double.NaN, alpha);
//    }
    
    protected DijkstraPathFinder getDijkstraPathFinder(Node startNode, double maxCost) {
        DijkstraPathFinder.NodeWeighter nodeWeighter = null;
        if(!INTRA_CENTROID && intraPatchDist)
            nodeWeighter = new DijkstraPathFinder.NodeWeighter() {
                @Override
                public double getWeight(Edge fromEdge, Node n, Edge toEdge) {
                    if(fromEdge == null)
                        return 0;
                    Feature patch = (Feature) n.getObject();
                    Path from = (Path) fromEdge.getObject();
                    Path to = (Path) toEdge.getObject();
                    Coordinate [] coords = from.getGeometry().getCoordinates();
                    Coordinate c1 = coords[0];
                    if(from.getPatch2() == patch)
                        c1 = coords[coords.length-1];
                    coords = to.getGeometry().getCoordinates();
                    Coordinate c2 = coords[0];
                    if(to.getPatch2() == patch)
                        c2 = coords[coords.length-1];

                    double d = c1.distance(c2);
                    if(getLinkset().getType_dist() != Linkset.EUCLID && cost.isCostLength()) {
                        Project prj = Project.getProject();
                        d *= getLinkset().getCosts()[prj.getPatchCode()] / prj.getResolution();
                    }
                    return d;
                }
            };
        DijkstraPathFinder finder = new DijkstraPathFinder(getGraph(), startNode, new EdgeWeighter() {
            public double getWeight(Edge e) {
                Path p = (Path) e.getObject();
                double w = getCost(p);

                if(INTRA_CENTROID && intraPatchDist) {
                    Project prj = Project.getProject();
                    double d = p.distToPatch1() + p.distToPatch2();
                    if(getLinkset().getType_dist() != Linkset.EUCLID && cost.isCostLength()) 
                        w += d * getLinkset().getCosts()[prj.getPatchCode()] / prj.getResolution();
                }
                
                return w;
            }
            public double getToGraphWeight(double dist) { return 0; }
        }, nodeWeighter);

        finder.calculate(maxCost);

        return finder;
    }
    
    public double getPatchArea() {
        double sum = 0;
        for(Object n : getGraph().getNodes())
            sum += Project.getPatchArea((Node)n);
        return sum;
    }
    
    public double getPatchCapacity() {
        double sum = 0;
        for(Object n : getGraph().getNodes())
            sum += Project.getPatchCapacity((Node)n);
        return sum;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public synchronized GraphGroupLayer getLayers() {
        if(layers == null) {
            layers = new GraphGroupLayer(name, getGraph(), MainFrame.project.getCRS()) {
                CircleStyle circleStyle;
                {
                    int col = 0;
                    switch(type) {
                        case MST:
                            col = 0x7c7e40;
                            break;
                        case COMPLETE:
                            if(cost.getTopology() == Linkset.PLANAR)
                                col = 0x951012;
                            else
                                col = 0xA2705E;
                            break;
                        case THRESHOLD:
                            if(cost.getTopology() == Linkset.PLANAR)
                                col = 0x42407E;
                            else
                                col = 0x5f91a2;
                            break;
                    }
                    edgeStyle = new LineStyle(new Color(col));
                    getEdgeLayer().setStyle(edgeStyle);
                    nodeStyle = new FeatureStyle(new Color(0x951012), new Color(0x212d19));
                }
                @Override
                public JPopupMenu getContextMenu() {
                    JPopupMenu menu = super.getContextMenu();
                    menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove")) {

                        public void actionPerformed(ActionEvent e) {
                            int res = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Do_you_want_to_remove_the_graph_") + name + " ?", java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove"), JOptionPane.YES_NO_OPTION);
                            if(res != JOptionPane.YES_OPTION)
                                return;

                            MainFrame.project.removeGraph(name);
                        }
                    });

                    menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties")) {
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showMessageDialog(null, getInfo());
                        }
                    });

                    menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("OD_matrix")) {
                        public void actionPerformed(ActionEvent e) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        calcODMatrix();
                                    } catch (Exception ex) {
                                        Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
                                        JOptionPane.showMessageDialog(null, "Error : " + ex);
                                    }
                                }
                            }).start();
                        }
                    });
                    
//                    menu.add(new AbstractAction("Circuit OD") {
//                        public void actionPerformed(ActionEvent e) {
//                            ArrayList<Node> nodes = new ArrayList<Node>(getGraph().getNodes());
//                            Collections.shuffle(nodes);
//                            HashMap<Feature, Counter> flows = new BCsCircuitODLocalIndice().performCircuitOD(GraphGenerator.this, nodes.get(0), nodes.get(1));
//                            DefaultFeature.addAttribute("circuit", getLinks(), null);
//                            for(Path p : getLinks())
//                                p.setAttribute("circuit", flows.get(p).getCount());
//
//                            DefaultFeature.addAttribute("circuit", getPatches(), null);
//                            for(DefaultFeature f : getPatches())
//                                f.setAttribute("circuit", flows.get(f).getCount());
//                            
//
//                        }
//                    });
//                    menu.add(new AbstractAction("Circuit O") {
//                        public void actionPerformed(ActionEvent e) {
//
//                            BCCircuitOLocalIndice indice = new BCCircuitOLocalIndice();
//                            new ParamEditorDialog(null, indice).setVisible(true);
//                            String res = JOptionPane.showInputDialog("ID patch origin", 1);
//                            if(res == null)
//                                return;
//                            int id = Integer.parseInt(res);
//                            HashMap<Feature, BCCircuitOLocalIndice.Counter> flows = indice.performCircuitO(GraphGenerator.this, GraphGenerator.this.getNode(Project.getProject().getPatch(id)));
//                            DefaultFeature.addAttribute(indice.getDetailName(), getLinks(), null);
//                            for(Path p : getLinks())
//                                p.setAttribute(indice.getDetailName(), flows.get(p).getCount());
//
//                            DefaultFeature.addAttribute(indice.getDetailName(), getPatches(), null);
//                            for(DefaultFeature f : getPatches())
//                                f.setAttribute(indice.getDetailName(), flows.get(f).getCount());
//                            
//
//                        }
//                    });

                    return menu;
                }

                @Override
                protected void createTopoLayers() {
                    super.createTopoLayers();
                    if(circleStyle == null) {
                        Number max = Collections.max(new FeatureAttributeCollection<Double>(getNodeLayer().getFeatures(), Project.CAPA_ATTR));
                        Number min = Collections.min(new FeatureAttributeCollection<Double>(getNodeLayer().getFeatures(), Project.CAPA_ATTR));
                        circleStyle = new CircleStyle(Project.CAPA_ATTR, min.doubleValue(), max.doubleValue(), new Color(0xcbcba7/*0x951012*/), new Color(0x212d19));
                    } else
                        circleStyle.setStyle(nodeStyle);
                    getNodeLayer().setStyle(circleStyle);
                }

                @Override
                protected void createSpatialLayers() {
                    nodeStyle.setStyle(circleStyle);
                    super.createSpatialLayers();
//                    getNodeLayer().setStyle(circleStyle);
                }
                

            }; 
            FeatureLayer fl = new FeatureLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Components"), new FeatureGetter() {
                    public Collection getFeatures() {
                        return getComponentFeatures();
                    }
                }, MainFrame.project.getZone(), new FeatureStyle(null, Color.BLACK), MainFrame.project.getCRS());

            if(getGraph().getEdges().size() > 500000)
                layers.getEdgeLayer().setVisible(false);
            layers.addLayer(fl);
        }

        return layers;
    }

    protected void createGraph() {
        BasicGraphBuilder gen = new BasicGraphBuilder();
        HashMap<DefaultFeature, Node> patchNodes = new HashMap<DefaultFeature, Node>();
        for(DefaultFeature p : Project.getProject().getPatches()) {
            Node n = gen.buildNode();
            n.setObject(p);
            gen.addNode(n);
            patchNodes.put(p, n);
        }

        for(Path p : cost.getPaths())
            if(type != THRESHOLD || getCost(p) <= threshold) {
                Edge e = gen.buildEdge(patchNodes.get(p.getPatch1()), patchNodes.get(p.getPatch2()));
                e.setObject(p);
                gen.addEdge(e);
            }

        graph = gen.getGraph();

        if(type == MST) {
            MinSpanTree span = new MinSpanTree(graph, new MinSpanTree.Weighter() {
                public double getWeight(Edge e) {
                    return getCost(e);
                }
            });

            graph = span.calcMST();
        }

    }

    protected List<Graph> partition(Graph g) {
        HashSet<Node> nodes = new HashSet<Node>(g.getNodes());
        List<Graph> comps = new ArrayList<Graph>();
        while(!nodes.isEmpty()) {
            Node n = nodes.iterator().next();
            nodes.remove(n);
            List<Node> part = new ArrayList<Node>();
            part.add(n);

            LinkedList<Node> queue = new LinkedList<Node>();
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

            HashSet<Edge> edges = new HashSet<Edge>();
            for (Node node : part)
                edges.addAll(node.getEdges());
            comps.add(new BasicGraph(part, edges));
        }

        return comps;

    }

    private void createVoronoi() {
        if(saved)
            try {
                List<DefaultFeature> features = Project.getProject().loadVoronoiGraph(name);
                // reorder features
                compFeatures = new ArrayList<DefaultFeature>(features);
                for(DefaultFeature f : features)
                    compFeatures.set(((Number)f.getId()).intValue()-1, f);
            } catch (IOException ex) {
                Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        else {
            compFeatures = new ArrayList<DefaultFeature>();

            int i = 1;
            for(Graph gr : getComponents()) {
                List<Geometry> geoms = new ArrayList<Geometry>();
                for(Object o : gr.getNodes()) {
                    Feature f = (Feature)((Node)o).getObject();
                    geoms.add(MainFrame.project.getVoronoi((Integer)f.getId()).getGeometry());
                }
                Geometry g = CascadedPolygonUnion.union(geoms);
                List<String> attrNames = new ArrayList<String>(0);
                compFeatures.add(new DefaultFeature(i, g, attrNames, new ArrayList(0)));
                i++;
            }
        }
    }

    public void calcODMatrix() throws IOException {
        ProgressBar bar = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("OD_matrix"), getNodes().size());
        FileWriter w = new FileWriter(new File(Project.getProject().getProjectDir(), getName() + "-odmatrix.txt"));
        Comparator<Node> cmpPatchId = new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return ((Comparable)Project.getPatch(n1).getId()).compareTo(Project.getPatch(n2).getId());
            }
        };
        TreeSet<Node> nodes = new TreeSet<Node>(cmpPatchId);
        nodes.addAll(getNodes());
        
        w.write("ID");
        for (Node n1 : nodes)
            w.write("\t" + Project.getPatch(n1).getId());
        for (Node n1 : nodes) {
            w.write("\n" + Project.getPatch(n1).getId());
            PathFinder pathfinder = getPathFinder(n1);
            for (Node n2 : nodes) {
                Double c = pathfinder.getCost(n2);
                w.write("\t" + (c == null ? Double.NaN : c));
            }
            bar.incProgress(1);
        }
        w.close();
        bar.close();
    }
    
    @Override
    public String toString() {
        return name;
    }

    public Linkset getLinkset() {
        return cost;
    }

    public int getDist_type() {
        return cost.getType_length();
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getThreshold() {
        return threshold;
    }

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
        if(intraPatchDist)
            info += "\n\n" + bundle.getString("NewGraphDialog.intraPatchCheckBox.text");
        
        info += "\n\n# edges : " + getGraph().getEdges().size();
        
        return info;
    }
    
    public Graph dupGraphWithout(Collection idNodes, Collection idEdges) {
        Graph g = getGraph();

        BasicGraphBuilder builder = new BasicGraphBuilder();
        HashMap<Node, Node> mapNodes = new HashMap<Node, Node>();
        for(Node node : (Collection<Node>)g.getNodes()) {
            if(idNodes.contains(((Feature)node.getObject()).getId()))
                continue;
            Node n = builder.buildNode();
            n.setID(node.getID());
            n.setObject(node.getObject());
            builder.addNode(n);
            mapNodes.put(node, n);
        }

        for(Edge edge : (Collection<Edge>)g.getEdges()) {
            if(idEdges.contains(((Feature)edge.getObject()).getId()))
                continue;
            if(!mapNodes.containsKey(edge.getNodeA()) || !mapNodes.containsKey(edge.getNodeB()))
                continue;
            Edge e = builder.buildEdge(mapNodes.get(edge.getNodeA()), mapNodes.get(edge.getNodeB()));
            e.setID(edge.getID());
            e.setObject(edge.getObject());
            builder.addEdge(e);
        }
        return builder.getGraph();
    }

}
