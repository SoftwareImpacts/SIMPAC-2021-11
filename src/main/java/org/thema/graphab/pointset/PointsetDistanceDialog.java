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


package org.thema.graphab.pointset;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.thema.common.Config;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask.IterParallelTask;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.links.CircuitRaster;
import org.thema.graphab.metric.Circuit;
import org.thema.graphab.links.SpacePathFinder;
import org.thema.graphab.metric.DistProbaPanel;

/**
 * Dialog for calculating several distance matrices between the pointset.
 * It supports :
 * <ul>
 * <li>Least cost raster distance</li>
 * <li>Circuit raster distance</li>
 * <li>Simple graph distance</li>
 * <li>Circuit graph distance</li>
 * <li>Flow graph distance</li>
 * </ul>
 * 
 * @author Gilles Vuidel
 */
public class PointsetDistanceDialog extends javax.swing.JDialog {

    private Project project;
    private Pointset pointset;
    private DistProbaPanel probaPanel;
    
    /** Creates new form PointsetDistanceDialog */
    public PointsetDistanceDialog(java.awt.Frame parent, Project project, Pointset exo) {
        super(parent, true);
        
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
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
        this.project = project;
        this.pointset = exo;
        
        probaPanel = new DistProbaPanel(exo.getLinkset(), 1000, 0.05, 1);
        
        List<GraphGenerator> graphs = new ArrayList<>();
        for(GraphGenerator g : project.getGraphs()) {
            if (g.getLinkset() == exo.getLinkset()) {
                graphs.add(g);
            }
        }
        graphCostComboBox.setModel(new DefaultComboBoxModel(graphs.toArray()));
        costComboBox.setModel(new DefaultComboBoxModel(project.getLinksets().toArray()));
        costComboBox.setSelectedItem(exo.getLinkset());
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
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        rasterRadioButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        costComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        costGraphRadioButton = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        graphCostComboBox = new javax.swing.JComboBox();
        simpleRadioButton = new javax.swing.JRadioButton();
        circuitGraphRadioButton = new javax.swing.JRadioButton();
        flowRadioButton = new javax.swing.JRadioButton();
        costRadioButton = new javax.swing.JRadioButton();
        circuitRasterRadioButton = new javax.swing.JRadioButton();
        circuitFlowRadioButton = new javax.swing.JRadioButton();
        paramButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/pointset/Bundle"); // NOI18N
        setTitle(bundle.getString("PointsetDistanceDialog.title")); // NOI18N
        setName("Form"); // NOI18N

        okButton.setText(bundle.getString("PointsetDistanceDialog.okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle.getString("PointsetDistanceDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(rasterRadioButton);
        rasterRadioButton.setSelected(true);
        rasterRadioButton.setText(bundle.getString("PointsetDistanceDialog.rasterRadioButton.text")); // NOI18N
        rasterRadioButton.setName("rasterRadioButton"); // NOI18N

        jLabel2.setText(bundle.getString("PointsetDistanceDialog.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rasterRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel2, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        costComboBox.setName("costComboBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rasterRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), costComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel3.setText(bundle.getString("PointsetDistanceDialog.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        fileTextField.setText(bundle.getString("PointsetDistanceDialog.fileTextField.text")); // NOI18N
        fileTextField.setName("fileTextField"); // NOI18N

        buttonGroup1.add(costGraphRadioButton);
        costGraphRadioButton.setText(bundle.getString("PointsetDistanceDialog.costGraphRadioButton.text")); // NOI18N
        costGraphRadioButton.setName("costGraphRadioButton"); // NOI18N

        jLabel5.setText(bundle.getString("PointsetDistanceDialog.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costGraphRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jLabel5, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        graphCostComboBox.setName("graphCostComboBox"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costGraphRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), graphCostComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup2.add(simpleRadioButton);
        simpleRadioButton.setSelected(true);
        simpleRadioButton.setText(bundle.getString("PointsetDistanceDialog.simpleRadioButton.text")); // NOI18N
        simpleRadioButton.setName("simpleRadioButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costGraphRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), simpleRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        simpleRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeDistRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(circuitGraphRadioButton);
        circuitGraphRadioButton.setText(bundle.getString("PointsetDistanceDialog.circuitGraphRadioButton.text")); // NOI18N
        circuitGraphRadioButton.setName("circuitGraphRadioButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costGraphRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), circuitGraphRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        circuitGraphRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeDistRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(flowRadioButton);
        flowRadioButton.setText(bundle.getString("PointsetDistanceDialog.flowRadioButton.text")); // NOI18N
        flowRadioButton.setName("flowRadioButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costGraphRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), flowRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        flowRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeDistRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup3.add(costRadioButton);
        costRadioButton.setSelected(true);
        costRadioButton.setText(bundle.getString("PointsetDistanceDialog.costRadioButton.text")); // NOI18N
        costRadioButton.setName("costRadioButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rasterRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), costRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup3.add(circuitRasterRadioButton);
        circuitRasterRadioButton.setText(bundle.getString("PointsetDistanceDialog.circuitRasterRadioButton.text")); // NOI18N
        circuitRasterRadioButton.setName("circuitRasterRadioButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rasterRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), circuitRasterRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        buttonGroup2.add(circuitFlowRadioButton);
        circuitFlowRadioButton.setText(bundle.getString("PointsetDistanceDialog.circuitFlowRadioButton.text")); // NOI18N
        circuitFlowRadioButton.setName("circuitFlowRadioButton"); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, costGraphRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), circuitFlowRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        circuitFlowRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeDistRadioButtonActionPerformed(evt);
            }
        });

        paramButton.setText(bundle.getString("PointsetDistanceDialog.paramButton.text")); // NOI18N
        paramButton.setEnabled(false);
        paramButton.setName("paramButton"); // NOI18N
        paramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paramButtonActionPerformed(evt);
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
                        .add(costGraphRadioButton)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(0, 0, Short.MAX_VALUE)
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cancelButton))
                            .add(layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileTextField))
                            .add(layout.createSequentialGroup()
                                .add(rasterRadioButton)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel2)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(costComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel5)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(graphCostComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(layout.createSequentialGroup()
                                        .add(12, 12, 12)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(layout.createSequentialGroup()
                                                .add(costRadioButton)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(circuitRasterRadioButton))
                                            .add(layout.createSequentialGroup()
                                                .add(circuitFlowRadioButton)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(paramButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .add(layout.createSequentialGroup()
                                                .add(simpleRadioButton)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(circuitGraphRadioButton)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(flowRadioButton)))
                                        .add(0, 0, Short.MAX_VALUE)))))
                        .addContainerGap())))
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(rasterRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(costComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(costRadioButton)
                    .add(circuitRasterRadioButton))
                .add(18, 18, 18)
                .add(costGraphRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(graphCostComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(simpleRadioButton)
                    .add(circuitGraphRadioButton)
                    .add(flowRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(circuitFlowRadioButton)
                    .add(paramButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(27, 27, 27)
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
        setVisible(false);
        dispose();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<DefaultFeature> exos = pointset.getFeatures();
                ProgressBar mon = Config.getProgressBar("Distances...", exos.size());

                final double [][][] distances = new double[exos.size()][exos.size()][2];  
                if(rasterRadioButton.isSelected()) {
                    Linkset costDist = (Linkset) costComboBox.getSelectedItem();
                    final List<Coordinate> dests = new ArrayList<>();
                    for(Feature f : exos) {
                        dests.add(f.getGeometry().getCoordinate());
                    }
                    IterParallelTask task;
                    if(costRadioButton.isSelected()) {
                        final Linkset linkset = costDist.getCostVersion();
                        task = new IterParallelTask(exos.size(), mon) {
                            @Override
                            protected void executeOne(Integer ind) {
                                try {
                                    SpacePathFinder pathFinder = project.getPathFinder(linkset);
                                    List<double[]> dist = pathFinder.calcPaths(exos.get(ind).getGeometry().getCoordinate(), dests);
                                    for(int j = ind+1; j < exos.size(); j++) {
                                        distances[ind][j][0] = dist.get(j)[0];
                                        distances[ind][j][1] = dist.get(j)[1];
                                        distances[j][ind][0] = distances[ind][j][0];
                                        distances[j][ind][1] = distances[ind][j][1];
                                    }
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                } 
                            }
                        };
                    } else {
                        final CircuitRaster circuit;
                        try {
                            circuit = project.getRasterCircuit(costDist.getCircuitVersion());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        task = new IterParallelTask(exos.size(), mon) {
                            @Override
                            protected void executeOne(Integer ind) {
                                for(int j = ind+1; j < exos.size(); j++) {
                                    distances[ind][j][0] = circuit.getODCircuit(dests.get(ind), dests.get(j)).getR();
                                    distances[ind][j][1] = Double.NaN;
                                    distances[j][ind][0] = distances[ind][j][0];
                                    distances[j][ind][1] = Double.NaN;
                                }
                            }
                        };
                    }
                    new ParallelFExecutor(task).executeAndWait();
                    if(task.isCanceled()) {
                        return;
                    }
                } else if(costGraphRadioButton.isSelected()) {
                    GraphGenerator graph = (GraphGenerator) graphCostComboBox.getSelectedItem();
                    if(simpleRadioButton.isSelected()) {
                        for(int i = 0; i < exos.size(); i++) {
                            Feature exo1 = exos.get(i);
                            Feature patch1 = project.getPatch((Integer)exo1.getAttribute(Project.EXO_IDPATCH));
                            GraphPathFinder finder = graph.getPathFinder(graph.getNode(patch1));
                            for(int j = 0; j < exos.size(); j++) {
                                if(i == j) {
                                    continue;
                                }
                                Feature exo2 = exos.get(j);
                                Feature patch2 = project.getPatch((Integer)exo2.getAttribute(Project.EXO_IDPATCH));
                                Double dist = finder.getCost(graph.getNode(patch2));
                                if(dist == null) {
                                    dist = Double.NaN;
                                }
                                dist += ((Number)exo1.getAttribute(Project.EXO_COST)).doubleValue() +
                                        ((Number)exo2.getAttribute(Project.EXO_COST)).doubleValue();
                                distances[i][j][0] = dist;
                            }
                            mon.incProgress(1);
                        }
                    } else if(flowRadioButton.isSelected()) {
                        double alpha = probaPanel.getAlpha();

                        for(int i = 0; i < exos.size(); i++) {
                            Feature exo1 = exos.get(i);
                            Feature patch1 = project.getPatch((Integer)exo1.getAttribute(Project.EXO_IDPATCH));
                            GraphPathFinder finder = graph.getFlowPathFinder(graph.getNode(patch1), alpha);
                            for(int j = 0; j < exos.size(); j++) {
                                if(i == j) {
                                    continue;
                                }
                                Feature exo2 = exos.get(j);
                                Feature patch2 = project.getPatch((Integer)exo2.getAttribute(Project.EXO_IDPATCH));
                                Double dist = finder.getCost(graph.getNode(patch2));
                                if(dist == null) {
                                    dist = Double.NaN;
                                }

                                distances[i][j][0] = dist - Math.log(Project.getPatchCapacity(patch1)*Project.getPatchCapacity(patch2)
                                            / Math.pow(project.getTotalPatchCapacity(), 2))
                                        + alpha * (((Number)exo1.getAttribute(Project.EXO_COST)).doubleValue() +
                                        ((Number)exo2.getAttribute(Project.EXO_COST)).doubleValue());
                            }
                            mon.incProgress(1);
                        }
                    } else {
                        Circuit circuit = circuitFlowRadioButton.isSelected() ? new Circuit(graph, probaPanel.getAlpha()) : new Circuit(graph);

                        for(int i = 0; i < exos.size(); i++) {
                            Feature exo1 = exos.get(i);
                            Feature patch1 = project.getPatch((Integer)exo1.getAttribute(Project.EXO_IDPATCH));

                            for(int j = 0; j < exos.size(); j++) {
                                if(i == j) {
                                    continue;
                                }
                                Feature exo2 = exos.get(j);
                                Feature patch2 = project.getPatch((Integer)exo2.getAttribute(Project.EXO_IDPATCH));
                                if(patch1.equals(patch2)) {
                                    continue;
                                }
                                distances[i][j][0] = circuit.computeR(graph.getNode(patch1), graph.getNode(patch2));
                            }
                            mon.incProgress(1);
                        }
                    }
                }
                mon.setNote("Saving...");
                try (FileWriter fw = new FileWriter(new File(project.getDirectory(), fileTextField.getText()))) {
                    fw.write("Id1\tId2\tDistance\tLength\n");
                    for(int i = 0; i < exos.size(); i++) {
                        for(int j = 0; j < exos.size(); j++) {
                            fw.write(exos.get(i).getId() + "\t" + exos.get(j).getId() + "\t" + distances[i][j][0] + "\t" + distances[i][j][1] + "\n");
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PointsetDistanceDialog.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(PointsetDistanceDialog.this, "An error has occured : \n" + ex.getLocalizedMessage());
                    return;
                } 
                mon.close();
                JOptionPane.showMessageDialog(PointsetDistanceDialog.this, "Operation finished");
            }
        }).start();

    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void typeDistRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeDistRadioButtonActionPerformed
        paramButton.setEnabled(costGraphRadioButton.isSelected() && (flowRadioButton.isSelected() || circuitFlowRadioButton.isSelected()));
    }//GEN-LAST:event_typeDistRadioButtonActionPerformed

    private void paramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramButtonActionPerformed
        JOptionPane.showMessageDialog(this, probaPanel);
    }//GEN-LAST:event_paramButtonActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton circuitFlowRadioButton;
    private javax.swing.JRadioButton circuitGraphRadioButton;
    private javax.swing.JRadioButton circuitRasterRadioButton;
    private javax.swing.JComboBox costComboBox;
    private javax.swing.JRadioButton costGraphRadioButton;
    private javax.swing.JRadioButton costRadioButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JRadioButton flowRadioButton;
    private javax.swing.JComboBox graphCostComboBox;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton okButton;
    private javax.swing.JButton paramButton;
    private javax.swing.JRadioButton rasterRadioButton;
    private javax.swing.JRadioButton simpleRadioButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
