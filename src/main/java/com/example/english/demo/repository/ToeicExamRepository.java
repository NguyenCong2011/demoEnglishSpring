package com.example.english.demo.repository;

import com.example.english.demo.entity.ToeicExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToeicExamRepository extends JpaRepository<ToeicExam,Long> {
    boolean existsByExamName(String examName);
    boolean findByExamName(String examName);
}
