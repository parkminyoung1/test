package org.carlspring.strongbox.validation.configuration;

import org.carlspring.strongbox.api.Describable;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Przemyslaw Fusik
 */
@Documented
@Constraint(validatedBy = {})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DescribableEnumValue
{

    String message();

    Class<? extends Describable> type();

    boolean allowNull() default true;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
