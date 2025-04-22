package com.example.english.demo.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToeicExamCreateRequest {
    private String examName;
    private int duration;
    private int numOfParticipants;
    private List<String> comments;
    private int numOfSections;
    private int numOfQuestions;
    private String description;
    private String audio;
    private transient MultipartFile audioFile;
}
