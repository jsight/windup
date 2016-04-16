package org.jboss.windup.rules.apps.mavenize;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;

/**
 * A POJO for Maven coordinate. A counterpart for ArchiveCoordinateModel.
 */
public class MavenCoords
{
    private String groupId;
    private String artifactId;
    private String version; // May be null if defined in a BOM.
    private String classifier;
    private String packaging;

    // This should not really be here, but for the simplicity, I keep it here.
    private String scope;
    private String comment;
    private Set<MavenCoords> exclusions = new HashSet<>();


    MavenCoords()
    {
    }


    @Deprecated
    MavenCoords(ArchiveCoordinateModel coordinate)
    {
        this.groupId = coordinate.getGroupId();
        this.artifactId = coordinate.getArtifactId();
        this.version = coordinate.getVersion();
        this.classifier = coordinate.getClassifier();
        this.packaging = coordinate.getPackaging();
    }

    static MavenCoords from(ArchiveCoordinateModel coordinate)
    {
        return new MavenCoords()
        .setGroupId(coordinate.getGroupId())
        .setArtifactId(coordinate.getArtifactId())
        .setVersion(coordinate.getVersion())
        .setClassifier(coordinate.getClassifier())
        .setPackaging(coordinate.getPackaging());
    }

    MavenCoords(String groupId, String artifactId, String version)
    {
        this(groupId, artifactId, version, "pom");
    }


    MavenCoords(String groupId, String artifactId, String version, String packaging)
    {
        this(groupId, artifactId, version, null, packaging);
    }


    MavenCoords(String groupId, String artifactId, String version, String classifier, String packaging)
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.packaging = packaging;
    }




    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.getGroupId());
        hash = 79 * hash + Objects.hashCode(this.getArtifactId());
        hash = 79 * hash + Objects.hashCode(this.getVersion());
        hash = 79 * hash + Objects.hashCode(this.getClassifier());
        hash = 79 * hash + Objects.hashCode(this.getPackaging());
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
        final MavenCoords other = (MavenCoords) obj;
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


    public String getGroupId()
    {
        return groupId;
    }


    public MavenCoords setGroupId(String groupId)
    {
        this.groupId = groupId;
        return this;
    }


    public String getArtifactId()
    {
        return artifactId;
    }


    public MavenCoords setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
        return this;
    }


    public String getVersion()
    {
        return version;
    }


    public MavenCoords setVersion(String version)
    {
        this.version = version;
        return this;
    }


    public String getClassifier()
    {
        return classifier;
    }


    public MavenCoords setClassifier(String classifier)
    {
        this.classifier = classifier;
        return this;
    }


    public String getPackaging()
    {
        return packaging; // == null ? "pom" : packaging;
    }


    public MavenCoords setPackaging(String packaging)
    {
        this.packaging = packaging;
        return this;
    }


    public String getScope()
    {
        return scope;
    }


    public MavenCoords setScope(String scope)
    {
        this.scope = scope;
        return this;
    }


    public String getComment()
    {
        return comment;
    }


    public MavenCoords setComment(String comment)
    {
        this.comment = comment;
        return this;
    }


    public Set<MavenCoords> getExclusions()
    {
        return exclusions;
    }

    public MavenCoords addExclusion(MavenCoords coord){
        this.getExclusions().add(coord);
        return this;
    }


    @Override
    public String toString()
    {
        return '{' + groupId + ":" + artifactId + ":" + version + ":" + StringUtils.defaultString(classifier) + ":" + packaging + ",s:" + scope +'}';
    }
}
