package org.jboss.windup.rules.apps.mavenize;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.versions.ComparableVersion;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import static org.jboss.windup.rules.apps.mavenize.PomXmlModel.DEPENDENCY;


/**
 * Serves only to identify the unique singleton object in the DB.
 */
@TypeValue(GlobalBomModel.TYPE)
public interface GlobalBomModel extends PomXmlModel
{
    String TYPE = "GlobalBom";

    /**
     * Adds a dependency to BOM. If the BOM already contains that G:A:P, updates the dependency the newer version.
     * @param dependency
     */
    @JavaHandler
    @Adjacency(label = DEPENDENCY, direction = Direction.OUT)
    void addNewerDependency(ArchiveCoordinateModel dependency);

    public abstract class Impl implements GlobalBomModel, JavaHandlerContext<Vertex> {
        public void addNewerDependency(final ArchiveCoordinateModel newCoord){
            Vertex v = this.it();
            final Iterable<ArchiveCoordinateModel> existingDeps = this.getDependencies();
            for (ArchiveCoordinateModel dep : existingDeps)
            {
                if(!StringUtils.equals(newCoord.getGroupId(), dep.getGroupId()))
                    continue;
                if(!StringUtils.equals(newCoord.getArtifactId(), dep.getArtifactId()))
                    continue;
                if(!StringUtils.equals(newCoord.getClassifier(), dep.getClassifier()))
                    continue;
                if(!StringUtils.equals(newCoord.getPackaging(), dep.getPackaging()))
                    continue;
                if(0 < compareVersions(newCoord.getVersion(), dep.getVersion()))
                    dep.setVersion(newCoord.getVersion());
                break;
            }
        }

        /** Uses Forge's comprehension of version strings. */
        private static int compareVersions(String aS, String bS)
        {
            ComparableVersion a = new ComparableVersion(aS);
            ComparableVersion b = new ComparableVersion(bS);
            return b.compareTo(a);
        }
    }
}
