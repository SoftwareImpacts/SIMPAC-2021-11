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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import org.geotools.feature.SchemaException;
import org.thema.common.JTS;
import org.thema.data.IOFeature;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.graphab.Project;
import org.thema.graphab.pointset.Pointset;
import org.thema.msca.Cell;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.AbstractAgregateOperation;
import org.thema.msca.operation.AbstractLayerOperation;

/**
 * Dialog form for generating stratified random points of pseudo-absence from a point set of species presence.
 * 
 * @author Gilles Vuidel
 */
public class RandomPointDlg extends javax.swing.JDialog {

    private Project project;
    private RasterLayer gridLayer;
    private SquareGrid grid;
    private DefaultFeatureCoverage<DefaultFeature> coverage;

    /** 
     * Creates new form RandomPointDlg
     * @param parent parent frame
     * @param prj the current project
     */
    public RandomPointDlg(java.awt.Frame parent, Project prj) {
        super(parent, false);
        initComponents();
        setLocationRelativeTo(parent);
        project = prj;

        distComboBox.setModel(new DefaultComboBoxModel(project.getLinksets().toArray()));

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        resoSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        mindDistSpinner = new javax.swing.JSpinner();
        genButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        closeButton = new javax.swing.JButton();
        keepOneCheckBox = new javax.swing.JCheckBox();
        shapeSelectFilePanel = new org.thema.common.swing.SelectFilePanel();
        distComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("RandomPointDlg.title")); // NOI18N
        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/thema/graphab/model/Bundle"); // NOI18N
        jLabel2.setText(bundle1.getString("RandomPointDlg.jLabel2.text")); // NOI18N

        resoSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        jLabel3.setText(bundle1.getString("RandomPointDlg.jLabel3.text")); // NOI18N

        mindDistSpinner.setModel(new javax.swing.SpinnerNumberModel(1000.0d, 0.0d, null, 1.0d));

        genButton.setText(bundle1.getString("RandomPointDlg.genButton.text")); // NOI18N
        genButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genButtonActionPerformed(evt);
            }
        });

        updateButton.setText(bundle1.getString("RandomPointDlg.updateButton.text")); // NOI18N
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        nameTextField.setText(bundle1.getString("RandomPointDlg.nameTextField.text")); // NOI18N

        closeButton.setText(bundle1.getString("RandomPointDlg.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        keepOneCheckBox.setText(bundle1.getString("RandomPointDlg.keepOneCheckBox.text")); // NOI18N
        keepOneCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepOneCheckBoxActionPerformed(evt);
            }
        });

        shapeSelectFilePanel.setDescription(bundle1.getString("RandomPointDlg.shapeSelectFilePanel.description")); // NOI18N
        shapeSelectFilePanel.setFileDesc(bundle1.getString("RandomPointDlg.shapeSelectFilePanel.fileDesc")); // NOI18N
        shapeSelectFilePanel.setFileExts(bundle1.getString("RandomPointDlg.shapeSelectFilePanel.fileExts")); // NOI18N
        shapeSelectFilePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shapeSelectFilePanelActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle1.getString("RandomPointDlg.jLabel1.text")); // NOI18N

        jLabel4.setText(bundle1.getString("RandomPointDlg.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shapeSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(resoSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keepOneCheckBox)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(distComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(mindDistSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shapeSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(resoSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(mindDistSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(keepOneCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(distComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(genButton)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeButton)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        updateGrid();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void genButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genButtonActionPerformed
        final double distMin = (Double)mindDistSpinner.getValue();
        final double resolution = (Integer)resoSpinner.getValue();

        if(distMin >= resolution/2) {
            JOptionPane.showMessageDialog(this, "Distance min. must be lower than resolution/2");
            return;
        }

        if(project.getPointsetNames().contains(nameTextField.getText())) {
            JOptionPane.showMessageDialog(this, "Dataset name already exist.");
            return;
        }

        final List<DefaultFeature> points = new ArrayList<>();
        final List<String> attrNames = new ArrayList<>(
                Arrays.asList("presence"));

        
        grid.execute(new AbstractLayerOperation() {
            int i = 1;
            @Override
            public void perform(Cell cell) {
                if(cell.getLayerValue("presence") == 0) {
                    return;
                }

                if(cell.getLayerValue("presence") == 1) {
                    boolean good = false;
                    double x = 0, y = 0;
                    while(!good) {
                        x = Math.random() * resolution + cell.getCentroid().getX() - resolution/2;
                        y = Math.random() * resolution + cell.getCentroid().getY() - resolution/2;
                        good = true;
                        int j = 0;
                        while(good && j < points.size()) {
                            if(points.get(j).getGeometry().getCoordinate()
                                    .distance(new Coordinate(x, y)) < distMin) {
                                good = false;
                            }
                            try {
                                if (!project.isInZone(x, y)) {
                                    good = false;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(RandomPointDlg.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            j++;
                        }
                    }
                    points.add(new DefaultFeature("rand" + i++, new GeometryFactory()
                            .createPoint(new Coordinate(x, y)), attrNames,
                            Arrays.asList(new Object[]{0})));

                } else {
                    List<DefaultFeature> lst = coverage.getFeaturesIn(cell.getGeometry());

                    if(keepOneCheckBox.isSelected()) {
                        Feature f = lst.get((int)(Math.random()*lst.size()));
                        points.add(new DefaultFeature(f.getId().toString(), f.getGeometry(),
                            attrNames, Arrays.asList(new Object[]{1})));
                    } else {
                        for (Feature f : lst) {
                            points.add(new DefaultFeature(f.getId().toString(), f.getGeometry(),
                                attrNames, Arrays.asList(new Object[]{1})));
                        }
                    }
                }
            }
        });

        Pointset exo = new Pointset(nameTextField.getText(),
                project.getLinkset(distComboBox.getSelectedItem().toString()),
                0, Pointset.AG_NONE);
        try {
            project.addPointset(exo, attrNames, points, true);
        } catch (SchemaException | IOException ex) {
            Logger.getLogger(RandomPointDlg.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error while adding points :\n" + ex.getLocalizedMessage());
        }

    }//GEN-LAST:event_genButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        project.removeLayer(gridLayer);
    }//GEN-LAST:event_formWindowClosed

    private void shapeSelectFilePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shapeSelectFilePanelActionPerformed
        try {
            List<DefaultFeature> features = IOFeature.loadFeatures(shapeSelectFilePanel.getSelectedFile());
        
            coverage = new DefaultFeatureCoverage(features);
            int nb = features.size();

            double area = project.getZone().getWidth()*project.getZone().getHeight();
            double res = Math.sqrt(area / (2*nb));
            resoSpinner.setValue((int)res);
            mindDistSpinner.setValue(res/2);
            updateGrid();
        } catch (Exception ex) {
            Logger.getLogger(RandomPointDlg.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error while loading points :\n" + ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_shapeSelectFilePanelActionPerformed

    private void keepOneCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepOneCheckBoxActionPerformed
        updateGrid();
    }//GEN-LAST:event_keepOneCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox distComboBox;
    private javax.swing.JButton genButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JCheckBox keepOneCheckBox;
    private javax.swing.JSpinner mindDistSpinner;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JSpinner resoSpinner;
    private org.thema.common.swing.SelectFilePanel shapeSelectFilePanel;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    private void updateGrid() {

        if(coverage == null) {
            return;
        }
        double res = (Integer)resoSpinner.getValue();
        Rectangle2D rect = project.getZone();
        double dx = rect.getWidth() - Math.ceil((rect.getWidth() - 2*res) / res) * res;
        double dy = rect.getHeight() - Math.ceil((rect.getHeight() - 2*res) / res) * res;
        rect = new Rectangle2D.Double(rect.getX()+dx/2, rect.getY()+dy/2,
                rect.getWidth()-dx, rect.getHeight()-dy);

        grid = new SquareGrid(JTS.geomFromRect(rect).getEnvelopeInternal(), res);
        grid.addLayer("presence", DataBuffer.TYPE_BYTE, 0);
        grid.execute(new AbstractLayerOperation() {
            @Override
            public void perform(Cell cell) {
                try {
                    Point2D p = cell.getCentroid();
                    if (project.isInZone(p.getX(), p.getY())) {
                        cell.setLayerValue("presence",
                                coverage.getFeaturesIn(cell.getGeometry()).isEmpty() ? 1 : 2);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(RandomPointDlg.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }
        });

        if(gridLayer != null) {
            project.removeLayer(gridLayer);
        }

        gridLayer = new RasterLayer("Grid", grid.getLayer("presence").getRaster(), rect);
        RasterStyle style = new RasterStyle(new Color[] {Color.BLACK, Color.LIGHT_GRAY},
                0, Color.WHITE);
        style.setDrawGrid(true);
        style.setDrawValue(false);
        gridLayer.setStyle(style);

        project.addLayer(gridLayer);

        int nbCell = grid.agregate(new AbstractAgregateOperation<Integer>(0, 0) {
            @Override
            public void perform(Cell cell) {
                if(cell.getLayerValue("presence") == 1) {
                    result++;
                }
            }
        });
        if(keepOneCheckBox.isSelected()) {
            int nbCellPres = grid.agregate(new AbstractAgregateOperation<Integer>(0, 0) {
                @Override
                public void perform(Cell cell) {
                    if(cell.getLayerValue("presence") == 2) {
                        result++;
                    }
                }
            });
            infoLabel.setText(String.format("Presences : %d - Absences : %d", nbCellPres, nbCell));
        } else {
            infoLabel.setText(String.format("Presences : %d - Absences : %d", coverage.getFeatures().size(), nbCell));
        }
    }

}
