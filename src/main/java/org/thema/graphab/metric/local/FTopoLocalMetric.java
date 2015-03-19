

package org.thema.graphab.metric.local;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.links.Linkset;
import org.thema.graphab.metric.DistProbaPanel;
import org.thema.graphab.metric.ParamPanel;

/**
 *
 * @author gvuidel
 */
public class FTopoLocalMetric extends LocalMetric {

    private double k = 0.0029957322735539907;
    private double d = 1000;
    private double p = 0.05;
    private double a = 1.0;


    public double calcIndice(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;
        double sum = 0;
        for(Edge edge : (List<Edge>)node.getEdges()) {
            sum += Math.exp(-k * gen.getCost(edge)) * Math.pow(Project.getPatchCapacity(node), a);            
        }
        return sum;
    }

    public String getName() {
        return java.util.ResourceBundle.getBundle("org/thema/graphab/indice/global/Bundle").getString(getShortName())
                + " (" + getShortName() + ")";
    }

    public String getShortName() {
        return "FTopo";
    }

    public void setParams(Map<String, Object> params) {
        d = ((Number)params.get(DistProbaPanel.DIST)).doubleValue();
        p = ((Number)params.get(DistProbaPanel.PROBA)).doubleValue();
        a = ((Number)params.get(DistProbaPanel.A)).doubleValue();
        k = -Math.log(p) / d;
    }

    public LinkedHashMap<String, Object> getParams() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put(DistProbaPanel.DIST, d);
        params.put(DistProbaPanel.PROBA, p);
        params.put(DistProbaPanel.A, a);
        return params;
    }

    public boolean calcNodes() {
        return true;
    }

    public boolean calcEdges() {
        return false;
    }

    public ParamPanel getParamPanel(Project project) {
        return new DistProbaPanel(d, p, a);
    }
    
    @Override
    public void setParamFromDetailName(String detailName) {
        a = Double.parseDouble(detailName.substring(detailName.indexOf(DistProbaPanel.A) + DistProbaPanel.A.length(), detailName.indexOf(DistProbaPanel.DIST)));
        d = Double.parseDouble(detailName.substring(detailName.indexOf(DistProbaPanel.DIST) + DistProbaPanel.DIST.length(), detailName.indexOf(DistProbaPanel.PROBA)));
        p = Double.parseDouble(detailName.substring(detailName.indexOf(DistProbaPanel.PROBA) + DistProbaPanel.PROBA.length()));
        k = -Math.log(p) / d;
    }    
    
    @Override
    public boolean isAcceptGraph(GraphGenerator graph) {
        return graph.getLinkset().getTopology() == Linkset.COMPLETE;
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }
}
