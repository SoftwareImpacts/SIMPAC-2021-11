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

import au.com.bytecode.opencsv.CSVWriter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.geotools.feature.SchemaException;
import org.thema.common.Util;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * A layer representing a pointset.
 * 
 * @author Gilles Vuidel
 */
public class PointsetLayer extends FeatureLayer {
    private final Pointset pointset;
    private final Project project;

    /**
     * Creates a new PointsetLayer
     * @param pointset the pointset
     * @param project the project containing this pointset
     */
    public PointsetLayer(Pointset pointset, Project project) {
        super(pointset.getName(), pointset.getFeatures(), null, project.getCRS());
        this.project = project;
        this.pointset = pointset;
    }

    @Override
    public JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Export_all")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphGenerator gen = (GraphGenerator) JOptionPane.showInputDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Select_graph"), java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Export..."), JOptionPane.PLAIN_MESSAGE, null, project.getGraphs().toArray(), null);
                if (gen == null) {
                    return;
                }
                File f = Util.getFileSave(".csv");
                if (f == null) {
                    return;
                }
                try (final CSVWriter w = new CSVWriter(new FileWriter(f))) {
                    List<DefaultFeature> exoData = pointset.getFeatures();
                    List<String> attrNames = new ArrayList<>(exoData.get(0).getAttributeNames());
                    attrNames.addAll(project.getPatches().iterator().next().getAttributeNames());
                    attrNames.addAll(gen.getComponentFeatures().get(0).getAttributeNames());
                    w.writeNext(attrNames.toArray(new String[attrNames.size()]));
                    String[] attrs = new String[attrNames.size()];
                    for (Feature exo : exoData) {
                        int n = exo.getAttributeNames().size();
                        for (int i = 0; i < n; i++) {
                            attrs[i] = exo.getAttribute(i) != null ? exo.getAttribute(i).toString() : null;
                        }
                        Feature patch = project.getPatch((Integer) exo.getAttribute(Project.EXO_IDPATCH));
                        for (int i = 0; i < patch.getAttributeNames().size(); i++) {
                            attrs[i + n] = patch.getAttribute(i).toString();
                        }
                        n += patch.getAttributeNames().size();
                        Feature comp = gen.getComponentFeature(patch);
                        for (int i = 0; i < comp.getAttributeNames().size(); i++) {
                            attrs[i + n] = comp.getAttribute(i).toString();
                        }
                        w.writeNext(attrs);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("DISTANCE MATRIX")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PointsetDistanceDialog(null, project, pointset).setVisible(true);
            }
        });
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Remove...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Do_you_want_to_remove_the_dataset_") + getName() + " ?");
                if (res != JOptionPane.YES_OPTION) {
                    return;
                }
                try {
                    project.removePointset(pointset.getName());
                    project.getPointsetLayers().removeLayer(PointsetLayer.this);
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Properties...")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, pointset.getInfo());
            }
        });
        return menu;
    }
    
}
