
package org.thema.graphab.metric;

import java.util.Map;
import javax.swing.JPanel;

/**
 * Base class for Panel editing metric parameters
 * 
 * @author Gilles Vuidel
 */
public abstract class ParamPanel extends JPanel{

    /**
     * @return the parameters modified in the Panel
     */
    public abstract Map<String, Object> getParams();
}
