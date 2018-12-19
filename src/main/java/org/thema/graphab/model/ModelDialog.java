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

import au.com.bytecode.opencsv.CSVWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.math.MathException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.thema.common.JTS;
import org.thema.common.Util;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.IOImage;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Path;
import org.thema.graphab.pointset.Pointset;
import org.thema.graphab.util.RSTGridReader;

/**
 * Dialog form for Species Distribution Model (SDM)
 * 
 * @author Gilles Vuidel
 */
public class ModelDialog extends javax.swing.JDialog {

    private Project project;

    private Pointset data;
    private String varName;
    private double alpha;
    private List<String> patchVars;
    private LinkedHashMap<String, GridCoverage2D> extVars;
    private boolean bestModel;
    private boolean multiAttach;
    private double dMax;
    private HashMap<Geometry, HashMap<DefaultFeature, Path>> costCache;

    private DistribModel model;

    /** 
     * Creates a new form ModelDialog
     * @param parent the parent frame
     * @param project the current project
     */
    public ModelDialog(java.awt.Frame parent, Project project) {
        super(parent, false);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(estimButton);
        
        this.project = project;
        exoDataComboBox.setModel(new DefaultComboBoxModel(project.getPointsets().toArray()));
        extVars = new LinkedHashMap<>();
        patchVars = new ArrayList<>();
        
        exoDataComboBoxActionPerformed(null);
        graphComboBoxActionPerformed(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        estimButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        paramPanel = new javax.swing.JPanel();
        exoDataComboBox = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        remVarButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        varList = new javax.swing.JList();
        varComboBox = new javax.swing.JComboBox();
        addPatchButton = new javax.swing.JButton();
        graphComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        pSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        alphaTextField = new javax.swing.JTextField();
        dSpinner = new javax.swing.JSpinner();
        multiAttachCheckBox = new javax.swing.JCheckBox();
        dMaxSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        addExtButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        resultPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        infoResTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        varTable = new javax.swing.JTable();
        extrapolateButton = new javax.swing.JButton();
        bestModelCheckBox = new javax.swing.JCheckBox();
        exportButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("ModelDialog.title")); // NOI18N

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/thema/graphab/model/Bundle"); // NOI18N
        estimButton.setText(bundle1.getString("ModelDialog.estimButton.text")); // NOI18N
        estimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                estimButtonActionPerformed(evt);
            }
        });

        closeButton.setText(bundle1.getString("ModelDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        exoDataComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exoDataComboBoxActionPerformed(evt);
            }
        });

        jLabel9.setText(bundle1.getString("ModelDialog.jLabel9.text")); // NOI18N

        remVarButton.setText(bundle1.getString("ModelDialog.remVarButton.text")); // NOI18N
        remVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remVarButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(varList);

        addPatchButton.setText(bundle1.getString("ModelDialog.addPatchButton.text")); // NOI18N
        addPatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPatchButtonActionPerformed(evt);
            }
        });

        graphComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle1.getString("ModelDialog.jLabel2.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("ModelDialog.jPanel1.border.title"))); // NOI18N

        pSpinner.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.001d, 1.0d, 0.01d));
        pSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pSpinnerStateChanged(evt);
            }
        });

        jLabel7.setText(bundle1.getString("ModelDialog.jLabel7.text")); // NOI18N

        jLabel6.setText(bundle1.getString("ModelDialog.jLabel6.text")); // NOI18N

        jLabel5.setText(bundle1.getString("ModelDialog.jLabel5.text")); // NOI18N

        jLabel8.setText(bundle1.getString("ModelDialog.jLabel8.text")); // NOI18N

        alphaTextField.setText(bundle1.getString("ModelDialog.alphaTextField.text")); // NOI18N

        dSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1000.0d), Double.valueOf(0.001d), null, Double.valueOf(1.0d)));
        dSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dSpinnerStateChanged(evt);
            }
        });

        multiAttachCheckBox.setText(bundle1.getString("ModelDialog.multiAttachCheckBox.text")); // NOI18N

        dMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1000.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, multiAttachCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), dMaxSpinner, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel3.setText(bundle1.getString("ModelDialog.jLabel3.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, multiAttachCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel3, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(multiAttachCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alphaTextField)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel8)
                        .addGap(49, 49, 49))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dSpinner)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pSpinner)
                        .addGap(43, 43, 43))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(alphaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(dSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(pSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(multiAttachCheckBox)
                    .addComponent(dMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        jLabel1.setText(bundle1.getString("ModelDialog.jLabel1.text")); // NOI18N

        addExtButton.setText(bundle1.getString("ModelDialog.addExtButton.text")); // NOI18N
        addExtButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addExtButtonActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle1.getString("ModelDialog.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout paramPanelLayout = new javax.swing.GroupLayout(paramPanel);
        paramPanel.setLayout(paramPanelLayout);
        paramPanelLayout.setHorizontalGroup(
            paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paramPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paramPanelLayout.createSequentialGroup()
                        .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(graphComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(exoDataComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(varComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paramPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addPatchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addExtButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(remVarButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        paramPanelLayout.setVerticalGroup(
            paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paramPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(exoDataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(varComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(graphComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(paramPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .addGroup(paramPanelLayout.createSequentialGroup()
                        .addComponent(addPatchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addExtButton)
                        .addGap(8, 8, 8)
                        .addComponent(remVarButton)))
                .addContainerGap())
        );

        infoResTextArea.setColumns(20);
        infoResTextArea.setEditable(false);
        infoResTextArea.setRows(5);
        jScrollPane3.setViewportView(infoResTextArea);

        varTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Variable", "Coef.", "Std coef."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(varTable);

        javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
        resultPanel.setLayout(resultPanelLayout);
        resultPanelLayout.setHorizontalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        resultPanelLayout.setVerticalGroup(
            resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );

        extrapolateButton.setText(bundle1.getString("ModelDialog.extrapolateButton.text")); // NOI18N
        extrapolateButton.setEnabled(false);
        extrapolateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extrapolateButtonActionPerformed(evt);
            }
        });

        bestModelCheckBox.setSelected(true);
        bestModelCheckBox.setText(bundle1.getString("ModelDialog.bestModelCheckBox.text")); // NOI18N

        exportButton.setText(bundle1.getString("ModelDialog.exportButton.text")); // NOI18N
        exportButton.setEnabled(false);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bestModelCheckBox)
                        .addGap(78, 78, 78)
                        .addComponent(estimButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))
                    .addComponent(paramPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(extrapolateButton)
                        .addGap(58, 58, 58)
                        .addComponent(exportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paramPanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bestModelCheckBox)
                    .addComponent(estimButton)
                    .addComponent(extrapolateButton)
                    .addComponent(closeButton)
                    .addComponent(exportButton))
                .addContainerGap())
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exoDataComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exoDataComboBoxActionPerformed
        Pointset exo = (Pointset)exoDataComboBox.getSelectedItem();
        Feature f = (exo).getFeatures().get(0);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(int i = 0; i < f.getAttributeNames().size(); i++) {
            if (Number.class.isAssignableFrom(f.getAttributeType(i))) {
                model.addElement(f.getAttributeNames().get(i));
            }
        }
        varComboBox.setModel(model);

        model = new DefaultComboBoxModel();
        for(GraphGenerator g : project.getGraphs()) {
            if (g.getLinkset() == exo.getLinkset()) {
                model.addElement(g);
            }
        }
        graphComboBox.setModel(model);
}//GEN-LAST:event_exoDataComboBoxActionPerformed

    private void pSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pSpinnerStateChanged
        alphaTextField.setText(String.valueOf(-Math.log((Double)pSpinner.getValue()) / (Double)dSpinner.getValue()));
}//GEN-LAST:event_pSpinnerStateChanged

    private void dSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dSpinnerStateChanged
        alphaTextField.setText(String.valueOf(-Math.log((Double)pSpinner.getValue()) / (Double)dSpinner.getValue()));
}//GEN-LAST:event_dSpinnerStateChanged

    private void graphComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphComboBoxActionPerformed
        String graph = graphComboBox.getSelectedItem().toString();
        patchVars.clear();
        patchVars.add(0, Project.CAPA_ATTR);
        patchVars.addAll(project.getGraphPatchAttr(graph));
        List<String> vars = new ArrayList<>(patchVars);
        vars.addAll(extVars.keySet());
        varList.setModel(new DefaultComboBoxModel(vars.toArray()));
        GraphGenerator g = (GraphGenerator)graphComboBox.getSelectedItem();
        if(g.getType() == GraphGenerator.THRESHOLD) {
            dSpinner.setValue(g.getThreshold());
        }
}//GEN-LAST:event_graphComboBoxActionPerformed

    private void estimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_estimButtonActionPerformed
        if(exoDataComboBox.getSelectedItem() != data 
                || ((Number)dMaxSpinner.getValue()).doubleValue() != dMax) {
            costCache = null;
        }

        data = (Pointset) exoDataComboBox.getSelectedItem();
        varName = varComboBox.getSelectedItem().toString();
        alpha = Double.parseDouble(alphaTextField.getText());
        bestModel = bestModelCheckBox.isSelected();
        multiAttach = multiAttachCheckBox.isSelected();
        dMax = (Double)dMaxSpinner.getValue();

        model = new DistribModel(project, data, varName, alpha, patchVars,
                extVars, bestModel, multiAttach, dMax, costCache
                );

        final int nVar = patchVars.size();
        if(nVar > 30) {
            JOptionPane.showMessageDialog(this, "Model is limited to 30 variables.");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TaskMonitor monitor = new TaskMonitor(ModelDialog.this, "Model...", "Loading data...", 0, 100);
                    monitor.setMillisToDecideToPopup(0);
                    String msg = model.estimModel(monitor);
                    if(multiAttach) {
                        costCache = model.getCostCache();
                    }
                    infoResTextArea.setText(msg);
                    DefaultTableModel table = (DefaultTableModel)varTable.getModel();
                    table.setRowCount(0);
                    for(String var : model.getUsedVars()) {
                        table.addRow(new Object[]{var, String.format("%g", model.getCoef(var))
                                , String.format("%g", model.getStdCoef(var))});
                    }
                    monitor.close();
                    extrapolateButton.setEnabled(true);
                    exportButton.setEnabled(true);
                } catch (IOException | MathException ex) {
                    Logger.getLogger(ModelDialog.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(ModelDialog.this, "An error occured : \n" + ex.getLocalizedMessage());
                }
            }
        }).start();
}//GEN-LAST:event_estimButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
        dispose();
}//GEN-LAST:event_closeButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        File file = Util.getFileSave(".csv");
        if(file == null) {
            return;
        }
        try (CSVWriter w = new CSVWriter(new FileWriter(file))) {
            List<String> header = new ArrayList<>(Arrays.asList("Id", "X", "Y", varName, "Estim", "Residu"));
            header.addAll(model.getVarNames());
            w.writeNext(header.toArray(new String[header.size()]));
            double [][] varExp = model.getVarExp();
            double[] estim = model.getVarEstim();
            int i = 0;
            for (Feature f : data.getFeatures()) {
                Coordinate c = f.getGeometry().getCentroid().getCoordinate();
                double val = ((Number)f.getAttribute(varName)).doubleValue();
                List<String> lst = new ArrayList<>(Arrays.asList(f.getId().toString(),
                        String.valueOf(c.x), String.valueOf(c.y),
                        String.valueOf(val), String.valueOf(estim[i]), String.valueOf(val-estim[i])));
                for (int j = 0; j < varExp[i].length; j++) {
                    lst.add(String.valueOf(varExp[i][j]));
                }
                w.writeNext(lst.toArray(new String[lst.size()]));
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(ModelDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "An error occured :\n" + ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_exportButtonActionPerformed

    private void addPatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPatchButtonActionPerformed
        List<String> vars = new ArrayList<>();
        vars.addAll(project.getPatches().iterator().next().getAttributeNames());
        vars.removeAll(patchVars);
        if(vars.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No more variable.");
            return;
        }
        String var = (String)JOptionPane.showInputDialog(this, "Add variable : ", "Variable",
                JOptionPane.PLAIN_MESSAGE, null, vars.toArray(), vars.get(0));
        if(var == null) {
            return;
        }
        patchVars.add(var);
        ((DefaultComboBoxModel)varList.getModel()).addElement(var);
}//GEN-LAST:event_addPatchButtonActionPerformed

    private void remVarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remVarButtonActionPerformed

        while(varList.getSelectedIndex() != -1) {
            int ind = varList.getSelectedIndex();
            String name = (String) varList.getModel().getElementAt(ind);
            ((DefaultComboBoxModel)varList.getModel()).removeElementAt(ind);
            if(name.startsWith("ext-")) {
                extVars.remove(name);
            } else {
                patchVars.remove(name);
            }
        }
        
}//GEN-LAST:event_remVarButtonActionPerformed

    private void addExtButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addExtButtonActionPerformed
        File f = Util.getFile(".tif|.asc|.rst", "Raster image");
        if(f == null) {
            return;
        }
        try {
            GridCoverage2D coverage;
            if (f.getName().toLowerCase().endsWith(".rst")) {
                coverage = new RSTGridReader(f).read(null);
            } else {
                coverage = IOImage.loadCoverage(f);
            }
            if(!coverage.getEnvelope2D().intersects(project.getZone())) {
                JOptionPane.showMessageDialog(this, "Raster does not intersect study area.");
                return;
            }
            data = (Pointset) exoDataComboBox.getSelectedItem();
            if(!coverage.getEnvelope2D().contains(JTS.envToRect(new DefaultFeatureCoverage(data.getFeatures()).getEnvelope()))) {
                JOptionPane.showMessageDialog(this, "Raster does not contain all data points.");
                return;
            }

            ((DefaultComboBoxModel)varList.getModel()).addElement("ext-" + coverage.getName().toString());
            extVars.put("ext-" + coverage.getName().toString(), coverage);

        } catch (IOException ex) {
            Logger.getLogger(ModelDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "An error occured :\n" + ex.getLocalizedMessage());
        }
}//GEN-LAST:event_addExtButtonActionPerformed

    private void extrapolateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extrapolateButtonActionPerformed
      
        double [] coefs = new double[model.getUsedVars().size()+1];
        List<String> vars = new ArrayList<>();
        coefs[0] = model.getConstant();
        int i = 1;
        for(String var : model.getUsedVars()) {
            vars.add(var);
            coefs[i] = model.getCoef(var);
            i++;
        }
        new ExtrapolateDialog((Frame) getParent(), project, data.getLinkset(), (Double)dSpinner.getValue(), (Double)pSpinner.getValue(), 
                vars, coefs, extVars, multiAttach, dMax).setVisible(true);
    }//GEN-LAST:event_extrapolateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addExtButton;
    private javax.swing.JButton addPatchButton;
    private javax.swing.JTextField alphaTextField;
    private javax.swing.JCheckBox bestModelCheckBox;
    private javax.swing.JButton closeButton;
    private javax.swing.JSpinner dMaxSpinner;
    private javax.swing.JSpinner dSpinner;
    private javax.swing.JButton estimButton;
    private javax.swing.JComboBox exoDataComboBox;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton extrapolateButton;
    private javax.swing.JComboBox graphComboBox;
    private javax.swing.JTextArea infoResTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JCheckBox multiAttachCheckBox;
    private javax.swing.JSpinner pSpinner;
    private javax.swing.JPanel paramPanel;
    private javax.swing.JButton remVarButton;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JComboBox varComboBox;
    private javax.swing.JList varList;
    private javax.swing.JTable varTable;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
