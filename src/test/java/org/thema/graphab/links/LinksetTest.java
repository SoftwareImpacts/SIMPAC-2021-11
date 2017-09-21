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
package org.thema.graphab.links;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.common.collection.HashMap2D;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;

/**
 *
 * @author gvuidel
 */
public class LinksetTest {
    
    private static Project project;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        project = ProjectTest.loadCrossProject();
    }

    /**
     * Test of getProject method, of class Linkset.
     */
    @Test
    public void testGetProject() {
        System.out.println("getProject");
        assertEquals(project, getLinkset().getProject());
    }


    /**
     * Test of computeCorridor method, of class Linkset.
     */
    @Test
    public void testComputeCorridor() {
        System.out.println("computeCorridor");

        List<Feature> corridors = getLinkset().computeCorridor(Config.getProgressBar(), 3);
        assertEquals(4, corridors.size());
        for(Feature f : corridors) {
            assertEquals(2, f.getGeometry().getArea(), 1e-15);
        }

        corridors = getLinkset().computeCorridor(Config.getProgressBar(), 4);
        assertEquals(4, corridors.size());
        for(Feature f : corridors) {
            assertEquals(6, f.getGeometry().getArea(), 1e-15);
        }
        
        corridors = getLinkset().computeCorridor(Config.getProgressBar(), 5);
        assertEquals(8, corridors.size());
    }

   
    private Linkset getLinkset() {
        return project.getLinksets().iterator().next();
    }
    
}
