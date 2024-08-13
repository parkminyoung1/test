package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.repository.RawRepositoryFeatures;

import javax.inject.Inject;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class RawRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private RawRepositoryFeatures rawRepositoryFeatures;


    @Override
    public RepositoryDto createRepository(String repositoryId)
    {
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setLayout(RawLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(
                new LinkedHashSet<>(rawRepositoryFeatures.getDefaultArtifactCoordinateValidators()));

        return repository;
    }

}
