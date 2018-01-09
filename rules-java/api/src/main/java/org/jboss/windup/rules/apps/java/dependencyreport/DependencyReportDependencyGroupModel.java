package org.jboss.windup.rules.apps.java.dependencyreport;

import com.syncleus.ferma.annotations.Incidence;
import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Contains a links to dependent archives, grouped by their SHA1 value.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(DependencyReportDependencyGroupModel.TYPE)
public interface DependencyReportDependencyGroupModel extends WindupVertexFrame
{
    String TYPE = "DependencyReportDependencyGroupModel";
    String DEPENDENCY_SHA1 = "depSHA1";
    String DEPENDENCY_GROUP_TO_ARCHIVE = "dependencyGroupToArchive";
    String CANONICAL_PROJECT = "canonicalProject";

    /**
     * Contains the SHA1 value of every archive in this group.
     */
    @Property(DEPENDENCY_SHA1)
    String getSHA1();

    /**
     * Contains the SHA1 value of every archive in this group.
     */
    @Property(DEPENDENCY_SHA1)
    void setSHA1(String sha1);

    /**
     * Contains the canonical project for this dependency group.
     */
    @Adjacency(label = CANONICAL_PROJECT, direction = Direction.OUT)
    ProjectModel getCanonicalProject();

    /**
     * Contains the canonical project for this dependency group.
     */
    @Adjacency(label = CANONICAL_PROJECT, direction = Direction.OUT)
    void setCanonicalProject(ProjectModel project);

    /**
     * Contains links to the archives that match this SHA1 hash.
     */
    @Incidence(label = DEPENDENCY_GROUP_TO_ARCHIVE, direction = Direction.OUT)
    Iterable<DependencyReportToArchiveEdgeModel> getArchives();

    /**
     * Contains links to the archives that match this SHA1 hash.
     */
    @Incidence(label = DEPENDENCY_GROUP_TO_ARCHIVE, direction = Direction.OUT)
    DependencyReportToArchiveEdgeModel addArchiveModel(ArchiveModel archiveModel);
}
