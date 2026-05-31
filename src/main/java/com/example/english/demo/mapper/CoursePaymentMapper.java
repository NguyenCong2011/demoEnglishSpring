package com.example.english.demo.mapper;

import com.example.english.demo.dto.request.CoursePaymentRequest;
import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.response.CoursePaymentResponse;
import com.example.english.demo.dto.response.CourseResponseDTO;
import com.example.english.demo.entity.Course;
import com.example.english.demo.entity.CoursePayment;
import com.example.english.demo.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoursePaymentMapper {
    CoursePayment toCoursePayment(CoursePaymentRequest request);
    CoursePaymentResponse toCoursePaymentResponse(CoursePayment coursePayment);
}
