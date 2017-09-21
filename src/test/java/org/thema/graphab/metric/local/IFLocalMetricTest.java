/*
 * Copyright (C) 2016 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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
package org.thema.graphab.metric.local;

import java.io.IOException;
import org.geotools.graph.structure.Node;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author Gilles Vuidel
 */
public class IFLocalMetricTest {
    
    private static Project project;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        project = ProjectTest.loadCrossProject();
    }
    

    /**
     * Test of calcMetric method, of class F6LocalMetric.
     */
    @Test
    public void testCalcMetric() {
        System.out.println("calcMetric");
        
        GraphGenerator cross = project.getGraph("cross");
        GraphGenerator all = project.getGraph("all");
        IFLocalMetric metric = new IFLocalMetric();
        metric.setParamFromDetailName("_d3_p0.5_beta1");
        assertEquals(0.5*4+1, metric.calcSingleMetric(cross.getNode(3), cross), 1e-15);
        assertEquals(0.5*4+1, metric.calcSingleMetric(all.getNode(3), all), 1e-15);
        
        metric.setParamFromDetailName("_d6_p0.5_beta1");
        assertEquals(0.7071067811865475+0.5*3+1, metric.calcSingleMetric(cross.getNode(1), cross), 1e-15);
        assertEquals(0.7071067811865475+0.5*3+1, metric.calcSingleMetric(cross.getNode(2), cross), 1e-15);
        assertEquals(0.7071067811865475*4+1, metric.calcSingleMetric(cross.getNode(3), cross), 1e-15);
        assertEquals(0.7071067811865475+0.5*3+1, metric.calcSingleMetric(cross.getNode(4), cross), 1e-15);
        assertEquals(0.7071067811865475+0.5*3+1, metric.calcSingleMetric(cross.getNode(5), cross), 1e-15);
        
        metric.setParamFromDetailName("_d1_p1_beta1");
        checkAll(metric, 5);
    }
    
    private void checkAll(LocalSingleMetric metric, double expected) {
        GraphGenerator cross = project.getGraph("cross");
        GraphGenerator all = project.getGraph("all");
        for(Node n : cross.getNodes()) {
            assertEquals(expected, metric.calcSingleMetric(n, cross), 1e-15);
        }
        for(Node n : all.getNodes()) {
            assertEquals(expected, metric.calcSingleMetric(n, all), 1e-15);
        }
    }
    
}
