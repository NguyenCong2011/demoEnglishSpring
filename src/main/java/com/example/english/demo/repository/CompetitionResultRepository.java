package com.example.english.demo.repository;

import com.example.english.demo.entity.CompetitionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionResultRepository extends JpaRepository<CompetitionResult, String> {

    List<CompetitionResult> findByExam_ExamIdAndUser1_IdOrExam_ExamIdAndUser2_Id(Long examId, String id, Long examId1, String id1);

    CompetitionResult findByRoomId(String roomId);
}
