package com.example.english.demo.dto.response;

import com.example.english.demo.entity.Course;
import com.example.english.demo.entity.User;
import com.example.english.demo.enums.PaymentStatus;

import java.time.LocalDateTime;

public class CoursePaymentResponse {
    private Integer id;
    private String order;
    private Float amount;
    private Boolean success;
    private LocalDateTime createdAt;
    private User user;
    private Course course;
    private PaymentStatus status;

}
