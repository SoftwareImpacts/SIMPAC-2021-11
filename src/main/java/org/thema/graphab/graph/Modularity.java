
package org.thema.graphab.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.common.collection.HashMap2D;
import org.thema.common.collection.HashMapList;
import org.thema.common.collection.TreeMapList;
import org.thema.graphab.Project;

/**
 *
 * @author Gilles Vuidel
 */
public class Modularity {
    private final GraphGenerator gen;
    private final Graph graph;
    private final double alpha, beta;
    private double m;
    
    private TreeMapList<Double, Set<Cluster>> partitions;

    public Modularity(GraphGenerator graph, double alpha, double beta) {
        this.gen = graph;
        this.graph = graph.getGraph();
        this.alpha = alpha;
        this.beta = beta;
        m = 0;
        for(Edge e : graph.getEdges()) {
            m += getImpedance(e);
        }
        
    }
    
    public Modularity(Graph graph) {
        this.graph = graph;
        m = graph.getEdges().size();
        gen = null;
        alpha = beta = 0;
    }
    
    public double getModularity(Set<Cluster> partition) {
        double mod = 0;
        for(Cluster c : partition) {
            mod += c.getPartModularity();
        }
        return mod;
    }
    
    public TreeMap<Integer, Double> getModularities() {
        TreeMap<Integer, Double> modularities = new TreeMap<>();
        for(double v : partitions.keySet()) {
            for(Set<Cluster> part : partitions.get(v)) {
                modularities.put(part.size(), v);
            }
        }
        
        return modularities;
    }
    
    public Set<Cluster> getBestPartition() {
        return partitions.lastEntry().getValue().get(0);
    }
    
    public Set<Cluster> getOptimPartition(int nbCluster) {
        Set<Cluster> partition = getPartition(nbCluster);
        Set<Cluster> part = new HashSet<>();
        for(Cluster c : partition) {
            part.add(new Cluster(c.getId(), c.getNodes()));
        }
        optimPartition(part);
        return part;
    }
    
    public void optimPartition(Set<Cluster> partition) {
        boolean increase = true;
        while(increase) {
            increase = false;
            HashMapList<Edge, Cluster> interEdges = new HashMapList<>();
            for(Cluster c : partition) {
                for(Edge e : c.interEdges) {
                    interEdges.putValue(e, c);
                }
            }
            
            for(Edge e : new ArrayList<>(interEdges.keySet())) {
                Cluster c1 = interEdges.get(e).get(0);
                Cluster c2 = interEdges.get(e).get(1);
                double m1 = c1.getPartModularity();
                double m2 = c2.getPartModularity();
                double d1 = c1.getDiffMod(e, c2);
                double d2 = c2.getDiffMod(e, c1);
                if(d1 >= d2 && d1 > 0) {
                    c1.includeEdge(e, c2);
                    if(c2.getNodes().isEmpty()) {
                        partition.remove(c2);
                    }
                    increase = true;
                    double newM = c1.getPartModularity() + c2.getPartModularity();
                    if(newM < (m1+m2) || Math.abs((newM - (m1+m2+d1)) / newM) > 1e-4) {
                        System.err.println("mod decrease : " + (newM - (m1+m2)));
                    }
                } else if(d2 >= d1 && d2 > 0) {
                    c2.includeEdge(e, c1);
                    if(c1.getNodes().isEmpty()) {
                        partition.remove(c1);
                    }
                    increase = true;
                    double newM = c1.getPartModularity() + c2.getPartModularity();
                    if(newM < (m1+m2) || Math.abs((newM - (m1+m2+d2)) / newM) > 1e-4) {
                        System.err.println("mod decrease : " + (newM - (m1+m2)));
                    }
                }
                
               
            }
        }
       
    }
    
    public Set<Cluster> getPartition(int nbCluster) {
        for(List<Set<Cluster>> parts : partitions.values()) {
            for(Set<Cluster> part : parts) {
                if(part.size() == nbCluster) {
                    return part;
                }
            }
        }
        throw new IllegalArgumentException("No partition with " + nbCluster + " clusters.");
    }
    
    public void partitions() {
        ProgressBar progressBar = Config.getProgressBar("Clustering", graph.getNodes().size());
        progressBar.setIndeterminate(true);
        partitions = new TreeMapList<>();
        Set<Cluster> clusters = new HashSet<>();
        double mod = 0;
        int i = 1;
        for(Node n : (Collection<Node>)graph.getNodes()) {
            Cluster c = new Cluster(i++, n);
            clusters.add(c);
            mod += c.getPartModularity();
        }
        partitions.putValue(mod, clusters);
        HashMap2D<Cluster, Cluster, Double> delta = new HashMap2D<>(clusters, clusters, Double.NaN);
        for(Cluster c1 : clusters) {
            for(Cluster c2 : clusters) {
                if(c1.getId() >= c2.getId()) {
                    continue;
                }
                if(c1.isConnected(c2)) {
                    double d = c1.getDiffMod(c2);
                    delta.setValue(c1, c2, d);
                    delta.setValue(c2, c1, d);
                }
            }
        }
        
        progressBar.setIndeterminate(false);
        while(true) {
            double bestDelta = Double.NEGATIVE_INFINITY;
            Cluster [] ij = new Cluster[2];
            for(Cluster c1 : delta.getKeys1()) {
                for(Cluster c2 : delta.getLine(c1).keySet()) {
                    if(c1.getId() >= c2.getId()) {
                        continue;
                    }
                    double d = delta.getValue(c1, c2);
                    if(d > bestDelta || d == bestDelta && c1.getId() < ij[0].getId()) {
                        ij[0] = c1;
                        ij[1] = c2;
                        bestDelta = d;
                    }
                }
            }
            
            if(bestDelta == Double.NEGATIVE_INFINITY) {
                break;
            }
            
            Cluster merge = ij[0].merge(i++, ij[1]);
            clusters = new HashSet(clusters);
            clusters.removeAll(Arrays.asList(ij));
            clusters.add(merge);
            mod += bestDelta;
            
            partitions.putValue(mod, clusters);
            
            // update delta
            Set<Cluster> con = new HashSet<>(delta.getLine(ij[0]).keySet());
            con.addAll(delta.getLine(ij[1]).keySet());
            con.removeAll(Arrays.asList(ij));
            for(Cluster c : con) {
                double d = merge.getDiffMod(c);
                delta.setValue(merge, c, d);
                delta.setValue(c, merge, d);
            }
            delta.removeKey1(ij[0]);
            delta.removeKey1(ij[1]);
            delta.removeKey2(ij[0]);
            delta.removeKey2(ij[1]);
            
            progressBar.incProgress(1);
        }

        progressBar.close();
    }

    public GraphGenerator getGraphGenerator() {
        return gen;
    }
    
    private double getImpedance(Edge e) {
        if(gen != null) {
            return Math.pow(Project.getPatchCapacity(e.getNodeA()) * Project.getPatchCapacity(e.getNodeB()), beta) 
                    * Math.exp(-alpha*gen.getCost(e));
        } else {
            return 1;
        }
    }
    
    
    public final class Cluster {
        private final int id;
        private final Set<Node> nodes;
        private Set<Edge> interEdges;
        private double sumIntraEdges = -1;
        private double sumEdges = -1;

        public Cluster(int id, Node n) {
            this.id = id;
            this.nodes = new HashSet<>(1);
            nodes.add(n);
            init();
        }

        public Cluster(int id, Set<Node> nodes) {
            this.id = id;
            this.nodes = new HashSet<>(nodes);
            init();
        }
        
        public double getPartModularity() {
            return getPartModularity(sumIntraEdges, sumEdges);
        }
        
        private double getPartModularity(double intra, double sum) {
            return intra / m - Math.pow(sum / (2*m), 2);
        }
        
        public double getDiffMod(Edge edge, Cluster c) {
            if(!interEdges.contains(edge) || !c.interEdges.contains(edge)) {
                return Double.NaN;
            }
            
            Node n = nodes.contains(edge.getNodeA()) ? edge.getNodeB() : edge.getNodeA();
            double intra1 = sumIntraEdges, intra2 = c.sumIntraEdges;
            double sum1 = sumEdges, sum2 = c.sumEdges;
            for(Edge e : (List<Edge>)n.getEdges()) {
                Node otherNode = e.getOtherNode(n);
                double imp = getImpedance(e);
                if(nodes.contains(otherNode)) {
                    intra1 += imp;
                    sum1 += imp;
                    sum2 -= imp;
                } else if(c.nodes.contains(otherNode)) {
                    intra2 -= imp;
                    sum2 -= imp;
                    sum1 += imp;
                } else { // edge is connected to another cluster
                    sum2 -= imp;
                    sum1 += imp;
                }
            }
            
            return (getPartModularity(intra1, sum1) + c.getPartModularity(intra2, sum2)) - (this.getPartModularity() + c.getPartModularity());
        }

        public void includeEdge(Edge edge, Cluster c) {
            if(!interEdges.contains(edge) || !c.interEdges.contains(edge)) {
                throw new IllegalArgumentException();
            }
            Node n = nodes.contains(edge.getNodeA()) ? edge.getNodeB() : edge.getNodeA();
            c.nodes.remove(n);
            nodes.add(n);
            init();            
            c.init();
        }
        
        public Cluster merge(int newId, Cluster c) {
            Set<Node> set = new HashSet<>(nodes);
            set.addAll(c.nodes);
            Cluster merge = new Cluster(newId, set);
            return merge;
        }
        
        public double getDiffMod(Cluster c) {
            if(!isConnected(c)) {
                return Double.NaN;
            }
            double intra = this.sumIntraEdges + c.sumIntraEdges
                    + getSumCommonEdges(c);
            
            double mod = intra / m - Math.pow((this.sumEdges + c.sumEdges) / (2*m), 2);
            
            return mod - (this.getPartModularity() + c.getPartModularity());
        }
        
        public boolean isConnected(Cluster c) {
            return !getCommonEdges(c).isEmpty();
        }
        
        private double getSumCommonEdges(Cluster c) {
            Set<Edge> edges = getCommonEdges(c);
            double sum = 0;
            for(Edge e : edges) {
                sum += getImpedance(e);
            }
            return sum;
        }
        
        private Set<Edge> getCommonEdges(Cluster c) {
            Set<Edge> edges = new HashSet<>(this.interEdges);
            edges.retainAll(c.interEdges);
            return edges;
        }

        public Set<Node> getNodes() {
            return nodes;
        }

        public int getId() {
            return id;
        }
        
        void init() {
            sumIntraEdges = 0;
            sumEdges = 0;
            interEdges = new HashSet<>();
            for(Node n : nodes) {
                List<Edge> edges = (List<Edge>)n.getEdges();
                for(Edge e : edges) {
                    if(e.getNodeA() == n && nodes.contains(e.getNodeB())) {
                        sumIntraEdges += getImpedance(e);
                    }
                    
                    if(!nodes.contains(e.getNodeA()) || !nodes.contains(e.getNodeB())) {
                        interEdges.add(e);
                    }
                    
                    sumEdges += getImpedance(e);
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.id;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cluster other = (Cluster) obj;
            
            return this.id == other.id;
        }

    }
    
    
   
}
