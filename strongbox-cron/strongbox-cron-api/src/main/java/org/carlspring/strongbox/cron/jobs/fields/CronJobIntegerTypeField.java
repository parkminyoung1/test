package org.carlspring.strongbox.cron.jobs.fields;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CronJobIntegerTypeField
        extends CronJobTypeField
{

    public CronJobIntegerTypeField()
    {
        this(null);
    }

    public CronJobIntegerTypeField(CronJobField field)
    {
        super(field);
    }

    @Override
    public String getValue()
    {
        return int.class.getSimpleName();
    }
}
