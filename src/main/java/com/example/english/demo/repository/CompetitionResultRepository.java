package com.example.english.demo.repository;

import com.example.english.demo.entity.CompetitionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetitionResultRepository extends JpaRepository<CompetitionResult, String> {

    Optional<CompetitionResult> findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
            Long examId1, String user1Id1, String user2Id1,
            Long examId2, String user1Id2, String user2Id2
    );
}
