package org.carlspring.strongbox.storage.validation.artifact;

/**
 * @author carlspring
 */
public class ArtifactCoordinatesValidationException extends Exception
{

    public ArtifactCoordinatesValidationException()
    {
    }

    public ArtifactCoordinatesValidationException(String message)
    {
        super(message);
    }

    public ArtifactCoordinatesValidationException(String message,
                                                  Throwable cause)
    {
        super(message, cause);
    }

    public ArtifactCoordinatesValidationException(Throwable cause)
    {
        super(cause);
    }

    public ArtifactCoordinatesValidationException(String message,
                                                  Throwable cause,
                                                  boolean enableSuppression,
                                                  boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
