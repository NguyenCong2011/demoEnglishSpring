package com.example.english.demo.dto.request;

import com.example.english.demo.entity.ToeicExam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ToeicQuestionUpdateRequest {
//    private String questionText;
//    private String dapAn1;
//    private String dapAn2;
//    private String dapAn3;
//    private String dapAn4;
//    private String correctAnswer;
    private String image;
}
