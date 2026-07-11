package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "password must be at least 8 characters and include an uppercase letter, "
            + "a lowercase letter, a digit, and a special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
