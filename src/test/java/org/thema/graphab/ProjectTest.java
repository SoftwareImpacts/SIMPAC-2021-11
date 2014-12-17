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
import org.thema.parallel.ExecutorService;
import org.thema.data.IOImage;
import org.thema.common.io.tab.CSVTabReader;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.Feature;
import org.thema.graphab.addpatch.AddPatchCommand;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.DeltaMetricTask;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.global.DeltaPCMetric;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.graphab.metric.local.LocalMetric;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;
import org.thema.graphab.metric.global.PCMetric;

/**
 * Test Project class
 * @author Gilles Vuidel
 */
public class ProjectTest {
    
    /** source coverage */
    private static GridCoverage2D coverage;
    /** Project created in /tmp */
    private static Project project;
    

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
        // load all metrics
        Project.loadPluginMetric(ProjectTest.class.getClassLoader());

        coverage = IOImage.loadTiff(new File("target/test-classes/org/thema/graphab/source.tif"));
        project = new Project("test", new File("/tmp"), coverage, new TreeSet(Arrays.asList(1, 2, 3, 4, 5, 6, 8, 9, 10)), 1, Double.NaN, false, 0, false);
//        project = Project.loadProject(new File("/tmp/test.xml"), true);
        MainFrame.project = project;
        
        // Load project references
        XStream xstream = new XStream(); 
        xstream.alias("Project", Project.class);
        xstream.alias("Pointset", Pointset.class);
        xstream.alias("Linkset", Linkset.class);
        xstream.alias("Graph", GraphGenerator.class);
        Project refPrj = (Project) xstream.fromXML(new File("target/test-classes/org/thema/graphab/TestProject.xml"));
        
        // create linksets
        for(Linkset costDist : refPrj.getLinksets()) {
            project.addLinkset(costDist, true);
        }
        
        // create graphs
        for(GraphGenerator gen : refPrj.getGraphs()) {
            gen.setSaved(false);
            project.addGraph(gen, true);
        }
    }


    /**
     * Check patch number and sizes
     * @throws Exception 
     */
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
            put("plan_circ", 399);

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
            put("plan_circ", 409.4970877191959);
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
            put("plan_circ", Double.NaN);
        }};
        
        System.out.println("Test addLinkset");
        for(Linkset costDist : project.getLinksets()) {
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
        System.out.println("Test addGraph");
        for(GraphGenerator gen : project.getGraphs()) {
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
    
    /**
     * Test all global metrics
     * @throws Exception 
     */
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
            
            // test metric
            put("E#eBC_d1000_p0.05_beta1-graph_comp_cout10", 0.7339459732275027);
            put("D#BC_d1000_p0.05_beta1-graph_comp_cout10", 0.9736784563148873);
            put("D#eBC_d1000_p0.05_beta1-graph_comp_cout10", 0.9845967148211576);
            put("PCCirc_d1000_p0.05_beta1-graph_comp_cout10", 0.00133658324001119);
        }};
        
        HashSet<String> testIndices = new HashSet<>();
        for(String varName : resIndices.keySet()) {
            System.out.println("Test global indice : " + varName);
            String indName = varName.substring(0, varName.indexOf("-"));
            if(indName.contains("_"))
                indName = indName.substring(0, indName.indexOf("_"));
            GlobalMetric indice = Project.getGlobalMetric(indName);
            indice.setParamFromDetailName(varName.substring(0, varName.indexOf("-")));
            GraphGenerator gen = project.getGraph(varName.substring(varName.indexOf("-")+1));
            GraphMetricLauncher launcher = new GraphMetricLauncher(indice, true);
            Double[] res = launcher.calcIndice(gen, new TaskMonitor.EmptyMonitor());
            double err = 1e-10;
            assertEquals("Indice " + indice.getDetailName() + "-" + gen.getName(), resIndices.get(indice.getDetailName() + "-" + gen.getName()), 
                    res[0], res[0]*err);
            testIndices.add(indName);
        }
        
        assertEquals("Check all global indices", Project.getGlobalMetricsFor(Project.Method.GLOBAL).size(), testIndices.size());
        
    }
    
    /**
     * Test all local metrics
     * @throws Throwable 
     */
    @Test
    public void testLocalIndices() throws Throwable {
        CSVTabReader r = new CSVTabReader(new File("target/test-classes/org/thema/graphab/patches.csv"));
        r.read("Id");
        HashSet<String> testIndices = new HashSet<>();
        
        for(String varName : r.getVarNames()) {
            // pour les attributs Area, Perim et Capacity et les delta métriques
            if(!varName.contains("_") || varName.startsWith("d_")) 
                continue;
            String indName = varName.substring(0, varName.indexOf("_"));
            
            GraphGenerator gen = null;
            for(String grName : project.getGraphNames()) {
                if(varName.endsWith("_"+grName)) {
                    gen = project.getGraph(grName);
                }
            }
            if(gen == null) {
                throw new RuntimeException("Graph not found for : " + varName);
            }
            // certains calculs ne sont pas testés car trop variables ou interdit :
            // - les graphes euclidiens avec dist intra taches
            // - les indices de circuit sur les graphes mst
            if(gen.getLinkset().getType_dist() == Linkset.EUCLID && gen.isIntraPatchDist() || 
                    isCircuit(indName) && gen.getType() == GraphGenerator.MST) {
                continue;
            }
            System.out.println("Test local indice : " + varName);
            double err = indName.equals("CBC") ? 1e-3 : isCircuit(indName) ? 1e-4 : 1e-9;
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

        // check if all metrics have been tested
        assertEquals("Check all local indices", Project.LOCAL_METRICS.size(), testIndices.size());
            
    }

    /**
     * Test delta metrics
     * @throws Throwable 
     */
    @Test
    public void testDeltaIndice() throws Throwable {
        System.out.println("delta metric");
        
        CSVTabReader r = new CSVTabReader(new File("target/test-classes/org/thema/graphab/patches.csv"));
        r.read("Id");
        int nbGraph = 0;
        DeltaPCMetric deltaPC = new DeltaPCMetric();
        GraphMetricLauncher launcher = new GraphMetricLauncher(deltaPC, false);
        String startName = "d_" + deltaPC.getDetailName() + "|";
        for(GraphGenerator gen : project.getGraphs()) {
            if(gen.getLinkset().getType_dist() == Linkset.EUCLID && gen.isIntraPatchDist()) {
                continue;
            }
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
        
        assertTrue("Delta no graph tested", nbGraph > 0);  
    }
    
    /**
     * Test add patch
     * @throws Throwable 
     */
    @Test
    public void testAddPatch() throws Throwable {
        PCMetric indice = new PCMetric();
        indice.setParams(1000, 0.05, 1);
        
        AddPatchCommand addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_cout10_500_nopath"), null, 1000, 1, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        double[] metric = new double[] {
            3.2880609909219464E-4,
            3.310670270101464E-4,
            3.3247253389400165E-4,
            3.3321668359908647E-4,
            3.3377521178276566E-4,
            3.342367536836475E-4,
            3.34660961733302E-4,
            3.3507512044854415E-4,
            3.3522097708321225E-4,
            3.353572949243866E-4,
            3.354917552276591E-4};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        reloadProject();
        
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_cout10_500_nopath"), 
                new File("target/test-classes/org/thema/graphab/patch_alea.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.288060990921947E-4,
            3.318280601637511E-4,
            3.337111357954791E-4,
            3.3475535460807815E-4,
            3.35787430334884E-4,
            3.361066228947054E-4,
            3.363812858721702E-4,
            3.3660585153397004E-4,
            3.367396334795461E-4,
            3.36848613338119E-4,
            3.3694048774752193E-4};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add geometry patch shapefile", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        reloadProject();
        
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_cout10_500_nopath"), 
                new File("target/test-classes/org/thema/graphab/point_alea.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.288060990921945E-4,
            3.317128734737994E-4,
            3.335155232091921E-4,
            3.342935746157495E-4,
            3.3460310195571757E-4,
            3.3482556544645883E-4,
            3.350232848996763E-4,
            3.3514876007901677E-4,
            3.352531327668829E-4,
            3.352938746508512E-4,
            3.353309445926464E-4 };
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add point patch shapefile", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        reloadProject();
        indice.setParams(10000, 0.05, 1);
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_euclid_1000_nointra"), null, 1000, 1, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.1948198846500254E-4,
            4.56556038211144E-4,
            4.6650788677922423E-4,
            4.7528040102511954E-4,
            4.827634893607471E-4,
            4.903433825337935E-4,
            4.942381096877162E-4,
            4.961944710793439E-4,
            4.97448888004178E-4,
            4.984450259964978E-4,
            4.993149630772023E-4};
        
        for(Integer i : addPatchCmd.getMetricValues().keySet()) {
            assertEquals("Add euclidean grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        reloadProject();
        indice.setParams(10000, 0.05, 1);
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_euclid_1000_nointra"), null, 1000, 2, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.194819884650025E-4,
            4.565560382111441E-4,
            0,
            4.786124884466496E-4,
            4.876118464369523E-4,
            0,
            4.961994164379232E-4,
            5.000687271718898E-4,
            5.03269544241578E-4,
            0,
            5.086599949425309E-4};
        
        for(Integer i : addPatchCmd.getMetricValues().keySet()) {
            assertEquals("Add euclidean multi grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        reloadProject();
        indice.setParams(10000, 0.05, 1);
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_euclid_1000_nointra"),
                new File("target/test-classes/org/thema/graphab/patch_alea.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.194819884650025E-4,
            4.708707205256025E-4,
            4.764749735090871E-4,
            4.7917863404543267E-4,
            4.8170680260761414E-4,
            4.8286242596010006E-4,
            4.838637168600206E-4,
            4.8446112987159936E-4,
            4.8504081895647566E-4,
            4.855894339519803E-4,
            4.8608219644911147E-4};
        for(Integer i : addPatchCmd.getMetricValues().keySet()) {
            assertEquals("Add euclidean polygon patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        reloadProject();
    }
    
    
    private static boolean isCircuit(String s) {
        return s.equals("BCCirc") || s.equals("CBC") || s.equals("CF") || s.equals("PCF");
    }
    
    private static void reloadProject() throws Exception {
        project = Project.loadProject(new File("/tmp/test.xml"), false);
        MainFrame.project = project;
    }
}
