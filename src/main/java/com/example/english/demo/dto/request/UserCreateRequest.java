package com.example.english.demo.dto.request;

import com.example.english.demo.validator.DobConstraint;
import com.example.english.demo.validator.PasswordCheck.PasswordValid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @Size(min=3,message = "USERNAME_INVALID")
    private String username;

    @Size(min=4,message = "INVALID_PASWORD")
    private String password;

    @DobConstraint(min=16,message = "INVALID_DOB")
    private LocalDate dob;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;
}
