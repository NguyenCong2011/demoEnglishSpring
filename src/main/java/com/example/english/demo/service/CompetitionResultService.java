package com.example.english.demo.service;

import com.example.english.demo.entity.CompetitionResult;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.User;
import com.example.english.demo.repository.CompetitionResultRepository;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionResultService {

    private final CompetitionResultRepository competitionResultRepository;
    private final UserRepository userRepository;
    private final ToeicExamRepository toeicExamRepository;

    @Transactional
    public CompetitionResult createOrUpdateCompetitionResult(String roomId, String userId, Long examId, Integer score) {
        CompetitionResult result = competitionResultRepository.findByRoomId(roomId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ToeicExam exam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (result == null) {
            // Create new competition result
            result = new CompetitionResult();
            result.setRoomId(roomId);
            result.setUser1(user);
            result.setExam(exam);
            result.setUser1Score(score);
        } else {
            // Update existing competition result with second user's score
            if (result.getUser1().getId().equals(userId)) {
                result.setUser1Score(score);
            } else {
                result.setUser2(user);
                result.setUser2Score(score);
            }
        }

        return competitionResultRepository.save(result);
    }
} 