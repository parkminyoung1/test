package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class IvyDependencyFormatter
        implements DependencySynonymFormatter
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    public static final String ALIAS = "Ivy";

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @PostConstruct
    @Override
    public void register()
    {
        compatibleDependencyFormatRegistry.addProviderImplementation(getLayout(), getFormatAlias(), this);

        logger.debug("Initialized the Ivy dependency formatter.");
    }

    @Override
    public String getLayout()
    {
        return Maven2LayoutProvider.ALIAS;
    }

    @Override
    public String getFormatAlias()
    {
        return ALIAS;
    }

    @Override
    public String getDependencySnippet(ArtifactCoordinates artifactCoordinates)
    {
        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) artifactCoordinates;

        return "<dependency org=\"" + coordinates.getGroupId() + "\"" +
               " name=\"" + coordinates.getArtifactId() + "\"" +
               " rev=\"" + coordinates.getVersion() + "\" />\n";
    }

}
