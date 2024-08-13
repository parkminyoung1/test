package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.client.MutableRemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.configuration.*;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.*;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.carlspring.strongbox.storage.routing.MutableRoutingRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author mtodorov
 */
@Service
public class ConfigurationManagementServiceImpl
        implements ConfigurationManagementService
{

    private final ReadWriteLock configurationLock = new ReentrantReadWriteLock();

    @Inject
    private ConfigurationFileManager configurationFileManager;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private PlatformTransactionManager transactionManager;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #configurationLock} here
     * and should not be exposed to the world.
     *
     * @see #getConfiguration()
     */
    private MutableConfiguration configuration;

    @PostConstruct
    public void init()
    {
        new TransactionTemplate(transactionManager).execute((s) -> doInit());
    }

    private Object doInit()
    {
        MutableConfiguration configuration;
        try
        {
            configuration = configurationFileManager.read();
            setConfiguration(configuration);
            setRepositoryArtifactCoordinateValidators();
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        return null;
    }

    @Override
    public MutableConfiguration getMutableConfigurationClone()
    {
        final Lock readLock = configurationLock.readLock();
        readLock.lock();

        try
        {
            return SerializationUtils.clone(configuration);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public Configuration getConfiguration()
    {
        final Lock readLock = configurationLock.readLock();
        readLock.lock();

        try
        {
            return new Configuration(configuration);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void setConfiguration(MutableConfiguration newConf) throws IOException
    {
        Objects.requireNonNull(newConf, "Configuration cannot be null");

        modifyInLock(configuration -> {
            ConfigurationManagementServiceImpl.this.configuration = newConf;
            try
            {
                setProxyRepositoryConnectionPoolConfigurations();
                setRepositoryStorageRelationships();
                setAllows();
            }
            catch (IOException e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    @Override
    public void setInstanceName(String instanceName) throws IOException
    {
        modifyInLock(configuration -> configuration.setInstanceName(instanceName));
    }

    @Override
    public void setBaseUrl(String baseUrl) throws IOException
    {
        modifyInLock(configuration -> configuration.setBaseUrl(baseUrl));
    }

    @Override
    public void setPort(int port) throws IOException
    {
        modifyInLock(configuration -> configuration.setPort(port));
    }

    @Override
    public void setProxyConfiguration(String storageId,
                                      String repositoryId,
                                      MutableProxyConfiguration proxyConfiguration) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         if (storageId != null && repositoryId != null)
                         {
                             configuration.getStorage(storageId)
                                          .getRepository(repositoryId)
                                          .setProxyConfiguration(proxyConfiguration);
                         }
                         else
                         {
                             configuration.setProxyConfiguration(proxyConfiguration);
                         }
                     });
    }

    @Override
    public void saveStorage(StorageDto storage) throws IOException
    {
        modifyInLock(configuration -> configuration.addStorage(storage));
    }
    
    @Override
    public void addStorageIfNotExists(StorageDto storage) throws IOException
    {
        modifyInLock(configuration -> configuration.addStorageIfNotExist(storage));
    }

    @Override
    public void removeStorage(String storageId) throws IOException
    {
        modifyInLock(configuration -> configuration.getStorages().remove(storageId));
    }

    @Override
    public void saveRepository(String storageId,
                               RepositoryDto repository) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         final StorageDto storage = configuration.getStorage(storageId);
                         repository.setStorage(storage);
                         storage.addRepository(repository);

                         if (repository.isEligibleForCustomConnectionPool())
                         {
                             proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                                     repository.getRemoteRepository().getUrl(),
                                     repository.getHttpConnectionPool().getAllocatedConnections());
                         }
                     });
    }

    @Override
    public void removeRepositoryFromAssociatedGroups(String storageId,
                                                     String repositoryId) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         List<Repository> includedInGroupRepositories = getConfiguration().getGroupRepositoriesContaining(
                                 storageId, repositoryId);

                         if (!includedInGroupRepositories.isEmpty())
                         {
                             for (Repository repository : includedInGroupRepositories)
                             {
                                 configuration.getStorage(repository.getStorage().getId())
                                              .getRepository(repository.getId())
                                              .getGroupRepositories().remove(repositoryId);
                             }
                         }
                     });
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId) throws IOException
    {
        modifyInLock(configuration -> {
            configuration.getStorage(storageId).removeRepository(repositoryId);
            try
            {
                removeRepositoryFromAssociatedGroups(storageId, repositoryId);
            }
            catch (IOException e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    @Override
    public void setProxyRepositoryMaxConnections(String storageId,
                                                 String repositoryId,
                                                 int numberOfConnections) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         RepositoryDto repository = configuration.getStorage(storageId).getRepository(repositoryId);
                         if (repository.getHttpConnectionPool() == null)
                         {
                             repository.setHttpConnectionPool(new MutableHttpConnectionPool());
                         }

                         repository.getHttpConnectionPool().setAllocatedConnections(numberOfConnections);
                     });
    }

    @Override
    public MutableRoutingRules getRoutingRules()
    {
        return getMutableConfigurationClone().getRoutingRules();
    }

    @Override
    public MutableRoutingRule getRoutingRule(UUID uuid)
    {
        return getMutableConfigurationClone().getRoutingRules()
                                             .getRules()
                                             .stream()
                                             .filter(r -> r.getUuid().equals(uuid))
                                             .findFirst()
                                             .orElse(null);
    }

    @Override
    public boolean updateRoutingRule(UUID uuid,
                                     MutableRoutingRule routingRule) throws IOException
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration -> configuration.getRoutingRules()
                                                   .getRules()
                                                   .stream()
                                                   .filter(r -> r.getUuid().equals(uuid))
                                                   .findFirst()
                                                   .ifPresent(r -> result.setValue(r.updateBy(routingRule))));

        return result.isTrue();
    }

    @Override
    public boolean addRoutingRule(MutableRoutingRule routingRule) throws IOException
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         routingRule.setUuid(UUID.randomUUID());
                         result.setValue(configuration.getRoutingRules()
                                                      .getRules()
                                                      .add(routingRule));
                     });

        return result.isTrue();
    }

    @Override
    public boolean removeRoutingRule(UUID uuid) throws IOException
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         configuration.getRoutingRules()
                                      .getRules()
                                      .stream()
                                      .filter(r -> r.getUuid().equals(uuid))
                                      .findFirst()
                                      .ifPresent(r -> result.setValue(configuration.getRoutingRules()
                                                                                   .getRules()
                                                                                   .remove(r)));

                     });

        return result.isTrue();
    }

    @Override
    public void addRepositoryToGroup(String storageId,
                                     String repositoryId,
                                     String repositoryGroupMemberId) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         final RepositoryDto repository = configuration.getStorage(storageId)
                                                                           .getRepository(repositoryId);
                         repository.addRepositoryToGroup(repositoryGroupMemberId);
                     });
    }

    private void setAllows() throws IOException
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, StorageDto> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (StorageDto storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (Repository repository : storage.getRepositories().values())
                                     {
                                        RepositoryDto mutableRepository = (RepositoryDto)repository;
                                        if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
                                         {
                                             mutableRepository.setAllowsDelete(false);
                                             mutableRepository.setAllowsDeployment(false);
                                             mutableRepository.setAllowsRedeployment(false);
                                         }
                                         if (repository.getType().equals(RepositoryTypeEnum.PROXY.getType()))
                                         {
                                             mutableRepository.setAllowsDeployment(false);
                                             mutableRepository.setAllowsRedeployment(false);
                                         }
                                     }
                                 }
                             }
                         }
                     }, false);
    }

    /**
     * Sets the repository <--> storage relationships explicitly, as initially, when these are deserialized from the
     * XML, they have no such relationship.
     * @throws IOException
     */
    private void setRepositoryStorageRelationships() throws IOException
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, StorageDto> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (StorageDto storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (Repository repository : storage.getRepositories().values())
                                     {
                                         ((RepositoryDto)repository).setStorage(storage);
                                     }
                                 }
                             }
                         }
                     }, false);
    }

    @Override
    public void setRepositoryArtifactCoordinateValidators() throws IOException
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, StorageDto> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (StorageDto storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (Repository repository : storage.getRepositories().values())
                                     {
                                         LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(
                                                 repository.getLayout());

                                         // Generally, this should not happen. However, there are at least two cases where it may occur:
                                         // 1) During testing -- various modules are not added as dependencies and a layout provider
                                         //    is thus not registered.
                                         // 2) Syntax error, or some other mistake leading to an incorrectly defined layout
                                         //    for a repository.
                                         if (layoutProvider != null)
                                         {
                                             @SuppressWarnings("unchecked")
                                             Set<String> defaultArtifactCoordinateValidators = layoutProvider.getDefaultArtifactCoordinateValidators();
                                             if ((repository.getArtifactCoordinateValidators() == null ||
                                                  (repository.getArtifactCoordinateValidators() != null &&
                                                   repository.getArtifactCoordinateValidators().isEmpty())) &&
                                                 defaultArtifactCoordinateValidators != null)
                                             {
                                                 ((RepositoryDto)repository).setArtifactCoordinateValidators(defaultArtifactCoordinateValidators);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     });
    }

    @Override
    public void putInService(final String storageId,
                             final String repositoryId) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setStatus(RepositoryStatusEnum.IN_SERVICE.getStatus());

                         RepositoryEvent event = new RepositoryEvent(storageId,
                                                                     repositoryId,
                                                                     RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_IN_SERVICE
                                                                             .getType());

                         repositoryEventListenerRegistry.dispatchEvent(event);
                     });
    }

    @Override
    public void putOutOfService(final String storageId,
                                final String repositoryId) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setStatus(RepositoryStatusEnum.OUT_OF_SERVICE.getStatus());

                         RepositoryEvent event = new RepositoryEvent(storageId,
                                                                     repositoryId,
                                                                     RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_OUT_OF_SERVICE
                                                                             .getType());

                         repositoryEventListenerRegistry.dispatchEvent(event);
                     });
    }

    @Override
    public void setArtifactMaxSize(final String storageId,
                                   final String repositoryId,
                                   final long value) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setArtifactMaxSize(value);
                     });
    }

    @Override
    public void set(final MutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         configuration.getRemoteRepositoriesConfiguration()
                                      .setRetryArtifactDownloadConfiguration(
                                              remoteRepositoryRetryArtifactDownloadConfiguration);
                     });
    }

    @Override
    public void addRepositoryArtifactCoordinateValidator(final String storageId,
                                                         final String repositoryId,
                                                         final String alias) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId).getRepository(
                                 repositoryId).getArtifactCoordinateValidators().add(alias);
                     });
    }

    @Override
    public boolean removeRepositoryArtifactCoordinateValidator(final String storageId,
                                                               final String repositoryId,
                                                               final String alias) throws IOException
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(config ->
                     {
                         result.setValue(config.getStorage(storageId).getRepository(
                                 repositoryId).getArtifactCoordinateValidators().remove(alias));
                     });

        return result.isTrue();
    }

    @Override
    public void setCorsAllowedOrigins(final List<String> allowedOrigins) throws IOException
    {
        modifyInLock(configuration ->
                     {
                         ArrayList origins;

                         if (CollectionUtils.isEmpty(allowedOrigins))
                         {
                             origins = new ArrayList<>();
                         }
                         else
                         {
                             origins = new ArrayList<>(allowedOrigins);
                         }

                         configuration.getCorsConfiguration()
                                      .setAllowedOrigins(origins);
                     });
    }

    private void setProxyRepositoryConnectionPoolConfigurations() throws IOException
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorages().values().stream()
                                      .filter(storage -> MapUtils.isNotEmpty(storage.getRepositories()))
                                      .flatMap(storage -> storage.getRepositories().values().stream())
                                      .map(r -> (RepositoryDto) r)
                                      .filter(RepositoryDto::isEligibleForCustomConnectionPool)
                                      .forEach(repository -> proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(repository.getRemoteRepository()
                                                                                                                                             .getUrl(),
                                                                                                                                   repository.getHttpConnectionPool()
                                                                                                                                             .getAllocatedConnections()));
                     }, false);
    }

    @Override
    public void setSmtpSettings(MutableSmtpConfiguration smtpConfiguration) throws IOException
    {
        modifyInLock(configuration -> {
            configuration.getSmtpConfiguration().setHost(smtpConfiguration.getHost());
            configuration.getSmtpConfiguration().setPort(smtpConfiguration.getPort());
            configuration.getSmtpConfiguration().setConnection(smtpConfiguration.getConnection());
            configuration.getSmtpConfiguration().setUsername(smtpConfiguration.getUsername());
            configuration.getSmtpConfiguration().setPassword(smtpConfiguration.getPassword());
        });
    }

    private void modifyInLock(final Consumer<MutableConfiguration> operation) throws IOException
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<MutableConfiguration> operation,
                              final boolean storeInFile) throws IOException
    {
        final Lock writeLock = configurationLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(configuration);

            if (storeInFile)
            {
                configurationFileManager.store(configuration);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

}
