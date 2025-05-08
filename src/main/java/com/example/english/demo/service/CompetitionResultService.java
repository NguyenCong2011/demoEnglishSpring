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

import java.util.List;
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
        List<CompetitionResult> results = competitionResultRepository.findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
                examId, user1Id, user2Id,
                examId, user2Id, user1Id
        );

        CompetitionResult resultToUpdate = null;

        if (!results.isEmpty()) {
            if (results.size() > 1) {
                log.warn("⚠️ Found multiple CompetitionResult entries for examId {} and users {} and {}. Searching for the correct one to update.", examId, user1Id, user2Id);
            }
            // Iterate through results to find the specific entry for this user pair
            for (CompetitionResult res : results) {
                if ((res.getUser1().getId().equals(user1Id) && res.getUser2().getId().equals(user2Id)) ||
                    (res.getUser1().getId().equals(user2Id) && res.getUser2().getId().equals(user1Id))) {
                    resultToUpdate = res;
                    log.info("✅ Found specific CompetitionResult to update for examId {} and users {} and {}", examId, user1Id, user2Id);
                    break; // Found the correct one, exit loop
                }
            }
        }

        if (resultToUpdate == null) {
             // This case should ideally not happen if CompetitionResult is created when invite is accepted
            log.warn("⚠️ CompetitionResult not found for examId {} and users {} and {}. Creating a new one.", examId, user1Id, user2Id);
            User user1 = userRepository.findById(user1Id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
            User user2 = userRepository.findById(user2Id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
            ToeicExam exam = toeicExamRepository.findById(examId).orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

            resultToUpdate = new CompetitionResult();
            resultToUpdate.setExam(exam);
            resultToUpdate.setUser1(user1);
            resultToUpdate.setUser2(user2);
            resultToUpdate.setUser1Score(0); // Initialize scores
            resultToUpdate.setUser2Score(0); // Initialize scores
        }


        if (resultToUpdate.getUser1() != null && resultToUpdate.getUser1().getId().equals(userId)) {
            resultToUpdate.setUser1Score(score);
            log.info("✅ Set user1Score = {} for userId {}", score, userId);
        } else if (resultToUpdate.getUser2() != null && resultToUpdate.getUser2().getId().equals(userId)) {
            resultToUpdate.setUser2Score(score);
            log.info("✅ Set user2Score = {} for userId {}", score, userId);
        } else {
            log.error("❌ User ID {} not matched in CompetitionResult for examId {} and users {} and {}", userId, examId, user1Id, user2Id);
            return;
        }

        competitionResultRepository.save(resultToUpdate);
        log.info("💾 Saved CompetitionResult for examId {} and users {} and {}", examId, user1Id, user2Id);
    }
}
