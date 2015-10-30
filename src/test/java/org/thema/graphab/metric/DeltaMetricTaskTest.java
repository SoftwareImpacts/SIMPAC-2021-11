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
package org.thema.graphab.metric;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thema.common.Config;
import org.thema.common.io.tab.CSVTabReader;
import org.thema.common.swing.TaskMonitor;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.global.DeltaPCMetric;
import org.thema.graphab.metric.global.GlobalMetricLauncher;
import org.thema.parallel.ExecutorService;

/**
 *
 * @author Gilles Vuidel
 */
public class DeltaMetricTaskTest {
    
    private Project project;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // init 2 threads
        Config.setNodeClass(DeltaMetricTaskTest.class);
        Config.setParallelProc(2);
    }
    
    
    @Before
    public void setUp() throws IOException {
        project = ProjectTest.loadTestProject();
    }

    /**
     * Test delta metrics
     * @throws Throwable 
     */
    @Test
    public void testDeltaPCMetric() throws Throwable {
        System.out.println("Delta PC metric");
        
        CSVTabReader r = new CSVTabReader(new File("target/test-classes/org/thema/graphab/patches.csv"));
        r.read("Id");
        int nbGraph = 0;
        DeltaPCMetric deltaPC = new DeltaPCMetric();
        GlobalMetricLauncher launcher = new GlobalMetricLauncher(deltaPC);
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
                ref = ((Number)r.getValue(id, startName + deltaPC.getResultNames()[1] + "_" + gen.getName())).doubleValue();
                assertEquals(startName + deltaPC.getResultNames()[1] + "_" + gen.getName() + " id:" + id, ref, result.get(id)[1], 1e-13);
                ref = ((Number)r.getValue(id, startName + deltaPC.getResultNames()[2] + "_" + gen.getName())).doubleValue();
                assertEquals(startName + deltaPC.getResultNames()[2] + "_" + gen.getName() + " id:" + id, ref, result.get(id)[2], 1e-13);
            }
            nbGraph++;
        } 
        
        assertTrue("Delta no graph tested", nbGraph > 0);  
    }
    
}
