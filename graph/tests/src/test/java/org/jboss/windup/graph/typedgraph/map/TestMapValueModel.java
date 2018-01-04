package org.jboss.windup.graph.typedgraph.map;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.syncleus.ferma.annotations.Property;

@TypeValue("MapModelValue")
public interface TestMapValueModel extends WindupVertexFrame
{
    @Property("myproperty")
    String getProperty();

    @Property("myproperty")
    void setProperty(String val);
}
