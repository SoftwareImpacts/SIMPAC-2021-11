/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.util.*;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.junit.*;
import static org.junit.Assert.*;
import org.thema.common.Config;
import org.thema.common.distribute.ExecutorService;
import org.thema.common.io.IOImage;
import org.thema.common.io.tab.CSVTabReader;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.feature.Feature;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.DeltaMetricTask;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.global.DeltaPCMetric;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.graphab.metric.local.LocalMetric;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;

/**
 *
 * @author gvuidel
 */
public class ProjectTest {
    
    static GridCoverage2D coverage;
    static Project project, refPrj;
    
    public ProjectTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Throwable {
        Config.setNodeClass(ProjectTest.class);
        // load all metrics
        Project.loadPluginMetric(ProjectTest.class.getClassLoader());
        coverage = IOImage.loadTiff(new File("target/test-classes/org/thema/graphab/source.tif"));
        project = new Project("test", new File("/tmp"), coverage, new TreeSet(Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10)), 1, Double.NaN, false, 0, false);
        MainFrame.project = project;
        XStream xstream = new XStream();
        xstream.alias("Project", Project.class);
        xstream.alias("Pointset", Pointset.class);
        xstream.alias("Linkset", Linkset.class);
        xstream.alias("Graph", GraphGenerator.class);
        refPrj = (Project) xstream.fromXML(new File("target/test-classes/org/thema/graphab/TestProject.xml"));
        
        Config.setParallelProc(2);
        
        for(Linkset costDist : refPrj.getLinksets())
            project.addLinkset(costDist, true);
        
        for(GraphGenerator gen : refPrj.getGraphs()) {
            gen.setSaved(false);
            project.addGraph(gen, true);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() throws Exception {
        
        assertEquals("Number of patches", 152, project.getPatches().size());
        double area = 0;
        for(Feature patch : project.getPatches())
            area += Project.getPatchArea(patch);
        assertEquals("Patches area", 7931963.04522982, area, area*1e-13);
        area = 0;
        for(Feature patch : project.getPatches())
            area += patch.getGeometry().getArea();
        assertEquals("Patches area", 7931963.04522982, area, area*1e-13);
    }

    /**
     * Test of addLinkset method, of class Project.
     */
    @Test
    public void testAddCostDistance() throws Throwable {
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
            put("comp_euclid", 8.273298688911343E7);
            put("plan_cout1", 27588.8605768);
            put("plan_cout10", 58782.0487);
            put("plan_cout10_keep_links", 78962.696132);
            put("plan_cout1_len", 27588.8605768);
            put("plan_euclid", 377561.1236639023);
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
        
        System.out.println("Test addCostDistance");
        for(Linkset costDist : refPrj.getLinksets()) {
            assertEquals("Nb links " + costDist.getName(), nbLinks.get(costDist.getName()), costDist.getPaths().size(), 0);
            double sumCost = 0, sumDist = 0;
            for(Path p : costDist.getPaths()) {
                sumCost += p.getCost();
                sumDist += p.getDist();
            }
            assertEquals("Sum of cost " + costDist.getName(), sumCosts.get(costDist.getName()), sumCost, sumCost*1e-14);
            assertEquals("Sum of length " + costDist.getName(), sumDists.get(costDist.getName()), sumDist, sumDist*1e-14);
        }   

    }

    /**
     * Test of addGraph method, of class Project.
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
        System.out.println("Test addGraph");
        for(GraphGenerator gen : refPrj.getGraphs()) {
            assertEquals("Nb links " + gen.getName(), nbLinks.get(gen.getName()), gen.getEdges().size(), 0);
            double sumCost = 0;
            for(Edge edge : gen.getEdges())
                sumCost += gen.getCost(edge);
            assertEquals("Sum costs " + gen.getName(), sumCosts.get(gen.getName()), sumCost, sumCost*1e-14);
            //System.out.println("put(\"" + gen.getName() + "\", " + sumCost + ");");
            assertEquals("Nb components " + gen.getName(), nbComps.get(gen.getName()), gen.getComponents().size(), 0);
        }
    }
    
    @Test
    public void testIntraPatchDist() {
        System.out.println("Test intra patch distance");
        GraphGenerator graph = project.getGraph("graph_comp_cout10");
        Linkset linkset = project.getLinkset("comp_cout10_all");
        for(Node node : graph.getNodes()) {
            GraphGenerator.PathFinder pathFinder = graph.getPathFinder(node);
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
    public void testGlobalIndices() throws Exception {
        HashMap<String, Double> resIndices = new HashMap<String, Double>() {{
            put("PC_d1000_p0.05_beta1-graph_comp_cout10", 2.597275864295179E-4);
            put("PC_d1000_p0.05_beta1-graph_comp_cout10_500_nopath", 3.2880609909219453E-4);
            put("PC_d1000_p0.05_beta1-graph_comp_euclid_1000_nointra", 9.581715576493942E-5);
            put("PC_d1000_p0.05_beta1-graph_plan_cout10_300", 2.3271157678692673E-4);            
            put("PC_d1000_p0.05_beta1-graph_plan_cout10_mst", 1.9835638747391063E-4);
            put("PC_d1000_p0.05_beta1-graph_plan_cout1_len", 6.69054229355518E-5);
//            put("PC_d1000_p0.05_beta1-graph_plan_euclid", 7.045308587598926E-5);
            put("S#F_d1000_p0.05_beta1-graph_comp_cout10", 1.452507396441085E8);
            put("S#F_d1000_p0.05_beta1-graph_comp_cout10_500_nopath", 1.9303189629387736E8);
            put("S#F_d1000_p0.05_beta1-graph_comp_euclid_1000_nointra", 3.831238163096813E7);
            put("S#F_d1000_p0.05_beta1-graph_plan_cout10_300", 1.2430411376651993E8);
            put("S#F_d1000_p0.05_beta1-graph_plan_cout10_mst", 1.0202141335734332E8);
            put("S#F_d1000_p0.05_beta1-graph_plan_cout1_len", 1.815613336195109E7);
//            put("S#F_d1000_p0.05_beta1-graph_plan_euclid", 2.0163399216204323E7);
            put("E#BC_d1000_p0.05_beta1-graph_comp_cout10", 0.8198695594806273);
            put("E#BC_d1000_p0.05_beta1-graph_comp_cout10_500_nopath", 0.7872122952172801);
            put("E#BC_d1000_p0.05_beta1-graph_comp_euclid_1000_nointra", 0.7223037316571634);
            put("E#BC_d1000_p0.05_beta1-graph_plan_cout10_300", 0.8398772543774017);
            put("E#BC_d1000_p0.05_beta1-graph_plan_cout10_mst", 0.8204448608776209);
            put("E#BC_d1000_p0.05_beta1-graph_plan_cout1_len", 0.666567473284984);
//            put("E#BC_d1000_p0.05_beta1-graph_plan_euclid", 0.6424046501364302);
            put("CCP-graph_comp_cout10", 1.0);
            put("CCP-graph_comp_euclid_1000", 0.3245421246977966);
            put("ECS-graph_comp_cout10", 7931963.045229822);
            put("ECS-graph_comp_euclid_1000", 2574256.1397232935);
            put("IIC-graph_comp_cout10", 4.2943441897345247E-4);
            put("IIC-graph_comp_cout10_500_nopath", 4.885949077592436E-4);
            put("IIC-graph_comp_euclid_1000", 1.6148035259804173E-4);
            put("IIC-graph_plan_cout10_mst", 1.687794720399462E-4);
            put("IIC-graph_plan_cout1_len", 3.5384810593771507E-4);
            put("IIC-graph_plan_euclid", 3.5701558731321767E-4);
            put("MSC-graph_comp_cout10", 7931963.045229822);
            put("MSC-graph_comp_euclid_1000", 417471.73922262224);
            put("SLC-graph_comp_cout10", 7931963.045229822);
            put("SLC-graph_comp_euclid_1000", 3735409.177527936);
            put("GD-graph_comp_cout10", 3290.6959320000024);
            put("GD-graph_comp_cout10_500_nopath", 4173.5731368000015);
            put("GD-graph_comp_euclid_1000_nointra", 8817.915643013066);
            put("GD-graph_plan_cout10_mst", 6845.8151244);
            put("GD-graph_plan_cout1_len", 22123.96312195184);
//            put("GD-graph_plan_euclid", 21114.413062993004);
            put("H-graph_comp_cout10", 3652.640873015876);
            put("H-graph_comp_cout10_500_nopath", 4567.878860028861);
            put("H-graph_comp_euclid_1000_nointra", 993.4060078810077);
            put("H-graph_plan_cout10_mst", 1047.894318914176);
            put("H-graph_plan_cout1_len", 2766.785411810412);
            put("H-graph_plan_euclid", 2814.2792735042735);
            put("NC-graph_comp_cout10", 1.0);
            put("NC-graph_plan_cout10_300", 9.0);
            put("NC-graph_comp_euclid_1000", 19.0);
            put("NC-graph_comp_euclid_1000_nointra", 19.0);
        }};
        
        HashSet<String> testIndices = new HashSet<String>();
        for(String varName : resIndices.keySet()) {
            System.out.println("Test global indice : " + varName);
            String indName = varName.substring(0, varName.indexOf("-"));
            if(indName.contains("_"))
                indName = indName.substring(0, indName.indexOf("_"));
            GlobalMetric indice = Project.getGlobalMetric(indName);
            indice.setParamFromDetailName(varName.substring(0, varName.indexOf("-")));
            GraphGenerator gen = refPrj.getGraph(varName.substring(varName.indexOf("-")+1));
            GraphMetricLauncher launcher = new GraphMetricLauncher(indice, true);
            Double[] res = launcher.calcIndice(gen, new TaskMonitor.EmptyMonitor());
            double err = 1e-12;
            assertEquals("Indice " + indice.getDetailName() + "-" + gen.getName(), resIndices.get(indice.getDetailName() + "-" + gen.getName()), 
                    res[0], res[0]*err);
            testIndices.add(indName);
        }
        
        assertEquals("Check all global indices", Project.getGlobalMetricsFor(Project.Method.GLOBAL).size(), testIndices.size());
        
    }
    
    @Test
    public void testLocalIndices() throws Throwable {
        CSVTabReader r = new CSVTabReader(new File("target/test-classes/org/thema/graphab/patches.csv"));
        r.read("Id");
        HashSet<String> testIndices = new HashSet<String>();
        
        for(String varName : r.getVarNames()) {
            // pour les attributs Area, Perim et Capacity et les delta métriques
            if(!varName.contains("_") || varName.startsWith("d_")) 
                continue;
            String indName = varName.substring(0, varName.indexOf("_"));
            
            GraphGenerator gen = null;
            for(String grName : refPrj.getGraphNames()) 
                if(varName.endsWith("_"+grName))
                    gen = refPrj.getGraph(grName);
            if(gen == null)
                throw new RuntimeException("Graph not found for : " + varName);
            
            // certains calculs ne sont pas testés car trop variables :
            // - les graphes euclidiens avec dist intra taches
            // - les indices de circuit sur les graphes mst
            if(gen.getLinkset().getType_dist() == Linkset.EUCLID && gen.isIntraPatchDist() || 
                    isCircuit(indName) && gen.getType() == GraphGenerator.MST)
                continue;
            
            System.out.println("Test local indice : " + varName);
            double err = indName.equals("CBC") ? 1e-3 : isCircuit(indName) ? 1e-4 : 1e-11;
            LocalMetric indice = Project.getLocalMetric(indName);
            indice.setParamFromDetailName(varName.replace("_"+gen.getName(), ""));
            MainFrame.calcLocalIndice(new TaskMonitor.EmptyMonitor(), gen, indice, Double.NaN);
            for(Object id : r.getKeySet()) {
                double ref = (Double)r.getValue(id, varName);
                
                double delta = isCircuit(indName) && ref < 1 ? 1e-5 : ref*err;
                assertEquals(varName + " id:" + id, ref, (Double)project.getPatch((Integer)id).getAttribute(varName), delta);
            }
            testIndices.add(indName);
        }

        assertEquals("Check all local indices", Project.LOCAL_METRICS.size(), testIndices.size());
            
    }

    @Test
    public void testDeltaIndice() throws Throwable {
        System.out.println("deltaIndice");
        
        CSVTabReader r = new CSVTabReader(new File("target/test-classes/org/thema/graphab/patches.csv"));
        r.read("Id");
        int nbGraph = 0;
        DeltaPCMetric deltaPC = new DeltaPCMetric();
        GraphMetricLauncher launcher = new GraphMetricLauncher(deltaPC, false);
        String startName = "d_" + deltaPC.getDetailName() + "|";
        for(GraphGenerator gen : refPrj.getGraphs()) {
            if(gen.getLinkset().getType_dist() == Linkset.EUCLID && gen.isIntraPatchDist())
                continue;
            
            assertTrue("No deltaPC for graph " + gen.getName(), 
                    r.getVarNames().contains(startName + deltaPC.getResultNames()[0] + "_" + gen.getName())) ;

            System.out.println("Test deltaPC on " + gen.getName());
            DeltaMetricTask task = new DeltaMetricTask(new TaskMonitor.EmptyMonitor(), gen, launcher, 1);
            ExecutorService.execute(task);
            Map<Object, Double[]> result = task.getResult();
            assertTrue("No results for deltaPC on graph " + gen.getName(), !result.isEmpty());
            for(Object id : result.keySet()) {
                double ref = (Double)r.getValue(id, startName + deltaPC.getResultNames()[0] + "_" + gen.getName());
                assertEquals(startName + deltaPC.getResultNames()[0] + "_" + gen.getName() + " id:" + id, ref, result.get(id)[0], 1e-13);
                ref = (Double)r.getValue(id, startName + deltaPC.getResultNames()[1] + "_" + gen.getName());
                assertEquals(startName + deltaPC.getResultNames()[1] + "_" + gen.getName() + " id:" + id, ref, result.get(id)[1], 1e-13);
                ref = (Double)r.getValue(id, startName + deltaPC.getResultNames()[2] + "_" + gen.getName());
                assertEquals(startName + deltaPC.getResultNames()[2] + "_" + gen.getName() + " id:" + id, ref, result.get(id)[2], 1e-13);
            }
            nbGraph++;
        } 
        
        assertTrue("Delta aucun graphe testé", nbGraph > 0);  
    }
    
    private static boolean isCircuit(String s) {
        return s.equals("BCCirc") || s.equals("CBC") || s.equals("CF") || s.equals("PCF");
    }
}
