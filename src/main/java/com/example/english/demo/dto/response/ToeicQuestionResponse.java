package com.example.english.demo.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToeicQuestionResponse {
    private Long questionId;
    private String questionText;
    private String dapAn1;
    private String dapAn2;
    private String dapAn3;
    private String dapAn4;
    private String correctAnswer;
    private String image;
    private Integer part;
}
