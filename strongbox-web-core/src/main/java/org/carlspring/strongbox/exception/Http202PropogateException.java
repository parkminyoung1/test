/**
 * @author Bogdan Sukonnov
 * 
 */
package org.carlspring.strongbox.exception;

import java.io.IOException;

/**
 * @author Bogdan Sukonnov
 *
 */
public class Http202PropogateException extends IOException
{

    public Http202PropogateException(final String message, IOException ex)
    {
        super(message);
        this.initCause(ex);
    }

}
