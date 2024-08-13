package org.carlspring.strongbox.converters.users;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.forms.users.AccessModelForm;
import org.carlspring.strongbox.forms.users.RepositoryAccessModelForm;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.carlspring.strongbox.users.dto.PathPrivilegesDto;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 * @author Przemyslaw Fusik
 */
public enum AccessModelFormToUserAccessModelDtoConverter
        implements Converter<AccessModelForm, AccessModelDto>
{

    INSTANCE;

    @Override
    public AccessModelDto convert(AccessModelForm accessModelForm)
    {
        if (accessModelForm == null)
        {
            return null;
        }
        
        AccessModelDto userAccessModelDto = new AccessModelDto();
        accessModelForm.getApiAccess()
                       .stream()
                       .map(p -> Privileges.valueOf(p))
                       .forEach(p -> userAccessModelDto.getApiAuthorities().add(p));
        
        for (RepositoryAccessModelForm repositoryAccess : accessModelForm.getRepositoriesAccess())
        {
            StoragePrivilegesDto storage = userAccessModelDto.getStorageAuthorities(repositoryAccess.getStorageId())
                                                       .orElseGet(
                                                               () ->
                                                               {
                                                                   StoragePrivilegesDto userStorageDto = new StoragePrivilegesDto();
                                                                   userStorageDto.setStorageId(
                                                                           repositoryAccess.getStorageId());
                                                                   userAccessModelDto.getStorageAuthorities().add(userStorageDto);
                                                                   return userStorageDto;
                                                               });

            RepositoryPrivilegesDto repository = storage.getRepositoryPrivileges(repositoryAccess.getRepositoryId())
                                                  .orElseGet(
                                                          () ->
                                                          {
                                                              RepositoryPrivilegesDto userRepositoryDto = new RepositoryPrivilegesDto();
                                                              userRepositoryDto.setRepositoryId(
                                                                      repositoryAccess.getRepositoryId());
                                                              storage.getRepositoryPrivileges().add(userRepositoryDto);
                                                              return userRepositoryDto;
                                                          });

            if (StringUtils.isBlank(repositoryAccess.getPath()))
            {
                repository.getRepositoryPrivileges().addAll(pullPrivileges(repositoryAccess));
                continue;
            }

            PathPrivilegesDto pathPrivileges = repository.getPathPrivilege(repositoryAccess.getPath(),
                                                                           repositoryAccess.isWildcard())
                                                         .orElseGet(
                                                                    () -> {
                                                                        PathPrivilegesDto pathPrivilegesDto = new PathPrivilegesDto();
                                                                        pathPrivilegesDto.setPath(
                                                                                                  repositoryAccess.getPath());
                                                                        pathPrivilegesDto.setWildcard(
                                                                                                      repositoryAccess.isWildcard());
                                                                        repository.getPathPrivileges()
                                                                                  .add(
                                                                                       pathPrivilegesDto);
                                                                        return pathPrivilegesDto;
                                                                    });
            pathPrivileges.getPrivileges().addAll(pullPrivileges(repositoryAccess));

        }
        return userAccessModelDto;
    }

    private Collection<Privileges> pullPrivileges(final RepositoryAccessModelForm repositoryAccess)
    {
        return repositoryAccess.getPrivileges()
                               .stream()
                               .map(p -> Privileges.valueOf(p))
                               .collect(Collectors.toSet());
    }
}
