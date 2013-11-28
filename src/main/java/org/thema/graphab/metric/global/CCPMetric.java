/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class CCPMetric extends GlobalMetric {

    public Double[] calcIndice(GraphGenerator g) {
        double total = g.getPatchCapacity();
        double sum = 0;
        for(int i = 0; i < g.getComponents().size(); i++) {
            double size = g.getComponentGraphGen(i).getPatchCapacity();
            sum += Math.pow(size / total, 2);
        }

        return new Double[]{sum};
    }

    public String getShortName() {
        return "CCP";
    }

    @Override
    public boolean isAcceptMethod(Project.Method method) {
        return method == Project.Method.GLOBAL;
    }

    @Override
    public Type getType() {
        return Type.AREA;
    }
}
