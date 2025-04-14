package com.example.english.demo.mapper;

import com.example.english.demo.dto.request.ToeicQuestionCreateRequest;
import com.example.english.demo.dto.request.ToeicQuestionUpdateRequest;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.entity.ToeicQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ToeicQuestionMapper {
    ToeicQuestion toToeicQuestion(ToeicQuestionCreateRequest toeicQuestionCreateRequest);
    ToeicQuestionResponse toToeicQuestionResponse(ToeicQuestion toeicQuestion);
    ToeicQuestionCreateRequest toToeicQuestionCreateRequest(ToeicQuestionUpdateRequest toeicQuestionUpdateRequest);

    void updateToeicQuestion(@MappingTarget ToeicQuestion toeicQuestion, ToeicQuestionUpdateRequest toeicQuestionUpdateRequest);
}
