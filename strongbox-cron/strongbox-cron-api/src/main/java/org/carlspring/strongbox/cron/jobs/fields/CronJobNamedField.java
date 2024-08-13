package org.carlspring.strongbox.cron.jobs.fields;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CronJobNamedField
        extends CronJobField
{

    private final String name;

    public CronJobNamedField(String name)
    {
        this(null, name);
    }

    public CronJobNamedField(CronJobField field,
                             String name)
    {
        super(field);
        this.name = name;
    }

    @Override
    public String getKey()
    {
        return "name";
    }

    @Override
    public String getValue()
    {
        return name;
    }

    @Override
    public String getName()
    {
        return getValue();
    }
}
