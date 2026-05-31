package com.example.english.demo.repository;

import com.example.english.demo.entity.CoursePayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePaymentRepository extends JpaRepository<CoursePayment, Long> {
}
