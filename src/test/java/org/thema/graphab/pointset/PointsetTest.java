/*
 * Copyright (C) 2018 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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
package org.thema.graphab.pointset;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.geotools.feature.SchemaException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.ProgressBar;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.Project;
import org.thema.graphab.ProjectTest;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.pointset.Pointset.Distance;

/**
 *
 * @author gvuidel
 */
public class PointsetTest {
    
    static Project project;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        project = ProjectTest.loadCrossProject();
    }
    

    /**
     * Test of calcSpaceDistanceMatrix method, of class Pointset.
     */
    @Test
    public void testCalcDistanceMatrix() throws SchemaException, IOException {
        System.out.println("calcRasterDistanceMatrix");
        Pointset pointset = new Pointset("test", project.getLinkset("comp"), 10, 0);
        GeometryFactory factory = new GeometryFactory();
        project.addPointset(pointset, Collections.EMPTY_LIST, Arrays.asList(
                new DefaultFeature("1", factory.createPoint(new Coordinate(1, -1))), 
                new DefaultFeature("2", factory.createPoint(new Coordinate(2, -2))), 
                new DefaultFeature("9", factory.createPoint(new Coordinate(9, -9)))), false);
        
        
        double[][][] result = pointset.calcSpaceDistanceMatrix(pointset.getLinkset(), Distance.LEASTCOST, new TaskMonitor.EmptyMonitor());
        assertEquals(Math.sqrt(2), result[0][1][0], 1e-6);
        assertEquals(8*Math.sqrt(2), result[0][2][0], 1e-6);
        assertEquals(7*Math.sqrt(2), result[1][2][0], 1e-6);
        
        project.removePointset("test");
    }

    /**
     * Test of calcGraphDistanceMatrix method, of class Pointset.
     */
    @Test
    public void testCalcGraphDistanceMatrix() throws SchemaException, IOException {
        Pointset pointset = new Pointset("test", project.getLinkset("comp"), 10, 0);
        GeometryFactory factory = new GeometryFactory();
        project.addPointset(pointset, Collections.EMPTY_LIST, Arrays.asList(
                new DefaultFeature("1", factory.createPoint(new Coordinate(1, -5))), 
                new DefaultFeature("1b", factory.createPoint(new Coordinate(1, -6))),
                new DefaultFeature("2", factory.createPoint(new Coordinate(2, -5))), 
                new DefaultFeature("7", factory.createPoint(new Coordinate(7, -5)))), false);
        
        
        double[][][] result = pointset.calcGraphDistanceMatrix(project.getGraph("comp_cross"), Distance.LEASTCOST, Double.NaN, new TaskMonitor.EmptyMonitor());
        assertEquals(1, result[0][1][0], 1e-6);
        assertEquals(1, result[0][2][0], 1e-6);
        assertEquals(6, result[0][3][0], 1e-6);
        assertEquals(Math.sqrt(2), result[1][2][0], 1e-6);
        assertEquals(7, result[1][3][0], 1e-6);
        assertEquals(7, result[2][3][0], 1e-6);
        
        result = pointset.calcGraphDistanceMatrix(project.getGraph("comp_cross"), Distance.FLOW, 0.1, new TaskMonitor.EmptyMonitor());
        assertEquals(0.1, result[0][1][0], 1e-6);
        assertEquals(0.1, result[0][2][0], 1e-6);
        assertEquals(0.1*6 - 3*Math.log(1.0/25), result[0][3][0], 1e-6);
        assertEquals(0.1*Math.sqrt(2), result[1][2][0], 1e-6);
        assertEquals(0.1*7 - 3*Math.log(1.0/25), result[1][3][0], 1e-6);
        assertEquals(0.1*7 - 3*Math.log(1.0/25), result[2][3][0], 1e-6);
        
        project.removePointset("test");
    }
    
}
