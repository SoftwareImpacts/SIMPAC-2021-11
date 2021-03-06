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



package org.thema.graphab.util;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;
import org.thema.drawshape.layer.RasterLayer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Frame for showing a line chart.
 * 
 * @author Gilles Vuidel
 */
public class SerieFrame extends javax.swing.JFrame {

    private JFreeChart chart;
    private XYSeriesCollection series;
    private String xLabel, yLabel;

    /**
     * Creates a new SerieFrame
     * @param title frame title
     * @param series data series
     * @param xLabel label for x axis
     * @param yLabel label for y axis
     */
    public SerieFrame(String title, XYSeriesCollection series, String xLabel, String yLabel) {
        super(title);
        initComponents();
        this.series = series;
        this.xLabel = xLabel == null ? "X" : xLabel;
        this.yLabel = yLabel == null ? "Y" : yLabel;
        chart = ChartFactory.createXYLineChart("", xLabel, yLabel,
                            series,
                            PlotOrientation.VERTICAL, false, false, false);
        ChartPanel chartPanel = new ChartPanel(chart, 500, 500, 100, 100,
                2000, 2000, true, true, true, true, true, true);

        panel.add(chartPanel, BorderLayout.CENTER);
        panel.validate();

        pack();
        setLocationRelativeTo(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        exportButton = new javax.swing.JButton();
        panel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jToolBar1.setRollover(true);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/util/Bundle"); // NOI18N
        exportButton.setText(bundle.getString("SerieFrame.exportButton.text")); // NOI18N
        exportButton.setFocusable(false);
        exportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(exportButton);

        panel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        ExtensionFileFilter svgFilter = new ExtensionFileFilter(
                "Image SVG", ".svg");
        fileChooser.addChoosableFileFilter(svgFilter);
        ExtensionFileFilter txtFilter = new ExtensionFileFilter(
                "Texte", ".txt");
        fileChooser.addChoosableFileFilter(txtFilter);
        
        int option = fileChooser.showSaveDialog(null);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }


        String filename = fileChooser.getSelectedFile().getPath();
        if(fileChooser.getFileFilter() == svgFilter) {
            if (!filename.endsWith(".svg")) {
                filename = filename + ".svg";
            }


            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(null, "svg", null);

            // Create an instance of the SVG Generator
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

            // draw the chart in the SVG generator
            chart.draw(svgGenerator, new Rectangle2D.Float(0, 0, 600, 400));

            // Write svg file
            try (OutputStream outputStream = new FileOutputStream(filename)) {
                Writer out = new OutputStreamWriter(outputStream, "UTF-8");
                svgGenerator.stream(out, true /* use css */);
                outputStream.flush();
            } catch (IOException ex) {
                Logger.getLogger(RasterLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (!filename.endsWith(".txt")) {
                filename = filename + ".txt";
            }

            try (FileWriter w = new FileWriter(filename)) {
                w.write(xLabel);
                for (int i = 0; i < series.getSeriesCount(); i++) {
                    w.write("\t" + series.getSeriesKey(i).toString());
                }
                w.write("\n");
                for (int i = 0; i < series.getSeries(0).getItemCount(); i++) {
                    w.write(series.getX(0, i).toString());
                    for (int j = 0; j < series.getSeriesCount(); j++) {
                        w.write("\t" + series.getY(j, i));
                    }
                    w.write("\n");
                }
            } catch (IOException ex) {
                Logger.getLogger(RasterLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_exportButtonActionPerformed

 
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables

}
