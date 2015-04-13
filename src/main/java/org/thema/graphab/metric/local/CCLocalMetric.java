
package org.thema.graphab.metric.local;

import java.util.HashSet;
import java.util.Iterator;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.thema.graphab.graph.GraphGenerator;

/**
 * Clustering Coefficient metric.
 * 
 * @author Gilles Vuidel
 */
public class CCLocalMetric extends LocalMetric {

    @Override
    public String getShortName() {
        return "CC";
    }

    @Override
    public double calcMetric(Graphable g, GraphGenerator gen) {
        Node node = (Node) g;

        HashSet<Node> related = new HashSet<>();
        for(Iterator it = node.getRelated(); it.hasNext(); ) {
            related.add((Node)it.next());
        }

        if(related.size() <= 1) {
            return 0;
        }
        
        double sum = 0;
        for(Node rel : related) {
            for(Iterator itRel = rel.getRelated(); itRel.hasNext(); ) {
                Node n = (Node) itRel.next();
                if(related.contains(n)) {
                    sum++;
                }
            }
        }

        return sum / (related.size()*(related.size()-1));
    }

    @Override
    public boolean calcNodes() {
        return true;
    }

    @Override
    public Type getType() {
        return Type.TOPO;
    }
}
