package org.carlspring.strongbox.mapper;

import org.carlspring.strongbox.forms.configuration.CustomRepositoryConfigurationForm;
import org.carlspring.strongbox.yaml.ObjectMapperSubtypes;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
public class WebObjectMapperSubtypes
        extends ObjectMapperSubtypes
{

    public static final WebObjectMapperSubtypes INSTANCE = new WebObjectMapperSubtypes();

    private static final Set<Class<?>> ADDITIONAL_TYPES = ImmutableSet.of(CustomRepositoryConfigurationForm.class);

    private WebObjectMapperSubtypes()
    {

    }

    @Override
    protected Set<Class<?>> getTypes()
    {
        ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
        builder.addAll(super.getTypes());
        builder.addAll(ADDITIONAL_TYPES);
        return builder.build();
    }
}
