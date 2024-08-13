package org.carlspring.strongbox.event.repository;

import org.carlspring.strongbox.event.Event;

/**
 * @author mtodorov
 */
public class RepositoryEvent extends Event
{

    private String storageId;
    private String repositoryId;
    
    public RepositoryEvent(String storageId,
                           String repositoryId,
                           int type)
    {
        super(type);
        this.storageId = storageId;
        this.repositoryId = repositoryId;
    }

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId(String storageId)
    {
        this.storageId = storageId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

}
