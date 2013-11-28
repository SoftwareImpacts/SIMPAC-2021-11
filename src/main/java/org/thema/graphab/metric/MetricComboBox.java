/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.metric;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import org.thema.graphab.Project;
import org.thema.graphab.metric.Metric.Type;

/**
 *
 * @author gvuidel
 */
public class MetricComboBox extends JComboBox {
    
    List<Metric> indices;
    
    public MetricComboBox() {
        setIndices(new ArrayList<Metric>(Project.GLOBAL_METRICS));
        setRenderer(new ComboBoxRenderer());
        addItemListener(new ItemListener() {
            Object lastItem;
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.DESELECTED)
                    lastItem = e.getItem();
                if(SEPARATOR.equals(e.getItem()) && e.getStateChange() == ItemEvent.SELECTED)
                    setSelectedItem(lastItem);
            }
        });
    }

    public List<Metric> getIndices() {
        return indices;
    }

    public void setIndices(List<? extends Metric> lstIndice) {
        this.indices = new ArrayList(lstIndice);
        Collections.sort(indices, new Comparator<Metric>() {
            @Override
            public int compare(Metric o1, Metric o2) {
                int cmp = o1.getType().compareTo(o2.getType());
                if(cmp == 0)
                    return o1.getShortName().compareTo(o2.getShortName());
                else
                    return cmp;
            }
        });
//        for(int i = 1; i < indices.size(); i++)
//            if(indices.get(i-1).getType() != indices.get(i).getType()) {
//                indices.add(i, SEPARATOR);
//                i++;
//            }
        setModel(new DefaultComboBoxModel(indices.toArray()));
        this.fireActionEvent();
    }
   
    private final static Metric SEPARATOR = new Metric() {
        @Override
        public String getShortName() {
            return "-";
        }  
        @Override
        public Type getType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    
    private class ComboBoxRenderer extends DefaultListCellRenderer {

        JSeparator separator;

        public ComboBoxRenderer() {
            super();
            separator = new JSeparator(JSeparator.HORIZONTAL);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            if (SEPARATOR.equals(value)) 
                return separator;
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}


