/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewProjectDialog.java
 *
 * Created on 4 mai 2010, 09:26:29
 */

package org.thema.graphab;

import org.thema.graphab.util.RSTGridReader;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.SchemaException;
import org.thema.common.Config;
import org.thema.data.IOImage;

/**
 *
 * @author gvuidel
 */
public class NewProjectDialog extends javax.swing.JDialog {

    private static final int NBPANEL = 3;
    int indPanel;

    public boolean isOk = false;

    TreeSet<Integer> codes;
    File imgFile;
    GridCoverage2D coverage;


    /** Creates new form NewProjectDialog */
    public NewProjectDialog(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        
        setPanel(0);

        prjNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update();  }
            public void insertUpdate(DocumentEvent e) { update();  }
            private void update() {
                String rep = prjPathTextField.getText();
                rep = rep.substring(0, rep.lastIndexOf(File.separator)+1);
                prjPathTextField.setText(rep + prjNameTextField.getText());
            }
            });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connexButtonGroup = new javax.swing.ButtonGroup();
        globalPanel = new javax.swing.JPanel();
        panel1 = new javax.swing.JPanel();
        prjNameLabel = new javax.swing.JLabel();
        prjNameTextField = new javax.swing.JTextField();
        prjPathLabel = new javax.swing.JLabel();
        prjPathTextField = new javax.swing.JTextField();
        selectPathButton = new javax.swing.JButton();
        panel2 = new javax.swing.JPanel();
        imgSelectFilePanel = new org.thema.common.swing.SelectFilePanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        minAreaSpinner = new javax.swing.JSpinner();
        connexPanel = new javax.swing.JPanel();
        con4RadioButton = new javax.swing.JRadioButton();
        con8RadioButton = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        codeComboBox = new javax.swing.JComboBox();
        noDataComboBox = new javax.swing.JComboBox();
        simplifyCheckBox = new javax.swing.JCheckBox();
        panel3 = new org.thema.graphab.links.LinksetPanel();
        cancelButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        endButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle"); // NOI18N
        setTitle(bundle.getString("NewProjectDialog.title")); // NOI18N

        globalPanel.setLayout(new java.awt.CardLayout());

        prjNameLabel.setText(bundle.getString("NewProjectDialog.prjNameLabel.text")); // NOI18N

        prjNameTextField.setText(bundle.getString("NewProjectDialog.prjNameTextField.text")); // NOI18N

        prjPathLabel.setText(bundle.getString("NewProjectDialog.prjPathLabel.text")); // NOI18N

        selectPathButton.setText("..."); // NOI18N
        selectPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPathButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prjNameLabel)
                    .addComponent(prjPathLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(prjPathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectPathButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(prjNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prjNameLabel)
                    .addComponent(prjNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prjPathLabel)
                    .addComponent(prjPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectPathButton))
                .addContainerGap(441, Short.MAX_VALUE))
        );

        globalPanel.add(panel1, "panel1");

        imgSelectFilePanel.setDescription(bundle.getString("NewProjectDialog.imgSelectFilePanel.description")); // NOI18N
        imgSelectFilePanel.setFileDesc(bundle.getString("NewProjectDialog.imgSelectFilePanel.fileDesc_1")); // NOI18N
        imgSelectFilePanel.setFileExts(bundle.getString("NewProjectDialog.imgSelectFilePanel.fileExts_1")); // NOI18N
        imgSelectFilePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgSelectFilePanelActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle.getString("NewProjectDialog.jLabel1.text_1")); // NOI18N

        jLabel3.setText(bundle.getString("NewProjectDialog.jLabel3.text_1")); // NOI18N

        jLabel2.setText(bundle.getString("NewProjectDialog.jLabel2.text_1")); // NOI18N

        minAreaSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        connexPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("NewProjectDialog.connexPanel.border.title_1"))); // NOI18N

        connexButtonGroup.add(con4RadioButton);
        con4RadioButton.setSelected(true);
        con4RadioButton.setText(bundle.getString("NewProjectDialog.con4RadioButton.text_1")); // NOI18N

        connexButtonGroup.add(con8RadioButton);
        con8RadioButton.setText(bundle.getString("NewProjectDialog.con8RadioButton.text_1")); // NOI18N

        javax.swing.GroupLayout connexPanelLayout = new javax.swing.GroupLayout(connexPanel);
        connexPanel.setLayout(connexPanelLayout);
        connexPanelLayout.setHorizontalGroup(
            connexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connexPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(con4RadioButton)
                    .addComponent(con8RadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        connexPanelLayout.setVerticalGroup(
            connexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connexPanelLayout.createSequentialGroup()
                .addComponent(con4RadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(con8RadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText(bundle.getString("NewProjectDialog.jLabel4.text")); // NOI18N

        simplifyCheckBox.setSelected(true);
        simplifyCheckBox.setText(bundle.getString("NewProjectDialog.simplifyCheckBox.text")); // NOI18N

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addComponent(imgSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addComponent(simplifyCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
                        .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(connexPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panel2Layout.createSequentialGroup()
                                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(minAreaSpinner)
                                    .addComponent(codeComboBox, 0, 1, Short.MAX_VALUE)
                                    .addGroup(panel2Layout.createSequentialGroup()
                                        .addComponent(noDataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(186, 186, 186))))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imgSelectFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(noDataComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(codeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(minAreaSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addComponent(connexPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(simplifyCheckBox)
                .addContainerGap(211, Short.MAX_VALUE))
        );

        globalPanel.add(panel2, "panel2");
        globalPanel.add(panel3, "panel3");

        cancelButton.setText(bundle.getString("NewProjectDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        prevButton.setText(bundle.getString("NewProjectDialog.prevButton.text")); // NOI18N
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        nextButton.setText(bundle.getString("NewProjectDialog.nextButton.text")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        endButton.setText(bundle.getString("NewProjectDialog.endButton.text")); // NOI18N
        endButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prevButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endButton)
                .addContainerGap())
            .addComponent(globalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(globalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endButton)
                    .addComponent(cancelButton)
                    .addComponent(prevButton)
                    .addComponent(nextButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
}//GEN-LAST:event_cancelButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        setPanel(prevPanel());
}//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if(validatePanel())
            setPanel(nextPanel());
}//GEN-LAST:event_nextButtonActionPerformed

    private void endButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endButtonActionPerformed
        if(validatePanel()) {
            isOk = true;
            setVisible(false);
            dispose();
        }
    }//GEN-LAST:event_endButtonActionPerformed

    private void selectPathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPathButtonActionPerformed
        JFileChooser fileDlg = new JFileChooser(prjPathTextField.getText());
        fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if(fileDlg.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)
            return;

        prjPathTextField.setText(fileDlg.getSelectedFile().getPath() + File.separator + prjNameTextField.getText());
    }//GEN-LAST:event_selectPathButtonActionPerformed

    private void imgSelectFilePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgSelectFilePanelActionPerformed
        imgFile = imgSelectFilePanel.getSelectedFile();
        if(imgFile == null)
            return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if(imgFile.getName().toLowerCase().endsWith(".tif"))
                coverage = IOImage.loadTiff(imgFile);
            else
                coverage = new RSTGridReader(imgFile).read(null);
            
            int dataType = coverage.getRenderedImage().getSampleModel().getDataType();
            if(dataType == DataBuffer.TYPE_DOUBLE || dataType == DataBuffer.TYPE_FLOAT)
                throw new RuntimeException("Image data type is not integer type");
            
            codes = getCodes(coverage);
            codeComboBox.setModel(new DefaultComboBoxModel(codes.toArray()));
            DefaultComboBoxModel model = new DefaultComboBoxModel(codes.toArray());
            model.insertElementAt("(None)", 0);
            noDataComboBox.setModel(model);
            noDataComboBox.setSelectedIndex(0);

        } catch (Exception ex) {
            Logger.getLogger(NewProjectDialog.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error while loading image file\n" + ex.getLocalizedMessage());
            imgSelectFilePanel.setSelectedFile(null);
        }
        
        setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_imgSelectFilePanelActionPerformed

    public static TreeSet<Integer> getCodes(GridCoverage2D cov) {
        HashSet<Integer> codes = new HashSet<Integer>();
        RenderedImage img = cov.getRenderedImage();
        RandomIter r = RandomIterFactory.create(img, null);
        for(int y = 0; y < img.getHeight(); y++)
            for(int x = 0; x < img.getWidth(); x++)
                codes.add(r.getSample(x, y, 0));

        return new TreeSet<Integer>(codes);
    }

    private boolean validatePanel() {
        switch(indPanel) {
            case 0:
                if(prjNameTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Project's name is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if(prjPathTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Path is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                String rep = prjPathTextField.getText();
                rep = rep.substring(0, rep.lastIndexOf(File.separator));
                File fRep = new File(rep);
                if(!fRep.exists() || !fRep.isDirectory()) {
                    JOptionPane.showMessageDialog(this, "Folder " + rep + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                Config.setDir(fRep.getAbsolutePath());
                
                fRep = new File(prjPathTextField.getText());
                if(fRep.exists()) {
                    if(!fRep.isDirectory()){
                        JOptionPane.showMessageDialog(this, fRep.getAbsolutePath() + " is not a directory", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if(!fRep.canRead() || !fRep.canWrite() || !fRep.canExecute()){
                        JOptionPane.showMessageDialog(this, "L'application n'a pas les droits nécessaires pour accéder au répertoire "+fRep.getAbsolutePath(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if(fRep.list().length > 0) {
                        if(JOptionPane.showConfirmDialog(this,
                                "Project directory is not empty\nDo you want to continue ?",
                                "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                            return false;
                    }
                }
                return true;
            case 1:
                if(codeComboBox.getSelectedItem().equals(noDataComboBox.getSelectedItem())) {
                    JOptionPane.showMessageDialog(this, "Habitat and no data codes are equals.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                double noData = noDataComboBox.getSelectedIndex() == 0 ? Double.NaN : ((Number)noDataComboBox.getSelectedItem()).doubleValue();
                TreeSet<Integer> newCodes = new TreeSet<Integer>(codes);
                if(!Double.isNaN(noData))
                    newCodes.remove((int)noData);
                panel3.setCodes(newCodes, null);

            default:
                return true;

        }
     
    }

    private int nextPanel() {
        int ind = indPanel+1;


        if(ind < 0) ind = 0;
        if(ind >= NBPANEL) ind = NBPANEL-1;

        return ind;
    }

    private int prevPanel() {
        int ind = indPanel-1;


        if(ind < 0) ind = 0;
        if(ind >= NBPANEL) ind = NBPANEL-1;

        return ind;
    }

    private void setPanel(int ind) {
        indPanel = ind;

        endButton.setEnabled(indPanel == NBPANEL-1);
        nextButton.setEnabled(indPanel < NBPANEL-1);
        prevButton.setEnabled(indPanel > 0);

        ((CardLayout)globalPanel.getLayout()).show(globalPanel, "panel" + (indPanel+1));

        invalidate();
        validate();
        pack();


    }

    public Project createProject() throws IOException, SchemaException {
        File prjPath = new File(prjPathTextField.getText());
        prjPath.mkdir();

        int code = (Integer)codeComboBox.getSelectedItem();

        // on le convertit d'ha en m2 Attention on suppose que le système de coordonnées est en mètre
        double minArea = (Double)minAreaSpinner.getValue() * 10000;
        boolean con8 = con8RadioButton.isSelected();
        double noData = noDataComboBox.getSelectedIndex() == 0 ? Double.NaN : ((Number)noDataComboBox.getSelectedItem()).doubleValue();
        if(!Double.isNaN(noData))
            codes.remove((int)noData);

        Project prj = new Project(prjNameTextField.getText(), prjPath, coverage, codes, code, noData, con8, minArea, simplifyCheckBox.isSelected());

        prj.addLinkset(panel3.getLinkset(), true);

        return prj;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox codeComboBox;
    private javax.swing.JRadioButton con4RadioButton;
    private javax.swing.JRadioButton con8RadioButton;
    private javax.swing.ButtonGroup connexButtonGroup;
    private javax.swing.JPanel connexPanel;
    private javax.swing.JButton endButton;
    private javax.swing.JPanel globalPanel;
    private org.thema.common.swing.SelectFilePanel imgSelectFilePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner minAreaSpinner;
    private javax.swing.JButton nextButton;
    private javax.swing.JComboBox noDataComboBox;
    private javax.swing.JPanel panel1;
    private javax.swing.JPanel panel2;
    private org.thema.graphab.links.LinksetPanel panel3;
    private javax.swing.JButton prevButton;
    private javax.swing.JLabel prjNameLabel;
    private javax.swing.JTextField prjNameTextField;
    private javax.swing.JLabel prjPathLabel;
    private javax.swing.JTextField prjPathTextField;
    private javax.swing.JButton selectPathButton;
    private javax.swing.JCheckBox simplifyCheckBox;
    // End of variables declaration//GEN-END:variables

}
