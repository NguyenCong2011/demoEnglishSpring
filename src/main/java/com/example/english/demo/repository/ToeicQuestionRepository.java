package com.example.english.demo.repository;

import com.example.english.demo.entity.ToeicQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToeicQuestionRepository extends JpaRepository<ToeicQuestion,Long> {
    boolean existsByQuestionText(String questionText);

    boolean existsByCorrectAnswer(String correctAnswer);

    List<ToeicQuestion> findByToeicExam_ExamIdAndPart(Long examId, Integer part);

}
