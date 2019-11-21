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

import java.awt.Component;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.thema.graphab.Project;

/**
 * Panel for creating a new Linkset.
 * 
 * @author Gilles Vuidel
 */
public class LinksetPanel extends javax.swing.JPanel {

    private class Editor extends DefaultCellEditor  {

        private Editor() {
            super(new JTextField());
            setClickCountToStart(1);
        }

        @Override
        public Object getCellEditorValue() {
            try {
                return NumberFormat.getInstance().parse(((JTextField)getComponent()).getText().trim()).doubleValue();
            } catch (ParseException ex) {
                Logger.getLogger(LinksetPanel.class.getName()).log(Level.WARNING, null, ex);
                return null;
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JTextField comp = (JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
            String defaultConv = String.valueOf(value);
            int prec = defaultConv.length() - defaultConv.indexOf('.') - 1;
            String s = String.format(Locale.getDefault(), "%."+prec+"f", value);
            comp.setText(s);
            // la sélection ne fonctionne pas...
            comp.setSelectionStart(0);
            comp.setSelectionEnd(s.length());
            return comp;
        }
        
    }

    private int maxCode;
    private Project project;

    /** Creates new form LinksetPanel */
    public LinksetPanel() {
        initComponents();

        table.setDefaultEditor(Double.class, new Editor());
    }

    /**
     * Set the current project for creating the Linkset.
     * 
     * @param project 
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Set the codes and default cost.
     * @param codes the codes of the landscape map
     * @param cost the default cost, may be null
     */
    public void setCodes(Set<Integer> codes, double [] cost) {
        if(cost != null && cost.length <= Collections.max(codes)) {
            throw new IllegalArgumentException("Cost table does not cover all codes");
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        
        TreeSet<Integer> treeCodes = new TreeSet<>(codes);
        for(Integer code : treeCodes) {
            model.addRow(new Object[] {code, cost != null ? cost[code] : 1.0});
        }

        maxCode = treeCodes.last();
    }

    /**
     * {@link #setProject(org.thema.graphab.Project)} must be called before.
     * @return the new linkset
     */
    public Linkset getLinkset() {
        
        String name = nameTextField.getText();
        int type;
        double distMax;
        double coefSlope = 0;
        if(useDEMCheckBox.isSelected()) {
            coefSlope = (Double)coefSlopeSpinner.getValue();
        }
        
        if(completeRadioButton.isSelected()) {
            type = Linkset.COMPLETE;
        } else {
            type = Linkset.PLANAR;
        }

        distMax = (Double)dMaxSpinner.getValue();
        
        int type_length = costDistRadioButton.isSelected() ?
            Linkset.COST_LENGTH : Linkset.DIST_LENGTH;
        
        Linkset cost;
        if(euclidRadioButton.isSelected()) {
            cost = new Linkset(project, name, type, realPathCheckBox.isSelected(), distMax);
        } else if(costRadioButton.isSelected()) {
            if(table.getCellEditor() != null) {
                table.getCellEditor().stopCellEditing();
            }
            double[] costs = new double[maxCode+1];
            TableModel model = table.getModel();
            for(int i = 0; i < model.getRowCount(); i++) {
                costs[(Integer)model.getValueAt(i, 0)] = (Double)model.getValueAt(i, 1);
            }
            cost = new Linkset(project, name, type, costs, type_length, realPathCheckBox.isSelected(), 
                    removeCrossPatchCheckBox.isSelected(), distMax, coefSlope);
        } else {
            File f = rasterSelectFilePanel.getSelectedFile();
            cost = new Linkset(project, name, type, type_length, realPathCheckBox.isSelected(), 
                    removeCrossPatchCheckBox.isSelected(), distMax, f, coefSlope);
        }

        return cost;
    }

    /**
     * @return the linkset name 
     */
    public String getLinksetName() {
        return nameTextField.getText();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        topoPanel = new javax.swing.JPanel();
        completeRadioButton = new javax.swing.JRadioButton();
        planarRadioButton = new javax.swing.JRadioButton();
        distMaxLabel = new javax.swing.JLabel();
        dMaxSpinner = new javax.swing.JSpinner();
        removeCrossPatchCheckBox = new javax.swing.JCheckBox();
        realPathCheckBox = new javax.swing.JCheckBox();
        unitLabel = new javax.swing.JLabel();
        distPanel = new javax.swing.JPanel();
        impedancePanel = new javax.swing.JPanel();
        costDistRadioButton = new javax.swing.JRadioButton();
        lengthRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        euclidRadioButton = new javax.swing.JRadioButton();
        rasterSelectFilePanel = new org.thema.common.swing.SelectFilePanel();
        costRadioButton = new javax.swing.JRadioButton();
        rasterRadioButton = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        useDEMCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        coefSlopeSpinner = new javax.swing.JSpinner();
        loadCostsButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/links/Bundle"); // NOI18N
        nameLabel.setText(bundle.getString("LinksetPanel.nameLabel.text")); // NOI18N

        nameTextField.setText(bundle.getString("LinksetPanel.nameTextField.text")); // NOI18N

        topoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LinksetPanel.topoPanel.border.title"))); // NOI18N

        buttonGroup2.add(completeRadioButton);
        completeRadioButton.setText(bundle.getString("LinksetPanel.completeRadioButton.text")); // NOI18N

        buttonGroup2.add(planarRadioButton);
        planarRadioButton.setSelected(true);
        planarRadioButton.setText(bundle.getString("LinksetPanel.planarRadioButton.text")); // NOI18N

        distMaxLabel.setText(bundle.getString("LinksetPanel.distMaxLabel.text")); // NOI18N

        dMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));

        removeCrossPatchCheckBox.setText(bundle.getString("LinksetPanel.removeCrossPatchCheckBox.text")); // NOI18N
        removeCrossPatchCheckBox.setEnabled(false);

        realPathCheckBox.setSelected(true);
        realPathCheckBox.setText(bundle.getString("LinksetPanel.realPathCheckBox.text")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, completeRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), realPathCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        realPathCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realPathCheckBoxActionPerformed(evt);
            }
        });

        unitLabel.setText(bundle.getString("LinksetPanel.unitLabel.text")); // NOI18N

        javax.swing.GroupLayout topoPanelLayout = new javax.swing.GroupLayout(topoPanel);
        topoPanel.setLayout(topoPanelLayout);
        topoPanelLayout.setHorizontalGroup(
            topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topoPanelLayout.createSequentialGroup()
                .addGroup(topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(topoPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(removeCrossPatchCheckBox))
                    .addGroup(topoPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(topoPanelLayout.createSequentialGroup()
                                .addComponent(distMaxLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unitLabel))
                            .addComponent(planarRadioButton))))
                .addGap(18, 18, 18)
                .addGroup(topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(completeRadioButton)
                    .addGroup(topoPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(realPathCheckBox)))
                .addGap(0, 14, Short.MAX_VALUE))
        );
        topoPanelLayout.setVerticalGroup(
            topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topoPanelLayout.createSequentialGroup()
                .addGroup(topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(planarRadioButton)
                    .addComponent(completeRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(distMaxLabel)
                    .addComponent(dMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unitLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(topoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(realPathCheckBox)
                    .addComponent(removeCrossPatchCheckBox)))
        );

        distPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LinksetPanel.distPanel.border.title"))); // NOI18N

        impedancePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LinksetPanel.impedancePanel.border.title"))); // NOI18N

        buttonGroup3.add(costDistRadioButton);
        costDistRadioButton.setSelected(true);
        costDistRadioButton.setText(bundle.getString("LinksetPanel.costDistRadioButton.text")); // NOI18N
        costDistRadioButton.setEnabled(false);

        buttonGroup3.add(lengthRadioButton);
        lengthRadioButton.setText(bundle.getString("LinksetPanel.lengthRadioButton.text")); // NOI18N
        lengthRadioButton.setEnabled(false);

        javax.swing.GroupLayout impedancePanelLayout = new javax.swing.GroupLayout(impedancePanel);
        impedancePanel.setLayout(impedancePanelLayout);
        impedancePanelLayout.setHorizontalGroup(
            impedancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(impedancePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(impedancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lengthRadioButton)
                    .addComponent(costDistRadioButton)))
        );
        impedancePanelLayout.setVerticalGroup(
            impedancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(impedancePanelLayout.createSequentialGroup()
                .addComponent(costDistRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lengthRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Cost"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.setRowSelectionAllowed(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), table, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jScrollPane1.setViewportView(table);

        buttonGroup1.add(euclidRadioButton);
        euclidRadioButton.setSelected(true);
        euclidRadioButton.setText(bundle.getString("LinksetPanel.euclidRadioButton.text")); // NOI18N
        euclidRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceRadioButtonActionPerformed(evt);
            }
        });

        rasterSelectFilePanel.setDescription(bundle.getString("LinksetPanel.rasterSelectFilePanel.description")); // NOI18N
        rasterSelectFilePanel.setFileDesc(bundle.getString("LinksetPanel.rasterSelectFilePanel.fileDesc")); // NOI18N
        rasterSelectFilePanel.setFileExts(bundle.getString("LinksetPanel.rasterSelectFilePanel.fileExts")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rasterRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), rasterSelectFilePanel, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup1.add(costRadioButton);
        costRadioButton.setText(bundle.getString("LinksetPanel.costRadioButton.text")); // NOI18N
        costRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(rasterRadioButton);
        rasterRadioButton.setText(bundle.getString("LinksetPanel.rasterRadioButton.text")); // NOI18N
        rasterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceRadioButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LinksetPanel.jPanel1.border.title"))); // NOI18N

        useDEMCheckBox.setText(bundle.getString("LinksetPanel.useDEMCheckBox.text")); // NOI18N
        useDEMCheckBox.setEnabled(false);

        jLabel1.setText(bundle.getString("LinksetPanel.jLabel1.text")); // NOI18N

        coefSlopeSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, null, 1.0d));
        coefSlopeSpinner.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(useDEMCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coefSlopeSpinner)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useDEMCheckBox)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(coefSlopeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        loadCostsButton.setText(bundle.getString("LinksetPanel.loadCostsButton.text")); // NOI18N
        loadCostsButton.setEnabled(false);
        loadCostsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCostsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout distPanelLayout = new javax.swing.GroupLayout(distPanel);
        distPanel.setLayout(distPanelLayout);
        distPanelLayout.setHorizontalGroup(
            distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(distPanelLayout.createSequentialGroup()
                .addGroup(distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(distPanelLayout.createSequentialGroup()
                        .addGroup(distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(distPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(costRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(distPanelLayout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(12, 12, 12)))
                        .addGroup(distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(impedancePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(distPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(distPanelLayout.createSequentialGroup()
                                .addComponent(euclidRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(loadCostsButton))
                            .addGroup(distPanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(rasterSelectFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(distPanelLayout.createSequentialGroup()
                                .addComponent(rasterRadioButton)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        distPanelLayout.setVerticalGroup(
            distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(distPanelLayout.createSequentialGroup()
                .addGroup(distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(euclidRadioButton)
                    .addComponent(loadCostsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(distPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(distPanelLayout.createSequentialGroup()
                        .addComponent(costRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(distPanelLayout.createSequentialGroup()
                        .addComponent(impedancePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rasterRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rasterSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addGap(6, 6, 6)
                        .addComponent(nameTextField))
                    .addComponent(distPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(topoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(distPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void distanceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distanceRadioButtonActionPerformed
        lengthRadioButton.setEnabled(!euclidRadioButton.isSelected());
        costDistRadioButton.setEnabled(!euclidRadioButton.isSelected());
        removeCrossPatchCheckBox.setEnabled(!euclidRadioButton.isSelected() && realPathCheckBox.isSelected());
        useDEMCheckBox.setEnabled(!euclidRadioButton.isSelected() && project != null && project.isDemExist());
        coefSlopeSpinner.setEnabled(!euclidRadioButton.isSelected() && project != null && project.isDemExist());
        loadCostsButton.setEnabled(!euclidRadioButton.isSelected());
        if(euclidRadioButton.isSelected()) {
            unitLabel.setText(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("UnitMeter"));
        } else {
            unitLabel.setText(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("UnitCost"));
        }
    }//GEN-LAST:event_distanceRadioButtonActionPerformed

    private void realPathCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realPathCheckBoxActionPerformed
        removeCrossPatchCheckBox.setEnabled(!euclidRadioButton.isSelected() && realPathCheckBox.isSelected());
    }//GEN-LAST:event_realPathCheckBoxActionPerformed

    private void loadCostsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadCostsButtonActionPerformed
        LoadCostsDialog dlg = new LoadCostsDialog(null, project);
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        setCodes(project.getCodes(), dlg.selectedLinkset.getCosts());
    }//GEN-LAST:event_loadCostsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JSpinner coefSlopeSpinner;
    private javax.swing.JRadioButton completeRadioButton;
    private javax.swing.JRadioButton costDistRadioButton;
    private javax.swing.JRadioButton costRadioButton;
    private javax.swing.JSpinner dMaxSpinner;
    private javax.swing.JLabel distMaxLabel;
    private javax.swing.JPanel distPanel;
    private javax.swing.JRadioButton euclidRadioButton;
    private javax.swing.JPanel impedancePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton lengthRadioButton;
    private javax.swing.JButton loadCostsButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JRadioButton planarRadioButton;
    private javax.swing.JRadioButton rasterRadioButton;
    private org.thema.common.swing.SelectFilePanel rasterSelectFilePanel;
    private javax.swing.JCheckBox realPathCheckBox;
    private javax.swing.JCheckBox removeCrossPatchCheckBox;
    private javax.swing.JTable table;
    private javax.swing.JPanel topoPanel;
    private javax.swing.JLabel unitLabel;
    private javax.swing.JCheckBox useDEMCheckBox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
