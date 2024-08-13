package org.carlspring.strongbox.configuration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
public class SessionConfiguration
{

    private Integer timeoutSeconds;

    SessionConfiguration()
    {

    }


    public SessionConfiguration(final MutableSessionConfiguration delegate)
    {
        this.timeoutSeconds = delegate.getTimeoutSeconds();
    }

    public Integer getTimeoutSeconds()
    {
        return timeoutSeconds;
    }
}
