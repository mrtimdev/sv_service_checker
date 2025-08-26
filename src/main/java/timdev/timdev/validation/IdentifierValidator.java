package timdev.timdev.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdentifierValidator implements ConstraintValidator<ValidIdentifier, String> {

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private static final String USERNAME_REGEX =
            "^[a-zA-Z0-9._-]{3,20}$"; // customize username rules here

    @Override
    public boolean isValid(String identifier, ConstraintValidatorContext context) {
        if (identifier == null) {
            return false;
        }
        return isValidEmail(identifier) || isValidUsername(identifier);
    }

    private boolean isValidEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }

    private boolean isValidUsername(String username) {
        return username.matches(USERNAME_REGEX);
    }
}
