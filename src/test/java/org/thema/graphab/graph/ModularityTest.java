/*
 * Copyright (C) 2015 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.graphab.graph.Modularity.Cluster;

/**
 *
 * @author gvuidel
 */
public class ModularityTest {
    
    private static List<Graph> graphs;
    private static List<List<Set<Integer>>> partitions;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        graphs = new ArrayList<>();
        partitions = new ArrayList<>();
        File f = new File("target/test-classes/org/thema/graphab/modularity.txt");
        BufferedReader r = new BufferedReader(new FileReader(f));
        String line = r.readLine();
        while(line != null) {
            BasicGraphBuilder gen = new BasicGraphBuilder();
            int n = Integer.parseInt(line.split("\\s+")[1]);
            List<Node> nodes = new ArrayList<>();
            for(int i = 0; i < n; i++) {
                Node node = gen.buildNode();
                node.setID(i+1);
                gen.addNode(node);
                nodes.add(node);
            }
            line = r.readLine();
            while(!line.startsWith("part")) {
                String[] tokens = line.split("\\s+");
                Edge e = gen.buildEdge(nodes.get(Integer.parseInt(tokens[0])-1), nodes.get(Integer.parseInt(tokens[1])-1));
                gen.addEdge(e);
                line = r.readLine();
            }
            graphs.add(gen.getGraph());
            List<Set<Integer>> part = new ArrayList<>();
            while(line != null && line.startsWith("part")) {
                String[] tokens = line.split("\\s+");
                Set<Integer> set = new HashSet<>();
                for(int i = 1; i < tokens.length; i++) {
                    set.add(Integer.parseInt(tokens[i]));
                }
                part.add(set);
                line = r.readLine();
            }
            partitions.add(part);
            line = r.readLine();
        }
        r.close();
        
    }
    

  

    /**
     * Test of getBestPartition method, of class Modularity.
     */
    @Test
    public void testGetBestPartition() {
        System.out.println("getBestPartition");
        for(int i = 0; i < graphs.size(); i++) {
            Graph g = graphs.get(i);
            List<Set<Integer>> partition = partitions.get(i);
            Modularity mod = new Modularity(g);
            mod.partitions();
            Set<Cluster> result = mod.getBestPartition();
            for(Cluster c : result) {
                Set<Integer> set = new HashSet<>();
                for(Node n : c.getNodes()) {
                    set.add(n.getID());
                }

                assertTrue("Wrong cluster for graph " + (i+1) + " : " + Arrays.deepToString(set.toArray()), partition.contains(set));
                
            }
        }
        
    }

    @Test 
    public void testGetModularity() {
        System.out.println("getModularity");
        for(Graph g : graphs) {
            Modularity mod = new Modularity(g);
            mod.partitions();
            Set<Cluster> result = mod.getBestPartition();
            double m = mod.getModularity(result);
            double m1 = 0, m2 = 0;
            for(Cluster c : result) {
                m1 += c.getPartModularity();
                c.init();
                m2 += c.getPartModularity();
            }
            assertEquals(m, m1, 0);
            assertEquals(m, m2, 0);
        }
    }
    
    @Test 
    public void testOptimPartition() {
        System.out.println("optimPartition");
        for(Graph g : graphs) {
            Modularity mod = new Modularity(g);
            mod.partitions();
            for(int nb : mod.getModularities().keySet()) {
                Set<Cluster> part = mod.getPartition(nb);
                Set<Cluster> optimPart = mod.getOptimPartition(nb);
                double m = mod.getModularity(part);
                double mOptim = mod.getModularity(optimPart);
                assertTrue("Optim est moins bon : " + (mOptim-m), m <= mOptim);
            }
        }
    }
}
