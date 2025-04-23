package com.example.english.demo.dto.response;

import com.example.english.demo.entity.Comment;
import com.example.english.demo.entity.ToeicQuestion;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToeicExamResponse {
    private Long examId;
    private String examName;
    private int duration;
    private int numOfParticipants;
    private List<Comment> comments;
    private int numOfSections;
    private int numOfQuestions;
    private String description;
    private String audioFile;
}
