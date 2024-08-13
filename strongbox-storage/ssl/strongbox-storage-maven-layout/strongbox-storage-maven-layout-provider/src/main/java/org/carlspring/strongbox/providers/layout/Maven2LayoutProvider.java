package org.carlspring.strongbox.providers.layout;


import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.archive.JarArchiveListingFunction;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributeType;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.maven.index.artifact.M2ArtifactRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("maven2LayoutProvider")
public class Maven2LayoutProvider
        extends AbstractLayoutProvider<MavenArtifactCoordinates>
{

    public static final String ALIAS = MavenArtifactCoordinates.LAYOUT_NAME;

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;


    @PostConstruct
    public void register()
    {
        logger.info("Registered layout provider '{}' with alias '{}'.",
                    getClass().getCanonicalName(), ALIAS);
    }

    protected MavenArtifactCoordinates getArtifactCoordinates(RepositoryPath repositoryPath)
            throws IOException
    {
        MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);

        return new MavenArtifactCoordinates(artifact);
    }

    public boolean isArtifactMetadata(RepositoryPath path)
    {
        return path.getFileName().toString().endsWith(".pom");
    }

    public boolean isMavenMetadata(RepositoryPath path)
    {
        return MetadataHelper.MAVEN_METADATA_XML.equals(path.getFileName().toString());
    }

    @Override
    protected Map<RepositoryFileAttributeType, Object> getRepositoryFileAttributes(RepositoryPath repositoryPath,
                                                                                   RepositoryFileAttributeType... attributeTypes)
            throws IOException
    {
        Map<RepositoryFileAttributeType, Object> result = super.getRepositoryFileAttributes(repositoryPath,
                                                                                            attributeTypes);

        for (RepositoryFileAttributeType attributeType : attributeTypes)
        {
            Object value = result.get(attributeType);
            switch (attributeType)
            {
                case ARTIFACT:
                    value = BooleanUtils.isTrue((Boolean) value) && MavenArtifactUtils.isGAV(repositoryPath);

                    result.put(attributeType, value);

                    break;
                case METADATA:
                    value = BooleanUtils.isTrue((Boolean) value) || isMavenMetadata(repositoryPath);

                    result.put(attributeType, value);

                    break;
                case EXPIRED:
                    final Instant tenSecondsAgo = Instant.now().minus(10, ChronoUnit.SECONDS);
                    value = BooleanUtils.isTrue((Boolean) value) || (isMavenMetadata(repositoryPath)
                                                                     &&
                                                                     !RepositoryFiles.wasModifiedAfter(repositoryPath,
                                                                                                       tenSecondsAgo));

                    result.put(attributeType, value);

                    break;
                default:

                    break;
            }
        }

        return result;
    }

    private boolean isIndex(RepositoryPath path)
    {
        if (!path.isAbsolute())
        {
            return false;
        }
        RepositoryPath indexRoot = path.getFileSystem().getRootDirectory().resolve(MavenRepositoryFeatures.INDEX);
        if (path.startsWith(indexRoot))
        {
            return true;
        }

        return false;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public MavenRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return mavenRepositoryManagementStrategy;
    }

    @Override
    public Set<String> listArchiveFilenames(final RepositoryPath repositoryPath)
    {
        if (JarArchiveListingFunction.INSTANCE.supports(repositoryPath))
        {
            try
            {
                return JarArchiveListingFunction.INSTANCE.listFilenames(repositoryPath);
            }
            catch (IOException e)
            {
                logger.warn("Unable to list filenames in archive path {} using {}",
                            repositoryPath, JarArchiveListingFunction.INSTANCE.getClass(), e);
            }
        }
        return Collections.emptySet();
    }

    public boolean requiresGroupAggregation(final RepositoryPath repositoryPath)
    {
        return isMavenMetadata(repositoryPath) &&
               !M2ArtifactRecognizer.isSnapshot(repositoryPath.getParent().getFileName().toString());
    }
}
