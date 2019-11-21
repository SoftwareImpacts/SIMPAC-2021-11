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
import org.thema.graphab.metric.local.IFInterLocalMetric;
import org.thema.graphab.metric.local.IFLocalMetric;
import org.thema.graphab.metric.local.LocalSingleMetric;

/**
 * Global metric summing the results of a local metric on nodes or edges.
 * 
 * @author Gilles Vuidel
 */
public class SumLocal2GlobalMetric extends AbstractLocal2GlobalMetric {

    /**
     * Creates a new global metric.
     * @param metric the local metric to sum
     * @param typeElem apply the local metric on nodes or edges ?
     */
    public SumLocal2GlobalMetric(LocalSingleMetric metric, TypeElem typeElem) {
        super(metric, typeElem);
    }

    @Override
    public Double[] calcMetric(GraphGenerator g) {
        double sum = 0;
        for(Double val : getValues()) {
            sum += val;
        }
        return new Double[] {sum};
    }

    @Override
    public String getPrefixShortName() {
        return "S";
    }

    @Override
    public String getPrefixName() {
        return "Sum";
    }
    
    public static class SumIFMetric extends SumLocal2GlobalMetric {
        
        public SumIFMetric() {
            super(new IFLocalMetric(), TypeElem.NODE);
        }
        /**
         * Constructor for dupplicate method.
         * Do not use !
         * @param metric may be IFLocalMetric
         * @param type may be TypeElem.NODE
         */
        public SumIFMetric(LocalSingleMetric metric, TypeElem type) {
            super(metric, type);
        }
    }
    
    public static class SumIFInterMetric extends SumLocal2GlobalMetric {
        
        public SumIFInterMetric() {
            super(new IFInterLocalMetric(), TypeElem.NODE);
        }
        /**
         * Constructor for dupplicate method.
         * Do not use !
         * @param metric may be IFInterLocalMetric
         * @param type may be TypeElem.NODE
         */
        public SumIFInterMetric(LocalSingleMetric metric, TypeElem type) {
            super(metric, type);
        }
    }
    
}