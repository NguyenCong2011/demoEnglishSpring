package com.example.english.demo.dto.request;

import com.example.english.demo.validator.DobConstraint;
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

    private String firstName;
    private String lastName;

    @DobConstraint(min=16,message = "INVALID_DOB")
    private LocalDate dob;
}
