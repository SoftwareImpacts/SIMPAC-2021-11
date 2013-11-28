/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.model;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.thema.common.Config;
import org.thema.common.parallel.ProgressBar;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class MetricInterpolDlg extends javax.swing.JDialog {


    /** Creates new form MetricInterpolDlg */
    public MetricInterpolDlg(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        
        graphComboBox.setModel(new DefaultComboBoxModel(Project.getProject().getGraphs().toArray()));
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

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
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
        jLabel9 = new javax.swing.JLabel();
        graphComboBox = new javax.swing.JComboBox();
        varComboBox = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        resolSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        rasterNameTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/model/Bundle"); // NOI18N
        setTitle(bundle.getString("MetricInterpolDlg.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText(bundle.getString("MetricInterpolDlg.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle.getString("MetricInterpolDlg.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ModelDialog.jPanel1.border.title"))); // NOI18N

        pSpinner.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.001d, 1.0d, 0.01d));
        pSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pSpinnerStateChanged(evt);
            }
        });

        jLabel7.setText(bundle.getString("ModelDialog.jLabel7.text")); // NOI18N

        jLabel6.setText(bundle.getString("ModelDialog.jLabel6.text")); // NOI18N

        jLabel5.setText(bundle.getString("ModelDialog.jLabel5.text")); // NOI18N

        jLabel8.setText(bundle.getString("ModelDialog.jLabel8.text")); // NOI18N

        alphaTextField.setText(bundle.getString("ModelDialog.alphaTextField.text")); // NOI18N

        dSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1000.0d), Double.valueOf(0.001d), null, Double.valueOf(1.0d)));
        dSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dSpinnerStateChanged(evt);
            }
        });

        multiAttachCheckBox.setText(bundle.getString("ModelDialog.multiAttachCheckBox.text")); // NOI18N

        dMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1000.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, multiAttachCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), dMaxSpinner, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel3.setText(bundle.getString("ModelDialog.jLabel3.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, multiAttachCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel3, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(multiAttachCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dMaxSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(alphaTextField)
                        .add(10, 10, 10)
                        .add(jLabel8)
                        .add(49, 49, 49))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dSpinner)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pSpinner)
                        .add(43, 43, 43))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(alphaTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(dSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7)
                    .add(pSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(multiAttachCheckBox)
                    .add(dMaxSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addContainerGap())
        );

        jLabel9.setText(bundle.getString("ModelDialog.jLabel9.text")); // NOI18N

        graphComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphComboBoxActionPerformed(evt);
            }
        });

        varComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                varComboBoxActionPerformed(evt);
            }
        });

        jLabel11.setText(bundle.getString("ExtrapolateDialog.jLabel10.text")); // NOI18N

        resolSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        jLabel1.setText(bundle.getString("MetricInterpolDlg.jLabel1.text")); // NOI18N

        rasterNameTextField.setText(bundle.getString("MetricInterpolDlg.rasterNameTextField.text")); // NOI18N

        jLabel2.setText(bundle.getString("MetricInterpolDlg.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel9)
                            .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(graphComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(varComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel11))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rasterNameTextField)
                            .add(layout.createSequentialGroup()
                                .add(resolSpinner)
                                .add(176, 176, 176)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(graphComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(varComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(18, 18, 18)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resolSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(rasterNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        getRootPane().setDefaultButton(okButton);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProgressBar progressBar = Config.getProgressBar();
                    okButton.setEnabled(false);
                    RasterLayer l = DistribModel.interpolate(Project.getProject(), (Double)resolSpinner.getValue(), varComboBox.getSelectedItem().toString(), 
                                        Double.parseDouble(alphaTextField.getText()), ((GraphGenerator)graphComboBox.getSelectedItem()).getCostDistance(), 
                                        multiAttachCheckBox.isSelected(), (Double)dMaxSpinner.getValue(), progressBar);
                    progressBar.setNote("Saving");
                    l.setName(rasterNameTextField.getText());
                    l.setRemovable(true);
                    Project.getProject().getAnalysisLayer().addLayerFirst(l);
                    l.saveRaster(new File(Project.getProject().getProjectDir(), l.getName() + ".tif"));
                    progressBar.close();
                } catch (Throwable ex) {
                    Logger.getLogger(MetricInterpolDlg.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MetricInterpolDlg.this, "Error : " + ex);
                } finally {
                    okButton.setEnabled(true);
                }
            }
        }).start();
        
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    private void pSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pSpinnerStateChanged
        alphaTextField.setText(String.valueOf(-Math.log((Double) pSpinner.getValue()) / (Double) dSpinner.getValue()));
    }//GEN-LAST:event_pSpinnerStateChanged

    private void dSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dSpinnerStateChanged
        alphaTextField.setText(String.valueOf(-Math.log((Double) pSpinner.getValue()) / (Double) dSpinner.getValue()));
    }//GEN-LAST:event_dSpinnerStateChanged

    private void graphComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphComboBoxActionPerformed

        GraphGenerator g = (GraphGenerator) graphComboBox.getSelectedItem();
        varComboBox.setModel(new DefaultComboBoxModel(Project.getProject().getGraphPatchAttr(g.getName()).toArray()));
        varComboBoxActionPerformed(null);
        if (g.getType() == GraphGenerator.THRESHOLD) 
            dSpinner.setValue(g.getThreshold());
        
    }//GEN-LAST:event_graphComboBoxActionPerformed

    private void varComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_varComboBoxActionPerformed
        rasterNameTextField.setText("Interp_" + varComboBox.getSelectedItem());
    }//GEN-LAST:event_varComboBoxActionPerformed

    private void doClose() {
        setVisible(false);
        dispose();
    }

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alphaTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JSpinner dMaxSpinner;
    private javax.swing.JSpinner dSpinner;
    private javax.swing.JComboBox graphComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox multiAttachCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JSpinner pSpinner;
    private javax.swing.JTextField rasterNameTextField;
    private javax.swing.JSpinner resolSpinner;
    private javax.swing.JComboBox varComboBox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
