package org.carlspring.strongbox.validation.configuration;

import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Przemyslaw Fusik
 */
public class LayoutProviderValueValidator
        implements ConstraintValidator<LayoutProviderValue, String>
{

    private boolean allowNull;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Override
    public void initialize(final LayoutProviderValue constraintAnnotation)
    {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext context)
    {
        if (value == null)
        {
            return allowNull;
        }

        return layoutProviderRegistry.getProviders().keySet().contains(value);
    }
}
