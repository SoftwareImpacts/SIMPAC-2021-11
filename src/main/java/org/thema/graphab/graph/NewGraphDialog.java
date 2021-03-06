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

import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import org.thema.graphab.links.Linkset;

/**
 * Dialog form for creating a new graph.
 * 
 * @author Gilles Vuidel
 */
public class NewGraphDialog extends javax.swing.JDialog {

    /** has user clicked Ok ? */
    public boolean isOk = false;
    /** name of the graph */
    public String name;
    /** the linkset used by the graph */
    public Linkset linkset;
    /** the threshold if any */
    public double threshold;
    /** type of graph : COMPLETE, THRESHOLD or MST */
    public int type;
    /** Use intrapatch distances for path calculation ? */
    public boolean intraPatchDist;

    /** 
     * Creates new form NewGraphDialog
     * @param parent the parent window
     * @param linksets the project linksets
     */
    public NewGraphDialog(java.awt.Frame parent, Collection<Linkset> linksets) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);

        costComboBox.setModel(new DefaultComboBoxModel(linksets.toArray()));
        costComboBoxActionPerformed(null);
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

        buttonGroup2 = new javax.swing.ButtonGroup();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        costComboBox = new javax.swing.JComboBox();
        linksetLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        completeRadioButton = new javax.swing.JRadioButton();
        thresholdRadioButton = new javax.swing.JRadioButton();
        mstRadioButton = new javax.swing.JRadioButton();
        thresholdSpinner = new javax.swing.JSpinner();
        intraPatchCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("NewGraphDialog.title")); // NOI18N

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/thema/graphab/graph/Bundle"); // NOI18N
        okButton.setText(bundle1.getString("NewGraphDialog.okButton.text")); // NOI18N
        okButton.setEnabled(false);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle1.getString("NewGraphDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        costComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                costComboBoxActionPerformed(evt);
            }
        });

        linksetLabel.setText(bundle1.getString("NewGraphDialog.linksetLabel.text")); // NOI18N

        nameLabel.setText(bundle1.getString("NewGraphDialog.nameLabel.text")); // NOI18N

        nameTextField.setText(bundle1.getString("NewGraphDialog.nameTextField.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("NewGraphDialog.jPanel1.border.title"))); // NOI18N

        buttonGroup2.add(completeRadioButton);
        completeRadioButton.setText(bundle1.getString("NewGraphDialog.completeRadioButton.text")); // NOI18N
        completeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(thresholdRadioButton);
        thresholdRadioButton.setSelected(true);
        thresholdRadioButton.setText(bundle1.getString("NewGraphDialog.thresholdRadioButton.text")); // NOI18N
        thresholdRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(mstRadioButton);
        mstRadioButton.setText(bundle1.getString("NewGraphDialog.mstRadioButton.text")); // NOI18N
        mstRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeRadioButtonActionPerformed(evt);
            }
        });

        thresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, thresholdRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), thresholdSpinner, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        thresholdSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                thresholdSpinnerStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(thresholdRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(thresholdSpinner))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(completeRadioButton)
                            .add(mstRadioButton))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(thresholdRadioButton)
                    .add(thresholdSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3)
                .add(completeRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mstRadioButton))
        );

        intraPatchCheckBox.setSelected(true);
        intraPatchCheckBox.setText(bundle1.getString("NewGraphDialog.intraPatchCheckBox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(linksetLabel)
                            .add(nameLabel))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .add(costComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton))
                    .add(intraPatchCheckBox))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(linksetLabel)
                    .add(costComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(intraPatchCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 9, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        name = nameTextField.getText();
        linkset = (Linkset) costComboBox.getSelectedItem();
        threshold = (Double)thresholdSpinner.getValue();

        if(completeRadioButton.isSelected()) {
            type = GraphGenerator.COMPLETE;
        } else if(thresholdRadioButton.isSelected()) {
            type = GraphGenerator.THRESHOLD;
        } else {
            type = GraphGenerator.MST;
        }

        intraPatchDist = intraPatchCheckBox.isSelected();
        
        isOk = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void typeRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeRadioButtonActionPerformed
        okButton.setEnabled(!thresholdRadioButton.isSelected() || ((Number)thresholdSpinner.getValue()).doubleValue() > 0);
    }//GEN-LAST:event_typeRadioButtonActionPerformed

    private void thresholdSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdSpinnerStateChanged
        okButton.setEnabled(!thresholdRadioButton.isSelected() || ((Number)thresholdSpinner.getValue()).doubleValue() > 0);
    }//GEN-LAST:event_thresholdSpinnerStateChanged

    private void costComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_costComboBoxActionPerformed
        Linkset cost = (Linkset) costComboBox.getSelectedItem();
        intraPatchCheckBox.setSelected(cost.isRealPaths());
        intraPatchCheckBox.setEnabled(cost.isRealPaths());
    }//GEN-LAST:event_costComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton completeRadioButton;
    private javax.swing.JComboBox costComboBox;
    private javax.swing.JCheckBox intraPatchCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel linksetLabel;
    private javax.swing.JRadioButton mstRadioButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton thresholdRadioButton;
    private javax.swing.JSpinner thresholdSpinner;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
