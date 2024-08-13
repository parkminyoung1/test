package org.carlspring.strongbox.validation.configuration;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.METHOD,
          ElementType.FIELD,
          ElementType.ANNOTATION_TYPE,
          ElementType.CONSTRUCTOR,
          ElementType.PARAMETER,
          ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ShouldNotContain
{

    String message();

    String[] strings();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
