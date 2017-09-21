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
package org.thema.graphab.metric.global;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.Config;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author Gilles Vuidel
 */
public class GlobalMetricLauncherTest {
    
    private Project project;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // init 2 threads
        Config.setNodeClass(GlobalMetricLauncherTest.class);
        Config.setParallelProc(2);
        // load all metrics
        Project.loadPluginMetric(GlobalMetricLauncherTest.class.getClassLoader());
    }
    
    
    @Before
    public void setUp() throws IOException {
        project = ProjectTest.loadTestProject();
    }
    
    /**
     * Test of calcMetric method, of class GlobalMetricLauncher.
     * Test all global metrics
     */
    @Test
    public void testCalcMetric() {
        HashMap<String, Double> resIndices = new HashMap<String, Double>() {{
            put("EC_d1000_p0.05-graph_comp_cout10", Math.sqrt(2.597275864295179E-4) * project.getArea());
            put("EC_d1000_p0.05-graph_comp_cout10_500_nopath", Math.sqrt(3.2880609909219453E-4) * project.getArea());
            put("EC_d1000_p0.05-graph_comp_euclid_1000_nointra", Math.sqrt(9.581715576493942E-5) * project.getArea());
            put("EC_d1000_p0.05-graph_plan_cout10_300", Math.sqrt(2.3271157678692673E-4) * project.getArea());            
            put("EC_d1000_p0.05-graph_plan_cout10_mst", Math.sqrt(1.9835638747391063E-4) * project.getArea());
            put("EC_d1000_p0.05-graph_plan_cout1_len", Math.sqrt(6.69054229355518E-5) * project.getArea());
            
            put("PC_d1000_p0.05-graph_comp_cout10", 2.597275864295179E-4);
            put("PC_d1000_p0.05-graph_comp_cout10_500_nopath", 3.2880609909219453E-4);
            put("PC_d1000_p0.05-graph_comp_euclid_1000_nointra", 9.581715576493942E-5);
            put("PC_d1000_p0.05-graph_plan_cout10_300", 2.3271157678692673E-4);            
            put("PC_d1000_p0.05-graph_plan_cout10_mst", 1.9835638747391063E-4);
            put("PC_d1000_p0.05-graph_plan_cout1_len", 6.69054229355518E-5);
//            put("PC_d1000_p0.05-graph_plan_euclid", 7.045308587598926E-5);
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
            put("ECCirc_d1000_p0.05-graph_comp_cout10", 7164952.08893959);
        }};
        
        HashSet<String> testIndices = new HashSet<>();
        for(String varName : resIndices.keySet()) {
            System.out.println("Test global metric : " + varName);
            String indName = varName.substring(0, varName.indexOf("-"));
            if(indName.contains("_")) {
                indName = indName.substring(0, indName.indexOf("_"));
            }
            GlobalMetric metric = Project.getGlobalMetric(indName);
            metric.setParamFromDetailName(varName.substring(0, varName.indexOf("-")));
            GraphGenerator gen = project.getGraph(varName.substring(varName.indexOf("-")+1));
            GlobalMetricLauncher launcher = new GlobalMetricLauncher(metric);
            Double[] res = launcher.calcMetric(gen, true, null);
            double err = 1e-9;
            assertEquals("Metric " + metric.getDetailName() + "-" + gen.getName(), resIndices.get(metric.getDetailName() + "-" + gen.getName()), 
                    res[0], res[0]*err);
            testIndices.add(indName);
        }
        
        // The Wilks metric is not tested for the moment
        assertEquals("Check all global metrics", Project.getGlobalMetricsFor(Project.Method.GLOBAL).size()-1, testIndices.size());
        
    }
    
}
