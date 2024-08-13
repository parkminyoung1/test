package org.carlspring.strongbox.client;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class RemoteRepositoryRetryArtifactDownloadConfiguration
{

    private final int timeoutSeconds;

    private final int maxNumberOfAttempts;

    private final int minAttemptsIntervalSeconds;

    public RemoteRepositoryRetryArtifactDownloadConfiguration(final MutableRemoteRepositoryRetryArtifactDownloadConfiguration delegate)
    {
        this.timeoutSeconds = delegate.getTimeoutSeconds();
        this.maxNumberOfAttempts = delegate.getMaxNumberOfAttempts();
        this.minAttemptsIntervalSeconds = delegate.getMinAttemptsIntervalSeconds();
    }

    public int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public int getMaxNumberOfAttempts()
    {
        return maxNumberOfAttempts;
    }

    public int getMinAttemptsIntervalSeconds()
    {
        return minAttemptsIntervalSeconds;
    }
}
