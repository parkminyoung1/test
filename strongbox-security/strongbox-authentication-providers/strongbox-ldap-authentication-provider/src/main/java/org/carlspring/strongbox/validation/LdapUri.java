package org.carlspring.strongbox.validation;

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
@Constraint(validatedBy = LdapUriValidator.class)
@Target({ ElementType.FIELD,
          ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LdapUri
{

    String message() default "must be a valid URI";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
