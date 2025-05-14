package com.example.english.demo.service;

import com.example.english.demo.entity.Course;
import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.entity.User;
import com.example.english.demo.repository.CoursePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoursePaymentService {

    private final CoursePaymentRepository coursePaymentRepository;

    public CoursePayment savePayment(User user, Course course, Float amount, String orderId) {
        CoursePayment payment = CoursePayment.builder()
                .user(user)
                .course(course)
                .amount(amount)
                .orderId(orderId)
                .success(true)
                .createdAt(LocalDateTime.now())
                .build();
        return coursePaymentRepository.save(payment);
    }

    public Optional<CoursePayment> getPaymentByOrderId(String orderId) {
        return coursePaymentRepository.findByOrderId(orderId);
    }

}
