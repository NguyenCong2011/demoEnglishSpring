package com.example.english.demo.dto.request;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefeshRequest {
    private String token;
}
