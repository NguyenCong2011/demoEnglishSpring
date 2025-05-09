package com.example.english.demo.repository;

import com.example.english.demo.entity.CompetitionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionResultRepository extends JpaRepository<CompetitionResult, Long> {

    List<CompetitionResult> findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
            Long examId1, String user1Id1, String user2Id1,
            Long examId2, String user1Id2, String user2Id2
    );

    @Query("SELECT c FROM CompetitionResult c WHERE c.exam.examId = :examId AND (c.user1.id = :userId OR c.user2.id = :userId)")
    List<CompetitionResult> findByExamAndUser(@Param("examId") Long examId, @Param("userId") String userId);
}
