/*
 * Copyright (C) 2017 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.style.FeatureStyle;

/**
 *
 * @author Gilles Vuidel
 */
public class PatchLayer extends FeatureLayer {

    private Project project;
    
    public PatchLayer(Project project) {
        super(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("Patch"), project.getPatches(), 
                new FeatureStyle(new Color(0x65a252), new Color(0x426f3c)), project.getCRS());
        this.project = project;
    }
    
    @Override
    public JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu(); 
        menu.add(new AbstractAction(java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("RemoveAttrMenuItem.text")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                List attrList = new ArrayList(project.getPatches().get(0).getAttributeNames());
                attrList = attrList.subList(4, attrList.size());
                JList list = new JList(attrList.toArray());
                int res = JOptionPane.showConfirmDialog(null, new JScrollPane(list), 
                        java.util.ResourceBundle.getBundle("org/thema/graphab/Bundle").getString("RemoveAttrMenuItem.text"), 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if(res == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                for(Object attr : list.getSelectedValuesList()) {
                    DefaultFeature.removeAttribute((String)attr, project.getPatches());
                }
                try {
                    project.savePatch();
                } catch (IOException | SchemaException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return menu;
    }
}
