package org.carlspring.strongbox.cron.jobs.fields;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CronJobRequiredField
        extends CronJobField
{

    public CronJobRequiredField()
    {
        this(null);
    }

    public CronJobRequiredField(CronJobField field)
    {
        super(field);
    }

    @Override
    public String getKey()
    {
        return "required";
    }

    @Override
    public String getValue()
    {
        return String.valueOf(true);
    }

    @Override
    public boolean isRequired()
    {
        return true;
    }
}
