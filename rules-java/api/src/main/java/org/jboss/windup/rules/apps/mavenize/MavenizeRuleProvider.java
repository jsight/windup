package org.jboss.windup.rules.apps.mavenize;

import freemarker.core.ParseException;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.freemarker.FurnaceFreeMarkerTemplateLoader;
import org.jboss.windup.rules.apps.java.archives.config.ArchiveIdentificationConfigLoadingRuleProvider;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.scan.provider.DiscoverMavenHierarchyRuleProvider;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Creates a stub of Maven project structure, including pom.xml's and the proper directory structure and dependencies,
 * based on the project structure determined by prior Windup rules (nested deployments) and the libraries included in them.
 *
 * TODO: For nested deployments like EAR with a WAR and JAR, this also creates the appropriate structure and packaging.
 *
 * @author Ondrej Zizka
 */
@RuleMetadata(after = {
    ArchiveMetadataExtractionPhase.class,
    ArchiveIdentificationConfigLoadingRuleProvider.class,
    ArchiveExtractionPhase.class,
    DiscoverMavenHierarchyRuleProvider.class,
    DiscoverProjectStructurePhase.class
}, phase = DependentPhase.class)
public class MavenizeRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE_POM_XML = "/org/jboss/windup/rules/apps/mavenize/pom.xml.ftl";
    public static final String TEMPLATE_BOM_XML = "/org/jboss/windup/rules/apps/mavenize/bom.xml.ftl";
    public static final MavenAxis JBOSS_PARENT = new MavenAxis("org.jboss", "jboss-parent", "20");

    // @formatter:off
    /**
     * Iterates over input projects and
     * @param grCtx
     * @return
     */
    @Override
    public Configuration getConfiguration(GraphContext grCtx)
    {
        ConditionBuilder applicationProjectModels = Query.fromType(WindupConfigurationModel.class);

        return ConfigurationBuilder.begin()
            // Create the BOM frame
            .addRule()
            .perform(new GraphOperation() {
                public void perform(GraphRewrite event, EvaluationContext context) {
                    GlobalBomModel bom = event.getGraphContext().getFramed().addVertex(null, GlobalBomModel.class);
                    ArchiveCoordinateModel jbossParent = event.getGraphContext().getFramed().addVertex(null, ArchiveCoordinateModel.class);
                    copyTo(JBOSS_PARENT, jbossParent);
                    bom.setParent(jbossParent);
                }
            })
            .withId("Mavenize-BOM-data-collection")

            // For each IdentifiedArchive, add it to the global BOM.
            .addRule()
            .when(Query.fromType(IdentifiedArchiveModel.class))
            .perform(new MavenizePutNewerVersionToGlobalBomOperation())
            .withId("Mavenize-BOM-file-creation")

            // For each application given to Windup as input, mavenize it.
            .addRule()
            .when(applicationProjectModels, SourceMode.isDisabled())
            .perform(new MavenizeApplicationOperation())
            .withId("Mavenize-projects-mavenization");
    }
    // @formatter:on


    /**
     * This operation puts the given IdentifiedArchiveModel to the global BOM frame.
     * If there's already one of such G:A:P, then the newer version is used.
     * Eventual version collisions are overridden in pom.xml's.
     */
    class MavenizePutNewerVersionToGlobalBomOperation extends AbstractIterationOperation<IdentifiedArchiveModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, IdentifiedArchiveModel archive)
        {
            // BOM
            //Query.fromType(GlobalBomModel.class).getUnique();
            GraphService<GlobalBomModel> bomServ = new GraphService<>(event.getGraphContext(), GlobalBomModel.class);
            GlobalBomModel bom = bomServ.getUnique();

            // Check for an existing coord, add the new one
            GraphService<ArchiveCoordinateModel> coordServ = new GraphService<>(event.getGraphContext(), ArchiveCoordinateModel.class);
            /*Iterable<ArchiveCoordinateModel> coordsOfGivenGA = coordServ.findAllByProperties(
                    new String[]{ArchiveCoordinateModel.GROUP_ID, ArchiveCoordinateModel.ARTIFACT_ID},
                    new String[]{archive.getCoordinate().getGroupId(), archive.getCoordinate().getArtifactId()}
            );*/
            bom.addNewerDependency(archive.getCoordinate());
            // TODO
        }
    }

    /**
     * Calls mavenizeApp() for each Windup input app.
     */
    private class MavenizeApplicationOperation extends AbstractIterationOperation<WindupConfigurationModel>
    {
        public MavenizeApplicationOperation()
        {
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
        {
            for (FileModel inputPath : payload.getInputPaths())
            {
                ProjectModel projectModel = inputPath.getProjectModel();
                if (projectModel == null)
                    throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                mavenizeApp(event.getGraphContext(), projectModel);
            }
        }
    }




    /**
     * For the given application (Windup input), creates a stub of Mavenized project.
     */
    private void mavenizeApp(GraphContext grCtx, ProjectModel projectModel)
    {
        MavenizationContext mavCtx = new MavenizationContext();
        ///String overwrite = (String) grCtx.getOptionMap().get(OutputPathOption.NAME);
        mavCtx.mavenizedBaseDir = Paths.get("/tmp/mavenized"); /// TODO
        mavCtx.unifiedGroupId = (String) grCtx.getOptionMap().get(ScanPackagesOption.NAME);
        mavenizeModule(mavCtx, projectModel, null);
    }


    /**
     * Mavenizes the particular project module, i.e. single pom.xml.
     * Then continues recursively to submodules.
     */
    private void mavenizeModule(MavenizationContext mavCtx, ProjectModel projectModel, Pom parentPom)
    {
        Pom pom = new Pom();
        mavCtx.knownSubmodules.add(pom);
        if(mavCtx.rootPom == null)
            mavCtx.rootPom = pom;

        // Figure out artifactId
        pom.identification.groupId = normalizeDirName(mavCtx.unifiedGroupId);
        pom.identification.version = mavCtx.unifiedVersion;
        pom.identification.artifactId = normalizeDirName(projectModel.getName());
        pom.identification.packaging = guessPackaging(projectModel);

        // Set the parent pom
        pom.parent = parentPom;

        // Local dependencies (other modules of this project)
        // For now, only count with the modules of this app. There are likely more in the other apps.

        // Other dependencies
        for (FileModel file : projectModel.getFileModelsNoDirectories())
        {
            if(!(file instanceof IdentifiedArchiveModel))
                continue;
            IdentifiedArchiveModel artifact = (IdentifiedArchiveModel) file;

            pom.dependencies.add(new MavenAxis(artifact.getCoordinate()));
        }

        String dir = normalizeDirName(projectModel.getName());
        Path resultPomXmlPath = mavCtx.mavenizedBaseDir.resolve(dir).resolve("pom.xml");
        renderPomXml(mavCtx, pom, resultPomXmlPath);

        /// TODO: Nested modulest must be flattened and weaved through intra-dependencies.
        /// TODO: WAR, EJB, EAR modules should have JBoss specific settings as per EAP quickstarts.
    }


    private void renderPomXml(MavenizationContext mavCtx, Pom pom, Path pomXmlPath)
    {
        Map vars = new HashMap();
        vars.put("pom", pom);

        try
        {
            renderFreemarkerTemplate(mavCtx.pomXmlTemplatePath, vars, pomXmlPath);
        }
        catch (ParseException ex)
        {
            throw new WindupException("Could not parse pom.xml template: " + mavCtx.pomXmlTemplatePath + "\nReason: " + ex.getMessage(), ex);
        }
        catch (IOException | TemplateException ex)
        {
            throw new WindupException("Error rendering pom.xml template: " + mavCtx.pomXmlTemplatePath + "\nReason: " + ex.getMessage(), ex);
        }
    }

    /**
     * Normalizes the name so it can be used as Maven artifactId or groupId.
     */
    private static String normalizeDirName(String name)
    {
        if(name == null)
            return null;
        return name.replaceAll("[^a-zA-Z0-9]", "-"); //\p{Alnum}
    }

    /**
     * Tries to guess the packaging of the archive - whether it's an EAR, WAR, JAR.
     * Maybe not needed as we can rely on the suffix?
     */
    private static String guessPackaging(ProjectModel projectModel)
    {

        return projectModel.getProjectType();
    }

    /**
     * Renders the given FreeMarker template to given directory, using given variables.
     */
    private static void renderFreemarkerTemplate(Path templatePath, Map vars, Path outputPath)
            throws MalformedTemplateNameException, ParseException, IOException, TemplateException
    {
        if(templatePath == null)
            throw new WindupException("templatePath is null");

        freemarker.template.Configuration freemarkerConfig = new freemarker.template.Configuration();
        DefaultObjectWrapperBuilder objectWrapperBuilder = new DefaultObjectWrapperBuilder(freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

        freemarkerConfig.setObjectWrapper(objectWrapperBuilder.build());

        //Map<String, Object> objects = new HashMap<>();

        freemarkerConfig.setTemplateLoader(new FurnaceFreeMarkerTemplateLoader());
        freemarkerConfig.setTemplateUpdateDelayMilliseconds(3600);
        Template template = freemarkerConfig.getTemplate(templatePath.toString());
        try (FileWriter fw = new FileWriter(outputPath.toFile()))
        {
            template.process(vars, fw);
        }
    }



    /**
     * Context of the mavenization - things to carry around.
     */
    private static class MavenizationContext {
        private Path mavenizedBaseDir;
        private Pom rootPom;
        private Set<Pom> knownSubmodules = new HashSet<>();
        private String unifiedVersion;
        private String unifiedGroupId;
        private Path pomXmlTemplatePath;
    }

    /**
     * A simplified POM moodel - just G:A:V:C:P and the dependencies.
     */
    private static class Pom
    {
        MavenAxis identification = new MavenAxis();
        Pom parent;
        String name;
        String description;

        Set<MavenAxis> dependencies = new LinkedHashSet<>();
        Set<Pom> localDependencies = new LinkedHashSet<>();
        Set<Pom> subModules = new LinkedHashSet<>();
    }


        private static void copyTo(MavenAxis from, ArchiveCoordinateModel to)
        {
            to.setArtifactId(from.artifactId);
            to.setGroupId(from.groupId);
            to.setVersion(from.version);
            to.setClassifier(from.classifier);
            to.setPackaging(from.packaging);
        }

}
