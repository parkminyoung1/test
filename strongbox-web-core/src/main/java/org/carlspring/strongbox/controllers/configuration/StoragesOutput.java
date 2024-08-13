package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.Views;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * @author Przemyslaw Fusik
 */
@JsonRootName("storages")
public class StoragesOutput
{

    @JsonView(Views.ShortStorage.class)
    private List<Storage> storages;

    public StoragesOutput()
    {
    }

    public StoragesOutput(final List<Storage> storages)
    {
        this.storages = storages;
    }

    public List<Storage> getStorages()
    {
        return storages;
    }

    public void setStorages(final List<Storage> storages)
    {
        this.storages = storages;
    }
}
