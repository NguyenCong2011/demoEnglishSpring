package com.example.english.demo.mapper;

import com.example.english.demo.dto.request.ToeicExamCreateRequest;
import com.example.english.demo.dto.request.ToeicExamUpdateRequest;
import com.example.english.demo.dto.response.ToeicExamResponse;
import com.example.english.demo.entity.ToeicExam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ToeicExamMapper {
    ToeicExam toToeicExam(ToeicExamCreateRequest toeicExamCreateRequest);

    ToeicExamResponse toToeicExamResponse(ToeicExam toeicExam);

    void updateToeicExam(@MappingTarget ToeicExam toeicExam, ToeicExamUpdateRequest toeicExamUpdateRequest);
}
