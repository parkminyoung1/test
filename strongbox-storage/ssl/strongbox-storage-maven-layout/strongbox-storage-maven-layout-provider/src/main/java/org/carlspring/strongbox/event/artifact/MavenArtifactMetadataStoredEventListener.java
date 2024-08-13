package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactMetadataStoredEventListener
        extends BaseMavenArtifactEventListener
{

    @Inject
    private Maven2LayoutProvider maven2LayoutProvider;

    /**
     * Why not @{@link org.carlspring.strongbox.event.AsyncEventListener}:
     * <p>
     * Consider call to group repository for expired maven-metadata.xml.
     * Then all underlying sub-repositories will fetch their maven-metadata.xml.
     * In case at least one sub-repository was a proxy repository and its maven-metadata.xml local copy expired, we will re-fetch the maven-metadata.xml from remote.
     * Then this listener will be invoked and it will update all groups containing this proxy repository.
     * We need to have updated initial group when we return from the initial call.
     * Async update could be done a bit later.
     */
    @EventListener
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        final Repository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_METADATA_STORED.getType())
        {
            return;
        }

        if (!maven2LayoutProvider.requiresGroupAggregation(event.getPath()))
        {
            return;
        }

        updateMetadataInGroupsContainingRepository(event, RepositoryPath::getParent);
    }

}
