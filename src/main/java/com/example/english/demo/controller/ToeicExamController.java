package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ToeicExamCreateRequest;
import com.example.english.demo.dto.request.ToeicExamUpdateRequest;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.service.FileUploadService;
import com.example.english.demo.service.ToeicExamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.english.demo.dto.request.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/toeic-exam")
@Slf4j
public class ToeicExamController {

    private final ToeicExamService toeicExamService;

    private final FileUploadService fileUploadService;
//
//    @PostMapping("/create")
//    public ApiResponse<ToeicExamResponse> createToeicExam(
//            @RequestParam("exam") String examJson,
//            @RequestParam("audio") MultipartFile audioFile) throws IOException {
//
//        // Tạo ApiResponse
//        ApiResponse<ToeicExamResponse> apiResponse = new ApiResponse<>();
//
//        // Xử lý tải lên tệp audio
//        String audioFileName = fileUploadService.uploadAudioFile(audioFile);
//
//        // Chuyển đổi examJson từ JSON thành đối tượng ToeicExamCreateRequest
//        ObjectMapper objectMapper = new ObjectMapper();
//        ToeicExamCreateRequest examRequest = objectMapper.readValue(examJson, ToeicExamCreateRequest.class);
//
//        examRequest.setAudio(audioFileName);
//
//        ToeicExamResponse response = toeicExamService.createToeicExam(examRequest);
//
//        apiResponse.setResult(response);
//
//        return apiResponse;
//    }


//    @PostMapping("/create")
//    public ApiResponse<ToeicExamResponse> createToeicExam(@RequestBody ToeicExamCreateRequest toeicExamCreateRequest) {
//        ApiResponse<ToeicExamResponse> apiResponse=new ApiResponse<ToeicExamResponse>();
//        apiResponse.setResult(toeicExamService.createToeicExam(toeicExamCreateRequest));
//        return apiResponse;
//    }


    @PutMapping("/update/{examId}")
    public ApiResponse<ToeicExamResponse> updateToeicExam(@PathVariable("examId") Long examId, @RequestBody ToeicExamUpdateRequest toeicExamUpdateRequest) {
        ApiResponse<ToeicExamResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(toeicExamService.updateToeicExam(examId, toeicExamUpdateRequest));
        return apiResponse;
    }


}