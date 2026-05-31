package com.example.english.demo.service;

import com.example.english.demo.dto.request.CoursePaymentRequest;
import com.example.english.demo.dto.response.CoursePaymentResponse;
import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.mapper.CoursePaymentMapper;
import com.example.english.demo.repository.CoursePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CoursePaymentService {

    private final CoursePaymentRepository coursePaymentRepository;
    private final CoursePaymentMapper coursePaymentMapper;


    public CoursePaymentResponse createCoursePayment(CoursePaymentRequest request) {
        CoursePayment coursePayment = coursePaymentMapper.toCoursePayment(request);

        CoursePayment savedPayment = coursePaymentRepository.save(coursePayment);

        return coursePaymentMapper.toCoursePaymentResponse(savedPayment);
    }
}
