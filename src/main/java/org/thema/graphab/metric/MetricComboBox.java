
package org.thema.graphab.metric;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import org.thema.graphab.Project;
import org.thema.graphab.metric.Metric.Type;

/**
 * Combobox showing metrics ordered by type and short name.
 * 
 * @author Gilles Vuidel
 */
public class MetricComboBox extends JComboBox {
       
    public MetricComboBox() {
        setMetrics(new ArrayList<Metric>(Project.GLOBAL_METRICS));
        setRenderer(new ComboBoxRenderer());
        addItemListener(new ItemListener() {
            Object lastItem;
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.DESELECTED) {
                    lastItem = e.getItem();
                }
                if(SEPARATOR.equals(e.getItem()) && e.getStateChange() == ItemEvent.SELECTED) {
                    setSelectedItem(lastItem);
                }
            }
        });
    }

    /**
     * Sets the metrics for this combo box.
     * @param metrics the new list of metrics
     */
    public final void setMetrics(List<? extends Metric> metrics) {
        Collections.sort(metrics, new Comparator<Metric>() {
            @Override
            public int compare(Metric o1, Metric o2) {
                int cmp = o1.getType().compareTo(o2.getType());
                if(cmp == 0) {
                    return o1.getShortName().compareTo(o2.getShortName());
                } else {
                    return cmp;
                }
            }
        });
//        for(int i = 1; i < metrics.size(); i++)
//            if(metrics.get(i-1).getType() != metrics.get(i).getType()) {
//                metrics.add(i, SEPARATOR);
//                i++;
//            }
        setModel(new DefaultComboBoxModel(metrics.toArray()));
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

        private JSeparator separator;

        private ComboBoxRenderer() {
            super();
            separator = new JSeparator(JSeparator.HORIZONTAL);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            if (SEPARATOR.equals(value)) { 
                return separator;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}


