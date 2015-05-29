
package org.thema.graphab.metric;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public class MetricTest {
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    public MetricTest() {
    }
    

    /**
     * Test of setParamFromDetailName method, of class Metric.
     */
    @Test
    public void testSetParamFromDetailName() {
        System.out.println("get/set ParamFromDetailName");
        for(Metric m : Project.getLocalMetrics()) {
            String detailName = m.getDetailName();
            m.setParamFromDetailName(detailName);
            assertEquals(detailName, m.getDetailName());
            if(m.hasParams()) {
                exception.expect(IllegalArgumentException.class);
                m.setParamFromDetailName(m.getShortName());
            }
        }
        for(Metric m : Project.getGlobalMetricsFor(Project.Method.GLOBAL)) {
            String detailName = m.getDetailName();
            m.setParamFromDetailName(detailName);
            assertEquals(detailName, m.getDetailName());
            if(m.hasParams()) {
                exception.expect(IllegalArgumentException.class);
                m.setParamFromDetailName(m.getShortName());
            }
        }
    }
    
}
