package com.example.english.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreateRequest {
    private String name;
    private String description;
}
