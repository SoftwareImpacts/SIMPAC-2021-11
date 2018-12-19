/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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

package org.thema.graphab;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import no.uib.cipr.matrix.Vector;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.SchemaException;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thema.common.Config;
import org.thema.common.io.IOFile;
import org.thema.data.IOImage;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.links.CircuitRaster;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;

/**
 * Test Project class
 * @author Gilles Vuidel
 */
public class ProjectTest {
    
    /** Project created in /tmp */
    private Project project;
    
    /**
     * Initialize test
     * Create the test project, linksets and graphs
     * @throws Exception 
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        // init 2 threads
        Config.setNodeClass(ProjectTest.class);
        Config.setParallelProc(2);
    }
    
    @Before
    public void beforeTest() throws Exception {
        File dir = Files.createTempDirectory("test_graphab").toFile();
        for(File f : new File("target/test-classes/org/thema/graphab/project").listFiles()) {
            if(!f.isDirectory()) {
                IOFile.copyFile(f, new File(dir, f.getName()));
            }
        }
        project = Project.loadProject(new File(dir, "test.xml"), true);
    }

    /**
     * Check patch number and sizes
     * @throws Exception 
     */
    @Test
    public void testConstructor() throws Exception {
        
        GridCoverage2D cov = IOImage.loadTiff(new File("target/test-classes/org/thema/graphab/source.tif"));
        Project prj = new Project("test", new File("/tmp"), cov, new TreeSet(Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10)), Collections.singleton(1), Double.NaN, false, 0, false);
        
        assertEquals("Number of patches", 152, prj.getPatches().size());
        double area = 0;
        for(Feature patch : prj.getPatches()) {
            area += Project.getPatchArea(patch);
        }
        assertEquals("Patches area", 7931963.04522982, area, area*1e-13);
        area = 0;
        for(Feature patch : prj.getPatches()) {
            area += patch.getGeometry().getArea();
        }
        assertEquals("Patches area", 7931963.04522982, area, area*1e-13);
    }

    /**
     * Test addLinkset method.
     * Check the number of links, sum of costs and sum of distances
     */
    @Test
    public void testAddLinkset() throws Throwable {
        HashMap<String, Integer> nbLinks = new HashMap<String, Integer>() {{
            put("plan_euclid", 399);
            put("plan_cout1", 381);
            put("plan_cout1_len", 381);
            put("plan_cout10", 304);
            put("plan_cout10_keep_links", 399);
            put("comp_euclid", 11476);
            put("comp_cout10", 605);
            put("comp_cout10_all", 11476);
            put("comp_cout10_500", 361);
            put("comp_cout10_500_nopath", 1704); // and keep links
        }};
        HashMap<String, Double> sumCosts = new HashMap<String, Double>() {{
            put("comp_cout10", 317252.9343452);
            put("comp_cout10_all", 14145418.8672805);
            put("comp_cout10_500", 73311.1909156);
            put("comp_cout10_500_nopath", 493887.4475448);
            put("comp_euclid", 8.27329868717217E7);
            put("plan_cout1", 27588.8605768);
            put("plan_cout10", 58782.0487);
            put("plan_cout10_keep_links", 78962.696132);
            put("plan_cout1_len", 27588.8605768);
            put("plan_euclid", 377561.1225223806);
        }};
        HashMap<String, Double> sumDists = new HashMap<String, Double>() {{
            put("comp_cout10", 1858377.81871097);
            put("comp_cout10_all", 99158916.904384);
            put("comp_cout10_500", 432229.9450662);
            put("comp_cout10_500_nopath", 3768785.74029268);
            put("comp_euclid", 8.27329868717217E7);
            put("plan_cout1", 386225.769698995);
            put("plan_cout10", 329736.579306309);
            put("plan_cout10_keep_links", 459194.940464);
            put("plan_cout1_len", 386225.769698995);
            put("plan_euclid", 377561.1225223806);
        }};
        
        // for circuit linkset
        CircuitRaster.errNorm = Vector.Norm.Two;
        CircuitRaster.prec = 1e-6;
        CircuitRaster.initVector = CircuitRaster.InitVector.FLAT;
        
        System.out.println("Test addLinkset / removeLinkset");
        // create linksets
        for(Linkset costDist : new ArrayList<>(project.getLinksets())) {
            project.removeLinkset(costDist, true);
            Assert.assertFalse(project.getLinksets().contains(costDist));
            
            project.addLinkset(costDist, true);
            
            assertEquals("Nb links " + costDist.getName(), nbLinks.get(costDist.getName()), costDist.getPaths().size(), 0);
            double sumCost = 0, sumDist = 0;
            for(Path p : costDist.getPaths()) {
                sumCost += p.getCost();
                sumDist += p.getDist();
            }
            assertEquals("Sum of cost " + costDist.getName(), sumCosts.get(costDist.getName()), sumCost, sumCost*1e-14);
            if(!Double.isNaN(sumDists.get(costDist.getName()))) {
                assertEquals("Sum of length " + costDist.getName(), sumDists.get(costDist.getName()), sumDist, sumDist*1e-14);
            }
        }   

    }

    /**
     * Test addLinkset method for circuit.
     * Check the number of links, sum of costs
     */
    @Test
    public void testAddCircuitLinkset() throws Throwable {
        Project prj = loadCrossProject();
        double [] costs = new double[256];
        Arrays.fill(costs, 1.0);
        Linkset linkset = new Linkset(prj, "circ", Linkset.COMPLETE, costs, null, false, 0);
        prj.addLinkset(linkset, false);
        
        assertEquals(10, linkset.getPaths().size());
        double sumCost = 0;
        for(Path p : linkset.getPaths()) {
            sumCost += p.getCost();
        }
        assertEquals(5.094316110093207, sumCost, 1e-10);
        
        
        linkset = new Linkset(prj, "circ", Linkset.PLANAR, costs, null, true, 0);
        prj.addLinkset(linkset, false);
        assertEquals(8, linkset.getPaths().size());
        sumCost = 0;
        for(Path p : linkset.getPaths()) {
            sumCost += p.getCost();
        }
        assertEquals(3.8846987460794975, sumCost, 1e-10);
    }
    
    /**
     * Test addGraph method.
     * Check the number of links, sum of costs and the number of components
     */
    @Test
    public void testAddGraph() throws Exception {
        HashMap<String, Integer> nbLinks = new HashMap<String, Integer>() {{
            put("graph_plan_euclid", 399);
            put("graph_plan_cout1_len", 381);
            put("graph_plan_cout10_mst", 151);
            put("graph_plan_cout10_300", 231);
            put("graph_comp_euclid_1000", 378);
            put("graph_comp_euclid_1000_nointra", 378);
            put("graph_comp_cout10", 605);
            put("graph_comp_cout10_500_nopath", 1704); // and keep links

        }};
        HashMap<String, Double> sumCosts = new HashMap<String, Double>() {{
            put("graph_comp_cout10", 317252.9343452);
            put("graph_comp_cout10_500_nopath", 493887.4475448);
            put("graph_comp_euclid_1000", 166109.80953905112);
            put("graph_comp_euclid_1000_nointra", 166109.80953905112);
            put("graph_plan_cout10_mst", 12503.94081);
            put("graph_plan_cout10_300", 24028.0754832);
            put("graph_plan_cout1_len", 386225.7696989946);
            put("graph_plan_euclid", 377561.1225223807);
        }};
        HashMap<String, Integer> nbComps = new HashMap<String, Integer>() {{
            put("graph_plan_euclid", 1);
            put("graph_plan_cout1_len", 1);
            put("graph_plan_cout10_mst", 1);
            put("graph_plan_cout10_300", 9);
            put("graph_comp_euclid_1000", 19);
            put("graph_comp_euclid_1000_nointra", 19);
            put("graph_comp_cout10", 1);
            put("graph_comp_cout10_500_nopath", 1); // and keep links

        }};
        System.out.println("Test addGraph / removeGraph");
        // create graphs
        for(GraphGenerator gen : new ArrayList<>(project.getGraphs())) {
            project.removeGraph(gen.getName());
            
            Assert.assertFalse(project.getGraphs().contains(gen));
            
            gen.setSaved(false);
            project.addGraph(gen, true);
       
            assertEquals("Nb links " + gen.getName(), nbLinks.get(gen.getName()), gen.getEdges().size(), 0);
            double sumCost = 0;
            for(Edge edge : gen.getEdges()) {
                sumCost += gen.getCost(edge);
            }
            assertEquals("Sum costs " + gen.getName(), sumCosts.get(gen.getName()), sumCost, sumCost*1e-14);
            //System.out.println("put(\"" + gen.getName() + "\", " + sumCost + ");");
            assertEquals("Nb components " + gen.getName(), nbComps.get(gen.getName()), gen.getComponents().size(), 0);
        }
    }
    
    /**
     * Check intra patch distances
     */
    @Test
    public void testIntraPatchDist() {
        System.out.println("Test intra patch distance");
        GraphGenerator graph = project.getGraph("graph_comp_cout10");
        Linkset linkset = project.getLinkset("comp_cout10_all");
        for(Node node : graph.getNodes()) {
            GraphPathFinder pathFinder = graph.getPathFinder(node);
            for(Path p : linkset.getPaths()) {
                if(p.getPatch1().equals(node.getObject())) {
                    assertTrue("Compare direct link with path with intrapatch between " + node.getObject() + " and " + p.getPatch2(), p.getCost() <= pathFinder.getCost(graph.getNode(p.getPatch2()))*(1+1e-11));
                } else if(p.getPatch2().equals(node.getObject())) {
                    assertTrue("Compare direct link with path with intrapatch between " + node.getObject() + " and " + p.getPatch2(), p.getCost() <= pathFinder.getCost(graph.getNode(p.getPatch1()))*(1+1e-11));
                }
            }
        }
    }

    @Test
    public void testCreateProject() throws IOException, SchemaException {
        File prjFile = project.createProject("testCreateProject", 10000);
        Project prj = Project.loadProject(prjFile, false);
        assertEquals(131, prj.getPatches().size());
        for(Feature patch : prj.getPatches()) {
            assertTrue(Project.getPatchCapacity(patch) >= 10000);
        }
    }
    
    @Test
    public void testCreateMetaPatchProject() throws IOException, SchemaException {
        GraphGenerator graph = project.getGraph("graph_comp_euclid_1000");
        File prjFile = project.createMetaPatchProject("testMetaPatchProject", graph, 0, 0);
        Project prj = Project.loadProject(prjFile, false);
        assertEquals(graph.getComponents().size(), prj.getPatches().size());
        
        prjFile = project.createMetaPatchProject("testMetaPatchProject", graph, 0, 100000);
        prj = Project.loadProject(prjFile, false);
        for(Feature patch : prj.getPatches()) {
            assertTrue(Project.getPatchArea(patch) >= 100000);
            assertTrue(Project.getPatchCapacity(patch) >= 100000);
        }
    }
    
    @Test
    public void testSetCapacity() throws IOException, SchemaException {
        CapaPatchDialog.CapaPatchParam param = new CapaPatchDialog.CapaPatchParam();
        param.calcArea = true;
        param.codeWeight = new HashMap<Integer, Double>() {{ put(1, 1.0);}};
        Assert.assertTrue(param.isArea());
        project.setCapacities(param, false);
        for(DefaultFeature patch : project.getPatches()) {
            Assert.assertEquals(Project.getPatchArea(patch), Project.getPatchCapacity(patch), 1e-5);
        }
        param.calcArea = true;
        param.codeWeight = new HashMap<Integer, Double>() {{ put(1, 2.0);}};
        Assert.assertFalse(param.isArea());
        project.setCapacities(param, false);
        for(DefaultFeature patch : project.getPatches()) {
            Assert.assertEquals(Project.getPatchArea(patch)*2, Project.getPatchCapacity(patch), Project.getPatchArea(patch)*1e-3);
        }
        
        GridCoverage2D cov = IOImage.loadTiff(new File("target/test-classes/org/thema/graphab/source.tif"));
        Project prj = new Project("test", new File("/tmp"), cov, new TreeSet(Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10)), new TreeSet(Arrays.asList(1,2)), Double.NaN, false, 0, false);
        param = new CapaPatchDialog.CapaPatchParam();
        param.calcArea = true;
        param.codeWeight = new HashMap<Integer, Double>() {{ put(1, 2.0); put(2, 2.0);}};
        Assert.assertFalse(param.isArea());
        prj.setCapacities(param, false);
        for(DefaultFeature patch : prj.getPatches()) {
            Assert.assertEquals(Project.getPatchArea(patch)*2, Project.getPatchCapacity(patch), Project.getPatchArea(patch)*1e-3);
        }
        
        prj = loadCrossProjectWithCapa();
        for(DefaultFeature patch : prj.getPatches()) {
            Assert.assertEquals((Integer)patch.getId(), Project.getPatchCapacity(patch), 1e-10);
        }
    }
    
    public static Project loadTestProject() throws IOException {
        return Project.loadProject(new File("target/test-classes/org/thema/graphab/project/test.xml"), false);
    }
    
    public static Project loadCrossProject() throws IOException {
        return Project.loadProject(new File("target/test-classes/org/thema/graphab/cross_project/cross.xml"), true);
    }
    
    public static Project loadCrossProjectWithCapa() throws IOException, SchemaException {
        Project prj = Project.loadProject(new File("target/test-classes/org/thema/graphab/cross_project/cross.xml"), true);
        CapaPatchDialog.CapaPatchParam param = new CapaPatchDialog.CapaPatchParam();
        param.importFile = new File("target/test-classes/org/thema/graphab/cross_project/capa.csv");
        param.idField="id";
        param.capaField="capa";
        prj.setCapacities(param, false);
        return prj;
    }
}
