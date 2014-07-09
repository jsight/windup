package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.operation.AddArchiveReferenceInformation;
import org.jboss.windup.rules.apps.java.scan.operation.RecurseDirectoryAndAddFiles;
import org.jboss.windup.util.ZipUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class FileScannerWindupRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()

                    .addRule()
                    .when(GraphSearchConditionBuilder.create("inputDirectories")
                                .ofType(FileModel.class)
                                .withProperty(FileModel.PROPERTY_IS_DIRECTORY, true)
                    )
                    .perform(Iteration.over("inputDirectories").as(FileModel.class, "directory")
                                .perform(RecurseDirectoryAndAddFiles.startingAt("directory")).endIteration()
                    )

                    .addRule()
                    .when(GraphSearchConditionBuilder.create("inputFiles")
                                .ofType(FileModel.class)
                                .withProperty(FileModel.PROPERTY_IS_DIRECTORY, false)
                                .withProperty(FileModel.PROPERTY_FILE_PATH,
                                            GraphSearchPropertyComparisonType.REGEX,
                                            ZipUtil.getEndsWithZipRegularExpression())
                    )
                    .perform(Iteration.over("inputFiles").as(FileModel.class, "file")
                                .perform(AddArchiveReferenceInformation.to("file")).endIteration()
                    );

    }
}