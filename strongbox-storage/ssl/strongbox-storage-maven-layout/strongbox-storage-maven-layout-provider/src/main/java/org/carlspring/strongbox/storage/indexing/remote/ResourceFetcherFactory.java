package org.carlspring.strongbox.storage.indexing.remote;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.index.updater.ResourceFetcher;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ResourceFetcherFactory
{

    public ResourceFetcher createIndexResourceFetcher(String repositoryBaseUrl,
                                                      CloseableHttpClient client)
    {
        return new IndexResourceFetcher(repositoryBaseUrl, client);
    }
}
