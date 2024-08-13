package org.carlspring.strongbox.web;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

import static org.carlspring.strongbox.web.Constants.REPOSITORY_NOT_FOUND_REQUEST_ATTRIBUTE;

/**
 * @author Przemyslaw Fusik
 */
public class RepositoryNotFoundRequestCondition
        extends ExposableRequestCondition
{

    private final String repositoryId;

    public RepositoryNotFoundRequestCondition(String repositoryId)
    {

        this.repositoryId = repositoryId;
    }

    @Override
    protected void expose(HttpServletRequest request)
    {
        request.setAttribute(REPOSITORY_NOT_FOUND_REQUEST_ATTRIBUTE, repositoryId);
    }

    @Override
    protected Collection<?> getContent()
    {
        return Collections.singletonList(repositoryId);
    }

    @Override
    protected String getToStringInfix()
    {
        return repositoryId;
    }
}
