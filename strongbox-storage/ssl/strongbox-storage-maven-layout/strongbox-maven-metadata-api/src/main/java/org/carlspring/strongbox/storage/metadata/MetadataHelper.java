package org.carlspring.strongbox.storage.metadata;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;

/**
 * @author mtodorov
 */
public class MetadataHelper
{

    public static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    public static final String MAVEN_METADATA_XML_CHECKSUM_MD5 = "maven-metadata.xml.md5";

    public static final String MAVEN_METADATA_XML_CHECKSUM_SHA1 = "maven-metadata.xml.sha1";

    private static SimpleDateFormat LAST_UPDATED_FIELD_FORMATTER;


    private MetadataHelper()
    {
    }

    public static SimpleDateFormat getDateFormatInstance()
    {
        if (LAST_UPDATED_FIELD_FORMATTER == null)
        {
            LAST_UPDATED_FIELD_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
        }

        return LAST_UPDATED_FIELD_FORMATTER;
    }

    public static void setLastUpdated(Versioning versioning)
    {
        if (versioning != null)
        {
            versioning.setLastUpdated(getDateFormatInstance().format(Calendar.getInstance().getTime()));
        }
    }

    public static void setLatest(Metadata metadata)
    {
        setLatest(metadata, null);
    }

    /**
     * Sets the "latest" field.
     *
     * @param metadata          The metadata to apply this to.
     * @param currentLatest     Only pass this in, if this is delete mode, otherwise
     *                          this method will figure things out on it's own.
     */
    public static void setLatest(Metadata metadata, String currentLatest)
    {
        if (metadata.getVersioning() == null)
        {
            metadata.setVersioning(new Versioning());
        }
        Versioning versioning = metadata.getVersioning();

        List<String> versions = versioning.getVersions();

        sortVersions(versions);

        if (currentLatest != null &&
            versioning.getLatest() != null && versioning.getLatest().equals(currentLatest))
        {
            // Delete mode:
            if (versions.size() > 1)
            {
                // TODO: Is this the right thing to do?
                versioning.setLatest(versions.get(versions.size() - 2));
            }
            else
            {
                // TODO: Figure out what we should do in case there are no other available versions
            }
        }
        else
        {
            // Regular mode
            String newLatest = versions.isEmpty() ? null : versions.get(versions.size() - 1);
            versioning.setLatest(newLatest);
        }
    }

    /**
     * @param metadata          The metadata to apply this to.
     */
    public static void setRelease(Metadata metadata)
    {
        setRelease(metadata, null);
    }

    /**
     * Sets the "release" field.
     *
     * @param metadata          The metadata to apply this to.
     * @param currentRelease    Only pass this in, if this is delete mode, otherwise
     *                          this method will figure things out on it's own.
     */
    public static void setRelease(Metadata metadata, String currentRelease)
    {
        Versioning versioning = metadata.getVersioning() != null ? metadata.getVersioning() : new Versioning();
        if (metadata.getVersioning() == null)
        {
            metadata.setVersioning(versioning);
        }

        List<String> versions = versioning.getVersions();

        sortVersions(versions);

        if (currentRelease != null &&
            versioning.getRelease()!= null && versioning.getRelease().equals(currentRelease))
        {
            // Delete mode:
            if (versions.size() > 1)
            {
                versioning.setRelease(versions.get(versions.size() - 2));
            }
            else
            {
                // TODO: Figure out what we should do in case there are no other available versions
            }
        }
        else
        {
            // Regular mode
            String newRelease = versions.isEmpty() ? null : versions.get(versions.size() - 1);
            versioning.setRelease(newRelease);
        }
    }

    private static void sortVersions(List<String> versions)
    {
        // Sort the versions in order to set <release> by figuring out the most recent upload
        if (versions != null)
        {
            Collections.sort(versions);
        }
    }

    public static SnapshotVersion createSnapshotVersion(String groupId,
                                                        String artifactId,
                                                        String version,
                                                        String classifier,
                                                        String extension)
    {

        Artifact artifact = new DefaultArtifact(groupId, artifactId, version, null, extension, classifier,
                                                new DefaultArtifactHandler(extension));
        return createSnapshotVersion(artifact, extension);
    }

    public static SnapshotVersion createSnapshotVersion(Artifact artifact,
                                                        String extension)
    {
        SnapshotVersion snapshotVersion = new SnapshotVersion();
        snapshotVersion.setVersion(artifact.getVersion());
        snapshotVersion.setExtension(extension);
        snapshotVersion.setUpdated(getDateFormatInstance().format(Calendar.getInstance().getTime()));

        if (artifact.getClassifier() != null)
        {
            snapshotVersion.setClassifier(artifact.getClassifier());
        }

        return snapshotVersion;
    }

    public static void setupSnapshotVersioning(Versioning snapshotVersioning)
    {
        if (!snapshotVersioning.getSnapshotVersions().isEmpty())
        {
            SnapshotVersion latestSnapshot = snapshotVersioning.getSnapshotVersions().get(
                    snapshotVersioning.getSnapshotVersions().size() - 1);

            SnapshotVersionDecomposition snapshotVersionDecomposition = SnapshotVersionDecomposition.of(
                    latestSnapshot.getVersion());

            if (!SnapshotVersionDecomposition.INVALID.equals(snapshotVersionDecomposition))
            {
                Snapshot snapshot = new Snapshot();

                snapshot.setTimestamp(snapshotVersionDecomposition.getTimestamp());
                snapshot.setBuildNumber(snapshotVersionDecomposition.getBuildNumber());

                snapshotVersioning.setSnapshot(snapshot);
            }
        }
    }

    public static boolean containsTimestampedSnapshotVersion(Metadata metadata,
                                                             String timestampedSnapshotVersion)
    {
        return containsTimestampedSnapshotVersion(metadata, timestampedSnapshotVersion, null);
    }

    public static boolean containsTimestampedSnapshotVersion(Metadata metadata,
                                                             String timestampedSnapshotVersion,
                                                             String classifier)
    {
        for (SnapshotVersion snapshotVersion : metadata.getVersioning().getSnapshotVersions())
        {
            if (snapshotVersion.getVersion().equals(timestampedSnapshotVersion) 
                && classifier == null || snapshotVersion.getClassifier().equals(classifier))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean containsVersion(Metadata metadata,
                                          String version)
    {
        return metadata.getVersioning().getVersions().contains(version);
    }

    /**
     * Returns artifact metadata File
     *
     * @param artifactBasePath Path
     * @return File
     */
    public static Path getMetadataPath(Path artifactBasePath)
            throws FileNotFoundException
    {
        if (Files.exists(artifactBasePath))
        {
            return artifactBasePath.resolve(MAVEN_METADATA_XML);
        }
        else
        {
            throw new FileNotFoundException(String.format("Path %s does not exist", artifactBasePath.toString()));
        }
    }

    public static Path getArtifactMetadataPath(Path artifactBasePath)
    {
        return getMetadataPath(artifactBasePath, null, MetadataType.ARTIFACT_ROOT_LEVEL);
    }

    public static Path getSnapshotMetadataPath(Path artifactBasePath, String version)
    {
        return getMetadataPath(artifactBasePath, version, MetadataType.SNAPSHOT_VERSION_LEVEL);
    }

    public static Path getPluginMetadataPath(Path artifactBasePath)
    {
        return getMetadataPath(artifactBasePath, null, MetadataType.PLUGIN_GROUP_LEVEL);
    }

    /**
     * Returns artifact metadata File
     *
     * @param artifactBasePath Path
     * @return File
     */
    public static Path getMetadataPath(Path artifactBasePath, String version, MetadataType metadataType)
    {
        switch (metadataType)
        {
            case PLUGIN_GROUP_LEVEL:
                return artifactBasePath.getParent().resolve(MAVEN_METADATA_XML);
            case SNAPSHOT_VERSION_LEVEL:
                return artifactBasePath.resolve(version).resolve(MAVEN_METADATA_XML);
            case ARTIFACT_ROOT_LEVEL:
            default:
                return artifactBasePath.resolve(MAVEN_METADATA_XML);
        }
    }

}
