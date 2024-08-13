package org.carlspring.strongbox.storage.validation.artifact.version;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 * @author Przemyslaw Fusik
 */
@Component
public class GenericReleaseVersionValidator
        implements ArtifactCoordinatesValidator
{

    private static final Logger logger = LoggerFactory.getLogger(GenericReleaseVersionValidator.class);

    public static final String ALIAS = "generic-release-version-validator";

    public static final String DESCRIPTION = "Generic release version validator";

    @Inject
    private ArtifactCoordinatesValidatorRegistry artifactCoordinatesValidatorRegistry;


    @PostConstruct
    @Override
    public void register()
    {
        artifactCoordinatesValidatorRegistry.addProvider(ALIAS, this);

        logger.info("Registered artifact coordinates validator '{}' with alias '{}'.",
                    getClass().getCanonicalName(), ALIAS);
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean supports(Repository repository)
    {
        return repository.getArtifactCoordinateValidators().contains(ALIAS);
    }

    @Override
    public boolean supports(String layoutProvider)
    {
        return true;
    }

    @Override
    public void validate(Repository repository,
                         ArtifactCoordinates coordinates)
            throws VersionValidationException
    {
        String version = coordinates.getVersion();
        if (isRelease(version) && !repository.acceptsReleases())
        {
            throw new VersionValidationException("Cannot deploy a release artifact to a repository which " +
                                                 "does not accept release policy!");
        }
        if (!isRelease(version) && repository.acceptsReleases() && !repository.acceptsSnapshots())
        {
            throw new VersionValidationException("Cannot deploy a snapshot artifact to a repository with " +
                                                 "a release policy!");
        }
    }

    public boolean isRelease(String version)
    {
        return StringUtils.isNotBlank(version) && !StringUtils.endsWithIgnoreCase(version, "-SNAPSHOT");
    }

}
