package org.carlspring.strongbox.forms;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Pablo Tirado
 */
public class RoleListForm
{

    @Valid
    private List<RoleForm> roles;

    public List<RoleForm> getRoles()
    {
        return roles;
    }

    public void setRoles(List<RoleForm> roles)
    {
        this.roles = roles;
    }
}
