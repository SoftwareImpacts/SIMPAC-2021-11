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
        
        Config.setParallelProc(7);
        
        for(Linkset costDist : refPrj.getLinksets())
            project.addLinkset(costDist, false);
        
        for(GraphGenerator gen : refPrj.getGraphs()) {
            gen.setSaved(false);
            project.addGraph(gen, false);
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
            put("plan_cout10", 305);
            put("plan_cout10_keep_links", 399);
            put("comp_euclid", 11476);
            put("comp_cout10", 611);
            put("comp_cout10_500", 361);
            put("comp_cout10_500_nopath", 1704); // and keep links

        }};
        HashMap<String, Double> sumCosts = new HashMap<String, Double>() {{
            put("comp_cout10", 325113.491543293);
            put("comp_cout10_500", 73011.57003450394);
            put("comp_cout10_500_nopath", 493886.84889149666);
            put("comp_euclid", 8.273298688911343E7);
            put("plan_cout1", 27588.819926977158);
            put("plan_cout10", 58964.78692293167);
            put("plan_cout10_keep_links", 78962.59125447273);
            put("plan_cout1_len", 27588.819926977158);
            put("plan_euclid", 377561.1236639023);
        }};
        HashMap<String, Double> sumDists = new HashMap<String, Double>() {{
            put("comp_cout10", 1900224.763380032);
            put("comp_cout10_500", 430544.5940399723);
            put("comp_cout10_500_nopath", 3755274.651401147);
            put("comp_euclid", 8.27329868717217E7);
            put("plan_cout1", 386340.45020947425);
            put("plan_cout10", 330809.0860193806);
            put("plan_cout10_keep_links", 459026.6819401853);
            put("plan_cout1_len", 386340.45020947425);
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
            put("graph_plan_cout10_300", 232);
            put("graph_comp_euclid_1000", 378);
            put("graph_comp_euclid_1000_nointra", 378);
            put("graph_comp_cout10", 611);
            put("graph_comp_cout10_500_nopath", 1704); // and keep links

        }};
        HashMap<String, Double> sumCosts = new HashMap<String, Double>() {{
            put("graph_comp_cout10", 325113.491543293);
            put("graph_comp_cout10_500_nopath", 493886.84889149666);
            put("graph_comp_euclid_1000", 166109.80953905112);
            put("graph_comp_euclid_1000_nointra", 166109.80953905112);
            put("graph_plan_cout10_mst", 12503.922990322113);
            put("graph_plan_cout10_300", 24210.858273029327);
            put("graph_plan_cout1_len", 386340.4502094745);
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
    public void testGlobalIndices() throws Exception {
        HashMap<String, Double> resIndices = new HashMap<String, Double>() {{
            put("PC_d1000_p0.05_beta1-graph_comp_cout10", 2.650910611113619E-4);
            put("PC_d1000_p0.05_beta1-graph_comp_cout10_500_nopath", 3.288065298587884E-4);
            put("PC_d1000_p0.05_beta1-graph_comp_euclid_1000_nointra", 9.581715576495827E-5);
            put("PC_d1000_p0.05_beta1-graph_plan_cout10_300", 2.3798796246929596E-4);            
            put("PC_d1000_p0.05_beta1-graph_plan_cout10_mst", 2.0496746501147566E-4);
            put("PC_d1000_p0.05_beta1-graph_plan_cout1_len", 6.711833864673239E-5);
//            put("PC_d1000_p0.05_beta1-graph_plan_euclid", 7.045308587598926E-5);
            put("S#F_d1000_p0.05_beta1-graph_comp_cout10", 1.4898414270878693E8);
            put("S#F_d1000_p0.05_beta1-graph_comp_cout10_500_nopath", 1.9303219148459563E8);
            put("S#F_d1000_p0.05_beta1-graph_comp_euclid_1000_nointra", 3.831238163096813E7);
            put("S#F_d1000_p0.05_beta1-graph_plan_cout10_300", 1.278656380450999E8);
            put("S#F_d1000_p0.05_beta1-graph_plan_cout10_mst", 1.0653028960154916E8);
            put("S#F_d1000_p0.05_beta1-graph_plan_cout1_len", 1.8292474994626906E7);
//            put("S#F_d1000_p0.05_beta1-graph_plan_euclid", 2.0163399216204323E7);
            put("E#BC_d1000_p0.05_beta1-graph_comp_cout10", 0.8202177311458566);
            put("E#BC_d1000_p0.05_beta1-graph_comp_cout10_500_nopath", 0.7877841686651839);
            put("E#BC_d1000_p0.05_beta1-graph_comp_euclid_1000_nointra", 0.7220420460787872);
            put("E#BC_d1000_p0.05_beta1-graph_plan_cout10_300", 0.8352240466472872);
            put("E#BC_d1000_p0.05_beta1-graph_plan_cout10_mst", 0.8194939322548981);
            put("E#BC_d1000_p0.05_beta1-graph_plan_cout1_len", 0.6544387579507804);
//            put("E#BC_d1000_p0.05_beta1-graph_plan_euclid", 0.6424046501364302);
            put("CCP-graph_comp_cout10", 1.0);
            put("CCP-graph_comp_euclid_1000", 0.3245421246977966);
            put("ECS-graph_comp_cout10", 7931963.045229822);
            put("ECS-graph_comp_euclid_1000", 2574256.1397232935);
            put("IIC-graph_comp_cout10", 4.303969510306552E-4);
            put("IIC-graph_comp_cout10_500_nopath", 4.885949077593412E-4);
            put("IIC-graph_comp_euclid_1000", 1.6148035259807588E-4);
            put("IIC-graph_plan_cout10_mst", 1.6877947203994617E-4);
            put("IIC-graph_plan_cout1_len", 3.5384810593776873E-4);
            put("IIC-graph_plan_euclid", 3.57015587313272E-4);
            put("MSC-graph_comp_cout10", 7931963.045229822);
            put("MSC-graph_comp_euclid_1000", 417471.73922262224);
            put("SLC-graph_comp_cout10", 7931963.045229822);
            put("SLC-graph_comp_euclid_1000", 3735409.177527936);
            put("GD-graph_comp_cout10", 3272.375139704387);
            put("GD-graph_comp_cout10_500_nopath", 4173.568296432495);
            put("GD-graph_comp_euclid_1000_nointra", 8817.915643013066);
            put("GD-graph_plan_cout10_mst", 6744.049794439901);
            put("GD-graph_plan_cout1_len", 22173.360015519687);
//            put("GD-graph_plan_euclid", 21114.413062993004);
            put("H-graph_comp_cout10", 3668.2373015873045);
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
        GraphMetricLauncher launcher = new GraphMetricLauncher(new DeltaPCMetric(), false);
        for(GraphGenerator gen : refPrj.getGraphs()) {
            if(gen.getLinkset().getType_dist() == Linkset.EUCLID && gen.isIntraPatchDist())
                continue;
            if(!r.getVarNames().contains("d_PCIntra_" + gen.getName())) 
                continue;
            System.out.println("Test deltaPC on " + gen.getName());
            DeltaMetricTask task = new DeltaMetricTask(new TaskMonitor.EmptyMonitor(), gen, launcher, 1);
            ExecutorService.execute(task);
            Map<Object, Double[]> result = task.getResult();
            for(Object id : result.keySet()) {
                double ref = (Double)r.getValue(id, "d_PCIntra_" + gen.getName());
                assertEquals("d_PCIntra_" + gen.getName() + " id:" + id, ref, result.get(id)[0], 1e-13);
                ref = (Double)r.getValue(id, "d_PCFlux_" + gen.getName());
                assertEquals("d_PCFlux_" + gen.getName() + " id:" + id, ref, result.get(id)[1], 1e-13);
                ref = (Double)r.getValue(id, "d_PCCon_" + gen.getName());
                assertEquals("d_PCCon_" + gen.getName() + " id:" + id, ref, result.get(id)[2], 1e-13);
            }
            nbGraph++;
        } 
        
        assertTrue("Delta aucun graphe testé", nbGraph > 0);  
    }
    
    private static boolean isCircuit(String s) {
        return s.equals("BCCirc") || s.equals("CBC") || s.equals("CF") || s.equals("PCF");
    }
}
