/*
 * Copyright (C) 2018 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thema.graphab.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.thema.graphab.links.Linkset;

/**
 * CLI range parsing and distance conversion.
 * 
 * @author Gilles Vuidel
 */
public class Range {
    private double min, max, inc;
    private List<Double> values;
    private boolean convDist;

    public Range(double val, boolean convDist) {
        this(val, 1, val, convDist);
    }

    public Range(double min, double max, boolean convDist) {
        this(min, 1, max, convDist);
    }

    public Range(double min, double inc, double max, boolean convDist) {
        this.min = min;
        this.max = max;
        this.inc = inc;
        this.convDist = convDist;
    }

    public Range(List<Double> values, boolean convDist) {
        this.values = values;
        this.min = Collections.min(values);
        this.max = Collections.max(values);
        this.convDist = convDist;
    }

    /**
     * @return all values of the range
     * @throws IllegalStateException if the range must convert the values
     */
    public List<Double> getValues() {
        if(convDist) {
            throw new IllegalStateException("Cannot convert distance without linkset");
        }
        if(values == null) {
            List<Double> lst = new ArrayList<>();
            for(double v = min; v <= max; v += inc) {
                lst.add(v);
            }
            return lst;
        } else {
            return values;
        }
    }
    
    private List<Double> getVals() {
        if(values == null) {
            List<Double> lst = new ArrayList<>();
            for(double v = min; v <= max; v += inc) {
                lst.add(v);
            }
            return lst;
        } else {
            return values;
        }
    }

    /**
     * May convert the values from distance to cost.
     * @param linkset the linkset for conversion if needed
     * @return all the values
     */
    public List<Double> getValues(Linkset linkset) {
        if(convDist) {
            List<Double> lst = new ArrayList<>();
            for(double v : getVals()) {
                lst.add(linkset.estimCost(v));
            }
            return lst;
        } else {
            return getVals();
        }
    }

    /**
     * Returns the first value (minimum value).
     * May convert the value from distance to cost
     * @param linkset the linkset for conversion if needed
     * @return the first value
     */
    public double getValue(Linkset linkset) {
        if(convDist) {
            return linkset.estimCost(min);
        } else {
            return min;
        }
    }

    /**
     * @return if this range contains only one number
     */
    public boolean isSingle() {
        return getSize() == 1;
    }

    /**
     * @return the minimum value (this value is never converted)
     */
    public double getMin() {
        return min;
    }

    /**
     * @return the number of values
     */
    public int getSize() {
        if(values == null) {
            int n = 0;
            for(double v = min; v <= max; v += inc) {
                n++;
            }
            return n;
        } else {
            return values.size();
        }
    }

    /**
     * Parse the string and extract the range.
     * It can be :
     * - a single number
     * - a list of number separated by comma
     * - a real range of the form min:max or min:inc:max
     * All three cases can be surrounded by bracket for automatic conversion from distance to cost
     * @param s the string containing the number 
     * @return the new range
     */
    public static Range parse(String s) {
        boolean conv = false;
        if(s.startsWith("{")) {
            s = s.substring(1, s.length()-1).trim();
            conv = true;
        }
        String [] tok = s.split(":");
        if(tok.length == 1) {
            tok = s.split(",");
            if(tok.length == 1) {
                return new Range(Double.parseDouble(tok[0]), conv);
            } else {
                List<Double> values = new ArrayList<>(tok.length);
                for(String tok1 : tok) {
                    values.add(Double.parseDouble(tok1));
                }
                return new Range(values, conv);
            }

        } else if(tok.length == 2) {
            return new Range(Double.parseDouble(tok[0]), Double.parseDouble(tok[1]), conv);
        } else if(tok.length == 3) {
            return new Range(Double.parseDouble(tok[0]), Double.parseDouble(tok[1]), Double.parseDouble(tok[2]), conv);
        }
        return  null;
    }
}