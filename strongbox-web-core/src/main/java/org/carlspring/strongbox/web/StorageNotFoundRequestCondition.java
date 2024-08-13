package org.carlspring.strongbox.web;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

import static org.carlspring.strongbox.web.Constants.STORAGE_NOT_FOUND_REQUEST_ATTRIBUTE;

/**
 * @author Przemyslaw Fusik
 */
public class StorageNotFoundRequestCondition
        extends ExposableRequestCondition
{

    private final String storageId;

    public StorageNotFoundRequestCondition(String storageId)
    {

        this.storageId = storageId;
    }

    @Override
    protected void expose(HttpServletRequest request)
    {
        request.setAttribute(STORAGE_NOT_FOUND_REQUEST_ATTRIBUTE, storageId);
    }

    @Override
    protected Collection<?> getContent()
    {
        return Collections.singletonList(storageId);
    }

    @Override
    protected String getToStringInfix()
    {
        return storageId;
    }
}
