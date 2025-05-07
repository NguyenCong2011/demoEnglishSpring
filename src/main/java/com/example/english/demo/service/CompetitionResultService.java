package com.example.english.demo.service;

import com.example.english.demo.entity.CompetitionResult;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.User;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.CompetitionResultRepository;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionResultService {

    private final CompetitionResultRepository competitionResultRepository;
    private final UserRepository userRepository;
    private final ToeicExamRepository toeicExamRepository;

    @Transactional
    public void createOrUpdateCompetitionResult(Long examId, String user1Id, String user2Id, String userId, int score) {
        Optional<CompetitionResult> resultOptional = competitionResultRepository.findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
                examId, user1Id, user2Id,
                examId, user2Id, user1Id
        );

        CompetitionResult result;
        if (resultOptional.isPresent()) {
            result = resultOptional.get();
            log.info("✅ Found existing CompetitionResult for examId {} and users {} and {}", examId, user1Id, user2Id);
        } else {
            // This case should ideally not happen if CompetitionResult is created when invite is accepted
            log.warn("⚠️ CompetitionResult not found for examId {} and users {} and {}. Creating a new one.", examId, user1Id, user2Id);
            User user1 = userRepository.findById(user1Id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
            User user2 = userRepository.findById(user2Id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
            ToeicExam exam = toeicExamRepository.findById(examId).orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

            result = new CompetitionResult();
            result.setExam(exam);
            result.setUser1(user1);
            result.setUser2(user2);
            result.setUser1Score(0); // Initialize scores
            result.setUser2Score(0); // Initialize scores
        }

        if (result.getUser1() != null && result.getUser1().getId().equals(userId)) {
            result.setUser1Score(score);
            log.info("✅ Set user1Score = {} for userId {}", score, userId);
        } else if (result.getUser2() != null && result.getUser2().getId().equals(userId)) {
            result.setUser2Score(score);
            log.info("✅ Set user2Score = {} for userId {}", score, userId);
        } else {
            log.error("❌ User ID {} not matched in CompetitionResult for examId {} and users {} and {}", userId, examId, user1Id, user2Id);
            return;
        }

        competitionResultRepository.save(result);
        log.info("💾 Saved CompetitionResult for examId {} and users {} and {}", examId, user1Id, user2Id);
    }
}
