package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.storage.repository.MutableHttpConnectionPool;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Przemyslaw Fusik
 */
public enum RepositoryFormConverter
        implements Converter<RepositoryForm, RepositoryDto>
{
    INSTANCE;

    @Override
    public RepositoryDto convert(final RepositoryForm source)
    {
        RepositoryDto result = new RepositoryDto();
        result.setId(source.getId());
        result.setPolicy(source.getPolicy());
        result.setStorageProvider(source.getStorageProvider());
        result.setLayout(source.getLayout());
        result.setType(source.getType());
        result.setSecured(source.isSecured());
        result.setStatus(source.getStatus());
        result.setArtifactMaxSize(source.getArtifactMaxSize());
        result.setTrashEnabled(source.isTrashEnabled());
        result.setAllowsForceDeletion(source.isAllowsForceDeletion());
        result.setAllowsDeployment(source.isAllowsDeployment());
        result.setAllowsRedeployment(source.isAllowsRedeployment());
        result.setAllowsDelete(source.isAllowsDelete());
        result.setAllowsDirectoryBrowsing(source.isAllowsDirectoryBrowsing());
        result.setChecksumHeadersEnabled(source.isChecksumHeadersEnabled());
        if (source.getRepositoryConfiguration() != null)
        {
            result.setRepositoryConfiguration(
                    source.getRepositoryConfiguration().accept(CustomRepositoryConfigurationFormConverter.INSTANCE));
        }
        if (source.getProxyConfiguration() != null)
        {
            result.setProxyConfiguration(ProxyConfigurationFormConverter.INSTANCE.convert(
                    source.getProxyConfiguration()));
        }
        if (source.getRemoteRepository() != null)
        {
            result.setRemoteRepository(
                    RemoteRepositoryFormConverter.INSTANCE.convert(source.getRemoteRepository()));
        }
        if (source.getHttpConnectionPool() != null)
        {
            MutableHttpConnectionPool httpConnectionPool = new MutableHttpConnectionPool();
            httpConnectionPool.setAllocatedConnections(source.getHttpConnectionPool());
            result.setHttpConnectionPool(httpConnectionPool);
        }
        if (source.getGroupRepositories() != null)
        {
            result.setGroupRepositories(source.getGroupRepositories());
        }
        if (source.getArtifactCoordinateValidators() != null)
        {
            result.setArtifactCoordinateValidators(source.getArtifactCoordinateValidators());
        }
        result.setBasedir(source.getBasedir());
        return result;
    }
}
