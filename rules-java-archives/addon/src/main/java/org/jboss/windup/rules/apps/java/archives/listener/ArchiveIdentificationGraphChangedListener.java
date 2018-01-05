package org.jboss.windup.rules.apps.java.archives.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.event.MutationListener;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.archives.identify.ArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

/**
 * {@link MutationListener} responsible for identifying {@link ArchiveModel} instances when they are added to the graph.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public final class ArchiveIdentificationGraphChangedListener implements MutationListener
{
    private static final Logger LOG = Logging.get(ArchiveIdentificationGraphChangedListener.class);

    private final ArchiveIdentificationService identifier;
    private GraphContext context;
    private ArchiveService archiveService;

    public ArchiveIdentificationGraphChangedListener(GraphContext context, ArchiveIdentificationService identifier)
    {
        this.identifier = identifier;
        this.setGraphContext(context);
    }

    public ArchiveIdentificationGraphChangedListener setGraphContext(GraphContext context)
    {
        this.context = context;
        this.archiveService = new ArchiveService(context);
        return this;
    }

    @Override
    public void vertexPropertyChanged(Vertex element, Property oldValue, Object setValue, Object... vertexPropertyKeyValues)
    {
        String key = oldValue.key();
        if (ArchiveModel.ARCHIVE_NAME.equals(key))
        {
            ArchiveModel archive = archiveService.frame(element);

            setArchiveHashes(archive);

            Coordinate coordinate = identifier.getCoordinate(archive.getSHA1Hash());
            if (coordinate != null)
            {
                // If this is not a jar file, do not ignore it
                if (!StringUtils.endsWithIgnoreCase(archive.getFileName(), ".jar"))
                    return;

                // Never ignore the input application
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(this.context);
                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    if (inputPath.equals(archive))
                        return;
                }

                IdentifiedArchiveModel identifiedArchive = GraphService.addTypeToModel(context, archive, IdentifiedArchiveModel.class);
                ArchiveCoordinateModel coordinateModel = new GraphService<>(context, ArchiveCoordinateModel.class).create();

                coordinateModel.setArtifactId(coordinate.getArtifactId());
                coordinateModel.setGroupId(coordinate.getGroupId());
                coordinateModel.setVersion(coordinate.getVersion());
                coordinateModel.setClassifier(coordinate.getClassifier());
                identifiedArchive.setCoordinate(coordinateModel);

                LOG.info("Identified archive: [" + archive.getFilePath() + "] as [" + coordinate + "] will not be unzipped or analyzed.");
                IgnoredArchiveModel ignoredArchive = GraphService.addTypeToModel(context, archive, IgnoredArchiveModel.class);
                ignoredArchive.setIgnoredRegex("Known open-source library");
            }
            else
            {
                LOG.info("Archive not identified: " + archive.getFilePath() + " SHA1: " + archive.getSHA1Hash());
            }
        }
    }

    private void setArchiveHashes(ArchiveModel payload)
    {
        if (payload.getMD5Hash() == null)
        {
            try (InputStream is = payload.asInputStream())
            {
                String md5 = DigestUtils.md5Hex(is);
                payload.setMD5Hash(md5);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to read archive file at: " + payload.getFilePath() + " due to: " + e.getMessage(), e);
            }
        }

        if (payload.getSHA1Hash() == null)
        {
            try (InputStream is = payload.asInputStream())
            {
                String sha1 = DigestUtils.sha1Hex(is);
                payload.setSHA1Hash(sha1);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to read archive file at: " + payload.getFilePath() + " due to: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void vertexAdded(Vertex vertex)
    {

    }

    @Override
    public void vertexRemoved(Vertex vertex)
    {

    }

    @Override
    public void vertexPropertyRemoved(VertexProperty vertexProperty)
    {

    }

    @Override
    public void edgeAdded(Edge edge)
    {

    }

    @Override
    public void edgeRemoved(Edge edge)
    {

    }

    @Override
    public void edgePropertyChanged(Edge element, Property oldValue, Object setValue)
    {

    }

    @Override
    public void edgePropertyRemoved(Edge element, Property property)
    {

    }

    @Override
    public void vertexPropertyPropertyChanged(VertexProperty element, Property oldValue, Object setValue)
    {

    }

    @Override
    public void vertexPropertyPropertyRemoved(VertexProperty element, Property property)
    {

    }
}
