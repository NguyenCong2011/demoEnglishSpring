package com.example.english.demo.repository;

import com.example.english.demo.entity.CoursePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CoursePaymentRepository extends JpaRepository<CoursePayment, Long> {
    Optional<CoursePayment> findByOrderId(String orderId);
}
