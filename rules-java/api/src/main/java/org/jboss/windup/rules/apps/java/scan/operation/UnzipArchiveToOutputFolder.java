package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.forge.furnace.util.Streams;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.rules.files.FileDiscoveredEvent;
import org.jboss.windup.rules.files.FileDiscoveredListener;
import org.jboss.windup.rules.files.FileDiscoveredResult;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class UnzipArchiveToOutputFolder extends AbstractIterationOperation<ArchiveModel>
{
    private static final String MALFORMED_ARCHIVE = "Malformed archive";
    private static final String ARCHIVES = "archives";
    private static final Logger LOG = Logging.get(UnzipArchiveToOutputFolder.class);

    public UnzipArchiveToOutputFolder(String variableName)
    {
        super(variableName);
    }

    public UnzipArchiveToOutputFolder()
    {
        super();
    }

    public static UnzipArchiveToOutputFolder unzip(String variableName)
    {
        return new UnzipArchiveToOutputFolder(variableName);
    }

    public static UnzipArchiveToOutputFolder unzip()
    {
        return new UnzipArchiveToOutputFolder();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        Iterable<FileDiscoveredListener> listeners = FurnaceHolder.getFurnace().getAddonRegistry().getServices(FileDiscoveredListener.class);

        LOG.info("Unzipping archive: " + payload.toPrettyString());
        File zipFile = payload.asFile();

        if (zipFile == null || !zipFile.isFile())
        {
            throw new WindupException("Input path doesn't point to a file: " + (zipFile == null ? "null" : zipFile.getAbsolutePath()));
        }

        // create a folder for all archive contents
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        String windupOutputFolder = cfg.getOutputPath().getFilePath();

        Path windupTempUnzippedArchiveFolder = Paths.get(windupOutputFolder, ARCHIVES);
        if (!Files.isDirectory(windupTempUnzippedArchiveFolder))
        {
            try
            {
                Files.createDirectories(windupTempUnzippedArchiveFolder);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to create temporary folder: " + windupTempUnzippedArchiveFolder
                            + " due to: " + e.getMessage(), e);
            }
        }
        unzipToTempDirectory(event, context, listeners, windupTempUnzippedArchiveFolder, zipFile, payload);
    }

    private Path getAppArchiveFolder(Path tempFolder, String appArchiveName)
    {
        Path appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName);

        int fileIdx = 1;
        // if it is already created, try another folder name
        while (Files.exists(appArchiveFolder))
        {
            appArchiveFolder = Paths.get(tempFolder.toString(), appArchiveName + "." + fileIdx);
            fileIdx++;
        }
        return appArchiveFolder;
    }

    private void unzipToTempDirectory(final GraphRewrite event, EvaluationContext context,
                Iterable<FileDiscoveredListener> listeners,
                final Path tempFolder, final File inputZipFile,
                final ArchiveModel archiveModel)
    {
        final FileService fileService = new FileService(event.getGraphContext());

        // Setup a temp folder for the archive
        String appArchiveName = archiveModel.getArchiveName();
        if (null == appArchiveName)
            throw new IllegalStateException("Archive model doesn't have an archiveName: " + archiveModel.getFilePath());

        final Path appArchiveFolder = getAppArchiveFolder(tempFolder, appArchiveName);

        try
        {
            Files.createDirectories(appArchiveFolder);
        }
        catch (IOException e)
        {
            throw new WindupException("Could not create a temporary directory for application \""
                        + appArchiveName + "\" at \"" + appArchiveFolder.toString() + "\" due to: " + e.getMessage(), e);
        }

        // unzip to the temp folder
        LOG.info("Unzipping " + inputZipFile.getPath() + " to "
                    + appArchiveFolder.toString());

        try
        {
            unzipToFolder(inputZipFile, appArchiveFolder.toFile(), listeners);
        }
        catch (Throwable e)
        {
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(context, archiveModel, MALFORMED_ARCHIVE, "Cannot unzip the file");
            LOG.warning("Cannot unzip the file " + inputZipFile.getPath() + " to " + appArchiveFolder.toString()
                        + ". The ArchiveModel was classified as malformed.");
            return;
        }

        FileModel newFileModel = fileService.createByFilePath(appArchiveFolder.toString());
        // mark the path to the archive
        archiveModel.setUnzippedDirectory(newFileModel);
        newFileModel.setParentArchive(archiveModel);

        // add all unzipped files, and make sure their parent archive is set
        recurseAndAddFiles(event, context, listeners, tempFolder, fileService, archiveModel, newFileModel);
    }

    /**
     * Unzip the given {@link File} to the specified directory.
     */
    private void unzipToFolder(File inputFile, File outputDir, Iterable<FileDiscoveredListener> listeners) throws IOException
    {
        if (inputFile == null)
            throw new IllegalArgumentException("Argument inputFile is null.");
        if (outputDir == null)
            throw new IllegalArgumentException("Argument outputDir is null.");

        try (final ZipFile zipFile = new ZipFile(inputFile))
        {
            Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
            while (entryEnum.hasMoreElements())
            {
                final ZipEntry entry = entryEnum.nextElement();
                String entryName = entry.getName();
                File destFile = new File(outputDir, entryName);
                if (!entry.isDirectory())
                {
                    File parentDir = destFile.getParentFile();
                    if (!parentDir.isDirectory() && !parentDir.mkdirs())
                    {
                        throw new WindupException("Unable to create directory: " + parentDir.getAbsolutePath());
                    }

                    FileDiscoveredEvent event = new FileDiscoveredEvent()
                    {
                        @Override
                        public String getFilename()
                        {
                            return entry.getName();
                        }

                        @Override
                        public InputStream getInputStream() throws IOException
                        {
                            return zipFile.getInputStream(entry);
                        }
                    };

                    boolean skipFile = false;
                    for (FileDiscoveredListener listener : listeners)
                    {
                        FileDiscoveredResult result = listener.fileDiscovered(event);
                        if (result == FileDiscoveredResult.KEEP)
                        {
                            skipFile = false;
                            break;
                        }
                        else if (result == FileDiscoveredResult.DISCARD)
                        {
                            skipFile = true;
                        }
                    }

                    if (skipFile)
                        continue;

                    try (InputStream zipInputStream = zipFile.getInputStream(entry))
                    {
                        try (FileOutputStream outputStream = new FileOutputStream(destFile))
                        {
                            Streams.write(zipInputStream, outputStream);
                        }
                    }
                }
            }
        }
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as FileModels.
     *
     * We don't set the parent file model in the case of the inital children, as the direct parent is really the archive itself. For example for file
     * "root.zip/pom.xml" - the parent for pom.xml is root.zip, not the directory temporary directory that happens to hold it.
     */
    private void recurseAndAddFiles(GraphRewrite event, EvaluationContext context,
                Iterable<FileDiscoveredListener> listeners, Path tempFolder,
                FileService fileService, ArchiveModel archiveModel,
                FileModel parentFileModel)
    {
        FileFilter filter = TrueFileFilter.TRUE;
        if (archiveModel instanceof IdentifiedArchiveModel)
        {
            filter = new IdentifiedArchiveFileFilter(archiveModel);
        }

        File fileReference = parentFileModel.asFile();
        WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(event.getGraphContext());
        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File subFile : subFiles)
                {
                    if (!filter.accept(subFile))
                    {
                        continue;
                    }

                    FileModel subFileModel = fileService.createByFilePath(parentFileModel, subFile.getAbsolutePath());
                    subFileModel.setParentArchive(archiveModel);

                    // check if this file should be ignored
                    if (checkIfIgnored(event.getGraphContext(), subFileModel, windupJavaConfigurationService.getIgnoredFileRegexes()))
                    {
                        continue;
                    }

                    if (subFile.isFile() && ZipUtil.endsWithZipExtension(subFileModel.getFilePath()))
                    {
                        File newZipFile = subFileModel.asFile();
                        ArchiveModel newArchiveModel = GraphService.addTypeToModel(event.getGraphContext(), subFileModel, ArchiveModel.class);
                        newArchiveModel.setParentArchive(archiveModel);
                        newArchiveModel.setArchiveName(newZipFile.getName());
                        archiveModel.addChildArchive(newArchiveModel);

                        /*
                         * New archive must be reloaded in case the archive should be ignored
                         */
                        newArchiveModel = GraphService.refresh(event.getGraphContext(), newArchiveModel);
                        unzipToTempDirectory(event, context, listeners, tempFolder, newZipFile, newArchiveModel);
                    }

                    if (subFile.isDirectory())
                    {
                        recurseAndAddFiles(event, context, listeners, tempFolder, fileService, archiveModel, subFileModel);
                    }
                }
            }
        }
    }

    /**
     * Checks if the {@link FileModel#getFilePath()} + {@link FileModel#getFileName()} is ignored by any of the specified regular expressions.
     */
    private boolean checkIfIgnored(final GraphContext context, FileModel file, List<String> patterns)
    {
        boolean ignored = false;
        if (patterns != null && patterns.size() != 0)
        {
            for (String pattern : patterns)
            {
                if (file.getFilePath().matches(pattern))
                {
                    IgnoredFileModel ignoredFileModel = GraphService.addTypeToModel(context, file, IgnoredFileModel.class);
                    ignoredFileModel.setIgnoredRegex(pattern);
                    LOG.info("File/Directory placed in " + file.getFilePath() + " was ignored, because matched [" + pattern + "].");
                    ignored = true;
                    break;
                }
            }
        }
        return ignored;
    }

    @Override
    public String toString()
    {
        return "UnzipArchivesToOutputFolder";
    }
}
