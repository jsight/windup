package org.jboss.windup.rules.apps.mavenize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.TypeAwareFramedGraphQuery;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.util.Logging;

import com.thinkaurelius.titan.core.attribute.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.CopySplitPipe;
import com.tinkerpop.pipes.filter.PropertyFilterPipe;
import com.tinkerpop.pipes.sideeffect.AggregatePipe;
import com.tinkerpop.pipes.sideeffect.GroupCountPipe;
import com.tinkerpop.pipes.transform.OutPipe;
import com.tinkerpop.pipes.transform.PropertyPipe;

/**
 * Contains methods for assistance in analyzing modules
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
public class ModuleAnalysisHelper
{
    public static final String LAST_RESORT_DEFAULT_GROUP_ID = "com.mycompany.mavenized";
    private static final Logger LOG = Logging.get(ModuleAnalysisHelper.class);

    protected GraphContext graphContext;


    public ModuleAnalysisHelper(GraphContext context)
    {
        this.graphContext = context;
    }

    String deriveGroupId(ProjectModel projectModel)
    {
        if (projectModel instanceof MavenProjectModel)
        {
            MavenProjectModel mavenProject = (MavenProjectModel) projectModel;
            mavenProject.getGroupId();
        }

        // User can set this via --mavenizeGroupId.
        {
            String groupId = (String) graphContext.getOptionMap().get(MavenizeGroupIdOption.NAME);
            if (groupId != null)
            {
                if (groupId.matches(MavenizeGroupIdOption.REGEX_GROUP_ID))
                    return groupId;
                LOG.severe(MavenizeGroupIdOption.NAME + " doesn't match the groupId pattern, ignoring: " + groupId);
            }
        }

        // Try to take what the user put to --packages.
        {
            List<String> scanPackages = (List<String>) graphContext.getOptionMap().get(ScanPackagesOption.NAME);
            if (scanPackages != null && !scanPackages.isEmpty() && scanPackages.get(0).contains("."))
                return scanPackages.get(0);
        }

        // FIXME TODO - If this is unused, it should be removed.
        // Get shared prefix of all packages in this application. Can result to "".
        //String fromPackages = new ModuleAnalysisHelper(graphContext).deriveGroupIdFromPackages(projectModel);
        //if (fromPackages != null)
            //return fromPackages;

        return LAST_RESORT_DEFAULT_GROUP_ID;
    }

    /**
     * Counts the packages prefixes appearing in this project and if some of them make more than half of the total of existing packages, this prefix
     * is returned. Otherwise, returns null.
     *
     * This is just a helper, it isn't something really hard-setting the package. It's something to use if the user didn't specify using
     * --mavenize.groupId, and the archive or project name is something insane, like few sencences paragraph (a description) or a number or such.
     */
    String deriveGroupIdFromPackages(ProjectModel projectModel)
    {
        Map<String, Integer> pkgsMap = new HashMap<>();
        Set<String> pkgs = new HashSet<>(1000);
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel);

        PipeFunction<String, String> upToThirdDot = new PipeFunction<String, String>()
        {
            @Override
            public String compute(String pkgName)
            {
                int upToThirdDot = StringUtils.ordinalIndexOf(pkgName, ".", 3);
                return StringUtils.substring(pkgName, 0, upToThirdDot);
            }
        };
        PipeFunction<String, String> identity = new PipeFunction<String, String>()
        {
            @Override
            public String compute(String pkgName)
            {
                return pkgName;
            }
        };

        pipeline
                    .add(new OutPipe(ProjectModel.PROJECT_MODEL_TO_FILE))
                    .add(new TypePipe(JavaClassFileModel.class))
                    // .add(new GroupByPipe(identity, identity)
                    .add(new PropertyPipe(JavaClassFileModel.PROPERTY_PACKAGE_NAME))
                    .add(new CopySplitPipe(
                                new AggregatePipe(pkgs, upToThirdDot),
                                new GroupCountPipe(pkgsMap)))
        // .add(new AggregatePipe(pkgs, upToThirdDot))
        ;

        Map.Entry<String, Integer> biggest = null;
        for (Map.Entry<String, Integer> entry : pkgsMap.entrySet())
        {
            if (biggest == null || biggest.getValue() < entry.getValue())
                biggest = entry;
        }
        // More than a half is of this package.
        if (biggest != null && biggest.getValue() > pkgsMap.size() / 2)
            return biggest.getKey();

        return null;
    }

    public class TypePipe extends PropertyFilterPipe<Vertex, Vertex>
    {
        public TypePipe(Class<? extends WindupVertexFrame> clazz)
        {
            super(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TypeAwareFramedGraphQuery.getTypeValue(clazz));
        }
    }

}
