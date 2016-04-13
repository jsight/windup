package org.jboss.windup.rules.apps.mavenize;

import java.util.Objects;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;

/**
 * A POJO for Maven coordinate. A counterpart for ArchiveCoordinateModel.
 */
public class MavenAxis
{
    String groupId;
    String artifactId;
    String version; // May be null if defined in a BOM.
    String classifier;
    String packaging;


    MavenAxis()
    {
    }


    MavenAxis(ArchiveCoordinateModel coordinate)
    {
        this.groupId = coordinate.getGroupId();
        this.artifactId = coordinate.getArtifactId();
        this.version = coordinate.getVersion();
        this.classifier = coordinate.getClassifier();
        this.packaging = coordinate.getPackaging();
    }


    MavenAxis(String groupId, String artifactId, String version)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.groupId);
        hash = 79 * hash + Objects.hashCode(this.artifactId);
        hash = 79 * hash + Objects.hashCode(this.version);
        hash = 79 * hash + Objects.hashCode(this.classifier);
        hash = 79 * hash + Objects.hashCode(this.packaging);
        return hash;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MavenAxis other = (MavenAxis) obj;
        if (!Objects.equals(this.groupId, other.groupId))
            return false;
        if (!Objects.equals(this.artifactId, other.artifactId))
            return false;
        if (!Objects.equals(this.version, other.version))
            return false;
        if (!Objects.equals(this.classifier, other.classifier))
            return false;
        if (!Objects.equals(this.packaging, other.packaging))
            return false;
        return true;
    }

}
