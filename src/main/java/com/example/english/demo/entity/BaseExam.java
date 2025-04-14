package com.example.english.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseExam {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String examName;
    private int duration;
    private int numOfParticipants;
    private List<String> comments;
    private int numOfSections;
    private int numOfQuestions;
    private String description;
}


