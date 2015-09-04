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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.common.collection.HashMap2D;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureGetter;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.style.LineStyle;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.pointset.Pointset;

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
                List<String> exoNames = new ArrayList<>();
                for (Pointset exo : linkset.getProject().getPointsets()) {
                    if (exo.getLinkset().getName().equals(getName())) {
                        exoNames.add(exo.getName());
                    }
                }
                if (!exoNames.isEmpty()) {
                    JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Links_is_used_by_exogenous_data") + Arrays.deepToString(exoNames.toArray()));
                    return;
                }
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
                    for (String gName : graphNames) {
                        linkset.getProject().removeGraph(gName);
                    }
                    linkset.getProject().getLinksetNames().remove(getName());
                    linkset.getProject().save();
                    linkset.getProject().getLinksetLayers().removeLayer(LinkLayer.this);
                } catch (IOException ex) {
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(new AbstractAction("Dist to cost") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String res = JOptionPane.showInputDialog("Distance : ");
                if(res == null) {
                    return;
                }
                double dist = Double.parseDouble(res);
                XYSeries s =  new XYSeries("regr");
                for(Feature f : getFeatures()) {
                    s.add(Math.log(((Number)f.getAttribute(Path.DIST_ATTR)).doubleValue()), Math.log(((Number)f.getAttribute(Path.COST_ATTR)).doubleValue()));
                }
                XYSeriesCollection dataregr = new XYSeriesCollection(s);

                double [] coef = Regression.getOLSRegression(dataregr, 0);
                double cost = linkset.estimCost(dist);
                
                JTextArea text = new JTextArea(String.format("Regression : cost = exp(%g + log(dist)*%g)\n\ndist %g = cost %g", 
                        coef[0], coef[1], dist, cost));
                JButton but = new JButton(new AbstractAction("Plot") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showScatterPlot(Path.DIST_ATTR, Path.COST_ATTR, true);
                    }
                });
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(text, BorderLayout.CENTER);
                panel.add(but, BorderLayout.EAST);
                JOptionPane.showMessageDialog(null, panel);
            }
        });
        menu.add(new AbstractAction("Extract path costs") {
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
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, linkset.getInfo());
            }
        });
        return menu;
    }
    
}
