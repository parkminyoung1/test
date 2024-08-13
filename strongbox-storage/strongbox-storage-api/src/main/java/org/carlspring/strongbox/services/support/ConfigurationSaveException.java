package org.carlspring.strongbox.services.support;

/**
 * @author Przemyslaw Fusik
 */
public class ConfigurationSaveException
        extends ConfigurationException
{

    public ConfigurationSaveException(final Throwable cause)
    {
        super(cause);
    }

    public ConfigurationSaveException(final String message)
    {
        super(message);
    }
}
