package org.carlspring.strongbox.event.artifact;

import java.nio.file.Path;

import org.carlspring.strongbox.event.RepositoryBasedEvent;

/**
 * @author mtodorov
 */
public class ArtifactEvent<T extends Path> extends RepositoryBasedEvent<T>
{

    private T targetPath;

    public ArtifactEvent(T sourcePath,
                         int type)
    {
        super(sourcePath, type);
    }

    public ArtifactEvent(T sourcePath,
                         T targetPath,
                         int type)
    {
        super(sourcePath, type);
        this.targetPath = targetPath;
    }

    public T getTargetPath()
    {
        return targetPath;
    }

    public void setTargetPath(T targetPath)
    {
        this.targetPath = targetPath;
    }

}
