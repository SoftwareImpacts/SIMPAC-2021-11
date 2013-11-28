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
public class ECSMetric extends GlobalMetric {

    public Double [] calcIndice(GraphGenerator gen) {
        double sum = 0;
        for(int i = 0; i < gen.getComponents().size(); i++) {
            double size = gen.getComponentGraphGen(i).getPatchCapacity();
            sum += Math.pow(size, 2);
        }
        return new Double[]{sum / gen.getPatchCapacity()};
    }

    public String getShortName() {
        return "ECS";
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
