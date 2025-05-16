package com.example.english.demo.service;

import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.entity.User;
import com.example.english.demo.repository.CoursePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoursePaymentService {

    private final CoursePaymentRepository coursePaymentRepository;

    public Optional<CoursePayment> getPaymentByOrderId(String orderId) {
        return coursePaymentRepository.findByOrderId(orderId);
    }
}
