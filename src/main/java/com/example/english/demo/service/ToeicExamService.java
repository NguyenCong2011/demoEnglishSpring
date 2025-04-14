package com.example.english.demo.service;

import com.example.english.demo.dto.request.ToeicExamCreateRequest;
import com.example.english.demo.dto.request.ToeicExamUpdateRequest;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.mapper.ToeicExamMapper;
import com.example.english.demo.repository.ToeicExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ToeicExamService {
    private final ToeicExamMapper toeicExamMapper;
    private final ToeicExamRepository toeicExamRepository;

    public ToeicExamResponse createToeicExam(ToeicExamCreateRequest toeicExamCreateRequest){
        if(toeicExamRepository.existsByExamName(toeicExamCreateRequest.getExamName())){
            throw new AppException(ErrorCode.TOEIC_EXAM_EXITSTED);
        }
        ToeicExam toeicExam=toeicExamMapper.toToeicExam(toeicExamCreateRequest);

        toeicExam=toeicExamRepository.save(toeicExam);

        return toeicExamMapper.toToeicExamResponse(toeicExam);
    }

    public ToeicExamResponse updateToeicExam(Long examId, ToeicExamUpdateRequest toeicExamUpdateRequest) {
        log.info("in method update toeic exam  alllll");
        ToeicExam toeicExam = toeicExamRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
        toeicExamMapper.updateToeicExam(toeicExam, toeicExamUpdateRequest);
        ToeicExam updatedToeicExam=toeicExamRepository.save(toeicExam);
        return toeicExamMapper.toToeicExamResponse(updatedToeicExam);
    }

    public Page<ToeicExamResponse> getToeicExams(int page) {
        Pageable pageable = PageRequest.of(page-1, 9); // 9 items per page
        var toeicExams = toeicExamRepository.findAll(pageable); // Get paginated TOEIC exams


        List<ToeicExamResponse> examResponses = toeicExams.getContent().stream()
                .map(toeicExamMapper::toToeicExamResponse) // Map entity to DTO
                .collect(Collectors.toList());

        // Chuyển từ List thành Page
        return new PageImpl<>(examResponses, pageable, toeicExams.getTotalElements());
    }




}
