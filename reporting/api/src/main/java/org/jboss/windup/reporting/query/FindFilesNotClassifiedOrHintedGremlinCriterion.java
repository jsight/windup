package org.jboss.windup.reporting.query;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.ExecutionStatistics;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This provides a helper class that can be used execute a Gremlin search returning all FileModels that do not have associated
 * {@link FileLocationModel}s or @{link ClassificationModel}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FindFilesNotClassifiedOrHintedGremlinCriterion
{
    public Iterable<Vertex> query(GraphContext context, Iterable<Vertex> initialVertices)
    {
        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.total");
        try
        {
            List<Vertex> results = new ArrayList<>();
            for (Vertex vertex : initialVertices)
            {
                GremlinPipeline hasClassification = new GremlinPipeline(vertex);
                hasClassification.in(ClassificationModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
                if (hasClassification.iterator().hasNext())
                    continue;

                GremlinPipeline hasHint = new GremlinPipeline(vertex);
                hasHint.in(FileLocationModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);

                if (!hasHint.iterator().hasNext())
                    results.add(vertex);
            }

            return results;
        }
        finally
        {
            ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.total");
        }
    }
}
