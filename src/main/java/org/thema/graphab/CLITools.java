/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab;

import java.io.*;
import org.thema.graphab.links.Path;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.DeltaAddGraphGenerator;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.model.Logistic;
import org.thema.graphab.model.DistribModel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.TaskMonitor;
import org.thema.common.distribute.ExecutorService;
import org.thema.common.distribute.ParallelExecutor;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.graphab.addpatch.AddPatchCommand;
import org.thema.graphab.metric.DeltaMetricTask;
import org.thema.graphab.metric.GraphMetricLauncher;
import org.thema.graphab.metric.Metric;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.graphab.metric.local.LocalMetric;

/**
 *
 * @author gvuidel
 */
public class CLITools {


    static class Range {
        double min, max, inc;
        List<Double> values;

        public Range(double val) {
            this(val, 1, val);
        }

        public Range(double min, double max) {
            this(min, 1, max);
        }

        public Range(double min, double inc, double max) {
            this.min = min;
            this.max = max;
            this.inc = inc;
        }
 
        public Range(List<Double> values) {
            this.values = values;
            this.min = Collections.min(values);
            this.max = Collections.max(values);
        }

        public List<Double> getValues() {
            if(values == null) {
                List<Double> lst = new ArrayList<Double>();
                for(double v = min; v <= max; v += inc)
                    lst.add(v);
                return lst;
            } else
                return values;
        }
        
        public double getValue(int ind) {
            return getValues().get(ind);
        }

        public boolean isUnique() {
            return getValues().size() == 1;
        }

        public static Range parse(String s) {
            String [] tok = s.split(":");
            if(tok.length == 1) {
                tok = s.split(",");
                if(tok.length == 1)
                    return new Range(Double.parseDouble(tok[0]));
                else {
                    List<Double> values = new ArrayList<Double>(tok.length);
                    for(int i = 0; i < tok.length; i++)
                        values.add(Double.parseDouble(tok[i]));
                    return new Range(values);
                }

            } else if(tok.length == 2)
                return new Range(Double.parseDouble(tok[0]), Double.parseDouble(tok[1]));
            else if(tok.length == 3)
                return new Range(Double.parseDouble(tok[0]), Double.parseDouble(tok[1]), Double.parseDouble(tok[2]));
            return  null;
        }
    }



    Project project;
    List<Linkset> useCosts = new ArrayList<Linkset>();
    List<GraphGenerator> useGraphs = new ArrayList<GraphGenerator>();
    List<Pointset> useExos = new ArrayList<Pointset>();
    boolean save = true;

    public void execute(String [] arg) throws Throwable {
        if(arg[0].equals("--help")) {
            System.out.println("Usage :\njava -jar graphab.jar --metrics\n" +
                    "java -jar graphab.jar --project prjfile.xml [-proc n] [-nosave] command1 [command2 ...]\n" +
                    "Commands list :\n" +
                    "--show\n" + 
                    "--linkset [complete[=dmax]] [code1,..,coden=cost1 ...] codei,..,codej=min:inc:max\n" +
                    "--uselinkset linkset1,...,linksetn\n" +
                    "--graph [threshold=min:inc:max]\n" +
                    "--usegraph graph1,...,graphn\n" +
                    "--pointset pointset.shp\n" +
                    "--usepointset pointset1,...,pointsetn\n" +
                    "--gmetric global_metric_name [maxcost=valcost] [param1=min:inc:max [param2=min:inc:max ...]]\n" +
                    "--cmetric comp_metric_name [maxcost=valcost] [param1=min:inc:max [param2=min:inc:max ...]]\n" +
                    "--lmetric local_metric_name [maxcost=valcost] [param1=min:inc:max [param2=min:inc:max ...]]\n" +
                    "--model variable distW=min:inc:max\n" +
                    "--delta global_metric_name [maxcost=valcost] [param1=val ...] obj=patch|link [sel=id1,id2,...,idn]\n" +
                    "--gtest nstep global_metric_name [maxcost=valcost] [param1=val ...] obj=patch|link sel=id1,id2,...,idn\n" +
                    "--ltest nstep local_metric_name [maxcost=valcost] [param1=val ...] obj=patch|link sel=id1,id2,...,idn\n" +
                    "--addpatch npatch global_metric_name [param1=val ...] [gridres=min:inc:max [capa=capa_file] [multi=nbpatch,size]]|[pointfile=file.shp [capa=capa_field]]\n" +
                    "--gremove global_metric_name [maxcost=valcost] [param1=val ...] [patch=id1,id2,...,idn|fpatch=file.txt] [link=id1,id2,...,idm|flink=file.txt]\n" +
                    "\nmin:inc:max -> val1,val2,val3...");
            return;
        }
        
        if(arg[0].equals("--metrics")) {
            showMetrics();
            return;
        }
        
        if(!arg[0].equals("--project"))
            throw new IllegalArgumentException("Unknown command " + arg[0]);
        
        TaskMonitor.setHeadlessStream(new PrintStream(File.createTempFile("java", "monitor")));
        
        project = Project.loadProject(new File(arg[1]), false);
        MainFrame.project = project;

        List<String> args = new ArrayList<String>(Arrays.asList(arg));
        args.remove(0); args.remove(0);
        String p = args.isEmpty() ? "--show" : args.remove(0);

        // global options
        while(p != null && !p.startsWith("--")) {
            if(p.equals("-proc")) {
                String n = args.remove(0);
                ParallelFExecutor.setNbProc(Integer.parseInt(n));
                ParallelExecutor.setNbProc(Integer.parseInt(n));
            } else if(p.equals("-nosave"))
                save = false;
            else
                throw new IllegalArgumentException("Unknown option " + p);

            if(args.isEmpty())
                p = null;
            else
                p = args.remove(0);
        }

        // treat each command
        while(p != null) {
            if(p.equals("--model"))
                batchModel(args);
            else if(p.equals("--linkset"))
                batchCost(args);
            else if(p.equals("--graph"))
                batchGraph(args);
            else if(p.equals("--pointset"))
                batchExo(args);
            else if(p.equals("--gmetric"))
                batchGlobalIndice(args);
            else if(p.equals("--cmetric"))
                batchCompIndice(args);
            else if(p.equals("--lmetric"))
                batchLocalIndice(args);
            else if(p.equals("--delta"))
                deltaIndice(args);
            else if(p.equals("--gtest"))
                addGlobal(args);
            else if(p.equals("--ltest"))
                addLocal(args);
            else if(p.equals("--addpatch"))
                addPatch(args);
            else if(p.equals("--gremove"))
                remGlobal(args);
            else if(p.startsWith("--use"))
                useObj(p, args);
            else if(p.startsWith("--show"))
                showProject();
            else 
                throw new IllegalArgumentException("Unknown command " + p);

            if(args.isEmpty())
                p = null;
            else
                p = args.remove(0);
        }

    }

    private void showProject() {
        System.out.println("===== Link sets =====");
        for(Linkset cost : project.getLinksets())
            System.out.println(cost.getName());
        System.out.println("\n===== Graphs =====");
        for(GraphGenerator graph : project.getGraphs())
            System.out.println(graph.getName());
        System.out.println("\n===== Point sets =====");
        for(Pointset exo : project.getPointsets())
            System.out.println(exo.getName());
    }

    private void showMetrics() {
        System.out.println("===== Global metrics =====");
        for(Metric indice : Project.GLOBAL_METRICS) {
            System.out.println(indice.getShortName() + " - " + indice.getName());
            if(indice.hasParams())
                System.out.println("\tparams : " + Arrays.deepToString(indice.getParams().keySet().toArray()));
        }
        System.out.println("\n===== Local metrics =====");
        for(Metric indice : Project.LOCAL_METRICS) {
            System.out.println(indice.getShortName() + " : " + indice.getName());
            if(indice.hasParams())
                System.out.println("\tparams : " + Arrays.deepToString(indice.getParams().keySet().toArray()));
        }
    }
    
    private void useObj(String p, List<String> args) {
        String param = args.remove(0);
        String [] toks = param.split(",");
        if(p.equals("--uselinkset")) {
            useCosts.clear();
            for(String tok : toks)
                if(project.getLinkset(tok) != null)
                    useCosts.add(project.getLinkset(tok));
                else
                    throw new IllegalArgumentException("Unknown linkset " + tok);
                    
        } else if(p.equals("--usegraph")) {
            useGraphs.clear();
            for(String tok : toks)
                if(project.getGraphNames().contains(tok))
                    useGraphs.add(project.getGraph(tok));
                else
                    throw new IllegalArgumentException("Unknown graph " + tok);
        } else if(p.equals("--usepointset")) {
            useExos.clear();
            for(String tok : toks)
                if(project.getPointsetNames().contains(tok))
                    useExos.add(project.getPointset(tok));
                else
                    throw new IllegalArgumentException("Unknown pointset " + tok);
        }
    }

    private Collection<Linkset> getCosts() {
        if(useCosts.isEmpty())
            return project.getLinksets();
        else
            return useCosts;
    }

    private Collection<GraphGenerator> getGraphs() {
        if(useGraphs.isEmpty())
            return project.getGraphs();
        else
            return useGraphs;
    }

    private Collection<Pointset> getExos() {
        if(useExos.isEmpty())
            return project.getPointsets();
        else
            return useExos;
    }

    public void batchModel(List<String> args) throws IOException {

            String var = args.remove(0);
            String arg = args.remove(0);
            String [] tok = arg.split("=");
            Range rangeW = Range.parse(tok[1]);

            TreeSet<String> vars = new TreeSet<String>(project.getGraphPatchVar(
                    getGraphs().iterator().next().getName()));
            for(GraphGenerator graph : getGraphs())
                vars.retainAll(project.getGraphPatchVar(graph.getName()));

            FileWriter wd = new FileWriter(new File(project.getProjectDir(), "model-" + var + "-dW" + tok[1] + ".txt"));
            wd.write("Graph\tMetric\tDistWeight\tR2\tAIC\tCoef\n");
            try {
                for(GraphGenerator graph : getGraphs()) {
                    System.out.println(graph.getName());
                    Pointset exoData = null;
                    for(Pointset exo : getExos())
                        if(exo.getLinkset() == graph.getLinkset())
                            exoData = exo;
                    if(exoData == null)
                        throw new RuntimeException("No available pointset for graph " + graph.getName());
                    
                    for(String v : vars) {
                        System.out.println(graph.getName() + " : " + v);

                        for(Double d : rangeW.getValues()) {
                            DistribModel model = new DistribModel(project, exoData, var, -Math.log(0.05) / d,
                            Arrays.asList(v+"_"+graph.getName()), new LinkedHashMap(), true, false, 0, null);
                            model.estimModel(new TaskMonitor.EmptyMonitor());
                            Logistic estim = model.getLogisticModel();                            
                            wd.write(String.format(Locale.US, "%s\t%s\t%g\t%g\t%g\t%g\n", graph.getName(), v, d, estim.getR2(), estim.getAIC(), estim.getCoefs()[1]));
                        }
                    }
                    wd.flush();
                }

                wd.close();
            } catch(Throwable ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void batchCost(List<String> args) throws Throwable {
        int type = Linkset.PLANAR;
        double threshold = 0;
        if(args.get(0).startsWith("complete")) {
            String arg = args.remove(0);
            type = Linkset.COMPLETE;
            if(arg.contains("=")) {
                String [] tok = arg.split("=");
                threshold = Double.parseDouble(tok[1]);
            }

        }
        int max = Collections.max(project.getCodes());
        double [] costs = new double[max+1];
        List<Double> dynCodes = null;
        Range rangeCost = null;
        String name = null;
        while(!args.isEmpty() && args.get(0).contains("=")) {
            String [] tok = args.remove(0).split("=");
            Range codes = Range.parse(tok[0]);
            Range cost = Range.parse(tok[1]);
            if(cost.isUnique()) {
                for(Double code : codes.getValues())
                    costs[code.intValue()] = cost.min;
            }
            if(rangeCost == null || !cost.isUnique()) {
                if(rangeCost != null && !rangeCost.isUnique())
                    throw new IllegalArgumentException("Only one range can be defined for linkset");
                rangeCost = cost;
                dynCodes = codes.getValues();
                name = tok[0];
            }
        }
        useCosts.clear();
        for(Double c : rangeCost.getValues()) {
            System.out.println("Calc cost " + c);
            for(Double code : dynCodes)
                costs[code.intValue()] = c;
            Linkset cost = new Linkset("cost_" + name + "-" + c, type, costs, Linkset.COST_LENGTH, true, false, threshold);
            project.addLinkset(cost, save);
            useCosts.add(cost);
        }
    }

    private void batchExo(List<String> args) throws Exception {
        File f = new File(args.remove(0));
        String name = f.getName().substring(0, f.getName().length()-4);
        List<DefaultFeature> features = DefaultFeature.loadFeatures(f, true);
        List<String> attrNames = features.get(0).getAttributeNames();
        useExos.clear();
        for(Linkset cost : getCosts()) {
            Pointset exo = new Pointset(name + "_" + cost.getName(), cost, 0, Pointset.AG_NONE);
            System.out.println("Add pointset " + exo.getName());
            project.addPointset(exo, attrNames, features, save);
            useExos.add(exo);
        }
    }

    private void batchGraph(List<String> args) throws Exception {
        int type = GraphGenerator.COMPLETE;
        Range range = null;
        if(!args.isEmpty() && args.get(0).startsWith("threshold=")) {
            type = GraphGenerator.THRESHOLD;
            String arg = args.remove(0);
            String [] tok = arg.split("=");
            range = Range.parse(tok[1]);
        }
        useGraphs.clear();
        for(Linkset cost : getCosts()) {
            if(type == GraphGenerator.COMPLETE) {
                GraphGenerator g = new GraphGenerator("comp_" + cost.getName(), cost, type, 0, true);
                System.out.println("Create graph " + g.getName());
                project.addGraph(g, save);
                useGraphs.add(g);
            }
            else
                for(Double d : range.getValues()) {
                    GraphGenerator g = new GraphGenerator("thresh_" + d + "_" + cost.getName(), cost, type, d, true);
                    System.out.println("Create graph " + g.getName());
                    project.addGraph(g, save);
                    useGraphs.add(g);
                }
        }
    }

    private void batchGlobalIndice(List<String> args) throws Throwable {
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(!args.isEmpty() && args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }

        HashMap<String, Range> ranges = new LinkedHashMap<String, Range>();
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.isEmpty() && args.get(0).contains("=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            params.put(tok[0], r.min);
            if(!r.isUnique())
                ranges.put(tok[0], r);
        }

        GlobalMetric indice = Project.getGlobalMetric(indName);
        System.out.println("Global metric " + indice.getName());
        
        FileWriter fw = new FileWriter(new File(project.getProjectDir(), indice.getShortName() + ".txt"));
        fw.write("Graph");
        for(String param : params.keySet())
            fw.write("\t" + param);
        for(String resName : indice.getResultNames())
            fw.write("\t" + resName);
        fw.write("\n");
        
        for(GraphGenerator graph : getGraphs()) {
            
            List<String> paramNames = new ArrayList<String>(ranges.keySet());
            int [] indParam = new int[ranges.size()];
            boolean end = false;
            while(!end) {
                for(int i = 0; i < indParam.length; i++)
                    params.put(paramNames.get(i), ranges.get(paramNames.get(i)).getValue(indParam[i]));
                indice.setParams(params);
                
                Double[] res = new GraphMetricLauncher(indice, maxCost, true).calcIndice(graph, null);
                System.out.println(graph.getName() + " - " + indice.getDetailName() + " : " + res[0]);
                
                fw.write(graph.getName());
                for(String param : params.keySet())
                    fw.write("\t" + params.get(param));
                for(Double val : res)
                    fw.write("\t" + val);
                fw.write("\n");
                
                if(indParam.length > 0)
                    indParam[0]++;
                for(int i = 0; i < indParam.length-1; i++)
                    if(indParam[i] >= ranges.get(paramNames.get(i)).getValues().size()) {
                        indParam[i] = 0;
                        indParam[i+1]++;
                    } else
                        break;
                if(indParam.length > 0)
                    end = indParam[indParam.length-1] >= ranges.get(paramNames.get(indParam.length-1)).getValues().size();
                else 
                    end = true;
            }
        }
        
        fw.close();
    }
    
    private void batchCompIndice(List<String> args) throws Throwable {
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(!args.isEmpty() && args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }

        HashMap<String, Range> ranges = new LinkedHashMap<String, Range>();
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.isEmpty() && args.get(0).contains("=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            params.put(tok[0], r.min);
            if(!r.isUnique())
                ranges.put(tok[0], r);
        }

        GlobalMetric indice = Project.getGlobalMetric(indName);
        System.out.println("Component metric " + indice.getName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println(graph.getName());

            List<String> paramNames = new ArrayList<String>(ranges.keySet());
            int [] indParam = new int[ranges.size()];
            boolean end = false;
            while(!end) {
                for(int i = 0; i < indParam.length; i++)
                    params.put(paramNames.get(i), ranges.get(paramNames.get(i)).getValue(indParam[i]));
                indice.setParams(params);

                System.out.println(graph.getName() + " : " + indice.getDetailName());

                MainFrame.calcCompIndice(new TaskMonitor.EmptyMonitor(), graph, indice, maxCost);

                if(indParam.length > 0)
                    indParam[0]++;
                for(int i = 0; i < indParam.length-1; i++)
                    if(indParam[i] >= ranges.get(paramNames.get(i)).getValues().size()) {
                        indParam[i] = 0;
                        indParam[i+1]++;
                    } else
                        break;

                if(indParam.length > 0)
                    end = indParam[indParam.length-1] >= ranges.get(paramNames.get(indParam.length-1)).getValues().size();
                else
                    end = true;
            }

            if(save) {
                try {
                    MainFrame.project.saveGraphVoronoi(graph.getName());
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void batchLocalIndice(List<String> args) throws Throwable {
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(!args.isEmpty() && args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }

        HashMap<String, Range> ranges = new LinkedHashMap<String, Range>();
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.isEmpty() && args.get(0).contains("=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            params.put(tok[0], r.min);
            if(!r.isUnique())
                ranges.put(tok[0], r);
        }

        LocalMetric indice = Project.getLocalMetric(indName);
        System.out.println("Local metric " + indice.getName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println(graph.getName());

            List<String> paramNames = new ArrayList<String>(ranges.keySet());
            int [] indParam = new int[ranges.size()];
            boolean end = false;
            while(!end) {
                for(int i = 0; i < indParam.length; i++)
                    params.put(paramNames.get(i), ranges.get(paramNames.get(i)).getValue(indParam[i]));
                indice.setParams(params);

                System.out.println(graph.getName() + " : " + indice.getDetailName());

                MainFrame.calcLocalIndice(new TaskMonitor.EmptyMonitor(), graph, indice, maxCost);

                if(indParam.length > 0)
                    indParam[0]++;
                for(int i = 0; i < indParam.length-1; i++)
                    if(indParam[i] >= ranges.get(paramNames.get(i)).getValues().size()) {
                        indParam[i] = 0;
                        indParam[i+1]++;
                    } else
                        break;

                if(indParam.length > 0)
                    end = indParam[indParam.length-1] >= ranges.get(paramNames.get(indParam.length-1)).getValues().size();
                else
                    end = true;
            }

            if(save) {
                try {
                    MainFrame.project.saveLinks(graph.getLinkset().getName());
                    MainFrame.project.savePatch();
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void deltaIndice(List<String> args) throws IOException {
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.get(0).startsWith("obj=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isUnique())
                params.put(tok[0], r.min);
            else
                throw new IllegalArgumentException("No range for metric params in --delta");
        }
        // obj=patch|link
        boolean patch = args.remove(0).split("=")[1].equals("patch");

        List ids = new ArrayList();
        if(!args.isEmpty() && args.get(0).startsWith("sel=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            for(String tok : toks)
                if(patch)
                    ids.add(Integer.parseInt(tok));
                else
                    ids.add(tok);
        } else 
            ids = null;

        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            if(params.isEmpty())
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --delta");
            indice.setParams(params);
        }
        
        GraphMetricLauncher launcher = new GraphMetricLauncher(indice, maxCost, false);
        System.out.println("Global indice " + indice.getName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println(graph.getName());
            FileWriter wd = new FileWriter(new File(project.getProjectDir(), "delta-" + indice.getDetailName() + "_" + graph.getName() + ".txt"));
            wd.write("Id");
            for(String name : indice.getResultNames())
                wd.write("\td_" + name);
            wd.write("\n");
            
            DeltaMetricTask task = ids == null ? new DeltaMetricTask(new TaskMonitor.EmptyMonitor(), graph, launcher, patch ? 1 : 2) :
                    new DeltaMetricTask(new TaskMonitor.EmptyMonitor(), graph, launcher, ids);
            ExecutorService.execute(task);
            wd.write("Init");
            for(Double val : task.getInit())
                wd.write("\t" + val);
            wd.write("\n");
            Map<Object, Double[]> result = task.getResult();
            for(Object id : new TreeSet(result.keySet())) {
                wd.write(id.toString());
                for(Double val : result.get(id))
                    wd.write("\t" + val);
                wd.write("\n");
            }
            
            wd.close();
        }
    }
    
    private void addGlobal(List<String> args) throws Throwable {
        int nbStep = Integer.parseInt(args.remove(0));
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.get(0).startsWith("obj=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isUnique())
                params.put(tok[0], r.min);
            else
                throw new IllegalArgumentException("No range for indice params in --gtest");
        }
        // obj=patch|link
        boolean patch = args.remove(0).split("=")[1].equals("patch");

        List lstIds = new ArrayList();
        String [] toks = args.remove(0).split("=")[1].split(",");
        for(String tok : toks)
            if(patch)
                lstIds.add(Integer.parseInt(tok));
            else
                lstIds.add(tok);

        GlobalMetric indice = Project.getGlobalMetric(indName);
        
        if(indice.hasParams()) {
            if(params.isEmpty())
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --gtest");
            indice.setParams(params);
        }
        GraphMetricLauncher launcher = new GraphMetricLauncher(indice, maxCost, true);
        
        for(GraphGenerator graph : getGraphs()) {
            HashSet ids = new HashSet(lstIds);
            FileWriter w = new FileWriter(new File(project.getProjectDir(), "gtest-" + graph.getName() + "-" + indice.getDetailName() + ".txt"));
            FileWriter wd = new FileWriter(new File(project.getProjectDir(), "gtest-" + graph.getName() + "-" + indice.getDetailName() + "-detail.txt"));
            wd.write("Step\tId\t"+indice.getShortName()+"\n");
            w.write("Step\tId\t"+indice.getShortName()+"\n");

            System.out.println("Global indice " + indice.getName());

            Double [] init = launcher.calcIndice(graph, new TaskMonitor.EmptyMonitor());
            w.write("0\tinit\t" + init[0] + "\n");
            
            for(int i = 1; i <= nbStep; i++) {
                System.out.println("Step : " + i);
            
                DeltaAddGraphGenerator deltaGraph = new DeltaAddGraphGenerator(graph,
                        patch ? ids : Collections.EMPTY_LIST, !patch ? ids : Collections.EMPTY_LIST);
                init = launcher.calcIndice(deltaGraph, new TaskMonitor.EmptyMonitor());
                wd.write(i + "\tinit\t" + init[0] + "\n");
                Object bestId = null;
                double maxVal = -Double.MAX_VALUE;
                for(Object id : ids) {
                    deltaGraph.addElem(id);
                    Double [] res = launcher.calcIndice(deltaGraph, new TaskMonitor.EmptyMonitor());
                    if(res[0] > maxVal) {
                        bestId = id;
                        maxVal = res[0];
                    } 
                        
                    wd.write(i + "\t" + id + "\t" + res[0] + "\n");
                    wd.flush();
                    deltaGraph.reset();
                }
                
                w.write(i + "\t" + bestId + "\t" + maxVal + "\n");
                w.flush();
                wd.flush();

                ids.remove(bestId);
            }
            
            wd.close();
            w.close();
        }
        
    }
    
    private void remGlobal(List<String> args) throws Throwable {
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }
        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            int nParam = indice.getParams().size();
        
            HashMap<String, Object> params = new HashMap<String, Object>();
            for(int i = 0; i < nParam && !args.isEmpty(); i++) {
                String [] tok = args.remove(0).split("=");
                Range r = Range.parse(tok[1]);
                if(r.isUnique())
                    params.put(tok[0], r.min);
                else
                    throw new IllegalArgumentException("No range for indice params in --gtest");
            }

            indice.setParams(params);
        }

        List patchIds = new ArrayList();
        List linkIds = new ArrayList();
        if(args.get(0).startsWith("patch=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            for(String tok : toks)
                patchIds.add(Integer.parseInt(tok));
        } else if(args.get(0).startsWith("fpatch=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> ids = readFile(f);
            for(String id : ids)
                patchIds.add(Integer.parseInt(id));
        }
        if(!args.isEmpty() && args.get(0).startsWith("link=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            linkIds.addAll(Arrays.asList(toks));
        } else if(!args.isEmpty() && args.get(0).startsWith("flink=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> ids = readFile(f);
            linkIds.addAll(ids);
        }
        
        
        
        GraphMetricLauncher launcher = new GraphMetricLauncher(indice, maxCost, true);
        System.out.println("Global indice " + indice.getDetailName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println("Graph " + graph.getName());
            DeltaAddGraphGenerator deltaGraph = new DeltaAddGraphGenerator(graph,
                    patchIds, linkIds);
            System.out.println("Remove " + (graph.getNodes().size()-deltaGraph.getNodes().size()) + " patches and " +
                    (graph.getEdges().size()-deltaGraph.getEdges().size()) + " links");
            Double[] res = launcher.calcIndice(deltaGraph, new TaskMonitor.EmptyMonitor());     

            System.out.println(indName + " : " + res[0] + "\n");
        }
        
    }
    
    private void addLocal(List<String> args) throws Throwable {
        int nbStep = Integer.parseInt(args.remove(0));
        String indName = args.remove(0);
        double maxCost = Double.NaN;
        if(args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.get(0).startsWith("obj=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isUnique())
                params.put(tok[0], r.min);
            else
                throw new IllegalArgumentException("No range for indice params in --ltest");
        }
        // obj=patch|link
        boolean patch = args.remove(0).split("=")[1].equals("patch");

        List lstIds = new ArrayList();
        String [] toks = args.remove(0).split("=")[1].split(",");
        for(String tok : toks)
            if(patch)
                lstIds.add(Integer.parseInt(tok));
            else
                lstIds.add(tok);

        LocalMetric indice = Project.getLocalMetric(indName);
        if(patch && !indice.calcNodes() || !patch && !indice.calcEdges())
            throw new IllegalArgumentException("Indice " + indice.getName() + " is not calculated on selected object type");
        if(indice.hasParams()) {
            if(params.isEmpty())
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --ltest");
            indice.setParams(params);
        }
        
        for(GraphGenerator graph : getGraphs()) {
            HashSet ids = new HashSet(lstIds);
            FileWriter w = new FileWriter(new File(project.getProjectDir(), "ltest-" + graph.getName() + "-" + indice.getDetailName() + ".txt"));
            FileWriter wd = new FileWriter(new File(project.getProjectDir(), "ltest-" + graph.getName() + "-" + indice.getDetailName() + "-detail.txt"));
            wd.write("Step\tId\t"+indice.getShortName()+"\n");
            w.write("Step\tId\t"+indice.getShortName()+"\n");

            System.out.println("Local indice " + indice.getName());

            for(int i = 1; i <= nbStep; i++) {
                System.out.println("Step : " + i);
            
                Map<Object, Double> mapVal = addLocal(graph, patch, ids, indice, maxCost);
                Object bestId = null;
                double maxVal = -Double.MAX_VALUE;
                for(Object id : mapVal.keySet()) {
                    double val = mapVal.get(id);
                    if(val > maxVal) {
                        bestId = id;
                        maxVal = val;
                    }
                    wd.write(i + "\t" + id + "\t" + val + "\n");
                }
                w.write(i + "\t" + bestId + "\t" + maxVal + "\n");
                w.flush();
                wd.flush();

                ids.remove(bestId);
            }
            
            wd.close();
            w.close();
        }
        
    }
    
    private void addPatch(List<String> args) throws Throwable {
        int nbPatch = Integer.parseInt(args.remove(0));
        String indName = args.remove(0);
        
        HashMap<String, Object> params = new HashMap<String, Object>();
        while(!args.get(0).startsWith("gridres=") && !args.get(0).startsWith("pointfile=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isUnique())
                params.put(tok[0], r.min);
            else
                throw new IllegalArgumentException("No range for indice params in --addpatch");
        }
        
        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            if(params.isEmpty())
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --addpatch");
            indice.setParams(params);
        }
        
        if(args.get(0).startsWith("gridres=")) {
            Range rangeRes = Range.parse(args.remove(0).split("=")[1]);
            File capaFile = null;
            int nbMulti = 1;
            int window = 1;
            while(!args.isEmpty()) {
                String arg = args.remove(0);
                if(arg.startsWith("capa=")) {
                    capaFile = new File(arg.split("=")[1]);
                    if(!capaFile.exists()) 
                        throw new IllegalArgumentException("File " + capaFile + " does not exist.");
                } else if(arg.startsWith("multi=")) {
                    arg = arg.split("=")[1];
                    nbMulti = Integer.parseInt(arg.split(",")[0]);
                    window = Integer.parseInt(arg.split(",")[1]);
                }
            }
            
            for(GraphGenerator graph : getGraphs()) {
                if(graph.getLinkset().getTopology() == Linkset.PLANAR) {
                    System.out.println("Planar graph is not supported : " + graph.getName());
                    continue;
                }
                for(Double res : rangeRes.getValues()) {
                    System.out.println("Add patches with graph " + graph.getName() + 
                            " and indice " + indice.getShortName() + " at resolution " + res);
                    AddPatchCommand addPatchCmd = new AddPatchCommand(nbPatch, indice, graph, capaFile, res, nbMulti, window);
                    addPatchCmd.run(new TaskMonitor.EmptyMonitor());
                    addPatchCmd.saveResults();
                }
            }
        } else {
            File pointFile = new File(args.remove(0).split("=")[1]);
            String capaField = null;
            if(!args.isEmpty() && args.get(0).startsWith("capa="))
                capaField = args.remove(0).split("=")[1];
            
            for(GraphGenerator graph : getGraphs()) {
                if(graph.getLinkset().getTopology() == Linkset.PLANAR) {
                    System.out.println("Planar graph is not supported : " + graph.getName());
                    continue;
                }
                System.out.println("Add patches with graph " + graph.getName() + " and indice " + indice.getShortName());
                AddPatchCommand addPatchCmd = new AddPatchCommand(nbPatch, indice, graph, pointFile, capaField);
                addPatchCmd.run(new TaskMonitor.EmptyMonitor());
                addPatchCmd.saveResults();
            }
        }
        
    }

    private Map<Object, Double> addLocal(GraphGenerator graph, boolean patch, Set ids, LocalMetric indice, double maxCost) throws Throwable {

        HashMap<Object, Path> links = new HashMap<Object, Path>();
        if(!patch)
            for(Path link : graph.getLinkset().getPaths())
                links.put(link.getId(), link);

        DeltaAddGraphGenerator deltaGraph = new DeltaAddGraphGenerator(graph,
                patch ? ids : Collections.EMPTY_LIST, !patch ? ids : Collections.EMPTY_LIST);

        HashMap<Object, Double> values = new HashMap<Object, Double>();
        for(Object id : ids) {
            System.out.println("Object : " + id);
            deltaGraph.addElem(id);
            MainFrame.calcLocalIndice(new TaskMonitor.EmptyMonitor(), deltaGraph,
                        indice, maxCost);
            double val;
            if(patch)
                val = ((Number)project.getPatch((Integer)id).getAttribute(indice.getDetailName() + "_" + deltaGraph.getName())).doubleValue();
            else
                val = ((Number)links.get(id).getAttribute(indice.getDetailName() + "_" + deltaGraph.getName())).doubleValue();
            
            values.put(id, val);

            deltaGraph.reset();
        }

        return values;
    }
    
    private List<String> readFile(File f) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(f));
        List<String> list = new ArrayList<String>();
        String l;
        while((l = r.readLine()) != null)
            if(!l.trim().isEmpty())
                list.add(l.trim());
        r.close();
        return list;
    }
    
}
