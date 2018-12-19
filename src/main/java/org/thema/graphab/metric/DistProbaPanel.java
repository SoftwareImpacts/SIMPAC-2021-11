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

import java.util.HashMap;
import java.util.Map;
import org.thema.graphab.links.Linkset;

/**
 * ParamPanel for metric with parameters Distance, Proba and Beta.
 * 
 * @author Gilles Vuidel
 */
public class DistProbaPanel extends ParamPanel {

    
    /** 
     * Creates new form DistProbaPanel.
     * 
     */
    public DistProbaPanel() {
        this(null, 1000, 0.05, 1.0);
    }
    /** 
     * Creates new form DistProbaPanel.
     * 
     * @param dist distance
     * @param p proba
     */
    public DistProbaPanel(Linkset linkset, double dist, double p) {
        this(linkset, dist, p, Double.NaN);
    }
    
    /** 
     * Creates new form DistProbaPanel.
     * 
     * @param dist distance
     * @param p proba
     * @param beta beta exponent
     */
    public DistProbaPanel(Linkset linkset, double dist, double p, double beta) {
        initComponents();
        dSpinner.setValue(dist);
        pSpinner.setValue(p);
        if(Double.isNaN(beta)) {
            betaLabel.setEnabled(false);
            betaSpinner.setEnabled(false);
        } else {
            betaSpinner.setValue(beta);
        }
        
        setLinkset(linkset);
    }

    public void setLinkset(Linkset linkset) {
        if(linkset != null && linkset.isCostUnit()) {
            unitLabel.setText(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("UnitCost"));
        } else {
            unitLabel.setText(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("UnitMeter"));
        }
    }
    
    @Override
    public Map<String, Object> getParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put(AlphaParamMetric.DIST, dSpinner.getValue());
        params.put(AlphaParamMetric.PROBA, pSpinner.getValue());
        if(betaSpinner.isEnabled()) {
            params.put(AlphaParamMetric.BETA, betaSpinner.getValue());
        }
        return params;
    }

    /**
     * @return alpha
     */
    public double getAlpha() {
        return Double.parseDouble(alphaTextField.getText());
    }

    /**
     * @return distance
     */
    public double getDist() {
        return (Double)dSpinner.getValue();
    }

    /**
     * @return proba
     */
    public double getP() {
        return (Double)pSpinner.getValue();
    }

    /**
     * @return beta
     */
    public double getBeta() {
        if(!betaSpinner.isEnabled()) {
            throw new IllegalArgumentException("This metric has not beta parameter.");
        }
        return (Double)betaSpinner.getValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        alphaTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        pSpinner = new javax.swing.JSpinner();
        dSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        betaLabel = new javax.swing.JLabel();
        betaSpinner = new javax.swing.JSpinner();
        unitLabel = new javax.swing.JLabel();

        jLabel5.setText("α");

        alphaTextField.setEditable(false);
        alphaTextField.setText("0.006931472");

        jLabel8.setText("α = -log(p) / d");

        pSpinner.setModel(new javax.swing.SpinnerNumberModel(0.5d, 0.0d, 1.0d, 0.1d));
        pSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pSpinnerStateChanged(evt);
            }
        });

        dSpinner.setModel(new javax.swing.SpinnerNumberModel(100.0d, null, null, 1.0d));
        dSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dSpinnerStateChanged(evt);
            }
        });

        jLabel7.setText("p");

        jLabel6.setText("d");

        betaLabel.setText("β");

        betaSpinner.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, null, 0.1d));

        unitLabel.setText("meter");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(alphaTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(betaLabel))
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(betaSpinner)
                                    .addComponent(pSpinner)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dSpinner)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unitLabel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(alphaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(dSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(unitLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(pSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(betaLabel)
                    .addComponent(betaSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pSpinnerStateChanged
        alphaTextField.setText(String.valueOf(-Math.log(getP()) / getDist()));
}//GEN-LAST:event_pSpinnerStateChanged

    private void dSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dSpinnerStateChanged
        alphaTextField.setText(String.valueOf(-Math.log(getP()) / getDist()));
}//GEN-LAST:event_dSpinnerStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alphaTextField;
    private javax.swing.JLabel betaLabel;
    private javax.swing.JSpinner betaSpinner;
    private javax.swing.JSpinner dSpinner;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSpinner pSpinner;
    private javax.swing.JLabel unitLabel;
    // End of variables declaration//GEN-END:variables



}
