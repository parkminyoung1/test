package org.carlspring.strongbox.artifact;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.File;

import org.apache.maven.artifact.Artifact;

/**
 * @author Przemyslaw Fusik
 */
public interface MavenArtifact
        extends Artifact
{

    @Deprecated
    @Override
    default File getFile()
    {
        throw new UnsupportedOperationException("Use getPath instead");
    }

    @Deprecated
    @Override
    default void setFile(File destination)
    {
        throw new UnsupportedOperationException("Use setPath instead");
    }

    RepositoryPath getPath();

    void setPath(RepositoryPath destination);
    
}
