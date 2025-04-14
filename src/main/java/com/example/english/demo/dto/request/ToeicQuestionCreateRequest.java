package com.example.english.demo.dto.request;

import com.example.english.demo.entity.ToeicExam;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToeicQuestionCreateRequest {
    private Long questionId;
    private String questionText;
    private String dapAn1;
    private String dapAn2;
    private String dapAn3;
    private String dapAn4;
    private String correctAnswer;
    private Integer part;
    private String image;
}
