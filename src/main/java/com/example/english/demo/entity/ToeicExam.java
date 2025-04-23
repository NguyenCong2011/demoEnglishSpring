package com.example.english.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@Entity
@Data
@NoArgsConstructor
public class ToeicExam{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "exam_id")
    private Long examId;
    @Column(unique = true)
    private String examName;
    private int duration;
    private int numOfParticipants;
    private int numOfSections;
    private int numOfQuestions;
    private String description;
    private String audio;

    @OneToMany(mappedBy = "toeicExam")
    private List<ToeicQuestion> toeicQuestions;

    @OneToMany(mappedBy = "toeicExam", cascade = CascadeType.ALL)
    private List<Comment> comments;

}
