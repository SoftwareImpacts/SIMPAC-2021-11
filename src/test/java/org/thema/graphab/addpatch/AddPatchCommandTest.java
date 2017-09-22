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
package org.thema.graphab.addpatch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.Config;
import org.thema.common.swing.TaskMonitor;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.global.ECMetric;
import org.thema.graphab.metric.global.PCMetric;

/**
 *
 * @author Gilles Vuidel
 */
public class AddPatchCommandTest {
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // init 2 threads
        Config.setNodeClass(AddPatchCommandTest.class);
        Config.setParallelProc(2);
    }

    /**
     * Test add patch
     * @throws Throwable 
     */
    @Test
    public void testAddPatch() throws Throwable {
        Project project = ProjectTest.loadCrossProjectWithCapa();
        ECMetric indice = new ECMetric();
        indice.setParams(new HashMap<String, Object>() {{put(AlphaParamMetric.DIST, 2); put(AlphaParamMetric.PROBA, 0.5);}});
        
        AddPatchCommand addPatchCmd = new AddPatchCommand(5, indice, project.getGraph("comp_euclid"), null, 1, 1, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        double[] metric = new double[] {
            11.158647962981298, 
            11.956813801796578, 
            12.723730091550099, 
            13.464737569559226, 
            14.183897469111928, 
            14.845205515662313};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add euclidean grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadCrossProjectWithCapa();
        addPatchCmd = new AddPatchCommand(5, indice, project.getGraph("comp_euclid"), 
                new File("target/test-classes/org/thema/graphab/grid_patch.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            11.158647962981298, 
            12.626436017620287, 
            13.761826977071465, 
            14.705946868581096, 
            15.611205952825062, 
            16.466635730325656};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add euclidean geom patch shapefile", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadCrossProjectWithCapa();
        addPatchCmd = new AddPatchCommand(5, indice, project.getGraph("comp_euclid"), 
                new File("target/test-classes/org/thema/graphab/grid_point.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            11.158647962981298, 
            11.956813801796578, 
            12.723730091550099, 
            13.464737569559226, 
            14.183897469111928, 
            14.845205515662313};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add euclidean point patch shapefile", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadCrossProjectWithCapa();
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("comp_empty"), null, 1, 2, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            7.416198487095663,
            0,
            9.230290396120584,
            0,
            10.686704113129593,
            0,
            11.813787681427662,
            12.369738136292174,
            12.915998570291446,
            13.44709006975252,
            0,
            14.414881471586924};
        
        for(Integer i : addPatchCmd.getMetricValues().keySet()) {
            assertEquals("Add multi grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadCrossProjectWithCapa();
        addPatchCmd = new AddPatchCommand(5, indice, project.getGraph("comp_cross"), null, 1, 1, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            9.628387410294401,
            10.513996800152892,
            11.261039820843715,
            11.908770703961983,
            12.511244211799747,
            13.066076777904314};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadCrossProjectWithCapa();
        addPatchCmd = new AddPatchCommand(5, indice, project.getGraph("comp_cross"),
                new File("target/test-classes/org/thema/graphab/grid_point.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            9.628387410294401,
            10.513996800152892,
            11.261039820843715,
            11.908770703961983,
            12.511244211799747,
            13.066076777904314};
        for(Integer i : addPatchCmd.getMetricValues().keySet()) {
            assertEquals("Add point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadCrossProjectWithCapa();
        addPatchCmd = new AddPatchCommand(5, indice, project.getGraph("comp_cross"),
                new File("target/test-classes/org/thema/graphab/grid_patch.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            9.628387410294401,
            10.513996800152892,
            11.261039820843715,
            11.908770703961983,
            12.511244211799747,
            13.066076777904314};
        for(Integer i : addPatchCmd.getMetricValues().keySet()) {
            assertEquals("Add polygon patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
    }

}
