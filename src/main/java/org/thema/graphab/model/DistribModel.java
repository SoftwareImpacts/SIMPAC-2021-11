/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
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

import java.awt.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandedSampleModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.geometry.DirectPosition;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graphab.Project;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.Path;
import org.thema.graphab.links.RasterPathFinder;
import org.thema.graphab.links.SpacePathFinder;
import org.thema.graphab.model.Logistic.LogisticFunction;
import org.thema.graphab.pointset.Pointset;

/**
 * Implementation for Species Distribution Model (SDM)
 * 
 * @author Gilles Vuidel
 */
public class DistribModel {

    private Project project;

    private Pointset exoData;
    private String varName;
    private double alpha;
    private List<String> patchVars;
    private LinkedHashMap<String, GridCoverage2D> extVars;
    private boolean bestModel;
    private boolean multiAttach;
    private double dMax;
    
    private int nVar;
    private double [][] a;
    private double [] y;
    private List<String> varNames;
    private int usedVars;
    private RealVector stdVar;
    private Logistic regression;

    private HashMap<Geometry, HashMap<DefaultFeature, Path>> costCache;

    /**
     * Creates a new distribution model
     * @param project the current project
     * @param data the presence/absence point set
     * @param varName the binary variable (absence = 0, presence =1) in the point set
     * @param alpha the coefficient of exponential decrease of the distance
     * @param patchVars the patch predictive variable
     * @param extVars the raster predictive variable (external data)
     * @param bestModel is testing all combination of variables to find the best model ?
     * @param multiAttach is multiple patch attachment or the nearest only ?
     * @param dMax the max distance for multi attachment
     * @param costCache the distance cache from a previous execution, can be null
     */
    public DistribModel(Project project, Pointset data, String varName, double alpha,
            List<String> patchVars, LinkedHashMap<String, GridCoverage2D> extVars,
            boolean bestModel, boolean multiAttach, double dMax,
            HashMap<Geometry, HashMap<DefaultFeature, Path>> costCache) {
        this.project = project;
        this.exoData = data;
        this.varName = varName;
        this.alpha = alpha;
        this.patchVars = patchVars;
        this.extVars = extVars;
        this.bestModel = bestModel;
        this.multiAttach = multiAttach;
        this.dMax = dMax;
        this.costCache = costCache;
  
        nVar = patchVars.size()+extVars.size();
    }

    /**
     * The method {@link #estimModel } must be called before.
     * @return a 2d array containing all predictive variables values
     */
    public double [][] getVarExp() {
        return a;
    }

    /**
     * The method {@link #estimModel } must be called before.
     * @return the estimated values (ŷ)
     */
    public double [] getVarEstim() {
        return regression.getEstimation();
    }

    /**
     * The method {@link #estimModel } must be called before.
     * @return all the predictive variable names ie. patch variables and external variables
     */
    public List<String> getVarNames() {
        return varNames;
    }

    /**
     *  The method {@link #estimModel } must be called before.
     * @return the logistic model
     */
    public Logistic getLogisticModel() {
        return regression;
    }
    
    /**
     * The method {@link #estimModel } must be called before.
     * @return the variable names used in the model
     */
    public List<String> getUsedVars() {
        List<String> vars = new ArrayList<>();
        for(int j = 0; j < nVar; j++) {
            if (((usedVars >> j) & 1) == 1) {
                vars.add(varNames.get(j));
            }
        }
        return vars;
    }

    /**
     * The method {@link #estimModel } must be called before.
     * @return the constant value of the model
     */
    public double getConstant() {
        return regression.getCoefs()[0];
    }
    
    /**
     * varName must be contained in {@link #getUsedVars() }.
     * The method {@link #estimModel } must be called before.
     * @param varName the predictive variable name
     * @return the coefficient for the predictive variable
     */
    public double getCoef(String varName) {
        return regression.getCoefs()[getUsedVars().indexOf(varName)+1];
    }
    
    /**
     * varName must be contained in {@link #getUsedVars() }.
     * The method {@link #estimModel } must be called before.
     * @param varName the predictive variable name
     * @return the standard coefficient for the predictive variable
     */
    public double getStdCoef(String varName) {
        return getCoef(varName) * stdVar.getEntry(getVarNames().indexOf(varName));
    }

    HashMap<Geometry, HashMap<DefaultFeature, Path>> getCostCache() {
        return costCache;
    }
    
    /**
     * Estimates the model
     * @param monitor the progress monitor
     * @return a string containing the results in human readable format
     * @throws IOException
     * @throws MathException 
     */
    public String estimModel(TaskMonitor monitor) throws IOException, MathException {
        monitor.setProgress(1);
        List<DefaultFeature> data = exoData.getFeatures();

        HashMap<Geometry, HashMap<DefaultFeature, Path>> cache = multiAttach ? new HashMap<Geometry, HashMap<DefaultFeature, Path>>() : null;
        SpacePathFinder pathfinder = multiAttach ? project.getPathFinder(exoData.getLinkset()) : null;

        a = new double[data.size()][nVar];
        y = new double[data.size()];
        varNames = new ArrayList<>();
        int i = 0;
        for(Feature f : data) {
            Coordinate coord = f.getGeometry().getCentroid().getCoordinate();
            y[i] = ((Number)f.getAttribute(varName)).doubleValue();
            if(y[i] != 0 && y[i] != 1) {
                throw new IllegalArgumentException("Variable " + varName + " : values must be 0 or 1.");
            }
            HashMap<DefaultFeature, Path> patchDists = new HashMap<>();
            if(multiAttach) {
                if(costCache != null) {
                    patchDists = costCache.get(f.getGeometry());
                } else {
                    patchDists = pathfinder.calcPaths(coord, dMax, false);
                    cache.put(f.getGeometry(), patchDists);
                }
            }
            if(patchDists.isEmpty()) {
                int idPatch = ((Number)f.getAttribute(Project.EXO_IDPATCH)).intValue();
                DefaultFeature patch = project.getPatch(idPatch);
                double dist = ((Number)f.getAttribute(Project.EXO_COST)).doubleValue();
                patchDists.put(patch, new Path(patch, patch, dist, dist));
            }
            int j;
            for(j = 0; j < patchVars.size(); j++) {
                double sum = 0;
                double weight = 0;
                for(Feature patch : patchDists.keySet()) {
                    double w = Math.exp(-alpha * (exoData.getLinkset().isCostLength() ? patchDists.get(patch).getCost() : patchDists.get(patch).getDist()));
                    sum += ((Number)patch.getAttribute(patchVars.get(j))).doubleValue() * w * w;
                    weight += w;
                }
                a[i][j] = sum / weight;
                if(i == 0) {
                    varNames.add(patchVars.get(j));
                }
            }
            int k = 0;
            for(GridCoverage2D grid : extVars.values()) {
                a[i][j+k] = grid.evaluate((DirectPosition)new DirectPosition2D(coord.x, coord.y), new double[1])[0];
                k++;
                if(i == 0) {
                    varNames.add("ext-" + grid.getName().toString());
                }
            }


            monitor.setProgress(i*50/y.length);
            i++;
        }

        if(multiAttach && costCache == null) {
            costCache = cache;
        }

        RealMatrix A = MatrixUtils.createRealMatrix(a);

        monitor.setProgress(50);
        monitor.setNote("Regression...");


        int nc = (int)Math.pow(2, nVar);
        Logistic bestLog = new Logistic(a, y);
        bestLog.regression();
        int bestI = nc - 1;
        if(bestModel) {
            for (i = 1; i < nc; i++) {
                int nv = Integer.bitCount(i);
                RealMatrix m = MatrixUtils.createRealMatrix(y.length, nv);
                int k = 0;
                for (int j = 0; j < nVar; j++) {
                    if (((i >> j) & 1) == 1) {
                        m.setColumnVector(k++, A.getColumnVector(j));
                    }
                }
                Logistic log = new Logistic(m.getData(), y);
                log.regression();
                if(log.getAIC() < bestLog.getAIC()) {
                    bestLog = log;
                    bestI = i;
                }
                monitor.setProgress(50+i*50/nc);
            }
        }

        usedVars = bestI;
        regression = bestLog;

        RealVector mean = A.preMultiply(new ArrayRealVector(data.size(), 1.0/data.size()));
        stdVar = new ArrayRealVector(nVar);
        for(i = 0; i < nVar; i++) {
            stdVar.setEntry(i, Math.sqrt(Math.pow(A.getColumnVector(i).mapSubtract(mean.getEntry(i)).getNorm(), 2) / data.size()));
        }

        double [] coef = bestLog.regression();
        String msg = "Formule : " + varName + " = " +  String.format("%g", coef[0]);
        String lineStd = "Standard : ";
        int k = 1;
        for(int j = 0; j < nVar; j++) {
            if(((bestI >> j) & 1) == 1) {
                String var = varNames.get(j);
                msg += String.format(" + %g*%s", coef[k], var);
                lineStd += String.format(" + %g*%s", coef[k] * stdVar.getEntry(j), var);
                k++;
            }
        }

        msg += String.format("\n%s\nLikelihood ratio : %g\np : %g\nr2(McFadden) : %g\nAIC : %g",
                lineStd, bestLog.getDiffLikelihood(), bestLog.getProbaTest(), bestLog.getR2(), bestLog.getAIC());

        return msg;
    }

    /**
     * Interpolates the distribution model to the whole area.
     * 
     * @param project the project 
     * @param resol the resolution of the resulting raster
     * @param vars the variables of the model
     * @param coefs the variable coefficients of the model
     * @param alpha the coefficient of exponential decrease of the distance
     * @param extVars the external variables (rasters)
     * @param linkset the linkset used for distance calculation
     * @param multiAttach is multiple patch attachment
     * @param dMax the max distance for multi attachment in the linkset unit
     * @param monitor the progress monitor
     * @return the raster containing the interpolated values of the distribution model
     */
    public static RasterLayer extrapolate(final Project project, final double resol, 
            final List<String> vars, final double [] coefs, final double alpha,
            final Map<String, GridCoverage2D> extVars, final Linkset linkset, final boolean multiAttach,
            final double dMax, ProgressBar monitor)  {
        monitor.setProgress(1);

        final int wi = (int)(project.getZone().getWidth() / resol);
        final int h = (int)(project.getZone().getHeight() / resol);
        final double minx = project.getZone().getMinX() + (project.getZone().getWidth() - wi*resol) / 2 + resol / 2;
        final double maxy = project.getZone().getMaxY() - (project.getZone().getHeight() - h*resol) / 2 - resol / 2;

        final WritableRaster raster = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, wi,
                h, 1, wi, new int[] {0}), null);

        monitor.setNote("Extrapolate...");
        
        final LogisticFunction function = new LogisticFunction(coefs);

        AbstractParallelFTask task = new AbstractParallelFTask(monitor) {
            @Override
            protected Object execute(int start, int end) {
                try {
                    SpacePathFinder pathFinder = project.getPathFinder(linkset);

                    for(int y = start; y < end; y++) {
                        for(int x = 0; x < wi; x++) {
                            Coordinate c = new Coordinate(minx + x*resol, maxy - y*resol);
                            if(isCanceled()) {
                                return null;
                            }
                            if(!project.isInZone(c.x, c.y)) {
                                raster.setSample(x, y, 0, Float.NaN);
                                continue;
                            }
                            HashMap<DefaultFeature, Path> patchDists = new HashMap<>();
                            if(multiAttach) {
                                patchDists = pathFinder.calcPaths(c, dMax, false);
                            }
  
                            if(patchDists.isEmpty()) {
                                double [] d = pathFinder.calcPathNearestPatch(new GeometryFactory().createPoint(c));
                                DefaultFeature patch = project.getPatch((int)d[0]);
                                patchDists.put(patch, new Path(patch, patch, d[1], d[2]));
                            }
                            
                            double [] xVal = new double[coefs.length];
                            xVal[0] = 1;
                            int k = 1;
                            for(String var : vars) {
                                if(var.startsWith("ext-")) {
                                    try {
                                        xVal[k] = extVars.get(var).evaluate((DirectPosition)new DirectPosition2D(c.x, c.y), new double[1])[0];
                                    } catch(PointOutsideCoverageException ex) {
                                        Logger.getLogger(DistribModel.class.getName()).log(Level.FINER, "Point is outside of grid " + var, ex);
                                        xVal[k] = Double.NaN;
                                    }
                                } else {
                                    double sum = 0;
                                    double weight = 0;
                                    for(DefaultFeature patch : patchDists.keySet()) {
                                        double w = Math.exp(-alpha * (linkset.isCostLength() ? patchDists.get(patch).getCost() : patchDists.get(patch).getDist()));
                                        sum += ((Number)patch.getAttribute(var)).doubleValue() * w * w;
                                        weight += w;
                                    }
                                    xVal[k] = sum / weight;
                                }
                                k++;
                            }

                            raster.setSample(x, y, 0, function.value((xVal)));
                        }
                        incProgress(1);
                    }
                } catch(IOException | CannotEvaluateException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            public int getSplitRange() {
                return h;
            }
            @Override
            public void finish(Collection results) { 
            }
            @Override
            public Object getResult() { 
                return null; 
            }
        };

        new ParallelFExecutor(task).executeAndWait();
        if(task.isCanceled()) { 
            return null;
        }
        
        return new RasterLayer("_" + resol, 
                new RasterShape(raster, new Rectangle2D.Double(minx-resol/2, maxy-h*resol+resol/2, wi*resol, h*resol),
                new RasterStyle(), true), project.getCRS());
    }
    
    /**
     * Method for interpolating a patch metric in the whole area.
     * The calculation is parallelized.
     * @param project the project
     * @param resol the resolution of the resulting raster
     * @param var the patch variable to interpolate
     * @param alpha the coefficient of exponential decrease of the distance
     * @param linkset the linkset for the distance calculation
     * @param multiAttach is multiple patch attachment
     * @param dMax the max distance for multi attachment in the linkset unit
     * @param avg is average or just the sum for multi attachment
     * @param monitor the progress monitor
     * @return the raster containing the interpolated values of the patch metric
     */
    public static RasterLayer interpolate(final Project project, final double resol, 
            final String var, final double alpha, final Linkset linkset, final boolean multiAttach,
            final double dMax, final boolean avg, ProgressBar monitor) {

        final int wi = (int)(project.getZone().getWidth() / resol);
        final int h = (int)(project.getZone().getHeight() / resol);
        final double minx = project.getZone().getMinX() + (project.getZone().getWidth() - wi*resol) / 2 + resol / 2;
        final double maxy = project.getZone().getMaxY() - (project.getZone().getHeight() - h*resol) / 2 - resol / 2;

        final WritableRaster raster;

        monitor.setNote("Interpolate...");

        AbstractParallelFTask<RasterLayer, Void> task;
        // calculates the distances from the pixel or from the patches ?
        // It depends on a lot of criteria. The first solution can be used in all cases, but the second is faster for multiAttach in most cases
        if(linkset.getType_dist() == Linkset.EUCLID || !multiAttach || avg || !linkset.isCostLength() || wi*h < project.getPatches().size()) {
            raster = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_DOUBLE, wi, h, 1), null);
            task = new AbstractParallelFTask(monitor) {
                @Override
                protected Object execute(int start, int end) {
                    try {
                        SpacePathFinder pathFinder = project.getPathFinder(linkset);

                        for(int y = start; y < end; y++) {
                            for(int x = 0; x < wi; x++) {
                                Coordinate c = new Coordinate(minx + x*resol, maxy - y*resol);
                                if(isCanceled()) {
                                    return null;
                                }
                                if(!project.isInZone(c.x, c.y)) {
                                    raster.setSample(x, y, 0, Float.NaN);
                                    continue;
                                }
                                HashMap<DefaultFeature, Path> patchDists = new HashMap<>();
                                if(multiAttach) {
                                    patchDists = pathFinder.calcPaths(c, dMax, false);
                                }

                                if(patchDists.isEmpty()) {
                                    double [] d = pathFinder.calcPathNearestPatch(new GeometryFactory().createPoint(c));
                                    DefaultFeature patch = project.getPatch((int)d[0]);
                                    patchDists.put(patch, new Path(patch, patch, d[1], d[2]));
                                }

                                double sum = 0;
                                double weight = 0;
                                for(DefaultFeature patch : patchDists.keySet()) {
                                    double w = Math.exp(-alpha * (linkset.isCostLength() ? patchDists.get(patch).getCost() : patchDists.get(patch).getDist()));
                                    sum += ((Number)patch.getAttribute(var)).doubleValue() * w * (avg ? w : 1);
                                    weight += w;
                                }
                                double val = sum / (avg ? weight : 1);
                                raster.setSample(x, y, 0, val);
                            }
                            incProgress(1);
                        }
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }

                @Override
                public int getSplitRange() {
                    return h;
                }
                @Override
                public void finish(Collection results) {  
                }
                @Override
                public RasterLayer getResult() { 
                    return new RasterLayer("_" + resol, 
                        new RasterShape(raster, new Rectangle2D.Double(minx-resol/2, maxy-h*resol+resol/2, wi*resol, h*resol),
                        new RasterStyle(), true), project.getCRS());
                }
            };
        } else {
            raster = Raster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_DOUBLE, 
                    project.getImageSource().getWidth(), project.getImageSource().getHeight(), 1), new Point(1, 1));
            task = new AbstractParallelFTask(monitor) {
                @Override
                protected Object execute(int start, int end) {
                    try {
                        RasterPathFinder pathFinder = project.getRasterPathFinder(linkset);
                        for(Feature patch : project.getPatches().subList(start, end)) {
                            if(isCanceled()) {
                                return null;
                            }
                            double patchVal = ((Number)patch.getAttribute(var)).doubleValue();
                            Raster distRaster = pathFinder.getDistRaster(patch, dMax);
                            Rectangle r = distRaster.getBounds();
                            
                            for(int y = (int)r.getMinY(); y < r.getMaxY(); y++) {
                                for(int x = (int)r.getMinX(); x < r.getMaxX(); x++) {
                                    double d = distRaster.getSampleDouble(x, y, 0);
                                    if(d == Double.MAX_VALUE) {
                                        continue;
                                    }
                                    double val = patchVal * Math.exp(-alpha * d);
                                    synchronized(this) {
                                        raster.setSample(x, y, 0, raster.getSampleDouble(x, y, 0) + val);
                                    }
                                }
                            }
                            
                            incProgress(1);
                        }
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }

                @Override
                public int getSplitRange() {
                    return project.getPatches().size();
                }
                @Override
                public void finish(Collection results) {  
                }
                @Override
                public RasterLayer getResult() { 
                    return new RasterLayer("_" + project.getResolution(), 
                        new RasterShape(raster, project.getZone(), new RasterStyle(), true), project.getCRS());
                }
            };
        }
        new ParallelFExecutor(task).executeAndWait();
        if(task.isCanceled()) { 
            return null;
        }
        
        return task.getResult();
    }
}
