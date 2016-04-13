package org.jboss.windup.rules.apps.mavenize;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;


/**
 * @see MavenProjectModel
 */
@TypeValue(PomXmlModel.TYPE)
public interface PomXmlModel extends WindupVertexFrame
{
    String TYPE = "PomXml";
    String DEPENDENCY = TYPE + "_dependency";
    String PARENT = TYPE + "_parent";
    String BOM = TYPE + "_bom";

    @Adjacency(label = PARENT, direction = Direction.OUT)
    ArchiveCoordinateModel getParent();

    @Adjacency(label = PARENT, direction = Direction.OUT)
    void setParent(ArchiveCoordinateModel parent);

    @Adjacency(label = BOM, direction = Direction.OUT)
    ArchiveCoordinateModel getBom();

    @Adjacency(label = BOM, direction = Direction.OUT)
    void setBom(ArchiveCoordinateModel bom);

    @Adjacency(label = DEPENDENCY, direction = Direction.OUT)
    Iterable<ArchiveCoordinateModel> getDependencies();

    @Adjacency(label = DEPENDENCY, direction = Direction.OUT)
    void setDependencies(Iterable<ArchiveCoordinateModel> deps);

    @Adjacency(label = DEPENDENCY, direction = Direction.OUT)
    void addDependency(ArchiveCoordinateModel dependency);
}
