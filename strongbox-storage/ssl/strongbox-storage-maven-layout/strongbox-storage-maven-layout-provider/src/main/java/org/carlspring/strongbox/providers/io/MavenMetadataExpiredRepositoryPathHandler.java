package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.carlspring.strongbox.storage.metadata.maven.ChecksumMetadataExpirationStrategy;
import org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategy;
import org.carlspring.strongbox.storage.metadata.maven.RefreshMetadataExpirationStrategy;
import org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategyType;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

import static org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategy.Decision.*;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenMetadataExpiredRepositoryPathHandler
        implements MavenExpiredRepositoryPathHandler
{

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataExpiredRepositoryPathHandler.class);

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    @Inject
    private ChecksumMetadataExpirationStrategy checksumMetadataExpirationStrategy;

    @Inject
    private RefreshMetadataExpirationStrategy refreshMetadataStrategy;

    @Override
    public boolean supports(final RepositoryPath repositoryPath)
            throws IOException
    {
        if (repositoryPath == null)
        {
            return false;
        }

        if (!RepositoryFiles.isMetadata(repositoryPath))
        {
            return false;
        }

        Repository repository = repositoryPath.getRepository();
        return ((RepositoryData)repository).getRemoteRepository() != null;
    }

    @Override
    public void handleExpiration(final RepositoryPath repositoryPath)
            throws IOException
    {
        MetadataExpirationStrategy metadataExpirationStrategy = getMetadataStrategy(repositoryPath);
        MetadataExpirationStrategy.Decision refetchMetadata = metadataExpirationStrategy.decide(repositoryPath);

        if (refetchMetadata == USABLE)
        {
            return;
        }
        proxyRepositoryArtifactResolver.fetchRemoteResource(repositoryPath);
    }

    private MetadataExpirationStrategy getMetadataStrategy(final RepositoryPath repositoryPath)
    {
        MavenRepositoryConfiguration repositoryConfiguration =
                (MavenRepositoryConfiguration) repositoryPath.getRepository().getRepositoryConfiguration();

        String strategy = Optional.ofNullable(repositoryConfiguration)
                                  .map(MavenRepositoryConfiguration::getMetadataExpirationStrategy)
                                  .orElse(null);

        if (MetadataExpirationStrategyType.REFRESH == MetadataExpirationStrategyType.ofStrategy(strategy))
        {
            return refreshMetadataStrategy;
        }

        return checksumMetadataExpirationStrategy;
    }

}
