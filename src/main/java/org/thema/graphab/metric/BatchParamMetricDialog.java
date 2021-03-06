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


package org.thema.graphab.metric;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Dialog for selecting a graph, a metric, a parameter of the metric and a range of values for the parameter.
 * 
 * @author Gilles Vuidel
 */
public class BatchParamMetricDialog<T extends Metric> extends javax.swing.JDialog {

    /** the selected graph */
    public GraphGenerator graph;
    /** the selected metric */
    public T metric;
    /** the selected param of the metric */
    public String param;
    /** the value range for the parameter */
    public double min, max, inc;
    /** has user clicked Ok ? */
    public boolean isOk = false;
    
    private List<? extends T> metrics;

    private Project project;
    
    /**
     * Creates new form BatchParamMetricDialog
     * @param parent parent frame
     * @param graphs collection of graphs
     * @param metrics list of metrics
     */
    public BatchParamMetricDialog(java.awt.Frame parent, Project project, Collection<GraphGenerator> graphs, List<? extends T> metrics) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
        this.project = project;
        this.metrics = metrics;
        graphComboBox.setModel(new DefaultComboBoxModel(graphs.toArray()));
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

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        graphComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        paramComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        minSpinner = new javax.swing.JSpinner();
        maxSpinner = new javax.swing.JSpinner();
        incSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        paramPanel = new javax.swing.JPanel();
        indiceComboBox = new org.thema.graphab.metric.MetricComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("BatchParamMetricDialog.title")); // NOI18N

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/thema/graphab/metric/Bundle"); // NOI18N
        okButton.setText(bundle1.getString("BatchParamMetricDialog.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle1.getString("BatchParamMetricDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle1.getString("BatchParamMetricDialog.jLabel1.text")); // NOI18N

        graphComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle1.getString("BatchParamMetricDialog.jLabel2.text")); // NOI18N

        paramComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paramComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText(bundle1.getString("BatchParamMetricDialog.jLabel3.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("BatchParamMetricDialog.jPanel1.border.title"))); // NOI18N

        jLabel4.setText(bundle1.getString("BatchParamMetricDialog.jLabel4.text")); // NOI18N

        jLabel5.setText(bundle1.getString("BatchParamMetricDialog.jLabel5.text")); // NOI18N

        minSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        maxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        incSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        jLabel6.setText(bundle1.getString("BatchParamMetricDialog.jLabel6.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel6)
                    .add(jLabel5))
                .add(12, 12, 12)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(incSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                    .add(maxSpinner)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, minSpinner))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(minSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(incSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        paramPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle1.getString("BatchParamMetricDialog.paramPanel.border.title"))); // NOI18N
        paramPanel.setLayout(new java.awt.BorderLayout());

        indiceComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiceComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(paramPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(indiceComboBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(paramComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cancelButton))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(graphComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(graphComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(indiceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(paramPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(paramComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .add(18, 18, 18)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelButton)
                            .add(okButton))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        min = (Double)minSpinner.getValue();
        max = (Double)maxSpinner.getValue();
        inc = (Double)incSpinner.getValue();

        metric.setParams(((ParamPanel)paramPanel.getComponent(0)).getParams());
        
        isOk = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void graphComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphComboBoxActionPerformed
        graph = (GraphGenerator) graphComboBox.getSelectedItem();
        List<Metric> inds = new ArrayList<>();
        for(Metric ind : metrics) {
            if(ind.isAcceptGraph(graph)) {
                inds.add(ind);
            }
        }
        indiceComboBox.setMetrics(inds);
}//GEN-LAST:event_graphComboBoxActionPerformed

    private void paramComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramComboBoxActionPerformed
        param = paramComboBox.getSelectedItem().toString();
}//GEN-LAST:event_paramComboBoxActionPerformed

    private void indiceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiceComboBoxActionPerformed
        metric = (T)indiceComboBox.getSelectedItem();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for(String p : metric.getParams().keySet()) {
            if(metric.getParams().get(p) instanceof Number) {
                model.addElement(p);
            }
        }
        paramComboBox.setModel(model);
        paramComboBoxActionPerformed(null);
        
        paramPanel.removeAll();
        T ind = (T) indiceComboBox.getSelectedItem();
        paramPanel.add(ind.getParamPanel(project), BorderLayout.CENTER);
        paramPanel.revalidate();
        paramPanel.repaint();
    }//GEN-LAST:event_indiceComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox graphComboBox;
    private javax.swing.JSpinner incSpinner;
    private org.thema.graphab.metric.MetricComboBox indiceComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSpinner maxSpinner;
    private javax.swing.JSpinner minSpinner;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox paramComboBox;
    private javax.swing.JPanel paramPanel;
    // End of variables declaration//GEN-END:variables

}
