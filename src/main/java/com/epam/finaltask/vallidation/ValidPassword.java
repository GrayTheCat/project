package com.epam.finaltask.vallidation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password must contain at least one digit, one lowercase, one uppercase letter, and be at least 6 characters long.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
