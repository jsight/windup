package org.jboss.windup.rules.apps.mavenize;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.util.Logging;

/**
 *
 *  @author Ondrej Zizka, zizka at seznam.cz
 */
public class MavenizationService
{
    private static final Logger LOG = Logging.get(MavenizationService.class);
    public static final String OUTPUT_SUBDIR_MAVENIZED = "mavenized";

    private final GraphContext grCtx;


    MavenizationService(GraphContext graphContext)
    {
        this.grCtx = graphContext;
    }

    /**
     * For the given application (Windup input), creates a stub of Mavenized project.
     * <p>
     * The resulting structure is: (+--- is "module", +~~~ is a dependency)
     * <pre>
     *  Parent POM
     *   +--- BOM
     *     +~~~ JBoss EAP BOM
     *   +--- JAR submodule
     *     +~~~ library JARs
     *   +--- WAR
     *     +~~~ library JARs
     *     +~~~ JAR submodule
     *   +--- EAR
     *     +~~~ library JAR
     *     +~~~ JAR submodule
     *     +~~~ WAR submodule
     * </pre>
     */
    void mavenizeApp(ProjectModel projectModel)
    {
        LOG.info("Mavenizing  ProjectModel " + projectModel.toPrettyString());
        MavenizationContext mavCtx = new MavenizationContext();
        mavCtx.graphContext = grCtx;

        WindupConfigurationModel config = grCtx.getUnique(WindupConfigurationModel.class);
        ///mavCtx.mavenizedBaseDir = Paths.get("/tmp/mavenized");
        mavCtx.mavenizedBaseDir = config.getOutputPath().asFile().toPath().resolve(OUTPUT_SUBDIR_MAVENIZED);
        mavCtx.unifiedGroupId = new ModuleAnalysisHelper(grCtx).deriveGroupId(projectModel);
        mavCtx.unifiedAppName = normalizeDirName(projectModel.getName());
        mavCtx.unifiedVersion = "1.0";

        // 1) create the overall structure - a parent, and a BOM.

        // Root pom.xml ( == parent pom.xml in our resulting structure).
        mavCtx.rootPom = new Pom(new MavenCoords(mavCtx.getUnifiedGroupId(), mavCtx.getUnifiedAppName() + "-parent", mavCtx.getUnifiedVersion()));
        mavCtx.rootPom.role = Pom.ModuleRole.PARENT;
        ///PomXmlModel rootPom = grCtx.service(PomXmlModel.class).create();
        mavCtx.rootPom.parent = new Pom(MavenizeRuleProvider.JBOSS_PARENT);
        mavCtx.rootPom.name = projectModel.getName() + " - Parent";
        mavCtx.rootPom.description = "Parent of " + projectModel.getName();
        mavCtx.rootPom.root = true;

        // BOM
        Pom bom = new Pom(new MavenCoords(mavCtx.getUnifiedGroupId(), mavCtx.getUnifiedAppName() + "-bom", mavCtx.getUnifiedVersion()));
        bom.role = Pom.ModuleRole.BOM;
        bom.parent = new Pom(MavenizeRuleProvider.JBOSS_PARENT);
        bom.description = "Bill of Materials. See https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html";
        bom.name = projectModel.getName() + " - BOM";
        mavCtx.getRootPom().submodules.put(mavCtx.getUnifiedAppName() + "-bom", bom);
        mavCtx.bom = bom;

        // BOM - dependencyManagement dependencies
        for( ArchiveCoordinateModel dep : grCtx.getUnique(GlobalBomModel.class).getDependencies() ){
            LOG.info("Adding dep to BOM: " + dep.toPrettyString());
            bom.dependencies.add(new MavenCoords(dep));
        }

        // 2) Recursively add the modules.
        mavenizeModule(mavCtx, projectModel, null);

        // 3) Write the pom.xml's. TODO
        new MavenStructureRenderer(mavCtx).createMavenProjectDirectoryTree();
    }


    /**
     * Mavenizes the particular project module, i.e. single pom.xml.
     * Then continues recursively to submodules.
     * @return null if the project can't be processed for some reason, e.g. is from an unparsable jar.
     */
    private Pom mavenizeModule(MavenizationContext mavCtx, ProjectModel projectModel, Pom containingModule)
    {
        LOG.info("Mavenizing submodule ProjectModel " + projectModel.toPrettyString());
        LOG.info("    Root file: " + projectModel.getRootFileModel().toPrettyString());
        LOG.info("    Containing module: " + containingModule);

        if (projectModel.getRootFileModel().getParseError() != null)
            return null;

        // Known library -> skip.
        if (projectModel.getRootFileModel() instanceof IdentifiedArchiveModel)
        {
            final IdentifiedArchiveModel idArch = (IdentifiedArchiveModel)projectModel.getRootFileModel();
            if (idArch == null)
                LOG.warning("Project's IdentifiedArchiveModel getRootFileModel() returned null.");
            else if (idArch.getCoordinate() == null)
                LOG.warning("Project's IdentifiedArchiveModel getRootFileModel().getCoordinate() returned null.");
            else if(containingModule == null)
                LOG.warning("containingModule is null."); // IllegalStateEx?
            else
                containingModule.dependencies.add(MavenCoords.from(idArch.getCoordinate()));
            return null;
        }

        MavenCoords modulePomCoords = new MavenCoords();
        modulePomCoords.setGroupId(mavCtx.getUnifiedGroupId());
        modulePomCoords.setVersion(mavCtx.getUnifiedVersion());
        final String artifactId = normalizeDirName(projectModel.getName());
        modulePomCoords.setArtifactId(artifactId);
        modulePomCoords.setPackaging(guessPackaging(projectModel));

        Pom modulePom = new Pom(modulePomCoords);
        modulePom.role = Pom.ModuleRole.NORMAL;
        modulePom.parent = mavCtx.getRootPom();
        modulePom.bom = mavCtx.getBom().coords;
        mavCtx.getRootPom().submodules.put(artifactId, modulePom);
        mavCtx.getKnownSubmodules().add(modulePom);


        // Set up the dependency of the containing module on the contained module. E.g. EAR depends on WAR.
        if(containingModule != null)
            containingModule.dependencies.add(modulePom.coords);

        // Nested archives
        // For now, only count with the modules of this app. There are likely more in the other apps.
        Set<ArchiveModel> nestedModules = new HashSet<>();
        for (FileModel file : projectModel.getFileModelsNoDirectories())
        {
            if(!(file instanceof ArchiveModel)) //  TODO: Query for ArchiveModel directly.
                continue;

            // Known library -> simple dependency.
            if(file instanceof IdentifiedArchiveModel){
                IdentifiedArchiveModel artifact = (IdentifiedArchiveModel) file;
                modulePom.dependencies.add(new MavenCoords(artifact.getCoordinate()));
            }
            // Unknown archives -> nested modules? -> local dependencies.
            else {
                nestedModules.add((ArchiveModel) file);
            }
        }

        // Nested modules already identified as ProjectModel
        for (ProjectModel subProject : projectModel.getChildProjects())
        {
            Pom subModulePom = mavenizeModule(mavCtx, subProject, modulePom);
            if (subModulePom == null)
                continue;
            modulePom.dependencies.add(subModulePom.coords);
        }

        // Nested module candidates.
        for (ArchiveModel nestedModule : nestedModules)
        {
            // TODO: Is it a submodule or a library? Does it appear in multiple applications?
            //Pom subModulePom = mavenizeModule(mavCtx, nestedModule, containingModule);
            //modulePom.dependencies.add(subModulePom.identification);
        }

        // TODO: Determine and add the project internal dependencies (on other submodules, cross-app)
        // Queues? JNDI? CDI? REST + WS endpoints?
        // Needs to be done after initial mavenization of all apps to have their G:A:V.

        // TODO: Determine and add compile-time (API) dependencies (like, Java EE API's)
        // One big Java EE API vs. individual?

        // TODO: Remove the deps versions overriding the BOM.
        new ApiDependenciesDeducer(mavCtx).addAppropriateDependencies(projectModel, modulePom);

        return modulePom;
    }


    /**
     * Normalizes the name so it can be used as Maven artifactId or groupId.
     */
    private static String normalizeDirName(String name)
    {
        if(name == null)
            return null;
        return name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "-"); //\p{Alnum}
    }

    /**
     * Tries to guess the packaging of the archive - whether it's an EAR, WAR, JAR.
     * Maybe not needed as we can rely on the suffix?
     */
    private static String guessPackaging(ProjectModel projectModel)
    {
        String projectType = projectModel.getProjectType();
        if (projectType != null)
            return projectType;

        LOG.warning("WINDUP-983 getProjectType() returned null for: " + projectModel.getRootFileModel().getPrettyPath());

        String suffix = StringUtils.substringAfterLast(projectModel.getRootFileModel().getFileName(), ".");
        if ("jar war ear sar har ".contains(suffix+" ")){
            projectModel.setProjectType(suffix); // FIXME: Remove when WINDUP-983 is fixed.
            return suffix;
        }

        // TODO: Should we try something more? Used APIs? What if it's a source?

        return "unknown";
    }


    /**
     * Context of the mavenization - things to carry around.
     */
    static class MavenizationContext {
        private Path mavenizedBaseDir;
        private Pom rootPom;
        private Set<Pom> knownSubmodules = new HashSet<>();
        private String unifiedVersion;
        private String unifiedGroupId;
        private String unifiedAppName;
        private Pom bom; // BOM shared by all other submodules.
        private GraphContext graphContext;


        public Path getMavenizedBaseDir() {
            return mavenizedBaseDir;
        }

        public Pom getRootPom() {
            return rootPom;
        }

        public Set<Pom> getKnownSubmodules() {
            return knownSubmodules;
        }

        public String getUnifiedVersion() {
            return unifiedVersion;
        }

        public String getUnifiedGroupId() {
            return unifiedGroupId;
        }

        public String getUnifiedAppName() {
            return unifiedAppName;
        }

        public Pom getBom() {
            return bom;
        }


        public GraphContext getGraphContext() {
            return graphContext;
        }

    }

}
