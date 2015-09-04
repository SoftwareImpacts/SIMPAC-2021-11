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

import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.metric.local.LocalMetric;

/**
 * Agregate a local metric by shannon entropy.
 * 
 * @author Gilles Vuidel
 */
public class EntropyLocal2GlobalMetric extends AbstractLocal2GlobalMetric {

    /**
     * Creates a new EntropyLocal2GlobalMetric.
     * @param metric the local metric to agregate
     * @param type agregate on nodes or on edges ?
     */
    public EntropyLocal2GlobalMetric(LocalMetric indice, TypeElem type) {
        super(indice, type);
    }

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues()) {
            if(val < 0) {
                throw new RuntimeException("Value < 0 not allowed for entropy");
            }
            sum += val;
        }
        
        double e = 0;
        int nb = 0;
        for(Double val : getValues()) { 
            if(val > 0) {
                e += (val/sum) * Math.log(val/sum);
                nb++;
            }
        }
        
        return new Double[]{-e / Math.log(nb)};
    }

    @Override
    public String getPrefixShortName() {
        return "E";
    }

    @Override
    public String getPrefixName() {
        return "Entropy";
    }
    
}