package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;
import org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategyType;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName(Maven2LayoutProvider.ALIAS)
public class MavenRepositoryConfigurationDto
        extends CustomRepositoryConfigurationDto implements MavenRepositoryConfiguration
{

    private boolean indexingEnabled = false;

    private boolean indexingClassNamesEnabled = true;

    // defaults to once daily at 2 am
    private String cronExpression = "0 0 2 * * ?";

    private String metadataExpirationStrategy = MetadataExpirationStrategyType.CHECKSUM.describe();

    @Override
    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public void setIndexingEnabled(boolean indexingEnabled)
    {
        this.indexingEnabled = indexingEnabled;
    }

    @Override
    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }

    public void setIndexingClassNamesEnabled(final boolean indexingClassNamesEnabled)
    {
        this.indexingClassNamesEnabled = indexingClassNamesEnabled;
    }

    @Override
    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    @Override
    public String getMetadataExpirationStrategy()
    {
        return metadataExpirationStrategy;
    }

    public void setMetadataExpirationStrategy(String metadataExpirationStrategy)
    {
        this.metadataExpirationStrategy = metadataExpirationStrategy;
    }

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new MavenRepositoryConfigurationData(this);
    }
}
