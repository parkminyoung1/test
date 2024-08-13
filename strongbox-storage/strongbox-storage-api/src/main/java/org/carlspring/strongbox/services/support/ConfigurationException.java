package org.carlspring.strongbox.services.support;

/**
 * @author Przemyslaw Fusik
 */
public abstract class ConfigurationException
        extends RuntimeException
{

    public ConfigurationException(final Throwable cause)
    {
        super(cause);
    }

    public ConfigurationException(final String message)
    {
        super(message);
    }
}
