/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.thema.graphab.Project.Method;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.Metric;

/**
 *
 * @author Gilles Vuidel
 */
public abstract class GlobalMetric extends Metric  {

    public abstract Double[] calcIndice(GraphGenerator g);

    public String[] getResultNames() {
        return new String[]{getShortName()};
    }

    @Override
    public boolean isAcceptMethod(Method method) {
        return method != Method.LOCAL;
    }

}
