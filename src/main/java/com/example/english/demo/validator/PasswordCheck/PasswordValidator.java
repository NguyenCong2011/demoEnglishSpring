package com.example.english.demo.validator.PasswordCheck;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordValid,String> {
  private static final String PASSWORD_PATTERN =
          "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    return password != null && password.matches(PASSWORD_PATTERN);
  }
}
