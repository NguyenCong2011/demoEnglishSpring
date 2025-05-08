package com.example.english.demo.dto.request;

import com.example.english.demo.validator.DobConstraint;
import com.example.english.demo.validator.PasswordCheck.PasswordValid;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String password;
    private String firstName;
    private String lastName;

    @DobConstraint(min=18,message = "INVALID_DOB")
    private LocalDate dob;

    List<String> roles;
}
