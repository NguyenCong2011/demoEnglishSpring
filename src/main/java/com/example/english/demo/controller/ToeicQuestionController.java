package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.ToeicQuestionCreateRequest;
import com.example.english.demo.dto.request.ToeicQuestionUpdateRequest;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.service.ToeicQuestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/toeic-question")
@Slf4j
public class ToeicQuestionController {
    private final ToeicQuestionService toeicQuestionService;

//    @PostMapping("/create/{examId}")
//    public ApiResponse<List<ToeicQuestionResponse>> createToeicQuestions(
//            @RequestParam("toeicQuestionCreateRequests") String toeicQuestionCreateRequestsJson,
//            @PathVariable Long examId,
//            @RequestParam(value = "images", required = false) MultipartFile[] images) {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        List<ToeicQuestionCreateRequest> toeicQuestionCreateRequests = null;
//        try {
//            toeicQuestionCreateRequests = objectMapper.readValue(toeicQuestionCreateRequestsJson, new TypeReference<List<ToeicQuestionCreateRequest>>(){});
//        } catch (IOException e) {
//            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
//        }
//
//        ApiResponse<List<ToeicQuestionResponse>> apiResponse = new ApiResponse<>();
//        List<ToeicQuestionResponse> toeicQuestionResponses = toeicQuestionService.createToeicQuestions(toeicQuestionCreateRequests, examId, images);
//        apiResponse.setResult(toeicQuestionResponses);
//        return apiResponse;
//    }


    @PutMapping("/update/{questionId}")
    public ApiResponse<ToeicQuestionResponse> updateToeicQuestion(@PathVariable Long questionId, @RequestBody ToeicQuestionUpdateRequest request) {
        ApiResponse<ToeicQuestionResponse> apiResponse = new ApiResponse<>();
        ToeicQuestionResponse response = toeicQuestionService.updateToeicQuestion(questionId, request);
        apiResponse.setResult(response);
        return apiResponse;
    }
}
