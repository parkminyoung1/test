package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.api.Describable;

/**
 * @author mtodorov
 */
public enum RepositoryTypeEnum
        implements Describable
{

    HOSTED("hosted"),

    PROXY("proxy"),

    GROUP("group"),

    VIRTUAL("virtual"); // Unsupported


    private String type;


    RepositoryTypeEnum(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String describe()
    {
        return getType();
    }
}
