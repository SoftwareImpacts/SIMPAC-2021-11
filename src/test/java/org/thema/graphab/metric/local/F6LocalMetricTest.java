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
public class F6LocalMetricTest {
    
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

        checkAll("_bq1000_dq10_bd1000_dd100", new Double[] {4.0, 4.0, 0.0, 0.0, 0.0, 0.0});
        checkAll("_bq1000_dq0_bd1000_dd100", new Double[] {0.0, 0.0, 4.0, 4.0, 0.0, 0.0});
        checkAll("_bq1000_dq0_bd1000_dd1", new Double[] {0.0, 0.0, 0.0, 0.0, 4.0, 4.0});
        
        GraphGenerator cross = project.getGraph("cross");
        GraphGenerator all = project.getGraph("all");
        F6LocalMetric metric = new F6LocalMetric();
        metric.setParamFromDetailName("_bq1000_dq3_bd1000_dd6");
        assertArrayEquals(new Double[] {0.5, 0.5, 2.0, 2.0, 1.5, 1.5}, metric.calcMetric(cross.getNode(1), cross));
        metric.setParamFromDetailName("_bq1_dq3_bd1000_dd100");
        assertArrayEquals(new Double[] {2.0, 2.0, 2.0, 2.0, 0.0, 0.0}, metric.calcMetric(cross.getNode(3), cross));
        assertArrayEquals(new Double[] {2.0, 2.0, 2.0, 2.0, 0.0, 0.0}, metric.calcMetric(all.getNode(3), all));
    }
    
    private void checkAll(String params, Double[] expected) {
        GraphGenerator cross = project.getGraph("cross");
        GraphGenerator all = project.getGraph("all");
        F6LocalMetric metric = new F6LocalMetric();
        metric.setParamFromDetailName(params);
        for(Node n : cross.getNodes()) {
            assertArrayEquals(expected, metric.calcMetric(n, cross));
        }
        for(Node n : all.getNodes()) {
            assertArrayEquals(expected, metric.calcMetric(n, all));
        }
    }
}
