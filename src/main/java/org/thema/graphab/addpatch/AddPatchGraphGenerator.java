/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.addpatch;

import com.vividsolutions.jts.geom.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.Feature;
import org.thema.graphab.links.Path;
import org.thema.graphab.Project;
import org.thema.graphab.graph.GraphGenerator;

/**
 *
 * @author gvuidel
 */
public class AddPatchGraphGenerator extends GraphGenerator {
    GraphGenerator gen;
    Node addedElem;

    public AddPatchGraphGenerator(GraphGenerator gen) {
        super(gen, "AddPatch");
        this.gen = gen;
        this.addedElem = null;
                
        if(gen.isIntraPatchDist())
            throw new IllegalArgumentException("Intra patch distance is not supported");
        
        graph = gen.dupGraphWithout(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }
    
    public void addPatch(Point p, double capa) throws Exception {
        if(addedElem != null)
            throw new IllegalStateException("Graph already contains an added element");
        
        BasicGraphBuilder builder = new BasicGraphBuilder();
        // crée le noeud
        DefaultFeature patch = Project.getProject().createPointPatch(p, capa);
        Node node = builder.buildNode();
        node.setObject(patch);
        graph.getNodes().add(node);
        
        // puis crée les liens
        HashMap<DefaultFeature, Path> newLinks = getLinkset().calcNewLinks(patch);
        for(DefaultFeature d : newLinks.keySet()) {
            Path path = new Path(patch, d, newLinks.get(d).getCost(), newLinks.get(d).getDist());  
            if(getType() != THRESHOLD || getCost(path) <= getThreshold()) {
                Node nodeB = null;
                for(Node n : (Collection<Node>)getGraph().getNodes())
                    if(((Feature)n.getObject()).getId().equals(d.getId())) {
                        nodeB = n;
                        break;
                    }

                Edge edge = builder.buildEdge(node, nodeB);
                edge.setObject(path);
                node.add(edge);
                nodeB.add(edge);
                graph.getEdges().add(edge);
            }
        }
        
        addedElem = node;
        
        components = null;
        compFeatures = null;
    }
    
    public void reset() {

        graph.getNodes().remove(addedElem);

        for(Object o : addedElem.getEdges()) {
            Edge e = (Edge) o;
            graph.getEdges().remove(e);
            e.getOtherNode(addedElem).remove(e);
        }
        
        components = null;
        compFeatures = null;
        addedElem = null;

    }
   

    @Override
    protected void createGraph() {
        // do nothing
    }
}
