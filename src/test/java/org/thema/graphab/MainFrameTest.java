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
package org.thema.graphab;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.Config;
import org.thema.common.io.tab.CSVTabReader;
import org.thema.common.swing.TaskMonitor;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.local.LocalMetric;

/**
 *
 * @author Gilles Vuidel
 */
public class MainFrameTest {
    
    private Project project;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // init 2 threads
        Config.setNodeClass(MainFrameTest.class);
        Config.setParallelProc(2);
        // load all metrics
        Project.loadPluginMetric(MainFrameTest.class.getClassLoader());
    }
    
    @Before
    public void setUp() throws IOException {
        project = ProjectTest.loadTestProject();
    }


    /**
     * Test of calcLocalMetric method, of class MainFrame.
     * Test all local metrics.
     */
    @Test
    public void testCalcLocalMetric() throws IOException {
        CSVTabReader r = new CSVTabReader(new File("target/test-classes/org/thema/graphab/patches.csv"));
        r.read("Id");
        HashSet<String> testIndices = new HashSet<>();
        
        for(String varName : r.getVarNames()) {
            // pour les attributs Area, Perim et Capacity et les delta métriques
            if(!varName.contains("_") || varName.startsWith("d_")) { 
                continue;
            }
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
            System.out.println("Test local metric : " + varName);
            double err = isCircuit(indName) ? 1e-4 : 1e-9;
            LocalMetric metric = Project.getLocalMetric(indName);
            metric.setParamFromDetailName(varName.replace("_"+gen.getName(), ""));
            MainFrame.calcLocalMetric(new TaskMonitor.EmptyMonitor(), gen, metric, Double.NaN);
            for(Object id : r.getKeySet()) {
                double ref = ((Number)r.getValue(id, varName)).doubleValue();
                
                double delta = isCircuit(indName) && ref < 1 ? 1e-5 : ref*err;
                assertEquals(varName + " id:" + id, ref, (Double)project.getPatch((Integer)id).getAttribute(varName), delta);
            }
            testIndices.add(indName);
        }
    }

    private static boolean isCircuit(String s) {
        return s.equals("BCCirc") || s.equals("CF") || s.equals("PCF");
    }
    
}
