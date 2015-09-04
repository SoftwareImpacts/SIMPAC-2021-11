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


package org.thema.graphab.metric.global;

import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Size of the Largest Component metric.
 * 
 * @author Gilles Vuidel
 */
public class SLCMetric extends GlobalMetric {

    @Override
    public Double [] calcMetric(GraphGenerator g) {
        double max = 0;
        for(int i = 0; i < g.getComponents().size(); i++) {
            double size = g.getComponentGraphGen(i).getPatchCapacity();
            if(size > max) {
                max = size;
            }
        }

        return new Double[]{max};
    }

    @Override
    public String getShortName() {
        return "SLC";
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
