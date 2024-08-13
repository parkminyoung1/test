package org.carlspring.strongbox.users.domain;

/**
 * Defines the restricted system roles. The purpose is to restrict changing or
 * deleting some of the configured roles.
 * 
 * @author sbespalov
 */
public enum SystemRole
{
    ADMIN,
    UI_MANAGER,
    REPOSITORY_MANAGER,
    ARTIFACTS_MANAGER,
    ANONYMOUS;

}
