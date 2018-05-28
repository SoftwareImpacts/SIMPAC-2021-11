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

package org.thema.graphab;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.StringContent;
import javax.swing.text.html.HTMLDocument;
import org.geotools.feature.SchemaException;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.common.Config;
import org.thema.common.JavaLoader;
import org.thema.common.ProgressBar;
import org.thema.common.Util;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.swing.LoggingDialog;
import org.thema.common.swing.PreferencesDialog;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.GlobalDataStore;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.WritableFeature;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.style.CircleStyle;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.table.ColorRamp;
import org.thema.drawshape.style.table.FeatureAttributeCollection;
import org.thema.drawshape.style.table.FeatureAttributeIterator;
import org.thema.drawshape.style.table.StrokeRamp;
import org.thema.graph.shape.GraphGroupLayer;
import org.thema.graphab.Project.Method;
import org.thema.graphab.addpatch.AddPatchDialog;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.NewGraphDialog;
import org.thema.graphab.links.LinksetPanel;
import org.thema.graphab.metric.BatchGraphMetricDialog;
import org.thema.graphab.metric.BatchGraphMetricTask;
import org.thema.graphab.metric.BatchParamMetricDialog;
import org.thema.graphab.metric.CalcMetricDialog;
import org.thema.graphab.metric.DeltaMetricTask;
import org.thema.graphab.metric.global.GlobalMetricLauncher;
import org.thema.graphab.metric.PreCalcMetric;
import org.thema.graphab.metric.PreCalcMetricTask;
import org.thema.graphab.metric.global.GlobalMetric;
import org.thema.graphab.metric.local.LocalMetric;
import org.thema.graphab.model.MetricInterpolDlg;
import org.thema.graphab.model.ModelDialog;
import org.thema.graphab.model.RandomPointDlg;
import org.thema.graphab.mpi.MpiLauncher;
import org.thema.graphab.pointset.PointImportDialog;
import org.thema.graphab.pointset.PointsetDataDialog;
import org.thema.graphab.util.SerieFrame;
import org.thema.parallel.ExecutorService;

/**
 * The main frame of the Graphab software.
 * 
 * @author Gilles Vuidel
 */
public class MainFrame extends javax.swing.JFrame {

    private Project project;
    private LoggingDialog logFrame;

    /** 
     * Creates new form MainFrame
     */
    public MainFrame() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/thema/graphab/ressources/ico64_graphab.png")));
        initComponents();
        setLocationRelativeTo(null);
        setTitle("Graphab - " + getVersion());
        mapViewer.putAddLayerButton();
        mapViewer.putExportButton();
        
        Config.setProgressBar(mapViewer.getProgressBar());
        logFrame = new LoggingDialog(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mapViewer = new org.thema.drawshape.ui.MapViewer();
        statusPanel = new javax.swing.JPanel();
        jMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newProjectMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        prefMenuItem = new javax.swing.JMenuItem();
        logMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        quitMenuItem = new javax.swing.JMenuItem();
        graphMenu = new javax.swing.JMenu();
        costDistMenuItem = new javax.swing.JMenuItem();
        createMenuItem = new javax.swing.JMenuItem();
        metaPatchMenuItem = new javax.swing.JMenuItem();
        dataMenu = new javax.swing.JMenu();
        calcCapaMenuItem = new javax.swing.JMenuItem();
        projectRemPatchMenuItem = new javax.swing.JMenuItem();
        addPointDataMenuItem = new javax.swing.JMenuItem();
        addPointMenuItem = new javax.swing.JMenuItem();
        setDEMMenuItem = new javax.swing.JMenuItem();
        indiceMenu = new javax.swing.JMenu();
        calcIndiceMenuItem = new javax.swing.JMenuItem();
        compIndiceMenuItem = new javax.swing.JMenuItem();
        localIndiceMenuItem = new javax.swing.JMenuItem();
        delatIndiceMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        batchGraphIndiceMenuItem = new javax.swing.JMenuItem();
        batchParamIndiceMenu = new javax.swing.JMenu();
        batchParamLocalMenuItem = new javax.swing.JMenuItem();
        batchParamGlobalMenuItem = new javax.swing.JMenuItem();
        analysisMenu = new javax.swing.JMenu();
        estimMenuItem = new javax.swing.JMenuItem();
        interpMetricMenuItem = new javax.swing.JMenuItem();
        addPatchMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("MainFrame.title")); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 734, Short.MAX_VALUE)
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        fileMenu.setText(bundle.getString("MainFrame.fileMenu.text")); // NOI18N

        newProjectMenuItem.setText(bundle.getString("MainFrame.newProjectMenuItem.text")); // NOI18N
        newProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newProjectMenuItem);

        openMenuItem.setText(bundle.getString("MainFrame.openMenuItem.text")); // NOI18N
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        prefMenuItem.setText(bundle.getString("MainFrame.prefMenuItem.text")); // NOI18N
        prefMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(prefMenuItem);

        logMenuItem.setText(bundle.getString("MainFrame.logMenuItem.text")); // NOI18N
        logMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(logMenuItem);
        fileMenu.add(jSeparator1);

        quitMenuItem.setText(bundle.getString("MainFrame.quitMenuItem.text")); // NOI18N
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitMenuItem);

        jMenuBar.add(fileMenu);

        graphMenu.setText(bundle.getString("MainFrame.graphMenu.text")); // NOI18N

        costDistMenuItem.setText(bundle.getString("MainFrame.costDistMenuItem.text")); // NOI18N
        costDistMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                costDistMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(costDistMenuItem);

        createMenuItem.setText(bundle.getString("MainFrame.createMenuItem.text")); // NOI18N
        createMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createGraphMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(createMenuItem);

        metaPatchMenuItem.setText(bundle.getString("MainFrame.metaPatchMenuItem.text")); // NOI18N
        metaPatchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                metaPatchMenuItemActionPerformed(evt);
            }
        });
        graphMenu.add(metaPatchMenuItem);

        jMenuBar.add(graphMenu);

        dataMenu.setText(bundle.getString("MainFrame.dataMenu.text")); // NOI18N

        calcCapaMenuItem.setText(bundle.getString("MainFrame.calcCapaMenuItem.text")); // NOI18N
        calcCapaMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcCapaMenuItemActionPerformed(evt);
            }
        });
        dataMenu.add(calcCapaMenuItem);

        projectRemPatchMenuItem.setText(bundle.getString("MainFrame.projectRemPatchMenuItem.text")); // NOI18N
        projectRemPatchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectRemPatchMenuItemActionPerformed(evt);
            }
        });
        dataMenu.add(projectRemPatchMenuItem);

        addPointDataMenuItem.setText(bundle.getString("MainFrame.addPointDataMenuItem.text")); // NOI18N
        addPointDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPointDataMenuItemActionPerformed(evt);
            }
        });
        dataMenu.add(addPointDataMenuItem);

        addPointMenuItem.setText(bundle.getString("MainFrame.addPointMenuItem.text")); // NOI18N
        addPointMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPointMenuItemActionPerformed(evt);
            }
        });
        dataMenu.add(addPointMenuItem);

        setDEMMenuItem.setText(bundle.getString("MainFrame.setDEMMenuItem.text")); // NOI18N
        setDEMMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDEMMenuItemActionPerformed(evt);
            }
        });
        dataMenu.add(setDEMMenuItem);

        jMenuBar.add(dataMenu);

        indiceMenu.setText(bundle.getString("MainFrame.indiceMenu.text")); // NOI18N

        calcIndiceMenuItem.setText(bundle.getString("MainFrame.calcIndiceMenuItem.text")); // NOI18N
        calcIndiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcIndiceMenuItemActionPerformed(evt);
            }
        });
        indiceMenu.add(calcIndiceMenuItem);

        compIndiceMenuItem.setText(bundle.getString("MainFrame.compIndiceMenuItem.text")); // NOI18N
        compIndiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compIndiceMenuItemActionPerformed(evt);
            }
        });
        indiceMenu.add(compIndiceMenuItem);

        localIndiceMenuItem.setText(bundle.getString("MainFrame.localIndiceMenuItem.text")); // NOI18N
        localIndiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localIndiceMenuItemActionPerformed(evt);
            }
        });
        indiceMenu.add(localIndiceMenuItem);

        delatIndiceMenuItem.setText(bundle.getString("MainFrame.delatIndiceMenuItem.text")); // NOI18N
        delatIndiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deltaIndiceMenuItemActionPerformed(evt);
            }
        });
        indiceMenu.add(delatIndiceMenuItem);
        indiceMenu.add(jSeparator3);

        batchGraphIndiceMenuItem.setText(bundle.getString("MainFrame.batchGraphIndiceMenuItem.text")); // NOI18N
        batchGraphIndiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchGraphIndiceMenuItemActionPerformed(evt);
            }
        });
        indiceMenu.add(batchGraphIndiceMenuItem);

        batchParamIndiceMenu.setText(bundle.getString("MainFrame.batchParamIndiceMenu.text")); // NOI18N

        batchParamLocalMenuItem.setText(bundle.getString("MainFrame.batchParamLocalMenuItem.text")); // NOI18N
        batchParamLocalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchParamLocalMenuItemActionPerformed(evt);
            }
        });
        batchParamIndiceMenu.add(batchParamLocalMenuItem);

        batchParamGlobalMenuItem.setText(bundle.getString("MainFrame.batchParamGlobalMenuItem.text")); // NOI18N
        batchParamGlobalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchParamGlobalMenuItemActionPerformed(evt);
            }
        });
        batchParamIndiceMenu.add(batchParamGlobalMenuItem);

        indiceMenu.add(batchParamIndiceMenu);

        jMenuBar.add(indiceMenu);

        analysisMenu.setText(bundle.getString("MainFrame.analysisMenu.text")); // NOI18N

        estimMenuItem.setText(bundle.getString("MainFrame.estimMenuItem.text")); // NOI18N
        estimMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                estimMenuItemActionPerformed(evt);
            }
        });
        analysisMenu.add(estimMenuItem);

        interpMetricMenuItem.setText(bundle.getString("MainFrame.interpMetricMenuItem.text")); // NOI18N
        interpMetricMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpMetricMenuItemActionPerformed(evt);
            }
        });
        analysisMenu.add(interpMetricMenuItem);

        addPatchMenuItem.setText(bundle.getString("MainFrame.addPatchMenuItem.text")); // NOI18N
        addPatchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPatchMenuItemActionPerformed(evt);
            }
        });
        analysisMenu.add(addPatchMenuItem);

        jMenuBar.add(analysisMenu);

        helpMenu.setText(bundle.getString("MainFrame.helpMenu.text")); // NOI18N

        aboutMenuItem.setText(bundle.getString("MainFrame.aboutMenuItem.text")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar.add(helpMenu);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(164, 164, 164)
                    .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(342, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectMenuItemActionPerformed
        final NewProjectDialog dlg = new NewProjectDialog(this);
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                   project = dlg.createProject();

                   mapViewer.setRootLayer(project.getRootLayer());
                   mapViewer.setTreeLayerVisible(true);
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Error") + ex.getLocalizedMessage());
                }
            }

        }).start();
        

    }//GEN-LAST:event_newProjectMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        final File f = Util.getFile(".xml", java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Project_file"));
        if (f == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadProject(f);
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Error_while_loading_project") + ex.getLocalizedMessage());
                }
            }
        }).start();
        

    }//GEN-LAST:event_openMenuItemActionPerformed

    private void costDistMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_costDistMenuItemActionPerformed
        final LinksetPanel panel = new LinksetPanel();
        panel.setProject(project);
        panel.setCodes(project.getCodes(), project.getLastCosts());

        int res = JOptionPane.showConfirmDialog(this, panel, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Cost_distances"), 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(res != JOptionPane.OK_OPTION) {
            return;
        }

        if(project.getLinksetNames().contains(panel.getLinksetName())) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("The_name_already_exists") + panel.getLinksetName());
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    project.addLinkset(panel.getLinkset(), true);
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
                }
            }
        }).start();
        
    }//GEN-LAST:event_costDistMenuItemActionPerformed

    private void createGraphMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createGraphMenuItemActionPerformed
       final NewGraphDialog dlg = new NewGraphDialog(this, project.getLinksets());
       dlg.setVisible(true);
       if(!dlg.isOk) {
           return;
       }
       if(project.getGraphNames().contains(dlg.name)) {
           JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("The_name_already_exists") + dlg.name);
           return;
       }

       new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    project.addGraph(new GraphGenerator(dlg.name, dlg.linkset, dlg.type, dlg.threshold, dlg.intraPatchDist), true);
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
                }
            }
        }).start();
       


    }//GEN-LAST:event_createGraphMenuItemActionPerformed

    private void addPointDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPointDataMenuItemActionPerformed
        final PointImportDialog dlg = new PointImportDialog(this);
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }

        final List<DefaultFeature> features;
        try {
            if(dlg.shpFile) {
                features = GlobalDataStore.getFeatures(dlg.file, dlg.idAttr, null);
            } else {
                features = DefaultFeature.loadFeatures(dlg.file, dlg.xAttr, dlg.yAttr, dlg.idAttr);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured_while_loading_data_!") + ex.getLocalizedMessage());
            return;
        }

        final PointsetDataDialog ddlg = new PointsetDataDialog(this, project.getLinksets());
        ddlg.setVisible(true);
        if(!ddlg.isOk) {
            return;
        }
        if(project.getPointsetNames().contains(ddlg.pointset.getName())) {
           JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("The_name_already_exists") + ddlg.pointset.getName());
           return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    project.addPointset(ddlg.pointset, dlg.zAttrs, features, true);
                } catch (SchemaException | IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
                }
            }
        }).start();
        
        
    }//GEN-LAST:event_addPointDataMenuItemActionPerformed

    private void calcIndiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcIndiceMenuItemActionPerformed
        final CalcMetricDialog<GlobalMetric> dlg = new CalcMetricDialog(this, project, project.getGraphs(), Project.getGlobalMetricsFor(Method.GLOBAL));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                GlobalMetricLauncher launcher = new GlobalMetricLauncher(dlg.metric);
                TaskMonitor monitor = new TaskMonitor(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_metrics..."), "", 0, 100);
                Double[]val = launcher.calcMetric(dlg.graph, true, monitor);
                String res = dlg.metric.getDetailName() + " : " + Arrays.deepToString(val) + "\n";

                monitor.close();
                System.out.println(res);
                System.out.println("Temps écoulé : " + (System.currentTimeMillis()-start));
                JOptionPane.showMessageDialog(MainFrame.this, new JScrollPane(new JTextArea(res, 10, 40)));
            }
        }).start();
    }//GEN-LAST:event_calcIndiceMenuItemActionPerformed

    private void batchGraphIndiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchGraphIndiceMenuItemActionPerformed
        final BatchGraphMetricDialog dlg = new BatchGraphMetricDialog(this, project);
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        if(dlg.min + dlg.inc > dlg.max) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Les_valeurs_de_seuils_sont_erronées."));
           return;
        }
        
        final GlobalMetricLauncher launcher = new GlobalMetricLauncher(dlg.metric);

        new Thread(new Runnable() {
            @Override
            public void run() {
                TaskMonitor monitor = new TaskMonitor(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_metrics..."), "",
                    0, 100);
                GlobalMetric indice = dlg.metric;

                BatchGraphMetricTask task = new BatchGraphMetricTask(monitor, dlg.linkset, dlg.distAbs, 
                    dlg.min, dlg.inc, dlg.max, launcher, dlg.intraPatchDist);

                new ParallelFExecutor(task).executeAndWait();

                if(task.isCanceled()) {
                    return;
                }
                SortedMap<Double, Double[]> results = task.getResult();
                XYSeriesCollection series = new XYSeriesCollection();
                for(int j = 0; j < indice.getResultNames().length; j++) {
                    XYSeries serie = new XYSeries(indice.getName());
                    for(Double x : results.keySet()) {
                        serie.add(x, results.get(x)[j]);
                    }
                    series.addSeries(serie);
                }
                SerieFrame frm = new SerieFrame(indice.getDetailName(),
                        series, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Threshold"), "");
                frm.pack();
                frm.setLocationRelativeTo(MainFrame.this);
                frm.setVisible(true);

                monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));
        
                monitor.close();

            }
        }).start();
}//GEN-LAST:event_batchGraphIndiceMenuItemActionPerformed

    private void deltaIndiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deltaIndiceMenuItemActionPerformed
        final CalcMetricDialog<GlobalMetric> dlg = new CalcMetricDialog<>(this, project, project.getGraphs(), Project.getGlobalMetricsFor(Method.DELTA));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        JPanel panel = new JPanel();
        JCheckBox checkNode = new JCheckBox(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Nodes"), true); panel.add(checkNode);
        JCheckBox checkEdge = new JCheckBox(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Edges")); panel.add(checkEdge);
        int res = JOptionPane.showConfirmDialog(this, panel, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Delta_metrics"), JOptionPane.OK_CANCEL_OPTION);
        if(res != JOptionPane.OK_OPTION) {
            return;
        }

        final int nodeEdge = (checkNode.isSelected() ? 1 : 0) + (checkEdge.isSelected() ? 2 : 0);

        final GlobalMetricLauncher launcher = new GlobalMetricLauncher(dlg.metric);

        new Thread(new Runnable() {
            @Override
            public void run() {
                GlobalMetric indice = dlg.metric;
                TaskMonitor monitor = new TaskMonitor(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_delta_metrics..."), "", 0,
                        100);
                try {
                    DeltaMetricTask task = new DeltaMetricTask(monitor, dlg.graph, launcher, nodeEdge);

                    ExecutorService.execute(task);

                    if(task.isCanceled()) {
                        return;
                    }
                    for(int i = 0; i < indice.getResultNames().length; i++) {
                        if((nodeEdge & 2) == 2) {
                            DefaultFeature.addAttribute("d_" + indice.getDetailName(i) + "_" + dlg.graph.getName(),
                                dlg.graph.getLinkset().getPaths(), Double.NaN);
                        }
                        if((nodeEdge & 1) == 1) {
                            DefaultFeature.addAttribute("d_" + indice.getDetailName(i) + "_" + dlg.graph.getName(),
                                project.getPatches(), Double.NaN);
                        }
                    }

                    Map<Object, Double[]> result = task.getResult();

                    if((nodeEdge & 1) == 1) {
                        for(DefaultFeature f : project.getPatches()) {
                            if(result.keySet().contains(f.getId())) {
                                Double[] res = result.get(f.getId());
                                for(int j = 0; j < indice.getResultNames().length; j++) {
                                    f.setAttribute("d_" + indice.getDetailName(j) + "_" + dlg.graph.getName(),
                                        res[j]);
                                }
                            }
                        }
                    }

                    if((nodeEdge & 2) == 2) {
                        for(DefaultFeature f : dlg.graph.getLinkset().getPaths()) {
                            if(result.keySet().contains(f.getId())) {
                                Double[] res = result.get(f.getId());
                                for(int j = 0; j < indice.getResultNames().length; j++) {
                                    f.setAttribute("d_" + indice.getDetailName(j) + "_" + dlg.graph.getName(),
                                        res[j]);
                                }
                            }
                        }
                    }
                    monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));
                
                    if((nodeEdge & 2) == 2) {
                        dlg.graph.getLinkset().saveLinks();
                    }
                    if((nodeEdge & 1) == 1) {
                        project.savePatch();
                    }
                    // show the result
                    viewMetricResult(dlg.graph, "d_" + indice.getDetailName(0) + "_" + dlg.graph.getName(), 
                            (nodeEdge & 1) == 1, (nodeEdge & 2) == 2);
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
                }
                monitor.close();
            }
        }).start();
    }//GEN-LAST:event_deltaIndiceMenuItemActionPerformed

    private void localIndiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localIndiceMenuItemActionPerformed
        final CalcMetricDialog<LocalMetric> dlg = new CalcMetricDialog<>(this, project, project.getGraphs(), Project.getLocalMetrics());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_local_metrics..."));
                try {
                    long start = System.currentTimeMillis();
                    calcLocalMetric(monitor, dlg.graph, dlg.metric, Double.NaN);
                    Logger.getLogger(MainFrame.class.getName()).info(dlg.metric + " - Elapsed time : " + (System.currentTimeMillis()-start) + "ms");

                    monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));
                    if(dlg.metric.calcEdges()) {
                        dlg.graph.getLinkset().saveLinks();
                    }
                    if(dlg.metric.calcNodes()) {
                        project.savePatch();
                    }

                    // show the result
                    viewMetricResult(dlg.graph, dlg.metric.getDetailName(0) + "_" + dlg.graph.getName(), dlg.metric.calcNodes(), dlg.metric.calcEdges());

                    monitor.close();
                } catch(IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
                } finally {
                    monitor.close();
                }
            }
        }).start();
    }//GEN-LAST:event_localIndiceMenuItemActionPerformed

    private void compIndiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compIndiceMenuItemActionPerformed
       
        final CalcMetricDialog<GlobalMetric> dlg = new CalcMetricDialog<>(this, project, project.getGraphs(), Project.getGlobalMetricsFor(Method.COMP));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_component_metric..."));
                try {
                    List<String> attrs = calcCompMetric(monitor, dlg.graph, dlg.metric, Double.NaN);

                    monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));

                    project.saveGraphVoronoi(dlg.graph.getName());

                    project.getRootLayer().setLayersVisible(false);
                    project.getGraphLayers().setExpanded(true);
                    GraphGroupLayer gl = dlg.graph.getLayers();
                    gl.setExpanded(true);
                    FeatureLayer compLayer = (FeatureLayer) gl.getLayers().get(0);
                    compLayer.setStyle(new FeatureStyle(attrs.get(0), new ColorRamp(ColorRamp.RAMP_SYM_GREEN_RED, 
                            new FeatureAttributeIterator<Number>(dlg.graph.getComponentFeatures(), attrs.get(0)))));
                    compLayer.setVisible(true);

                } catch(IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
                } finally {
                    monitor.close();
                }
            }
        }).start();
    }//GEN-LAST:event_compIndiceMenuItemActionPerformed

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void prefMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefMenuItemActionPerformed
        PreferencesDialog dlg = new PreferencesDialog(this, true);
        dlg.setProcPanelVisible(true);
        dlg.setVisible(true);
    }//GEN-LAST:event_prefMenuItemActionPerformed

    private void addPointMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPointMenuItemActionPerformed
        RandomPointDlg dlg = new RandomPointDlg(this, project);
        dlg.setVisible(true);

    }//GEN-LAST:event_addPointMenuItemActionPerformed

    private void estimMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_estimMenuItemActionPerformed
        ModelDialog dlg = new ModelDialog(this, project);
        dlg.setVisible(true);
    }//GEN-LAST:event_estimMenuItemActionPerformed

    private void batchParamLocalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchParamLocalMenuItemActionPerformed
        List<LocalMetric> metrics = new ArrayList<>();
        for(LocalMetric ind : Project.getLocalMetrics()) {
            if(ind.hasParams()) {
                metrics.add(ind);
            }
        }
        final BatchParamMetricDialog<LocalMetric> dlg = new BatchParamMetricDialog<>
                (this, project, project.getGraphs(), metrics);
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
         new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                int n = (int)((dlg.max - dlg.min) / dlg.inc) + 1;
                LocalMetric indice = (LocalMetric)dlg.metric.dupplicate();
                ProgressBar monitor = Config.getProgressBar(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_local_metric_") + dlg.metric.getName(), n*100);
                        
               Map<String, Object> params = indice.getParams();
               for(double p = dlg.min; p <= dlg.max; p += dlg.inc) {
                   params.put(dlg.param, p);
                   indice.setParams(params);
                   monitor.setNote(dlg.param + " : " + String.format("%g", p));
                   calcLocalMetric(monitor.getSubProgress(100), dlg.graph,
                            indice, Double.NaN);

               }

                monitor.setNote(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Saving..."));

                dlg.graph.getLinkset().saveLinks();
                project.savePatch();

                monitor.close();
            } catch(IOException | SchemaException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("An_error_occured") + ex.getLocalizedMessage());
            }
            }
        }).start();
    }//GEN-LAST:event_batchParamLocalMenuItemActionPerformed

    private void batchParamGlobalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchParamGlobalMenuItemActionPerformed
        List<GlobalMetric> metrics = new ArrayList<>();
        for(GlobalMetric ind : Project.getGlobalMetricsFor(Method.GLOBAL)) {
            if(ind.hasParams()) {
                metrics.add(ind);
            }
        }
        final BatchParamMetricDialog<GlobalMetric> dlg = new BatchParamMetricDialog<>
                (this, project, project.getGraphs(), metrics);
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
               int n = (int)((dlg.max - dlg.min) / dlg.inc) + 1;
               
               TaskMonitor monitor = new TaskMonitor(MainFrame.this, java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Calc_global_metric_") + dlg.metric.getName(), "", 0,
                        n*100);
               monitor.popupNow();
               
               final List<Double> steps = new ArrayList<>();
               for(double p = dlg.min; p <= dlg.max; p += dlg.inc) {
                   steps.add(p);
                }

               AbstractParallelFTask<TreeMap<Double, Double[]>, TreeMap<Double, Double[]>> task =
                       new AbstractParallelFTask<TreeMap<Double, Double[]>, TreeMap<Double, Double[]>>(monitor.getSubMonitor(0, 100, 100)) {
                        TreeMap<Double, Double[]> results;
                        @Override
                        protected TreeMap<Double, Double[]> execute(int start, int end) {
                            if(isCanceled()) {
                                return null;
                            }
                            GlobalMetric indice = (GlobalMetric)dlg.metric.dupplicate();
                            TreeMap<Double, Double[]> result = new TreeMap<>();
                            Map<String, Object> params = indice.getParams();
                            for(int i = start; i < end; i++) {
                                double p = steps.get(i);
                                params.put(dlg.param, p);
                                indice.setParams(params);
                                monitor.setNote(dlg.param + " : " + String.format("%g", p));
                                GlobalMetricLauncher launcher = new GlobalMetricLauncher(indice);
                                Double [] res = launcher.calcMetric(dlg.graph, false,
                                        ((TaskMonitor)monitor).getSubMonitor(0, 100, 100));
                                result.put(p, res);
                            }
                            return result;
                        }
                        @Override
                        public int getSplitRange() {
                            return steps.size();
                        }
                        @Override
                        public void finish(Collection<TreeMap<Double, Double[]>> col) {
                            results = new TreeMap<>();
                            for(Map<Double, Double[]> o : col) {
                                results.putAll(o);
                            }
                        }
                        @Override
                        public TreeMap<Double, Double[]> getResult() {
                            return results;
                        }
                    };

                new ParallelFExecutor(task).executeAndWait();

                if(task.isCanceled()) {
                    return;
                }
                TreeMap<Double, Double[]> results = task.getResult();
               
                monitor.close();
                XYSeriesCollection series = new XYSeriesCollection();
                for(int j = 0; j < ((GlobalMetric)dlg.metric).getResultNames().length; j++) {
                    XYSeries serie = new XYSeries(((GlobalMetric)dlg.metric).getResultNames()[j]);
                    for(Double x : results.keySet()) {
                        serie.add(x, results.get(x)[j]);
                    }
                    series.addSeries(serie);
                }
                SerieFrame frm = new SerieFrame(dlg.metric.getName() ,
                        series, dlg.param, "");
                frm.pack();
                frm.setLocationRelativeTo(MainFrame.this);
                frm.setVisible(true);
            }
        }).start();
    }//GEN-LAST:event_batchParamGlobalMenuItemActionPerformed

    private void calcCapaMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcCapaMenuItemActionPerformed
        final CapaPatchDialog dlg = new CapaPatchDialog(this, project, project.getCapacityParams());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    project.setCapacities(dlg.params, true);
                    JOptionPane.showMessageDialog(MainFrame.this, "Capacity saved.");
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, ex);
                }
            }
        }).start();
        
    }//GEN-LAST:event_calcCapaMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        String text = "<html>\n" +
                "<h1>Graphab 2</h1>\n" +
                "<p>\n" +
                "Laboratoire ThéMA - UMR 6049<br/>\n" +
                "CNRS - Université de Franche Comté<br/>\n" +
                "</p>\n" +
                "<br/>\n" +
                "<p>\n" +
                "J. C. Foltête, G. Vuidel, C. Clauzel, X. Girardet<br>\n" +
                "</p>\n" +
                "<br/>\n" +
                "<a href=\"https://sourcesup.renater.fr/graphab\">https://sourcesup.renater.fr/graphab</a>\n" +
                "</html>";
        JEditorPane pane = new JEditorPane("text/html", text);
        pane.setBackground(new Color(0, 0, 0, 0));
        pane.setEditable(false);
        JOptionPane.showMessageDialog(this, pane, 
            "Graphab - " + getVersion(), JOptionPane.PLAIN_MESSAGE, new ImageIcon(getIconImage()));
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void logMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logMenuItemActionPerformed
        logFrame.setVisible(true);
    }//GEN-LAST:event_logMenuItemActionPerformed

    private void interpMetricMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpMetricMenuItemActionPerformed
        new MetricInterpolDlg(this, project, true).setVisible(true);
    }//GEN-LAST:event_interpMetricMenuItemActionPerformed

    private void metaPatchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_metaPatchMenuItemActionPerformed
        final MetaPatchDialog dlg = new MetaPatchDialog(this, project.getGraphs());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        final File prjDir = new File(project.getDirectory(), dlg.prjName);
        if(prjDir.exists()) {
            JOptionPane.showMessageDialog(this, "The project name already exists", "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File prjFile = project.createMetaPatchProject(dlg.prjName, dlg.graph, dlg.alpha, dlg.minCapa);
                    loadProject(prjFile);
                    ((DefaultGroupLayer)mapViewer.getLayers()).addLayerLast(new FeatureLayer("Patch voronoi", project.getVoronoi(), new FeatureStyle(null, Color.BLACK)));
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, "Error " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } 
            }
        }).start();
          
    }//GEN-LAST:event_metaPatchMenuItemActionPerformed

    private void setDEMMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDEMMenuItemActionPerformed
        File f = Util.getFile(".tif|.asc", "DEM raster");
        if(f == null) {
            return;
        }
        try {
            project.setDemFile(f, true);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(MainFrame.this, "Error " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_setDEMMenuItemActionPerformed

    private void projectRemPatchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectRemPatchMenuItemActionPerformed
        String res = JOptionPane.showInputDialog("Create a new project while removing patches with a capacity less than :", 0);
        if(res == null) {
            return;
        }
        final double minCapa = Double.parseDouble(res.trim());
        final String name = project.getName() + "-" + res.trim();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File prjFile = project.createProject(name, minCapa);
                    loadProject(prjFile);
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(MainFrame.this, "Error " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } 
            }
        }).start();
       
        
    }//GEN-LAST:event_projectRemPatchMenuItemActionPerformed

    private void addPatchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPatchMenuItemActionPerformed
        new AddPatchDialog(this, project).setVisible(true);
    }//GEN-LAST:event_addPatchMenuItemActionPerformed

    /**
     * Changes the current view, to show the result of a local or delta metric.
     * 
     * @param graph the graph on which the metric has been calculated
     * @param attr the name of the attribute containing the metric values
     * @param node show the results on nodes ?
     * @param edge show the results on edge ?
     */
    public void viewMetricResult(GraphGenerator graph, String attr, boolean node, boolean edge) {
        // show the result
        project.getRootLayer().setLayersVisible(false);
        project.getGraphLayers().setExpanded(true);
        GraphGroupLayer gl = graph.getLayers();
        gl.setExpanded(true);
        gl.setSpatialView(false);
        FeatureLayer nodeLayer = gl.getNodeLayer();
        CircleStyle nodeStyle = (CircleStyle) nodeLayer.getStyle();
        if(node) {
            Number max = Collections.max(new FeatureAttributeCollection<Double>(nodeLayer.getFeatures(), attr));
            Number min = Collections.min(new FeatureAttributeCollection<Double>(nodeLayer.getFeatures(), attr));
            nodeStyle.setCircleAttr(attr, min.doubleValue(), max.doubleValue());
            nodeStyle.setAttrFill(attr);
            nodeStyle.setRampFill(new ColorRamp(ColorRamp.RAMP_SYM_GREEN_RED, 
                    new FeatureAttributeIterator<Number>(nodeLayer.getFeatures(), attr)));
        } else {
            nodeStyle.setCircleAttr(null, 0, 0);
//            nodeStyle.setMinRadius(3);
//            nodeStyle.setMaxRadius(3);
            nodeStyle.setAttrFill(null);
            nodeStyle.setRampFill(new ColorRamp(new Color[]{new Color(0xcbcba7)}));
        }
        nodeLayer.setVisible(true);
        FeatureLayer edgeLayer = gl.getEdgeLayer();
        FeatureStyle style = (FeatureStyle) edgeLayer.getStyle();
        style.setAttrContour(null);
        if(edge) {
            style.setAttrStroke(attr);
            style.setRampStroke(new StrokeRamp(0.5f, 4, new FeatureAttributeIterator<Number>(edgeLayer.getFeatures(), attr)));
        } else {
            style.setAttrStroke(null);
        }
        edgeLayer.setVisible(true);
    }
    
    /**
     * Loads a project and shows it on this frame.
     * 
     * @param prjFile the xml project file
     * @throws IOException 
     */
    public void loadProject(File prjFile) throws IOException {
        project = Project.loadProject(prjFile, true);
        ProgressBar progressBar = Config.getProgressBar("Create layers...");
        progressBar.setIndeterminate(true);
        mapViewer.setRootLayer(project.getRootLayer());
        mapViewer.setTreeLayerVisible(true);
        progressBar.close();
    }
    
    /**
     * Calculates a global metric on each component of a graph.
     * @param monitor the progress monitor
     * @param graph the graph
     * @param metric the global metric
     * @param maxCost max distance for path metric
     * @return list of created attributes
     */
    public static List<String> calcCompMetric(ProgressBar monitor, GraphGenerator graph,
                GlobalMetric metric, double maxCost) {
        GlobalMetricLauncher launcher = new GlobalMetricLauncher(metric);
        String [] resNames = metric.getResultNames();
        List<String> attrNames = new ArrayList<>();
        for(String name : resNames) {
            String attr = metric.getDetailName() + (resNames.length > 1 ? "|" + name : "") + "_" + graph.getName();
            DefaultFeature.addAttribute(attr, graph.getComponentFeatures(), Double.NaN);
            attrNames.add(attr);
        }

        monitor.setMaximum(graph.getComponents().size());

        for(int i = 0; i < graph.getComponents().size(); i++) {
            Double [] res = launcher.calcMetric(graph.getComponentGraphGen(i), true, null);
            DefaultFeature f = graph.getComponentFeatures().get(i);
            for(int j = 0; j < resNames.length; j++) {
                f.setAttribute(metric.getDetailName() + (resNames.length > 1 ? "|" + resNames[j] : "") + "_" + graph.getName(), res[j]);
            }
            monitor.incProgress(1);
        }

        return attrNames;
    }
    
    /**
     * Calculates a local metric on a graph patches and links.
     * @param monitor the progress monitor
     * @param graph the graph
     * @param refMetric the local metric
     * @param maxCost the maximum distance for PathMetric
     */
    public static void calcLocalMetric(ProgressBar monitor, final GraphGenerator graph,
                LocalMetric refMetric, double maxCost) {
        final LocalMetric metric = (LocalMetric)refMetric.dupplicate();           
        
        if(metric instanceof PreCalcMetric) {
            PreCalcMetricTask pathTask = new PreCalcMetricTask(graph, (PreCalcMetric)metric, maxCost, monitor.getSubProgress(99));
            ExecutorService.execute(pathTask);
            monitor = monitor.getSubProgress(1);
        }

        monitor.setMaximum((metric.calcNodes() ? graph.getGraph().getNodes().size() : 0) +
                (metric.calcEdges() ? graph.getGraph().getEdges().size() : 0));

        if(metric.calcNodes()) {
            for(int i = 0; i < metric.getResultNames().length; i++) {
                DefaultFeature.addAttribute(metric.getDetailName(i) + "_" + graph.getName(),
                    graph.getProject().getPatches(), Double.NaN);
            }
            List<Node> nodes = new ArrayList(graph.getGraph().getNodes());
            SimpleParallelTask<Node> task = new SimpleParallelTask<Node>(nodes, monitor.getSubProgress(nodes.size())) {
                @Override
                protected void executeOne(Node node) {
                    WritableFeature f = (WritableFeature)node.getObject();
                    Double[] val = metric.calcMetric(node, graph);
                    for(int i = 0; i < metric.getResultNames().length; i++) {
                        f.setAttribute(metric.getDetailName(i) + "_" + graph.getName(), val[i]);                              
                    }
                }
            };

            try {
                new ParallelFExecutor(task).executeAndWait();
            } catch(Exception ex) {
                for(int i = 0; i < metric.getResultNames().length; i++) {
                    DefaultFeature.removeAttribute(metric.getDetailName(i) + "_" + graph.getName(),
                        graph.getProject().getPatches());          
                }
                throw new RuntimeException(ex);
            }
        }

        if(metric.calcEdges()) {
            for(int i = 0; i < metric.getResultNames().length; i++) {
                DefaultFeature.addAttribute(metric.getDetailName(i) + "_" + graph.getName(),
                    graph.getLinkset().getPaths(), Double.NaN);        
            }
            
            List<Edge> edges = new ArrayList(graph.getGraph().getEdges());
            SimpleParallelTask<Edge> task = new SimpleParallelTask<Edge>(edges, monitor.getSubProgress(edges.size())) {
                @Override
                protected void executeOne(Edge edge) {
                    WritableFeature f = (WritableFeature)edge.getObject();
                    Double[] val = metric.calcMetric(edge, graph);
                    for(int i = 0; i < metric.getResultNames().length; i++) {
                        f.setAttribute(metric.getDetailName(i) + "_" + graph.getName(), val[i]);                              
                    }
                }
                    
            };

            try {
                new ParallelFExecutor(task).executeAndWait();
            } catch(Exception ex) {
                for(int i = 0; i < metric.getResultNames().length; i++) {
                    DefaultFeature.removeAttribute(metric.getDetailName(i) + "_" + graph.getName(),
                        graph.getLinkset().getPaths());
                }
                throw new RuntimeException(ex);
            }
        }

    }
    
    /**
     * @return the version stored in the manifest
     */
    public static String getVersion() {
        String version = MainFrame.class.getPackage().getImplementationVersion();
        return version == null ? "unpackage version" : version;
    }
    
    /**
     * Main entry point for MPI, CLI or GUI.
     * 
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        try {
            Project.loadPluginMetric();
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.WARNING, null, ex);
        }
        
        // MPI Execution
        if(args.length > 0 && args[0].equals("-mpi")) {
            new MpiLauncher(Arrays.copyOfRange(args, 1, args.length)).run();
            return;
        }
        
        // CLI execution
        if(args.length > 0 && !args[0].equals(JavaLoader.NOFORK)) {
            if(!GraphicsEnvironment.isHeadless() && SplashScreen.getSplashScreen() != null) {
                SplashScreen.getSplashScreen().close();
            }
            new CLITools().execute(args);
            
            return;
        }
        
        Config.setNodeClass(MainFrame.class);
        
        // Relaunch java with preferences memory
        try {
            if(args.length == 0) {
                if(JavaLoader.forkJava(MainFrame.class, 2048)) {
                    return;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Default execution (UI)
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if(e instanceof CancellationException) {
                    JOptionPane.showMessageDialog(null, "Execution has been cancelled");
                } else {
                    Logger.getLogger("").log(Level.SEVERE, null, e);
                    JOptionPane.showMessageDialog(null, "An error has occurred : " + e);
                }
            }
        });
        PreferencesDialog.initLanguage();

        try {  // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.WARNING, null, e);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem addPatchMenuItem;
    private javax.swing.JMenuItem addPointDataMenuItem;
    private javax.swing.JMenuItem addPointMenuItem;
    private javax.swing.JMenu analysisMenu;
    private javax.swing.JMenuItem batchGraphIndiceMenuItem;
    private javax.swing.JMenuItem batchParamGlobalMenuItem;
    private javax.swing.JMenu batchParamIndiceMenu;
    private javax.swing.JMenuItem batchParamLocalMenuItem;
    private javax.swing.JMenuItem calcCapaMenuItem;
    private javax.swing.JMenuItem calcIndiceMenuItem;
    private javax.swing.JMenuItem compIndiceMenuItem;
    private javax.swing.JMenuItem costDistMenuItem;
    private javax.swing.JMenuItem createMenuItem;
    private javax.swing.JMenu dataMenu;
    private javax.swing.JMenuItem delatIndiceMenuItem;
    private javax.swing.JMenuItem estimMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu graphMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu indiceMenu;
    private javax.swing.JMenuItem interpMetricMenuItem;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JMenuItem localIndiceMenuItem;
    private javax.swing.JMenuItem logMenuItem;
    private org.thema.drawshape.ui.MapViewer mapViewer;
    private javax.swing.JMenuItem metaPatchMenuItem;
    private javax.swing.JMenuItem newProjectMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem prefMenuItem;
    private javax.swing.JMenuItem projectRemPatchMenuItem;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem setDEMMenuItem;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

}
