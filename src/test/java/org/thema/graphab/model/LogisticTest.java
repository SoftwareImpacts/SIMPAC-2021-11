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
package org.thema.graphab.model;

import org.apache.commons.math.FunctionEvaluationException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Gilles Vuidel
 */
public class LogisticTest {
    
    private Logistic log;
    
    @Before
    public void beforeTest() throws FunctionEvaluationException {
        double [][] x = new double [][]
            {{    1},
            {    2},
            {    3},
            {    4},
            {    5},
            {    6},
            {    7},
            {    8},
            {    9},
            {   10}};
        double [] y = new double [] {0,   0,   0,   0,   1,   0,   1,   0,   1,   1};
        log = new Logistic(x, y);
        log.regression();
    }
    

    /**
     * Test of getCoefs method, of class Logistic.
     */
    @Test
    public void testGetCoefs() {
        System.out.println("getCoefs");
        double[] result = log.getCoefs();
        assertEquals(-4.3574639636, result[0], 1e-10);
        assertEquals(0.6621631919, result[1], 1e-10);
    }

    /**
     * Test of regression method, of class Logistic.
     */
    @Test
    public void testRegression() throws FunctionEvaluationException {
        System.out.println("regression");
        double[] result = log.regression();
        assertEquals(-4.3574639636, result[0], 1e-10);
        assertEquals(0.6621631919, result[1], 1e-10);
    }

    /**
     * Test of getEstimation method, of class Logistic.
     */
    @Test
    public void testGetEstimation() {
        System.out.println("getEstimation");
        double[] result = log.getEstimation();
        assertArrayEquals(new double[]{0.024237912089, 0.0459510807381, 
            0.0854129918268, 0.1533179726988, 0.2598692953702, 
            0.4050456719454, 0.5689769561814, 0.7190676545533, 
            0.8322983920481, 0.9058656471871}, result, 1e-10);
    }

    /**
     * Test of getLikelihood method, of class Logistic.
     */
    @Test
    public void testGetLikelihood() {
        System.out.println("getLikelihood");
        double result = log.getLikelihood();
        assertEquals(0.01343191140, result, 1e-10);
        assertEquals(Math.log(result), log.getLogLikelihood(), 1e-10);
    }

    /**
     * Test of getDiffLikelihood method, of class Logistic.
     */
    @Test
    public void testGetDiffLikelihood() {
        System.out.println("getDiffLikelihood");
        double result = log.getDiffLikelihood();
        assertEquals(4.83998943008, result, 1e-10);
    }

    /**
     * Test of getProbaTest method, of class Logistic.
     */
    @Test
    public void testGetProbaTest() throws Exception {
        System.out.println("getProbaTest");
        double result = log.getProbaTest();
        assertEquals(0.02780706546550471, result, 0.0);
    }

    /**
     * Test of getR2 method, of class Logistic.
     */
    @Test
    public void testGetR2() {
        System.out.println("getR2");
        double result = log.getR2();
        assertEquals(0.3595769336, result, 1e-10);
    }

    /**
     * Test of getAIC method, of class Logistic.
     */
    @Test
    public void testGetAIC() {
        System.out.println("getAIC");
        double result = log.getAIC();
        assertEquals(10.62024391010, result, 1e-10);
    }

    
}
