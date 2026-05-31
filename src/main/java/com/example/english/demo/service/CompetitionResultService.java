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
        // Lấy thông tin user và đề thi
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
        ToeicExam exam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        // Tìm bản ghi CompetitionResult gần nhất (mới nhất) chưa đủ điểm
        List<CompetitionResult> existingResults = competitionResultRepository
                .findByExam_ExamIdAndUser1_IdAndUser2_IdOrExam_ExamIdAndUser2_IdAndUser1_Id(
                        examId, user1Id, user2Id,
                        examId, user2Id, user1Id
                );

        CompetitionResult resultToUpdate = null;

        for (CompetitionResult result : existingResults) {
            boolean user1IsNull = result.getUser1Score() == null;
            boolean user2IsNull = result.getUser2Score() == null;

            if ((result.getUser1().getId().equals(user1Id) && result.getUser2().getId().equals(user2Id)) ||
                    (result.getUser1().getId().equals(user2Id) && result.getUser2().getId().equals(user1Id))) {

                if ((userId.equals(user1Id) && user1IsNull) || (userId.equals(user2Id) && user2IsNull)) {
                    resultToUpdate = result;
                    break;
                }
            }
        }

        // Nếu không có bản ghi nào phù hợp để cập nhật điểm, tạo mới bản ghi
        if (resultToUpdate == null) {
            resultToUpdate = new CompetitionResult();
            resultToUpdate.setExam(exam);
            resultToUpdate.setUser1(user1);
            resultToUpdate.setUser2(user2);
            resultToUpdate.setUser1Score(null);
            resultToUpdate.setUser2Score(null);
        }

        // Cập nhật điểm cho người thi
        if (resultToUpdate.getUser1() != null && resultToUpdate.getUser1().getId().equals(userId)) {
            resultToUpdate.setUser1Score(score);
            log.info("✅ Set user1Score = {} for userId {}", score, userId);
        } else if (resultToUpdate.getUser2() != null && resultToUpdate.getUser2().getId().equals(userId)) {
            resultToUpdate.setUser2Score(score);
            log.info("✅ Set user2Score = {} for userId {}", score, userId);
        } else {
            log.error("❌ User ID {} not matched in CompetitionResult", userId);
            return;
        }

        // Lưu kết quả
        competitionResultRepository.save(resultToUpdate);
        log.info("💾 Saved CompetitionResult (new or updated)");

        competitionResultRepository.flush();
        log.info("✅ Flushed CompetitionResult to database");
    }
}
