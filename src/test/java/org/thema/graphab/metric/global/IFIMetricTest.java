/*
 * Copyright (C) 2017 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.graph.Modularity;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.ModGraphGenerator;
import org.thema.graphab.metric.global.SumLocal2GlobalMetric.SumIFInterMetric;
import org.thema.graphab.metric.global.SumLocal2GlobalMetric.SumIFMetric;

/**
 *
 * @author gvuidel
 */
public class IFIMetricTest {
    
    private static Project project;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        project = ProjectTest.loadTestProject();
    }

    /**
     * Test of calcMetric method, of class WilksMetric.
     */
    @Test
    public void testCalcMetric() {
        System.out.println("calcMetric S#IFI");
        GraphGenerator graph = project.getGraph("graph_comp_euclid_1000");
        Modularity mod = new Modularity(graph.getGraph(), new Modularity.OneWeighter());
        mod.partitions();
        ModGraphGenerator graphMod = new ModGraphGenerator(null, graph, mod.getBestPartition());
        SumIFInterMetric IFI = new SumLocal2GlobalMetric.SumIFInterMetric();
        Double[] resultIfi = new GlobalMetricLauncher(IFI).calcMetric(graphMod, true, null);
        assertEquals(1.5685025728220188E12, resultIfi[0], 1e3);
        
        SumIFMetric IF = new SumLocal2GlobalMetric.SumIFMetric();
        Double[] resultIf = new GlobalMetricLauncher(IF).calcMetric(graphMod, true, null);
        
        Double[] result = new GlobalMetricLauncher(IF).calcMetric(graph, true, null);
        
        assertEquals(result[0], resultIfi[0]+resultIf[0], 2e-2 * result[0]);
       
    }
}
