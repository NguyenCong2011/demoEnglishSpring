package com.example.english.demo.dto.request;

import com.example.english.demo.entity.ToeicQuestion;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToeicExamUpdateRequest {
    private String examName;
    private int duration;
    private int numOfParticipants;
    private int numOfSections;
    private int numOfQuestions;
    private String description;
}
