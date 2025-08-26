package timdev.timdev.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IdentifierValidator.class)
@Documented
public @interface ValidIdentifier {
    String message() default "Invalid identifier. Must be a valid email or username";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
