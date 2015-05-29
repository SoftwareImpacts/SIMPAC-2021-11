
package org.thema.graphab.metric.local;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.Feature;
import org.thema.graph.pathfinder.Path;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;
import org.thema.graphab.graph.GraphPathFinder;
import org.thema.graphab.metric.AlphaParamMetric;
import org.thema.graphab.metric.ParamPanel;

/**
 * Betweeness Centrality metric.
 * 
 * @author Gilles Vuidel
 */
public class BCLocalMetric extends AbstractBCLocalMetric<GraphPathFinder> {

    private AlphaParamMetric alphaParam = new AlphaParamMetric();
    
    @Override
    public HashMap<Object, Double> calcPartMetric(GraphPathFinder finder, GraphGenerator g) {
        HashMap<Object, Double> result = new HashMap<>();
        double srcCapa = Project.getPatchCapacity(finder.getNodeOrigin());
        for(Node node : finder.getComputedNodes()) {
            if (((Integer)Project.getPatch(finder.getNodeOrigin()).getId()) < (Integer)Project.getPatch(node).getId()) {
                Path path = finder.getPath(node);
                if (path == null) {
                    continue;
                }
                double v = Math.pow(Project.getPatchCapacity(node) * srcCapa, alphaParam.getBeta())
                        * Math.exp(-alphaParam.getAlpha() * finder.getCost(node));
                
                List<Node> nodes = path.getNodes();
                for (int i = 1; i < nodes.size()-1; i++) {
                    Feature f = (Feature)nodes.get(i).getObject();
                    if (result.containsKey(f.getId())) {
                        result.put(f.getId(), result.get(f.getId()) + v);
                    } else {
                        result.put(f.getId(), v);
                    }
                }
                for (Edge e : path.getEdges()) {
                    Feature f = (Feature)e.getObject();
                    if (result.containsKey(f.getId())) {
                        result.put(f.getId(), result.get(f.getId()) + v);
                    } else {
                        result.put(f.getId(), v);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getShortName() {
        return "BC";
    }
    
    @Override
    public void setParams(Map<String, Object> params) {
        alphaParam.setParams(params);
    }

    @Override
    public LinkedHashMap<String, Object> getParams() {
        return alphaParam.getParams();
    }

    @Override
    public ParamPanel getParamPanel(Project project) {
        return alphaParam.getParamPanel(project);
    }
    
    @Override
    public Type getType() {
        return Type.WEIGHT;
    }

    @Override
    public TypeParam getTypeParam() {
        return TypeParam.PATHFINDER;
    }    
}
