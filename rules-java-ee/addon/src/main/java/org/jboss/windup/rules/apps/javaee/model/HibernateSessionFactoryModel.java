package org.jboss.windup.rules.apps.javaee.model;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to Hibernate Session Factories.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
@TypeValue(HibernateSessionFactoryModel.TYPE)
public interface HibernateSessionFactoryModel extends WindupVertexFrame
{

    public static final String TYPE = "HibernateSessionFactory";

    /**
     * Contains a link back to the {@link HibernateConfigurationFileModel} containing these properties
     */
    @Adjacency(label = HibernateConfigurationFileModel.HIBERNATE_SESSION_FACTORY, direction = Direction.IN)
    HibernateConfigurationFileModel getHibernateConfigurationFileModel();

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DataSourceModel.DATA_SOURCE, direction = Direction.OUT)
    public Iterable<DataSourceModel> getDataSources();

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DataSourceModel.DATA_SOURCE, direction = Direction.OUT)
    void addDataSource(DataSourceModel dataSource);
    
    /**
     * Contains the hibernate session factories properties
     */
    @MapInAdjacentProperties(label = "sessionFactoryProperties")
    Map<String, String> getSessionFactoryProperties();

    /**
     * Contains the hibernate session factories properties
     */
    @MapInAdjacentProperties(label = "sessionFactoryProperties")
    void setSessionFactoryProperties(Map<String, String> map);
}
