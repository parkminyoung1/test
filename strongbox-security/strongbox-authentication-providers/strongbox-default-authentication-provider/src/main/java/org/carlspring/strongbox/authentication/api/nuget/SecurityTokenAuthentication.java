package org.carlspring.strongbox.authentication.api.nuget;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author Sergey Bespalov
 *
 */
public class SecurityTokenAuthentication extends UsernamePasswordAuthenticationToken
{
    
    public SecurityTokenAuthentication(String principal,
                                       String credentials)
    {
        super(principal, credentials);
    }

    @Override
    public String getCredentials()
    {
        return (String) super.getCredentials();
    }

    @Override
    public String getPrincipal()
    {
        return (String) super.getPrincipal();
    }

    
}
