package org.carlspring.strongbox.dependency.snippet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Declan-Y
 *
 */
@Component
public class BazelDependencyFormatter
        implements DependencySynonymFormatter
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);
    
    public static final String ALIAS = "Bazel";
    
    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;
    
    
    @PostConstruct
    @Override
    public void register()
    {
        compatibleDependencyFormatRegistry.addProviderImplementation(getLayout(), getFormatAlias(), this);
        
        logger.debug("Initialized the Bazel dependency formatter.");
    }
    @Override
    public String getLayout()
    {
        return Maven2LayoutProvider.ALIAS;
    }
    
    
    public String getFormatAlias()
    {
        return ALIAS;
    }
    
    @Override
    public String getDependencySnippet(ArtifactCoordinates artifactCoordinates)
    {
        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) artifactCoordinates;
        
        return "maven_jar(" +"\n"+"    name ="+" "
        +"\""+coordinates.getArtifactId()+"\""+","+
        (coordinates.getArtifactId() != null && coordinates.getGroupId() != null && coordinates.getVersion() != null ? 
        "\n    artifact = " + "\""+
        coordinates.getGroupId()+":"+coordinates.getArtifactId()+":"+coordinates.getVersion()+"\""+",\n)\n": 
        "\n)\n");
    }

}
