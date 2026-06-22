package com.epam.finaltask.vallidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$");
    }
}
