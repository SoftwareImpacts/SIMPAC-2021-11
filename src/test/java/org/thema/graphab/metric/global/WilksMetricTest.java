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
import java.util.Arrays;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class WilksMetricTest {
    
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
        System.out.println("calcMetric");
        GraphGenerator graph = project.getGraph("graph_comp_euclid_1000");
        WilksMetric metric = new WilksMetric();
    
        metric.setParams(new HashMap<String, Object>() {{ 
            put(WilksMetric.ATTRS, Arrays.asList(Project.AREA_ATTR, Project.PERIM_ATTR)); 
            put(WilksMetric.NB_PATCH, 2);
            put(WilksMetric.WEIGHT_AREA, false);
        }});
        assertArrayEquals(new Double[]{0.9002233972616329, 0.9822583533006123, 14.0}, metric.calcMetric(graph));
        
        metric.setParams(new HashMap<String, Object>() {{ 
            put(WilksMetric.ATTRS, Arrays.asList(Project.AREA_ATTR, Project.PERIM_ATTR)); 
            put(WilksMetric.NB_PATCH, 2);
            put(WilksMetric.WEIGHT_AREA, true);
        }});
        assertArrayEquals(new Double[]{1.12318472518711, 1.0, 14.0}, metric.calcMetric(graph));
    
        metric.setParams(new HashMap<String, Object>() {{ 
            put(WilksMetric.ATTRS, Arrays.asList(Project.AREA_ATTR, Project.PERIM_ATTR)); 
            put(WilksMetric.NB_PATCH, 4);
            put(WilksMetric.WEIGHT_AREA, false);
        }});
        assertArrayEquals(new Double[]{0.9677880588729856, 0.9829389692802237, 6.0}, metric.calcMetric(graph));
    }
}
