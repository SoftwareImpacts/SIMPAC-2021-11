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


package org.thema.graphab.pointset;

import org.locationtech.jts.geom.Coordinate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask.IterParallelTask;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.links.CircuitRaster;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.links.SpacePathFinder;
import org.thema.graphab.metric.Circuit;

/**
 * A set of points connected to nearest patch with distance defined by a linkset.
 * The class is serialized in the xml project file.
 * @author Gilles Vuidel
 */
public class Pointset {
    public static final int AG_NONE = 0;
    public static final int AG_SUM = 1;

    public enum Distance {
        LEASTCOST,
        CIRCUIT,
        FLOW,
        CIRCUIT_FLOW
    }
    
    private final String name;
    private final Linkset cost;
    private final double maxCost;
    private final int agregType;

    private transient List<DefaultFeature> features;

    /**
     * Creates a new Pointset.
     * The calculation, saving and loading of the pointset is done by the Project class.
     * 
     * @param name the name of the pointset
     * @param linkset the linkset associated
     * @param maxCost the max distance for agregating point attributes to patches or 0
     * @param agregType the type of agregation : AG_NONE or AG_SUM
     */
    public Pointset(String name, Linkset linkset, double maxCost, int agregType) {
        this.name = name;
        this.cost = linkset;
        this.maxCost = maxCost;
        this.agregType = agregType;
    }

    /**
     * Returns the features representing the point set.
     * {@link#setFeatures} must be called before
     * @return the features representing the point set or null
     */
    public List<DefaultFeature> getFeatures() {
        return features;
    }

    /**
     * Set the features representing the point set.
     * @param features 
     */
    public void setFeatures(List<DefaultFeature> features) {
        this.features = features;
    }

    /**
     * @return the linkset associated with this point set
     */
    public Linkset getLinkset() {
        return cost;
    }

    /**
     * @return the max distance for agregating point attributes to patches or 0
     */
    public double getMaxCost() {
        return maxCost;
    }

    /**
     * @return the name of the point set
     */
    public String getName() {
        return name;
    }

    /**
     * Return true if agregType != AG_NONE and maxCost &gt; 0
     * 
     * @return true if point set fields are summarized in patches
     */
    public boolean isAgreg() {
        return agregType != AG_NONE && maxCost > 0;
    }
    
    @Override
    public String toString() {
        return name;
    }

    /**
     * @return a String containing informations about this point set
     */
    public String getInfo() {
        return "Name : " + name + "\nLinkset : " + cost.getName();
    }
    
    public double [][][] calcSpaceDistanceMatrix(Linkset costDist, Distance type, ProgressBar mon) {
        final List<DefaultFeature> exos = getFeatures();
        final double [][][] distances = new double[exos.size()][exos.size()][2];  
        final List<Coordinate> dests = new ArrayList<>();
        for(Feature f : exos) {
            dests.add(f.getGeometry().getCoordinate());
        }
        IterParallelTask task;
        if(type == Distance.LEASTCOST) {
            final Linkset linkset = costDist.getCostVersion();
            task = new IterParallelTask(exos.size(), mon) {
                @Override
                protected void executeOne(Integer ind) {
                    try {
                        SpacePathFinder pathFinder = linkset.getProject().getPathFinder(linkset);
                        List<double[]> dist = pathFinder.calcPaths(exos.get(ind).getGeometry().getCoordinate(), dests);
                        for(int j = ind+1; j < exos.size(); j++) {
                            distances[ind][j][0] = dist.get(j)[0];
                            distances[ind][j][1] = dist.get(j)[1];
                            distances[j][ind][0] = distances[ind][j][0];
                            distances[j][ind][1] = distances[ind][j][1];
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } 
                }
            };
        } else if(type == Distance.CIRCUIT) {
            final CircuitRaster circuit;
            try {
                circuit = costDist.getProject().getRasterCircuit(costDist.getCircuitVersion());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            task = new IterParallelTask(exos.size(), mon) {
                @Override
                protected void executeOne(Integer ind) {
                    for(int j = ind+1; j < exos.size(); j++) {
                        distances[ind][j][0] = circuit.getODCircuit(dests.get(ind), dests.get(j)).getR();
                        distances[ind][j][1] = Double.NaN;
                        distances[j][ind][0] = distances[ind][j][0];
                        distances[j][ind][1] = Double.NaN;
                    }
                }
            };
        } else {
            throw new IllegalArgumentException("Distance type unknown in raster : " + type);
        }
        new ParallelFExecutor(task).executeAndWait();
        if(task.isCanceled()) {
            throw new CancellationException();
        }
        
        return distances;
    }
    
    public double [][][] calcGraphDistanceMatrix(GraphGenerator graph, Distance type, double alpha, ProgressBar mon) {
        final List<DefaultFeature> exos = getFeatures();
        final double [][][] distances = new double[exos.size()][exos.size()][2];  
        Project project = graph.getProject();
        
        if(graph.getLinkset() != getLinkset()) {
            throw new IllegalArgumentException("The pointset cannot be used with the linkset of the graph " + graph.getName());
        }
        
        switch(type) {
        case LEASTCOST:
            for(int i = 0; i < exos.size(); i++) {
                Feature exo1 = exos.get(i);
                Feature patch1 = project.getPatch((Integer)exo1.getAttribute(Project.EXO_IDPATCH));
                GraphPathFinder finder = graph.getPathFinder(graph.getNode(patch1));
                for(int j = 0; j < exos.size(); j++) {
                    if(i == j) {
                        continue;
                    }
                    Feature exo2 = exos.get(j);
                    Feature patch2 = project.getPatch((Integer)exo2.getAttribute(Project.EXO_IDPATCH));
                    Double dist = finder.getCost(graph.getNode(patch2));
                    if(dist == null) {
                        dist = Double.NaN;
                    } else if(dist == 0) {
                        try {
                            List<double[]> paths = graph.getProject().getPathFinder(cost).calcPaths(exo1.getGeometry().getCoordinate(), Arrays.asList(exo2.getGeometry().getCoordinate()));
                            dist = paths.get(0)[0];
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        dist += ((Number)exo1.getAttribute(Project.EXO_COST)).doubleValue() +
                                ((Number)exo2.getAttribute(Project.EXO_COST)).doubleValue();
                    }
                    distances[i][j][0] = dist;
                }
                mon.incProgress(1);
            }
            break;
        case FLOW:    
            for(int i = 0; i < exos.size(); i++) {
                Feature exo1 = exos.get(i);
                Feature patch1 = project.getPatch((Integer)exo1.getAttribute(Project.EXO_IDPATCH));
                GraphPathFinder finder = graph.getFlowPathFinder(graph.getNode(patch1), alpha);
                for(int j = 0; j < exos.size(); j++) {
                    if(i == j) {
                        continue;
                    }
                    Feature exo2 = exos.get(j);
                    Feature patch2 = project.getPatch((Integer)exo2.getAttribute(Project.EXO_IDPATCH));
                    Double dist = finder.getCost(graph.getNode(patch2));
                    if(dist == null) {
                        dist = Double.NaN;
                    } else if(dist == 0) {
                        try {
                            List<double[]> paths = graph.getProject().getPathFinder(cost).calcPaths(exo1.getGeometry().getCoordinate(), Arrays.asList(exo2.getGeometry().getCoordinate()));
                            dist = alpha * paths.get(0)[0];
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        dist += - Math.log(Project.getPatchCapacity(patch1)*Project.getPatchCapacity(patch2)
                                / Math.pow(project.getTotalPatchCapacity(), 2))
                            + alpha * (((Number)exo1.getAttribute(Project.EXO_COST)).doubleValue() +
                            ((Number)exo2.getAttribute(Project.EXO_COST)).doubleValue());
                    }
                    distances[i][j][0] = dist;
                }
                mon.incProgress(1);
            }
            break;
        case CIRCUIT:
        case CIRCUIT_FLOW:
            Circuit circuit = type == Distance.CIRCUIT_FLOW ? new Circuit(graph, alpha) : new Circuit(graph);

            for(int i = 0; i < exos.size(); i++) {
                Feature exo1 = exos.get(i);
                Feature patch1 = project.getPatch((Integer)exo1.getAttribute(Project.EXO_IDPATCH));

                for(int j = 0; j < exos.size(); j++) {
                    if(i == j) {
                        continue;
                    }
                    Feature exo2 = exos.get(j);
                    Feature patch2 = project.getPatch((Integer)exo2.getAttribute(Project.EXO_IDPATCH));
                    if(patch1.equals(patch2)) {
                        continue;
                    }
                    distances[i][j][0] = circuit.computeR(graph.getNode(patch1), graph.getNode(patch2));
                }
                mon.incProgress(1);
            }
        }
        
        return distances;
    }
    
    public void saveMatrix(double [][][] matrix, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Id1\tId2\tDistance\tLength\n");
            for(int i = 0; i < features.size(); i++) {
                for(int j = 0; j < features.size(); j++) {
                    fw.write(features.get(i).getId() + "\t" + features.get(j).getId() + "\t" + matrix[i][j][0] + "\t" + matrix[i][j][1] + "\n");
                }
            }
        } 
    }
}
