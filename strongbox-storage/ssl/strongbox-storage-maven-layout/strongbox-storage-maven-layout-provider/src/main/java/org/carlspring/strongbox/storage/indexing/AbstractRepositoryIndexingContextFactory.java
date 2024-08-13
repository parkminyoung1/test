package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.indexing.local.ArtifactEntryJarFileContentsIndexCreator;
import org.carlspring.strongbox.storage.indexing.local.ArtifactEntryMinimalArtifactInfoIndexCreator;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.index.context.IndexCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public abstract class AbstractRepositoryIndexingContextFactory
        implements RepositoryIndexingContextFactory
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public RepositoryCloseableIndexingContext create(final Repository repository)
            throws IOException
    {

        final RepositoryPath indexRepositoryPath = getRepositoryIndexDirectoryPathResolver().resolve(repository);

        final RepositoryCloseableIndexingContext indexingContext = new RepositoryCloseableIndexingContext(
                Indexer.INSTANCE.createIndexingContext(getIndexingContextId(repository),
                                                       repository.getId(),
                                                       indexRepositoryPath.resolve(
                                                               ".cache").toFile(),
                                                       indexRepositoryPath.toFile(),
                                                       getRepositoryUrl(repository),
                                                       null,
                                                       true,
                                                       true,
                                                       getIndexCreators(repository)),
                repository);

        return indexingContext;
    }

    protected String getRepositoryUrl(Repository repository)
    {
        return null;
    }

    protected abstract RepositoryIndexDirectoryPathResolver getRepositoryIndexDirectoryPathResolver();

    protected String getIndexingContextId(final Repository repository)
    {
        return repository.getStorage().getId() + ":" + repository.getId();
    }

    protected List<IndexCreator> getIndexCreators(final Repository repository)
    {
        final List<IndexCreator> indexCreators = new ArrayList<>();
        indexCreators.add(ArtifactEntryMinimalArtifactInfoIndexCreator.INSTANCE);
        final MavenRepositoryConfiguration repositoryConfiguration = (MavenRepositoryConfiguration) repository.getRepositoryConfiguration();
        if (repositoryConfiguration != null && repositoryConfiguration.isIndexingClassNamesEnabled())
        {
            indexCreators.add(ArtifactEntryJarFileContentsIndexCreator.INSTANCE);
        }
        return indexCreators;
    }

}
