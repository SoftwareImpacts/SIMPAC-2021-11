/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric;

import java.util.Map;
import javax.swing.JPanel;

/**
 *
 * @author gvuidel
 */
public abstract class ParamPanel extends JPanel{

    public abstract Map<String, Object> getParams();
}
