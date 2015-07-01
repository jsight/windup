package org.jboss.windup.reporting.service;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.taskdefs.Length;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.util.ExecutionStatistics;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Adds methods for loading and querying ClassificationModel related data.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class ClassificationService extends GraphService<ClassificationModel>
{
    public ClassificationService(GraphContext context)
    {
        super(context, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the provided
     * {@link FileModel}.
     */
    public int getMigrationEffortPoints(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(fileModel.asVertex());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);

        int classificationEffort = 0;
        for (Vertex v : classificationPipeline)
        {
            Integer migrationEffort = v.getProperty(ClassificationModel.EFFORT);
            if (migrationEffort != null)
            {
                classificationEffort += migrationEffort;
            }
        }
        return classificationEffort;
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance.
     */
    public Iterable<ClassificationModel> getClassifications(FileModel model)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(model.asVertex());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        return new FramedVertexIterable<ClassificationModel>(getGraphContext().getFramed(), pipeline, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the files in this
     * project.
     * 
     * If set to recursive, then also include the effort points from child projects.
     */
    public int getMigrationEffortPoints(ProjectModel projectModel, boolean recursive)
    {
        String taskName = getClass().getName() + ".getMigrationEffortPortsForProjectModel(" + recursive + ")";
        ExecutionStatistics.get().begin(taskName);
        try
        {
            GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(projectModel.asVertex());
            classificationPipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE).in(ClassificationModel.FILE_MODEL);
            classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);

            int classificationEffort = 0;
            for (Vertex v : classificationPipeline)
            {
                Integer migrationEffort = v.getProperty(ClassificationModel.EFFORT);
                if (migrationEffort != null)
                {
                    classificationEffort += migrationEffort;
                }
            }
            if (recursive)
            {
                for (ProjectModel childProject : projectModel.getChildProjects())
                {
                    // ExecutionStatistics cannot be called recursively, so pause around the recursive call
                    ExecutionStatistics.get().end(taskName);

                    classificationEffort += getMigrationEffortPoints(childProject, recursive);

                    ExecutionStatistics.get().begin(taskName);
                }
            }
            return classificationEffort;
        }
        finally
        {
            ExecutionStatistics.get().end(taskName);
        }
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided
     * {@link FileModel}. If an existing Model exists with the provided classificationText, that one will be used
     * instead.
     */
    public ClassificationModel attachClassification(FileModel fileModel, String classificationText, String description)
    {
        ClassificationModel model = getUnique(getTypedQuery()
                    .has(ClassificationModel.CLASSIFICATION, classificationText));
        if (model == null)
        {
            model = create();
            model.setClassification(classificationText);
            model.setDescription(description);
            model.setEffort(0);
            model.addFileModel(fileModel);
        }
        else
        {
            return attachClassification(model, fileModel);
        }

        return model;
    }

    /**
     * This method just attaches the {@link ClassificationModel} to the {@link Length.FileMode}. It will only do so if
     * this link is not already present.
     */
    public ClassificationModel attachClassification(ClassificationModel classificationModel, FileModel fileModel)
    {
        // check for duplicates
        for (FileModel existingFileModel : classificationModel.getFileModels())
        {
            if (existingFileModel.equals(fileModel))
            {
                return classificationModel;
            }
        }
        classificationModel.addFileModel(fileModel);
        return classificationModel;
    }

    public ClassificationModel attachLink(ClassificationModel classificationModel, LinkModel linkModel)
    {
        for (LinkModel existing : classificationModel.getLinks())
        {
            if (StringUtils.equals(existing.getLink(), linkModel.getLink()))
            {
                return classificationModel;
            }
        }
        classificationModel.addLink(linkModel);
        return classificationModel;
    }
}
