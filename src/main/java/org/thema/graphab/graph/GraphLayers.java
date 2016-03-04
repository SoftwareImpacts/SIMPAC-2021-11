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


package org.thema.graphab.graph;

import org.thema.graph.Modularity;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.geotools.graph.structure.Edge;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureGetter;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.style.CircleStyle;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.LineStyle;
import org.thema.drawshape.style.table.FeatureAttributeCollection;
import org.thema.graph.pathfinder.EdgeWeighter;
import org.thema.graph.shape.GraphGroupLayer;
import org.thema.graphab.Project;
import static org.thema.graphab.graph.GraphGenerator.COMPLETE;
import static org.thema.graphab.graph.GraphGenerator.MST;
import static org.thema.graphab.graph.GraphGenerator.THRESHOLD;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.DistProbaPanel;
import org.thema.graphab.util.SerieFrame;

/**
 * GroupLayer representing a graph.
 * It contains 3 layers : nodes, edges and components.
 * @author Gilles Vuidel
 */
public class GraphLayers extends GraphGroupLayer {
    private GraphGenerator graph;
    private CircleStyle circleStyle;

    /**
     * Creates a new GraphLayers
     * @param name the name of the grouplayer
     * @param graph the graph
     * @param crs the coordinate reference system
     */
    public GraphLayers(String name, GraphGenerator graph, CoordinateReferenceSystem crs) {
        super(name, graph.getGraph(), crs);
        this.graph = graph;
        int col = 0;
        switch(graph.getType()) {
            case MST:
                col = 0x7c7e40;
                break;
            case COMPLETE:
                if(graph.getLinkset().getTopology() == Linkset.PLANAR) {
                    col = 0x951012;
                } else {
                    col = 0xA2705E;
                }
                break;
            case THRESHOLD:
                if(graph.getLinkset().getTopology() == Linkset.PLANAR) {
                    col = 0x42407E;
                } else {
                    col = 0x5f91a2;
                }
                break;
        }
        edgeStyle = new LineStyle(new Color(col));
        getEdgeLayer().setStyle(edgeStyle);
        nodeStyle = new FeatureStyle(new Color(0x951012), new Color(0x212d19));
        
        FeatureLayer fl = new FeatureLayer(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Components"), 
            new FeatureGetter() {
                @Override
                public Collection getFeatures() {
                    return GraphLayers.this.graph.getComponentFeatures();
                }
            }, graph.getProject().getZone(), new FeatureStyle(null, Color.BLACK), graph.getProject().getCRS());

        if(graph.getGraph().getEdges().size() > 500000) {
            getEdgeLayer().setVisible(false);
        }
        addLayer(fl);
    }
    
    @Override
    public JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();       

        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("partition")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String modName = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Modularity");
                        final DistProbaPanel distProbaPanel = new DistProbaPanel(1000, 0.05, 1);
                        int res = JOptionPane.showConfirmDialog(null, distProbaPanel, modName, JOptionPane.OK_CANCEL_OPTION);
                        if(res != JOptionPane.OK_OPTION) {
                            return;
                        }
                          
                        Modularity mod = new Modularity(graph.getGraph(), new EdgeWeighter() {
                            private final double alpha = distProbaPanel.getAlpha();
                            private final double beta = distProbaPanel.getA();
                            @Override
                            public double getWeight(Edge e) {
                                return Math.pow(Project.getPatchCapacity(e.getNodeA()) * Project.getPatchCapacity(e.getNodeB()), beta) 
                                    * Math.exp(-alpha*graph.getCost(e));
                            }
                            @Override
                            public double getToGraphWeight(double dist) {
                                return 0;
                            }
                        });

                        mod.partitions();
                        TreeMap<Integer, Double> modularities = mod.getModularities();
                        XYSeriesCollection series = new XYSeriesCollection();

                        XYSeries serie = new XYSeries("mod");
                        for(Integer n : modularities.keySet()) {
                            serie.add(n, modularities.get(n));
                        }
                        series.addSeries(serie);

                        SerieFrame frm = new SerieFrame(modName + " - " + graph.getName(),
                                series, "Nb clusters", modName);
                        frm.pack();
                        frm.setVisible(true);

                        new ModularityDialog(null, graph, mod).setVisible(true);
                    }
                }).start();
            }
        });

        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("OD_matrix")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            graph.calcODMatrix(new File(graph.getProject().getDirectory(), getName() + "-odmatrix.txt"));
                        } catch (IOException ex) {
                            Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(null, "Error : " + ex);
                        }
                    }
                }).start();
            }
        });

        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("OD_matrix_circuit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            graph.calcODMatrixCircuit(new File(graph.getProject().getDirectory(), getName() + "-odmatrix-circuit.txt"));
                        } catch (IOException ex) {
                            Logger.getLogger(GraphGenerator.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(null, "Error : " + ex);
                        }
                    }
                }).start();
            }
        });

        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Set_Comp_Id")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Project project = graph.getProject();
                        ProgressBar progressBar = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Set_Comp_Id") + "...", 
                                project.getPatches().size());
                        String attrName = "comp_" + graph.getName();
                        DefaultFeature.addAttribute(attrName,
                            project.getPatches(), -1);
                        for(Feature comp : graph.getComponentFeatures()) {
                            Object id = comp.getId();
                            for(DefaultFeature patch : (List<DefaultFeature>)project.getPatchIndex()
                                    .query(comp.getGeometry().getEnvelopeInternal())) {
                                if(patch.getGeometry().intersects(comp.getGeometry())) {
                                    patch.setAttribute(attrName, id);
                                    progressBar.incProgress(1);
                                }
                            }
                        }
                        progressBar.close();
                    }
                }).start();
            }
        });

        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Do_you_want_to_remove_the_graph_") + graph.getName() + " ?", java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove"), JOptionPane.YES_NO_OPTION);
                if(res != JOptionPane.YES_OPTION) {
                    return;
                }

                graph.getProject().removeGraph(graph.getName());
            }
        });

        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, graph.getInfo());
            }
        });

        return menu;
    }

    @Override
    protected void createTopoLayers() {
        super.createTopoLayers();
        if(circleStyle == null) {
            Number max = Collections.max(new FeatureAttributeCollection<Double>(getNodeLayer().getFeatures(), Project.CAPA_ATTR));
            Number min = Collections.min(new FeatureAttributeCollection<Double>(getNodeLayer().getFeatures(), Project.CAPA_ATTR));
            circleStyle = new CircleStyle(Project.CAPA_ATTR, min.doubleValue(), max.doubleValue(), new Color(0xcbcba7/*0x951012*/), new Color(0x212d19));
        } else {
            circleStyle.setStyle(nodeStyle);
        }
        getNodeLayer().setStyle(circleStyle);
    }

    @Override
    protected void createSpatialLayers() {
        nodeStyle.setStyle(circleStyle);
        super.createSpatialLayers();
    }
}
