package org.carlspring.strongbox.storage.metadata;

import org.carlspring.strongbox.storage.metadata.maven.io.filters.ArtifactVersionDirectoryFilter;
import org.carlspring.strongbox.storage.metadata.maven.comparators.MetadataVersionComparator;
import org.carlspring.strongbox.storage.metadata.maven.comparators.SnapshotVersionComparator;
import org.carlspring.strongbox.storage.metadata.maven.versions.MetadataVersion;
import org.carlspring.strongbox.storage.metadata.maven.visitors.ArtifactVersionDirectoryVisitor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.M2GavCalculator;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stodorov
 */
public class VersionCollector
{

    private static final Logger logger = LoggerFactory.getLogger(VersionCollector.class);

    private static final M2GavCalculator M2_GAV_CALCULATOR = new M2GavCalculator();

    public VersionCollectionRequest collectVersions(Path artifactBasePath)
            throws IOException
    {
        VersionCollectionRequest request = new VersionCollectionRequest();
        request.setArtifactBasePath(artifactBasePath);

        List<MetadataVersion> versions = new ArrayList<>();

        List<Path> versionPaths;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(artifactBasePath,
                                                                 new ArtifactVersionDirectoryFilter()))
        {
            versionPaths = Lists.newArrayList(ds);
        }

        // Add all versions
        for (Path versionDirectoryPath : versionPaths)
        {
            try
            {
                Path pomArtifactPath = getPomPath(artifactBasePath, versionDirectoryPath);

                // No pom, no metadata.
                if (pomArtifactPath != null)
                {
                    Model pom = getPom(pomArtifactPath);

                    BasicFileAttributes fileAttributes = Files.readAttributes(versionDirectoryPath,
                                                                              BasicFileAttributes.class);

                    // TODO: This will not work for versionless POM-s which extend the version from a parent.
                    // TODO: If pom.getVersion() == null, walk the parents until a parent with
                    // TODO: a non-null version is found and use that as the version.
                    String version = pom.getVersion() != null ? pom.getVersion() :
                                     (pom.getParent() != null ? pom.getVersion() : null);

                    if (version == null)
                    {
                        continue;
                    }

                    if (ArtifactUtils.isSnapshot(version))
                    {
                        version = ArtifactUtils.toSnapshotVersion(version);
                    }

                    MetadataVersion metadataVersion = new MetadataVersion();
                    metadataVersion.setVersion(version);
                    metadataVersion.setCreatedDate(fileAttributes.lastModifiedTime());

                    versions.add(metadataVersion);

                    if (artifactIsPlugin(pom))
                    {
                        String name = pom.getName() != null ? pom.getName() : pom.getArtifactId();

                        // TODO: SB-339: Get the maven plugin's prefix properly when generating metadata
                        // TODO: This needs to be addressed properly, as it's not correct.
                        // TODO: This can be obtained from the jar's META-INF/maven/plugin.xml and should be read
                        // TODO: either via a ZipInputStream, or using TrueZip.
                        // String prefix = pom.getArtifactId().replace("maven-plugin", "").replace("-plugin$", "");

                        Plugin plugin = new Plugin();
                        plugin.setName(name);
                        plugin.setArtifactId(pom.getArtifactId());
                        plugin.setPrefix(PluginDescriptor.getGoalPrefixFromArtifactId(pom.getArtifactId()));

                        request.addPlugin(plugin);
                    }
                }
            }
            catch (XmlPullParserException | IOException e)
            {
                logger.error("POM file '{}' appears to be corrupt.", versionDirectoryPath.toAbsolutePath(), e);
            }
        }

        // 1.1 < 1.2 < 1.3 ....
        if (!versions.isEmpty())
        {
            Collections.sort(versions, new MetadataVersionComparator());
        }

        request.setMetadataVersions(versions);
        request.setVersioning(generateVersioning(versions));

        return request;
    }

    private Path getPomPath(Path artifactBasePath,
                            Path versionDirectoryPath)
    {
        String version = versionDirectoryPath.getFileName().toString();
        if (!ArtifactUtils.isSnapshot(version))
        {
            return Paths.get(versionDirectoryPath.toAbsolutePath().toString(),
                             artifactBasePath.getFileName().toString() + "-" +
                             versionDirectoryPath.getFileName() + ".pom");
        }
        else
        {
            // Attempt to get the latest available POM
            List<String> filePaths = Arrays.asList(versionDirectoryPath.toFile()
                                                                       .list((dir, name) -> name.endsWith(".pom")));

            if (filePaths != null && !filePaths.isEmpty())
            {
                Collections.sort(filePaths);
                return Paths.get(versionDirectoryPath.toAbsolutePath().toString(),
                                 filePaths.get(filePaths.size() - 1));
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Get snapshot versioning information for every released snapshot
     *
     * @param artifactVersionPath
     * @throws IOException
     */
    public List<SnapshotVersion> collectTimestampedSnapshotVersions(Path artifactVersionPath)
            throws IOException
    {
        List<SnapshotVersion> snapshotVersions = new ArrayList<>();

        ArtifactVersionDirectoryVisitor artifactVersionDirectoryVisitor = new ArtifactVersionDirectoryVisitor();

        Files.walkFileTree(artifactVersionPath, artifactVersionDirectoryVisitor);

        for (Path filePath : artifactVersionDirectoryVisitor.getMatchingPaths())
        {
            String unixBasedFilePath = FilenameUtils.separatorsToUnix(filePath.toString());
            Gav gav = M2_GAV_CALCULATOR.pathToGav(unixBasedFilePath);

            Artifact artifact = new DefaultArtifact(gav.getGroupId(),
                                                    gav.getArtifactId(),
                                                    gav.getVersion(),
                                                    null,
                                                    gav.getExtension(),
                                                    gav.getClassifier(),
                                                    new DefaultArtifactHandler(gav.getExtension()));

            String name = filePath.toFile().getName();

            SnapshotVersion snapshotVersion = MetadataHelper.createSnapshotVersion(artifact,
                                                                                   FilenameUtils.getExtension(name));

            snapshotVersions.add(snapshotVersion);
        }

        if (!snapshotVersions.isEmpty())
        {
            Collections.sort(snapshotVersions, new SnapshotVersionComparator());
        }

        return snapshotVersions;
    }

    public Versioning generateVersioning(List<MetadataVersion> versions)
    {
        Versioning versioning = new Versioning();

        if (!versions.isEmpty())
        {
            // Sort versions naturally (1.1 < 1.2 < 1.3 ...)
            Collections.sort(versions, new MetadataVersionComparator());
            for (MetadataVersion version : versions)
            {
                versioning.addVersion(version.getVersion());
            }

            // Sort versions naturally but consider creation date as well so that
            // 1.1 < 1.2 < 1.4 < 1.3 (1.3 is considered latest release because it was changed recently)
            // TODO: Sort this out as part of SB-333
            //Collections.sort(versions);
        }

        return versioning;
    }

    public Versioning generateSnapshotVersions(List<SnapshotVersion> snapshotVersionList)
    {
        Versioning versioning = new Versioning();

        if (!snapshotVersionList.isEmpty())
        {
            versioning.setSnapshotVersions(snapshotVersionList);
        }

        return versioning;
    }

    private boolean artifactIsPlugin(Model model)
    {
        return model.getPackaging().equals("maven-plugin");
    }

    private Model getPom(Path filePath)
            throws IOException, XmlPullParserException
    {
        try (Reader rr = new FileReader(filePath.toFile()))
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read(rr);
        }

    }

}
