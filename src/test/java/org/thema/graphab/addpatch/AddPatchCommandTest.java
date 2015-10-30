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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.Config;
import org.thema.common.swing.TaskMonitor;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
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
        
        project = ProjectTest.loadTestProject();
        
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
        
        project = ProjectTest.loadTestProject();
        
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
        
        project = ProjectTest.loadTestProject();
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
        
        project = ProjectTest.loadTestProject();
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
        
        project = ProjectTest.loadTestProject();
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
        
    }

}
