package org.carlspring.strongbox.cron.jobs.fields;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CronJobStringTypeField
        extends CronJobTypeField
{

    public CronJobStringTypeField()
    {
        this(null);
    }

    public CronJobStringTypeField(CronJobField field)
    {
        super(field);
    }

    @Override
    public String getValue()
    {
        return String.class.getSimpleName().toLowerCase();
    }
}
