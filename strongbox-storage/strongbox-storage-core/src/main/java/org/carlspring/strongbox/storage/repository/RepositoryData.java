package org.carlspring.strongbox.storage.repository;

import static org.carlspring.strongbox.util.CustomStreamCollectors.toLinkedHashMap;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.json.MapValuesJsonSerializer;
import org.carlspring.strongbox.json.StringArrayToMapJsonDeserializer;
import org.carlspring.strongbox.storage.StorageData;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryDto;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryData;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.repository.RepositoryConfiguration;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
@JsonIgnoreProperties(value = {"storageId"}, allowGetters = true)
public class RepositoryData
        implements Repository
{

    private String id;

    private String basedir;

    private String policy;

    private String storageProvider;

    private String layout;

    private String type;

    private boolean secured;

    private String status;

    private long artifactMaxSize;

    private boolean trashEnabled;

    private boolean allowsForceDeletion;

    private boolean allowsDeployment;

    private boolean allowsRedeployment;

    private boolean allowsDelete;

    private boolean allowsDirectoryBrowsing;

    private boolean checksumHeadersEnabled;

    private ProxyConfiguration proxyConfiguration;

    private RemoteRepositoryData remoteRepository;

    private HttpConnectionPool httpConnectionPool;

    private List<CustomConfiguration> customConfigurations;

    private CustomRepositoryConfiguration repositoryConfiguration;

    @JsonSerialize(using = MapValuesJsonSerializer.class)
    @JsonDeserialize(using = StringArrayToMapJsonDeserializer.class)
    private Map<String, String> groupRepositories;

    @JsonSerialize(using = MapValuesJsonSerializer.class)
    @JsonDeserialize(using = StringArrayToMapJsonDeserializer.class)
    private Map<String, String> artifactCoordinateValidators;

    @JsonIgnore
    private Storage storage;

    RepositoryData()
    {

    }

    public RepositoryData(final Repository delegate)
    {
        this(delegate, null);
    }

    public RepositoryData(final Repository delegate,
                          final Storage storage)
    {
        this.id = delegate.getId();
        this.policy = delegate.getPolicy();
        this.storageProvider = delegate.getStorageProvider();
        this.layout = delegate.getLayout();
        this.type = delegate.getType();
        this.secured = delegate.isSecured();
        this.status = delegate.getStatus();
        this.artifactMaxSize = delegate.getArtifactMaxSize();
        this.trashEnabled = delegate.isTrashEnabled();
        this.allowsForceDeletion = delegate.allowsForceDeletion();
        this.allowsDeployment = delegate.allowsDeployment();
        this.allowsRedeployment = delegate.allowsRedeployment();
        this.allowsDelete = delegate.allowsDeletion();
        this.allowsDirectoryBrowsing = delegate.allowsDirectoryBrowsing();
        this.checksumHeadersEnabled = delegate.isChecksumHeadersEnabled();

        RepositoryDto mutableRepository = (RepositoryDto)delegate;
        this.proxyConfiguration = immuteProxyConfiguration(mutableRepository.getProxyConfiguration());
        this.remoteRepository = immuteRemoteRepository(mutableRepository.getRemoteRepository());
        this.httpConnectionPool = immuteHttpConnectionPool(mutableRepository.getHttpConnectionPool());
        this.customConfigurations = immuteCustomConfigurations(mutableRepository.getCustomConfigurations());
        this.repositoryConfiguration = immuteCustomRepositoryConfiguration(mutableRepository.getRepositoryConfiguration());

        this.groupRepositories = immuteGroupRepositories(delegate.getGroupRepositories());
        this.artifactCoordinateValidators = immuteArtifactCoordinateValidators(
                delegate.getArtifactCoordinateValidators());
        this.storage = storage != null ? storage : immuteStorage(delegate.getStorage());
        this.basedir = delegate.getBasedir();
    }

    private ProxyConfiguration immuteProxyConfiguration(final MutableProxyConfiguration source)
    {
        return source != null ? new ProxyConfiguration(source) : null;
    }

    private RemoteRepositoryData immuteRemoteRepository(final RemoteRepositoryDto source)
    {
        return source != null ? new RemoteRepositoryData(source) : null;
    }

    private Map<String, String> immuteGroupRepositories(final Set<String> source)
    {
        return source != null ? ImmutableMap.copyOf(source.stream().collect(toLinkedHashMap(e -> e, e -> e))) :
               Collections.emptyMap();
    }

    private Storage immuteStorage(final Storage source)
    {
        return source != null ? new StorageData(source) : null;
    }

    private HttpConnectionPool immuteHttpConnectionPool(final MutableHttpConnectionPool source)
    {
        return source != null ? new HttpConnectionPool(source) : null;
    }

    private List<CustomConfiguration> immuteCustomConfigurations(final List<MutableCustomConfiguration> source)
    {
        return source != null ?
               ImmutableList.copyOf(source.stream().map(MutableCustomConfiguration::getImmutable).collect(
                       Collectors.toList())) : Collections.emptyList();
    }

    private CustomRepositoryConfiguration immuteCustomRepositoryConfiguration(final CustomRepositoryConfigurationDto source)
    {
        return source != null ? source.getImmutable() : null;
    }


    private Map<String, String> immuteArtifactCoordinateValidators(final Set<String> source)
    {
        return source != null ? ImmutableMap.copyOf(source.stream().collect(toLinkedHashMap(e -> e, e -> e))) :
               Collections.emptyMap();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getBasedir()
    {
        return basedir;
    }

    @Override
    public String getPolicy()
    {
        return policy;
    }

    @Override
    public String getStorageProvider()
    {
        return storageProvider;
    }

    @Override
    public String getLayout()
    {
        return layout;
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public boolean isSecured()
    {
        return secured;
    }

    @Override
    public String getStatus()
    {
        return status;
    }

    @Override
    public long getArtifactMaxSize()
    {
        return artifactMaxSize;
    }

    @Override
    public boolean isTrashEnabled()
    {
        return trashEnabled;
    }

    @Override
    public boolean allowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    @Override
    public boolean allowsDeployment()
    {
        return allowsDeployment;
    }

    @Override
    public boolean allowsRedeployment()
    {
        return allowsRedeployment;
    }

    @Override
    public boolean allowsDeletion()
    {
        return allowsDelete;
    }

    @Override
    public boolean allowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    @Override
    public boolean isChecksumHeadersEnabled()
    {
        return checksumHeadersEnabled;
    }

    public ProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public RemoteRepositoryData getRemoteRepository()
    {
        return remoteRepository;
    }

    public HttpConnectionPool getHttpConnectionPool()
    {
        return httpConnectionPool;
    }

    public List<CustomConfiguration> getCustomConfigurations()
    {
        return customConfigurations;
    }

    public RepositoryConfiguration getRepositoryConfiguration()
    {
        return repositoryConfiguration;
    }

    @Override
    @JsonIgnore
    public String getStorageIdAndRepositoryId()
    {
        StringJoiner storageAndRepositoryId = new StringJoiner(":");

        if (StringUtils.isNotBlank(getStorage().getId()))
        {
            storageAndRepositoryId.add(getStorage().getId());
        }

        if (StringUtils.isNotBlank(getId()))
        {
            storageAndRepositoryId.add(getId());
        }

        return storageAndRepositoryId.toString();
    }

    @Override
    public boolean isType(String compareType)
    {
        return type.equalsIgnoreCase(compareType);
    }

    @Override
    public Set<String> getGroupRepositories()
    {
        return groupRepositories.keySet();
    }

    @Override
    public Set<String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators.keySet();
    }

    @Override
    public Storage getStorage()
    {
        return storage;
    }

    /**
     * This field is mainly used in the UI so don't remove it!
     */
    @JsonGetter("storageId")
    public String getStorageId()
    {
        return this.storage != null ? this.storage.getId() : null;
    }

    @Override
    public boolean isHostedRepository()
    {
        return RepositoryTypeEnum.HOSTED.getType().equals(type);
    }

    @Override
    public boolean isProxyRepository()
    {
        return RepositoryTypeEnum.PROXY.getType().equals(type);
    }

    @Override
    public boolean isGroupRepository()
    {
        return RepositoryTypeEnum.GROUP.getType().equals(type);
    }

    @Override
    public boolean isInService()
    {
        return RepositoryStatusEnum.IN_SERVICE.getStatus().equalsIgnoreCase(getStatus());
    }

    @Override
    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsSnapshots();
    }

    @Override
    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsReleases();
    }

}
