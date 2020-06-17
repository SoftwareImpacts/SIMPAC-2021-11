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

package org.thema.graphab;

import org.locationtech.jts.geom.Geometry;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.apache.commons.math.MathException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.SchemaException;
import org.geotools.graph.structure.Edge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.thema.common.Config;
import org.thema.common.ConsoleProgress;
import org.thema.common.JTS;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.IOImage;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graph.Modularity;
import org.thema.graph.pathfinder.EdgeWeighter;
import org.thema.graphab.addpatch.AddPatchCommand;
import org.thema.graphab.graph.DeltaAddGraphGenerator;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.ModGraphGenerator;
import org.thema.graphab.links.CircuitRaster;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.DeltaMetricTask;
import org.thema.graphab.metric.global.GlobalMetricLauncher;
import org.thema.graphab.metric.Metric;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.graphab.metric.local.LocalMetric;
import org.thema.graphab.model.DistribModel;
import org.thema.graphab.model.Logistic;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.pointset.Pointset.Distance;
import org.thema.graphab.util.RSTGridReader;
import org.thema.graphab.util.Range;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AbstractLayerOperation;
import org.thema.parallel.ExecutorService;
import org.thema.parallel.ParallelExecutor;

/**
 * Command Line Interface class.
 * 
 * @author Gilles Vuidel
 */
public class CLITools {

    private Project project;
    private List<Linkset> useLinksets = new ArrayList<>();
    private List<GraphGenerator> useGraphs = new ArrayList<>();
    private List<Pointset> useExos = new ArrayList<>();
    private boolean save = true;
    
    /**
     * Executes the commands from the command line
     * @param argArray the command line arguments
     * @throws IOException
     * @throws SchemaException
     * @throws MathException 
     */
    public void execute(String [] argArray) throws IOException, SchemaException, MathException {
        if(argArray[0].equals("--help")) {
            System.out.println("Usage :\njava -jar graphab.jar --metrics\n" +
                    "java -jar graphab.jar [-proc n] --create prjname landrasterfile habitat=code1,...,coden [nodata=val] [minarea=val] [maxsize=val] [con8] [simp] [dir=path]\n" +
                    "java -jar graphab.jar [-mpi | -proc n] [-nosave] --project prjfile.xml command1 [command2 ...]\n" +
                    "Commands list :\n" +
                    "--show\n" + 
                    "--dem rasterfile\n" +
                    "--linkset distance=euclid|cost [name=linkname] [complete] [maxcost=valcost] [slope=coef] [remcrosspath|nopathsaved] [[code1,..,coden=cost1 ...] codei,..,codej=min:inc:max | extcost=rasterfile]\n" +
                    "--uselinkset linkset1,...,linksetn\n" +
                    "--corridor maxcost=[{]min:inc:max[}] [format=raster|vector] [beta=exp|var=name]\n" +
                    "--graph [name=graphname] [nointra] [threshold=[{]min:inc:max[}]]\n" +
                    "--usegraph graph1,...,graphn\n" +
                    "--cluster d=val p=val [beta=val] [nb=val]\n" +                    
                    "--pointset pointset.shp [name=pointname] [random_absence=value [inpatch|outpatch[=dist]]]\n" +
                    "--usepointset pointset1,...,pointsetn\n" +
                    "--pointdistance type=raster|graph distance=leastcost|circuit|flow|circuitflow [dist=val proba=val]\n" +
                    "--capa [area [exp=value] [code1,..,coden=weight ...]] | [file=capacity.csv id=fieldname capa=fieldname] | [maxcost=[{]valcost[}] codes=code1,code2,...,coden [weight]]\n" +
                    "--gmetric global_metric_name [maxcost=valcost] [param1=[{]min:inc:max[}] [param2=[{]min:inc:max[}] ...]]\n" +
                    "--cmetric comp_metric_name [maxcost=valcost] [param1=[{]min:inc:max[}] [param2=[{]min:inc:max[}] ...]]\n" +
                    "--lmetric local_metric_name [maxcost=valcost] [param1=[{]min:inc:max[}] [param2=[{]min:inc:max[}] ...]]\n" +
                    "--interp name resolution var=patch_var_name d=val p=val [multi=dist_max [sum]]\n" +                    
                    "--model variable distW=min:inc:max [vars=var1,...,varn] [raster=r1,...,rn]\n" +
                    "--delta global_metric_name [maxcost=valcost] [param1=val ...] obj=patch|link [sel=id1,id2,...,idn|fsel=file.txt]\n" +                    
                    "--addpatch npatch global_metric_name [param1=val ...] [gridres=min:inc:max [capa=capa_file] [multi=nbpatch,size]]|[patchfile=file.shp [capa=capa_field]]\n" +
                    "--remelem nstep global_metric_name [maxcost=valcost] [param1=val ...] obj=patch|link [sel=id1,id2,...,idn|fsel=file.txt]\n" +
                    "--gtest nstep global_metric_name [maxcost=valcost] [param1=val ...] obj=patch|link [sel=id1,id2,...,idn|fsel=file.txt]\n" +
                    "--gremove global_metric_name [maxcost=valcost] [param1=val ...] [patch=id1,id2,...,idn|fpatch=file.txt] [link=id1,id2,...,idm|flink=file.txt]\n" +
                    "--metapatch [mincapa=value]\n" +
                    "--landmod zone=filezones.shp id=fieldname code=fieldname [sel=id1,id2,...,idn ] [novoronoi]\n" +

                    "\nmin:inc:max -> val1,val2,val3...");
            return;
        }
        if(argArray[0].equals("--advanced")) {
            System.out.println("Advanced commands :\n" +
                    "--linkset distance=euclid [name=linkname] [complete] [maxcost=valcost] [nopathsaved] [height=rastefile]\n" +
                    "--linkset distance=circuit [name=linkname] [complete[=dmax]] [slope=coef] [[code1,..,coden=cost1 ...] codei,..,codej=min:inc:max | extcost=rasterfile]\n" +
                    "--circuit [corridor=current_max] [optim] [con4] [link=id1,id2,...,idm|flink=file.txt]\n");
            return;
        }
        
        if(argArray[0].equals("--metrics")) {
            showMetrics();
            return;
        }
        
        List<String> args = new ArrayList<>(Arrays.asList(argArray));
        String p = args.remove(0);
        
        // global options
        while(p != null && !p.startsWith("--")) {
            switch (p) {
                case "-proc":
                    String n = args.remove(0);
                    ParallelFExecutor.setNbProc(Integer.parseInt(n));
                    ParallelExecutor.setNbProc(Integer.parseInt(n));
                    break;
                case "-nosave":
                    save = false;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option " + p);
            }
            p = args.isEmpty() ? null : args.remove(0);
        }
        
        Config.setProgressBar(new ConsoleProgress());
        TaskMonitor.setHeadlessStream(new PrintStream(File.createTempFile("java", "monitor")));
        if(p == null) {
            throw new IllegalArgumentException("No command to execute");
        }
        
        switch (p) {
            case "--create":
                project = createProject(args);
                break;
            case "--project":
                p = args.remove(0);
                project = Project.loadProject(new File(p), false);
                break;
            default:
                throw new IllegalArgumentException("Unknown command " + p);
        }
        
        p = args.isEmpty() ? "--show" : args.remove(0);

        // treat each command
        while(p != null) {
            if(p.equals("--model")) {
                batchModel(args);
            } else if(p.equals("--linkset")) {
                createLinkset(args);
            } else if(p.equals("--graph")) {
                createGraph(args);
            } else if(p.equals("--pointset")) {
                createPointset(args);
            } else if(p.equals("--gmetric")) {
                calcGlobalMetric(args);
            } else if(p.equals("--cmetric")) {
                calcCompMetric(args);
            } else if(p.equals("--lmetric")) {
                calcLocalMetric(args);
            } else if(p.equals("--delta")) {
                calcDeltaMetric(args);
            } else if(p.equals("--gtest")) {
                addGlobal(args);
            } else if(p.equals("--addpatch")) {
                addPatch(args);
            } else if(p.equals("--remelem")) {
                remElem(args);
            } else if(p.equals("--gremove")) {
                remGlobal(args);
            } else if(p.equals("--circuit")) {
                circuit(args);
            } else if(p.equals("--corridor")) {
                corridor(args);
            } else if(p.equals("--metapatch")) {
                createMetapatch(args);
            } else if(p.equals("--cluster")) {
                clustering(args);
            } else if(p.equals("--capa")) {
                calcCapa(args);
            } else if(p.equals("--landmod")) {
                landmod(args);
            } else if(p.equals("--interp")) {
                interp(args);
            } else if(p.startsWith("--use")) {
                useObj(p, args);
            } else if(p.startsWith("--show")) {
                showProject();
            } else if(p.equals("--dem")) {
                setDEM(args);
            } else if(p.equals("--pointdistance")) {
                calcPointDistance(args);
            } else {
                throw new IllegalArgumentException("Unknown command " + p);
            }
            p = args.isEmpty() ? null : args.remove(0);
        }

    }

    private void showProject() {
        System.out.println("===== Link sets =====");
        for(Linkset cost : project.getLinksets()) {
            System.out.println(cost.getName());
        }
        System.out.println("\n===== Graphs =====");
        for(GraphGenerator graph : project.getGraphs()) {
            System.out.println(graph.getName());
        }
        System.out.println("\n===== Point sets =====");
        for(Pointset exo : project.getPointsets()) {
            System.out.println(exo.getName());
        }
    }

    private void showMetrics() {
        System.out.println("===== Global metrics =====");
        for(Metric indice : Project.GLOBAL_METRICS) {
            System.out.println(indice.getShortName() + " - " + indice.getName());
            if(indice.hasParams()) {
                System.out.println("\tparams : " + Arrays.deepToString(indice.getParams().keySet().toArray()));
            }
        }
        System.out.println("\n===== Local metrics =====");
        for(Metric indice : Project.LOCAL_METRICS) {
            System.out.println(indice.getShortName() + " : " + indice.getName());
            if(indice.hasParams()) {
                System.out.println("\tparams : " + Arrays.deepToString(indice.getParams().keySet().toArray()));
            }
        }
    }
    
    private void useObj(String p, List<String> args) {
        String param = args.remove(0);
        String [] toks = param.split(",");
        switch (p) {
            case "--uselinkset":
                useLinksets.clear();
                for(String tok : toks) {
                    if(project.getLinkset(tok) != null) {
                        useLinksets.add(project.getLinkset(tok));
                    } else {
                        throw new IllegalArgumentException("Unknown linkset " + tok);
                    }
                }   break;
            case "--usegraph":
                useGraphs.clear();
                for(String tok : toks) {
                    if(project.getGraphNames().contains(tok)) {
                        useGraphs.add(project.getGraph(tok));
                    } else {
                        throw new IllegalArgumentException("Unknown graph " + tok);
                    }
                }   break;
            case "--usepointset":
                useExos.clear();
                for(String tok : toks) {
                    if(project.getPointsetNames().contains(tok)) {
                        useExos.add(project.getPointset(tok));
                    } else {
                        throw new IllegalArgumentException("Unknown pointset " + tok);
                    }
                }   break;
        }
    }

    private Collection<Linkset> getLinksets() {
        return useLinksets.isEmpty() ? project.getLinksets() :  useLinksets;
    }

    private Collection<GraphGenerator> getGraphs() {
        return useGraphs.isEmpty() ? project.getGraphs() :  useGraphs;
    }

    private Collection<Pointset> getExos() {
        return useExos.isEmpty() ? project.getPointsets() : useExos;
    }

    
    private Project createProject(List<String> args) throws IOException, SchemaException {
        String name = args.remove(0);
        File land = new File(args.remove(0));
        Range range = Range.parse(args.remove(0).split("=")[1]);
        HashSet<Integer> patchCodes = new HashSet<>();
        for(Double code : range.getValues()) {
            patchCodes.add(code.intValue());
        }
        double nodata = Double.NaN;
        double minArea = 0;
        double maxSize = 0;
        boolean con8 = false;
        boolean simp = false;
        File dir = new File(".");
        
        // parameter
        while(!args.isEmpty() && !args.get(0).startsWith("--")) {
            String p = args.remove(0);
            String[] tok = p.split("=");
            switch (tok[0]) {
                case "nodata":
                    nodata = Double.parseDouble(tok[1]);
                    break;
                case "minarea":
                    minArea = Double.parseDouble(tok[1]) * 10000; // ha -> m2
                    break;
                case "maxsize":
                    maxSize = Double.parseDouble(tok[1]); // ha -> m2
                    break;
                case "con8":
                    con8 = true;
                    break;
                case "simp":
                    simp = true;
                    break;
                case "dir":
                    dir = new File(tok[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown parameter " + p);
            }
        }
        
        GridCoverage2D coverage;
        if(land.getName().toLowerCase().endsWith(".rst")) {
            coverage = new RSTGridReader(land).read(null);
        } else {
            coverage = IOImage.loadCoverage(land);
        }
        int dataType = coverage.getRenderedImage().getSampleModel().getDataType();
        if(dataType == DataBuffer.TYPE_DOUBLE || dataType == DataBuffer.TYPE_FLOAT) {
            throw new RuntimeException("Image data type is not integer type");
        }
        HashSet<Integer> codes = new HashSet<>();
        RenderedImage img = coverage.getRenderedImage();
        RandomIter r = RandomIterFactory.create(img, null);
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++) {
                codes.add(r.getSample(x, y, 0));
            }
        }

        return new Project(name, new File(dir, name), coverage, new TreeSet<>(codes), patchCodes, nodata, con8, minArea, maxSize, simp);
    }
    
    private void setDEM(List<String> args) throws IOException {
        project.setDemFile(new File(args.remove(0)), save);
    }
    
    private void batchModel(List<String> args) throws IOException, MathException {
        
        String var = args.remove(0);
        String arg = args.remove(0);
        String [] tok = arg.split("=");
        Range rangeW = Range.parse(tok[1]);
        
        final List<String> otherVars;
        if(!args.isEmpty() && args.get(0).startsWith("vars=")) {
            otherVars = Arrays.asList(args.remove(0).split("=")[1].split(","));
        } else {
            otherVars = Collections.EMPTY_LIST;
        }
        
        final LinkedHashMap<String, GridCoverage2D> rasterVars = new LinkedHashMap<>();
        if(!args.isEmpty() && args.get(0).startsWith("raster=")) {
            List<String> rasters = Arrays.asList(args.remove(0).split("=")[1].split(","));
            for(String raster : rasters) {
                File file = new File(raster);
                rasterVars.put(file.getName(), IOImage.loadCoverage(file));
            }
        }

        TreeSet<String> vars = new TreeSet<>(project.getGraphPatchVar(
                getGraphs().iterator().next().getName()));
        for(GraphGenerator graph : getGraphs()) {
            vars.retainAll(project.getGraphPatchVar(graph.getName()));
        }
        vars.addAll(otherVars);
        vars.addAll(rasterVars.keySet());

        try (FileWriter wd = new FileWriter(new File(project.getDirectory(), "model-" + var + "-dW" + tok[1] + ".txt"))) {
            wd.write("Graph\tPointset\tMetric\tDistWeight\tR2\tp-value\tCoef\n");
            for(GraphGenerator graph : getGraphs()) {
                System.out.println(graph.getName());
                List<Pointset> exoDatas = new ArrayList<>();
                for(Pointset exo : getExos()) {
                    if(exo.getLinkset() == graph.getLinkset()) {
                        exoDatas.add(exo);
                    }
                }
                if(exoDatas.isEmpty()) {
                    throw new RuntimeException("No available pointset for graph " + graph.getName());
                }
                for(Pointset exoData : exoDatas) {
                    new ParallelFExecutor(new SimpleParallelTask<String>(new ArrayList<String>(vars)) {
                        @Override
                        protected void executeOne(String v) {
                            System.out.println(graph.getName() + " - " + exoData.getName() + " : " + v);
                            if(!otherVars.contains(v) && !rasterVars.containsKey(v)) {
                                v = v+"_"+graph.getName();
                            }
                            LinkedHashMap<String, GridCoverage2D> mRaster = new LinkedHashMap<>();
                            boolean rast = rasterVars.containsKey(v);
                            if(rast) {
                                mRaster.put(v, rasterVars.get(v));
                            }
                               
                            for(Double d : rast ? Collections.singletonList(1.0) : rangeW.getValues()) {
                                DistribModel model = 
                                        new DistribModel(project, exoData, var, -Math.log(0.05) / d, rast ? Collections.EMPTY_LIST : Arrays.asList(v), mRaster, true, false, 0, null);
                                try {
                                    model.estimModel(new TaskMonitor.EmptyMonitor());
                                    Logistic estim = model.getLogisticModel();
                                    synchronized(CLITools.this) {  
                                        wd.write(String.format(Locale.US, "%s\t%s\t%s\t%g\t%g\t%g\t%g\n",
                                                graph.getName(), exoData.getName(), v, d, estim.getR2(), estim.getProbaTest(), estim.getCoefs()[1]));
                                        wd.flush();
                                    }
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            
                        }
                    }).executeAndWait();
                    
                    
                }
            }
        } 
    }

    private void createLinkset(List<String> args) throws IOException, SchemaException {
        Map<String, String> params = extractAndCheckParams(args, Arrays.asList("distance"), null);
        
        int type = Linkset.PLANAR;
        double threshold = 0;
        String linkName = null;
        int type_dist;

        switch(params.remove("distance")) {
            case "euclid":
                type_dist = Linkset.EUCLID;
                break;
            case "cost":
                type_dist = Linkset.COST;
                break;
            case "circuit":
                type_dist = Linkset.CIRCUIT;
                break;
            default:
                throw new IllegalArgumentException("Unknown linkset distance type");
        }
        if(params.containsKey("name")) {
            linkName = params.remove("name");
        }
        if(params.containsKey("complete")) {
            type = Linkset.COMPLETE;
            String arg = params.remove("complete");
            // for compatibility only use maxcost inplace
            if(arg != null) {
                threshold = Double.parseDouble(arg);
            }
        }
        if(params.containsKey("maxcost")) {
            threshold = Double.parseDouble(params.remove("maxcost"));
        }
        boolean realPaths = true;
        if(params.containsKey("nopathsaved")) {
            params.remove("nopathsaved");
            realPaths = false;
        }
        useLinksets.clear();
        if(type_dist == Linkset.EUCLID) {
            File heightRaster = null;
            if(params.containsKey("height")) {
                heightRaster = new File(params.remove("height"));
            }
            if(linkName == null) {
                linkName = "euclid_" + (type == Linkset.COMPLETE ? "comp" : "plan")+threshold;
            }
            Linkset cost = new Linkset(project, linkName, type, realPaths, threshold, heightRaster);
            project.addLinkset(cost, save);
            useLinksets.add(cost);
        } else {
            boolean circuit = type_dist == Linkset.CIRCUIT;
            double coefSlope = 0;
            if(params.containsKey("slope")) {
                coefSlope = Double.parseDouble(params.remove("slope"));
            }
            boolean removeCrossPath = false;
            if(params.containsKey("remcrosspath")) {
                params.remove("remcrosspath");
                removeCrossPath = true;
            }
            if(params.containsKey("extcost")) {
                File extCost = new File(params.remove("extcost"));
                if(linkName == null) {
                    linkName = (circuit ? "circ_" : "cost_") + extCost.getName();
                } 
                Linkset cost = circuit ? new Linkset(project, linkName, type, null, extCost, true, coefSlope) : 
                        new Linkset(project, linkName, type, Linkset.COST_LENGTH, realPaths, removeCrossPath, threshold, extCost, coefSlope);
                project.addLinkset(cost, save);
                useLinksets.add(cost);
            } else {
                int max = Collections.max(project.getCodes());
                double [] costs = new double[max+1];
                List<Double> dynCodes = null;
                Range rangeCost = null;
                String name = null;
                Set<Integer> codeSet = new HashSet<>();
                codeSet.add((int)project.getNoData());
                for(String param : params.keySet()) {
                    Range codes = Range.parse(param);
                    Range cost = Range.parse(params.get(param));
                    if(cost.isSingle()) {
                        for(Double code : codes.getValues()) {
                            costs[code.intValue()] = cost.getMin();
                            codeSet.add(code.intValue());
                        }
                    }
                    if(rangeCost == null || !cost .isSingle()) {
                        if(rangeCost != null && !rangeCost.isSingle()) {
                            throw new IllegalArgumentException("Only one range can be defined for linkset");
                        }
                        rangeCost = cost;
                        dynCodes = codes.getValues();
                        for(Double code : codes.getValues()) {
                            codeSet.add(code.intValue());
                        }
                        name = param.replace(',', '_');
                    }
                }
                if(!codeSet.containsAll(project.getCodes())){
                    HashSet<Integer> codes = new HashSet<>(project.getCodes());
                    codes.removeAll(codeSet);
                    throw new IllegalArgumentException("--linkset : some codes are missing " + Arrays.deepToString(codes.toArray()));
                }
                boolean multi = !rangeCost.isSingle();
                if(linkName == null) {
                    name = (circuit ? "circ_" : "cost_") + name;
                } else {
                    name = linkName;
                }
                for(Double c : rangeCost.getValues()) {
                    System.out.println("Calc cost " + c);
                    for(Double code : dynCodes) {
                        costs[code.intValue()] = c;
                    }
                    String s = name + (multi ? "-" + c : "");
                    Linkset cost = circuit ? new Linkset(project, s, type, costs, null, true, coefSlope) : 
                            new Linkset(project, s, type, costs, Linkset.COST_LENGTH, realPaths, removeCrossPath, threshold, coefSlope);
                    project.addLinkset(cost, save);
                    useLinksets.add(cost);
                }
            }
        }
    }

    private void createPointset(List<String> args) throws IOException, SchemaException {
        File file = new File(args.remove(0));
        String name = file.getName().substring(0, file.getName().length()-4);
        if(!args.isEmpty() && args.get(0).startsWith("name=")) {
            name = args.remove(0).split("=")[1];
        }
        boolean rand_abs = false;
        double part = 0;
        boolean iPatch = false, oPatch = false;
        double oDist = 0;
        if(!args.isEmpty() && args.get(0).startsWith("random_absence=")) {
            rand_abs = true;
            part = Double.parseDouble(args.remove(0).split("=")[1]);
            if(!args.isEmpty() && args.get(0).startsWith("inpatch")) {
                iPatch = true;
                args.remove(0);
            } else if(!args.isEmpty() && args.get(0).startsWith("outpatch")) {
                oPatch = true;
                String arg = args.remove(0);
                if(arg.contains("=")) {
                    oDist = Double.parseDouble(arg.split("=")[1]);
                }
            }
        }
        
        List<DefaultFeature> features = DefaultFeature.loadFeatures(file);
        final List<String> attrNames;
        if(rand_abs) {
            attrNames = new ArrayList<>(Arrays.asList("presence"));
        } else {
            attrNames = new ArrayList<>();
            for(String attr : features.get(0).getAttributeNames()) {
                 if(features.get(0).getAttribute(attr) instanceof Number) {
                     attrNames.add(attr);
                 }
            }
        }

        if(rand_abs) {
            final boolean inPatch = iPatch, outPatch = oPatch;
            final double outDist = oDist;
            int nb = features.size();
            DefaultFeatureCoverage coverage = new DefaultFeatureCoverage(features);

            double area = project.getZone().getWidth()*project.getZone().getHeight();
            double res = Math.sqrt(area / (part*nb));
            double distMin = res/2;
            
            Rectangle2D rect = project.getZone();
            double dx = rect.getWidth() - Math.ceil((rect.getWidth() - 2*res) / res) * res;
            double dy = rect.getHeight() - Math.ceil((rect.getHeight() - 2*res) / res) * res;
            rect = new Rectangle2D.Double(rect.getX()+dx/2, rect.getY()+dy/2,
                    rect.getWidth()-dx, rect.getHeight()-dy);

            SquareGrid grid = new SquareGrid(JTS.geomFromRect(rect).getEnvelopeInternal(), res);
            DefaultFeatureCoverage<Feature> patchCov = new DefaultFeatureCoverage(project.getPatches());
            final List<DefaultFeature> points = new ArrayList<>();
            
            grid.execute(new AbstractLayerOperation() {
                int i = 1;
                @Override
                public void perform(Cell cell) {
                    if(!coverage.getFeaturesIn(cell.getGeometry()).isEmpty()) {
                        return;
                    }
                    GeometryFactory fact = new GeometryFactory();
                    FeatureCoverage<Feature> zoneCov = null;
                    if(inPatch) {
                        zoneCov = new DefaultFeatureCoverage(patchCov.getFeaturesIn(cell.getGeometry()));
                    } else if(outPatch) {
                        List<Geometry> geoms = new ArrayList<>();
                        for(Feature patch : patchCov.getFeaturesIn(cell.getGeometry())) {
                            geoms.add(outDist == 0 ? patch.getGeometry() : patch.getGeometry().buffer(outDist));
                        }
                        Geometry outGeom = cell.getGeometry().difference(fact.buildGeometry(geoms).buffer(0));
                        List<Feature> outFeatures = new ArrayList<>();
                        for(int i = 0; i < outGeom.getNumGeometries(); i++) {
                            outFeatures.add(new DefaultFeature(i, outGeom.getGeometryN(i)));
                        }
                        zoneCov = new DefaultFeatureCoverage(outFeatures);
                    }
                    if(zoneCov != null && zoneCov.getFeatures().isEmpty()) {
                        return;
                    }
                    boolean good = false;
                    double x = 0, y = 0;
                    int ntest = 0;
                    
                    while(!good && ntest < 100) {
                        if(inPatch) {
                            int ind = (int)(Math.random()*zoneCov.getFeatures().size());
                            Coordinate c = zoneCov.getFeatures().get(ind).getGeometry().intersection(cell.getGeometry()).getInteriorPoint().getCoordinate();
                            x = c.x;
                            y = c.y;
                        } else {
                            x = Math.random() * res + cell.getCentroid().getX() - res/2;
                            y = Math.random() * res + cell.getCentroid().getY() - res/2;
                        }
                        try {
                            good = project.isInZone(x, y);
                        } catch (IOException ex) {
                            Logger.getLogger(CLITools.class.getName()).log(Level.SEVERE, null, ex);
                            good = false;
                        }
                        if(good && outPatch) {
                            good = !zoneCov.getFeaturesIn(fact.createPoint(new Coordinate(x, y))).isEmpty();
                        }
                        int j = 0;
                        while(good && j < points.size()) {
                            if(points.get(j).getGeometry().getCoordinate()
                                    .distance(new Coordinate(x, y)) < distMin) {
                                good = false;
                            }

                            j++;
                        }
                        ntest++;
                    }
                    if(good) {
                        points.add(new DefaultFeature("rand" + i++, new GeometryFactory()
                                .createPoint(new Coordinate(x, y)), attrNames,
                                Arrays.asList(new Object[]{0})));
                    } else {
                       System.err.println("Warning no location for random absence"); 
                    }

                }
            });

            System.out.println("Nb presence : " + features.size() + " - Nb Absence : " + points.size());
            
            for (Feature f : features) {
                points.add(new DefaultFeature(f.getId().toString(), f.getGeometry(),
                    attrNames, Arrays.asList(new Object[]{1})));
            }
            
            features = points;
        }


        useExos.clear();
        for(Linkset cost : getLinksets()) {
            Pointset exo = new Pointset(name + "_" + cost.getName(), cost, 0, Pointset.AG_NONE);
            System.out.println("Add pointset " + exo.getName());
            project.addPointset(exo, attrNames, features, save);
            useExos.add(exo);
        }
    }

    private void createGraph(List<String> args) throws IOException, SchemaException {
        Map<String, String> params = extractAndCheckParams(args, Collections.EMPTY_LIST, Arrays.asList("name", "nointra", "threshold"));
        
        int type = GraphGenerator.COMPLETE;
        boolean intra = true;
        Range range = null;
        String singleName = null;
        
        if(params.containsKey("name")) {
            singleName = params.get("name");
        }
        
        if(params.containsKey("nointra")) {
            intra = false;
        }
        if(params.containsKey("threshold")) {
            type = GraphGenerator.THRESHOLD;
            range = Range.parse(params.get("threshold"));
        }
        
        if(singleName != null && (getLinksets().size() > 1 || range != null && range.getValues().size() > 1)) {
            throw new IllegalArgumentException("name parameter can be used only when creating only one graph");
        }
        
        useGraphs.clear();
        for(Linkset cost : getLinksets()) {
            if(type == GraphGenerator.COMPLETE) {
                String name = singleName == null ? "comp_" + cost.getName() : singleName;
                GraphGenerator g = new GraphGenerator(name, cost, type, 0, intra && cost.isRealPaths());
                System.out.println("Create graph " + g.getName());
                project.addGraph(g, save);
                useGraphs.add(g);
            } else {
                for(Double d : range.getValues(cost)) {
                    String name = singleName == null ? "thresh_" + d + "_" + cost.getName() : singleName;
                    GraphGenerator g = new GraphGenerator(name, cost, type, d, intra && cost.isRealPaths());
                    System.out.println("Create graph " + g.getName());
                    project.addGraph(g, save);
                    useGraphs.add(g);
                }
            }
        }
    }
    
    private void clustering(List<String> args) throws IOException, SchemaException {
        Map<String, String> params = extractAndCheckParams(args, Arrays.asList("d", "p"), Arrays.asList("beta", "nb"));
        final double alpha = AlphaParamMetric.getAlpha(Double.parseDouble(params.get("d")), Double.parseDouble(params.get("p")));
        final double beta = params.containsKey("beta") ? Double.parseDouble(params.get("beta")) : 1;
        final int nb = params.containsKey("nb") ? Integer.parseInt(params.get("nb")) : -1;

        List<GraphGenerator> newGraphs = new ArrayList<>();
        for(final GraphGenerator graph : new ArrayList<>(getGraphs())) {
            Modularity mod = new Modularity(graph.getGraph(), new EdgeWeighter() {
                    @Override
                    public double getWeight(Edge e) {
                        return Math.pow(Project.getPatchCapacity(e.getNodeA()) * Project.getPatchCapacity(e.getNodeB()), beta) 
                            * Math.exp(-alpha*graph.getCost(e));
                    }
                    @Override
                    public double getToGraphWeight(double dist) {
                        return 0;
                    }
                });

            if(nb == -1) {
                mod.setKeepList(Collections.EMPTY_SET);
            } else {
                mod.setKeepList(Collections.singleton(nb));
            }
            mod.partitions();
            
            Set<Modularity.Cluster> part = nb == -1 ? mod.getBestPartition() : mod.getPartition(nb);
            ModGraphGenerator g = new ModGraphGenerator(null, graph, part);
            project.addGraph(g, save);
            newGraphs.add(g);
        }
        
        useGraphs = newGraphs;
    }

    private void calcGlobalMetric(List<String> args) throws IOException {
        if(args.isEmpty()) {
            throw new IllegalArgumentException("Needs a global metric shortname");
        }
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);

        Map<String, Range> ranges = readMetricParams(args);

        GlobalMetric metric = Project.getGlobalMetric(indName);
        System.out.println("Global metric " + metric.getName());
        List<String> paramNames = new ArrayList<>(ranges.keySet());
        
        try (FileWriter fw = new FileWriter(new File(project.getDirectory(), metric.getShortName() + ".txt"))) {
            fw.write("Graph");
            for(String param : paramNames) {
                fw.write("\t" + param);
            }
            for(String resName : metric.getResultNames()) {
                fw.write("\t" + resName);
            }
            fw.write("\n");
            
            for(GraphGenerator graph : getGraphs()) {    
                HashMap<String, Object> params = new HashMap<>();                
                
                int [] indParam = new int[ranges.size()];
                boolean end = false;
                while(!end) {
                    for(int i = 0; i < indParam.length; i++) {
                        params.put(paramNames.get(i), ranges.get(paramNames.get(i)).getValues(graph.getLinkset()).get(indParam[i]));
                    }
                    metric.setParams(params);
                    
                    Double[] res = new GlobalMetricLauncher(metric, maxCost).calcMetric(graph, true, null);
                    System.out.println(graph.getName() + " - " + metric.getDetailName() + " : " + res[0]);
                    
                    fw.write(graph.getName());
                    for(String param : paramNames) {
                        fw.write("\t" + params.get(param));
                    }
                    for(Double val : res) {
                        fw.write("\t" + val);
                    }
                    fw.write("\n");
                    fw.flush();
                    
                    if(indParam.length > 0) {
                        indParam[0]++;
                    }
                    for(int i = 0; i < indParam.length-1; i++) {
                        if(indParam[i] >= ranges.get(paramNames.get(i)).getSize()) {
                            indParam[i] = 0;
                            indParam[i+1]++;
                        } else {
                            break;
                        }
                    }
                    if(indParam.length > 0) {
                        end = indParam[indParam.length-1] >= ranges.get(paramNames.get(indParam.length-1)).getSize();
                    } else {
                        end = true;
                    }
                }
            }
        }
    }
    
    private void calcCompMetric(List<String> args) throws IOException, SchemaException {
        if(args.isEmpty()) {
            throw new IllegalArgumentException("Needs a global metric shortname");
        }
        
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);

        Map<String, Range> ranges = readMetricParams(args);

        GlobalMetric indice = Project.getGlobalMetric(indName);
        System.out.println("Component metric " + indice.getName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println(graph.getName());
            HashMap<String, Object> params = new HashMap<>();     
            List<String> paramNames = new ArrayList<>(ranges.keySet());
            int [] indParam = new int[ranges.size()];
            boolean end = false;
            while(!end) {
                for(int i = 0; i < indParam.length; i++) {
                    params.put(paramNames.get(i), ranges.get(paramNames.get(i)).getValues(graph.getLinkset()).get(indParam[i]));
                }
                indice.setParams(params);

                MainFrame.calcCompMetric(Config.getProgressBar(graph.getName() + " : " + indice.getDetailName()), 
                        graph, indice, maxCost);

                if(indParam.length > 0) {
                    indParam[0]++;
                }
                for(int i = 0; i < indParam.length-1; i++) {
                    if(indParam[i] >= ranges.get(paramNames.get(i)).getSize()) {
                        indParam[i] = 0;
                        indParam[i+1]++;
                    } else {
                        break;
                    }
                }
                if(indParam.length > 0) {
                    end = indParam[indParam.length-1] >= ranges.get(paramNames.get(indParam.length-1)).getSize();
                } else {
                    end = true;
                }
            }

            if(save) {
                project.saveGraphVoronoi(graph.getName());
            }
        }
    }
    
    private void calcLocalMetric(List<String> args) throws IOException, SchemaException {
        if(args.isEmpty()) {
            throw new IllegalArgumentException("Needs a local metric shortname");
        }
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);

        Map<String, Range> ranges = readMetricParams(args);

        LocalMetric indice = Project.getLocalMetric(indName);
        System.out.println("Local metric " + indice.getName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println(graph.getName());
            HashMap<String, Object> params = new HashMap<>();
            List<String> paramNames = new ArrayList<>(ranges.keySet());
            int [] indParam = new int[ranges.size()];
            boolean end = false;
            while(!end) {
                for(int i = 0; i < indParam.length; i++) {
                    params.put(paramNames.get(i), ranges.get(paramNames.get(i)).getValues(graph.getLinkset()).get(indParam[i]));
                }
                indice.setParams(params);
                
                MainFrame.calcLocalMetric(Config.getProgressBar(graph.getName() + " : " + indice.getDetailName()), graph, indice, maxCost);

                if(indParam.length > 0) {
                    indParam[0]++;
                }
                for(int i = 0; i < indParam.length-1; i++) {
                    if(indParam[i] >= ranges.get(paramNames.get(i)).getSize()) {
                        indParam[i] = 0;
                        indParam[i+1]++;
                    } else {
                        break;
                    }
                }
                if(indParam.length > 0) {
                    end = indParam[indParam.length-1] >= ranges.get(paramNames.get(indParam.length-1)).getSize();
                } else {
                    end = true;
                }
            }

            if(save) {
                graph.getLinkset().saveLinks();
                project.savePatch();
            }
        }
    }

    private void calcDeltaMetric(List<String> args) throws IOException {
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);
        HashMap<String, Object> params = new HashMap<>();
        while(!args.get(0).startsWith("obj=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isSingle()) {
                params.put(tok[0], r.getMin());
            } else {
                throw new IllegalArgumentException("No range for metric params in --delta");
            }
        }
        // obj=patch|link
        boolean patch = args.remove(0).split("=")[1].equals("patch");

        List ids = new ArrayList();
        if(!args.isEmpty() && args.get(0).startsWith("sel=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            for(String tok : toks) {
                ids.add(patch ? Integer.parseInt(tok) : tok);
            }
        } else if(!args.isEmpty() && args.get(0).startsWith("fsel=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> lst = readFile(f);
            for(String id : lst) {
                ids.add(patch ? Integer.parseInt(id) : id);
            }
        }

        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            if(params.isEmpty()) {
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --delta");
            }
            indice.setParams(params);
        }
        
        GlobalMetricLauncher launcher = new GlobalMetricLauncher(indice, maxCost);
        System.out.println("Global metric " + indice.getName());
        for(GraphGenerator graph : getGraphs()) {
            try (FileWriter wd = new FileWriter(new File(project.getDirectory(), "delta-" + indice.getDetailName() + "_" + graph.getName() + ".txt"))) {
                wd.write("Id");
                for(String name : indice.getResultNames()) {
                    wd.write("\td_" + name);
                }
                wd.write("\n");
                ProgressBar progress = Config.getProgressBar(graph.getName());
                DeltaMetricTask task = ids.isEmpty() ? new DeltaMetricTask(progress, graph, launcher, patch ? 1 : 2) :
                        new DeltaMetricTask(progress, graph, launcher, ids);
                ExecutorService.execute(task);
                wd.write("Init");
                for(Double val : task.getInit()) {
                    wd.write("\t" + val);
                }
                wd.write("\n");
                Map<Object, Double[]> result = task.getResult();
                for(Object id : new TreeSet(result.keySet())) {
                    wd.write(id.toString());
                    for(Double val : result.get(id)) {
                        wd.write("\t" + val);
                    }
                    wd.write("\n");
                }
            }
        }
    }
    
    private void addGlobal(List<String> args) throws IOException {
        int nbStep = Integer.parseInt(args.remove(0));
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);
        
        HashMap<String, Object> params = new HashMap<>();
        while(!args.get(0).startsWith("obj=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isSingle()) {
                params.put(tok[0], r.getMin());
            } else {
                throw new IllegalArgumentException("No range for metric params in --gtest");
            }
        }
        // obj=patch|link
        boolean patch = args.remove(0).split("=")[1].equals("patch");

        List lstIds = new ArrayList();
        if(args.get(0).startsWith("sel=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            for(String tok : toks) {
                lstIds.add(patch ? Integer.parseInt(tok) : tok);
            }
        } else if(args.get(0).startsWith("fsel=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> lst = readFile(f);
            for(String id : lst) {
                lstIds.add(patch ? Integer.parseInt(id) : id);
            }
        } else {
            throw new IllegalArgumentException("sel or fsel parameter is missing");
        }

        GlobalMetric indice = Project.getGlobalMetric(indName);
        
        if(indice.hasParams()) {
            if(params.isEmpty()) {
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --gtest");
            }
            indice.setParams(params);
        }
        GlobalMetricLauncher launcher = new GlobalMetricLauncher(indice, maxCost);
        
        for(GraphGenerator graph : getGraphs()) {
            HashSet ids = new HashSet(lstIds);
            try (FileWriter w = new FileWriter(new File(project.getDirectory(), "gtest-" + graph.getName() + "-" + indice.getDetailName() + ".txt")); 
                 FileWriter wd = new FileWriter(new File(project.getDirectory(), "gtest-" + graph.getName() + "-" + indice.getDetailName() + "-detail.txt"))) {
                wd.write("Step\tId\t"+indice.getShortName()+"\n");
                w.write("Step\tId\t"+indice.getShortName()+"\n");
                
                System.out.println("Global metric " + indice.getName());
                
                Double [] init = launcher.calcMetric(graph, true, null);
                w.write("0\tinit\t" + init[0] + "\n");
                
                for(int i = 1; i <= nbStep; i++) {
                    System.out.println("Step : " + i);
                    
                    DeltaAddGraphGenerator deltaGraph = new DeltaAddGraphGenerator(graph,
                            patch ? ids : Collections.EMPTY_LIST, !patch ? ids : Collections.EMPTY_LIST);
                    init = launcher.calcMetric(deltaGraph, true, null);
                    wd.write(i + "\tinit\t" + init[0] + "\n");
                    Object bestId = null;
                    double maxVal = -Double.MAX_VALUE;
                    for(Object id : ids) {
                        deltaGraph.addElem(id);
                        Double [] res = launcher.calcMetric(deltaGraph, true, null);
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
            }
        }
        
    }
    
    private void remGlobal(List<String> args) throws IOException {
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);
        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            int nParam = indice.getParams().size();
        
            HashMap<String, Object> params = new HashMap<>();
            for(int i = 0; i < nParam && !args.isEmpty(); i++) {
                String [] tok = args.remove(0).split("=");
                Range r = Range.parse(tok[1]);
                if(r.isSingle()) {
                    params.put(tok[0], r.getMin());
                } else {
                    throw new IllegalArgumentException("No range for indice params in --gremove");
                }
            }

            indice.setParams(params);
        }

        List patchIds = new ArrayList();
        List linkIds = new ArrayList();
        if(args.get(0).startsWith("patch=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            for(String tok : toks) {
                patchIds.add(Integer.parseInt(tok));
            }
        } else if(args.get(0).startsWith("fpatch=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> ids = readFile(f);
            for(String id : ids) {
                patchIds.add(Integer.parseInt(id));
            }
        }
        if(!args.isEmpty() && args.get(0).startsWith("link=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            linkIds.addAll(Arrays.asList(toks));
        } else if(!args.isEmpty() && args.get(0).startsWith("flink=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> ids = readFile(f);
            linkIds.addAll(ids);
        }
        
        GlobalMetricLauncher launcher = new GlobalMetricLauncher(indice, maxCost);
        System.out.println("Global metric " + indice.getDetailName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println("Graph " + graph.getName());
            GraphGenerator deltaGraph = new GraphGenerator(graph, patchIds, linkIds);
            System.out.println("Remove " + (graph.getNodes().size()-deltaGraph.getNodes().size()) + " patches and " +
                    (graph.getEdges().size()-deltaGraph.getEdges().size()) + " links");
            Double[] res = launcher.calcMetric(deltaGraph, true, null);     

            System.out.println(indName + " : " + res[0] + "\n");
        }
    }
    
    private void remElem(List<String> args) throws IOException {
        int nElem = Integer.parseInt(args.remove(0));
        String indName = args.remove(0);
        double maxCost = readMaxCost(args);
        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            int nParam = indice.getParams().size();
        
            HashMap<String, Object> params = new HashMap<>();
            for(int i = 0; i < nParam && !args.isEmpty(); i++) {
                String [] tok = args.remove(0).split("=");
                Range r = Range.parse(tok[1]);
                if(r.isSingle()) {
                    params.put(tok[0], r.getMin());
                } else {
                    throw new IllegalArgumentException("No range for metric params in --remelem");
                }
            }

            indice.setParams(params);
        }
        
        // obj=patch|link
        boolean patch = args.remove(0).split("=")[1].equals("patch");
        
        List ids = new ArrayList();
        if(!args.isEmpty() && args.get(0).startsWith("sel=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            for(String tok : toks) {
                ids.add(patch ? Integer.parseInt(tok) : tok);
            }
        } else if(!args.isEmpty() && args.get(0).startsWith("fsel=")) {
            File f = new File(args.remove(0).split("=")[1]);
            List<String> lst = readFile(f);
            for(String id : lst) {
                ids.add(patch ? Integer.parseInt(id) : id);
            }
        }
        
        GlobalMetricLauncher launcher = new GlobalMetricLauncher(indice, maxCost);
        System.out.println("Global metric " + indice.getDetailName());
        for(GraphGenerator graph : getGraphs()) {
            System.out.println("Graph " + graph.getName());
            List remIds = new ArrayList();
            GraphGenerator gr = graph;
            try (FileWriter wd = new FileWriter(new File(project.getDirectory(), "rem" + (patch ? "patch" : "link") + "-" + indice.getDetailName() + "_" + graph.getName() + ".txt"))) {
                wd.write("Step\tId\t" + indice.getShortName() + "\n");
                for(int step = 1; step <= nElem; step++) {
                    DeltaMetricTask deltaTask = ids.isEmpty() ? 
                            new DeltaMetricTask(new TaskMonitor.EmptyMonitor(), gr, launcher, patch ? 1 : 2) :
                            new DeltaMetricTask(new TaskMonitor.EmptyMonitor(), gr, launcher, ids);
                    ExecutorService.execute(deltaTask);
                    Map<Object, Double[]> result = deltaTask.getResult();
                    double max = Double.NEGATIVE_INFINITY;
                    Object bestId = null;
                    for(Object id : result.keySet()) {
                        Double[] res = result.get(id);
                        if(res[0] > max) {
                            max = res[0];
                            bestId = id;
                        }
                    }
                    double init = deltaTask.getInit()[0];
                    double metric = init - (max * init);
                    remIds.add(bestId);
                    if(step == 1) {
                        wd.write("0\tinit\t" + init + "\n");
                    }
                    wd.write(step + "\t" + bestId + "\t" + metric + "\n");
                    wd.flush();
                    System.out.println("Remove elem " + bestId + " with metric value " + metric);
                    gr = new GraphGenerator(graph, patch ? remIds : Collections.EMPTY_LIST, !patch ? remIds : Collections.EMPTY_LIST);
                }
            }
        } 
    }
    
    private void addPatch(List<String> args) throws IOException, SchemaException {
        int nbPatch = Integer.parseInt(args.remove(0));
        String indName = args.remove(0);
        
        HashMap<String, Object> params = new HashMap<>();
        while(!args.get(0).startsWith("gridres=") && !args.get(0).startsWith("patchfile=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            if(r.isSingle()) {
                params.put(tok[0], r.getMin());
            } else {
                throw new IllegalArgumentException("No range for metric params in --addpatch");
            }
        }
        
        GlobalMetric indice = Project.getGlobalMetric(indName);
        if(indice.hasParams()) {
            if(params.isEmpty()) {
                throw new IllegalArgumentException("Params for " + indice.getName() + " not found in --addpatch");
            }
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
                    if(!capaFile.exists())  {
                        throw new IllegalArgumentException("File " + capaFile + " does not exist.");
                    }
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
                if(graph.isIntraPatchDist()) {
                    System.out.println("Intrapatch distance is not supported : " + graph.getName());
                    continue;
                }
                for(Double res : rangeRes.getValues()) {
                    System.out.println("Add patches with graph " + graph.getName() + 
                            " and metric " + indice.getShortName() + " at resolution " + res);
                    AddPatchCommand addPatchCmd = new AddPatchCommand(nbPatch, indice, graph, capaFile, res, nbMulti, window);
                    addPatchCmd.run(Config.getProgressBar());
                }
            }
        } else {
            File patchFile = new File(args.remove(0).split("=")[1]);
            String capaField = null;
            if(!args.isEmpty() && args.get(0).startsWith("capa=")) {
                capaField = args.remove(0).split("=")[1];
            }
            for(GraphGenerator graph : getGraphs()) {
                if(graph.getLinkset().getTopology() == Linkset.PLANAR) {
                    System.out.println("Planar graph is not supported : " + graph.getName());
                    continue;
                }
                System.out.println("Add patches with graph " + graph.getName() + " and metric " + indice.getShortName());
                AddPatchCommand addPatchCmd = new AddPatchCommand(nbPatch, indice, graph, patchFile, capaField);
                addPatchCmd.run(Config.getProgressBar());
            }
        }
        
    }
    
    private void calcCapa(List<String> args) throws IOException, SchemaException {
        
        CapaPatchDialog.CapaPatchParam params = new CapaPatchDialog.CapaPatchParam();

        String arg = args.remove(0);
        if(arg.startsWith("maxcost=")) {
            if(getLinksets().size() > 1) {
                throw new IllegalArgumentException("--capa command works only for one linkset. Select a linkset with --uselinkset.");
            }
            Linkset linkset = getLinksets().iterator().next();
            params.calcArea = false;
            params.weightCost = false;
            Range maxcost = Range.parse(arg.split("=")[1]);
            params.maxCost = maxcost.getValue(linkset);
            params.costName = linkset.getName();
            String[] tokens = args.remove(0).split("=")[1].split(",");
            params.codes = new HashSet<>();
            for(String tok : tokens) {
                params.codes.add(Integer.parseInt(tok));
            }
            if(!args.isEmpty() && args.get(0).equals("weight")) {
                args.remove(0);
                params.weightCost = true;
            }
        } else if(arg.equals("area")) {
            params.calcArea = true;
            params.exp = 1.0;
            params.codeWeight = new HashMap<>();
            while(!args.isEmpty() && !args.get(0).startsWith("--")) {
                arg = args.remove(0);
                String[] tokens = arg.split("=");
                if(tokens[0].equals("exp")) {
                    params.exp = Double.parseDouble(tokens[1]);
                } else {
                    String[] codes = tokens[0].split(",");
                    double w = Double.parseDouble(tokens[1]);
                    for(String code : codes) {
                        params.codeWeight.put(Integer.parseInt(code), w);
                    }
                }
            }
        } else if(arg.startsWith("file=")) {
            params.importFile = new File(arg.split("=")[1]);
            params.idField = args.remove(0).split("=")[1];
            params.capaField = args.remove(0).split("=")[1];
        } else {
            throw new IllegalArgumentException("Unknown argument " + arg + " for --capa command");
        }
        
        project.setCapacities(params, save);
    }
    
    private void createMetapatch(List<String> args) throws IOException, SchemaException {
        double minCapa = 0;
        if(!args.isEmpty() && args.get(0).startsWith("mincapa=")) {
            minCapa = Double.parseDouble(args.remove(0).split("=")[1]);
        }
        
        if(getGraphs().size() > 1) {
            throw new IllegalArgumentException("--metapatch command works only for one graph. Select a graph with --usegraph.");
        }
        GraphGenerator g = getGraphs().iterator().next();
        File prjFile = project.createMetaPatchProject(project.getName() + "-" + g.getName(), g, 0, minCapa);
        project = Project.loadProject(prjFile, false);
        
        useLinksets.clear();
        useGraphs.clear();
        useExos.clear();
    }
    
    private void landmod(final List<String> args) throws IOException, SchemaException, MathException {
        File fileZone = new File(args.remove(0).split("=")[1]);
        String idField = args.remove(0).split("=")[1];
        String codeField = args.remove(0).split("=")[1];
        Set<String> ids = null;
        if(!args.isEmpty() && args.get(0).startsWith("sel=")) {
            ids = new HashSet<>(Arrays.asList(args.remove(0).split("=")[1].split(",")));
            
        }
        boolean voronoi = true;
        if(!args.isEmpty() && args.get(0).equals("novoronoi")) {
            args.remove(0);
            voronoi = false;
        }
        // in threaded mode, does not manage reentrant call with old executor, solution : set nb proc to one
        ParallelFExecutor.setNbProc(1);
        LandModTask task = new LandModTask(project, fileZone, idField, codeField, voronoi, ids, args);
        ExecutorService.execute(task);

        args.clear();
    }
    
    private void interp(List<String> args) throws IOException {
        String name = args.remove(0);
        double res = Double.parseDouble(args.remove(0));
        String var = args.remove(0).split("=")[1];
        double d = Double.parseDouble(args.remove(0).split("=")[1]);
        double p = Double.parseDouble(args.remove(0).split("=")[1]);
        boolean multi = false;
        boolean avg = true;
        double dmax = Double.NaN;
        if(!args.isEmpty() && args.get(0).startsWith("multi=")) {
            dmax = Double.parseDouble(args.remove(0).split("=")[1]);
            multi = true;
            if(!args.isEmpty() && args.get(0).equals("sum")) {
                args.remove(0);
                avg = false;
            }
        }
        for(Linkset linkset : getLinksets()) {
            RasterLayer raster = DistribModel.interpolate(project, res, var, AlphaParamMetric.getAlpha(d, p), 
                    linkset, multi, dmax, avg, Config.getProgressBar(linkset.getName()));
            raster.saveRaster(new File(project.getDirectory(), name + "-" + linkset.getName() + ".tif"));
        }
    }

    private void circuit(List<String> args) throws IOException, SchemaException {
        final double threshold;
        if(!args.isEmpty() && args.get(0).startsWith("corridor=")) {
            threshold = Double.parseDouble(args.remove(0).split("=")[1]);
        } else {
            threshold = 0;
        }
        boolean optim = false;
        if(!args.isEmpty() && args.get(0).equals("optim")) {
            optim = true;
        }
        boolean con4 = false;
        if(!args.isEmpty() && args.get(0).equals("con4")) {
            con4 = true;
        }
        final Set<String> linkIds = new HashSet<>();
        if(!args.isEmpty() && args.get(0).startsWith("link=")) {
            String [] toks = args.remove(0).split("=")[1].split(",");
            linkIds.addAll(Arrays.asList(toks));
        } else if(!args.isEmpty() && args.get(0).startsWith("flink=")) {
            File f = new File(args.remove(0).split("=")[1]);
            linkIds.addAll(readFile(f));
        }
        
        for(Linkset link : getLinksets()) {
            if(link.getType_dist() == Linkset.EUCLID) {
                continue;
            }
            System.out.println("Linkset : " + link.getName());
            final CircuitRaster circuit = link.isExtCost() ? 
                    new CircuitRaster(project, project.getExtRaster(link.getExtCostFile()), !con4, optim, link.getCoefSlope()) :
                    new CircuitRaster(project, project.getImageSource(), link.getCosts(), !con4, optim, link.getCoefSlope());
                    
            final File dir = new File(project.getDirectory(), link.getName() + "-circuit");
            dir.mkdir();
            final List<DefaultFeature> corridors;
            try (FileWriter fw = new FileWriter(new File(dir, "resistances.csv"))) {
                fw.write("Id1,Id2,R\n");
                corridors = new ArrayList<>();
                SimpleParallelTask task = new SimpleParallelTask<Path>(link.getPaths()) {
                    @Override
                    protected void executeOne(Path p) {
                        try {
                            if(!linkIds.isEmpty() && !linkIds.contains((String)p.getId())) {
                                return;
                            }
                            long t1 = System.currentTimeMillis();
                            CircuitRaster.PatchODCircuit odCircuit = circuit.getODCircuit(p.getPatch1(), p.getPatch2());
                            odCircuit.solve();
                            long t2 = System.currentTimeMillis();
                            synchronized (CLITools.this) {
                                System.out.println(p.getPatch1() + " - " + p.getPatch2() + " : " + odCircuit.getZone());
                                System.out.print("R : " + odCircuit.getR());
                                System.out.print(" - cost : " + p.getCost());
                                System.out.println(" - time : " + (t2 - t1) / 1000.0 + "s");
                                System.out.println("Nb iteration : " + odCircuit.getNbIter());
                                System.out.println("Err max : " + odCircuit.getErrMax());
                                System.out.println("Err max Wo 2 : " + odCircuit.getErrMaxWithoutFirst());
                                System.out.println("Err sum : " + odCircuit.getErrSum());
                                fw.write(p.getPatch1() + "," + p.getPatch2() + "," + odCircuit.getR() + "\n");
                                fw.flush();
                                Raster raster = odCircuit.getCurrentMap();
                                new RasterLayer("", new RasterShape(raster, JTS.envToRect(odCircuit.getEnvelope()), new RasterStyle(), true), project.getCRS())
                                        .saveRaster(new File(dir, p.getPatch1() + "-" + p.getPatch2() + "-cur.tif"));
                                if (threshold > 0) {
                                    raster = odCircuit.getCorridorMap(threshold);
                                    new RasterLayer("", new RasterShape(raster, JTS.envToRect(odCircuit.getEnvelope()), new RasterStyle(), true), project.getCRS())
                                            .saveRaster(new File(dir, p.getPatch1() + "-" + p.getPatch2() + "-cor.tif"));
                                    Geometry poly = odCircuit.getCorridor(threshold);
                                    if (!poly.isEmpty()) {
                                        corridors.add(new DefaultFeature(p.getPatch1() + "-" + p.getPatch2(), poly));
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(CLITools.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                new ParallelFExecutor(task).executeAndWait();
            }
            if(threshold > 0) { 
                DefaultFeature.saveFeatures(corridors, new File(dir, "corridor-" + threshold + ".shp"), project.getCRS());
            }
        }
    }
    
    private void corridor(List<String> args) throws IOException, SchemaException {
        Map<String, String> params = extractAndCheckParams(args, Arrays.asList("maxcost"), Arrays.asList("format", "beta", "var"));
                
        Range rMax = Range.parse(params.get("maxcost"));
        boolean raster = "raster".equals(params.get("format"));
        Double beta = null;
        if(params.containsKey("beta")) {
            beta = Double.parseDouble(params.get("beta"));
        }
        String var = null;
        if(params.containsKey("var")) {
            var = params.get("var");
        }
        
        for(Linkset link : getLinksets()) {
            if(link.getType_dist() == Linkset.EUCLID) {
                continue;
            }

            System.out.println("Linkset : " + link.getName());
            for(double maxCost : rMax.getValues(link)) {
                ProgressBar progress = Config.getProgressBar("maxCost : " + maxCost);
                if(raster) {
                    Raster corridors = link.computeRasterCorridor(progress, maxCost, beta, var);
                    Rectangle2D zone = project.getZone();
                    double res = project.getResolution();
                    Rectangle2D extZone = new Rectangle2D.Double(
                        zone.getX()-res, zone.getY()-res, zone.getWidth()+2*res, zone.getHeight()+2*res);
                    new RasterLayer("corridor", new RasterShape(corridors, extZone, new RasterStyle(), true), project.getCRS())
                            .saveRaster(new File(project.getDirectory(), link.getName() +
                                "-corridor-" + maxCost + "-" + (beta != null ? ("beta"+beta) : var != null ? var : "1") + ".tif"));
                } else {
                    List<Feature> corridors = link.computeCorridor(progress, maxCost);
                    DefaultFeature.saveFeatures(corridors, new File(project.getDirectory(), link.getName() +
                            "-corridor-" + maxCost + ".shp"), project.getCRS());
                }
            }
        }
    }
    
    private void calcPointDistance(List<String> args) throws IOException {
        Map<String, String> params = extractAndCheckParams(args, Arrays.asList("type", "distance"), Arrays.asList("dist", "proba"));
        String distType = params.get("distance");
        Distance type;
        switch (distType) {
            case "leastcost":
                type = Distance.LEASTCOST;
                break;
            case "circuit":
                type = Distance.CIRCUIT;
                break;
            case "flow":
                type = Distance.FLOW;
                break;
            case "circuitflow":
                type = Distance.CIRCUIT_FLOW;
                break;
            default:
                throw new IllegalArgumentException("Unknown distance type : " + distType);
        }
        for(Pointset pointset : getExos()) {
            if(params.get("type").equals("raster")) {
                for(Linkset linkset : getLinksets()) {
                    double [][][] distances = pointset.calcRasterDistanceMatrix(linkset, type, new TaskMonitor.EmptyMonitor());
                    pointset.saveMatrix(distances, new File(project.getDirectory(), "distance_" + pointset.getName() + "_raster_" + linkset.getName() + "-" + type + ".txt"));
                }
            } else {
                double alpha = Double.NaN;
                if(type == Distance.FLOW || type == Distance.CIRCUIT_FLOW) {
                    alpha = AlphaParamMetric.getAlpha(Double.parseDouble(params.get("dist")), Double.parseDouble(params.get("proba")));
                }
                for(GraphGenerator graph : getGraphs()) {
                    double [][][] distances = pointset.calcGraphDistanceMatrix(graph, type, alpha, new TaskMonitor.EmptyMonitor());
                    pointset.saveMatrix(distances, new File(project.getDirectory(), "distance_" + pointset.getName() + "_graph_" + graph.getName() + "-" + type +
                            (Double.isNaN(alpha) ? "" : "-d"+params.get("dist")+"-p"+params.get("proba")) + ".txt"));
                }
            }
        }
    }
    
    private List<String> readFile(File f) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String l;
            while((l = r.readLine()) != null) {
                if(!l.trim().isEmpty()) {
                    list.add(l.trim());
                }
            }
        }
        return list;
    }
    
    private double readMaxCost(List<String> args) {
        double maxCost = Double.NaN;
        if(!args.isEmpty() && args.get(0).startsWith("maxcost")) {
            String [] tok = args.remove(0).split("=");
            maxCost = Double.parseDouble(tok[1]);
        }
        return maxCost;
    }

    private Map<String, Range> readMetricParams(List<String> args) {
        HashMap<String, Range> ranges = new LinkedHashMap<>();
        while(!args.isEmpty() && args.get(0).contains("=")) {
            String [] tok = args.remove(0).split("=");
            Range r = Range.parse(tok[1]);
            ranges.put(tok[0], r);
        }
        return ranges;
    }
    
    private static Map<String, String> extractAndCheckParams(List<String> args, List<String> mandatoryParams, List<String> optionalParams) {
        Map<String, String> params = new LinkedHashMap<>();
                
        while(!args.isEmpty() && !args.get(0).startsWith("--")) {
            String arg = args.remove(0);
            if(arg.contains("=")) {
                String[] tok = arg.split("=");
                params.put(tok[0], tok[1]);
            } else {
                params.put(arg, null);
            }
        }
        
        // check mandatory parameters
        if(!params.keySet().containsAll(mandatoryParams)) {
            HashSet<String> set = new HashSet<>(mandatoryParams);
            set.removeAll(params.keySet());
            throw new IllegalArgumentException("Mandatory parameters are missing : " + Arrays.deepToString(set.toArray()));
        }
        
        // check unknown parameters if optionalParams is set
        if(optionalParams != null) {
            HashSet<String> set = new HashSet<>(params.keySet());
            set.removeAll(mandatoryParams);
            set.removeAll(optionalParams);
            if(!set.isEmpty()) {
                throw new IllegalArgumentException("Unknown parameters : " + Arrays.deepToString(set.toArray()));
            }
        }
        
        return params;
    }
    
}
