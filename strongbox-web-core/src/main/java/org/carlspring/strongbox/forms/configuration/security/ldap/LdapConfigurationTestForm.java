package org.carlspring.strongbox.forms.configuration.security.ldap;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.carlspring.strongbox.authentication.api.ldap.LdapConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfigurationTestForm
{

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @NotNull
    @Valid
    private LdapConfiguration configuration;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(final String password)
    {
        this.password = password;
    }

    public LdapConfiguration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(LdapConfiguration configuration)
    {
        this.configuration = configuration;
    }

}
