/*
 * Copyright (C) 2015 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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

package org.thema.graphab.addpatch;

import org.thema.graphab.graph.GraphGenerator;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.SchemaException;
import org.opengis.feature.type.AttributeType;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.graphab.Project;
import org.thema.graphab.metric.Metric;
import org.thema.graphab.metric.ParamPanel;
import org.thema.graphab.metric.global.GlobalMetric;

/**
 * Dialog form for launching add patch command.
 * 
 * @author Gilles Vuidel
 */
public class AddPatchDialog extends javax.swing.JDialog {

    private GlobalMetric metric;
    
    private JFrame mainFrame;
    private Project project;
    /**
     * Creates new form AddPatchDialog
     * @param parent the parent frame
     * @param prj the current project
     */
    public AddPatchDialog(JFrame parent, Project prj) {
        super(parent, true);
        initComponents();
        project = prj;

        setLocationRelativeTo(parent);
        mainFrame = parent;
        graphComboBox.setModel(new DefaultComboBoxModel(prj.getGraphs().toArray()));
        indiceComboBox.setModel(new DefaultComboBoxModel(Project.getGlobalMetricsFor(Project.Method.GLOBAL).toArray()));
        indiceComboBoxActionPerformed(null);
        
        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        graphComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        nbPatchSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        indiceComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        nbMultiPatchSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        windowSpinner = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        capaSelectFilePanel = new org.thema.common.swing.SelectFilePanel();
        gridRadioButton = new javax.swing.JRadioButton();
        shapeRadioButton = new javax.swing.JRadioButton();
        shapeSelectFilePanel = new org.thema.common.swing.SelectFilePanel();
        jLabel8 = new javax.swing.JLabel();
        capaFieldComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        resoSpinner = new javax.swing.JSpinner();
        paramButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("AddPatchDialog.title")); // NOI18N

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/thema/graphab/addpatch/Bundle"); // NOI18N
        okButton.setText(bundle1.getString("AddPatchDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle1.getString("AddPatchDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle1.getString("AddPatchDialog.jLabel1.text")); // NOI18N

        graphComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText(bundle1.getString("AddPatchDialog.jLabel3.text")); // NOI18N

        nbPatchSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        jLabel4.setText(bundle1.getString("AddPatchDialog.jLabel4.text")); // NOI18N

        indiceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiceComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle1.getString("AddPatchDialog.jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12), java.awt.Color.black)); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jPanel1, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        nbMultiPatchSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), nbMultiPatchSpinner, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel2.setText(bundle1.getString("AddPatchDialog.jLabel2.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel2, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel7.setText(bundle1.getString("AddPatchDialog.jLabel7.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel7, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel6.setText(bundle1.getString("AddPatchDialog.jLabel6.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel6, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        windowSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), windowSpinner, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(nbMultiPatchSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(windowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nbMultiPatchSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(windowSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("AddPatchDialog.jPanel2.border.title"))); // NOI18N

        capaSelectFilePanel.setDescription(bundle1.getString("AddPatchDialog.capaSelectFilePanel.description")); // NOI18N
        capaSelectFilePanel.setFileDesc(bundle1.getString("AddPatchDialog.capaSelectFilePanel.fileDesc")); // NOI18N
        capaSelectFilePanel.setFileExts(bundle1.getString("AddPatchDialog.capaSelectFilePanel.fileExts")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), capaSelectFilePanel, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup1.add(gridRadioButton);
        gridRadioButton.setSelected(true);
        gridRadioButton.setText(bundle1.getString("AddPatchDialog.gridRadioButton.text")); // NOI18N

        buttonGroup1.add(shapeRadioButton);
        shapeRadioButton.setText(bundle1.getString("AddPatchDialog.shapeRadioButton.text")); // NOI18N

        shapeSelectFilePanel.setDescription(bundle1.getString("AddPatchDialog.shapeSelectFilePanel.description")); // NOI18N
        shapeSelectFilePanel.setFileDesc(bundle1.getString("AddPatchDialog.shapeSelectFilePanel.fileDesc")); // NOI18N
        shapeSelectFilePanel.setFileExts(bundle1.getString("AddPatchDialog.shapeSelectFilePanel.fileExts")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, shapeRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), shapeSelectFilePanel, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        shapeSelectFilePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shapeSelectFilePanelActionPerformed(evt);
            }
        });

        jLabel8.setText(bundle1.getString("AddPatchDialog.jLabel8.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, shapeRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel8, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, shapeRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), capaFieldComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel5.setText(bundle1.getString("AddPatchDialog.jLabel5.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel5, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        resoSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1000.0d), Double.valueOf(0.0d), null, Double.valueOf(100.0d)));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), resoSpinner, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(capaFieldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(shapeRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(shapeSelectFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(capaSelectFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(resoSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(gridRadioButton))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(gridRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resoSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(capaSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(shapeSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shapeRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(capaFieldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        paramButton.setText(bundle1.getString("AddPatchDialog.paramButton.text")); // NOI18N
        paramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paramButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1))
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(indiceComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(graphComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paramButton)
                        .addGap(39, 39, 39))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nbPatchSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(graphComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(indiceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paramButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cancelButton)
                            .addComponent(okButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(nbPatchSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        getRootPane().setDefaultButton(okButton);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        final int nbPatch = (Integer)nbPatchSpinner.getValue();
        final String graphName = ((GraphGenerator) graphComboBox.getSelectedItem()).getName();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ProgressBar bar = Config.getProgressBar();
                bar.setNote("Add patches...");
                try {                    
                    // load the project
                    Project addProject = Project.loadProject(project.getProjectFile(), false);
                    GraphGenerator gen = addProject.getGraph(graphName);
                    bar.setProgress(0);
                    AddPatchCommand addPatchCmd;
                    if(gridRadioButton.isSelected()) {
                        double res = (Double)resoSpinner.getValue();
                        File capaFile = capaSelectFilePanel.getSelectedFile();
                        int nbMultiPatch = (Integer)nbMultiPatchSpinner.getValue();
                        int windowMulti = (Integer)windowSpinner.getValue();
                        addPatchCmd = new AddPatchCommand(nbPatch, metric, gen, capaFile, res, nbMultiPatch, windowMulti);
                    } else {
                        File pointFile = shapeRadioButton.isSelected() ? shapeSelectFilePanel.getSelectedFile() : null;
                        String capaField = shapeRadioButton.isSelected() ? (String)capaFieldComboBox.getSelectedItem() : null;
                        addPatchCmd = new AddPatchCommand(nbPatch, metric, gen, pointFile, capaField);
                    }
                    
                    addPatchCmd.run(bar);
                    AddPatchResultDialog resDlg = new AddPatchResultDialog(mainFrame, addProject);
                    resDlg.showResults(metric, gen, addPatchCmd.getAddedPatches(), addPatchCmd.getMetricValues());
                } catch(CancellationException cancel) {
                    Logger.getLogger(AddPatchDialog.class.getName()).log(Level.INFO, null, cancel);
                } catch(IOException | SchemaException ex) {
                    Logger.getLogger(AddPatchDialog.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(AddPatchDialog.this, ex);
                } finally {
                    bar.close();
                    setVisible(false);
                    dispose();
                }
            }
        }).start();
        
    }//GEN-LAST:event_okButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void graphComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphComboBoxActionPerformed
        GraphGenerator graph = (GraphGenerator) graphComboBox.getSelectedItem();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(Metric ind : Project.getGlobalMetricsFor(Project.Method.GLOBAL)) {
            if(ind.isAcceptGraph(graph)) {
                model.addElement(ind);
            }
        }
        indiceComboBox.setModel(model);
    }//GEN-LAST:event_graphComboBoxActionPerformed

    private void shapeSelectFilePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shapeSelectFilePanelActionPerformed
        try {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            
            ShapefileDataStore dataStore = new ShapefileDataStore(shapeSelectFilePanel.getSelectedFile().toURI().toURL());
            List<AttributeType> attrs = dataStore.getSchema().getTypes();
            for(AttributeType attr : attrs) {
                if(Number.class.isAssignableFrom(attr.getBinding())) {
                    model.addElement(attr.getName().getLocalPart());
                }
            }
            
            capaFieldComboBox.setModel(model);
        } catch (Exception ex) {
            Logger.getLogger(AddPatchDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error while loading layer.\n Details : " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_shapeSelectFilePanelActionPerformed

    private void paramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramButtonActionPerformed
        ParamPanel panel = metric.getParamPanel(project);
        int ret = JOptionPane.showConfirmDialog(this, panel, ResourceBundle.getBundle("org/thema/graphab/addpatch/Bundle").getString("AddPatchDialog.paramButton.text"), JOptionPane.OK_CANCEL_OPTION);
        if(ret == JOptionPane.CANCEL_OPTION) {
            return;
        }
        metric.setParams(panel.getParams());
        
    }//GEN-LAST:event_paramButtonActionPerformed

    private void indiceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiceComboBoxActionPerformed
        metric = (GlobalMetric) indiceComboBox.getSelectedItem();
        paramButton.setEnabled(!metric.getParams().isEmpty());
    }//GEN-LAST:event_indiceComboBoxActionPerformed
    
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox capaFieldComboBox;
    private org.thema.common.swing.SelectFilePanel capaSelectFilePanel;
    private javax.swing.JComboBox graphComboBox;
    private javax.swing.JRadioButton gridRadioButton;
    private javax.swing.JComboBox indiceComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner nbMultiPatchSpinner;
    private javax.swing.JSpinner nbPatchSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JButton paramButton;
    private javax.swing.JSpinner resoSpinner;
    private javax.swing.JRadioButton shapeRadioButton;
    private org.thema.common.swing.SelectFilePanel shapeSelectFilePanel;
    private javax.swing.JSpinner windowSpinner;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
