package org.jboss.windup.graph.model;

import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import org.apache.tinkerpop.gremlin.structure.Edge;

/**
 * The base {@link EdgeFrame} type implemented by all model types.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeField(WindupFrame.TYPE_PROP)
public interface WindupEdgeFrame extends EdgeFrame, WindupFrame<Edge>
{
}
