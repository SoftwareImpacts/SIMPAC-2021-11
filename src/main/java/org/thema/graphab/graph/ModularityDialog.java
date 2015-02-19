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
package org.thema.graphab.graph;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.SpinnerListModel;
import org.geotools.graph.structure.Node;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.Layer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.table.ColorRamp;
import org.thema.drawshape.style.table.FeatureAttributeIterator;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public class ModularityDialog extends javax.swing.JDialog {

    private final Modularity mod;
    /**
     * Creates new form ModularityDialog
     */
    public ModularityDialog(java.awt.Frame parent, Modularity mod) {
        super(parent, false);
        this.mod = mod;
        initComponents();
        setLocationRelativeTo(parent);
        
        nbSpinner.setValue(mod.getBestPartition().size());
        
        showButtonActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        closeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        nbSpinner = new javax.swing.JSpinner();
        showButton = new javax.swing.JButton();
        modLabel = new javax.swing.JLabel();
        optimCheckBox = new javax.swing.JCheckBox();
        optimLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Modularity");
        setAlwaysOnTop(true);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Nb clusters");

        nbSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        nbSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                nbSpinnerStateChanged(evt);
            }
        });

        showButton.setText("Show");
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });

        modLabel.setText("Modularity");

        optimCheckBox.setSelected(true);
        optimCheckBox.setText("Optimize");

        optimLabel.setText("Modularity");

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, optimCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), optimLabel, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(optimCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(modLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(optimLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nbSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(showButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nbSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showButton))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optimCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optimLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = Config.getProgressBar("Merge voronoi...");
                progressBar.setIndeterminate(true);
                Set<Modularity.Cluster> partition = optimCheckBox.isSelected() ? mod.getOptimPartition((int) nbSpinner.getValue())
                        : mod.getPartition((int) nbSpinner.getValue());
                if(optimCheckBox.isSelected()) {
                    optimLabel.setText(String.format("Modularity : %g", mod.getModularity(partition)));
                } else {
                    optimLabel.setText("");
                }
                List<Feature> clusters = new ArrayList<>();
                List<String> attrNames = Arrays.asList("modularity");
                for(Modularity.Cluster c : partition) {
                    List<Geometry> geoms = new ArrayList<>();
                    for(Node n : c.getNodes())  {
                        geoms.add(Project.getProject().getVoronoi((Integer)Project.getPatch(n).getId()).getGeometry());
                    }
                    clusters.add(new DefaultFeature(c.getId(), CascadedPolygonUnion.union(geoms), attrNames, Arrays.asList(c.getPartModularity())));
                }
                ColorRamp ramp = new ColorRamp(ColorRamp.reverse(ColorRamp.RAMP_SYM_GREEN_RED), -1, 0, 1);
                ramp.setBoundsCentered(new FeatureAttributeIterator(clusters, attrNames.get(0)));
                Layer l = new FeatureLayer("Clustering - " + clusters.size(), clusters, new FeatureStyle(attrNames.get(0), 
                        ramp, null, new ColorRamp(new Color[]{Color.black})));
                l.setVisible(true);
                l.setRemovable(true);

                mod.getGraphGenerator().getLayers().addLayer(l);
                progressBar.close();
            }
        }).start();
    }//GEN-LAST:event_showButtonActionPerformed

    private void nbSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_nbSpinnerStateChanged
        modLabel.setText(String.format("Modularity : %g", mod.getModularities().get((Integer)nbSpinner.getValue())));
        optimLabel.setText("");
    }//GEN-LAST:event_nbSpinnerStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel modLabel;
    private javax.swing.JSpinner nbSpinner;
    private javax.swing.JCheckBox optimCheckBox;
    private javax.swing.JLabel optimLabel;
    private javax.swing.JButton showButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
