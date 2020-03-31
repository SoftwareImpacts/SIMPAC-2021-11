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


package org.thema.graphab.links;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.ChartFrame;
import org.jfree.data.statistics.Regression;
import org.thema.common.Config;
import org.thema.common.collection.HashMap2D;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureGetter;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.LineStyle;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graphab.MainFrame;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Layer for a linkset.
 * 
 * @author Gilles Vuidel
 */
public class LinkLayer extends FeatureLayer {
    private Linkset linkset;

    /**
     * Creates a new layer for the linkset
     * @param linkset the linkset
     */
    public LinkLayer(final Linkset linkset) {
        super(linkset.getName(), new FeatureGetter<Path>() {
            @Override
            public Collection<Path> getFeatures() {
                return linkset.getPaths();
            }
        }, linkset.getProject().getZone(), new LineStyle(new Color(linkset.getTopology() == Linkset.PLANAR ? 0x25372b : 0xb8c45d)), linkset.getProject().getCRS());
        this.linkset = linkset;
    }

    @Override
    public JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> graphNames = new ArrayList<>();
                for (GraphGenerator g : linkset.getProject().getGraphs()) {
                    if (g.getLinkset().getName().equals(getName())) {
                        graphNames.add(g.getName());
                    }
                }
                int res = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Do_you_want_to_remove_the_links_") + getName() + " ?" + (!graphNames.isEmpty() ? "\nGraph " + Arrays.deepToString(graphNames.toArray()) + java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("_will_be_removed.") : ""), java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove"), JOptionPane.YES_NO_OPTION);
                if (res != JOptionPane.YES_OPTION) {
                    return;
                }
                try {
                    linkset.getProject().removeLinkset(linkset, true);
                    linkset.getProject().getLinksetLayers().removeLayer(LinkLayer.this);
                } catch (IOException ex) {
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        menu.add(new AbstractAction("Corridor...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                final CorridorDialog dlg = new CorridorDialog(null, enabled);
                dlg.setVisible(true);
                if(!dlg.isOk) {
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(dlg.raster) {
                            Raster corridors = linkset.computeRasterCorridor(Config.getProgressBar("Corridor..."), dlg.maxCost, null, null);
                            RasterLayer l = new RasterLayer(linkset.getName() +
                                    "-corridor-" + dlg.maxCost, new RasterShape(corridors, linkset.getProject().getZone(), new RasterStyle(), true), linkset.getProject().getCRS());
                            l.setRemovable(true);
                            linkset.getProject().getAnalysisLayer().addLayerFirst(l);
                            try {
                                l.saveRaster(new File(linkset.getProject().getDirectory(), linkset.getName() +
                                        "-corridor-" + dlg.maxCost + ".tif"));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            List<Feature> corridors = linkset.computeCorridor(Config.getProgressBar("Corridor..."), dlg.maxCost);
                            FeatureLayer l = new FeatureLayer(linkset.getName() +
                                    "-corridor-" + dlg.maxCost, corridors, new FeatureStyle(new Color(32, 192, 0, 50), null));
                            l.setRemovable(true);
                            linkset.getProject().getAnalysisLayer().addLayerFirst(l);

                            try {
                                DefaultFeature.saveFeatures(corridors, new File(linkset.getProject().getDirectory(), linkset.getName() +
                                        "-corridor-" + dlg.maxCost + ".shp"), linkset.getProject().getCRS());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }).start();
                
                
            }
        });
        
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Dist2Cost")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DistanceConvDialog dlg = new DistanceConvDialog(null, linkset, true);
                dlg.setVisible(true);
                if(!dlg.isOk) {
                    return;
                }
                List<Feature> paths = new ArrayList<>();
                if(dlg.costMax != null) {
                    try {
                        HashMap2D<Path, Double, Integer> costPaths = linkset.extractCostFromPath();
                        Set<Double> costs = new TreeSet<>(costPaths.getKeys2()).tailSet(dlg.costMax);
                        for(Path p : costPaths.getKeys1()) {
                            boolean include = true;
                            for(Double cost : costs) {
                                Integer nb = costPaths.getValue(p, cost);
                                if(nb != null && nb > 0) {
                                    include = false;
                                    break;
                                }
                            }
                            if(include) {
                                paths.add(p);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(LinkLayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    paths.addAll(getFeatures());
                }
                
                double dist = dlg.distance;
                double[][] data = new double[paths.size()][2];
                int i = 0;
                double sumX = 0;
                for(Feature f : paths) {
                    if(dlg.log) {
                        data[i][0] = Math.log(((Number)f.getAttribute(Path.DIST_ATTR)).doubleValue());
                        data[i][1] = Math.log(((Number)f.getAttribute(Path.COST_ATTR)).doubleValue());
                    } else {
                        data[i][0] = ((Number)f.getAttribute(Path.DIST_ATTR)).doubleValue();
                        data[i][1] = ((Number)f.getAttribute(Path.COST_ATTR)).doubleValue();
                    }
                    sumX += data[i][0];
                    i++;        
                }
                
                double [] coef = Regression.getOLSRegression(data);
                double x = dlg.log ? Math.log(dlg.distance) : dlg.distance;
                double cost = coef[0] + coef[1]*x;
                if(dlg.log) {
                    cost = Math.exp(cost);
                } 
                SimpleRegression reg = new SimpleRegression(true);
                reg.addData(data);
                double r2 = reg.getRSquare();
                double inter = -new TDistribution(reg.getN()-2).inverseCumulativeProbability(0.05/2) * 
                        Math.sqrt(reg.getMeanSquareError() * (1 + 1/reg.getN() + Math.pow(x-(sumX/reg.getN()), 2) / reg.getXSumSquares()))  ;
                double min = reg.predict(x)-inter, max = reg.predict(x)+inter;
                if(dlg.log) {
                    min = Math.exp(min);
                    max = Math.exp(max);
                } 
                final JFrame frm = FeatureLayer.showScatterPlot(paths, Path.DIST_ATTR, Path.COST_ATTR, dlg.log);
                String equation = dlg.log ? 
                        String.format("Regression : cost = exp(%g + log(dist)*%g)\n\n", coef[0], coef[1]) : 
                        String.format("Regression : cost = %g + dist*%g\n\n", coef[0], coef[1]);
                String result = String.format("dist %g = cost %g +/- [%g - %g]", dist, cost, min, max);
                ((ChartFrame)frm).getChartPanel().getChart().setTitle(equation+result);
                
                final JTextArea text = new JTextArea(result);
                text.addComponentListener(new ComponentAdapter() {
                    // event for moving plot window below message dialog
                    @Override
                    public void componentResized(ComponentEvent e) {
                        super.componentResized(e); //To change body of generated methods, choose Tools | Templates.
                        Point p = SwingUtilities.windowForComponent(text).getLocation();
                        frm.setLocation(p.x, p.y+130);
                    }
                    
                });

                JOptionPane.showMessageDialog(null, text, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Dist2Cost"), JOptionPane.PLAIN_MESSAGE);
            }
        });
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Extract_path_costs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HashMap2D map = linkset.extractCostFromPath();
                    map.saveToCSV(new File(linkset.getProject().getDirectory(), getName() + "-links-extract-cost.csv"));
                    JOptionPane.showMessageDialog(null, "Costs extracted into file " + getName() + "-links-extract-cost.csv");
                } catch (IOException ex) {
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("RemoveAttrMenuItem.text")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                List attrList = new ArrayList(linkset.getPaths().get(0).getAttributeNames());
                attrList = attrList.subList(4, attrList.size());
                JList list = new JList(attrList.toArray());
                int res = JOptionPane.showConfirmDialog(null, new JScrollPane(list), 
                        java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("RemoveAttrMenuItem.text"), 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if(res == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                for(Object attr : list.getSelectedValuesList()) {
                    DefaultFeature.removeAttribute((String)attr, linkset.getPaths());
                }
                try {
                    linkset.saveLinks();
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, linkset.getInfo());
            }
        });
        return menu;
    }
    
}
