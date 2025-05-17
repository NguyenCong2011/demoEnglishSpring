package com.example.english.demo.dto.request;

import com.example.english.demo.entity.Course;
import com.example.english.demo.entity.User;
import com.example.english.demo.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursePaymentRequest {
    private Integer id;
    private String order;
    private Float amount;
    private Boolean success;
    private LocalDateTime createdAt;
    private User user;
    private Course course;
    private PaymentStatus status;

}
