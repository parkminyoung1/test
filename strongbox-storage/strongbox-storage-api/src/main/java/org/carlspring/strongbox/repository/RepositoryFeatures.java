package org.carlspring.strongbox.repository;

import java.util.Set;

/**
 * @author carlspring
 */
public interface RepositoryFeatures
{

    /**
     * Returns the default list of artifact coordinate validators.
     *
     * @return
     */
    Set<String> getDefaultArtifactCoordinateValidators();

}
