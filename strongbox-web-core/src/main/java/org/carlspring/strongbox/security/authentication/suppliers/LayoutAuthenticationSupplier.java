package org.carlspring.strongbox.security.authentication.suppliers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.Order;
import static org.carlspring.strongbox.web.Constants.ARTIFACT_ROOT_PATH;

@Order(3)
public abstract class LayoutAuthenticationSupplier
        implements AuthenticationSupplier
{

    @Inject
    private ConfigurationManager configurationManager;

    private String layoutAlias;

    public LayoutAuthenticationSupplier(String layoutAlias)
    {
        this.layoutAlias = layoutAlias;
    }

    @Override
    public boolean supports(@Nonnull HttpServletRequest request)
    {
        String uri = request.getRequestURI();
        if (!uri.startsWith(ARTIFACT_ROOT_PATH))
        {
            return false;
        }

        String[] pathParts = uri.split("/");
        if (pathParts.length < 4)
        {
            return false;
        }

        String storageId = pathParts[2];
        String repositoryId = pathParts[3];
        if (storageId == null || repositoryId == null)
        {
            return false;
        }

        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            return false;
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            return false;
        }

        return layoutAlias.equals(repository.getLayout());
    }
}
