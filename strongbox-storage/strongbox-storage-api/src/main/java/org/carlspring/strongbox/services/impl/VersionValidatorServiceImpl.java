package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.services.VersionValidatorService;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("versionValidatorService")
public class VersionValidatorServiceImpl
        implements VersionValidatorService
{

    @Inject
    private Set<ArtifactCoordinatesValidator> versionValidators = new LinkedHashSet<>();


    public VersionValidatorServiceImpl()
    {
    }

    @Override
    public Set<ArtifactCoordinatesValidator> getVersionValidators()
    {
        return versionValidators;
    }

    public void setVersionValidators(Set<ArtifactCoordinatesValidator> versionValidators)
    {
        this.versionValidators = versionValidators;
    }

}
