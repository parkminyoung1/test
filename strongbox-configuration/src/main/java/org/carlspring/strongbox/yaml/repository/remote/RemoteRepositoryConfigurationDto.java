package org.carlspring.strongbox.yaml.repository.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Pablo Tirado
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class RemoteRepositoryConfigurationDto
        implements CustomRemoteRepositoryConfiguration
{

    @JsonIgnore
    public abstract CustomRemoteRepositoryConfigurationData getImmutable();

}
