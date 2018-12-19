/*
 * Copyright (C) 2017 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;

/**
 *
 * @author gvuidel
 */
public class CLIToolsTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private File prjDir = new File("target/test-classes/org/thema/graphab/cross_project");
    private String prjFile = "target/test-classes/org/thema/graphab/cross_project/cross.xml";
    private String landFile = "target/test-classes/org/thema/graphab/cross_project/source.tif";

    /**
     * Test of execute method, of class CLITools.
     */
    @Test
    public void testExecute1() throws Exception {
        System.out.println("execute1");

        CLITools cli = new CLITools();
        cli.execute(new String[]{"--help"});
        cli.execute(new String[]{"--advanced"});
        cli.execute(new String[]{"--metrics"});
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"-nooption"});
    }
    
    
    /**
     * Test of execute method, of class CLITools.
     */
    @Test
    public void testExecuteProject() throws Exception {
        System.out.println("executeProject");

        CLITools cli = new CLITools();
        cli.execute(new String[]{"--create", "test", landFile, "habitat=0", "simp", "dir=target/test-classes/org/thema/graphab"});
        
        cli.execute(new String[]{"--project", prjFile});
        cli.execute(new String[]{"--project", prjFile, "--show"});
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--nocmd"});

    }
    
    /**
     * Test of execute method, of class CLITools.
     */
    @Test
    public void testExecuteLinkset() throws Exception {
        System.out.println("executeLinkset");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--linkset", "distance=euclid"});
        checkAndRemoveLinkset("euclid_plan", Linkset.PLANAR, Linkset.EUCLID, 8);
        cli.execute(new String[]{"--project", prjFile, "--linkset", "distance=euclid", "complete"});
        checkAndRemoveLinkset("euclid_comp0.0", Linkset.COMPLETE, Linkset.EUCLID, 10);
        cli.execute(new String[]{"--project", prjFile, "--linkset", "distance=euclid", "name=comp2", "complete=2"});
        checkAndRemoveLinkset("comp2", Linkset.COMPLETE, Linkset.EUCLID, 4);
        cli.execute(new String[]{"--project", prjFile, "--linkset", "distance=cost", "name=plan", "0=1", "255=10"});
        checkAndRemoveLinkset("plan", Linkset.PLANAR, Linkset.COST, 8);
        cli.execute(new String[]{"--project", prjFile, "--linkset", "distance=cost", "complete", "remcrosspath", "0,255=1"});
        checkAndRemoveLinkset("cost_0_255", Linkset.COMPLETE, Linkset.COST, 8);
        cli.execute(new String[]{"--project", prjFile, "--linkset", "distance=cost", "0,255=1:1:2"});
        checkAndRemoveLinkset("cost_0_255-1.0", Linkset.PLANAR, Linkset.COST, 8);
        checkAndRemoveLinkset("cost_0_255-2.0", Linkset.PLANAR, Linkset.COST, 8);
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--linkset"});

    }
    
    private void checkAndRemoveLinkset(String name, int topo, int distance, int nbLinks) throws IOException {
        Project prj = Project.loadProject(new File(prjFile), false);
        assertTrue("Linkset exists", prj.getLinksetNames().contains(name));
        
        Linkset linkset = prj.getLinkset(name);
        assertEquals(topo, linkset.getTopology());
        assertEquals(distance, linkset.getType_dist());
        assertEquals(nbLinks, linkset.getPaths().size());
        prj.removeLinkset(linkset, true);
    }
    
    @Test
    public void testExecuteCorridor() throws Exception {
        System.out.println("executeCorridor");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--corridor", "maxcost=3"});
        File f = new File(prjDir, "Jeulien1-corridor-3.0.shp");
        assertTrue(f.exists());
        assertEquals(4, DefaultFeature.loadFeatures(f).size());
        
        cli.execute(new String[]{"--project", prjFile, "--corridor", "maxcost=3", "format=raster"});
        f = new File(prjDir, "Jeulien1-corridor-3.0.tif");
        assertTrue(f.exists());
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--corridor"});
    }
    
    /**
     * Test of execute method, of class CLITools.
     */
    @Test
    public void testExecuteGraph() throws Exception {
        System.out.println("executeGraph");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--uselinkset", "Jeulien1", "--graph"});
        checkAndRemoveGraph("comp_Jeulien1", GraphGenerator.COMPLETE, 0, 8, true);
        cli.execute(new String[]{"--project", prjFile, "--uselinkset", "Jeulien1", "--graph", "nointra", "name=comptest"});
        checkAndRemoveGraph("comptest", GraphGenerator.COMPLETE, 0, 8, false);
        cli.execute(new String[]{"--project", prjFile, "--uselinkset", "Jeulien1", "--graph", "threshold=3:2:5"});
        checkAndRemoveGraph("thresh_3.0_Jeulien1", GraphGenerator.THRESHOLD, 3, 4, true);
        checkAndRemoveGraph("thresh_5.0_Jeulien1", GraphGenerator.THRESHOLD, 5, 8, true);
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--graph", "name=test", "threshold=1:1:5"});
    }
    
    @Test
    public void testExecuteCluster() throws Exception {
        System.out.println("executeCluster");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--cluster", "d=1", "p=1"});
        checkAndRemoveGraph("mod1_all", GraphGenerator.COMPLETE, 0.0, 8, true);
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--cluster", "d=1", "p=1", "beta=1", "nb=2"});
        checkAndRemoveGraph("mod2_all", GraphGenerator.COMPLETE, 0.0, 4, true);
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--cluster"});
    }
    
    private void checkAndRemoveGraph(String name, int type, double threshold, int nbLinks, boolean intra) throws IOException {
        Project prj = Project.loadProject(new File(prjFile), false);
        assertTrue("Graph exists", prj.getGraphNames().contains(name));
        
        GraphGenerator graph = prj.getGraph(name);
        assertEquals(type, graph.getType());
        assertEquals(threshold, graph.getThreshold(), 0.0);
        assertEquals(nbLinks, graph.getEdges().size());
        assertEquals(intra, graph.isIntraPatchDist());
        prj.removeGraph(name);
    }
    
    @Test
    public void testExecuteGmetric() throws Exception {
        System.out.println("executeGmetric");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--gmetric", "IIC"});
        File f = new File(prjDir, "IIC.txt");
        assertTrue(f.exists());
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--gmetric", "EC", "d=10", "p=1"});
        f = new File(prjDir, "EC.txt");
        assertTrue(f.exists());
        try(BufferedReader r = new BufferedReader(new FileReader(f))) {
            assertArrayEquals(new String[]{"Graph", "d", "p", "EC"}, r.readLine().split("\\s+"));
            assertArrayEquals(new String[]{"all", "10.0", "1.0", "5.0"}, r.readLine().split("\\s+"));
        }
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--gmetric", "EC", "d=1:1:2", "p=1"});
        f = new File(prjDir, "EC.txt");
        assertTrue(f.exists());
        try(BufferedReader r = new BufferedReader(new FileReader(f))) {
            assertArrayEquals(new String[]{"Graph", "d", "p", "EC"}, r.readLine().split("\\s+"));
            assertArrayEquals(new String[]{"all", "1.0", "1.0", "5.0"}, r.readLine().split("\\s+"));
            assertArrayEquals(new String[]{"all", "2.0", "1.0", "5.0"}, r.readLine().split("\\s+"));
        }
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--gmetric", "PC"});
    }
    
    @Test
    public void testExecuteCmetric() throws Exception {
        System.out.println("executeCmetric");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--cmetric", "H"});
        File f = new File(prjDir, "all-voronoi.csv");
        assertTrue(f.exists());
        try(CSVReader r = new CSVReader(new FileReader(f))) {
            assertArrayEquals(new String[]{"Id", "Id", "H_all"}, r.readNext());
            assertArrayEquals(new String[]{"1", "1", "9.0"}, r.readNext());
        }
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "cross", "--cmetric", "EC", "d=1:1:2", "p=1"});
        f = new File(prjDir, "cross-voronoi.csv");
        assertTrue(f.exists());
        try(CSVReader r = new CSVReader(new FileReader(f))) {
            assertArrayEquals(new String[]{"Id", "Id", "EC_d1_p1_cross", "EC_d2_p1_cross"}, r.readNext());
            assertArrayEquals(new String[]{"1", "1", "5.0", "5.0"}, r.readNext());
        }
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--cmetric", "NO"});
    }
    
    @Test
    public void testExecuteLmetric() throws Exception {
        System.out.println("executeLmetric");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--lmetric", "Dg"});
        File f = new File(prjDir, "patches.csv");
        assertTrue(f.exists());
        try(CSVReader r = new CSVReader(new FileReader(f))) {
            assertEquals("Dg_all", r.readNext()[4]);
            assertEquals("3.0", r.readNext()[4]);
            assertEquals("3.0", r.readNext()[4]);
            assertEquals("4.0", r.readNext()[4]);
            assertEquals("3.0", r.readNext()[4]);
            assertEquals("3.0", r.readNext()[4]);
        }
        
        cli.execute(new String[]{"--project", prjFile, "--usegraph", "all", "--lmetric", "F", "d=1:1:2", "p=1", "beta=1:1:2"});
        f = new File(prjDir, "patches.csv");
        assertTrue(f.exists());
        try(CSVReader r = new CSVReader(new FileReader(f))) {
            assertArrayEquals(new String[]{"F_d1_p1_beta1_all", "F_d2_p1_beta1_all", 
                "F_d1_p1_beta2_all", "F_d2_p1_beta2_all"}, Arrays.copyOfRange(r.readNext(), 5, 9));
            assertEquals("4.0", r.readNext()[5]);
            assertEquals("4.0", r.readNext()[5]);
            assertEquals("4.0", r.readNext()[5]);
            assertEquals("4.0", r.readNext()[5]);
            assertEquals("4.0", r.readNext()[5]);
        }
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--lmetric"});
    }
    
    @Test
    public void testExecuteCapa() throws Exception {
        System.out.println("executeCapa");

        CLITools cli = new CLITools();
        
        cli.execute(new String[]{"--project", prjFile, "--capa", "area", "0=1.0"});
        Project prj = Project.loadProject(new File(prjFile), false);
        for(DefaultFeature patch : prj.getPatches()) {
            assertEquals(Project.getPatchArea(patch), Project.getPatchCapacity(patch), 1e-10);
        }
        
        cli.execute(new String[]{"--project", prjFile, "--capa", "area", "0=2.0"});
        prj = Project.loadProject(new File(prjFile), false);
        for(DefaultFeature patch : prj.getPatches()) {
            assertEquals(Project.getPatchArea(patch)*2, Project.getPatchCapacity(patch), 1e-10);
        }
        
        thrown.expect(IllegalArgumentException.class);
        cli.execute(new String[]{"--project", prjFile, "--capa", "vds"});
    }
}
