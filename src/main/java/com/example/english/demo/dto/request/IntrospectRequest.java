package com.example.english.demo.dto.request;
//class này dùng để verify lại token đã tạo nhé
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectRequest {
    private String token;
}
