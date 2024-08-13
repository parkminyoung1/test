package org.carlspring.strongbox.providers.io;

import java.io.IOException;

/**
 * @author Przemyslaw Fusik
 */
public interface ExpiredRepositoryPathHandler
{

    default boolean supports(RepositoryPath repositoryPath)
            throws IOException
    {
        return true;
    }

    void handleExpiration(RepositoryPath repositoryPath)
            throws IOException;

}
