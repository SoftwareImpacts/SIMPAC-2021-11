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
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.global.PCMetric;

/**
 *
 * @author Gilles Vuidel
 */
public class AddPatchCommandTest {
    
    private Project project;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // init 2 threads
        Config.setNodeClass(AddPatchCommandTest.class);
        Config.setParallelProc(2);
    }
    
    
    @Before
    public void setUp() throws IOException {
        project = ProjectTest.loadTestProject();
    }

    /**
     * Test add patch
     * @throws Throwable 
     */
    @Test
    public void testAddPatch() throws Throwable {
        PCMetric indice = new PCMetric();
        indice.setParams(new HashMap<String, Object>() {{put(AlphaParamMetric.DIST, 1000); put(AlphaParamMetric.PROBA, 0.05);}});
        
        AddPatchCommand addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_cout10_500_nopath"), null, 1000, 1, 1);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        double[] metric = new double[] {
            3.288060990921949E-4, 
            3.310782256080401E-4, 
            3.324925948524221E-4, 
            3.3322725793645866E-4, 
            3.337805170520438E-4, 
            3.342439766168192E-4, 
            3.346686013511277E-4, 
            3.3508099455261824E-4, 
            3.3522503024244427E-4, 
            3.3536513772171127E-4, 
            3.3549792861558696E-4};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add grid point patch", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadTestProject();
        
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_cout10_500_nopath"), 
                new File("target/test-classes/org/thema/graphab/patch_alea.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.2880609909219485E-4, 
            3.3185369564855065E-4, 
            3.337312276321283E-4, 
            3.3500099731190305E-4, 
            3.357904466761313E-4, 
            3.361121734573257E-4, 
            3.3641175029575543E-4, 
            3.3662079743374576E-4, 
            3.367542196720986E-4, 
            3.3688149569700414E-4, 
            3.369939798871662E-4};
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add geometry patch shapefile", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadTestProject();
        
        addPatchCmd = new AddPatchCommand(10, indice, project.getGraph("graph_comp_cout10_500_nopath"), 
                new File("target/test-classes/org/thema/graphab/point_alea.shp"), null);
        addPatchCmd.run(new TaskMonitor.EmptyMonitor());
        metric = new double[] {
            3.288060990921949E-4, 
            3.317376569782912E-4, 
            3.3353912037261855E-4, 
            3.343047951381572E-4, 
            3.3460807509385753E-4, 
            3.3483147072926466E-4, 
            3.350273602292435E-4, 
            3.3515221292269176E-4, 
            3.3525530312214334E-4, 
            3.3534463642953213E-4, 
            3.353847648215637E-4 };
        for(int i = 0; i < metric.length; i++) {
            assertEquals("Add point patch shapefile", metric[i], addPatchCmd.getMetricValues().get(i), 1e-12);
        }
        
        project = ProjectTest.loadTestProject();
        indice.setParams(new HashMap<String, Object>() {{put(AlphaParamMetric.DIST, 1000); put(AlphaParamMetric.PROBA, 0.05);}});
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
        
        project = ProjectTest.loadTestProject();
        indice.setParams(new HashMap<String, Object>() {{put(AlphaParamMetric.DIST, 1000); put(AlphaParamMetric.PROBA, 0.05);}});
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
        
        project = ProjectTest.loadTestProject();
        indice.setParams(new HashMap<String, Object>() {{put(AlphaParamMetric.DIST, 1000); put(AlphaParamMetric.PROBA, 0.05);}});
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
        
    }

}
