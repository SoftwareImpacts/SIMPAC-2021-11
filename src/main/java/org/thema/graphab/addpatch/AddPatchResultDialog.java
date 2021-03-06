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
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.*;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.data.feature.DefaultFeature;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.PointStyle;
import org.thema.graphab.Project;
import org.thema.graphab.metric.global.GlobalMetric;

/**
 * Dialog form showing results of add patch command.
 * 
 * @author Gilles Vuidel
 */
public class AddPatchResultDialog extends javax.swing.JDialog {

    private DefaultGroupLayer layers;
    
    /**
     * Creates new form AddPatchResultDialog
     * @param parent the parent frame
     * @param project the project for adding patches
     */
    public AddPatchResultDialog(JFrame parent, Project project) {
        super(parent, false);
        initComponents();
        setLocationRelativeTo(parent);
        
        layers = new DefaultGroupLayer("Results", true);
        FeatureLayer l = new FeatureLayer("Patches", project.getPatches(), new FeatureStyle(new Color(0x65a252), new Color(0x426f3c)));
        l.setVisible(false);
        layers.addLayerFirst(l);
        mapViewer1.setRootLayer(layers);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        mapViewer1 = new org.thema.drawshape.ui.MapViewer();
        graphPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/addpatch/Bundle"); // NOI18N
        setTitle(bundle.getString("AddPatchResultDialog.title")); // NOI18N

        closeButton.setText(bundle.getString("AddPatchResultDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jSplitPane1.setDividerLocation(500);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setLeftComponent(mapViewer1);

        graphPanel.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(graphPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1008, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 481, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(closeButton)
                .addGap(6, 6, 6))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed
    
    /**
     * Shows the results of addPatch command
     * @param metric the global metric
     * @param gen the graph
     * @param addedPatches the added patches
     * @param indiceValues the metric value for each added patch
     */
    public void showResults(GlobalMetric metric, GraphGenerator gen, List<DefaultFeature> addedPatches, TreeMap<Integer, Double> indiceValues) {
        XYSeries serie = new XYSeries("Serie");
        for(Integer nbPatch : indiceValues.keySet()) {
            serie.add(nbPatch, indiceValues.get(nbPatch));
        }
        JFreeChart chart = ChartFactory.createXYLineChart(null, "# added patch", metric.getDetailName(), 
                new XYSeriesCollection(serie), PlotOrientation.VERTICAL, false, false, false);
        ((NumberAxis)chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        
        layers.addLayerFirst(new FeatureLayer("Patches added", addedPatches, new PointStyle(Color.red, 1, Color.orange, "Etape")));
        layers.addLayerLast(new GraphGenerator(gen, "").getLayers());
        setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JSplitPane jSplitPane1;
    private org.thema.drawshape.ui.MapViewer mapViewer1;
    // End of variables declaration//GEN-END:variables
}
